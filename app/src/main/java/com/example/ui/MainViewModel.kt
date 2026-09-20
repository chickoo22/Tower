package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRecord
import com.example.data.GameRepository
import com.example.data.PlayerProfile
import com.example.engine.AdRewardType
import com.example.engine.DifficultyMode
import com.example.engine.GameTheme
import com.example.engine.HapticManager
import com.example.engine.MonetizationManager
import com.example.engine.SoundManager
import com.example.engine.TowerEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenState {
    MENU,
    PLAYING
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    val soundManager = SoundManager(application)
    val hapticManager = HapticManager(application)
    val monetizationManager = MonetizationManager(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.gameDao())
    }

    private val _currentMode = MutableStateFlow(DifficultyMode.HARD)
    val currentMode: StateFlow<DifficultyMode> = _currentMode.asStateFlow()

    private val _screenState = MutableStateFlow(ScreenState.PLAYING)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    // Dialog state management
    private val _showShopDialog = MutableStateFlow(false)
    val showShopDialog: StateFlow<Boolean> = _showShopDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showPrivacyDialog = MutableStateFlow(false)
    val showPrivacyDialog: StateFlow<Boolean> = _showPrivacyDialog.asStateFlow()

    private val _showLeaderboardDialog = MutableStateFlow(false)
    val showLeaderboardDialog: StateFlow<Boolean> = _showLeaderboardDialog.asStateFlow()

    val playerProfile: StateFlow<PlayerProfile> = repository.getPlayerProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerProfile())

    val easyBest: StateFlow<Int> = repository.getHighScore("EASY")
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mediumBest: StateFlow<Int> = repository.getHighScore("MEDIUM")
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hardBest: StateFlow<Int> = repository.getHighScore("HARD")
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val extraHardBest: StateFlow<Int> = repository.getHighScore("EXTRA_HARD")
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val globalBest: StateFlow<Int> = repository.getGlobalHighScore()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val topScores: StateFlow<List<GameRecord>> = repository.getTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGames: StateFlow<Int> = repository.getTotalGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val engine = TowerEngine(
        soundManager = soundManager,
        hapticManager = hapticManager,
        onGameOverRecorded = { mode, score, coinsEarned ->
            saveGameScore(mode, score, coinsEarned)
        }
    )

    // Current Tower Height is directly tracked through the game state
    val currentTowerHeight: StateFlow<Int> = engine.score

    init {
        // Sync player settings, theme, and high score
        viewModelScope.launch {
            val profile = repository.getPlayerProfile().first()
            soundManager.setMuted(!profile.soundEnabled)
            val theme = GameTheme.fromId(profile.selectedThemeId)
            val best = repository.getGlobalHighScore().first() ?: 0
            engine.setTheme(theme)
            engine.setPreviousHighScore(best)
        }

        // Observe profile to sync sound/haptics settings and active theme
        viewModelScope.launch {
            playerProfile.collect { profile ->
                soundManager.setMuted(!profile.soundEnabled)
                val theme = GameTheme.fromId(profile.selectedThemeId)
                engine.setTheme(theme)
            }
        }
    }

    fun setDifficultyMode(mode: DifficultyMode) {
        soundManager.playButtonTap()
        hapticManager.triggerTap()
        _currentMode.value = mode
    }

    fun startGame() {
        soundManager.playButtonTap()
        val theme = GameTheme.fromId(playerProfile.value.selectedThemeId)
        val currentHighScore = globalBest.value
        engine.startNewGame(_currentMode.value, theme, currentHighScore)
        _screenState.value = ScreenState.PLAYING
    }

    fun restartGame() {
        soundManager.playButtonTap()
        val theme = GameTheme.fromId(playerProfile.value.selectedThemeId)
        val currentHighScore = globalBest.value
        engine.startNewGame(_currentMode.value, theme, currentHighScore)
    }

    fun backToMenu() {
        soundManager.playButtonTap()
        restartGame()
    }

    fun onScreenTap() {
        engine.onScreenTap()
    }

    // Monetization: Rewarded Ad for Save Me extra life
    fun triggerSaveMeWithAd() {
        val isVip = playerProfile.value.isVipNoAds
        monetizationManager.showRewardedVideo(
            rewardType = AdRewardType.SAVE_ME_EXTRA_LIFE,
            isVipUser = isVip
        ) {
            engine.onSaveMeClicked()
        }
    }

    // Monetization: Rewarded Ad for Bonus Coins (+50)
    fun watchAdForBonusCoins() {
        soundManager.playButtonTap()
        val isVip = playerProfile.value.isVipNoAds
        monetizationManager.showRewardedVideo(
            rewardType = AdRewardType.BONUS_COINS_50,
            isVipUser = isVip
        ) {
            viewModelScope.launch {
                repository.addCoins(50)
                soundManager.playCoinSound()
            }
        }
    }

    // Monetization: Double Coins on Game Over
    fun watchAdForDoubleCoins(earnedCoins: Int) {
        soundManager.playButtonTap()
        val isVip = playerProfile.value.isVipNoAds
        monetizationManager.showRewardedVideo(
            rewardType = AdRewardType.DOUBLE_GAME_COINS,
            isVipUser = isVip
        ) {
            viewModelScope.launch {
                repository.addCoins(earnedCoins)
                soundManager.playCoinSound()
            }
        }
    }

    // Shop Theme Purchase
    fun purchaseTheme(theme: GameTheme, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.unlockTheme(theme.id, theme.priceCoins)
            if (success) {
                soundManager.playUnlockSound()
                engine.setTheme(theme)
                onSuccess()
            } else {
                hapticManager.triggerGameOver()
                onFailure()
            }
        }
    }

    fun selectTheme(theme: GameTheme) {
        viewModelScope.launch {
            soundManager.playButtonTap()
            repository.selectTheme(theme.id)
            engine.setTheme(theme)
        }
    }

    fun unlockVipPass() {
        viewModelScope.launch {
            soundManager.playUnlockSound()
            repository.unlockVipNoAds()
        }
    }

    // Settings
    fun toggleSound() {
        viewModelScope.launch {
            val current = playerProfile.value.soundEnabled
            repository.setSoundEnabled(!current)
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            val current = playerProfile.value.hapticsEnabled
            repository.setHapticsEnabled(!current)
        }
    }

    // Dialog toggles
    fun setShowShop(show: Boolean) {
        soundManager.playButtonTap()
        _showShopDialog.value = show
    }

    fun setShowSettings(show: Boolean) {
        soundManager.playButtonTap()
        _showSettingsDialog.value = show
    }

    fun setShowPrivacy(show: Boolean) {
        soundManager.playButtonTap()
        _showPrivacyDialog.value = show
    }

    fun setShowLeaderboard(show: Boolean) {
        soundManager.playButtonTap()
        _showLeaderboardDialog.value = show
    }

    private fun saveGameScore(mode: DifficultyMode, score: Int, coinsEarned: Int) {
        if (score <= 0) return
        viewModelScope.launch {
            repository.saveGameResult(mode.name, score, coinsEarned)
        }
    }
}
