package io.streamroot.dna.samples.brightcove;

import com.brightcove.player.display.ExoPlayerVideoDisplayComponent;
import com.google.android.exoplayer2.Player;

import io.streamroot.dna.core.BandwidthListener;

final class BrightcoveBandwidthListener implements BandwidthListener {
    private ExoPlayerVideoDisplayComponent displayComponent;

    BrightcoveBandwidthListener(ExoPlayerVideoDisplayComponent displayComponent) {
        this.displayComponent = displayComponent;
    }

    @Override
    public void onBandwidthChange(long estimatedBandwidth) {
        displayComponent.setPeakBitrate((int) estimatedBandwidth);
    }
}