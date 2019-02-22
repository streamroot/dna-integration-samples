package io.streamroot.dna.exoplayer

import android.os.Looper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Timeline
import io.streamroot.dna.core.PlayerInteractor
import io.streamroot.dna.core.TimeRange
import com.google.android.exoplayer2.LoadControl
import java.lang.reflect.Field
import java.util.concurrent.TimeUnit

class ExoPlayerInteractor(
    private val player: ExoPlayer,
    private val loadControl: LoadControl,
    maxBufferFieldName: String = "maxBufferUs",
    minBufferFieldName: String = "minBufferUs"
) : PlayerInteractor {
    private val minBufferUs: Long
    private val maxBufferField: Field

    init {
        minBufferUs = runCatching {
            val minBufferField = loadControl::class.java.getDeclaredField(minBufferFieldName)
            minBufferField.isAccessible = true
            minBufferField.getLong(loadControl)
        }.getOrNull() ?: throw IllegalArgumentException("Impossible to retrieve minBuffer field `$minBufferFieldName` value from LoadControl of type `${loadControl::class.java.simpleName}`")

        maxBufferField = runCatching { loadControl::class.java.getDeclaredField(maxBufferFieldName) }.getOrNull()
            ?: throw IllegalArgumentException("Impossible to retrieve maxBuffer field `$maxBufferFieldName` from LoadControl of type `${loadControl::class.java.simpleName}`")
        maxBufferField.isAccessible = true
    }

    override fun looper(): Looper = player.applicationLooper

    override fun bufferTarget(): Double {
        return runCatching { maxBufferField.getLong(loadControl).let { TimeUnit.MICROSECONDS.toSeconds(it) }.toDouble() }.getOrNull()
            ?: 0.0
    }

    override fun setBufferTarget(target: Double) {
        val maxBufferUs = TimeUnit.SECONDS.toMicros(target.toLong())
        if (maxBufferUs > minBufferUs) runCatching { maxBufferField.setLong(loadControl, maxBufferUs) }
    }

    override fun loadedTimeRanges(): List<TimeRange> {
        val shift = getCurrentWindowShift()
        val rangeDurationMs = player.bufferedPosition - player.currentPosition
        return if (rangeDurationMs > 0) {
            arrayListOf(TimeRange(shift + player.currentPosition, rangeDurationMs) )
        } else {
            emptyList()
        }
    }

    override fun playbackTime(): Long {
        return getCurrentWindowShift() + player.currentPosition
    }

    private fun getCurrentWindowShift(): Long {
        val current = player.currentTimeline
        val timelineWindow = Timeline.Window()
        var shift: Long = 0

        if (player.currentWindowIndex < current?.windowCount!!) {
            player.currentTimeline?.getWindow(player.currentWindowIndex, timelineWindow)
            shift = timelineWindow.positionInFirstPeriodMs
        }

        return shift
    }
}