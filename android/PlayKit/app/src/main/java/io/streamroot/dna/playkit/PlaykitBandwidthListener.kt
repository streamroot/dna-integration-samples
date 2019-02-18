package io.streamroot.dna.playkit

import com.kaltura.playkit.Player
import io.streamroot.dna.core.BandwidthListener

class PlaykitBandwidthListener(
    player: Player
): BandwidthListener {
    private val mPlayer = player

    override fun onBandwidthChange(estimatedBandwidth: Long) {
        mPlayer.estimatedBandwidth = estimatedBandwidth
    }
}