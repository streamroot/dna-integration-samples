package io.streamroot.dna.samples.exoplayer

import android.os.Handler
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.TransferListener
import io.streamroot.dna.core.BandwidthListener
import java.util.concurrent.atomic.AtomicLong

class ExoPlayerBandwidthMeter : BandwidthMeter, BandwidthListener {

    override fun getTransferListener(): TransferListener? = null

    override fun addEventListener(
        eventHandler: Handler?,
        eventListener: BandwidthMeter.EventListener?
    ) {
    }

    override fun removeEventListener(eventListener: BandwidthMeter.EventListener?) {}

    private val estimatedBandwidth = AtomicLong(0L)

    override fun getBitrateEstimate(): Long {
        return estimatedBandwidth.get()
    }

    override fun onBandwidthChange(estimatedBandwidth: Long) {
        this.estimatedBandwidth.set(estimatedBandwidth)
    }
}