package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.engine.DifficultyMode

@Composable
fun DifficultyAvatar(
    mode: DifficultyMode,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 110.dp
) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w * 0.42f

        // Outer white ring (as in screenshots)
        drawCircle(
            color = Color.White,
            radius = radius + 8f,
            center = Offset(cx, cy)
        )

        when (mode) {
            DifficultyMode.EASY -> {
                // Black base stroke
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    center = Offset(cx, cy)
                )
                // Green face
                drawCircle(
                    color = Color(0xFF2ECC71),
                    radius = radius - 8f,
                    center = Offset(cx, cy)
                )
                // Left Eye
                drawCircle(
                    color = Color.Black,
                    radius = radius * 0.24f,
                    center = Offset(cx - radius * 0.38f, cy - radius * 0.15f)
                )
                // Left Eye shine
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.09f,
                    center = Offset(cx - radius * 0.32f, cy - radius * 0.22f)
                )
                // Right Eye
                drawCircle(
                    color = Color.Black,
                    radius = radius * 0.24f,
                    center = Offset(cx + radius * 0.38f, cy - radius * 0.15f)
                )
                // Right Eye shine
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.09f,
                    center = Offset(cx + radius * 0.44f, cy - radius * 0.22f)
                )
                // Happy curved smile
                val smilePath = Path().apply {
                    moveTo(cx - radius * 0.25f, cy + radius * 0.30f)
                    quadraticTo(cx, cy + radius * 0.58f, cx + radius * 0.25f, cy + radius * 0.30f)
                }
                drawPath(
                    path = smilePath,
                    color = Color.Black,
                    style = Stroke(width = 9f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }

            DifficultyMode.MEDIUM -> {
                // Black base stroke
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    center = Offset(cx, cy)
                )
                // Orange face
                drawCircle(
                    color = Color(0xFFF39C12),
                    radius = radius - 8f,
                    center = Offset(cx, cy)
                )
                // Cheeky Eyes looking sideways
                drawCircle(
                    color = Color.Black,
                    radius = radius * 0.24f,
                    center = Offset(cx - radius * 0.36f, cy - radius * 0.15f)
                )
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.08f,
                    center = Offset(cx - radius * 0.42f, cy - radius * 0.20f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = radius * 0.24f,
                    center = Offset(cx + radius * 0.36f, cy - radius * 0.15f)
                )
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.08f,
                    center = Offset(cx + radius * 0.30f, cy - radius * 0.20f)
                )
                // Smirk mouth
                val smirkPath = Path().apply {
                    moveTo(cx - radius * 0.15f, cy + radius * 0.35f)
                    quadraticTo(cx + radius * 0.15f, cy + radius * 0.48f, cx + radius * 0.32f, cy + radius * 0.28f)
                }
                drawPath(
                    path = smirkPath,
                    color = Color.Black,
                    style = Stroke(width = 9f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }

            DifficultyMode.HARD -> {
                // Royal Violet Monster Horns/Ears on top
                val leftHorn = Path().apply {
                    moveTo(cx - radius * 0.60f, cy - radius * 0.40f)
                    quadraticTo(cx - radius * 0.95f, cy - radius * 0.95f, cx - radius * 0.40f, cy - radius * 0.80f)
                    close()
                }
                drawPath(path = leftHorn, color = Color.Black)
                val rightHorn = Path().apply {
                    moveTo(cx + radius * 0.60f, cy - radius * 0.40f)
                    quadraticTo(cx + radius * 0.95f, cy - radius * 0.95f, cx + radius * 0.40f, cy - radius * 0.80f)
                    close()
                }
                drawPath(path = rightHorn, color = Color.Black)

                // Black base circle
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    center = Offset(cx, cy)
                )
                // Violet face
                drawCircle(
                    color = Color(0xFF6366F1),
                    radius = radius - 8f,
                    center = Offset(cx, cy)
                )
                // Fierce angled eyes
                val leftEyePath = Path().apply {
                    moveTo(cx - radius * 0.50f, cy - radius * 0.25f)
                    lineTo(cx - radius * 0.16f, cy - radius * 0.05f)
                    lineTo(cx - radius * 0.40f, cy + radius * 0.10f)
                    close()
                }
                drawPath(path = leftEyePath, color = Color.Black)
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.06f,
                    center = Offset(cx - radius * 0.32f, cy - radius * 0.08f)
                )

                val rightEyePath = Path().apply {
                    moveTo(cx + radius * 0.50f, cy - radius * 0.25f)
                    lineTo(cx + radius * 0.16f, cy - radius * 0.05f)
                    lineTo(cx + radius * 0.40f, cy + radius * 0.10f)
                    close()
                }
                drawPath(path = rightEyePath, color = Color.Black)
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.06f,
                    center = Offset(cx + radius * 0.32f, cy - radius * 0.08f)
                )

                // Fierce grin
                val devilGrin = Path().apply {
                    moveTo(cx - radius * 0.25f, cy + radius * 0.30f)
                    quadraticTo(cx, cy + radius * 0.50f, cx + radius * 0.25f, cy + radius * 0.30f)
                }
                drawPath(
                    path = devilGrin,
                    color = Color.Black,
                    style = Stroke(width = 9f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }

            DifficultyMode.EXTRA_HARD -> {
                // Character from image.png: Distinctive tall curved monster horns/ears on top!
                val leftEar = Path().apply {
                    moveTo(cx - radius * 0.65f, cy - radius * 0.30f)
                    cubicTo(
                        cx - radius * 1.05f, cy - radius * 0.85f,
                        cx - radius * 0.70f, cy - radius * 1.15f,
                        cx - radius * 0.35f, cy - radius * 0.75f
                    )
                    close()
                }
                drawPath(path = leftEar, color = Color.Black)
                val leftEarInner = Path().apply {
                    moveTo(cx - radius * 0.60f, cy - radius * 0.35f)
                    cubicTo(
                        cx - radius * 0.95f, cy - radius * 0.80f,
                        cx - radius * 0.68f, cy - radius * 1.05f,
                        cx - radius * 0.40f, cy - radius * 0.72f
                    )
                    close()
                }
                drawPath(path = leftEarInner, color = Color(0xFF5B67F6))

                val rightEar = Path().apply {
                    moveTo(cx + radius * 0.65f, cy - radius * 0.30f)
                    cubicTo(
                        cx + radius * 1.05f, cy - radius * 0.85f,
                        cx + radius * 0.70f, cy - radius * 1.15f,
                        cx + radius * 0.35f, cy - radius * 0.75f
                    )
                    close()
                }
                drawPath(path = rightEar, color = Color.Black)
                val rightEarInner = Path().apply {
                    moveTo(cx + radius * 0.60f, cy - radius * 0.35f)
                    cubicTo(
                        cx + radius * 0.95f, cy - radius * 0.80f,
                        cx + radius * 0.68f, cy - radius * 1.05f,
                        cx + radius * 0.40f, cy - radius * 0.72f
                    )
                    close()
                }
                drawPath(path = rightEarInner, color = Color(0xFF5B67F6))

                // Black base circle
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    center = Offset(cx, cy)
                )
                // Electric purple face (matching image.png)
                drawCircle(
                    color = Color(0xFF5B67F6),
                    radius = radius - 8f,
                    center = Offset(cx, cy)
                )

                // Sharp geometric angled monster eyes (> <)
                val leftEye = Path().apply {
                    moveTo(cx - radius * 0.52f, cy - radius * 0.22f)
                    lineTo(cx - radius * 0.15f, cy - radius * 0.04f)
                    lineTo(cx - radius * 0.42f, cy + radius * 0.12f)
                    close()
                }
                drawPath(path = leftEye, color = Color.Black)
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.06f,
                    center = Offset(cx - radius * 0.34f, cy - radius * 0.06f)
                )

                val rightEye = Path().apply {
                    moveTo(cx + radius * 0.52f, cy - radius * 0.22f)
                    lineTo(cx + radius * 0.15f, cy - radius * 0.04f)
                    lineTo(cx + radius * 0.42f, cy + radius * 0.12f)
                    close()
                }
                drawPath(path = rightEye, color = Color.Black)
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.06f,
                    center = Offset(cx + radius * 0.34f, cy - radius * 0.06f)
                )

                // Determined mouth with center notch
                val mouthPath = Path().apply {
                    moveTo(cx - radius * 0.22f, cy + radius * 0.36f)
                    lineTo(cx, cy + radius * 0.44f)
                    lineTo(cx + radius * 0.22f, cy + radius * 0.36f)
                }
                drawPath(
                    path = mouthPath,
                    color = Color.Black,
                    style = Stroke(width = 9f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
        }
    }
}
