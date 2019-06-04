package io.streamroot.dna.playkit;

import com.kaltura.playkit.Player;
import io.streamroot.dna.core.BandwidthListener;

public final class PlayKitBandwidthListener implements BandwidthListener {
    private Player mPlayer;

    public PlayKitBandwidthListener(Player player) {
        this.mPlayer = player;
    }

    @Override
    public void onBandwidthChange(long estimatedBandwidth) {
        this.mPlayer.setEstimatedBandwidth(estimatedBandwidth);
    }
}
