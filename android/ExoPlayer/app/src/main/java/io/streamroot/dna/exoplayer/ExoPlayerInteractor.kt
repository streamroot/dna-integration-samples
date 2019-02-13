package io.streamroot.dna.exoplayer

import android.os.Looper
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import io.streamroot.dna.core.PlayerInteractor
import io.streamroot.dna.app.demo.player.exoplayer.ExoLoadControl
import io.streamroot.dna.core.TimeRange
import com.google.android.exoplayer2.LoadControl

class ExoPlayerInteractor(
    private val player: SimpleExoPlayer,
    val loadControl: LoadControl
) : PlayerInteractor {

    override fun looper(): Looper? = player.applicationLooper

    override fun loadedTimeRanges(): MutableList<TimeRange> {
        val shift = getCurrentWindowShift()
        val rangeDurationMs = player.bufferedPosition - player.currentPosition
        val timeRanges: MutableList<TimeRange> = ArrayList()

        if (rangeDurationMs > 0) {
            timeRanges.add(
                    TimeRange(
                            shift + player.currentPosition,
                            rangeDurationMs
                    )
            )
        }

        return timeRanges
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

    override fun bufferTarget(): Double {
        return if (loadControl is ExoLoadControl) {
            this.loadControl.getMaxBufferTarget()
        } else 0.0
    }

    override fun setBufferTarget(target: Double) {
        if (loadControl is ExoLoadControl) {
            loadControl.setMaxBufferTarget(target)
        }
    }
}