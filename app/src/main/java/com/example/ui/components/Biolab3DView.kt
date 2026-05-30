package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.*

// Continent point coordinates on a unit 3D sphere
data class GlobeVertex(val x: Float, val y: Float, val z: Float, val continent: String)

object GlobeData {
    val vertices: List<GlobeVertex> by lazy {
        val list = mutableListOf<GlobeVertex>()
        
        // Helper to add pseudorandom points strictly within a continent's geographic boundaries
        fun addContinent(name: String, minLat: Float, maxLat: Float, minLon: Float, maxLon: Float, count: Int, seed: Int) {
            val random = java.util.Random(seed.toLong())
            for (i in 0 until count) {
                val latDeg = minLat + random.nextFloat() * (maxLat - minLat)
                val lonDeg = minLon + random.nextFloat() * (maxLon - minLon)
                val lat = Math.toRadians(latDeg.toDouble()).toFloat()
                val lon = Math.toRadians(lonDeg.toDouble()).toFloat()
                
                // Convert lat/lon radians to unit sphere 3D coordinates
                val x = cos(lat) * cos(lon)
                val y = sin(lat)
                val z = cos(lat) * sin(lon)
                list.add(GlobeVertex(x, y, z, name))
            }
        }

        // Approximate boundaries for beautiful dot-matrix continent rendering
        addContinent("NorthAmerica", 20f, 65f, -125f, -70f, 90, 42)
        addContinent("SouthAmerica", -50f, 10f, -80f, -40f, 75, 43)
        addContinent("Africa", -32f, 32f, -15f, 45f, 110, 44)
        addContinent("Eurasia", 10f, 70f, 0f, 130f, 180, 45)
        addContinent("Australia", -38f, -12f, 113f, 150f, 50, 46)
        addContinent("Antarctica", -85f, -70f, -180f, 180f, 40, 47)
        
        // Add random island sprinkles
        addContinent("Islands", -10f, 40f, 140f, 180f, 15, 48)
        addContinent("Islands2", -20f, 10f, -150f, -100f, 10, 49)
        
        list
    }
}

@Composable
fun TimeLeftEarthView(
    modifier: Modifier = Modifier,
    themeColor: Color = Color(0xFF00FFCC), // active day color
    nightColor: Color = Color(0xFF1E3F66), // night shaded color
    batterySaving: Boolean = false,
    autoRotateSpeed: Float = 1.0f,
    userQuote: String = "",
    themeId: String = "neon_matrix"
) {
    // Rotation state: auto-updating + user manual scroll offsets
    var rotXOffset by remember { mutableFloatStateOf(-0.1f) } // subtle pitch
    var rotYOffset by remember { mutableFloatStateOf(0f) }

    // Animation tick for automatic spin over time
    val infiniteTransition = rememberInfiniteTransition(label = "EarthSpinTransition")
    
    // Decrease frames and precision under Battery Saving mode
    val duration = if (batterySaving) 45000 else 25000
    val spinValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinValue"
    )

    // Satellite orbital phase animator
    val satellitePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (batterySaving) 12000 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "satellite"
    )

    // Background cosmic particle twinklers
    val stars = remember {
        val random = java.util.Random(101)
        List(40) {
            Offset(random.nextFloat(), random.nextFloat()) to random.nextFloat()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag adjusts manual offset coordinates
                    rotYOffset += dragAmount.x * 0.005f
                    rotXOffset -= dragAmount.y * 0.005f
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            val radius = min(width, height) * 0.35f

            // 1. Draw Space Background Stars
            stars.forEach { (pos, alpha) ->
                val px = pos.x * width
                val py = pos.y * height
                val twinkle = alpha * (0.3f + 0.7f * abs(sin(spinValue * 1.5f + alpha * 10f)))
                drawCircle(
                    color = Color.White.copy(alpha = twinkle),
                    radius = 1.5f.dp.toPx(),
                    center = Offset(px, py)
                )
            }

            // 2. Beautiful Deep Atmosphere Outer Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        themeColor.copy(alpha = 0.25f),
                        themeColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius * 1.4f
                ),
                radius = radius * 1.4f,
                center = Offset(centerX, centerY)
            )

            // Draw Batman Spotlight searchlight and silhouette if we are on the "batman" theme
            if (themeId == "batman") {
                // Spotlight beam path
                val beamPath = Path().apply {
                    moveTo(centerX - 35.dp.toPx(), height) // narrow source at bottom
                    lineTo(centerX + 35.dp.toPx(), height)
                    lineTo(centerX + 180.dp.toPx(), centerY * 0.4f) // broad projected area
                    lineTo(centerX - 180.dp.toPx(), centerY * 0.4f)
                    close()
                }
                // Draw searchlight beam with gradient fading downwards and outwards
                drawPath(
                    path = beamPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        startY = centerY * 0.15f,
                        endY = height
                    )
                )

                // Glowing bat-signal projection oval in the sky!
                val signalRx = 100.dp.toPx()
                val signalRy = 60.dp.toPx()
                val signalCx = centerX
                val signalCy = centerY * 0.4f

                // Draw the searchlight circle projection base (glowing halo)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(signalCx, signalCy),
                        radius = signalRx * 1.6f
                    ),
                    center = Offset(signalCx, signalCy),
                    radius = signalRx * 1.6f
                )

                // Now, draw the solid Batman wing crest outline INSIDE the glowing projection!
                val batPath = Path().apply {
                    val w = signalRx * 0.9f
                    val h = signalRy * 0.9f

                    // Start at top center: dip between ears
                    moveTo(signalCx, signalCy - h * 0.25f)
                    
                    // Left ear
                    lineTo(signalCx - w * 0.08f, signalCy - h * 0.45f)
                    lineTo(signalCx - w * 0.12f, signalCy - h * 0.45f)
                    
                    // Left wing top curve
                    quadraticTo(
                        signalCx - w * 0.35f, signalCy - h * 0.55f, // control point
                        signalCx - w * 0.95f, signalCy - h * 0.15f  // left wingtip
                    )
                    
                    // Left wing bottom curves (outer scalloped edge)
                    quadraticTo(
                        signalCx - w * 0.70f, signalCy - h * 0.10f, // control
                        signalCx - w * 0.65f, signalCy + h * 0.35f  // lower rib tip
                    )
                    
                    // Inner scallop curve towards tail
                    quadraticTo(
                        signalCx - w * 0.40f, signalCy + h * 0.15f, // control
                        signalCx - w * 0.22f, signalCy + h * 0.55f  // second rib tip
                    )
                    
                    quadraticTo(
                        signalCx - w * 0.15f, signalCy + h * 0.40f, // control
                        signalCx, signalCy + h * 0.95f              // bottom tail point
                    )
                    
                    // Right side (symmetrical reverse)
                    quadraticTo(
                        signalCx + w * 0.15f, signalCy + h * 0.40f,
                        signalCx + w * 0.22f, signalCy + h * 0.55f
                    )
                    
                    quadraticTo(
                        signalCx + w * 0.40f, signalCy + h * 0.15f,
                        signalCx + w * 0.65f, signalCy + h * 0.35f
                    )
                    
                    quadraticTo(
                        signalCx + w * 0.70f, signalCy - h * 0.10f,
                        signalCx + w * 0.95f, signalCy - h * 0.15f // right wingtip
                    )
                    
                    quadraticTo(
                        signalCx + w * 0.35f, signalCy - h * 0.55f,
                        signalCx + w * 0.12f, signalCy - h * 0.45f
                    )
                    
                    lineTo(signalCx + w * 0.08f, signalCy - h * 0.45f)
                    
                    close()
                }

                // Render the Batman silhouette as a solid, powerful shadow inside the projected light
                drawPath(
                    path = batPath,
                    color = Color(0xFF030712).copy(alpha = 0.92f) // deep noir shadow contrast
                )
            }

            // Inner dark globe base shielding
            drawCircle(
                color = Color(0xFF030712),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Draw grid longitude & latitude wireframe rings beneath continents
            if (!batterySaving) {
                // Latitudes
                for (i in -2..2) {
                    val latDeg = i * 30f
                    val rLat = radius * cos(Math.toRadians(latDeg.toDouble())).toFloat()
                    val yLat = centerY + radius * sin(Math.toRadians(latDeg.toDouble())).toFloat()
                    drawOval(
                        color = themeColor.copy(alpha = 0.07f),
                        topLeft = Offset(centerX - rLat, yLat - 4.dp.toPx()),
                        size = Size(rLat * 2, 8.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // 3. Mathematical 3D Rotation Calculation
            // Combined rotation angle around the Y axis
            val thetaY = spinValue * autoRotateSpeed + rotYOffset
            val cosY = cos(thetaY)
            val sinY = sin(thetaY)

            // Fixed pitch tilt around X axis (23.5 degrees typical tilt + user vertical drag offset)
            val tiltAngle = 0.409f + rotXOffset
            val cosX = cos(tiltAngle)
            val sinX = sin(tiltAngle)

            // Virtual sun source vector positioned at top-right (for day/night shading)
            // Sun vector is fixed relative to screen space
            val sunX = 0.8f
            val sunY = -0.3f
            val sunZ = 0.5f
            val sunLength = sqrt(sunX * sunX + sunY * sunY + sunZ * sunZ)
            val sunNx = sunX / sunLength
            val sunNy = sunY / sunLength
            val sunNz = sunZ / sunLength

            // Render globe continent vertices
            GlobeData.vertices.forEach { vertex ->
                // A. Rotate around Y Axis (vertical spin)
                val x1 = vertex.x * cosY - vertex.z * sinY
                val z1 = vertex.x * sinY + vertex.z * cosY
                val y1 = vertex.y

                // B. Pitch around X Axis (tilt)
                val x2 = x1
                val y2 = y1 * cosX - z1 * sinX
                val z2 = y1 * sinX + z1 * cosX // Z-depth: Positive is towards user, Negative is hidden back

                // C. Occlusion Check: Render only if point faces the camera (z2 > -0.15f)
                if (z2 > -0.15f) {
                    // Projection to screen space
                    val screenX = centerX + radius * x2
                    val screenY = centerY + radius * y2

                    // Dynamic Day/Night Cycle lighting score: normal dot sun
                    val dotProduct = x2 * sunNx + y2 * sunNy + z2 * sunNz
                    
                    // We map the dot product (-1..1) to daylight mix value (0..1)
                    val daylightMix = ((dotProduct + 1f) / 2f).coerceIn(0f, 1f)

                    // Active luminous neon day color vs shaded night core
                    val pointColor = Color(
                        red = (themeColor.red * daylightMix + nightColor.red * (1f - daylightMix)),
                        green = (themeColor.green * daylightMix + nightColor.green * (1f - daylightMix)),
                        blue = (themeColor.blue * daylightMix + nightColor.blue * (1f - daylightMix)),
                        alpha = z2.coerceIn(0.2f, 1.0f) // fade slightly near edges
                    )

                    // Draw the physical continent dot
                    val dotRadius = if (batterySaving) 2.dp.toPx() else (2.0f + z2 * 1.5f).dp.toPx()
                    drawCircle(
                        color = pointColor,
                        radius = dotRadius,
                        center = Offset(screenX, screenY)
                    )
                }
            }

            // 4. Futuristic Satellite Orbit Ring & Shooting Beacon
            val orbitRadiusX = radius * 1.35f
            val orbitRadiusY = radius * 0.45f
            
            // Draw satellite path wireframe ring
            drawOval(
                color = themeColor.copy(alpha = 0.12f),
                topLeft = Offset(centerX - orbitRadiusX, centerY - orbitRadiusY),
                size = Size(orbitRadiusX * 2, orbitRadiusY * 2),
                style = Stroke(width = 1.dp.toPx())
            )

            // Calculate active orbiting satellite translation in 3D
            val satCos = cos(satellitePhase)
            val satSin = sin(satellitePhase)
            
            // Orbit is titled slightly to make it look dynamic in space
            val satX3d = satCos * orbitRadiusX
            val satZ3d = satSin * orbitRadiusX
            val satY3d = satCos * 10f + satSin * orbitRadiusY

            // Only draw satellite beacon if it is on the front side of orbit (depth-simulated)
            val isFront = satZ3d > 0f
            if (isFront || !batterySaving) {
                val satScreenX = centerX + satX3d
                val satScreenY = centerY + satY3d
                val satSize = if (isFront) 6.dp.toPx() else 3.dp.toPx()
                val satOpacity = if (isFront) 1.0f else 0.4f

                // Draw satellite glowing core
                drawCircle(
                    color = themeColor.copy(alpha = satOpacity),
                    radius = satSize,
                    center = Offset(satScreenX, satScreenY)
                )

                // Beacon pulsing locator ring
                if (isFront && !batterySaving) {
                    val pulse = 1f + abs(sin(spinValue * 4f)) * 1.5f
                    drawCircle(
                        color = themeColor.copy(alpha = 0.4f),
                        radius = satSize * pulse,
                        center = Offset(satScreenX, satScreenY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }
    }
}
