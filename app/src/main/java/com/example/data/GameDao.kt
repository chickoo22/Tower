package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT MAX(score) FROM game_scores WHERE mode = :mode")
    fun getHighScoreForMode(mode: String): Flow<Int?>

    @Query("SELECT MAX(score) FROM game_scores")
    fun getGlobalHighScore(): Flow<Int?>

    @Query("SELECT * FROM game_scores ORDER BY score DESC LIMIT 10")
    fun getTopScores(): Flow<List<GameRecord>>

    @Query("SELECT COUNT(*) FROM game_scores")
    fun getTotalGamesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(record: GameRecord): Long

    // Player profile queries
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getPlayerProfileDirect(): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfile)
}
