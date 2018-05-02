package io.streamroot.dna.videoview

import android.widget.VideoView
import io.streamroot.dna.core.StreamrootDNA
import java.util.Collections

class VideoViewPlayerInteractor(
        private val mVideoView: VideoView
) : StreamrootDNA.Interactor {
    override fun loadedTimeRanges(): MutableList<StreamrootDNA.Interactor.TimeRange> {
        return Collections.emptyList()
    }

    override fun playbackTime(): Long {
        return mVideoView.currentPosition.toLong()
    }
}