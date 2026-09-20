package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String, // "EASY", "MEDIUM", "HARD"
    val score: Int,
    val coinsEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
