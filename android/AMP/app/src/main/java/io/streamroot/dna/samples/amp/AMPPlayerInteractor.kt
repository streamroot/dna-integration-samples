package io.streamroot.dna.samples.amp

import com.akamai.amp.media.VideoPlayerView
import io.streamroot.dna.core.PlayerInteractor
import io.streamroot.dna.core.TimeRange

class AMPPlayerInteractor : PlayerInteractor {
    private var player: VideoPlayerView? = null
    fun setPlayer(player: VideoPlayerView) { this.player = player }

    override fun playbackTime(): Long {
        return 0L
    }

    override fun loadedTimeRanges(): List<TimeRange> {
        return listOf()
    }
}