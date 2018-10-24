package io.streamroot.dna.exoplayer

import com.google.android.exoplayer2.upstream.BandwidthMeter
import io.streamroot.dna.core.BandwidthListener
import java.util.concurrent.atomic.AtomicLong

class ExoplayerBandwidthMeter : BandwidthMeter, BandwidthListener {
    private val estimatedBandwidth = AtomicLong(0L)

    override fun getBitrateEstimate(): Long {
        return estimatedBandwidth.get()
    }

    override fun onBandwidthChange(estimatedBandwidth: Long) {
        this.estimatedBandwidth.set(estimatedBandwidth)
    }
}