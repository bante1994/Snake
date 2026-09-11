package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class RetroAudioSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sampleRate = 44100

    var isEnabled: Boolean = true

    fun playMoveTick() {
        if (!isEnabled) return
        scope.launch {
            generateTone(frequency = 280.0, durationMs = 25, waveType = WaveType.SQUARE, volume = 0.15f)
        }
    }

    fun playAppleEat() {
        if (!isEnabled) return
        scope.launch {
            generateTone(frequency = 523.25, durationMs = 45, waveType = WaveType.SQUARE, volume = 0.25f)
            generateTone(frequency = 659.25, durationMs = 60, waveType = WaveType.SQUARE, volume = 0.25f)
        }
    }

    fun playGoldenApple() {
        if (!isEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50)
            for (freq in notes) {
                generateTone(frequency = freq, durationMs = 40, waveType = WaveType.SQUARE, volume = 0.3f)
            }
        }
    }

    fun playSpeedUp() {
        if (!isEnabled) return
        scope.launch {
            for (i in 0..5) {
                generateTone(frequency = 400.0 + (i * 80), durationMs = 25, waveType = WaveType.SAWTOOTH, volume = 0.25f)
            }
        }
    }

    fun playGameOver() {
        if (!isEnabled) return
        scope.launch {
            val tones = listOf(350.0, 280.0, 220.0, 150.0, 90.0)
            for (freq in tones) {
                generateTone(frequency = freq, durationMs = 70, waveType = WaveType.SQUARE, volume = 0.35f)
            }
        }
    }

    fun playReviveTone() {
        if (!isEnabled) return
        scope.launch {
            val notes = listOf(220.0, 330.0, 440.0, 554.37, 659.25, 880.0)
            for (freq in notes) {
                generateTone(frequency = freq, durationMs = 50, waveType = WaveType.SQUARE, volume = 0.32f)
            }
        }
    }

    fun playButtonClick() {
        if (!isEnabled) return
        scope.launch {
            generateTone(frequency = 780.0, durationMs = 20, waveType = WaveType.TRIANGLE, volume = 0.2f)
        }
    }

    fun playGestureFeedback() {
        if (!isEnabled) return
        scope.launch {
            generateTone(frequency = 640.0, durationMs = 18, waveType = WaveType.SINE, volume = 0.15f)
        }
    }

    fun playRadioChirp() {
        if (!isEnabled) return
        scope.launch {
            generateTone(frequency = 1760.0, durationMs = 25, waveType = WaveType.SINE, volume = 0.22f)
            generateTone(frequency = 2100.0, durationMs = 30, waveType = WaveType.SINE, volume = 0.22f)
        }
    }

    fun playRadioSquelch() {
        if (!isEnabled) return
        scope.launch {
            generateNoise(durationMs = 45, volume = 0.12f)
            generateTone(frequency = 1200.0, durationMs = 20, waveType = WaveType.TRIANGLE, volume = 0.15f)
        }
    }

    fun playTeamPing() {
        if (!isEnabled) return
        scope.launch {
            val notes = listOf(587.33, 880.0, 1174.66)
            for (freq in notes) {
                generateTone(frequency = freq, durationMs = 35, waveType = WaveType.SINE, volume = 0.25f)
            }
        }
    }

    fun playTeamWarning() {
        if (!isEnabled) return
        scope.launch {
            generateTone(frequency = 880.0, durationMs = 40, waveType = WaveType.SAWTOOTH, volume = 0.2f)
            generateTone(frequency = 440.0, durationMs = 50, waveType = WaveType.SAWTOOTH, volume = 0.2f)
        }
    }

    private enum class WaveType { SQUARE, SAWTOOTH, TRIANGLE, SINE }

    private fun generateNoise(durationMs: Int, volume: Float) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return
        val buffer = ShortArray(numSamples)
        val rng = java.util.Random()
        for (i in 0 until numSamples) {
            val env = when {
                i < numSamples * 0.2 -> i / (numSamples * 0.2)
                i > numSamples * 0.7 -> (numSamples - i) / (numSamples * 0.3)
                else -> 1.0
            }
            val white = (rng.nextFloat() * 2f - 1f) * env * Short.MAX_VALUE * volume
            buffer[i] = white.toInt().toShort()
        }
        playRawBuffer(buffer, durationMs)
    }

    private fun generateTone(
        frequency: Double,
        durationMs: Int,
        waveType: WaveType,
        volume: Float
    ) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        if (numSamples <= 0) return
        val buffer = ShortArray(numSamples)
        val period = sampleRate / frequency

        for (i in 0 until numSamples) {
            val phase = (i % period) / period
            val sampleValue = when (waveType) {
                WaveType.SQUARE -> if (phase < 0.5) 1.0 else -1.0
                WaveType.SAWTOOTH -> 2.0 * phase - 1.0
                WaveType.TRIANGLE -> if (phase < 0.5) 4.0 * phase - 1.0 else 3.0 - 4.0 * phase
                WaveType.SINE -> sin(2.0 * PI * phase)
            }
            // Apply quick attack and decay envelope to prevent audio popping
            val envelope = when {
                i < numSamples * 0.1 -> i / (numSamples * 0.1)
                i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                else -> 1.0
            }
            buffer[i] = (sampleValue * envelope * Short.MAX_VALUE * volume).toInt().toShort()
        }

        playRawBuffer(buffer, durationMs)
    }

    private fun playRawBuffer(buffer: ShortArray, durationMs: Int) {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
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
                .setBufferSizeInBytes(maxOf(minBufSize, buffer.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            // Release after playing
            Thread.sleep(durationMs.toLong() + 20)
            track.stop()
            track.release()
        } catch (_: Exception) {
            // Audio failure safe fallback
        }
    }
}
