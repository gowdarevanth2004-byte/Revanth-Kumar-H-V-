package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.util.Random
import kotlin.math.*

@Composable
fun Biolab3DView(
    modelId: Int,
    modifier: Modifier = Modifier,
    zoomScale: Float = 1.0f,
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    onRotationChanged: (Float, Float) -> Unit = { _, _ -> },
    actionTriggered: Boolean = false,
    speedFactor: Float = 1.0f,
    selectedFeatureIndex: Int = -1,
    onFeatureLabelTapped: (String) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val dx = dragAmount.x * 0.5f
                    val dy = dragAmount.y * 0.5f
                    onRotationChanged(rotationX - dy, rotationY + dx)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "BiolabRenderer")
        val animValue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin"
        )

        val beatAnimation by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween((600 / speedFactor).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "heartBeat"
        )

        when (modelId) {
            1 -> Cell3D(animValue, zoomScale, rotationY, selectedFeatureIndex, onFeatureLabelTapped)
            2 -> Dna3D(animValue, zoomScale, rotationY, actionTriggered, selectedFeatureIndex, onFeatureLabelTapped)
            3 -> Heart3D(beatAnimation, zoomScale, speedFactor, selectedFeatureIndex, onFeatureLabelTapped)
            4 -> Brain3D(animValue, zoomScale, selectedFeatureIndex, onFeatureLabelTapped)
            5 -> Neuron3D(animValue, zoomScale, actionTriggered, selectedFeatureIndex, onFeatureLabelTapped)
            6 -> Skeleton3D(rotationY, zoomScale, selectedFeatureIndex, onFeatureLabelTapped)
            7 -> Digestive3D(animValue, zoomScale, actionTriggered, selectedFeatureIndex, onFeatureLabelTapped)
            8 -> Respiratory3D(beatAnimation, zoomScale, selectedFeatureIndex, onFeatureLabelTapped)
            9 -> PlantCell3D(animValue, zoomScale, actionTriggered, selectedFeatureIndex, onFeatureLabelTapped)
            10 -> Immune3D(animValue, zoomScale, actionTriggered, selectedFeatureIndex, onFeatureLabelTapped)
            else -> Text("Generating standard model...")
        }
    }
}

// 1. HUMAN CELL MODEL
@Composable
fun Cell3D(
    anim: Float,
    scale: Float,
    rotY: Float,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val baseRadius = min(cX, cY) * 0.65f * scale

        // Outer Membrane Gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF233B2B), Color(0xFF1B221E), Color(0xFF0F1512)),
                center = Offset(cX, cY),
                radius = baseRadius
            )
        )

        // Membrane Borders (Cytoplasm boundary)
        drawCircle(
            color = Color(0xFF4CAF50),
            radius = baseRadius,
            center = Offset(cX, cY),
            style = Stroke(width = 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
        )

        // Cytoplasm dynamic mesh particle draw helper
        val random = Random(42)
        for (i in 0..12) {
            val rVal = random.nextFloat() * baseRadius * 0.8f
            val theta = random.nextFloat() * 2 * Math.PI + anim * 0.15f
            val px = cX + rVal * cos(theta).toFloat()
            val py = cY + rVal * sin(theta).toFloat()
            drawCircle(
                color = Color(0x6681C784),
                radius = 3.dp.toPx(),
                center = Offset(px, py)
            )
        }

        // Draw Nucleus (Center)
        val nRadius = baseRadius * 0.35f
        val nucleColor = if (selectedFeature == 3) Color(0xFFEEFF41) else Color(0xFF9575CD)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFCE93D8), nucleColor, Color(0xFF4A148C)),
                center = Offset(cX, cY),
                radius = nRadius
            ),
            radius = nRadius,
            center = Offset(cX, cY)
        )

        // Nucleolus inside nucleus
        drawCircle(
            color = Color(0xFFE040FB),
            radius = nRadius * 0.3f,
            center = Offset(cX - 10f, cY - 10f)
        )

        // Endoplasmic Reticulum (Curved bands around Nucleus)
        val erColor = if (selectedFeature == 5) Color(0xFF00E676) else Color(0xFFF06292)
        for (i in 1..3) {
            val erRad = nRadius + i * 20.dp.toPx()
            drawArc(
                color = erColor,
                startAngle = (0f + i * 40f + rotY) % 360f,
                sweepAngle = 110f,
                useCenter = false,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                size = Size(erRad * 2, erRad * 1.5f),
                topLeft = Offset(cX - erRad, cY - erRad * 0.75f)
            )
        }

        // Mitochondria (Rotates in 3D-like orbit)
        val mitoColor = if (selectedFeature == 1) Color(0xFFFFEB3B) else Color(0xFFFF7043)
        val mAngle = anim + (rotY * 0.017f)
        val mX = cX + (baseRadius * 0.6f) * cos(mAngle)
        val mY = cY + (baseRadius * 0.45f) * sin(mAngle)
        
        rotate(degrees = Math.toDegrees(mAngle.toDouble()).toFloat() + 45f, pivot = Offset(mX, mY)) {
            // Capsule
            drawRoundRect(
                color = mitoColor,
                topLeft = Offset(mX - 35f, mY - 15f),
                size = Size(70f, 30f),
                cornerRadius = CornerRadius(15f, 15f),
                style = Stroke(width = 3.dp.toPx())
            )
            // Crystals (Inner lines)
            for (offset in -20..20 step 10) {
                drawLine(
                    color = mitoColor,
                    start = Offset(mX + offset, mY - 10f),
                    end = Offset(mX + offset + 3f, mY + 10f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Golgi Apparatus (Opposite side)
        val golgiColor = if (selectedFeature == 6) Color(0xFF00E5FF) else Color(0xFFFF9800)
        val gX = cX + (baseRadius * 0.55f) * cos(mAngle + Math.PI.toFloat())
        val gY = cY + (baseRadius * 0.55f) * sin(mAngle + Math.PI.toFloat())
        for (i in 0..3) {
            drawArc(
                color = golgiColor,
                startAngle = 180f,
                sweepAngle = 150f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(gX - 30f, gY - 20f + i * 10f),
                size = Size(60f + i * 8f, 25f + i * 3f)
            )
        }

        // Ribosome clusters (Tiny white dots)
        for (i in 0..20) {
            val rRadius = 2.dp.toPx()
            val rx = cX + (erColor.hashCode().absoluteValue % 100 - i) * cos(i.toFloat()) * 1.5f
            val ry = cY + (erColor.hashCode().absoluteValue % 80 + i) * sin(i.toFloat()) * 1.2f
            drawCircle(color = Color.White, radius = rRadius, center = Offset(rx, ry))
        }

        // Tap-to-learn text anchors (Illustrative indicators)
        drawPointCircle(cX + baseRadius * 0.6f * cos(mAngle), cY + baseRadius * 0.45f * sin(mAngle), "Mito", mitoColor)
        drawPointCircle(cX, cY, "Nuc", Color(0xFFAB47BC))
    }
}

// Helper to draw a small descriptive text flag on Canvas
private fun DrawScope.drawPointCircle(x: Float, y: Float, label: String, color: Color) {
    drawCircle(color = color, radius = 5.dp.toPx(), center = Offset(x, y))
    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
}

// 2. DNA DOUBLE HELIX MODEL
@Composable
fun Dna3D(
    anim: Float,
    scale: Float,
    rotY: Float,
    isUnzipping: Boolean,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val hHeight = size.height * 0.8f
        val baseWidth = size.width * 0.25f * scale

        val steps = 24
        val spreadOffset = if (isUnzipping) 90f else 0f

        for (i in 0 until steps) {
            val progress = i / steps.toFloat()
            val yOffset = cY - hHeight / 2 + progress * hHeight

            // Double Helix math
            val theta = progress * 4 * Math.PI.toFloat() + anim + (rotY * 0.02f)

            // Strand 1 projection
            val baseRadius1 = baseWidth * cos(theta)
            val dZ1 = sin(theta) // Used for depth scaling and shading

            // Strand 2 projection (Offset by Pi)
            val baseRadius2 = baseWidth * cos(theta + Math.PI.toFloat())
            val dZ2 = sin(theta + Math.PI.toFloat())

            // Unzipping pushes the helix sides apart laterally
            val x1 = cX + baseRadius1 - (spreadOffset * (1.0f - progress))
            val x2 = cX + baseRadius2 + (spreadOffset * (1.0f - progress))

            // Determine if base pairing gets highlighted
            val isHighlighted = selectedFeature == 2 && i % 3 == 0

            // Base pairs (Horizontal rungs) color code
            val basePairColor = when {
                isHighlighted -> Color(0xFFE040FB)
                i % 4 == 0 -> Color(0xFFEF5350) // Red (A)
                i % 4 == 1 -> Color(0xFF26A69A) // Green (T)
                i % 4 == 2 -> Color(0xFF29B6F6) // Blue (G)
                else -> Color(0xFFFFCA28)       // Yellow (C)
            }

            // Draw line connecting the base pair rungs (if not completely unzipped)
            if (!isUnzipping || progress > 0.4f) {
                drawLine(
                    color = basePairColor.copy(alpha = if (dZ1 > 0) 1.0f else 0.5f),
                    start = Offset(x1, yOffset),
                    end = Offset(x2, yOffset),
                    strokeWidth = (4f + (if (isHighlighted) 4 else 0)).dp.toPx()
                )
            }

            // Draw backbone node 1
            val n1Size = (7f + dZ1 * 3f).dp.toPx()
            drawCircle(
                color = Color(0xFFEC407A).copy(alpha = if (dZ1 > 0) 1f else 0.5f),
                radius = n1Size,
                center = Offset(x1, yOffset)
            )

            // Draw backbone node 2
            val n2Size = (7f + dZ2 * 3f).dp.toPx()
            drawCircle(
                color = Color(0xFF26C6DA).copy(alpha = if (dZ2 > 0) 1f else 0.5f),
                radius = n2Size,
                center = Offset(x2, yOffset)
            )
        }
    }
}

// 3. HUMAN HEART
@Composable
fun Heart3D(
    beat: Float,
    scale: Float,
    bpm: Float,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "bloodFlow")
    val flowOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blood"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val hScale = 1.3f * scale * beat

        // Outer Muscle structure (Stylized volumetric heart)
        val heartPath = Path().apply {
            moveTo(cX, cY - 100f * hScale)
            // Left atria/ventricle lobe
            cubicTo(
                cX - 180f * hScale, cY - 220f * hScale,
                cX - 250f * hScale, cY - 20f * hScale,
                cX, cY + 180f * hScale
            )
            // Right atria/ventricle lobe
            cubicTo(
                cX + 250f * hScale, cY - 20f * hScale,
                cX + 180f * hScale, cY - 220f * hScale,
                cX, cY - 100f * hScale
            )
            close()
        }

        val muscleColor = if (selectedFeature == 0) Color(0xFFFF1744) else Color(0xFFC62828)
        drawPath(
            path = heartPath,
            brush = Brush.verticalGradient(
                colors = listOf(muscleColor, Color(0xFF3E2723)),
                startY = cY - 100f,
                endY = cY + 180f
            )
        )

        // Draw major blood pipes on top
        // Aorta (Oxygenated - Red ribbon arching out of the top left)
        val aortaPath = Path().apply {
            moveTo(cX - 30f * hScale, cY - 80f * hScale)
            cubicTo(
                cX - 30f * hScale, cY - 220f * hScale,
                cX + 40f * hScale, cY - 240f * hScale,
                cX + 50f * hScale, cY - 160f * hScale
            )
        }
        drawPath(
            path = aortaPath,
            color = Color(0xFFEF5350),
            style = Stroke(width = 25.dp.toPx(), cap = StrokeCap.Round)
        )

        // Pulmonary Artery (Deoxygenated - Blue pipe crossing behind/front)
        val pulmonaryPath = Path().apply {
            moveTo(cX - 70f * hScale, cY - 140f * hScale)
            lineTo(cX + 60f * hScale, cY - 80f * hScale)
        }
        drawPath(
            path = pulmonaryPath,
            color = Color(0xFF29B6F6),
            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
        )

        // Left/Right internal septal separator line
        drawLine(
            color = Color(0x33FFFFFF),
            start = Offset(cX, cY - 100f * hScale),
            end = Offset(cX - 20f * hScale, cY + 170f * hScale),
            strokeWidth = 4.dp.toPx()
        )

        // Arrows / Blood flow dots
        // Oxygenated blood flow (Left side, traveling down and out)
        val oxDotOffset = flowOffset % 100f
        val oxY = cY - 50f * hScale + (oxDotOffset / 100f) * 150f * hScale
        val oxX = cX + 40f * hScale - (oxDotOffset / 100f) * 10f * hScale
        drawCircle(
            color = Color(0xFFFFEB3B),
            radius = 6.dp.toPx() + (sin(flowOffset) * 2f).dp.toPx(),
            center = Offset(oxX, oxY)
        )

        // Deoxygenated blood flow (Right side, blue flow traveling up)
        val deoxDotOffset = (flowOffset + 50f) % 100f
        val deoxY = cY + 100f * hScale - (deoxDotOffset / 100f) * 180f * hScale
        val deoxX = cX - 60f * hScale + (deoxDotOffset / 100f) * 20f * hScale
        drawCircle(
            color = Color(0xFFE0F7FA),
            radius = 5.dp.toPx(),
            center = Offset(deoxX, deoxY)
        )

        // Interactive Valves representation
        val valveColor = if (selectedFeature == 4) Color(0xFF76FF03) else Color.White
        drawRect(
            color = valveColor,
            topLeft = Offset(cX - 15f * hScale, cY - 30f * hScale),
            size = Size(30f, 6f)
        )
    }
}

// 4. HUMAN BRAIN
@Composable
fun Brain3D(
    anim: Float,
    scale: Float,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val s = 1.2f * scale

        // Predefine the regional bounding paths for anatomical lobes
        // 1. Frontal Lobe (Decision center - orange/pink)
        val fLobeColor = if (selectedFeature == 7) Color(0xFFFF4081) else Color(0xFFFF8A80)
        val fPath = Path().apply {
            moveTo(cX - 40f * s, cY - 140f * s)
            cubicTo(
                cX - 180f * s, cY - 130f * s,
                cX - 190f * s, cY + 10f * s,
                cX - 70f * s, cY + 30f * s
            )
            lineTo(cX - 30f * s, cY - 10f * s)
            close()
        }
        drawPath(path = fPath, color = fLobeColor)

        // 2. Parietal Lobe (Top back - yellow)
        val pLobeColor = if (selectedFeature == 7) Color(0xFFEEFF41) else Color(0xFFFFD54F)
        val pPath = Path().apply {
            moveTo(cX - 30f * s, cY - 140f * s)
            cubicTo(
                cX + 60f * s, cY - 160f * s,
                cX + 120f * s, cY - 90f * s,
                cX + 70f * s, cY - 10f * s
            )
            lineTo(cX - 30f * s, cY - 10f * s)
            close()
        }
        drawPath(path = pPath, color = pLobeColor)

        // 3. Occipital Lobe (Back skull - purple)
        val oLobeColor = if (selectedFeature == 7) Color(0xFFE040FB) else Color(0xFFB39DDB)
        val oPath = Path().apply {
            moveTo(cX + 70f * s, cY - 10f * s)
            lineTo(cX + 120f * s, cY - 90f * s)
            cubicTo(
                cX + 170f * s, cY - 30f * s,
                cX + 140f * s, cY + 40f * s,
                cX + 80f * s, cY + 30f * s
            )
            close()
        }
        drawPath(path = oPath, color = oLobeColor)

        // 4. Temporal Lobe (Underneath front - teal/green)
        val tLobeColor = if (selectedFeature == 7) Color(0xFF00E5FF) else Color(0xFF4DB6AC)
        val tPath = Path().apply {
            moveTo(cX - 70f * s, cY + 20f * s)
            cubicTo(
                cX - 20f * s, cY + 60f * s,
                cX + 60f * s, cY + 40f * s,
                cX + 75f * s, cY + 10f * s
            )
            lineTo(cX - 30f * s, cY - 10f * s)
            close()
        }
        drawPath(path = tPath, color = tLobeColor)

        // 5. Cerebellum (Balance center - salmon pink bottom back)
        val cLobeColor = if (selectedFeature == 1) Color(0xFFFF5252) else Color(0xFFFFAB91)
        val cPath = Path().apply {
            moveTo(cX + 80f * s, cY + 35f * s)
            cubicTo(
                cX + 130f * s, cY + 45f * s,
                cX + 100f * s, cY + 110f * s,
                cX + 40f * s, cY + 95f * s
            )
            close()
        }
        drawPath(path = cPath, color = cLobeColor)

        // 6. Brain Stem (Automatic core - grey/blue extending block)
        val bColor = if (selectedFeature == 2) Color(0xFF00E676) else Color(0xFF90A4AE)
        val bPath = Path().apply {
            moveTo(cX - 10f * s, cY + 40f * s)
            lineTo(cX + 35f * s, cY + 40f * s)
            lineTo(cX + 20f * s, cY + 160f * s)
            lineTo(cX - 5f * s, cY + 160f * s)
            close()
        }
        drawPath(path = bPath, color = bColor)

        // Draw Brain folds (Cortical sulci lines over brain regions)
        val random = Random(12)
        for (i in 0..15) {
            val rx = cX + (random.nextFloat() - 0.5f) * 180f * s
            val ry = cY + (random.nextFloat() - 0.5f) * 180f * s
            drawArc(
                color = Color(0x33000000),
                startAngle = random.nextFloat() * 360f,
                sweepAngle = 90f + random.nextFloat() * 90f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                size = Size(35f, 20f),
                topLeft = Offset(rx, ry)
            )
        }

        // Action potentials/neural fire loops (Glowing spark particles)
        for (i in 0..3) {
            val tOffset = (anim + i * 1.5f) % (2 * Math.PI.toFloat())
            val pulseX = cX + 80f * s * cos(tOffset)
            val pulseY = cY + 60f * s * sin(tOffset) * 0.5f
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 4.dp.toPx() + (sin(anim * 4 + i) * 2f).dp.toPx(),
                center = Offset(pulseX, pulseY)
            )
        }
    }
}

// 5. NEURON & SYNAPSE
@Composable
fun Neuron3D(
    anim: Float,
    scale: Float,
    triggerFire: Boolean,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    val signalProgress = remember { Animatable(0f) }
    LaunchedEffect(triggerFire) {
        if (triggerFire) {
            signalProgress.snapTo(0f)
            signalProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(1500, easing = LinearEasing)
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val s = scale

        // Draw Dendrites & Soma (Cell body center left)
        val somaX = cX - 120f * s
        val somaY = cY - 80f * s
        val somaColor = if (selectedFeature == 1) Color(0xFFFFEB3B) else Color(0xFFAB47BC)

        // Draw five major dendrite branch arms
        val dendriteAngles = listOf(45, 120, 180, 240, 315)
        dendriteAngles.forEach { angle ->
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            drawLine(
                color = somaColor,
                start = Offset(somaX, somaY),
                end = Offset(somaX + 80f * s * cos(rad), somaY + 80f * s * sin(rad)),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Tiny branch buds
            drawLine(
                color = somaColor.copy(alpha = 0.8f),
                start = Offset(somaX + 50f * s * cos(rad), somaY + 50f * s * sin(rad)),
                end = Offset(somaX + 75f * s * cos(rad) + 15f, somaY + 75f * s * sin(rad) - 15f),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Central Soma ball
        drawCircle(
            color = somaColor,
            radius = 35.dp.toPx(),
            center = Offset(somaX, somaY)
        )
        // Nucleus
        drawCircle(color = Color(0xFFE91E63), radius = 12.dp.toPx(), center = Offset(somaX, somaY))

        // Axon fiber extending right down
        val axonPath = Path().apply {
            moveTo(somaX, somaY)
            cubicTo(
                somaX + 120f * s, somaY + 100f * s,
                cX + 20f * s, cY + 40f * s,
                cX + 120f * s, cY + 80f * s
            )
        }
        drawPath(
            path = axonPath,
            color = Color(0xFF26A69A),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Action potential electrical wave animation sweeping along axon
        if (signalProgress.value > 0f && signalProgress.value < 0.9f) {
            val t = signalProgress.value
            // Calculate progress coordinates based on cubic segments
            val p1 = Offset(somaX, somaY)
            val cp1 = Offset(somaX + 120f * s, somaY + 100f * s)
            val cp2 = Offset(cX + 20f * s, cY + 40f * s)
            val p2 = Offset(cX + 120f * s, cY + 80f * s)

            // Bezier cubic formula
            val u = 1 - t
            val tt = t * t
            val uu = u * u
            val uuu = uu * u
            val ttt = tt * t

            val pX = uuu * p1.x + 3 * uu * t * cp1.x + 3 * u * tt * cp2.x + ttt * p2.x
            val pY = uuu * p1.y + 3 * uu * t * cp1.y + 3 * u * tt * cp2.y + ttt * p2.y

            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(pX, pY)
            )
            drawCircle(
                color = Color(0xFF00E676),
                radius = 6.dp.toPx(),
                center = Offset(pX, pY)
            )
        }

        // Synaptic terminal window on bottom-right (Zoomed in synapsis display block)
        val synX = cX + 120f * s
        val synY = cY + 80f * s

        // Draw terminal button bulb circle
        drawCircle(
            color = Color(0x22FFFFFF),
            style = Stroke(width = 2.dp.toPx()),
            radius = 60.dp.toPx(),
            center = Offset(synX, synY)
        )

        // Inside synapsis bulb display vesicle circles
        val isSynSelected = selectedFeature == 3
        val vesColor = if (isSynSelected) Color(0xFFEEFF41) else Color(0xFFFF9800)
        
        drawCircle(color = vesColor.copy(alpha = 0.5f), radius = 10.dp.toPx(), center = Offset(synX - 25f, synY - 15f))
        drawCircle(color = vesColor.copy(alpha = 0.5f), radius = 12.dp.toPx(), center = Offset(synX + 15f, synY + 25f))

        // Under firing impulse, draw released chemical transmitters traveling outward
        if (signalProgress.value > 0.6f) {
            val scaleBurst = (signalProgress.value - 0.6f) * 2.5f
            for (i in 0..6) {
                val scatterAngle = i * 60f
                val scRad = Math.toRadians(scatterAngle.toDouble()).toFloat()
                val scatterX = synX + (45f + scaleBurst * 40f) * cos(scRad)
                val scatterY = synY + (45f + scaleBurst * 40f) * sin(scRad)
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 4.dp.toPx() * (1f - scaleBurst * 0.5f),
                    center = Offset(scatterX, scatterY)
                )
            }
        }
    }
}

// 6. HUMAN SKELETON MODEL
@Composable
fun Skeleton3D(
    rotY: Float,
    scale: Float,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val s = scale * 1.5f

        // Theme-responsive styling: check if "X-Ray mode" is toggled on
        val isXray = selectedFeature == 2
        val boneColor = if (isXray) Color(0xFF00E676) else Color(0xFFECEFF1)
        val bgLineColor = if (isXray) Color(0x3300C853) else Color(0x22FFFFFF)

        // Rotate lines around vertical Y Axis using 3D perspective rotation
        val rotRad = Math.toRadians(rotY.toDouble()).toFloat()

        // Local coord drawing utility that projects standard 3D points [x, y, z] to 2D
        fun getProjected(x: Float, y: Float, z: Float): Offset {
            val rotX = x * cos(rotRad) - z * sin(rotRad)
            return Offset(cX + rotX * s, cY + y * s)
        }

        // Draw Spine background grid
        for (i in -4..4) {
            val pY = cY + i * 40f
            drawLine(color = bgLineColor, start = Offset(0f, pY), end = Offset(size.width, pY))
        }

        // 1. Skull (Projected sphere)
        val skullCenter = getProjected(0f, -120f, 0f)
        drawCircle(color = boneColor, radius = 22.dp.toPx() * s, center = skullCenter)
        // Draw eyes silhouettes
        drawCircle(color = Color(0xFF121212), radius = 5.dp.toPx() * s, center = Offset(skullCenter.x - 7.dp.toPx() * s, skullCenter.y + 3.dp.toPx() * s))
        drawCircle(color = Color(0xFF121212), radius = 5.dp.toPx() * s, center = Offset(skullCenter.x + 7.dp.toPx() * s, skullCenter.y + 3.dp.toPx() * s))

        // 2. Spine column (Rib links)
        var lastPr = skullCenter
        for (spineY in -80..30 step 15) {
            val currPr = getProjected(0f, spineY.toFloat(), 0f)
            drawLine(color = boneColor, start = lastPr, end = currPr, strokeWidth = 8.dp.toPx() * s, cap = StrokeCap.Round)
            // Draw rib curves side
            val leftRib = getProjected(-35f, spineY.toFloat(), 5f)
            val rightRib = getProjected(35f, spineY.toFloat(), -5f)
            if (spineY < 0f) {
                drawLine(color = boneColor, start = currPr, end = leftRib, strokeWidth = 3.dp.toPx() * s, cap = StrokeCap.Round)
                drawLine(color = boneColor, start = currPr, end = rightRib, strokeWidth = 3.dp.toPx() * s, cap = StrokeCap.Round)
            }
            lastPr = currPr
        }

        // 3. Pelvis structure bowl
        val pelLeft = getProjected(-25f, 40f, 0f)
        val pelRight = getProjected(25f, 40f, 0f)
        val pelBottom = getProjected(0f, 60f, 0f)
        drawLine(color = boneColor, start = pelLeft, end = pelRight, strokeWidth = 10.dp.toPx() * s, cap = StrokeCap.Round)
        drawLine(color = boneColor, start = pelLeft, end = pelBottom, strokeWidth = 8.dp.toPx() * s, cap = StrokeCap.Round)
        drawLine(color = boneColor, start = pelRight, end = pelBottom, strokeWidth = 8.dp.toPx() * s, cap = StrokeCap.Round)

        // 4. Limbs (Femur/Arms)
        // Left arm joints
        val shoulderL = getProjected(-40f, -70f, 0f)
        val elbowL = getProjected(-60f, -20f, 0f)
        val handL = getProjected(-70f, 20f, 0f)
        drawLine(color = boneColor, start = lastPr, end = shoulderL, strokeWidth = 6.dp.toPx() * s)
        drawLine(color = boneColor, start = shoulderL, end = elbowL, strokeWidth = 5.dp.toPx() * s)
        drawLine(color = boneColor, start = elbowL, end = handL, strokeWidth = 4.dp.toPx() * s)

        // Right arm joints
        val shoulderR = getProjected(40f, -70f, 0f)
        val elbowR = getProjected(60f, -20f, 0f)
        val handR = getProjected(70f, 20f, 0f)
        drawLine(color = boneColor, start = lastPr, end = shoulderR, strokeWidth = 6.dp.toPx() * s)
        drawLine(color = boneColor, start = shoulderR, end = elbowR, strokeWidth = 5.dp.toPx() * s)
        drawLine(color = boneColor, start = elbowR, end = handR, strokeWidth = 4.dp.toPx() * s)

        // Left Femur (Thigh bone)
        val thighL = getProjected(-22f, 50f, 0f)
        val kneeL = getProjected(-20f, 110f, 0f)
        val footL = getProjected(-20f, 170f, 0f)
        drawLine(color = boneColor, start = thighL, end = kneeL, strokeWidth = (if (selectedFeature == 0) 9 else 6).dp.toPx() * s)
        drawLine(color = boneColor, start = kneeL, end = footL, strokeWidth = 5.dp.toPx() * s)

        // Right Femur joint
        val thighR = getProjected(22f, 50f, 0f)
        val kneeR = getProjected(20f, 110f, 0f)
        val footR = getProjected(20f, 170f, 0f)
        drawLine(color = boneColor, start = thighR, end = kneeR, strokeWidth = (if (selectedFeature == 0) 9 else 6).dp.toPx() * s)
        drawLine(color = boneColor, start = kneeR, end = footR, strokeWidth = 5.dp.toPx() * s)
    }
}

// 7. DIGESTIVE SYSTEM
@Composable
fun Digestive3D(
    anim: Float,
    scale: Float,
    feedTrigger: Boolean,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    val digestProgress = remember { Animatable(0f) }
    LaunchedEffect(feedTrigger) {
        if (feedTrigger) {
            digestProgress.snapTo(0f)
            digestProgress.animateTo(1.0f, animationSpec = tween(4000, easing = LinearEasing))
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val s = scale

        // Draw stylized body background overlay
        drawCircle(color = Color(0x11FFFFFF), radius = min(cX, cY) * 0.8f)

        // 1. Mouth / Oral cavity (Top)
        val pathMouth = Path().apply {
            moveTo(cX, cY - 140f * s)
            lineTo(cX - 25f * s, cY - 140f * s)
            lineTo(cX, cY - 100f * s)
        }
        drawPath(path = pathMouth, color = Color(0xFFFFCC80))

        // 2. Esophagus muscular pipeline
        val esoColor = if (selectedFeature == 1) Color(0xFFE040FB) else Color(0xFFFFAB91)
        drawLine(
            color = esoColor,
            start = Offset(cX, cY - 100f * s),
            end = Offset(cX - 15f * s, cY - 20f * s),
            strokeWidth = 14.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 3. J-Shaped Stomach (Acid bath)
        val stomachColor = if (selectedFeature == 2) Color(0xFF00E676) else Color(0xFFE57373)
        val pathStomach = Path().apply {
            moveTo(cX - 15f * s, cY - 20f * s)
            cubicTo(
                cX - 80f * s, cY - 30f * s,
                cX - 80f * s, cY + 40f * s,
                cX + 5f * s, cY + 30f * s
            )
            cubicTo(
                cX + 20f * s, cY + 25f * s,
                cX - 5f * s, cY + 5f * s,
                cX - 15f * s, cY - 20f * s
            )
            close()
        }
        drawPath(path = pathStomach, color = stomachColor)

        // Stomach acids inside (Wavy glowing liquid)
        val acidLevel = cY + (15f + sin(anim * 5) * 4f) * s
        drawRect(
            color = Color(0x6676FF03),
            topLeft = Offset(cX - 55f * s, acidLevel),
            size = Size(45f * s, 15f * s)
        )

        // 4. Liver projection block
        drawArc(
            color = Color(0xFF8D6E63).copy(alpha = 0.9f),
            startAngle = 180f,
            sweepAngle = 140f,
            useCenter = true,
            topLeft = Offset(cX, cY - 35f * s),
            size = Size(75f * s, 45f * s)
        )

        // 5. Small Intestines (Dense folded tubing loops)
        val sIntColor = if (selectedFeature == 5) Color(0xFF00E5FF) else Color(0xFFFFB74D)
        for (i in 0..4) {
            drawArc(
                color = sIntColor,
                startAngle = i * 45f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx() * s, cap = StrokeCap.Round),
                topLeft = Offset(cX - 40f * s, cY + 30f * s + i * 8f),
                size = Size(50f * s, 35f * s)
            )
        }

        // 6. Large Intestinal frame loops
        drawArc(
            color = Color(0xFFCE93D8),
            startAngle = 150f,
            sweepAngle = 240f,
            useCenter = false,
            style = Stroke(width = 24.dp.toPx() * s, cap = StrokeCap.Round),
            topLeft = Offset(cX - 60f * s, cY + 15f * s),
            size = Size(100f * s, 85f * s)
        )

        // Food Bolus Particle sliding down target routes
        if (digestProgress.value > 0f) {
            val progress = digestProgress.value
            val foodCol = if (progress > 0.4f) Color(0xFFFDD835) else Color(0xFF8D6E63)

            // Step coordinate sequence maps paths: Mouth -> Esophagus -> Stomach -> Small Intestine -> Exit
            val fx: Float
            val fy: Float

            if (progress <= 0.25f) { // Esophagus run
                val localP = progress / 0.25f
                fx = cX - (15f * s * localP)
                fy = cY - 100f * s + (80f * s * localP)
            } else if (progress <= 0.5f) { // Stomach Acid melt
                val localP = (progress - 0.25f) / 0.25f
                fx = cX - 15f * s - (30f * s * localP)
                fy = cY - 20f * s + (40f * s * localP)
            } else if (progress <= 0.8f) { // Intestine loop transit
                val localP = (progress - 0.5f) / 0.3f
                fx = cX - 40f * s + (50f * s * localP)
                fy = cY + 45f * s + (15f * s * sin(localP * 12))
            } else { // Large intestinal exit framing
                val localP = (progress - 0.8f) / 0.2f
                fx = cX + 40f * s - (80f * s * localP)
                fy = cY + 80f * s + (15f * s * localP)
            }

            drawCircle(color = foodCol, radius = 8.dp.toPx() * s, center = Offset(fx, fy))
        }
    }
}

// 8. RESPIRATORY SYSTEM
@Composable
fun Respiratory3D(
    breath: Float,
    scale: Float,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "gasExchange")
    val exchangeVal by transition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "exchange"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val s = scale * breath

        // Trachea Tube windpipe
        drawLine(
            color = Color(0xFFECEFF1),
            start = Offset(cX, cY - 140f * s),
            end = Offset(cX, cY - 40f * s),
            strokeWidth = 16.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Trachea cartilage rings
        for (i in -120..-50 step 20) {
            drawLine(
                color = Color(0xFF90A4AE),
                start = Offset(cX - 12f, cY + i * s),
                end = Offset(cX + 12f, cY + i * s),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Left and Right Lungs (Expandable contours)
        val lungColor = if (selectedFeature == 2) Color(0xFFE91E63) else Color(0xFFEF9A9A)
        
        // Left Lung
        val leftLungPath = Path().apply {
            moveTo(cX, cY - 40f * s)
            cubicTo(
                cX - 40f * s, cY - 60f * s,
                cX - 150f * s, cY - 30f * s,
                cX - 140f * s, cY + 80f * s
            )
            cubicTo(
                cX - 120f * s, cY + 130f * s,
                cX - 20f * s, cY + 110f * s,
                cX, cY + 60f * s
            )
            close()
        }
        drawPath(path = leftLungPath, color = lungColor)

        // Right Lung
        val rightLungPath = Path().apply {
            moveTo(cX, cY - 40f * s)
            cubicTo(
                cX + 40f * s, cY - 60f * s,
                cX + 150f * s, cY - 30f * s,
                cX + 140f * s, cY + 80f * s
            )
            cubicTo(
                cX + 120f * s, cY + 130f * s,
                cX + 20f * s, cY + 110f * s,
                cX, cY + 60f * s
            )
            close()
        }
        drawPath(path = rightLungPath, color = lungColor)

        // Diaphragm Muscle sheet (Lies flat under lungs, flexing in sync with breath size)
        val dMuscleColor = if (selectedFeature == 7) Color(0xFFEEFF41) else Color(0xFF90A4AE)
        val diaphragmPath = Path().apply {
            moveTo(cX - 170f * scale, cY + 140f * scale + (breath * 20f))
            quadraticTo(
                cX, cY + 100f * scale + (breath * 12f),
                cX + 170f * scale, cY + 140f * scale + (breath * 20f)
            )
        }
        drawPath(
            path = diaphragmPath,
            color = dMuscleColor,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Oxygen (Red ovals entering windpipe) vs Carbon Dioxide (Blue dots exiting)
        val pProgress = exchangeVal / 100f
        
        // Inhaled Oxygen dots going downward
        drawCircle(
            color = Color(0xFFFF5252), // Oxygen Red
            radius = 6.dp.toPx(),
            center = Offset(cX, cY - 140f * s + pProgress * 150f * s)
        )

        // Exhaled Carbon Dioxide dots drifting outward
        drawCircle(
            color = Color(0xFF40C4FF), // CO2 Blue
            radius = 5.dp.toPx(),
            center = Offset(cX - (50f * s * (1f - pProgress)), cY - 10f * s - pProgress * 130f * s)
        )
    }
}

// 9. PLANT CELL MODEL
@Composable
fun PlantCell3D(
    anim: Float,
    scale: Float,
    actionPhotosyn: Boolean,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    val lightRayProgress = remember { Animatable(0f) }
    LaunchedEffect(actionPhotosyn) {
        if (actionPhotosyn) {
            lightRayProgress.snapTo(0f)
            lightRayProgress.animateTo(1.0f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val s = scale

        // 1. Rigid Hexagonal Outer Cellulose Cell Wall (Green geometric outline)
        val hexagonPath = Path().apply {
            val r = min(cX, cY) * 0.75f * s
            for (i in 0..5) {
                val angle = i * Math.PI / 3
                val x = cX + r * cos(angle).toFloat()
                val y = cY + r * sin(angle).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        
        val wallColor = if (selectedFeature == 0) Color(0xFF00E676) else Color(0xFF2E7D32)
        drawPath(
            path = hexagonPath,
            color = wallColor,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Cytoplasm cell interior
        drawPath(
            path = hexagonPath,
            color = Color(0xFF1B3020)
        )

        // 2. Giant Central Vacuole (Huge blue turgor sphere balloon)
        val vacuoleColor = if (selectedFeature == 3) Color(0xFF00E5FF) else Color(0x770288D1)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE0F7FA), vacuoleColor, Color(0x33000000)),
                center = Offset(cX - 20f * s, cY + 10f * s),
                radius = 75.dp.toPx() * s
            ),
            radius = 70.dp.toPx() * s,
            center = Offset(cX - 20f * s, cY + 10f * s)
        )

        // 3. Chloroplast solar collectors (Green discs with layered stacks)
        val chloColor = if (selectedFeature == 1) Color(0xFFEEFF41) else Color(0xFF76FF03)
        val chloroplasts = listOf(
            Offset(cX + 70f * s, cY - 60f * s),
            Offset(cX - 70f * s, cY - 70f * s),
            Offset(cX + 80f * s, cY + 50f * s)
        )

        chloroplasts.forEach { offset ->
            drawCircle(
                color = chloColor,
                radius = 18.dp.toPx() * s,
                center = offset
            )
            // Thylakoid stacks lines inside disc
            drawLine(color = Color(0xFF1B5E20), start = Offset(offset.x - 8f, offset.y - 4f), end = Offset(offset.x + 8f, offset.y - 4f), strokeWidth = 3f)
            drawLine(color = Color(0xFF1B5E20), start = Offset(offset.x - 10f, offset.y + 2f), end = Offset(offset.x + 10f, offset.y + 2f), strokeWidth = 3f)
        }

        // 4. Plant Cellular Nucleus
        drawCircle(
            color = Color(0xFFAB47BC),
            radius = 20.dp.toPx() * s,
            center = Offset(cX + 20f * s, cY - 80f * s)
        )

        // Falling Sunlight photon beams (Yellow light stripes striking chloroplast)
        if (lightRayProgress.value > 0f) {
            val t = lightRayProgress.value
            val lightY = -80f + t * (cY + 20f)
            val lightX = cX + 150f - t * 80f
            
            // Draw glowing beam line to chloroplast
            drawLine(
                color = Color(0xFFFFEB3B).copy(alpha = 1f - t),
                start = Offset(lightX - 45f, lightY - 45f),
                end = Offset(lightX, lightY),
                strokeWidth = 6.dp.toPx()
            )

            // Spark green glucose particles outward
            if (t > 0.6f) {
                drawCircle(
                    color = Color(0xFF00FFCC).copy(alpha = 1.0f - t),
                    radius = 15.dp.toPx() * t,
                    center = chloroplasts[0]
                )
            }
        }
    }
}

// 10. IMMUNE SYSTEM / VIRUS ATTACK WAVES
@Composable
fun Immune3D(
    anim: Float,
    scale: Float,
    triggerAttack: Boolean,
    selectedFeature: Int,
    onTap: (String) -> Unit
) {
    val battleState = remember { Animatable(0f) }
    LaunchedEffect(triggerAttack) {
        if (triggerAttack) {
            battleState.snapTo(0f)
            battleState.animateTo(1.0f, animationSpec = tween(2000, easing = FastOutSlowInEasing))
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cX = size.width / 2
        val cY = size.height / 2
        val s = scale

        // 1. Large Central Macrophage Cell (WBC - purple body extending defensive feelers)
        val wbcColor = if (selectedFeature == 0) Color(0xFF00E676) else Color(0xFF3F51B5)
        val wbcRadius = 60.dp.toPx() * s

        // Cytoplasm blobs extending out (Pseudopods)
        val pseudopodAngle = anim + (if (selectedFeature == 2) 45f else 0f)
        val pPath = Path().apply {
            moveTo(cX, cY)
            for (i in 0..7) {
                val angle = i * Math.PI / 4 + pseudopodAngle * 0.5f
                val ext = wbcRadius + (12f + sin(anim * 6 + i) * 8f).dp.toPx()
                val px = cX + ext * cos(angle).toFloat()
                val py = cY + ext * sin(angle).toFloat()
                lineTo(px, py)
            }
            close()
        }
        drawPath(path = pPath, color = wbcColor)
        drawCircle(color = wbcColor, radius = wbcRadius, center = Offset(cX, cY))
        
        // Nucleus
        drawCircle(color = Color(0xFFE91E63), radius = 18.dp.toPx(), center = Offset(cX - 10f, cY))

        // 2. Enemy Spiky Viruses approaching inward from borders
        val vColor = if (selectedFeature == 2) Color(0xFF26C6DA) else Color(0xFFEF5350)
        val virusPositions = listOf(
            Offset(cX - 140f * s, cY - 120f * s),
            Offset(cX + 150f * s, cY - 90f * s),
            Offset(cX + 120f * s, cY + 110f * s)
        )

        virusPositions.forEachIndexed { index, staticPos ->
            // Pathogens drift inward based on battle state progress
            val approachX: Float
            val approachY: Float

            if (battleState.value > 0f) {
                val d = battleState.value
                approachX = staticPos.x - (staticPos.x - cX) * d * 0.6f
                approachY = staticPos.y - (staticPos.y - cY) * d * 0.6f
            } else {
                approachX = staticPos.x
                approachY = staticPos.y
            }

            // Draw Virus envelope circle with spikes
            drawCircle(color = vColor, radius = 10.dp.toPx() * s, center = Offset(approachX, approachY))
            for (spike in 0..5) {
                val sa = spike * Math.PI / 3
                val spikeLength = 16.dp.toPx() * s
                drawLine(
                    color = vColor,
                    start = Offset(approachX, approachY),
                    end = Offset(approachX + spikeLength * cos(sa).toFloat(), approachY + spikeLength * sin(sa).toFloat()),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // 3. Firing Antibodies (Y-shaped defensive rockets deployed on attack)
        if (battleState.value > 0f) {
            val t = battleState.value
            val antiColor = if (selectedFeature == 1) Color(0xFFEEFF41) else Color(0xFFFFEB3B)

            for (i in 0..2) {
                val targetPos = virusPositions[i]
                // Intercept trajectory coordinates from center toward virus
                val aX = cX + (targetPos.x - cX) * t
                val aY = cY + (targetPos.y - cY) * t

                // Draw miniature Y-shaped antibody
                drawLine(color = antiColor, start = Offset(aX, aY), end = Offset(aX + 8f, aY + 12f), strokeWidth = 3.dp.toPx())
                drawLine(color = antiColor, start = Offset(aX + 8f, aY + 12f), end = Offset(aX + 16f, aY + 12f), strokeWidth = 3.dp.toPx())
                drawLine(color = antiColor, start = Offset(aX + 8f, aY + 12f), end = Offset(aX + 8f, aY + 24f), strokeWidth = 3.dp.toPx())
            }
        }
    }
}
