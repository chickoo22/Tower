package com.example.engine

import androidx.compose.ui.graphics.Color

enum class DifficultyMode(
    val title: String,
    val speed: Float,
    val perfectTolerance: Float,
    val initialSize: Float,
    val themeColor: Color,
    val displayHeader: String,
    val speedDescription: String,
    val levelRange: String,
    val levelNumber: Int,
    val coinMultiplier: Int
) {
    EASY(
        title = "EASY",
        speed = 2.0f,
        perfectTolerance = 5.0f,
        initialSize = 310f,
        themeColor = Color(0xFF10B981), // Emerald Green
        displayHeader = "SCORE",
        speedDescription = "Relaxed Pace (1x Coins)",
        levelRange = "Levels 1 - 60",
        levelNumber = 1,
        coinMultiplier = 1
    ),
    MEDIUM(
        title = "MEDIUM",
        speed = 2.8f,
        perfectTolerance = 4.5f,
        initialSize = 310f,
        themeColor = Color(0xFFF59E0B), // Amber Orange
        displayHeader = "SCORE",
        speedDescription = "Brisk Pace (1.5x Coins)",
        levelRange = "Levels 61 - 120",
        levelNumber = 61,
        coinMultiplier = 2
    ),
    HARD(
        title = "HARD",
        speed = 3.6f,
        perfectTolerance = 4.5f,
        initialSize = 310f,
        themeColor = Color(0xFF6366F1), // Royal Violet
        displayHeader = "SCORE",
        speedDescription = "Standard Pace (2x Coins)",
        levelRange = "Infinite Stacking",
        levelNumber = 121,
        coinMultiplier = 2
    ),
    EXTRA_HARD(
        title = "EXTRA HARD",
        speed = 4.6f,
        perfectTolerance = 3.5f,
        initialSize = 310f,
        themeColor = Color(0xFF5B67F6), // Electric Purple matching image.png
        displayHeader = "SCORE",
        speedDescription = "Extreme Pace (3x Coins)",
        levelRange = "Levels 181 - 240",
        levelNumber = 181,
        coinMultiplier = 3
    )
}
