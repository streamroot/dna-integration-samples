package io.streamroot.dna.samples.exoplayer;

import android.os.Handler;

import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.TransferListener;

import org.jetbrains.annotations.Nullable;

import io.streamroot.dna.core.BandwidthListener;
import java.util.concurrent.atomic.AtomicLong;

public final class ExoPlayerBandwidthMeter implements BandwidthMeter, BandwidthListener {
    private final AtomicLong estimatedBandwidth = new AtomicLong(0L);

    @Nullable
    @Override
    public TransferListener getTransferListener() { return null; }

    @Override
    public void addEventListener(Handler eventHandler, EventListener eventListener) {}

    @Override
    public void removeEventListener(EventListener eventListener) {}

    @Override
    public long getBitrateEstimate() {
        return this.estimatedBandwidth.get();
    }

    @Override
    public void onBandwidthChange(long estimatedBandwidth) {
        this.estimatedBandwidth.set(estimatedBandwidth);
    }
}