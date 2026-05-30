package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.screens.MainContentRouter
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite/Room database instance
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.timeLeftDao()
        val repository = TimeLeftRepository(dao)

        val viewModelFactory = MainViewModelFactory(application, repository)

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                MainContentRouter(viewModel)
            }
        }
    }
}
