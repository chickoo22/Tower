package com.example.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun triggerTap() {
        vibrate(15, 60)
    }

    fun triggerPerfect() {
        vibrate(30, 150)
    }

    fun triggerSlice() {
        vibrate(20, 100)
    }

    fun triggerGameOver() {
        vibrate(80, 255)
    }

    fun triggerHighScoreCelebration() {
        vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Triple celebratory buzz pattern
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 45, 60, 50, 60, 90),
                        intArrayOf(0, 160, 0, 210, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (_: Exception) {
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }
}
