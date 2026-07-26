package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.BlockType
import com.example.data.local.DocumentBlock
import com.example.data.service.PdfExportManager
import com.example.ui.viewmodel.DocumentEditorViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditorScreen(
    viewModel: DocumentEditorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val document by viewModel.document.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()
    val exportedPdf by viewModel.exportedPdf.collectAsState()

    var activeBlockIndex by remember { mutableIntStateOf(0) }
    var showPdfPreviewDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }
    var aiTaskType by remember { mutableStateOf("SUMMARIZE") }
    var aiParamText by remember { mutableStateOf("") }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    // Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.insertImageBlock(activeBlockIndex, it.toString())
        }
    }

    if (document == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val doc = document!!

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 56.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            // AI Assistant Drawer Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFEA580C)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DocFusion AI Assistant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Powered by Gemini",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEA580C)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Tool Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val aiTools = listOf(
                        "SUMMARIZE" to "Summarize",
                        "REWRITE" to "Rewrite Tone",
                        "GRAMMAR" to "Fix Grammar",
                        "TRANSLATE" to "Translate",
                        "NOTES" to "Extract Notes"
                    )
                    items(aiTools) { (key, label) ->
                        FilterChip(
                            selected = aiTaskType == key,
                            onClick = {
                                aiTaskType = key
                                val textToProcess = doc.blocks.joinToString("\n") { it.text }
                                viewModel.runAiTask(key, textToProcess, aiParamText)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            leadingIcon = {
                                if (aiTaskType == key && aiLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                                }
                            }
                        )
                    }
                }

                if (aiTaskType == "TRANSLATE") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = aiParamText,
                        onValueChange = { aiParamText = it },
                        label = { Text("Target Language (e.g. Spanish, French, German)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (aiLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gemini is analyzing document...", fontSize = 14.sp)
                    }
                } else if (aiResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = aiResult!!,
                                fontSize = 13.sp,
                                color = Color(0xFF1E3A8A),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.appendAiResultToDocument(aiResult!!)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Insert into Document", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Tap any AI tool above to summarize, improve tone, correct grammar or translate your document instantly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        OutlinedTextField(
                            value = doc.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .testTag("document_title_input")
                        )
                        Text(
                            text = saveStatus,
                            fontSize = 11.sp,
                            color = if (saveStatus == "Saving...") Color(0xFFEA580C) else Color(0xFF16A34A)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.undo() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = { viewModel.redo() }) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            if (bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                                bottomSheetState.expand()
                            } else {
                                bottomSheetState.partialExpand()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = Color(0xFFEA580C)
                        )
                    }
                    IconButton(onClick = {
                        viewModel.exportToPdf(context) {
                            showPdfPreviewDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Preview & Export",
                            tint = Color(0xFF2563EB)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Formatting Toolbar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Block Style Selector Button
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.clickable { showTypeMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (doc.blocks.getOrNull(activeBlockIndex)?.type) {
                                            BlockType.HEADING1 -> "Heading 1"
                                            BlockType.HEADING2 -> "Heading 2"
                                            BlockType.BULLET_LIST -> "Bullet List"
                                            BlockType.NUMBERED_LIST -> "Numbered List"
                                            BlockType.QUOTE -> "Quote"
                                            else -> "Paragraph"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = showTypeMenu,
                                onDismissRequest = { showTypeMenu = false }
                            ) {
                                DropdownMenuItem(text = { Text("Heading 1") }, onClick = { showTypeMenu = false; viewModel.changeBlockType(activeBlockIndex, BlockType.HEADING1) })
                                DropdownMenuItem(text = { Text("Heading 2") }, onClick = { showTypeMenu = false; viewModel.changeBlockType(activeBlockIndex, BlockType.HEADING2) })
                                DropdownMenuItem(text = { Text("Paragraph") }, onClick = { showTypeMenu = false; viewModel.changeBlockType(activeBlockIndex, BlockType.PARAGRAPH) })
                                DropdownMenuItem(text = { Text("Bullet List") }, onClick = { showTypeMenu = false; viewModel.changeBlockType(activeBlockIndex, BlockType.BULLET_LIST) })
                                DropdownMenuItem(text = { Text("Numbered List") }, onClick = { showTypeMenu = false; viewModel.changeBlockType(activeBlockIndex, BlockType.NUMBERED_LIST) })
                                DropdownMenuItem(text = { Text("Quote Block") }, onClick = { showTypeMenu = false; viewModel.changeBlockType(activeBlockIndex, BlockType.QUOTE) })
                            }
                        }

                        // Formatting Toggle Buttons
                        val currentBlock = doc.blocks.getOrNull(activeBlockIndex)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.toggleBlockStyle(activeBlockIndex, isBold = !(currentBlock?.isBold ?: false)) }) {
                                Icon(
                                    Icons.Default.FormatBold,
                                    contentDescription = "Bold",
                                    tint = if (currentBlock?.isBold == true) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { viewModel.toggleBlockStyle(activeBlockIndex, isItalic = !(currentBlock?.isItalic ?: false)) }) {
                                Icon(
                                    Icons.Default.FormatItalic,
                                    contentDescription = "Italic",
                                    tint = if (currentBlock?.isItalic == true) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { viewModel.toggleBlockStyle(activeBlockIndex, isUnderline = !(currentBlock?.isUnderline ?: false)) }) {
                                Icon(
                                    Icons.Default.FormatUnderlined,
                                    contentDescription = "Underline",
                                    tint = if (currentBlock?.isUnderline == true) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showColorPicker = !showColorPicker }) {
                                Icon(Icons.Default.Palette, contentDescription = "Font Color", tint = Color(0xFF2563EB))
                            }
                            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Insert Image", tint = Color(0xFF2563EB))
                            }
                        }
                    }

                    // Color Picker Row
                    AnimatedVisibility(visible = showColorPicker) {
                        val colors = listOf("#0F172A", "#2563EB", "#1E40AF", "#DC2626", "#16A34A", "#D97706", "#7C3AED")
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(colors) { hex ->
                                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Black }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(2.dp, Color.White, CircleShape)
                                        .clickable {
                                            viewModel.updateBlockFontColor(activeBlockIndex, hex)
                                            showColorPicker = false
                                        }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }

            // Document Canvas (A4 Paper Style Card)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(doc.blocks, key = { _, block -> block.id }) { index, block ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (activeBlockIndex == index) 2.dp else 0.dp,
                                color = if (activeBlockIndex == index) Color(0xFF2563EB) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { activeBlockIndex = index },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Block Header Action bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Block ${index + 1} • ${block.type.name}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.moveBlockUp(index) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.moveBlockDown(index) },
                                        enabled = index < doc.blocks.lastIndex,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.duplicateBlock(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteBlock(index) },
                                        enabled = doc.blocks.size > 1,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Block Content Render
                            if (block.type == BlockType.IMAGE) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (!block.imageUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = block.imageUrl,
                                            contentDescription = "Inserted Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🖼️ Sample Document Image Asset", fontSize = 13.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = block.imageCaption ?: "",
                                        onValueChange = { caption ->
                                            viewModel.updateBlockText(index, caption)
                                        },
                                        label = { Text("Image Caption") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            } else {
                                val fontColor = try {
                                    Color(android.graphics.Color.parseColor(block.fontColorHex))
                                } catch (_: Exception) {
                                    MaterialTheme.colorScheme.onSurface
                                }

                                OutlinedTextField(
                                    value = block.text,
                                    onValueChange = { text ->
                                        activeBlockIndex = index
                                        viewModel.updateBlockText(index, text)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("block_input_$index"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    textStyle = TextStyle(
                                        fontSize = block.fontSizeSp.sp,
                                        fontWeight = if (block.isBold || block.type == BlockType.HEADING1 || block.type == BlockType.HEADING2) FontWeight.Bold else FontWeight.Normal,
                                        fontStyle = if (block.isItalic || block.type == BlockType.QUOTE) FontStyle.Italic else FontStyle.Normal,
                                        textDecoration = if (block.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                        color = fontColor
                                    ),
                                    placeholder = { Text("Type block text...", fontSize = block.fontSizeSp.sp) }
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.insertNewParagraph(doc.blocks.lastIndex) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("add_paragraph_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Paragraph Block", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // PDF Preview Modal
    if (showPdfPreviewDialog) {
        AlertDialog(
            onDismissRequest = { showPdfPreviewDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PDF Export Preview")
                }
            },
            text = {
                Column {
                    Text("A4 Print Document format generated successfully.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E3A8A))
                            Text("Author: ${doc.authorName}", fontSize = 12.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${doc.blocks.size} content blocks • A4 Standard Print", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        exportedPdf?.let { file ->
                            PdfExportManager.sharePdfFile(context, file)
                        }
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfPreviewDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
