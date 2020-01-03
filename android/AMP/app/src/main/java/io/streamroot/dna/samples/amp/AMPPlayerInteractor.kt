package io.streamroot.dna.samples.amp

import android.os.Looper
import android.util.Log
import com.akamai.amp.media.VideoPlayerView
import com.akamai.exoplayer2.DefaultLoadControl
import com.akamai.exoplayer2.Timeline
import com.akamai.exoplayer2.util.AMPPreSettings
import io.streamroot.dna.core.PlayerInteractor
import io.streamroot.dna.core.TimeRange
import java.lang.StringBuilder
import java.lang.reflect.Field
import java.util.concurrent.TimeUnit

class AMPPlayerInteractor(
        private val loadControl: DefaultLoadControl,
        maxBufferFieldName: String = "maxBufferUs",
        minBufferFieldName: String = "minBufferUs"
) : PlayerInteractor {
    private val TAG = "AMPPlayerInteractor"
    private val ref = System.currentTimeMillis()
    private val minBufferUs: Long
    private val maxBufferField: Field

    private var playerView: VideoPlayerView? = null
    fun setPlayer(player: VideoPlayerView) { this.playerView = player }

    init {
        minBufferUs = runCatching {
            val minBufferField = loadControl::class.java.getDeclaredField(minBufferFieldName)
            minBufferField.isAccessible = true
            minBufferField.getLong(loadControl)
        }.getOrNull()
                ?: throw IllegalArgumentException("Impossible to retrieve minBuffer field `$minBufferFieldName` value from LoadControl of type `${loadControl::class.java.simpleName}`")

        maxBufferField =
                runCatching { loadControl::class.java.getDeclaredField(maxBufferFieldName) }.getOrNull()
                        ?: throw IllegalArgumentException("Impossible to retrieve maxBuffer field `$maxBufferFieldName` from LoadControl of type `${loadControl::class.java.simpleName}`")
        maxBufferField.isAccessible = true
    }

    override fun looper(): Looper? {
        return playerView?.ampBasePlayer?.applicationLooper ?: super.looper()
    }

    override fun bufferTarget(): Double {
        return runCatching {
            maxBufferField.getLong(loadControl).let { TimeUnit.MICROSECONDS.toSeconds(it) }
                    .toDouble()
        }.getOrNull()
                ?: 0.0
    }

    override fun setBufferTarget(bufferTarget: Double) {
        Log.v(TAG, "Setbuffer target -> " + bufferTarget)
        val maxBufferUs = TimeUnit.SECONDS.toMicros(bufferTarget.toLong())
        if (maxBufferUs > minBufferUs) runCatching {
            maxBufferField.setLong(
                    loadControl,
                    maxBufferUs
            )
        }
    }

    override fun playbackTime(): Long {
        return playerView?.let {
            Log.v(TAG, "Playbacktime -> " + (getCurrentWindowShift() + it.ampBasePlayer.currentPosition))
            Log.v(TAG, "Buffered -> " + it.ampBasePlayer.totalBufferedDuration)
            getCurrentWindowShift() + it.ampBasePlayer.currentPosition
        } ?: 0L
    }

    override fun loadedTimeRanges(): List<TimeRange> {
        return playerView?.let {
            val player = it.ampBasePlayer
            val shift = getCurrentWindowShift()
            val rangeDurationMs = player.bufferedPosition - player.currentPosition
            return if (rangeDurationMs > 0) {
                val tr = TimeRange(shift + player.currentPosition, rangeDurationMs)
                Log.v(TAG, "Time range -> " + tr)
                arrayListOf(tr)
            } else {
                emptyList()
            }
        } ?: emptyList()
    }

    private fun getCurrentWindowShift(): Long {
        return playerView?.let {
            val player = it.ampBasePlayer
            val current = player.currentTimeline
            val timelineWindow = Timeline.Window()
            var shift: Long = 0

            if (player.currentWindowIndex < current?.windowCount!!) {
                player.currentTimeline?.getWindow(player.currentWindowIndex, timelineWindow)
                shift = timelineWindow.positionInFirstPeriodMs
            }

            return shift
        } ?: 0L
    }


    fun logFull() {
        playerView?.let { playerView -> playerView.ampBasePlayer.let { ampPlayer ->
            val sb = StringBuilder().append("\n")
            sb.append("Timestamp " + (System.currentTimeMillis() - ref)).append("\n")
            sb.append("playerView.playheadPosition " + playerView.playheadPosition).append("\n")
            //sb.append("playerView.bufferAvailable " + playerView.bufferAvailable).append("\n")
            //sb.append("playerView.bufferingPercentage " + playerView.bufferingPercentage).append("\n")
            //sb.append("playerView.bytesLoaded " + playerView.bytesLoaded).append("\n")
            //sb.append("playerView.currentBitrate " + playerView.currentBitrate).append("\n")
            sb.append("playerView.currentPositionPeriod " + playerView.currentPositionPeriod).append("\n")
            //sb.append("playerView.currentStreamPosition " + playerView.currentStreamPosition).append("\n")
            sb.append("playerView.currentStreamPositionMS " + playerView.currentStreamPositionMS).append("\n")
            //sb.append("playerView.currentTimelinePosition " + playerView.currentTimelinePosition).append("\n")
            sb.append("playerView.currentTimelinePositionMS " + playerView.currentTimelinePositionMS).append("\n")
            //sb.append("playerView.currentStreamPositionAsDate " + playerView.currentStreamPositionAsDate).append("\n")
            //sb.append("playerView.lastMeasuredBandwidth " + playerView.lastMeasuredBandwidth).append("\n")
            //sb.append("playerView.playbackRate " + playerView.playbackRate).append("\n")
            //sb.append("playerView.positionInDVR " + playerView.positionInDVR).append("\n")
            //sb.append("playerView.rebufferingTime " + playerView.rebufferingTime).append("\n")
            //sb.append("playerView.rebuffers " + playerView.rebuffers).append("\n")
            //sb.append("playerView.isLive " + playerView.isLive).append("\n")
            //sb.append("playerView.streamDuration " + playerView.streamDuration).append("\n")
            //sb.append("playerView.timelineDuration " + playerView.timelineDuration).append("\n")
            //sb.append("playerView.bufferingPercentage " + playerView.setRebufferingSize()).append("\n")
            //sb.append("playerView.bufferingPercentage " + playerView.setRebufferingMode()).append("\n")
            //sb.append("playerView.bufferingPercentage " + playerView.setTimelineListener()).append("\n")
            //sb.append("playerView.bufferingPercentage " + playerView.setVideoBufferSize()).append("\n")

            sb.append("ampPlayer.bufferedPosition " + ampPlayer.bufferedPosition).append("\n")
            sb.append("ampPlayer.currentPosition " + ampPlayer.currentPosition).append("\n")
            //sb.append("ampPlayer.contentBufferedPosition " + ampPlayer.contentBufferedPosition).append("\n")
            //sb.append("ampPlayer.contentPosition " + ampPlayer.contentPosition).append("\n")
            //sb.append("ampPlayer.currentPeriodIndex " + ampPlayer.currentPeriodIndex).append("\n")
            //sb.append("ampPlayer.currentWindowIndex " + ampPlayer.currentWindowIndex).append("\n")

            sb.append("ampPlayer.duration " + ampPlayer.duration).append("\n")
            sb.append("ampPlayer.totalBufferedDuration " + ampPlayer.totalBufferedDuration).append("\n")
            //sb.append("ampPlayer.bufferedPercentage " + ampPlayer.bufferedPercentage).append("\n")
            //sb.append("ampPlayer.contentDuration " + ampPlayer.contentDuration).append("\n")
            //sb.append("ampPlayer.bufferingPercentage " + ampPlayer.addListener()).append("\n")
            //sb.append("ampPlayer.bufferingPercentage " + ampPlayer.addVideoListener()).append("\n")

            val tl = ampPlayer.currentTimeline
            //sb.append("tl.isEmpty " + tl.isEmpty).append("\n")
            //sb.append("tl.periodCount " + tl.periodCount).append("\n")
            //sb.append("tl.windowCount " + tl.windowCount).append("\n")

            val win = Timeline.Window()
            tl.getWindow(0, win)
            sb.append("win.defaultPositionMs " + win.defaultPositionMs).append("\n")
            //sb.append("win.defaultPositionUs " + win.defaultPositionUs).append("\n")
            sb.append("win.durationMs " + win.durationMs).append("\n")
            //sb.append("win.durationUs " + win.durationUs).append("\n")
            //sb.append("win.firstPeriodIndex " + win.firstPeriodIndex).append("\n")
            //sb.append("win.lastPeriodIndex " + win.lastPeriodIndex).append("\n")
            sb.append("win.positionInFirstPeriodMs " + win.positionInFirstPeriodMs).append("\n")
            //sb.append("win.positionInFirstPeriodUs " + win.positionInFirstPeriodUs).append("\n")
            sb.append("win.presentationStartTimeMs " + win.presentationStartTimeMs).append("\n")
            sb.append("win.windowStartTimeMs " + win.windowStartTimeMs).append("\n")
            //sb.append("win.getDefaultPositionUs " + win.getDefaultPositionUs()).append("\n")
            sb.append("win.getDurationUs " + win.getDurationUs()).append("\n")
            //sb.append("win.getPositionInFirstPeriodUs " + win.getPositionInFirstPeriodUs()).append("\n")

            val period = Timeline.Period()
            tl.getPeriod(0, period)
            sb.append("period.durationMs " + period.durationMs).append("\n")
            //sb.append("period.durationUs " + period.durationUs).append("\n")
            sb.append("period.positionInWindowMs " + period.positionInWindowMs).append("\n")
            //sb.append("period.positionInWindowUs " + period.positionInWindowUs).append("\n")
            //sb.append("period.getDurationUs " + period.getDurationUs()).append("\n")
            //sb.append("period.windowIndex " + period.windowIndex).append("\n")

            Log.v(TAG, sb.toString())
        }}
    }
}