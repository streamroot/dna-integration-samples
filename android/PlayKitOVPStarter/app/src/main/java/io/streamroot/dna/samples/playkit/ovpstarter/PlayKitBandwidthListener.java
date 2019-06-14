package io.streamroot.dna.samples.playkit.ovpstarter;

import com.kaltura.playkit.Player;

import io.streamroot.dna.core.BandwidthListener;

final class PlayKitBandwidthListener implements BandwidthListener {
    private Player mPlayer;

    PlayKitBandwidthListener(Player player) {
        this.mPlayer = player;
    }

    @Override
    public void onBandwidthChange(long estimatedBandwidth) {
        this.mPlayer.setEstimatedBandwidth(estimatedBandwidth);
    }
}