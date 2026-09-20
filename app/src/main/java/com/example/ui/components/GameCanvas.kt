package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.engine.Axis
import com.example.engine.Block3D
import com.example.engine.ColorPaletteGenerator
import com.example.engine.ConfettiShape
import com.example.engine.FallingPiece3D
import com.example.engine.TowerEngine
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private const val COS30 = 0.8660254f
private const val SIN30 = 0.5f

@Composable
fun GameCanvas(
    engine: TowerEngine,
    frameTick: Long = 0L,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    onTap()
                }
            }
    ) {
        // Read frameTick in DrawScope so Compose executes this draw block on every frame (60fps)
        @Suppress("UNUSED_VARIABLE")
        val tick = frameTick

        val width = size.width
        val height = size.height

        // Dynamic scale factor ensuring the tower is prominently sized (approx 72% screen width)
        val viewScale = (width / 380f).coerceIn(0.85f, 3.5f)

        // 1. Draw Sky Background
        drawSkyBackground(width, height, engine.currentTheme)

        // 2. Draw Stars & Shooting Stars
        drawStarsAndShootingStars(engine, width, height)

        // 3. Compute screen origin for the active stacking zone
        // Centered around 49% down the screen matching reference screenshot
        val originX = width / 2f
        val originY = height * 0.49f
        val cameraY = engine.cameraY

        // 4. Draw Tower Stacked Blocks (bottom to top)
        val blocks = engine.stackedBlocks
        for (i in blocks.indices) {
            val block = blocks[i]
            if (i == 0) {
                drawBlock(block, originX, originY, cameraY, viewScale, isFoundation = true)
            } else {
                val approxScreenY = (block.cx + block.cy) * viewScale * SIN30 - (block.zTop - cameraY) * viewScale + originY
                if (approxScreenY < height + 450f && approxScreenY > -350f) {
                    drawBlock(block, originX, originY, cameraY, viewScale, isFoundation = false)
                }
            }
        }

        // 5. Draw Falling Overhang Pieces
        for (piece in engine.fallingPieces) {
            drawFallingPiece(piece, originX, originY, cameraY, viewScale)
        }

        // 6. Draw Active Moving Block
        if (engine.gamePhase.value is com.example.engine.GamePhase.Playing) {
            val lastBlock = engine.stackedBlocks.lastOrNull()
            if (lastBlock != null) {
                val movingBlock = Block3D(
                    cx = if (engine.movingAxis == Axis.X) lastBlock.cx + engine.movingOffset else lastBlock.cx,
                    cy = if (engine.movingAxis == Axis.Y) lastBlock.cy + engine.movingOffset else lastBlock.cy,
                    sizeX = engine.movingSizeX,
                    sizeY = engine.movingSizeY,
                    zTop = engine.movingZTop,
                    height = engine.blockHeight,
                    baseColor = engine.movingColor
                )
                drawBlock(movingBlock, originX, originY, cameraY, viewScale, isMoving = true)
            }
        }

        // 7. Draw Shockwave Rings on perfect stacks
        drawShockwaveRings(engine, originX, originY, cameraY, viewScale)

        // 8. Draw Combo Text Particles ("PERFECT!", "+1", etc.)
        drawComboParticles(engine, textMeasurer, originX, originY, cameraY, viewScale)

        // 9. Draw High Score Celebratory Confetti Shower
        drawConfettiParticles(engine)
    }
}

private fun DrawScope.drawSkyBackground(width: Float, height: Float, theme: com.example.engine.GameTheme) {
    // Atmospheric gradient dynamically driven by selected theme:
    val skyBrush = Brush.verticalGradient(
        colors = theme.skyColors,
        startY = 0f,
        endY = height
    )
    drawRect(brush = skyBrush)

    // Sun / Atmosphere halo glow in upper-mid screen
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.accentColor.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = Offset(width * 0.55f, height * 0.26f),
            radius = width * 0.75f
        ),
        radius = width * 0.75f,
        center = Offset(width * 0.55f, height * 0.26f)
    )

    // Distant faint clouds
    drawCircle(
        color = Color.White.copy(alpha = 0.08f),
        radius = 120f,
        center = Offset(width * 0.15f, height * 0.48f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.08f),
        radius = 150f,
        center = Offset(width * 0.88f, height * 0.55f)
    )
}

private fun DrawScope.drawStarsAndShootingStars(engine: TowerEngine, width: Float, height: Float) {
    // Fixed background twinkling star positions
    val starCoords = listOf(
        Offset(width * 0.08f, height * 0.06f),
        Offset(width * 0.22f, height * 0.03f),
        Offset(width * 0.39f, height * 0.07f),
        Offset(width * 0.61f, height * 0.04f),
        Offset(width * 0.82f, height * 0.08f),
        Offset(width * 0.94f, height * 0.03f),
        Offset(width * 0.14f, height * 0.12f),
        Offset(width * 0.31f, height * 0.15f),
        Offset(width * 0.75f, height * 0.13f),
        Offset(width * 0.89f, height * 0.16f),
        Offset(width * 0.05f, height * 0.19f),
        Offset(width * 0.52f, height * 0.18f)
    )
    for (star in starCoords) {
        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = 1.8f,
            center = star
        )
    }

    // Shooting stars
    for (shootingStar in engine.shootingStars) {
        val startX = shootingStar.x
        val startY = shootingStar.y
        val endX = startX - shootingStar.vx * 6f
        val endY = startY - shootingStar.vy * 6f
        val lifeAlpha = (1f - (shootingStar.progress / shootingStar.maxLifetime)).coerceIn(0f, 1f)

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = lifeAlpha * 0.9f),
                    Color(0xFFBCE7F0).copy(alpha = lifeAlpha * 0.4f),
                    Color.Transparent
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2.2f
        )
    }
}

private fun DrawScope.drawBlock(
    block: Block3D,
    originX: Float,
    originY: Float,
    cameraY: Float,
    scale: Float,
    isMoving: Boolean = false,
    isFoundation: Boolean = false
) {
    val hx = block.sizeX / 2f
    val hy = block.sizeY / 2f
    val zTop = block.zTop
    val height = block.height

    // Corner coordinates in world space at zTop
    val northTop = projectPoint(block.cx - hx, block.cy - hy, zTop, originX, originY, cameraY, scale)
    val eastTop  = projectPoint(block.cx + hx, block.cy - hy, zTop, originX, originY, cameraY, scale)
    val southTop = projectPoint(block.cx + hx, block.cy + hy, zTop, originX, originY, cameraY, scale)
    val westTop  = projectPoint(block.cx - hx, block.cy + hy, zTop, originX, originY, cameraY, scale)

    // Corner coordinates at zBottom = zTop - height
    val eastBottom  = projectPoint(block.cx + hx, block.cy - hy, zTop - height, originX, originY, cameraY, scale)
    val southBottom = projectPoint(block.cx + hx, block.cy + hy, zTop - height, originX, originY, cameraY, scale)
    val westBottom  = projectPoint(block.cx - hx, block.cy + hy, zTop - height, originX, originY, cameraY, scale)

    val leftColor = ColorPaletteGenerator.getLeftFaceColor(block.baseColor)
    val rightColor = ColorPaletteGenerator.getRightFaceColor(block.baseColor)

    // 1. Left Face (West to South)
    val leftPath = Path().apply {
        moveTo(westTop.x, westTop.y)
        lineTo(southTop.x, southTop.y)
        lineTo(southBottom.x, southBottom.y)
        lineTo(westBottom.x, westBottom.y)
        close()
    }
    if (isFoundation) {
        // Smooth foundation gradient from lime top through teal and deep ocean down into dark midnight navy
        val leftBrush = Brush.verticalGradient(
            colors = listOf(
                leftColor,
                leftColor.copy(alpha = 0.90f),
                Color(0xFF1E5C4E),
                Color(0xFF183B5E),
                Color(0xFF121B38)
            ),
            startY = westTop.y,
            endY = (southBottom.y).coerceAtLeast(size.height)
        )
        drawPath(path = leftPath, brush = leftBrush)
    } else {
        drawPath(path = leftPath, color = leftColor)
    }

    // 2. Right Face (South to East)
    val rightPath = Path().apply {
        moveTo(southTop.x, southTop.y)
        lineTo(eastTop.x, eastTop.y)
        lineTo(eastBottom.x, eastBottom.y)
        lineTo(southBottom.x, southBottom.y)
        close()
    }
    if (isFoundation) {
        val rightBrush = Brush.verticalGradient(
            colors = listOf(
                rightColor,
                rightColor.copy(alpha = 0.90f),
                Color(0xFF16473D),
                Color(0xFF122C47),
                Color(0xFF0E152D)
            ),
            startY = southTop.y,
            endY = (southBottom.y).coerceAtLeast(size.height)
        )
        drawPath(path = rightPath, brush = rightBrush)
    } else {
        drawPath(path = rightPath, color = rightColor)
    }

    // 3. Top Face (North -> East -> South -> West)
    val topPath = Path().apply {
        moveTo(northTop.x, northTop.y)
        lineTo(eastTop.x, eastTop.y)
        lineTo(southTop.x, southTop.y)
        lineTo(westTop.x, westTop.y)
        close()
    }
    val topColor = ColorPaletteGenerator.getTopFaceColor(block.baseColor)
    drawPath(path = topPath, color = topColor)

    // Subtle crisp edge separation for clean tactile look
    val edgeColor = Color.White.copy(alpha = if (isMoving) 0.35f else 0.18f)
    drawPath(path = topPath, color = edgeColor, style = Stroke(width = 1.2f))
    if (!isFoundation) {
        drawLine(color = edgeColor, start = southTop, end = southBottom, strokeWidth = 1.2f)
    }
}

private fun DrawScope.drawFallingPiece(
    piece: FallingPiece3D,
    originX: Float,
    originY: Float,
    cameraY: Float,
    scale: Float
) {
    val hx = piece.sizeX / 2f
    val hy = piece.sizeY / 2f
    val zTop = piece.zTop
    val height = piece.height
    val rot = piece.rotation

    // Rotated offsets
    fun rotateOffset(dx: Float, dy: Float): Pair<Float, Float> {
        val rx = cos(rot) * dx - sin(rot) * dy
        val ry = sin(rot) * dx + cos(rot) * dy
        return Pair(rx, ry)
    }

    val (ndx, ndy) = rotateOffset(-hx, -hy)
    val (edx, edy) = rotateOffset(hx, -hy)
    val (sdx, sdy) = rotateOffset(hx, hy)
    val (wdx, wdy) = rotateOffset(-hx, hy)

    val northTop = projectPoint(piece.cx + ndx, piece.cy + ndy, zTop, originX, originY, cameraY, scale)
    val eastTop  = projectPoint(piece.cx + edx, piece.cy + edy, zTop, originX, originY, cameraY, scale)
    val southTop = projectPoint(piece.cx + sdx, piece.cy + sdy, zTop, originX, originY, cameraY, scale)
    val westTop  = projectPoint(piece.cx + wdx, piece.cy + wdy, zTop, originX, originY, cameraY, scale)

    val eastBottom  = projectPoint(piece.cx + edx, piece.cy + edy, zTop - height, originX, originY, cameraY, scale)
    val southBottom = projectPoint(piece.cx + sdx, piece.cy + sdy, zTop - height, originX, originY, cameraY, scale)
    val westBottom  = projectPoint(piece.cx + wdx, piece.cy + wdy, zTop - height, originX, originY, cameraY, scale)

    val alpha = piece.alpha.coerceIn(0f, 1f)

    // Left face
    val leftPath = Path().apply {
        moveTo(westTop.x, westTop.y)
        lineTo(southTop.x, southTop.y)
        lineTo(southBottom.x, southBottom.y)
        lineTo(westBottom.x, westBottom.y)
        close()
    }
    drawPath(path = leftPath, color = ColorPaletteGenerator.getLeftFaceColor(piece.baseColor).copy(alpha = alpha))

    // Right face
    val rightPath = Path().apply {
        moveTo(southTop.x, southTop.y)
        lineTo(eastTop.x, eastTop.y)
        lineTo(eastBottom.x, eastBottom.y)
        lineTo(southBottom.x, southBottom.y)
        close()
    }
    drawPath(path = rightPath, color = ColorPaletteGenerator.getRightFaceColor(piece.baseColor).copy(alpha = alpha))

    // Top face
    val topPath = Path().apply {
        moveTo(northTop.x, northTop.y)
        lineTo(eastTop.x, eastTop.y)
        lineTo(southTop.x, southTop.y)
        lineTo(westTop.x, westTop.y)
        close()
    }
    drawPath(path = topPath, color = ColorPaletteGenerator.getTopFaceColor(piece.baseColor).copy(alpha = alpha))
}

private fun DrawScope.drawComboParticles(
    engine: TowerEngine,
    textMeasurer: TextMeasurer,
    originX: Float,
    originY: Float,
    cameraY: Float,
    scale: Float
) {
    for (particle in engine.comboParticles) {
        val proj = projectPoint(particle.cx, particle.cy, particle.zTop, originX, originY, cameraY, scale)
        val alpha = particle.alpha.coerceIn(0f, 1f)

        val textLayout = textMeasurer.measure(
            text = particle.text,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = alpha)
            )
        )

        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                x = proj.x - textLayout.size.width / 2f,
                y = proj.y + particle.yOffset - 40f
            )
        )
    }
}

private fun DrawScope.drawShockwaveRings(
    engine: TowerEngine,
    originX: Float,
    originY: Float,
    cameraY: Float,
    scale: Float
) {
    for (ring in engine.shockwaveRings) {
        val alpha = ring.alpha.coerceIn(0f, 1f)
        if (alpha <= 0f) continue
        val exp = ring.expansion
        val hx = (ring.sizeX / 2f) + exp
        val hy = (ring.sizeY / 2f) + exp
        val z = ring.zTop + 1f

        val north = projectPoint(ring.cx - hx, ring.cy - hy, z, originX, originY, cameraY, scale)
        val east  = projectPoint(ring.cx + hx, ring.cy - hy, z, originX, originY, cameraY, scale)
        val south = projectPoint(ring.cx + hx, ring.cy + hy, z, originX, originY, cameraY, scale)
        val west  = projectPoint(ring.cx - hx, ring.cy + hy, z, originX, originY, cameraY, scale)

        val path = Path().apply {
            moveTo(north.x, north.y)
            lineTo(east.x, east.y)
            lineTo(south.x, south.y)
            lineTo(west.x, west.y)
            close()
        }

        drawPath(
            path = path,
            color = ring.color.copy(alpha = alpha * 0.9f),
            style = Stroke(width = (2.6f * (1f - (ring.expansion / ring.maxExpansion))).coerceAtLeast(1.2f))
        )
    }
}

private fun DrawScope.drawConfettiParticles(engine: TowerEngine) {
    for (p in engine.confettiParticles) {
        val alpha = p.alpha.coerceIn(0f, 1f)
        if (alpha <= 0f) continue

        val particleColor = p.color.copy(alpha = alpha)
        val rotRad = Math.toRadians(p.rotation.toDouble()).toFloat()
        val flutterCos = cos(p.flutterPhase)

        when (p.shapeType) {
            ConfettiShape.RECTANGLE -> {
                val halfW = (p.size * flutterCos) / 2f
                val halfH = (p.size * 1.65f) / 2f

                val cosR = cos(rotRad)
                val sinR = sin(rotRad)

                fun rotPt(dx: Float, dy: Float): Offset {
                    val rx = cosR * dx - sinR * dy
                    val ry = sinR * dx + cosR * dy
                    return Offset(p.x + rx, p.y + ry)
                }

                val p1 = rotPt(-halfW, -halfH)
                val p2 = rotPt(halfW, -halfH)
                val p3 = rotPt(halfW, halfH)
                val p4 = rotPt(-halfW, halfH)

                val ribbonPath = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    close()
                }
                drawPath(path = ribbonPath, color = particleColor)
            }
            ConfettiShape.CIRCLE -> {
                drawCircle(
                    color = particleColor,
                    radius = (p.size / 2f) * abs(flutterCos).coerceAtLeast(0.25f),
                    center = Offset(p.x, p.y)
                )
            }
            ConfettiShape.DIAMOND, ConfettiShape.STAR -> {
                val d = p.size
                val path = Path().apply {
                    moveTo(p.x, p.y - d)
                    lineTo(p.x + d * 0.7f * flutterCos, p.y)
                    lineTo(p.x, p.y + d)
                    lineTo(p.x - d * 0.7f * flutterCos, p.y)
                    close()
                }
                drawPath(path = path, color = particleColor)
            }
        }
    }
}

private fun projectPoint(
    wx: Float,
    wy: Float,
    wz: Float,
    originX: Float,
    originY: Float,
    cameraY: Float,
    scale: Float = 1.0f
): Offset {
    val sx = (wx - wy) * scale * COS30 + originX
    val sy = (wx + wy) * scale * SIN30 - (wz - cameraY) * scale + originY
    return Offset(sx, sy)
}
