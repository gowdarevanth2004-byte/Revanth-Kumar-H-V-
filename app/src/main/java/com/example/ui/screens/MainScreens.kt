package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.app.WallpaperManager
import android.content.ComponentName
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MyWallpaperService
import com.example.data.*
import com.example.ui.components.TimeLeftEarthView
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// Theme helper: Returns colors according to selected premium theme
@Composable
fun getThemeColorTuple(themeId: String): Pair<Color, Color> {
    return when (themeId) {
        "cyberpunk" -> Color(0xFFFF007F) to Color(0xFFFD7E14) // Hot Pink, Sunset Orange
        "aurora" -> Color(0xFFA855F7) to Color(0xFF3B82F6) // Aurora Violet, Arctic Blue
        "oled" -> Color(0xFFF59E0B) to Color(0xFF1E1E1E) // High contrast Gold, Charcoal
        "batman" -> Color(0xFFFFFFFF) to Color(0xFF1A1B20) // The Dark Knight Monochrome (B&W)
        else -> Color(0xFF00FFCC) to Color(0xFF0A2540) // Neon Matrix, Deep Space
    }
}

@Composable
fun MainContentRouter(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
    ) {
        // Shared immersive 3D rotating globe background centered in space
        TimeLeftEarthView(
            modifier = Modifier.fillMaxSize(),
            themeColor = getThemeColorTuple(viewModel.selectedThemeId).first,
            nightColor = getThemeColorTuple(viewModel.selectedThemeId).second,
            batterySaving = viewModel.isBatterySaving,
            userQuote = viewModel.userMotto,
            themeId = viewModel.selectedThemeId
        )

        // Semi-opaque glass overlay for superior layout contrast
        val glassAlpha = if (viewModel.selectedThemeId == "batman") 0.58f else 0.82f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712).copy(alpha = glassAlpha))
        )

        // Screen selection cross-router with scrolling containers
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavigationHud(viewModel) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = viewModel.currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "DashboardTransition"
                ) { screen ->
                    when (screen) {
                        "home" -> HomeScreenDashboard(viewModel)
                        "wallpaper" -> LiveWallpaperSetupScreen(viewModel)
                        "stats" -> ScreenTimeStatsScreen(viewModel)
                        "settings" -> PersonalSettingsScreen(viewModel)
                        else -> HomeScreenDashboard(viewModel)
                    }
                }
            }
        }
    }
}

// --- SCREEN 1: MOTIVATIONAL DASHBOARD HOME SCREEN ---
@Composable
fun HomeScreenDashboard(viewModel: MainViewModel) {
    val themeColor = getThemeColorTuple(viewModel.selectedThemeId).first
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Visual digital typography heading
        Text(
            text = "TIME LEFT TODAY",
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 3.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Circular Pulsing Timer Core
        Box(
            modifier = Modifier
                .size(230.dp)
                .testTag("circular_timer_core"),
            contentAlignment = Alignment.Center
        ) {
            // Background track
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = themeColor.copy(alpha = 0.08f),
                    radius = size.width / 2,
                    style = Stroke(width = 8.dp.toPx())
                )
            }

            // Animated sweeping indicator outline
            val baseRotation = rememberInfiniteTransition("IndicatorSweeps")
            val sweepProgress by baseRotation.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(12000, easing = LinearEasing)
                ),
                label = "IndicatorSweep"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = themeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * viewModel.percentLeftToday,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx())
                )
            }

            // Central time text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = viewModel.hoursLeftToday,
                        fontSize = 42.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = themeColor
                    )
                    Text(
                        text = "h",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp, end = 6.dp)
                    )
                    Text(
                        text = viewModel.minutesLeftToday,
                        fontSize = 42.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = themeColor
                    )
                    Text(
                        text = "m",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp, end = 6.dp)
                    )
                }
                Text(
                    text = "${viewModel.secondsLeftToday} seconds remaining",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Personal motto card banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Mantra Alert",
                    tint = themeColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${viewModel.userMotto}\"",
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress breakdown block: Year Progress & Productivity Rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Year progress card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "THIS YEAR",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${viewModel.daysLeftThisYear} DAYS",
                        color = themeColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Left to accomplish goals",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            // Screen productivity score card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "PRODUCTIVITY",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${viewModel.productivityScore}%",
                        color = themeColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Focus efficiency rating",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- SCREEN 2: LIVE WALLPAPER INSTRUCTIONS & CONFIG PORTAL ---
@Composable
fun LiveWallpaperSetupScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val themeColor = getThemeColorTuple(viewModel.selectedThemeId).first
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "LIVE WALLPAPER",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = themeColor
        )
        Text(
            "Configure motivational live countdowns on home / lock screen",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Wallpaper dynamic mockup container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF030712).copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.4f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive miniature earth view inside card
                TimeLeftEarthView(
                    modifier = Modifier.fillMaxSize(),
                    themeColor = themeColor,
                    nightColor = getThemeColorTuple(viewModel.selectedThemeId).second,
                    batterySaving = viewModel.isBatterySaving
                )
                
                // Text overlay previews
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("WALLPAPER MOCKUP", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${viewModel.hoursLeftToday}:${viewModel.minutesLeftToday}:${viewModel.secondsLeftToday}", color = themeColor, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(viewModel.userMotto.uppercase(), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Steps instructions list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("How to apply wallpaper:", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text("1. Confirm your motto & premium styling are set in the Settings tab.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("2. Press the 'Set Live Wallpaper' action trigger button below.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("3. Choose to install on either Home Screen, Lock Screen, or Both screens when prompted by the OS.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Launcher trigger button
        Button(
            onClick = {
                try {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(context, MyWallpaperService::class.java)
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColor, contentColor = Color(0xFF030712)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("apply_wallpaper_btn")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Launch wallpaper chooser")
            Spacer(modifier = Modifier.width(8.dp))
            Text("SET LIVE WALLPAPER", fontWeight = FontWeight.Bold)
        }
    }
}

// --- SCREEN 3: SCREEN TIME TRACKING & PRODUCTIVITY HISTORY ---
@Composable
fun ScreenTimeStatsScreen(viewModel: MainViewModel) {
    val themeColor = getThemeColorTuple(viewModel.selectedThemeId).first
    val scrollState = rememberScrollState()
    val dailyStats by viewModel.dailyStatsList.collectAsState()

    // Trigger update on open
    LaunchedEffect(Unit) {
        viewModel.checkUsageStatsPermission()
        viewModel.refreshScreenTimeStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "SCREEN TIME TRACKER",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = themeColor
        )
        Text(
            "Measure physical habits using Android Usage Stats systems",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Permission handler card block
        if (!viewModel.isUsagePermissionGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Lock", tint = Color(0xFFFCA5A5), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Permission Required", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "Time Left needs standard usage monitoring authorization to fetch screen minutes securely.",
                        color = Color(0xFFFECACA),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = { viewModel.openUsageSettings() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Authorize Access Setting")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live statistics indicators
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SCREEN TIME USED TODAY", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${viewModel.screenTimeMinutesToday.toInt()} mins",
                        color = themeColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Button(
                    onClick = { viewModel.refreshScreenTimeStats() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = CircleShape,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload Stats", tint = themeColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Vector Weekly Progress Area Charts drawn purely in Jetpack Compose Canvas!
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "7-DAY HISTORY LOG",
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Line Plot Canvas rendering
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val entries = dailyStats.filter { it.dateStr != "MIGRATION_STATUS_DO_NOT_DELETE" }.take(7).reversed()
                    
                    if (entries.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxTime = maxOf(300f, entries.maxOf { it.screenTimeMinutes.toFloat() })
                            val pointsCount = entries.size
                            val stepX = size.width / (pointsCount - 1).coerceAtLeast(1)

                            // Generate path vectors
                            val path = androidx.compose.ui.graphics.Path()
                            val fillPath = androidx.compose.ui.graphics.Path()

                            entries.forEachIndexed { idx, stat ->
                                val x = idx * stepX
                                val ratio = stat.screenTimeMinutes.toFloat() / maxTime
                                val y = size.height - (ratio * (size.height - 20f))

                                if (idx == 0) {
                                    path.moveTo(x, y)
                                    fillPath.moveTo(x, size.height)
                                    fillPath.lineTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                    fillPath.lineTo(x, y)
                                }

                                if (idx == pointsCount - 1) {
                                    fillPath.lineTo(x, size.height)
                                    fillPath.close()
                                }

                                // Plot item nodes
                                drawCircle(
                                    color = themeColor,
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }

                            // Render area gradient shading
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(themeColor.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )

                            // Draw central trace outline
                            drawPath(
                                path = path,
                                color = themeColor,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    } else {
                        // Empty outline fallback
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading daily statistics history...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                    }
                }

                // Days axis labels
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val entries = dailyStats.filter { it.dateStr != "MIGRATION_STATUS_DO_NOT_DELETE" }.take(7).reversed()
                    if (entries.isNotEmpty()) {
                        entries.forEach { stat ->
                            val readableDay = try {
                                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val formatter = SimpleDateFormat("EEE", Locale.getDefault())
                                formatter.format(parser.parse(stat.dateStr)!!)
                            } catch (e: Exception) {
                                "Day"
                            }
                            Text(
                                readableDay.uppercase(),
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text("-", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Educational feedback tip
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Focus Assessment Rule:", fontWeight = FontWeight.Bold, color = themeColor, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Your productivity percentage scales down if foreground screentime exceeds your custom limit in the settings tab. Keep track of metrics to optimize your lifestyle.",
                    fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- SCREEN 4: PERSONAL MOTIVATIONAL CONFIGURATIONS SCREEN ---
@Composable
fun PersonalSettingsScreen(viewModel: MainViewModel) {
    val themeColor = getThemeColorTuple(viewModel.selectedThemeId).first
    val scrollState = rememberScrollState()

    var inputMotto by remember { mutableStateOf(viewModel.userMotto) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "MOTIVATIONAL OPTIONS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = themeColor
        )
        Text(
            "Personalize parameters, mantra texts, and design schemes",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Custom written motto text portal
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("YOUR PERSONAL MANTRA (WALLPAPER TEXT)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Write anything here. It will display directly on your live wallpaper!", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = inputMotto,
                    onValueChange = { inputMotto = it },
                    placeholder = { Text("Enter custom slogan...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF030712),
                        unfocusedContainerColor = Color(0xFF030712),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = themeColor,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("motto_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.saveMotto(inputMotto) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor, contentColor = Color(0xFF030712)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Slogan")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Premium Themes selector chips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("STUDIO DESIGN SCHEMES", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val customThemes = listOf(
                    Triple("neon_matrix", "Neon Matrix", Color(0xFF00FFCC)),
                    Triple("cyberpunk", "Cyberpunk Pink", Color(0xFFFF007F)),
                    Triple("aurora", "Aurora Violet", Color(0xFFA855F7)),
                    Triple("oled", "OLED Stealth", Color(0xFFF59E0B)),
                    Triple("batman", "The Dark Knight (B&W)", Color(0xFFFFFFFF))
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    customThemes.forEach { (id, label, col) ->
                        val active = viewModel.selectedThemeId == id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) col.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (active) col else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateTheme(id) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.size(16.dp).background(col, CircleShape))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sliders & Toggles: Battery saver & Screentime Limit details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ECOLOGY ADJUSTMENTS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Screen minutes limit slider
                Text(
                    "TARGET SCREEN LIMIT: ${viewModel.targetScreentimeMinutes.toInt()} MINS",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Slider(
                    value = viewModel.targetScreentimeMinutes,
                    onValueChange = { viewModel.saveTargetScreentime(it) },
                    valueRange = 60f..480f,
                    colors = SliderDefaults.colors(
                        thumbColor = themeColor,
                        activeTrackColor = themeColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Switch for Low power optimizer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("POWER OPTIMIZER MODE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Lowers earth rotating resolution and FPS frame steps to increase hardware performance.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = viewModel.isBatterySaving,
                        onCheckedChange = { viewModel.saveBatteryOptimization(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeColor,
                            checkedTrackColor = themeColor.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- STANDARD BOT BAR BOTTOM NAVIGATION DUMMY HUD ---
@Composable
fun BottomNavigationHud(viewModel: MainViewModel) {
    val themeColor = getThemeColorTuple(viewModel.selectedThemeId).first
    NavigationBar(
        containerColor = Color(0xFF0F172A).copy(alpha = 0.9f),
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .height(64.dp)
    ) {
        val current = viewModel.currentScreen

        NavigationBarItem(
            selected = current == "home",
            onClick = { viewModel.currentScreen = "home" },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Display", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = themeColor,
                selectedTextColor = themeColor,
                indicatorColor = themeColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                unselectedTextColor = Color.White.copy(alpha = 0.4f)
            )
        )

        NavigationBarItem(
            selected = current == "wallpaper",
            onClick = { viewModel.currentScreen = "wallpaper" },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Wallpaper Guide") },
            label = { Text("Wallpaper", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = themeColor,
                selectedTextColor = themeColor,
                indicatorColor = themeColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                unselectedTextColor = Color.White.copy(alpha = 0.4f)
            )
        )

        NavigationBarItem(
            selected = current == "stats",
            onClick = { viewModel.currentScreen = "stats" },
            icon = { Icon(Icons.Default.Menu, contentDescription = "Screen stats log") },
            label = { Text("Stats", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = themeColor,
                selectedTextColor = themeColor,
                indicatorColor = themeColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                unselectedTextColor = Color.White.copy(alpha = 0.4f)
            )
        )

        NavigationBarItem(
            selected = current == "settings",
            onClick = { viewModel.currentScreen = "settings" },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Options") },
            label = { Text("Options", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = themeColor,
                selectedTextColor = themeColor,
                indicatorColor = themeColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                unselectedTextColor = Color.White.copy(alpha = 0.4f)
            )
        )
    }
}
