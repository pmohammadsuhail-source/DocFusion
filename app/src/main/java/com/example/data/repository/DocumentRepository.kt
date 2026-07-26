package com.example.data.repository

import com.example.data.local.BlockType
import com.example.data.local.DocumentBlock
import com.example.data.local.DocumentDao
import com.example.data.local.DocumentEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DocumentRepository(private val documentDao: DocumentDao) {

    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    fun getDocumentById(id: String): Flow<DocumentEntity?> = documentDao.getDocumentById(id)

    fun searchDocuments(query: String): Flow<List<DocumentEntity>> = documentDao.searchDocuments(query)

    suspend fun saveDocument(document: DocumentEntity) {
        documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: DocumentEntity) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(id: String) {
        documentDao.deleteDocumentById(id)
    }

    suspend fun duplicateDocument(id: String) {
        val doc = documentDao.getDocumentByIdSync(id) ?: return
        val newDoc = doc.copy(
            id = UUID.randomUUID().toString(),
            title = "${doc.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        documentDao.insertDocument(newDoc)
    }

    suspend fun createNewBlankDocument(): DocumentEntity {
        val newDoc = DocumentEntity(
            id = UUID.randomUUID().toString(),
            title = "Untitled Document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDraft = true,
            blocks = listOf(
                DocumentBlock(
                    type = BlockType.HEADING1,
                    text = "Untitled Document",
                    fontSizeSp = 24,
                    fontColorHex = "#1E3A8A"
                ),
                DocumentBlock(
                    type = BlockType.PARAGRAPH,
                    text = "Start typing your rich text here or use AI Assistant to generate content...",
                    fontSizeSp = 16
                )
            )
        )
        documentDao.insertDocument(newDoc)
        return newDoc
    }

    suspend fun createFromTemplate(templateName: String): DocumentEntity {
        val blocks = when (templateName) {
            "Executive Report" -> listOf(
                DocumentBlock(type = BlockType.HEADING1, text = "Q3 Executive Summary & Market Analysis", fontSizeSp = 24, fontColorHex = "#1E3A8A"),
                DocumentBlock(type = BlockType.HEADING2, text = "1. Financial Highlights", fontSizeSp = 18, fontColorHex = "#2563EB"),
                DocumentBlock(type = BlockType.PARAGRAPH, text = "Revenue increased by 28% quarter-over-quarter driven by high enterprise demand for our cloud document solutions."),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Gross margin expanded to 74.2%"),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Customer retention rate achieved record 96.5%"),
                DocumentBlock(type = BlockType.HEADING2, text = "2. Key Growth Initiatives", fontSizeSp = 18, fontColorHex = "#2563EB"),
                DocumentBlock(type = BlockType.PARAGRAPH, text = "We launched DocFusion AI features, reducing doc creation time for enterprise users by over 40%.")
            )
            "Meeting Minutes" -> listOf(
                DocumentBlock(type = BlockType.HEADING1, text = "Product Strategy Sync - Meeting Minutes", fontSizeSp = 24, fontColorHex = "#1E3A8A"),
                DocumentBlock(type = BlockType.PARAGRAPH, text = "Date: July 26, 2026 | Attendees: Alex R., Sarah T., David K.", isItalic = true),
                DocumentBlock(type = BlockType.HEADING2, text = "Discussion Topics", fontSizeSp = 18, fontColorHex = "#2563EB"),
                DocumentBlock(type = BlockType.NUMBERED_LIST, text = "Finalize Material 3 UI design polish for v2.4 launch."),
                DocumentBlock(type = BlockType.NUMBERED_LIST, text = "Integrate Gemini 3.5 Flash for grammar & summarization."),
                DocumentBlock(type = BlockType.NUMBERED_LIST, text = "Expand PDF rendering capabilities with custom styling."),
                DocumentBlock(type = BlockType.HEADING2, text = "Action Items", fontSizeSp = 18, fontColorHex = "#2563EB"),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Alex: Deploy updated build to QA testing by Friday."),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Sarah: Complete user onboarding illustration assets.")
            )
            "Project Proposal" -> listOf(
                DocumentBlock(type = BlockType.HEADING1, text = "DocFusion Enterprise AI Modernization Proposal", fontSizeSp = 24, fontColorHex = "#1E3A8A"),
                DocumentBlock(type = BlockType.HEADING2, text = "Objective", fontSizeSp = 18, fontColorHex = "#2563EB"),
                DocumentBlock(type = BlockType.PARAGRAPH, text = "Transform corporate documentation workflows with instant local PDF rendering and Gemini AI automated summaries."),
                DocumentBlock(type = BlockType.HEADING2, text = "Scope of Work", fontSizeSp = 18, fontColorHex = "#2563EB"),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Rich document composition & real-time offline saving."),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Multi-language document translation & grammar check."),
                DocumentBlock(type = BlockType.BULLET_LIST, text = "Seamless DOCX and TXT import/export engine.")
            )
            else -> listOf(
                DocumentBlock(type = BlockType.HEADING1, text = "Welcome to DocFusion AI", fontSizeSp = 24, fontColorHex = "#1E3A8A"),
                DocumentBlock(type = BlockType.PARAGRAPH, text = "DocFusion is your professional AI-powered document editor for Android.")
            )
        }

        val doc = DocumentEntity(
            id = UUID.randomUUID().toString(),
            title = templateName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            blocks = blocks
        )
        documentDao.insertDocument(doc)
        return doc
    }

    suspend fun seedInitialDataIfEmpty() {
        if (documentDao.getDocumentCount() == 0) {
            val starter1 = DocumentEntity(
                id = "doc_welcome_1",
                title = "Welcome to DocFusion AI Editor",
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis() - 3600000,
                isFavorite = true,
                isDraft = false,
                fileType = "DOCX",
                fileSizeKb = 24,
                authorName = "Alex Rivera",
                summary = "Getting started guide for DocFusion rich document editor and AI assistant features.",
                blocks = listOf(
                    DocumentBlock(
                        id = "b1",
                        type = BlockType.HEADING1,
                        text = "Welcome to DocFusion AI Editor",
                        fontSizeSp = 24,
                        fontColorHex = "#1E3A8A"
                    ),
                    DocumentBlock(
                        id = "b2",
                        type = BlockType.PARAGRAPH,
                        text = "DocFusion combines the power of Microsoft Word, Google Docs and WPS Office with modern AI document synthesis built specifically for Android.",
                        fontSizeSp = 16
                    ),
                    DocumentBlock(
                        id = "b3",
                        type = BlockType.HEADING2,
                        text = "🚀 Key Features at Your Fingertips:",
                        fontSizeSp = 18,
                        fontColorHex = "#2563EB"
                    ),
                    DocumentBlock(
                        id = "b4",
                        type = BlockType.BULLET_LIST,
                        text = "Rich Text Editing: Bold, Italic, Underline, Font size, Colors, and Alignment."
                    ),
                    DocumentBlock(
                        id = "b5",
                        type = BlockType.BULLET_LIST,
                        text = "AI Assistant: Summarize documents, rewrite tone, correct grammar, translate languages, and generate meeting notes."
                    ),
                    DocumentBlock(
                        id = "b6",
                        type = BlockType.BULLET_LIST,
                        text = "PDF Engine: Instant A4 PDF Preview, high-resolution rendering, export & sharing."
                    ),
                    DocumentBlock(
                        id = "b7",
                        type = BlockType.BULLET_LIST,
                        text = "File Imports: Import DOCX, TXT, and Images directly into editable document blocks."
                    ),
                    DocumentBlock(
                        id = "b8",
                        type = BlockType.QUOTE,
                        text = "“DocFusion makes document creation on Android feel effortless, intelligent, and beautifully styled.”",
                        isItalic = true,
                        fontColorHex = "#1E40AF"
                    )
                )
            )

            val starter2 = DocumentEntity(
                id = "doc_sample_report",
                title = "Q3 Product Growth & AI Strategy Report",
                createdAt = System.currentTimeMillis() - 172800000,
                updatedAt = System.currentTimeMillis() - 7200000,
                isFavorite = false,
                isDraft = false,
                fileType = "DOCX",
                fileSizeKb = 38,
                authorName = "Alex Rivera",
                summary = "Quarterly growth metric report detailing AI feature adoption and document processing performance.",
                blocks = listOf(
                    DocumentBlock(
                        id = "r1",
                        type = BlockType.HEADING1,
                        text = "Q3 Product Growth & Strategy Report",
                        fontSizeSp = 24,
                        fontColorHex = "#1E3A8A"
                    ),
                    DocumentBlock(
                        id = "r2",
                        type = BlockType.PARAGRAPH,
                        text = "This report highlights key operational milestones for Q3 2026, focusing on mobile document editing productivity and user engagement."
                    ),
                    DocumentBlock(
                        id = "r3",
                        type = BlockType.HEADING2,
                        text = "Performance Highlights",
                        fontSizeSp = 18,
                        fontColorHex = "#2563EB"
                    ),
                    DocumentBlock(
                        id = "r4",
                        type = BlockType.NUMBERED_LIST,
                        text = "Mobile active users grew by 42% following the launch of Gemini AI Assistant."
                    ),
                    DocumentBlock(
                        id = "r5",
                        type = BlockType.NUMBERED_LIST,
                        text = "PDF export speed improved by 65% using native Android graphics hardware acceleration."
                    ),
                    DocumentBlock(
                        id = "r6",
                        type = BlockType.NUMBERED_LIST,
                        text = "Document auto-save reliability achieved 99.98% uptime without data loss."
                    )
                )
            )

            documentDao.insertDocument(starter1)
            documentDao.insertDocument(starter2)
        }
    }
}
