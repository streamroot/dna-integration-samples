package io.streamroot.dna.samples.playkit;

import android.os.Handler;

import androidx.annotation.Nullable;

import com.kaltura.android.exoplayer2.upstream.BandwidthMeter;
import com.kaltura.android.exoplayer2.upstream.TransferListener;
import io.streamroot.dna.core.BandwidthListener;

import java.util.concurrent.atomic.AtomicLong;

final class PlayKitCustomBandwidthMeter implements BandwidthMeter, BandwidthListener {
    private AtomicLong estimatedBandwidth = new AtomicLong(0L);

    @Override @Nullable
    public TransferListener getTransferListener() {
        return null;
    }

    @Override
    public void addEventListener(Handler eventHandler, BandwidthMeter.EventListener eventListener) {}

    @Override
    public void removeEventListener(BandwidthMeter.EventListener eventListener) {}


    @Override
    public long getBitrateEstimate() {
        return estimatedBandwidth.get();
    }

    @Override
    public void onBandwidthChange(long estimatedBandwidth) {
        this.estimatedBandwidth.set(estimatedBandwidth);
    }
}
