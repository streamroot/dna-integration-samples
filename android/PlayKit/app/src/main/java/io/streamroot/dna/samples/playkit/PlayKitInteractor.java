package io.streamroot.dna.samples.playkit;

import android.os.Looper;
import com.kaltura.android.exoplayer2.LoadControl;
import com.kaltura.playkit.Player;
import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.TimeRange;

import java.util.Collections;
import java.util.List;

final class PlayKitInteractor implements PlayerInteractor {
    private Player mPlayer;
    private DynamicBufferLoadControl mLoadControlWrapper;
    PlayKitInteractor(Player player, LoadControl loadControl) {
        mPlayer = player;
        mLoadControlWrapper = new DynamicBufferLoadControl(loadControl);
    }

    @Override
    public double bufferTarget() {
        return mLoadControlWrapper.bufferTarget();
    }

    @Override
    public void setBufferTarget(double target) {
        mLoadControlWrapper.setBufferTarget(target);
    }

    @Override
    public List<TimeRange> loadedTimeRanges() {
        long currentPosition = mPlayer.getCurrentPosition();
        long bufferedPosition = mPlayer.getBufferedPosition();
        return Collections.singletonList(new TimeRange(currentPosition, bufferedPosition));
    }

    @Override
    public Looper looper() {
        return Looper.getMainLooper();
    }

    @Override
    public long playbackTime() {
        return mPlayer.getCurrentPosition();
    }
}
