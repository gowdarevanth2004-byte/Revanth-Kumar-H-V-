package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.BioRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite/Room local database persistent drivers
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.bioDao()
        val repository = BioRepository(dao)

        val viewModelFactory = MainViewModelFactory(application, repository)

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (viewModel.currentScreen != "details") {
                            BottomNavBarView(viewModel)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        CrossScreenRouter(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun CrossScreenRouter(viewModel: MainViewModel) {
    AnimatedContent(
        targetState = viewModel.currentScreen,
        transitionSpec = {
            slideInVertically { height -> height } togetherWith slideOutVertically { height -> -height }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            "home" -> ModelsHomeScreen(viewModel)
            "details" -> ModelDetailScreen(viewModel)
            "quiz" -> QuizLabScreen(viewModel)
            "chatbot" -> AiAssistantScreen(viewModel)
            "ar_lab" -> ArLabScreen(viewModel)
            "dashboard" -> ProgressDashboardScreen(viewModel)
            else -> ModelsHomeScreen(viewModel)
        }
    }
}

@Composable
fun BottomNavBarView(viewModel: MainViewModel) {
    NavigationBar(
        containerColor = DarkCardBg,
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .height(64.dp)
    ) {
        val current = viewModel.currentScreen

        NavigationBarItem(
            selected = current == "home",
            onClick = { viewModel.currentScreen = "home" },
            icon = { Icon(Icons.Default.Home, contentDescription = "Models") },
            label = { Text("Models", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScannerCyan,
                selectedTextColor = ScannerCyan,
                indicatorColor = ScannerCyan.copy(alpha = 0.15f),
                unselectedIconColor = PaleSlate.copy(alpha = 0.5f),
                unselectedTextColor = PaleSlate.copy(alpha = 0.5f)
            )
        )

        NavigationBarItem(
            selected = current == "quiz",
            onClick = {
                viewModel.currentScreen = "quiz"
                viewModel.startNewQuiz()
            },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Quiz Lab") },
            label = { Text("Quiz", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScannerCyan,
                selectedTextColor = ScannerCyan,
                indicatorColor = ScannerCyan.copy(alpha = 0.15f),
                unselectedIconColor = PaleSlate.copy(alpha = 0.5f),
                unselectedTextColor = PaleSlate.copy(alpha = 0.5f)
            )
        )

        NavigationBarItem(
            selected = current == "chatbot",
            onClick = { viewModel.currentScreen = "chatbot" },
            icon = { Icon(Icons.Default.Send, contentDescription = "AI Assistant") },
            label = { Text("AI Tutor", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScannerCyan,
                selectedTextColor = ScannerCyan,
                indicatorColor = ScannerCyan.copy(alpha = 0.15f),
                unselectedIconColor = PaleSlate.copy(alpha = 0.5f),
                unselectedTextColor = PaleSlate.copy(alpha = 0.5f)
            )
        )

        NavigationBarItem(
            selected = current == "ar_lab",
            onClick = { viewModel.currentScreen = "ar_lab" },
            icon = { Icon(Icons.Default.Star, contentDescription = "AR Lab") },
            label = { Text("AR View", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScannerCyan,
                selectedTextColor = ScannerCyan,
                indicatorColor = ScannerCyan.copy(alpha = 0.15f),
                unselectedIconColor = PaleSlate.copy(alpha = 0.5f),
                unselectedTextColor = PaleSlate.copy(alpha = 0.5f)
            )
        )

        NavigationBarItem(
            selected = current == "dashboard",
            onClick = { viewModel.currentScreen = "dashboard" },
            icon = { Icon(Icons.Default.Person, contentDescription = "Dashboard") },
            label = { Text("Progress", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScannerCyan,
                selectedTextColor = ScannerCyan,
                indicatorColor = ScannerCyan.copy(alpha = 0.15f),
                unselectedIconColor = PaleSlate.copy(alpha = 0.5f),
                unselectedTextColor = PaleSlate.copy(alpha = 0.5f)
            )
        )
    }
}
