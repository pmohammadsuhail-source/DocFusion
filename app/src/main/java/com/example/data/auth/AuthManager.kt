package com.example.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val uid: String = "user_10293",
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@docfusion.app",
    val photoUrl: String? = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
    val isLoggedIn: Boolean = true,
    val isPro: Boolean = true,
    val storageUsedMb: Float = 2450f, // 2.45 GB
    val storageLimitMb: Float = 15360f // 15 GB
)

object AuthManager {

    private val _currentUser = MutableStateFlow(UserProfile())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    fun loginWithEmail(email: String, pass: String): Boolean {
        val userName = email.substringBefore("@").replace(".", " ").capitalizeWords()
        _currentUser.value = _currentUser.value.copy(
            email = email,
            name = if (userName.isBlank()) "DocFusion User" else userName,
            isLoggedIn = true
        )
        return true
    }

    fun registerWithEmail(name: String, email: String, pass: String): Boolean {
        _currentUser.value = UserProfile(
            uid = "user_" + System.currentTimeMillis(),
            name = name.ifBlank { "New User" },
            email = email,
            isLoggedIn = true
        )
        return true
    }

    fun loginWithGoogle(): Boolean {
        _currentUser.value = UserProfile(
            uid = "google_9921",
            name = "Alex Rivera",
            email = "alex.rivera@gmail.com",
            isLoggedIn = true
        )
        return true
    }

    fun updateProfile(name: String, email: String) {
        _currentUser.value = _currentUser.value.copy(
            name = name,
            email = email
        )
    }

    fun logout() {
        _currentUser.value = _currentUser.value.copy(isLoggedIn = false)
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}
