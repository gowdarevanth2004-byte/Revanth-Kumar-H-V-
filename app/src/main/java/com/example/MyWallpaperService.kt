package com.example

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.data.AppDatabase
import com.example.ui.components.GlobeData
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.*

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return TimeLeftEngine(applicationContext)
    }

    private inner class TimeLeftEngine(private val context: Context) : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        // Scope to access database settings reactively on a background thread
        private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        private var preferenceJob: Job? = null

        // Persistent configurations fetched from database
        private var themeId = "neon_matrix"
        private var customMotto = ""
        private var batterySaving = false

        // Animating parameters
        private var spinAngle = 0f
        private var satellitePhase = 0f
        private val drawRunnable = Runnable { draw() }

        // Standard Paints
        private val backgroundPaint = Paint().apply { isAntiAlias = true }
        private val starsPaint = Paint().apply { isAntiAlias = true; color = Color.WHITE }
        private val globeBasePaint = Paint().apply { isAntiAlias = true; color = Color.parseColor("#030712") }
        private val gridPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }
        private val dotPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        private val textTimerPaint = Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
        private val textLabelPaint = Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
        private val textMottoPaint = Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }

        // Background Star coordinates
        private val stars = List(35) {
            val random = Random()
            PointF(random.nextFloat(), random.nextFloat()) to random.nextFloat()
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadPreferencesFromDatabase()
        }

        override fun onDestroy() {
            super.onDestroy()
            scope.cancel()
            handler.removeCallbacks(drawRunnable)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                draw()
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            this.visible = false
            handler.removeCallbacks(drawRunnable)
        }

        private fun loadPreferencesFromDatabase() {
            preferenceJob?.cancel()
            preferenceJob = scope.launch {
                val db = AppDatabase.getDatabase(context)
                val dao = db.timeLeftDao()
                
                // Keep polling or observing database configuration values
                while (isActive) {
                    try {
                        val allPrefs = dao.getDailyStatsForDate("MIGRATION_STATUS_DO_NOT_DELETE") // dummy check or preferences
                        val storedTheme = dao.getPreferenceValueDirect("theme_id")?.value ?: "neon_matrix"
                        val storedMotto = dao.getPreferenceValueDirect("user_motto")?.value ?: "Make Every Second Count."
                        val storedBattery = dao.getPreferenceValueDirect("battery_saving")?.value == "true"

                        themeId = storedTheme
                        customMotto = storedMotto
                        batterySaving = storedBattery
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    delay(3000) // update wallpaper specs every 3 seconds
                }
            }
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderWallpaperFrame(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            // Schedule next frame refresh rate mapping saving optimization constraints
            if (visible) {
                val nextDelay = if (batterySaving) 66L else 30L // 15 FPS under power-save mode vs ~33 FPS standard
                handler.removeCallbacks(drawRunnable)
                handler.postDelayed(drawRunnable, nextDelay)
            }
        }

        private fun renderWallpaperFrame(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val cx = w / 2f
            val cy = h / 2f
            val r = min(w, h) * 0.35f
            val density = context.resources.displayMetrics.density

            // Frame Step updates
            val angleStep = if (batterySaving) 0.001f else 0.0025f
            spinAngle = (spinAngle + angleStep) % (2f * Math.PI.toFloat())
            val satStep = if (batterySaving) 0.005f else 0.015f
            satellitePhase = (satellitePhase + satStep) % (2f * Math.PI.toFloat())

            // Define themed palettes
            val themeColor: Int
            val shadingColor: Int
            when (themeId) {
                "cyberpunk" -> {
                    themeColor = Color.parseColor("#FF007F") // Hyper Pink
                    shadingColor = Color.parseColor("#FD7E14") // Sunset Orange
                }
                "aurora" -> {
                    themeColor = Color.parseColor("#A855F7") // Aurora Violet
                    shadingColor = Color.parseColor("#3B82F6") // Arctic Blue
                }
                "oled" -> {
                    themeColor = Color.parseColor("#F59E0B") // Stealth Amber
                    shadingColor = Color.parseColor("#1F1F1F") // Low-glow slate
                }
                "batman" -> {
                    themeColor = Color.parseColor("#FFFFFF") // The Dark Knight White (B&W)
                    shadingColor = Color.parseColor("#16171B") // Deep Dark Knight shadow
                }
                "neon_matrix" -> {
                    themeColor = Color.parseColor("#00FFCC") // Futuristic Teal
                    shadingColor = Color.parseColor("#0A2540") // Dark Space
                }
                else -> {
                    themeColor = Color.parseColor("#00FFCC")
                    shadingColor = Color.parseColor("#0A2540")
                }
            }

            // 1. Solid Pure OLED Pitch Black underneath for battery optimization
            canvas.drawColor(Color.parseColor("#030712"))

            // 2. Cosmic Space Starfield Background
            stars.forEach { (pos, alpha) ->
                val px = pos.x * w
                val py = pos.y * h
                // Subtle organic star flicker
                val twinkle = alpha * (0.3f + 0.7f * abs(sin(spinAngle * 3f + alpha * 15f)))
                starsPaint.alpha = (twinkle * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(px, py, 2f, starsPaint)
            }

            // Draw Batman Spotlight searchlight and silhouette if we are on the "batman" theme
            if (themeId == "batman") {
                val beamPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                val beamPath = android.graphics.Path().apply {
                    moveTo(cx - 35f * density, h) // bottom source
                    lineTo(cx + 35f * density, h)
                    lineTo(cx + 180f * density, cy * 0.4f) // broad target projection
                    lineTo(cx - 180f * density, cy * 0.4f)
                    close()
                }
                
                // Shader gradient fading up
                beamPaint.shader = LinearGradient(
                    cx, cy * 0.15f, cx, h,
                    intArrayOf(Color.argb(56, 255, 255, 255), Color.argb(10, 255, 255, 255), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawPath(beamPath, beamPaint)

                // Glowing halo base for searchlight projected circle
                val haloPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    shader = RadialGradient(
                        cx, cy * 0.4f, 160f * density,
                        intArrayOf(Color.argb(89, 255, 255, 255), Color.argb(20, 255, 255, 255), Color.TRANSPARENT),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(cx, cy * 0.4f, 160f * density, haloPaint)

                // Draw the Bat silhouetted crest outline inside the searchlight beam!
                val batPath = android.graphics.Path()
                val sw = 90f * density
                val sh = 54f * density
                val scx = cx
                val scy = cy * 0.4f

                batPath.moveTo(scx, scy - sh * 0.25f)
                
                // Left side
                batPath.lineTo(scx - sw * 0.08f, scy - sh * 0.45f)
                batPath.lineTo(scx - sw * 0.12f, scy - sh * 0.45f)
                batPath.quadTo(
                    scx - sw * 0.35f, scy - sh * 0.55f,
                    scx - sw * 0.95f, scy - sh * 0.15f
                )
                batPath.quadTo(
                    scx - sw * 0.70f, scy - sh * 0.10f,
                    scx - sw * 0.65f, scy + sh * 0.35f
                )
                batPath.quadTo(
                    scx - sw * 0.40f, scy + sh * 0.15f,
                    scx - sw * 0.22f, scy + sh * 0.55f
                )
                batPath.quadTo(
                    scx - sw * 0.15f, scy + sh * 0.40f,
                    scx, scy + sh * 0.95f
                )
                // Right side
                batPath.quadTo(
                    scx + sw * 0.15f, scy + sh * 0.40f,
                    scx + sw * 0.22f, scy + sh * 0.55f
                )
                batPath.quadTo(
                    scx + sw * 0.40f, scy + sh * 0.15f,
                    scx + sw * 0.65f, scy + sh * 0.35f
                )
                batPath.quadTo(
                    scx + sw * 0.70f, scy - sh * 0.10f,
                    scx + sw * 0.95f, scy - sh * 0.15f
                )
                batPath.quadTo(
                    scx + sw * 0.35f, scy - sh * 0.55f,
                    scx + sw * 0.12f, scy - sh * 0.45f
                )
                batPath.lineTo(scx + sw * 0.08f, scy - sh * 0.45f)
                batPath.close()

                val batShadowPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = Color.parseColor("#030712")
                }
                canvas.drawPath(batPath, batShadowPaint)
            }

            // 3. Ambient Atmosphere Planetary Radial Glow
            val glowGrad = RadialGradient(
                cx, cy, r * 1.4f,
                intArrayOf(adjustAlpha(themeColor, 0.2f), adjustAlpha(themeColor, 0.05f), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
            backgroundPaint.shader = glowGrad
            canvas.drawCircle(cx, cy, r * 1.4f, backgroundPaint)
            backgroundPaint.shader = null // reset

            // 4. Solid Globe Underlay
            canvas.drawCircle(cx, cy, r, globeBasePaint)

            // 5. Draw Longitudes Grid Rings
            if (!batterySaving) {
                gridPaint.color = adjustAlpha(themeColor, 0.05f)
                gridPaint.strokeWidth = 1.5f
                for (i in -2..2) {
                    val latDeg = i * 30f
                    val latRad = Math.toRadians(latDeg.toDouble()).toFloat()
                    val rLat = r * cos(latRad)
                    val yLat = cy + r * sin(latRad)
                    val rect = RectF(cx - rLat, yLat - 10f, cx + rLat, yLat + 10f)
                    canvas.drawOval(rect, gridPaint)
                }
            }

            // 6. Draw 3D Dot Matrix Continent Projector
            // Math constants
            val thetaY = spinAngle
            val cosY = cos(thetaY)
            val sinY = sin(thetaY)

            // Fixed pitch tilt (~23 degrees)
            val tiltAngle = 0.409f
            val cosX = cos(tiltAngle)
            val sinX = sin(tiltAngle)

            // Sun vector (shading point light direction)
            val sunNx = 0.8f
            val sunNy = -0.3f
            val sunNz = 0.5f

            GlobeData.vertices.forEach { vertex ->
                // Orbit-rotation around Y
                val x1 = vertex.x * cosY - vertex.z * sinY
                val z1 = vertex.x * sinY + vertex.z * cosY
                val y1 = vertex.y

                // Fixed Tilt pitch around X
                val x2 = x1
                val y2 = y1 * cosX - z1 * sinX
                val z2 = y1 * sinX + z1 * cosX // positive Z-depth points face the viewer

                if (z2 > -0.15f) {
                    val screenX = cx + r * x2
                    val screenY = cy + r * y2

                    // Calculate illumination factor (Sun dot surface-normal)
                    val dot = x2 * sunNx + y2 * sunNy + z2 * sunNz
                    val daylight = ((dot + 1f) / 2f).coerceIn(0f, 1f)

                    // Blend day color vs night color
                    val finalColor = blendColors(themeColor, shadingColor, daylight)
                    dotPaint.color = finalColor
                    dotPaint.alpha = ((z2.coerceIn(0.2f, 1.0f)) * 255).toInt().coerceIn(0, 255)

                    val dotRadius = if (batterySaving) 3f else (3.5f + z2 * 2f)
                    canvas.drawCircle(screenX, screenY, dotRadius, dotPaint)
                }
            }

            // 7. Render Satellite orbit trail & Beacon
            val orbitRx = r * 1.35f
            val orbitRy = r * 0.45f

            gridPaint.color = adjustAlpha(themeColor, 0.08f)
            gridPaint.strokeWidth = 2f
            canvas.drawOval(RectF(cx - orbitRx, cy - orbitRy, cx + orbitRx, cy + orbitRy), gridPaint)

            // Satellite location
            val satCos = cos(satellitePhase)
            val satSin = sin(satellitePhase)
            val satX = satCos * orbitRx
            val satY = satCos * 10f + satSin * orbitRy
            val isFront = satSin > 0f // approximate orbit-axis relative depth of outer beacon

            if (isFront || !batterySaving) {
                dotPaint.color = themeColor
                dotPaint.alpha = if (isFront) 255 else 100
                val orbitRadiusSize = if (isFront) 10f else 6f
                canvas.drawCircle(cx + satX, cy + satY, orbitRadiusSize, dotPaint)
            }

            // 8. OSD System Countdown (Futuristic motivational time hud)
            // Time Left Calculations
            val calendar = Calendar.getInstance()
            val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
            val nowMins = calendar.get(Calendar.MINUTE)
            val nowSecs = calendar.get(Calendar.SECOND)

            val currentDayProgressSeconds = nowHour * 3600 + nowMins * 60 + nowSecs
            val totalDaySeconds = 24 * 3600
            val secondsLeftToday = max(0, totalDaySeconds - currentDayProgressSeconds)

            val hh = secondsLeftToday / 3600
            val mm = (secondsLeftToday % 3600) / 60
            val ss = secondsLeftToday % 60

            val daysLeftYear = 365 - calendar.get(Calendar.DAY_OF_YEAR)

            // Dynamic String formatting
            val secondsPadded = String.format("%02d", ss)
            val minutesPadded = String.format("%02d", mm)
            val hoursPadded = String.format("%02d", hh)
            val timeString = "$hoursPadded : $minutesPadded : $secondsPadded"

            val daysString = "$daysLeftYear DAYS REMAINING IN ${calendar.get(Calendar.YEAR)}"

            // Render HUD overlay
            textTimerPaint.color = themeColor
            textTimerPaint.textSize = 90f
            textTimerPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

            textLabelPaint.color = Color.WHITE
            textLabelPaint.textSize = 34f
            textLabelPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textLabelPaint.alpha = 180

            textMottoPaint.color = themeColor
            textMottoPaint.textSize = 40f
            textMottoPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)

            // Absolute dynamic layouts responsive to space (fits lock screen vs home screen)
            val upperY = cy - r * 1.55f
            val lowerY = cy + r * 1.55f

            // Top Countdown
            canvas.drawText("TIME LEFT TODAY", cx, upperY - 45f, textLabelPaint)
            canvas.drawText(timeString, cx, upperY + 50f, textTimerPaint)

            // Bottom Counts & Wallpaper writable motto
            canvas.drawText(daysString, cx, lowerY - 60f, textLabelPaint)
            
            // Custom writable text entered on app configurations!
            val mottoPhrase = if (customMotto.trim().isEmpty()) "MAKE EVERY SECOND COUNT." else customMotto.uppercase()
            canvas.drawText(mottoPhrase, cx, lowerY, textMottoPaint)
        }

        private fun adjustAlpha(color: Int, factor: Float): Int {
            val alpha = (Color.alpha(color) * factor).roundToInt()
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return Color.argb(alpha, red, green, blue)
        }

        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio = 1f - ratio
            val r = (Color.red(color1) * ratio + Color.red(color2) * inverseRatio).toInt()
            val g = (Color.green(color1) * ratio + Color.green(color2) * inverseRatio).toInt()
            val b = (Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio).toInt()
            return Color.rgb(r, g, b)
        }
    }
}
