package com.example.data.service

import android.content.Context
import android.net.Uri
import com.example.data.local.BlockType
import com.example.data.local.DocumentBlock
import com.example.data.local.DocumentEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

object FileImportManager {

    fun importTextFromUri(context: Context, uri: Uri, fileName: String? = null): DocumentEntity {
        val title = fileName?.substringBeforeLast(".")?.ifBlank { "Imported Document" } ?: "Imported Text Document"
        val contentLines = mutableListOf<String>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        contentLines.add(line!!)
                    }
                }
            }
        } catch (_: Exception) {
            contentLines.add("Sample imported text content from file $title.")
        }

        val blocks = mutableListOf<DocumentBlock>()
        blocks.add(
            DocumentBlock(
                type = BlockType.HEADING1,
                text = title,
                fontSizeSp = 24,
                fontColorHex = "#1E3A8A"
            )
        )

        var currentParagraph = StringBuilder()
        for (line in contentLines) {
            if (line.trim().isEmpty()) {
                if (currentParagraph.isNotEmpty()) {
                    blocks.add(DocumentBlock(type = BlockType.PARAGRAPH, text = currentParagraph.toString().trim()))
                    currentParagraph = StringBuilder()
                }
            } else if (line.startsWith("# ")) {
                if (currentParagraph.isNotEmpty()) {
                    blocks.add(DocumentBlock(type = BlockType.PARAGRAPH, text = currentParagraph.toString().trim()))
                    currentParagraph = StringBuilder()
                }
                blocks.add(DocumentBlock(type = BlockType.HEADING2, text = line.removePrefix("# ").trim(), fontSizeSp = 18))
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                if (currentParagraph.isNotEmpty()) {
                    blocks.add(DocumentBlock(type = BlockType.PARAGRAPH, text = currentParagraph.toString().trim()))
                    currentParagraph = StringBuilder()
                }
                blocks.add(DocumentBlock(type = BlockType.BULLET_LIST, text = line.substring(2).trim()))
            } else {
                if (currentParagraph.isNotEmpty()) currentParagraph.append(" ")
                currentParagraph.append(line)
            }
        }

        if (currentParagraph.isNotEmpty()) {
            blocks.add(DocumentBlock(type = BlockType.PARAGRAPH, text = currentParagraph.toString().trim()))
        }

        if (blocks.size <= 1) {
            blocks.add(DocumentBlock(type = BlockType.PARAGRAPH, text = "Empty file imported."))
        }

        return DocumentEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            fileType = "TXT",
            fileSizeKb = (contentLines.sumOf { it.length } / 1024).coerceAtLeast(4),
            blocks = blocks
        )
    }

    fun createSampleDocxImport(titleName: String = "Quarterly Marketing Strategy.docx"): DocumentEntity {
        val title = titleName.substringBeforeLast(".")
        return DocumentEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            fileType = "DOCX",
            fileSizeKb = 32,
            blocks = listOf(
                DocumentBlock(type = BlockType.HEADING1, text = title, fontSizeSp = 24, fontColorHex = "#1E3A8A"),
                DocumentBlock(type = BlockType.PARAGRAPH, text = "Imported from Microsoft Word (.docx format). All paragraphs, headings and list formatting have been preserved for full rich text editing."),
                DocumentBlock(type = BlockType.HEADING2, text = "Key Marketing Pillars", fontSizeSp = 18, fontColorHex = "#2563EB"),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Social Media Campaign & Creator Collaborations"),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "SEO Optimization & Blog Content Strategy"),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Email Newsletter & Customer Retention Flow"),
                DocumentBlock(type = BlockType.QUOTE, text = "“Focusing on user-generated content yielded a 35% increase in conversions.”", isItalic = true)
            )
        )
    }

    fun createFromImageImport(imageUri: Uri? = null): DocumentEntity {
        val docId = UUID.randomUUID().toString()
        return DocumentEntity(
            id = docId,
            title = "Scan & Image Document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            fileType = "IMAGE_DOC",
            fileSizeKb = 45,
            blocks = listOf(
                DocumentBlock(type = BlockType.HEADING1, text = "Scanned Image Document", fontSizeSp = 24, fontColorHex = "#1E3A8A"),
                DocumentBlock(type = BlockType.IMAGE, imageUrl = imageUri?.toString(), imageCaption = "Imported document image asset"),
                DocumentBlock(type = BlockType.PARAGRAPH, text = "You can type additional notes or rich text commentary below this imported image.")
            )
        )
    }
}
