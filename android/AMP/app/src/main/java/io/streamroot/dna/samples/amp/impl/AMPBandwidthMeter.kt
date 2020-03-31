package io.streamroot.dna.samples.amp.impl

import android.os.Handler
import com.akamai.amp.media.VideoPlayerView
import com.akamai.exoplayer2.upstream.BandwidthMeter
import com.akamai.exoplayer2.upstream.TransferListener
import io.streamroot.dna.core.BandwidthListener
import java.util.concurrent.atomic.AtomicLong

class AMPBandwidthMeter : BandwidthMeter, BandwidthListener {

    private val estimatedBandwidth = AtomicLong(0L)

    fun setPlayer(player: VideoPlayerView) { player.setBandwidthMeter(this) }

    override fun getBitrateEstimate() : Long {
        return estimatedBandwidth.get()
    }

    override fun onBandwidthChange(estimatedBandwidth: Long) {
        this.estimatedBandwidth.set(estimatedBandwidth)
    }

    override fun getTransferListener(): TransferListener? = null
    override fun addEventListener(eventHandler: Handler?, eventListener: BandwidthMeter.EventListener?) {}
    override fun removeEventListener(eventListener: BandwidthMeter.EventListener?) {}
}