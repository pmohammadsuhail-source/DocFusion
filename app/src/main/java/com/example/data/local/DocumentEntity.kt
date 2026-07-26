package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Untitled Document",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDraft: Boolean = false,
    val fileType: String = "DOCX", // DOCX, TXT, PDF_EXPORT
    val fileSizeKb: Int = 12,
    val blocks: List<DocumentBlock> = emptyList(),
    val pdfPath: String? = null,
    val authorName: String = "Alex Rivera",
    val summary: String? = null
)
