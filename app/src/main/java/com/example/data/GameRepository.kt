package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val gameDao: GameDao) {
    fun getHighScore(mode: String): Flow<Int?> = gameDao.getHighScoreForMode(mode)

    fun getGlobalHighScore(): Flow<Int?> = gameDao.getGlobalHighScore()

    fun getTopScores(): Flow<List<GameRecord>> = gameDao.getTopScores()

    fun getTotalGames(): Flow<Int> = gameDao.getTotalGamesCount()

    fun getPlayerProfile(): Flow<PlayerProfile> {
        return gameDao.getPlayerProfile().map { profile ->
            profile ?: PlayerProfile()
        }
    }

    suspend fun saveGameResult(mode: String, score: Int, coinsEarned: Int): Long {
        val recordId = gameDao.insertScore(
            GameRecord(
                mode = mode,
                score = score,
                coinsEarned = coinsEarned
            )
        )

        val currentProfile = gameDao.getPlayerProfileDirect() ?: PlayerProfile()
        val newHighest = maxOf(currentProfile.highestTowerHeight, score)
        val updatedProfile = currentProfile.copy(
            coins = currentProfile.coins + coinsEarned,
            totalCoinsEarned = currentProfile.totalCoinsEarned + coinsEarned,
            totalGamesPlayed = currentProfile.totalGamesPlayed + 1,
            highestTowerHeight = newHighest
        )
        gameDao.insertOrUpdateProfile(updatedProfile)
        return recordId
    }

    suspend fun addCoins(amount: Int) {
        val current = gameDao.getPlayerProfileDirect() ?: PlayerProfile()
        val updated = current.copy(
            coins = current.coins + amount,
            totalCoinsEarned = current.totalCoinsEarned + amount
        )
        gameDao.insertOrUpdateProfile(updated)
    }

    suspend fun unlockTheme(themeId: String, cost: Int): Boolean {
        val current = gameDao.getPlayerProfileDirect() ?: PlayerProfile()
        if (current.coins < cost) return false

        val unlockedList = current.unlockedThemeIds.split(",").toMutableSet()
        unlockedList.add(themeId)

        val updated = current.copy(
            coins = current.coins - cost,
            unlockedThemeIds = unlockedList.joinToString(","),
            selectedThemeId = themeId
        )
        gameDao.insertOrUpdateProfile(updated)
        return true
    }

    suspend fun selectTheme(themeId: String) {
        val current = gameDao.getPlayerProfileDirect() ?: PlayerProfile()
        val updated = current.copy(selectedThemeId = themeId)
        gameDao.insertOrUpdateProfile(updated)
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        val current = gameDao.getPlayerProfileDirect() ?: PlayerProfile()
        gameDao.insertOrUpdateProfile(current.copy(soundEnabled = enabled))
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        val current = gameDao.getPlayerProfileDirect() ?: PlayerProfile()
        gameDao.insertOrUpdateProfile(current.copy(hapticsEnabled = enabled))
    }

    suspend fun unlockVipNoAds() {
        val current = gameDao.getPlayerProfileDirect() ?: PlayerProfile()
        gameDao.insertOrUpdateProfile(current.copy(isVipNoAds = true))
    }
}
