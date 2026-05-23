package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val repository: BioRepository) : AndroidViewModel(application) {

    // --- State Navigation ---
    var currentScreen by mutableStateOf("home") // home, details, quiz, chatbot, dashboard, ar_lab, favorites
    var selectedModelId by mutableStateOf(1)
    
    // --- 3D interaction configuration state parameters ---
    var zoomScale by mutableStateOf(1.0f)
    var rotationX by mutableStateOf(0f)
    var rotationY by mutableStateOf(0f)
    var activeFeatureTriggered by mutableStateOf(false)
    var activeSpeedFactor by mutableStateOf(1.0f)
    var activeFeatureIndex by mutableStateOf(-1)

    // --- Database-linked Reactive flows ---
    val savedModelIds: StateFlow<List<Int>> = repository.savedModelIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val modelProgressList: StateFlow<List<ModelProgressEntity>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizScoresList: StateFlow<List<QuizScoreEntity>> = repository.allScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStreak: StateFlow<UserStreakEntity?> = repository.userStreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Interactive Quiz Control variables ---
    var quizActiveIndex by mutableStateOf(0)
    var selectedOptionIndex by mutableStateOf<Int?>(null)
    var isSubmitted by mutableStateOf(false)
    var activeQuizScore by mutableStateOf(0)
    var showQuizFinished by mutableStateOf(false)

    // --- chatbot AI assistant variables ---
    var chatHistory = mutableStateListOf<Pair<String, Boolean>>() // Pair of <MessageText, IsUserMessage>
    var isAiLoading by mutableStateOf(false)

    init {
        // Pre-populate chat with a warm welcome from the biology professor
        chatHistory.add(
            Pair(
                "Welcome to the BioLab 3D assistant! 🔬🧬\n\nI am Professor Protoplasm, your cell-by-cell guide. I can explain double helix transcription, capillary alveoli, mitochondria ATP processes, and more.\n\nAsk me anything, or tap one of the shortcuts below!",
                false
            )
        )
    }

    // --- Repository Access Methods ---
    fun toggleFavorite(modelId: Int) {
        viewModelScope.launch {
            val isFav = savedModelIds.value.contains(modelId)
            repository.toggleFavorite(modelId, !isFav)
        }
    }

    fun markModelAsViewed(modelId: Int) {
        viewModelScope.launch {
            repository.recordModelViewed(modelId)
        }
    }

    // --- Quiz flow logic ---
    fun startNewQuiz() {
        quizActiveIndex = 0
        selectedOptionIndex = null
        isSubmitted = false
        activeQuizScore = 0
        showQuizFinished = false
    }

    fun submitAnswer() {
        if (selectedOptionIndex == null || isSubmitted) return
        isSubmitted = true
        val currentQuestion = BiologyData.quizQuestions[quizActiveIndex]
        if (selectedOptionIndex == currentQuestion.correctAnswerIndex) {
            activeQuizScore++
        }
    }

    fun nextQuizStep() {
        if (!isSubmitted) return
        
        if (quizActiveIndex < BiologyData.quizQuestions.size - 1) {
            quizActiveIndex++
            selectedOptionIndex = null
            isSubmitted = false
        } else {
            // Save quiz scores chunk to Room Database
            viewModelScope.launch {
                val totalPercent = (activeQuizScore * 100) / BiologyData.quizQuestions.size
                // Associate average score with main test entry model (ModelId=1)
                repository.saveQuizScore(modelId = 1, score = activeQuizScore, total = BiologyData.quizQuestions.size)
                showQuizFinished = true
            }
        }
    }

    // --- AI Chatbot methods ---
    fun sendChatMessage(inputMessage: String) {
        val prompt = inputMessage.trim()
        if (prompt.isEmpty() || isAiLoading) return

        chatHistory.add(Pair(prompt, true))
        isAiLoading = true

        viewModelScope.launch {
            // Log interaction increments to stats
            repository.incrementAiQuestionCount()
            
            val reply = GeminiRetrofitClient.askAssistant(prompt, chatHistory.map { it })
            chatHistory.add(Pair(reply, false))
            isAiLoading = false
        }
    }

    fun clearChat() {
        chatHistory.clear()
        chatHistory.add(
            Pair(
                "Nucleus connection restored! How can I assist your biological studies today? Ask about cellular transport, organ cavities, or the circulatory system.",
                false
            )
        )
    }
}

// Utility class to implement mutableStateList additions inside Viewmodel cleanly
fun <T> mutableStateListOf(vararg elements: T) = androidx.compose.runtime.mutableStateListOf<T>().apply { addAll(elements) }

class MainViewModelFactory(
    private val application: Application,
    private val repository: BioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
