package com.example.twentyquestions.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class RetroSoundManager {
    var enabled: Boolean = true

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playClick() {
        if (!enabled) return
        scope.launch {
            playToneSweep(800.0, 300.0, 0.04, WaveType.TRIANGLE, 0.15f)
        }
    }

    fun playAnswerYes() {
        if (!enabled) return
        scope.launch {
            playToneSequence(listOf(520.0 to 0.07, 660.0 to 0.09), WaveType.SINE, 0.2f)
        }
    }

    fun playAnswerNo() {
        if (!enabled) return
        scope.launch {
            playToneSequence(listOf(320.0 to 0.07, 220.0 to 0.09), WaveType.SQUARE, 0.15f)
        }
    }

    fun playAnswerSometimes() {
        if (!enabled) return
        scope.launch {
            playToneSequence(listOf(440.0 to 0.06, 554.0 to 0.08), WaveType.TRIANGLE, 0.18f)
        }
    }

    fun playAnswerUnknown() {
        if (!enabled) return
        scope.launch {
            playToneSweep(380.0, 380.0, 0.1, WaveType.SINE, 0.15f)
        }
    }

    fun playVictory() {
        if (!enabled) return
        scope.launch {
            playToneSequence(
                listOf(
                    440.0 to 0.09,
                    554.0 to 0.09,
                    659.0 to 0.09,
                    880.0 to 0.22
                ),
                WaveType.SINE,
                0.25f
            )
        }
    }

    fun playDefeat() {
        if (!enabled) return
        scope.launch {
            playToneSweep(440.0, 180.0, 0.35, WaveType.SQUARE, 0.15f)
        }
    }

    fun playLearning() {
        if (!enabled) return
        scope.launch {
            playToneSequence(
                listOf(300.0 to 0.06, 600.0 to 0.06, 900.0 to 0.1),
                WaveType.TRIANGLE,
                0.2f
            )
        }
    }

    private enum class WaveType {
        SINE, SQUARE, TRIANGLE
    }

    private fun playToneSequence(notes: List<Pair<Double, Double>>, type: WaveType, volume: Float) {
        try {
            val totalSeconds = notes.sumOf { it.second }
            val totalSamples = (totalSeconds * sampleRate).toInt()
            val buffer = ShortArray(totalSamples)

            var offset = 0
            for ((freq, duration) in notes) {
                val numSamples = (duration * sampleRate).toInt()
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val sample = generateSample(freq, t, type)
                    val envelope = when {
                        i < numSamples * 0.1 -> i / (numSamples * 0.1)
                        i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                        else -> 1.0
                    }
                    if (offset + i < buffer.size) {
                        buffer[offset + i] = (sample * volume * envelope * Short.MAX_VALUE).toInt().toShort()
                    }
                }
                offset += numSamples
            }

            playBuffer(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playToneSweep(startFreq: Double, endFreq: Double, duration: Double, type: WaveType, volume: Float) {
        try {
            val numSamples = (duration * sampleRate).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val t = i.toDouble() / sampleRate
                val sample = generateSample(currentFreq, t, type)
                val envelope = 1.0 - progress
                buffer[i] = (sample * volume * envelope * Short.MAX_VALUE).toInt().toShort()
            }

            playBuffer(buffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateSample(freq: Double, t: Double, type: WaveType): Double {
        val cycle = (t * freq) % 1.0
        return when (type) {
            WaveType.SINE -> sin(2.0 * PI * cycle)
            WaveType.SQUARE -> if (cycle < 0.5) 1.0 else -1.0
            WaveType.TRIANGLE -> if (cycle < 0.5) 4.0 * cycle - 1.0 else 3.0 - 4.0 * cycle
        }
    }

    private fun playBuffer(buffer: ShortArray) {
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
        audioTrack.setNotificationMarkerPosition(buffer.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
            override fun onPeriodicNotification(track: AudioTrack?) {}
        })
    }
}
