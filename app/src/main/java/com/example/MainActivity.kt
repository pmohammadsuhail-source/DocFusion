package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DocumentEditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyDocumentsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.DocFusionTheme
import com.example.ui.viewmodel.DocumentEditorViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MyDocumentsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocFusionApp()
        }
    }
}

@Composable
fun DocFusionApp(
    mainViewModel: MainViewModel = viewModel()
) {
    val isDarkMode by mainViewModel.isDarkMode.collectAsState()
    val currentRoute by mainViewModel.currentRoute.collectAsState()
    val activeDocumentId by mainViewModel.activeDocumentId.collectAsState()

    val showBottomBar = currentRoute in listOf("home", "my_documents", "ai_assistant", "profile")

    DocFusionTheme(darkTheme = isDarkMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == "home",
                            onClick = { mainViewModel.navigateTo("home") },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2563EB),
                                selectedTextColor = Color(0xFF2563EB),
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == "my_documents",
                            onClick = { mainViewModel.navigateTo("my_documents") },
                            icon = { Icon(Icons.Default.Folder, contentDescription = "Documents") },
                            label = { Text("Documents", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2563EB),
                                selectedTextColor = Color(0xFF2563EB),
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == "ai_assistant",
                            onClick = { mainViewModel.navigateTo("ai_assistant") },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Studio", tint = Color(0xFFEA580C)) },
                            label = { Text("AI Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFEA580C),
                                selectedTextColor = Color(0xFFEA580C),
                                indicatorColor = Color(0xFFFFF7ED)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == "profile",
                            onClick = { mainViewModel.navigateTo("profile") },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2563EB),
                                selectedTextColor = Color(0xFF2563EB),
                                indicatorColor = Color(0xFFEFF6FF)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentRoute) {
                    "auth" -> {
                        AuthScreen(
                            onLoginSuccess = { mainViewModel.navigateTo("home") }
                        )
                    }

                    "home" -> {
                        HomeScreen(
                            mainViewModel = mainViewModel,
                            onOpenDocument = { docId ->
                                mainViewModel.navigateTo("editor", docId)
                            }
                        )
                    }

                    "editor" -> {
                        val docId = activeDocumentId ?: ""
                        val editorViewModel: DocumentEditorViewModel = viewModel(
                            key = docId,
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return DocumentEditorViewModel(mainViewModel.repository, docId) as T
                                }
                            }
                        )
                        DocumentEditorScreen(
                            viewModel = editorViewModel,
                            onBack = { mainViewModel.navigateTo("home") }
                        )
                    }

                    "my_documents" -> {
                        val docsViewModel: MyDocumentsViewModel = viewModel(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return MyDocumentsViewModel(mainViewModel.repository) as T
                                }
                            }
                        )
                        MyDocumentsScreen(
                            viewModel = docsViewModel,
                            onBack = { mainViewModel.navigateTo("home") },
                            onOpenDocument = { docId ->
                                mainViewModel.navigateTo("editor", docId)
                            }
                        )
                    }

                    "ai_assistant" -> {
                        AiAssistantScreen(
                            onBack = { mainViewModel.navigateTo("home") }
                        )
                    }

                    "profile" -> {
                        UserProfileScreen(
                            onBack = { mainViewModel.navigateTo("home") },
                            onLogout = { mainViewModel.navigateTo("auth") }
                        )
                    }

                    "settings" -> {
                        SettingsScreen(
                            mainViewModel = mainViewModel,
                            onBack = { mainViewModel.navigateTo("home") }
                        )
                    }

                    else -> {
                        HomeScreen(
                            mainViewModel = mainViewModel,
                            onOpenDocument = { docId ->
                                mainViewModel.navigateTo("editor", docId)
                            }
                        )
                    }
                }
            }
        }
    }
}
