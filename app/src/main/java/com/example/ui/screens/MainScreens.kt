package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.*
import com.example.ui.components.Biolab3DView
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

// --- TAB 1: MODELS EXPLORER HOME SCREEN ---
@Composable
fun ModelsHomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val savedIds by viewModel.savedModelIds.collectAsState()
    val progressList by viewModel.modelProgressList.collectAsState()

    val filteredList = remember(searchQuery, selectedCategory, savedIds) {
        BiologyData.models.filter { model ->
            val matchesSearch = model.name.contains(searchQuery, ignoreCase = true) || 
                                model.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> savedIds.contains(model.id)
                else -> model.category == selectedCategory
            }
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicMidnight)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title and description block
        Text(
            text = "3D Interactive Lab",
            style = MaterialTheme.typography.headlineMedium,
            color = ScannerCyan
        )
        Text(
            text = "Tap a biometric model to start active simulation learning.",
            style = MaterialTheme.typography.bodySmall,
            color = PaleSlate.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search text field
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search biological systems...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ScannerCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkCardBg,
                unfocusedContainerColor = DarkCardBg,
                focusedTextColor = PaleSlate,
                unfocusedTextColor = PaleSlate,
                focusedIndicatorColor = ScannerCyan,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .testTag("search_field"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Selection Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == "All",
                    onClick = { selectedCategory = "All" },
                    label = { Text("All Systems") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScannerCyan.copy(alpha = 0.3f),
                        selectedLabelColor = ScannerCyan
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCategory == "Favorites",
                    onClick = { selectedCategory = "Favorites" },
                    label = { Text("⭐ Saved") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = InfoAmber.copy(alpha = 0.3f),
                        selectedLabelColor = InfoAmber
                    )
                )
            }
            items(BiologyData.categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MetabolicGreen.copy(alpha = 0.3f),
                        selectedLabelColor = MetabolicGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid contents / Lists
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Empty list",
                        tint = InfoAmber,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No biological systems found matching criteria.", color = PaleSlate.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredList) { model ->
                    val isFav = savedIds.contains(model.id)
                    val progress = progressList.find { it.modelId == model.id }
                    val isViewed = progress?.isViewed ?: false

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectedModelId = model.id
                                viewModel.currentScreen = "details"
                                viewModel.markModelAsViewed(model.id)
                            }
                            .testTag("model_card_${model.id}"),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        border = BorderStroke(1.dp, if (isFav) InfoAmber.copy(alpha = 0.5f) else Color(0x3300E5FF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mini 3D preview viewport inside card
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CosmicMidnight),
                                contentAlignment = Alignment.Center
                            ) {
                                Biolab3DView(
                                    modelId = model.id,
                                    modifier = Modifier.size(70.dp),
                                    zoomScale = 0.8f,
                                    rotationY = 20f
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = model.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PaleSlate
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = model.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (model.category) {
                                        "Human Anatomy" -> ScannerCyan
                                        "Cell Biology" -> MetabolicGreen
                                        "Genetics" -> HelixViolet
                                        else -> InfoAmber
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isViewed) {
                                        Icon(Icons.Default.Check, contentDescription = "Mastered", tint = MetabolicGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Explored", style = MaterialTheme.typography.bodySmall, color = MetabolicGreen)
                                    } else {
                                        Text("Not explored yet", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.5f))
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { viewModel.toggleFavorite(model.id) }) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Save favorite",
                                        tint = if (isFav) InfoAmber else PaleSlate.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- MODEL DETAILED STUDY & SIMULATION SCREEN ---
@Composable
fun ModelDetailScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val model = BiologyData.models.find { it.id == viewModel.selectedModelId } ?: return
    val savedIds by viewModel.savedModelIds.collectAsState()
    val isFav = savedIds.contains(model.id)

    var currentRotX by remember { mutableStateOf(0f) }
    var currentRotY by remember { mutableStateOf(0f) }
    var currentZoom by remember { mutableStateOf(1.0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "StudyLights")
    val laserSwipe by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicMidnight)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.currentScreen = "home" }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ScannerCyan)
            }
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleMedium,
                color = PaleSlate,
                maxLines = 1
            )
            IconButton(onClick = { viewModel.toggleFavorite(model.id) }) {
                Icon(
                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) InfoAmber else PaleSlate
                )
            }
        }

        // Top Area: Dynamic 3D Renderer Dashboard Viewport (40% Screen space)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(DarkSlate)
                .testTag("3d_viewport"),
            contentAlignment = Alignment.Center
        ) {
            // Radial grid/scaffolding visual guidelines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                drawCircle(color = Color(0x1100C853), radius = 60.dp.toPx(), style = Stroke(width = 1f))
                drawCircle(color = Color(0x0600C853), radius = 120.dp.toPx(), style = Stroke(width = 1f))
                // Horizontal crosshairs
                drawLine(color = Color(0x11FFFFFF), start = Offset(0f, cy), end = Offset(size.width, cy))
                // Sliding cybersecurity scanner laser
                drawLine(
                    color = ScannerCyan.copy(alpha = 0.25f),
                    start = Offset(0f, laserSwipe),
                    end = Offset(size.width, laserSwipe),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Central Renderer block
            Biolab3DView(
                modelId = model.id,
                modifier = Modifier.fillMaxSize(),
                zoomScale = currentZoom,
                rotationX = currentRotX,
                rotationY = currentRotY,
                onRotationChanged = { rx, ry ->
                    currentRotX = rx
                    currentRotY = ry
                },
                actionTriggered = viewModel.activeFeatureTriggered,
                speedFactor = viewModel.activeSpeedFactor,
                selectedFeatureIndex = viewModel.activeFeatureIndex,
                onFeatureLabelTapped = { label ->
                    viewModel.activeFeatureTriggered = !viewModel.activeFeatureTriggered
                }
            )

            // Controls overlays inside viewport bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xD30B100D))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Feature action trigger button
                val actLabel = when (model.id) {
                    1 -> "Pulse Cell Organelles"
                    2 -> if (viewModel.activeFeatureTriggered) "Re-Zip DNA Helix" else "Unzip Base Pairs"
                    3 -> "Tachometer Cycle: Pulse"
                    5 -> "Trigger Action Impulse"
                    7 -> "Feed Food Nutrient bolus"
                    9 -> "Strike to Photosynthesize"
                    10 -> "Shield Activation / Battlers"
                    else -> "Toggle Active Mode"
                }

                Button(
                    onClick = { viewModel.activeFeatureTriggered = !viewModel.activeFeatureTriggered },
                    colors = ButtonDefaults.buttonColors(containerColor = MetabolicGreen, contentColor = CosmicMidnight),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("action_btn")
                ) {
                    Text(actLabel, style = MaterialTheme.typography.bodySmall)
                }

                // Zoom layout control
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Zoom", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.7f))
                    Slider(
                        value = currentZoom,
                        onValueChange = { currentZoom = it },
                        valueRange = 0.5f..1.8f,
                        modifier = Modifier.width(100.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = ScannerCyan,
                            activeTrackColor = ScannerCyan
                        )
                    )
                }
            }
        }

        // Bottom Area: Interactive Pedagogical Lesson Panels
        Surface(
            modifier = Modifier.weight(1.0f).fillMaxWidth(),
            color = CosmicMidnight
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Pedagogical value message box
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ScannerCyan.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, ScannerCyan.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Pedagogical Value", style = MaterialTheme.typography.titleSmall, color = ScannerCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(model.pedagogicalValue, style = MaterialTheme.typography.bodySmall, color = PaleSlate)
                        }
                    }
                }

                // Core details text
                item {
                    Text("Detailed Curriculum Overview", style = MaterialTheme.typography.titleMedium, color = ScannerCyan)
                    Text("Study each of the 10 core physiological principles below. Highlighted entries trigger visual canvas alerts.", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.7f))
                }

                // Sequenced numbered lesson cards
                items(model.infoPoints.size) { index ->
                    val pointStr = model.infoPoints[index]
                    val parts = pointStr.split(": ", limit = 2)
                    val label = parts.getOrNull(0) ?: "Fact"
                    val desc = parts.getOrNull(1) ?: pointStr

                    val isHighlighted = viewModel.activeFeatureIndex == index

                    Card(
                        onClick = {
                            // Highlights the item on list, and triggers visual overlay in canvas!
                            viewModel.activeFeatureIndex = if (isHighlighted) -1 else index
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHighlighted) ScannerCyan.copy(alpha = 0.15f) else DarkCardBg
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isHighlighted) ScannerCyan else Color(0x1100E5FF)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(if (isHighlighted) ScannerCyan else DarkSlate, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isHighlighted) CosmicMidnight else PaleSlate
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isHighlighted) ScannerCyan else PaleSlate
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.82f))
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 2: INTERACTIVE BIOLOGY QUIZ LAB ---
@Composable
fun QuizLabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scoresList by viewModel.quizScoresList.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicMidnight)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Active Quiz Laboratory", style = MaterialTheme.typography.headlineMedium, color = ScannerCyan)
        Text("Review concepts via targeted multiple-choice challenges.", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.showQuizFinished) {
            // Display Results Screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(2.dp, MetabolicGreen)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Session Completed! 🎓", style = MaterialTheme.typography.headlineSmall, color = MetabolicGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${viewModel.activeQuizScore} / ${BiologyData.quizQuestions.size}",
                        style = MaterialTheme.typography.displayMedium,
                        color = ScannerCyan
                    )
                    Text("Accurate Answers Checked", style = MaterialTheme.typography.bodyMedium, color = PaleSlate)

                    Spacer(modifier = Modifier.height(24.dp))

                    val message = when {
                        viewModel.activeQuizScore == BiologyData.quizQuestions.size -> "Flawless score! You have achieved true cellular mastery!"
                        viewModel.activeQuizScore >= 7 -> "Outstanding biological accuracy! You are ready for college studies."
                        else -> "Good effort. Review information cards to double-check core cell functions."
                    }
                    Text(message, style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.8f))

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.startNewQuiz() },
                        colors = ButtonDefaults.buttonColors(containerColor = MetabolicGreen, contentColor = CosmicMidnight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("restart_quiz_button")
                    ) {
                        Text("Initiate New Assessment")
                    }
                }
            }
        } else {
            // Standard ongoing Quiz UI card
            val currentQuestion = BiologyData.quizQuestions[viewModel.quizActiveIndex]

            // Question counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${viewModel.quizActiveIndex + 1} of ${BiologyData.quizQuestions.size}",
                    style = MaterialTheme.typography.titleSmall,
                    color = ScannerCyan
                )
                Text(
                    text = "Accuracy: ${viewModel.activeQuizScore} hits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetabolicGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (viewModel.quizActiveIndex + 1) / BiologyData.quizQuestions.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ScannerCyan,
                trackColor = DarkSlate
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question Box card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, ScannerCyan.copy(alpha = 0.5f))
            ) {
                Text(
                    text = currentQuestion.question,
                    style = MaterialTheme.typography.titleMedium,
                    color = PaleSlate,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Options cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1.0f)
            ) {
                currentQuestion.options.forEachIndexed { optIndex, text ->
                    val isSelected = viewModel.selectedOptionIndex == optIndex
                    val isCorr = currentQuestion.correctAnswerIndex == optIndex

                    val cardCol = when {
                        viewModel.isSubmitted && isCorr -> MetabolicGreen.copy(alpha = 0.2f)
                        viewModel.isSubmitted && isSelected && !isCorr -> Color(0x33EF5350)
                        isSelected -> ScannerCyan.copy(alpha = 0.2f)
                        else -> DarkCardBg
                    }

                    val borderCol = when {
                        viewModel.isSubmitted && isCorr -> MetabolicGreen
                        viewModel.isSubmitted && isSelected && !isCorr -> Color(0xFFEF5350)
                        isSelected -> ScannerCyan
                        else -> Color(0x22FFFFFF)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !viewModel.isSubmitted) { viewModel.selectedOptionIndex = optIndex }
                            .testTag("option_${optIndex}"),
                        colors = CardDefaults.cardColors(containerColor = cardCol),
                        border = BorderStroke(1.5.dp, borderCol)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (optIndex) {
                                    0 -> "A"
                                    1 -> "B"
                                    2 -> "C"
                                    else -> "D"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) ScannerCyan else PaleSlate.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = text, style = MaterialTheme.typography.bodySmall, color = PaleSlate)
                        }
                    }
                }
            }

            // Explanatory Panel
            if (viewModel.isSubmitted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x2200E5FF)),
                    border = BorderStroke(1.dp, ScannerCyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Teacher Explanation", style = MaterialTheme.typography.titleSmall, color = ScannerCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentQuestion.explanation, style = MaterialTheme.typography.bodySmall, color = PaleSlate)
                    }
                }
            }

            // Lower Action panel button
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (!viewModel.isSubmitted) {
                        viewModel.submitAnswer()
                    } else {
                        viewModel.nextQuizStep()
                    }
                },
                enabled = viewModel.selectedOptionIndex != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isSubmitted) ScannerCyan else MetabolicGreen,
                    contentColor = CosmicMidnight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("quiz_action_button")
            ) {
                Text(
                    text = if (!viewModel.isSubmitted) "Submit Diagnostics" else "Advance to Next Topic",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

// --- TAB 3: AI BIOLOGY ASSISTANT SCREEN ---
@Composable
fun AiAssistantScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var chatMessage by remember { mutableStateOf("") }
    val listState = rememberScrollState()

    // Autoscroll chat on message updates
    LaunchedEffect(viewModel.chatHistory.size) {
        listState.animateScrollTo(listState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicMidnight)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AI Biology Assistant", style = MaterialTheme.typography.headlineMedium, color = ScannerCyan)
                Text("Query any curriculum syllabus or biological concept here.", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.7f))
            }
            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear thread", tint = ScannerCyan)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Chat scroll dialog
        Column(
            modifier = Modifier
                .weight(1.0f)
                .verticalScroll(listState)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            viewModel.chatHistory.forEach { (text, isUser) ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) DarkSlate else DarkCardBg
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isUser) ScannerCyan.copy(alpha = 0.5f) else Color(0x3300C853)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "You" else "Professor Protoplasm 👨‍🔬",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isUser) ScannerCyan else MetabolicGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = text, style = MaterialTheme.typography.bodySmall, color = PaleSlate)
                        }
                    }
                }
            }

            if (viewModel.isAiLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        border = BorderStroke(1.dp, Color(0x3300C853))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MetabolicGreen, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cell synthesis in progress...", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Suggested prompts shelf
        Spacer(modifier = Modifier.height(10.dp))
        Text("Teacher Suggestions", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.4f))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Explain mitochondria energy production simplemente",
                "What's the difference between animal and plant cells?",
                "Tell me how DNA transcription encodes traits"
            )
            items(suggestions) { keyword ->
                SuggestionChip(
                    onClick = { viewModel.sendChatMessage(keyword) },
                    label = { Text(keyword, maxLines = 1) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = ScannerCyan,
                        containerColor = DarkSlate
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = chatMessage,
                onValueChange = { chatMessage = it },
                placeholder = { Text("Ask about ATP, DNA, cells...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    focusedTextColor = PaleSlate,
                    unfocusedTextColor = PaleSlate,
                    focusedIndicatorColor = ScannerCyan,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1.0f)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("ai_input_text"),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val prompt = chatMessage
                    chatMessage = ""
                    viewModel.sendChatMessage(prompt)
                },
                modifier = Modifier
                    .background(ScannerCyan, CircleShape)
                    .size(48.dp)
                    .testTag("ai_send_button"),
                enabled = chatMessage.isNotEmpty() && !viewModel.isAiLoading
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send prompt", tint = CosmicMidnight)
            }
        }
    }
}

// --- TAB 4: MOCK BIOMETRIC AR LAB ---
@Composable
fun ArLabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    var arModelIndex by remember { mutableStateOf(1) }
    val arModel = remember(arModelIndex) { BiologyData.models.find { it.id == arModelIndex }!! }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicMidnight)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("AR Biometric Simulator", style = MaterialTheme.typography.headlineMedium, color = ScannerCyan)
        Text("Overlay biological specimens inside physical environments dynamically.", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(14.dp))

        // Main camera/grid layout
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (hasCameraPermission) Color.DarkGray else DarkCardBg)
                .border(2.dp, ScannerCyan, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!hasCameraPermission) {
                // Background grid shader
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (i in 0..size.width.toInt() step 50) {
                        drawLine(color = Color(0x3300C853), start = Offset(i.toFloat(), 0f), end = Offset(i.toFloat(), size.height))
                    }
                    for (i in 0..size.height.toInt() step 50) {
                        drawLine(color = Color(0x3300C853), start = Offset(0f, i.toFloat()), end = Offset(size.width, i.toFloat()))
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Camera AR", tint = ScannerCyan, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Interactive Studio Grid", style = MaterialTheme.typography.titleMedium, color = PaleSlate)
                    Text("Enable camera permission to enter immersive local background overlays, or study the 3D projection directly in the grid below.", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan, contentColor = CosmicMidnight)
                    ) {
                        Text("Permit Live Camera Overlay")
                    }
                }
            } else {
                // If has camera permission, we mock the laboratory view backdrop with cyber contours!
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0x730B100D))
                    // Scanning cyber bounding targets
                    val gap = 40f
                    drawLine(color = ScannerCyan, start = Offset(gap, gap), end = Offset(gap + 40f, gap), strokeWidth = 4f)
                    drawLine(color = ScannerCyan, start = Offset(gap, gap), end = Offset(gap, gap + 40f), strokeWidth = 4f)

                    drawLine(color = ScannerCyan, start = Offset(size.width - gap, gap), end = Offset(size.width - gap - 40f, gap), strokeWidth = 4f)
                    drawLine(color = ScannerCyan, start = Offset(size.width - gap, gap), end = Offset(size.width - gap, gap + 40f), strokeWidth = 4f)

                    drawLine(color = ScannerCyan, start = Offset(gap, size.height - gap), end = Offset(gap + 40f, size.height - gap), strokeWidth = 4f)
                    drawLine(color = ScannerCyan, start = Offset(gap, size.height - gap), end = Offset(gap, size.height - gap - 40f), strokeWidth = 4f)

                    drawLine(color = ScannerCyan, start = Offset(size.width - gap, size.height - gap), end = Offset(size.width - gap - 40f, size.height - gap), strokeWidth = 4f)
                    drawLine(color = ScannerCyan, start = Offset(size.width - gap, size.height - gap), end = Offset(size.width - gap, size.height - gap - 40f), strokeWidth = 4f)
                }
            }

            // Interactive rotating model projection on top of viewport
            Biolab3DView(
                modelId = arModelIndex,
                modifier = Modifier.size(240.dp),
                zoomScale = 1.3f
            )

            // Current specimen label card
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicMidnight.copy(alpha = 0.8f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Specimen Core: ${arModel.name}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ScannerCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        //specimen selector lists
        Text("Select Specimen", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.5f))
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BiologyData.models) { item ->
                FilterChip(
                    selected = arModelIndex == item.id,
                    onClick = { arModelIndex = item.id },
                    label = { Text(item.name.take(15) + "...") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScannerCyan.copy(alpha = 0.3f),
                        selectedLabelColor = ScannerCyan
                    )
                )
            }
        }
    }
}

// --- TAB 5: LEARNER'S STATS/PROGRESS DASHBOARD SCREEN ---
@Composable
fun ProgressDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.modelProgressList.collectAsState()
    val scoresList by viewModel.quizScoresList.collectAsState()
    val streakRecord by viewModel.userStreak.collectAsState()

    val completedPercentage = remember(progressList) {
        val count = progressList.count { it.isViewed }
        if (BiologyData.models.isEmpty()) 0 else (count * 100) / BiologyData.models.size
    }

    val averageScore = remember(scoresList) {
        if (scoresList.isEmpty()) 0 else {
            val totalHits = scoresList.sumOf { it.score }
            val totalQuestions = scoresList.sumOf { it.totalQuestions }
            if (totalQuestions == 0) 0 else (totalHits * 100) / totalQuestions
        }
    }

    val currentStreak = streakRecord?.streakCount ?: 0
    val totalConsults = streakRecord?.totalAiQuestions ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicMidnight)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Biometric Assessment", style = MaterialTheme.typography.headlineMedium, color = ScannerCyan)
        Text("Review curriculum accomplishments and daily streaks log.", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(16.dp))

        // Streaks and summary indicators row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Daily Streak card
            Card(
                modifier = Modifier.weight(1.0f),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, Color(0xFFFF9800))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Daily Streak 🔥", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$currentStreak", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFFFB74D))
                    Text("Consecutive Days", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.5f))
                }
            }

            // AI interactions card
            Card(
                modifier = Modifier.weight(1.0f),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, ScannerCyan)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("AI Consults 🔬", style = MaterialTheme.typography.bodySmall, color = ScannerCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$totalConsults", style = MaterialTheme.typography.headlineMedium, color = ScannerCyan)
                    Text("Inquiries asked", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress mastery gauges Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = BorderStroke(1.dp, ScannerCyan.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Syllabus Mastery", style = MaterialTheme.typography.titleMedium, color = PaleSlate)
                    Text("Completed exploration tasks", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "$completedPercentage%", style = MaterialTheme.typography.displayMedium, color = MetabolicGreen)
                }

                // Simple Circular progress ring chart
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { completedPercentage / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = MetabolicGreen,
                        trackColor = DarkSlate,
                        strokeWidth = 10.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quiz metrics score performance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = BorderStroke(1.dp, MetabolicGreen.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quiz Diagnostics hits", style = MaterialTheme.typography.titleMedium, color = PaleSlate)
                    Text("Composite accuracy percent", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "$averageScore%", style = MaterialTheme.typography.displayMedium, color = ScannerCyan)
                }

                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { averageScore / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = ScannerCyan,
                        trackColor = DarkSlate,
                        strokeWidth = 10.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category completion summary table list
        Text("Tracked Curriculum Milestones", style = MaterialTheme.typography.titleSmall, color = ScannerCyan)
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 90.dp)
        ) {
            BiologyData.categories.forEach { category ->
                val categoryModelsCount = BiologyData.models.count { it.category == category }
                val viewCount = progressList.count { p ->
                    val model = BiologyData.models.find { it.id == p.modelId }
                    model?.category == category && p.isViewed
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSlate)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(category, style = MaterialTheme.typography.titleSmall, color = PaleSlate)
                            Text("$viewCount of $categoryModelsCount mastered.", style = MaterialTheme.typography.bodySmall, color = PaleSlate.copy(alpha = 0.5f))
                        }

                        Icon(
                            imageVector = if (viewCount == categoryModelsCount && categoryModelsCount > 0) Icons.Default.Check else Icons.Default.Info,
                            contentDescription = "Category Done",
                            tint = if (viewCount == categoryModelsCount && categoryModelsCount > 0) MetabolicGreen else ScannerCyan
                        )
                    }
                }
            }
        }
    }
}
