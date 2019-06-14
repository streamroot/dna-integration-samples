package io.streamroot.dna.samples.playkit.ovpstarter;

import android.os.Looper;

import com.kaltura.playkit.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.TimeRange;

final class PlayKitInteractor implements PlayerInteractor {
    private Player mPlayer;

    PlayKitInteractor(Player player) {
        this.mPlayer = player;
    }

    @Override
    public double bufferTarget() {
        Double bufferTarget = mPlayer.getBufferTarget();

        if (bufferTarget != null) {
            return bufferTarget;
        }

        return 0.0;
    }

    @NotNull
    @Override
    public List<TimeRange> loadedTimeRanges() {
        long currentPosition = mPlayer.getCurrentPosition();
        long bufferedPosition = mPlayer.getBufferedPosition();
        return Collections.singletonList(new TimeRange(currentPosition, bufferedPosition));
    }

    @Nullable
    @Override
    public Looper looper() {
        return Looper.getMainLooper();
    }

    @Override
    public long playbackTime() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public void setBufferTarget(double target) {
        mPlayer.setBufferTarget(target);
    }
}
