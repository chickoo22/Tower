package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 50, // Starting welcome bonus
    val selectedThemeId: String = "CLASSIC",
    val unlockedThemeIds: String = "CLASSIC",
    val isVipNoAds: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val totalGamesPlayed: Int = 0,
    val totalCoinsEarned: Int = 0,
    val highestTowerHeight: Int = 0
)
