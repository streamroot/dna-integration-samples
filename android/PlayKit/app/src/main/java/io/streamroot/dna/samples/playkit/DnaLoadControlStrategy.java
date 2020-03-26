package io.streamroot.dna.samples.playkit;

import com.kaltura.android.exoplayer2.LoadControl;
import com.kaltura.android.exoplayer2.upstream.BandwidthMeter;
import com.kaltura.playkit.player.ExoPlayerWrapper;

public class DnaLoadControlStrategy implements ExoPlayerWrapper.LoadControlStrategy {
    LoadControl mLoadControl;
    BandwidthMeter mBandwidthMeter;

    public DnaLoadControlStrategy(LoadControl loadControl, BandwidthMeter bandwidthMeter) {
        mLoadControl = loadControl;
        mBandwidthMeter = bandwidthMeter;
    }

    @Override
    public LoadControl getCustomLoadControl() {
        return mLoadControl;
    }

    @Override
    public BandwidthMeter getCustomBandwidthMeter() {
        return mBandwidthMeter;
    }
}
