package io.streamroot.dna.samples.amp

import com.akamai.amp.media.VideoPlayerView
import io.streamroot.dna.core.QosModule

class AMPQosModule : QosModule() {
    private var player: VideoPlayerView? = null
    fun setPlayer(player: VideoPlayerView) { this.player = player }
}
