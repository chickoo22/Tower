package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050
    private var isMuted = false

    // Musical scale for combo notes: C4, D4, E4, F4, G4, A4, B4, C5, D5, E5, F5, G5, A5, B5, C6
    private val scaleFrequencies = floatArrayOf(
        261.63f, 293.66f, 329.63f, 349.23f, 392.00f, 440.00f, 493.88f,
        523.25f, 587.33f, 659.25f, 698.46f, 783.99f, 880.00f, 987.77f, 1046.50f
    )

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        return isMuted
    }

    fun playStackSound(combo: Int) {
        if (isMuted) return
        val noteIdx = combo.coerceIn(0, scaleFrequencies.lastIndex)
        val freq = scaleFrequencies[noteIdx]
        playTone(freq, durationMs = 120, decay = 0.85f, amplitude = 0.7f)
    }

    fun playSliceSound() {
        if (isMuted) return
        // Crisp slice / woodblock click sound
        playClick(durationMs = 45, startFreq = 480f, endFreq = 220f)
    }

    fun playGameOverSound() {
        if (isMuted) return
        scope.launch {
            playTone(330f, 150, 0.9f, 0.6f)
            kotlinx.coroutines.delay(100)
            playTone(260f, 250, 0.95f, 0.7f)
        }
    }

    fun playSaveMeSound() {
        if (isMuted) return
        scope.launch {
            playTone(440f, 100, 0.8f, 0.6f)
            kotlinx.coroutines.delay(80)
            playTone(660f, 100, 0.8f, 0.7f)
            kotlinx.coroutines.delay(80)
            playTone(880f, 200, 0.9f, 0.8f)
        }
    }

    fun playCoinSound() {
        if (isMuted) return
        scope.launch {
            playTone(987.77f, 60, 0.9f, 0.5f) // B5
            kotlinx.coroutines.delay(50)
            playTone(1318.51f, 120, 0.95f, 0.6f) // E6
        }
    }

    fun playUnlockSound() {
        if (isMuted) return
        scope.launch {
            playTone(523.25f, 90, 0.85f, 0.6f) // C5
            kotlinx.coroutines.delay(70)
            playTone(659.25f, 90, 0.85f, 0.6f) // E5
            kotlinx.coroutines.delay(70)
            playTone(783.99f, 90, 0.85f, 0.7f) // G5
            kotlinx.coroutines.delay(70)
            playTone(1046.50f, 220, 0.9f, 0.8f) // C6
        }
    }

    fun playButtonTap() {
        if (isMuted) return
        playClick(durationMs = 25, startFreq = 600f, endFreq = 300f)
    }

    fun playHighScoreSound() {
        if (isMuted) return
        scope.launch {
            // Triumphant rising celebration fanfare: E5 -> G#5 -> B5 -> E6
            playTone(659.25f, 100, 0.85f, 0.7f) // E5
            kotlinx.coroutines.delay(85)
            playTone(830.61f, 100, 0.85f, 0.75f) // G#5
            kotlinx.coroutines.delay(85)
            playTone(987.77f, 120, 0.9f, 0.8f) // B5
            kotlinx.coroutines.delay(100)
            playTone(1318.51f, 320, 0.92f, 0.9f) // E6 triumphant peak
        }
    }

    private fun playTone(frequency: Float, durationMs: Int, decay: Float = 0.9f, amplitude: Float = 0.6f) {
        scope.launch {
            try {
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                val angularFreq = (2.0 * Math.PI * frequency) / sampleRate

                for (i in 0 until numSamples) {
                    val envelope = Math.pow((1.0 - (i.toDouble() / numSamples)), decay.toDouble())
                    val sample = (sin(angularFreq * i) * Short.MAX_VALUE * amplitude * envelope).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                // Release after playback
                kotlinx.coroutines.delay(durationMs + 50L)
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio errors gracefully
            }
        }
    }

    private fun playClick(durationMs: Int, startFreq: Float, endFreq: Float) {
        scope.launch {
            try {
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress
                    val angularFreq = (2.0 * Math.PI * currentFreq) / sampleRate
                    val envelope = (1.0 - progress) * (1.0 - progress)
                    val sample = (sin(angularFreq * i) * Short.MAX_VALUE * 0.5 * envelope).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                kotlinx.coroutines.delay(durationMs + 40L)
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }
}
