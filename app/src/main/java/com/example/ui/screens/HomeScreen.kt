package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthManager
import com.example.data.local.DocumentEntity
import com.example.data.service.FileImportManager
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val containerColor: Color,
    val iconColor: Color,
    val actionKey: String
)

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    onOpenDocument: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val documents by mainViewModel.repository.allDocuments.collectAsState(initial = emptyList())
    val userProfile by AuthManager.currentUser.collectAsState()

    var showDocxDialog by remember { mutableStateOf(false) }
    var selectedDocForMenu by remember { mutableStateOf<DocumentEntity?>(null) }
    var renameDialogDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // File launcher for TXT import
    val txtPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val importedDoc = FileImportManager.importTextFromUri(context, it)
            scope.launch {
                mainViewModel.repository.saveDocument(importedDoc)
                onOpenDocument(importedDoc.id)
            }
        }
    }

    val quickActions = remember {
        listOf(
            QuickActionItem("New Doc", "Blank canvas", Icons.Default.Add, Color(0xFFEFF6FF), Color(0xFF2563EB), "NEW_DOC"),
            QuickActionItem("Import", "Word & TXT", Icons.Default.Description, Color(0xFFECFDF5), Color(0xFF059669), "IMPORT_DOCX"),
            QuickActionItem("OCR Scan", "Photo to text", Icons.Default.Image, Color(0xFFFFEDD5), Color(0xFFEA580C), "IMPORT_IMG"),
            QuickActionItem("Export", "High-res PDF", Icons.Default.PictureAsPdf, Color(0xFFF3E8FF), Color(0xFF9333EA), "EXPORT_PDF")
        )
    }

    val secondaryActions = remember {
        listOf(
            QuickActionItem("All Documents", "Browse files", Icons.Default.Folder, Color(0xFFEFF6FF), Color(0xFF2563EB), "MY_DOCS"),
            QuickActionItem("TXT Import", "Plain text", Icons.Default.TextSnippet, Color(0xFFF5F3FF), Color(0xFF7C3AED), "IMPORT_TXT"),
            QuickActionItem("AI Studio", "Smart polish", Icons.Default.AutoAwesome, Color(0xFFFFF7ED), Color(0xFFEA580C), "AI_ASSISTANT"),
            QuickActionItem("Settings", "App config", Icons.Default.Settings, Color(0xFFF1F5F9), Color(0xFF475569), "SETTINGS")
        )
    }

    val filteredDocs = if (searchQuery.isBlank()) documents else {
        documents.filter { it.title.contains(searchQuery, ignoreCase = true) || (it.summary?.contains(searchQuery, ignoreCase = true) == true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
    ) {
        // Sleek Top App Bar
        Surface(
            color = Color.White,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Logo & Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "DocFusion Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "DocFusion",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Search Toggle & Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSearchExpanded) Color(0xFFEFF6FF) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF475569)
                            )
                        }

                        // User Initials Badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE))
                                .border(1.dp, Color(0xFFBFDBFE), CircleShape)
                                .clickable { mainViewModel.navigateTo("profile") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.name.take(2).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }
                }

                // Expandable Search Bar
                if (isSearchExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search documents...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // Sleek AI Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { mainViewModel.navigateTo("ai_assistant") },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        // Pill Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI ASSISTANT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Enhance your writing",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.3).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Summarize, rewrite, or translate documents in seconds with DocFusion AI.",
                            fontSize = 13.sp,
                            color = Color(0xFFDBEAFE),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { mainViewModel.navigateTo("ai_assistant") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Open AI Studio",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Tools Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK TOOLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.2.sp
                )
                TextButton(
                    onClick = { mainViewModel.navigateTo("my_documents") },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4-Column Quick Tools Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickActions.forEach { action ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_card_${action.actionKey}")
                            .clickable {
                                when (action.actionKey) {
                                    "NEW_DOC" -> scope.launch {
                                        val newDoc = mainViewModel.repository.createNewBlankDocument()
                                        onOpenDocument(newDoc.id)
                                    }
                                    "IMPORT_DOCX" -> showDocxDialog = true
                                    "IMPORT_IMG" -> scope.launch {
                                        val imgDoc = FileImportManager.createFromImageImport()
                                        mainViewModel.repository.saveDocument(imgDoc)
                                        onOpenDocument(imgDoc.id)
                                    }
                                    "EXPORT_PDF" -> {
                                        val targetDoc = documents.firstOrNull()
                                        if (targetDoc != null) onOpenDocument(targetDoc.id)
                                        else mainViewModel.navigateTo("my_documents")
                                    }
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(action.containerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.title,
                                    tint = action.iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = action.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary Quick Actions Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(secondaryActions) { sec ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable {
                            when (sec.actionKey) {
                                "MY_DOCS" -> mainViewModel.navigateTo("my_documents")
                                "IMPORT_TXT" -> txtPickerLauncher.launch("text/plain")
                                "AI_ASSISTANT" -> mainViewModel.navigateTo("ai_assistant")
                                "SETTINGS" -> mainViewModel.navigateTo("settings")
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(sec.containerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = sec.icon,
                                    contentDescription = null,
                                    tint = sec.iconColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = sec.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = sec.subtitle,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Starter Templates Section
            Text(
                text = "STARTER TEMPLATES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val templates = listOf("Executive Report", "Meeting Minutes", "Project Proposal")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                items(templates) { template ->
                    Card(
                        modifier = Modifier
                            .width(150.dp)
                            .height(90.dp)
                            .clickable {
                                scope.launch {
                                    val doc = mainViewModel.repository.createFromTemplate(template)
                                    onOpenDocument(doc.id)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = template,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Files Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT FILES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredDocs.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching documents" else "No documents yet",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Tap 'New Doc' or import files to get started.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredDocs.take(5).forEach { doc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_item_${doc.id}")
                                .clickable { onOpenDocument(doc.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (doc.fileType == "PDF_EXPORT") Color(0xFFE0F2FE)
                                                else Color(0xFFEFF6FF)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (doc.fileType == "PDF_EXPORT") Icons.Default.PictureAsPdf else Icons.Default.Description,
                                            contentDescription = null,
                                            tint = if (doc.fileType == "PDF_EXPORT") Color(0xFF0284C7) else Color(0xFF2563EB),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = doc.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${doc.fileType} • ${doc.fileSizeKb} KB • Modified ${dateFormat.format(Date(doc.updatedAt))}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Box {
                                    IconButton(onClick = { selectedDocForMenu = doc }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = selectedDocForMenu?.id == doc.id,
                                        onDismissRequest = { selectedDocForMenu = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Open & Edit") },
                                            onClick = {
                                                selectedDocForMenu = null
                                                onOpenDocument(doc.id)
                                            },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Rename") },
                                            onClick = {
                                                selectedDocForMenu = null
                                                renameText = doc.title
                                                renameDialogDoc = doc
                                            },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Duplicate") },
                                            onClick = {
                                                selectedDocForMenu = null
                                                scope.launch {
                                                    mainViewModel.repository.duplicateDocument(doc.id)
                                                }
                                            },
                                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                selectedDocForMenu = null
                                                scope.launch {
                                                    mainViewModel.repository.deleteDocument(doc.id)
                                                }
                                            },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Import DOCX Modal
    if (showDocxDialog) {
        AlertDialog(
            onDismissRequest = { showDocxDialog = false },
            title = { Text("Import Word DOCX") },
            text = { Text("Select or generate a sample Microsoft Word (.docx) file to convert into an editable DocFusion document.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDocxDialog = false
                        scope.launch {
                            val sampleDoc = FileImportManager.createSampleDocxImport()
                            mainViewModel.repository.saveDocument(sampleDoc)
                            onOpenDocument(sampleDoc.id)
                        }
                    }
                ) {
                    Text("Import Sample DOCX", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDocxDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog
    renameDialogDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { renameDialogDoc = null },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = renameDialogDoc ?: return@TextButton
                        renameDialogDoc = null
                        scope.launch {
                            mainViewModel.repository.updateDocument(
                                target.copy(title = renameText, updatedAt = System.currentTimeMillis())
                            )
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogDoc = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
