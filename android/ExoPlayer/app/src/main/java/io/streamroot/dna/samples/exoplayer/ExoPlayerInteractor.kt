package io.streamroot.dna.samples.exoplayer

import android.os.Looper
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.Timeline
import io.streamroot.dna.core.PlayerInteractor
import io.streamroot.dna.core.TimeRange
import java.util.concurrent.TimeUnit

private fun LoadControl.getAccessibleFieldElseThrow(fieldName: String) = runCatching {
    val minBufferField = this::class.java.getDeclaredField(fieldName)
    minBufferField.isAccessible = true
    minBufferField
}.getOrNull() ?: throw IllegalArgumentException("Impossible to retrieve field `$fieldName` value from LoadControl of type `${this::class.java.simpleName}`")

private fun LoadControl.getLongFromFieldElseThrow(fieldName: String) = runCatching {
    getAccessibleFieldElseThrow(fieldName).getLong(this)
}.getOrNull() ?: throw IllegalArgumentException("Impossible to retrieve long `$fieldName` value from LoadControl of type `${this::class.java.simpleName}`")

private open class ExoPlayerInteractorBase(private val player: ExoPlayer) : PlayerInteractor {

    override fun looper(): Looper = player.applicationLooper

    override fun loadedTimeRanges(): List<TimeRange> {
        val shift = getCurrentWindowShift()
        val rangeDurationMs = player.bufferedPosition - player.currentPosition
        return if (rangeDurationMs > 0) {
            arrayListOf(TimeRange(shift + player.currentPosition, rangeDurationMs))
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

private abstract class ExoPlayerInteractorBT(
        player: ExoPlayer,
        private val loadControl: LoadControl
) : ExoPlayerInteractorBase(player) {

    companion object {
        private const val MAX_BUFFER_FIELD_NAME = "maxBufferUs"
    }

    protected val maxBufferField = loadControl.getAccessibleFieldElseThrow(MAX_BUFFER_FIELD_NAME)
    protected abstract val minBufferUs: Long

    override fun bufferTarget(): Double {
        return runCatching {
            maxBufferField.getLong(loadControl).let { TimeUnit.MICROSECONDS.toSeconds(it) }.toDouble()
        }.getOrNull() ?: 0.0
    }

    override fun setBufferTarget(bufferTarget: Double) {
        val maxBufferUs = TimeUnit.SECONDS.toMicros(bufferTarget.toLong())
        if (maxBufferUs > minBufferUs) runCatching {
            maxBufferField.setLong(
                    loadControl,
                    maxBufferUs
            )
        }
    }
}

private class ExoPlayerInteractorV1(player: ExoPlayer, loadControl: LoadControl)
    : ExoPlayerInteractorBT(player, loadControl) {
    companion object {
        private const val MIN_BUFFER_FIELD_NAME = "minBufferUs"
    }

    override val minBufferUs = loadControl.getLongFromFieldElseThrow(MIN_BUFFER_FIELD_NAME)
}

private class ExoPlayerInteractorV2(player: ExoPlayer, loadControl: LoadControl, audioOnly: Boolean)
    : ExoPlayerInteractorBT(player, loadControl) {
    companion object {
        private const val MIN_BUFFER_AUDIO_FIELD_NAME = "minBufferAudioUs"
        private const val MIN_BUFFER_VIDEO_FIELD_NAME = "minBufferVideoUs"
    }

    override val minBufferUs = loadControl.getLongFromFieldElseThrow(
            if (audioOnly) MIN_BUFFER_AUDIO_FIELD_NAME else MIN_BUFFER_VIDEO_FIELD_NAME
    )
}

object ExoPlayerInteractorFactory {
    fun createInteractor(player: ExoPlayer, loadControl: LoadControl, audioOnly: Boolean = false) : PlayerInteractor {
        return runCatching { ExoPlayerInteractorV1(player, loadControl) }.getOrNull()
            ?: runCatching { ExoPlayerInteractorV2(player, loadControl, audioOnly) }.getOrNull()
            ?: ExoPlayerInteractorBase(player)
    }
}