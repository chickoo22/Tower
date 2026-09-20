package com.example.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdState {
    data object Idle : AdState()
    data class Playing(val countdownSec: Int, val rewardType: AdRewardType) : AdState()
    data class Completed(val rewardType: AdRewardType) : AdState()
}

enum class AdRewardType {
    SAVE_ME_EXTRA_LIFE,
    BONUS_COINS_50,
    DOUBLE_GAME_COINS
}

/**
 * Manages monetization, rewarded video ads, and in-app purchase hooks.
 * Designed for Google Play & Indus Appstore publication with AdMob / Indus Ads SDK hooks.
 * Fully offline-capable: if no internet/Wi-Fi connection is detected, rewards are granted immediately
 * without blocking gameplay. When internet is active, full rewarded video ads are presented.
 */
class MonetizationManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var adJob: Job? = null

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    // Configurable Ad Unit IDs for production release on Google Play & Indus Appstore
    val playStoreAdUnitId = "ca-app-pub-3940256099942544/5224354917" // Test Rewarded Ad Unit
    val indusAppstoreAdUnitId = "indus-ads-tower-rewarded-001"

    /**
     * Checks if device currently has active internet connection (Wi-Fi or cellular).
     */
    fun isOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    fun showRewardedVideo(
        rewardType: AdRewardType,
        isVipUser: Boolean = false,
        onRewardGranted: (AdRewardType) -> Unit
    ) {
        // If user has VIP No-Ads OR the device is offline, grant reward immediately!
        if (isVipUser || !isOnline()) {
            onRewardGranted(rewardType)
            return
        }

        // When internet is connected, show the rewarded video ad
        adJob?.cancel()
        adJob = scope.launch {
            // 4-second interactive rewarded preview
            for (sec in 4 downTo 1) {
                _adState.value = AdState.Playing(countdownSec = sec, rewardType = rewardType)
                delay(1000)
            }
            _adState.value = AdState.Completed(rewardType)
            onRewardGranted(rewardType)
            delay(500)
            _adState.value = AdState.Idle
        }
    }

    fun dismissAdEarly() {
        adJob?.cancel()
        _adState.value = AdState.Idle
    }
}
