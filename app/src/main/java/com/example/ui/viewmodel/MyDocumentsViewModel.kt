package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DocumentEntity
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyDocumentsViewModel(private val repository: DocumentRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredDocuments: StateFlow<List<DocumentEntity>> = combine(
        repository.allDocuments,
        _searchQuery,
        _selectedCategory
    ) { docs, query, category ->
        docs.filter { doc ->
            val matchesSearch = query.isBlank() ||
                    doc.title.contains(query, ignoreCase = true) ||
                    doc.summary?.contains(query, ignoreCase = true) == true
            val matchesCategory = when (category) {
                "Recent" -> System.currentTimeMillis() - doc.updatedAt <= 86400000 * 7 // within 7 days
                "Favorites" -> doc.isFavorite
                "PDF Exports" -> doc.fileType == "PDF_EXPORT" || doc.pdfPath != null
                "Drafts" -> doc.isDraft
                else -> true
            }
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(document: DocumentEntity) {
        viewModelScope.launch {
            repository.updateDocument(document.copy(isFavorite = !document.isFavorite))
        }
    }

    fun renameDocument(document: DocumentEntity, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateDocument(document.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    fun duplicateDocument(documentId: String) {
        viewModelScope.launch {
            repository.duplicateDocument(documentId)
        }
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            repository.deleteDocument(documentId)
        }
    }
}
