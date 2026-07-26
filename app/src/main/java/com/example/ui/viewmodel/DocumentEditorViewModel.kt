package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.local.BlockType
import com.example.data.local.DocumentBlock
import com.example.data.local.DocumentEntity
import com.example.data.repository.DocumentRepository
import com.example.data.service.PdfExportManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Stack
import java.util.UUID

class DocumentEditorViewModel(
    private val repository: DocumentRepository,
    private val documentId: String
) : ViewModel() {

    private val _document = MutableStateFlow<DocumentEntity?>(null)
    val document: StateFlow<DocumentEntity?> = _document.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveStatus = MutableStateFlow("Saved")
    val saveStatus: StateFlow<String> = _saveStatus.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    private val _exportedPdf = MutableStateFlow<File?>(null)
    val exportedPdf: StateFlow<File?> = _exportedPdf.asStateFlow()

    private val undoStack = Stack<List<DocumentBlock>>()
    private val redoStack = Stack<List<DocumentBlock>>()

    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getDocumentById(documentId).collect { doc ->
                if (_document.value == null && doc != null) {
                    _document.value = doc
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        val current = _document.value ?: return
        _document.value = current.copy(title = newTitle, updatedAt = System.currentTimeMillis())
        triggerAutoSave()
    }

    private fun pushUndoState() {
        _document.value?.blocks?.let { currentBlocks ->
            val snapshot = currentBlocks.map { it.copy() }
            undoStack.push(snapshot)
            redoStack.clear()
        }
    }

    fun undo() {
        if (!undoStack.isEmpty()) {
            val currentBlocks = _document.value?.blocks?.map { it.copy() } ?: emptyList()
            redoStack.push(currentBlocks)
            val previous = undoStack.pop()
            _document.value = _document.value?.copy(blocks = previous)
            triggerAutoSave()
        }
    }

    fun redo() {
        if (!redoStack.isEmpty()) {
            val currentBlocks = _document.value?.blocks?.map { it.copy() } ?: emptyList()
            undoStack.push(currentBlocks)
            val next = redoStack.pop()
            _document.value = _document.value?.copy(blocks = next)
            triggerAutoSave()
        }
    }

    fun updateBlockText(index: Int, text: String) {
        val current = _document.value ?: return
        if (index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        newBlocks[index] = newBlocks[index].copy(text = text)
        _document.value = current.copy(blocks = newBlocks, updatedAt = System.currentTimeMillis())
        triggerAutoSave()
    }

    fun toggleBlockStyle(index: Int, isBold: Boolean? = null, isItalic: Boolean? = null, isUnderline: Boolean? = null) {
        val current = _document.value ?: return
        if (index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        val target = newBlocks[index]
        newBlocks[index] = target.copy(
            isBold = isBold ?: target.isBold,
            isItalic = isItalic ?: target.isItalic,
            isUnderline = isUnderline ?: target.isUnderline
        )
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun updateBlockFontSize(index: Int, sizeSp: Int) {
        val current = _document.value ?: return
        if (index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        newBlocks[index] = newBlocks[index].copy(fontSizeSp = sizeSp)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun updateBlockFontColor(index: Int, colorHex: String) {
        val current = _document.value ?: return
        if (index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        newBlocks[index] = newBlocks[index].copy(fontColorHex = colorHex)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun updateBlockAlignment(index: Int, alignment: String) {
        val current = _document.value ?: return
        if (index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        newBlocks[index] = newBlocks[index].copy(alignment = alignment)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun changeBlockType(index: Int, newType: BlockType) {
        val current = _document.value ?: return
        if (index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        val fontSize = when (newType) {
            BlockType.HEADING1 -> 24
            BlockType.HEADING2 -> 18
            else -> 16
        }
        val fontColor = when (newType) {
            BlockType.HEADING1 -> "#1E3A8A"
            BlockType.HEADING2 -> "#2563EB"
            else -> "#0F172A"
        }
        newBlocks[index] = newBlocks[index].copy(type = newType, fontSizeSp = fontSize, fontColorHex = fontColor)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun moveBlockUp(index: Int) {
        val current = _document.value ?: return
        if (index <= 0 || index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        val item = newBlocks.removeAt(index)
        newBlocks.add(index - 1, item)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun moveBlockDown(index: Int) {
        val current = _document.value ?: return
        if (index >= current.blocks.lastIndex || index < 0) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        val item = newBlocks.removeAt(index)
        newBlocks.add(index + 1, item)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun deleteBlock(index: Int) {
        val current = _document.value ?: return
        if (current.blocks.size <= 1 || index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        newBlocks.removeAt(index)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun duplicateBlock(index: Int) {
        val current = _document.value ?: return
        if (index !in current.blocks.indices) return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        val dup = newBlocks[index].copy(id = UUID.randomUUID().toString())
        newBlocks.add(index + 1, dup)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun insertNewParagraph(afterIndex: Int) {
        val current = _document.value ?: return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        val newBlock = DocumentBlock(type = BlockType.PARAGRAPH, text = "")
        val insertPos = (afterIndex + 1).coerceAtMost(newBlocks.size)
        newBlocks.add(insertPos, newBlock)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    fun insertImageBlock(afterIndex: Int, imageUriStr: String) {
        val current = _document.value ?: return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        val imgBlock = DocumentBlock(
            type = BlockType.IMAGE,
            imageUrl = imageUriStr,
            imageCaption = "Inserted Image Asset"
        )
        val insertPos = (afterIndex + 1).coerceAtMost(newBlocks.size)
        newBlocks.add(insertPos, imgBlock)
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }

    private fun triggerAutoSave() {
        _saveStatus.value = "Saving..."
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1200)
            saveDocument()
        }
    }

    fun saveDocument() {
        val current = _document.value ?: return
        viewModelScope.launch {
            _isSaving.value = true
            val updated = current.copy(updatedAt = System.currentTimeMillis(), isDraft = false)
            repository.saveDocument(updated)
            _isSaving.value = false
            _saveStatus.value = "Saved"
        }
    }

    fun exportToPdf(context: Context, onComplete: (File) -> Unit) {
        val current = _document.value ?: return
        viewModelScope.launch {
            val file = PdfExportManager.generatePdf(context, current)
            _exportedPdf.value = file
            repository.saveDocument(current.copy(pdfPath = file.absolutePath, fileType = "PDF_EXPORT"))
            onComplete(file)
        }
    }

    fun runAiTask(task: String, inputText: String, param: String = "") {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiResult.value = null
            val result = when (task) {
                "SUMMARIZE" -> GeminiAiService.summarizeText(inputText)
                "REWRITE" -> GeminiAiService.rewriteText(inputText, param.ifBlank { "Professional" })
                "GRAMMAR" -> GeminiAiService.correctGrammar(inputText)
                "TRANSLATE" -> GeminiAiService.translateText(inputText, param.ifBlank { "Spanish" })
                "NOTES" -> GeminiAiService.generateNotes(inputText)
                else -> GeminiAiService.summarizeText(inputText)
            }
            _aiLoading.value = false
            _aiResult.value = result
        }
    }

    fun appendAiResultToDocument(text: String) {
        val current = _document.value ?: return
        pushUndoState()
        val newBlocks = current.blocks.toMutableList()
        newBlocks.add(
            DocumentBlock(
                type = BlockType.QUOTE,
                text = "🤖 AI Assistant Note:\n$text",
                isItalic = true,
                fontColorHex = "#1E40AF"
            )
        )
        _document.value = current.copy(blocks = newBlocks)
        triggerAutoSave()
    }
}
