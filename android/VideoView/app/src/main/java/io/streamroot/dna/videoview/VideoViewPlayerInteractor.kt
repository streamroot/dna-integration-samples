package io.streamroot.dna.videoview

import android.widget.VideoView
import io.streamroot.dna.core.PlayerInteractor
import io.streamroot.dna.core.TimeRange
import java.util.Collections

class VideoViewPlayerInteractor(
        private val mVideoView: VideoView
) : PlayerInteractor{
    override fun loadedTimeRanges(): MutableList<TimeRange> {
        return Collections.emptyList()
    }

    override fun playbackTime(): Long {
        return mVideoView.currentPosition.toLong()
    }
}