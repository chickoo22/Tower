package com.example.engine

import androidx.compose.ui.graphics.Color
import kotlin.math.sin

data class Block3D(
    val cx: Float,
    val cy: Float,
    val sizeX: Float,
    val sizeY: Float,
    val zTop: Float,
    val height: Float = 22f,
    val baseColor: Color
)

data class FallingPiece3D(
    var cx: Float,
    var cy: Float,
    val sizeX: Float,
    val sizeY: Float,
    var zTop: Float,
    val height: Float = 22f,
    val baseColor: Color,
    var vz: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    var alpha: Float = 1.0f
)

data class ComboParticle(
    val cx: Float,
    val cy: Float,
    val zTop: Float,
    val text: String,
    var alpha: Float = 1.0f,
    var yOffset: Float = 0f
)

enum class ConfettiShape {
    RECTANGLE,
    CIRCLE,
    STAR,
    DIAMOND
}

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    val shapeType: ConfettiShape = ConfettiShape.RECTANGLE,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    var flutterPhase: Float = 0f,
    var flutterSpeed: Float = 0.12f,
    var alpha: Float = 1f,
    val maxAge: Float = 220f,
    var age: Float = 0f
)

data class ShockwaveRing(
    val cx: Float,
    val cy: Float,
    val zTop: Float,
    val sizeX: Float,
    val sizeY: Float,
    var expansion: Float = 0f,
    val maxExpansion: Float = 48f,
    val color: Color = Color.White,
    var alpha: Float = 0.85f
)

data class ShootingStar(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val length: Float,
    var progress: Float = 0f,
    val maxLifetime: Float = 45f
)

enum class Axis {
    X, Y
}

sealed class GamePhase {
    data object Playing : GamePhase()
    data object GameOverPrompt : GamePhase() // Shows "Save Me" prompt
    data object GameOverFinal : GamePhase()  // Final score summary
}

object ColorPaletteGenerator {
    // Generates smooth continuous pastel-vibrant colors shifting by layer index and theme matching reference video
    fun getColorForLayer(layerIndex: Int, theme: GameTheme = GameTheme.CLASSIC): Color {
        // Continuous hue shift of ~4.2 degrees per layer matches the smooth video gradient
        val hue = (theme.baseHue + layerIndex * 4.5f) % 360f
        val saturation = when (theme) {
            GameTheme.CYBERPUNK -> 0.85f
            GameTheme.CANDY -> 0.58f
            GameTheme.SUNSET -> 0.78f
            GameTheme.NEBULA -> 0.70f
            GameTheme.CLASSIC -> 0.70f
        }
        val lightness = when (theme) {
            GameTheme.CYBERPUNK -> 0.58f
            GameTheme.CANDY -> 0.72f
            GameTheme.SUNSET -> 0.60f
            GameTheme.NEBULA -> 0.62f
            GameTheme.CLASSIC -> 0.64f
        }
        return hslToColor(hue, saturation, lightness)
    }

    private fun hslToColor(hue: Float, sat: Float, light: Float): Color {
        val c = (1f - Math.abs(2f * light - 1f)) * sat
        val x = c * (1f - Math.abs((hue / 60f) % 2f - 1f))
        val m = light - c / 2f

        var r = 0f
        var g = 0f
        var b = 0f

        when {
            hue < 60f -> { r = c; g = x; b = 0f }
            hue < 120f -> { r = x; g = c; b = 0f }
            hue < 180f -> { r = 0f; g = c; b = x }
            hue < 240f -> { r = 0f; g = x; b = c }
            hue < 300f -> { r = x; g = 0f; b = c }
            else -> { r = c; g = 0f; b = x }
        }

        return Color(
            red = (r + m).coerceIn(0f, 1f),
            green = (g + m).coerceIn(0f, 1f),
            blue = (b + m).coerceIn(0f, 1f),
            alpha = 1.0f
        )
    }

    fun getTopFaceColor(base: Color): Color {
        return base
    }

    fun getLeftFaceColor(base: Color): Color {
        return Color(
            red = (base.red * 0.84f).coerceIn(0f, 1f),
            green = (base.green * 0.84f).coerceIn(0f, 1f),
            blue = (base.blue * 0.84f).coerceIn(0f, 1f),
            alpha = 1.0f
        )
    }

    fun getRightFaceColor(base: Color): Color {
        return Color(
            red = (base.red * 0.65f).coerceIn(0f, 1f),
            green = (base.green * 0.65f).coerceIn(0f, 1f),
            blue = (base.blue * 0.65f).coerceIn(0f, 1f),
            alpha = 1.0f
        )
    }
}
