package io.streamroot.dna.samples.amp

import android.util.Log
import com.akamai.amp.media.VideoPlayerView
import com.akamai.exoplayer2.Timeline
import java.lang.StringBuilder

/**
 * Created by Boris Borgobello on 2020-03-26.
 */

fun VideoPlayerView.logFull(refTimestamp: Long? = null) {
    ampBasePlayer.let { ampPlayer ->
        val sb = StringBuilder().append("\n")
        refTimestamp?.let { sb.append("Timestamp " + (System.currentTimeMillis() - it)).append("\n") }
        sb.append("playerView.playheadPosition " + this.playheadPosition).append("\n")
        //sb.append("playerView.bufferAvailable " + playerView.bufferAvailable).append("\n")
        //sb.append("playerView.bufferingPercentage " + playerView.bufferingPercentage).append("\n")
        //sb.append("playerView.bytesLoaded " + playerView.bytesLoaded).append("\n")
        //sb.append("playerView.currentBitrate " + playerView.currentBitrate).append("\n")
        sb.append("playerView.currentPositionPeriod " + this.currentPositionPeriod).append("\n")
        //sb.append("playerView.currentStreamPosition " + playerView.currentStreamPosition).append("\n")
        sb.append("playerView.currentStreamPositionMS " + this.currentStreamPositionMS).append("\n")
        //sb.append("playerView.currentTimelinePosition " + playerView.currentTimelinePosition).append("\n")
        sb.append("playerView.currentTimelinePositionMS " + this.currentTimelinePositionMS).append("\n")
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

        Log.v("POC", sb.toString())
    }
}