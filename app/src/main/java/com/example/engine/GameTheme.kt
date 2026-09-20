package com.example.engine

import androidx.compose.ui.graphics.Color

enum class GameTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceCoins: Int,
    val skyColors: List<Color>,
    val accentColor: Color,
    val baseHue: Float,
    val isDefault: Boolean = false
) {
    CLASSIC(
        id = "CLASSIC",
        title = "Classic Twilight",
        subtitle = "Vibrant rose-magenta sky with fresh lime tower and deep twilight mist",
        priceCoins = 0,
        skyColors = listOf(
            Color(0xFFB51A75), // Rich magenta pink at top matching screenshot
            Color(0xFFC73086), // Vibrant fuchsia
            Color(0xFFD45598), // Radiant rose
            Color(0xFFDC7DAF), // Soft pastel rose
            Color(0xFFF0BDD6), // Luminous sun halo at horizon
            Color(0xFFA1729E), // Lavender mist
            Color(0xFF5A4469)  // Atmospheric twilight
        ),
        accentColor = Color(0xFFFF4081), // Vibrant electric pink matching screenshot
        baseHue = 100f, // Exact vibrant fresh lime green (#76D842) default tower from screenshot!
        isDefault = true
    ),
    CYBERPUNK(
        id = "CYBERPUNK",
        title = "Cyber Neon",
        subtitle = "Synthwave pulse with electric violet & laser pink",
        priceCoins = 150,
        skyColors = listOf(
            Color(0xFF16032B), // Deep void violet
            Color(0xFF2E0854), // Synthwave purple
            Color(0xFF511281), // Vivid magenta
            Color(0xFF86198F), // Neon glow
            Color(0xFF0F172A)  // Electric horizon
        ),
        accentColor = Color(0xFFFF007F),
        baseHue = 180f
    ),
    CANDY(
        id = "CANDY",
        title = "Pastel Candy",
        subtitle = "Sweet strawberry blush and marshmallow sky",
        priceCoins = 250,
        skyColors = listOf(
            Color(0xFF4A3E72), // Lavender dusk
            Color(0xFF7B68EE), // Soft periwinkle
            Color(0xFFEE82EE), // Candy violet
            Color(0xFFFFB6C1), // Baby pink
            Color(0xFFFFDAB9)  // Peach horizon
        ),
        accentColor = Color(0xFFFF69B4),
        baseHue = 45f
    ),
    SUNSET(
        id = "SUNSET",
        title = "Golden Sunset",
        subtitle = "Radiant amber horizon and fiery dusk gradients",
        priceCoins = 350,
        skyColors = listOf(
            Color(0xFF2B0938), // Dark berry
            Color(0xFF5E1343), // Deep crimson
            Color(0xFF9E2A2B), // Sunset red
            Color(0xFFE07A5F), // Coral glow
            Color(0xFFF4A261)  // Golden horizon
        ),
        accentColor = Color(0xFFFFA000),
        baseHue = 15f
    ),
    NEBULA(
        id = "NEBULA",
        title = "Deep Nebula",
        subtitle = "Cosmic starfield with mystical stardust waves",
        priceCoins = 500,
        skyColors = listOf(
            Color(0xFF0B0C10), // Deep obsidian
            Color(0xFF1F2833), // Space grey
            Color(0xFF2C061F), // Dark nebula
            Color(0xFF371B58), // Cosmic violet
            Color(0xFF4C3575)  // Mystic haze
        ),
        accentColor = Color(0xFF00E5FF),
        baseHue = 260f
    );

    companion object {
        fun fromId(id: String): GameTheme {
            return entries.find { it.id == id } ?: CLASSIC
        }
    }
}
