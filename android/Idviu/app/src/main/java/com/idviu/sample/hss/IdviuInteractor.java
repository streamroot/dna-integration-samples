package com.idviu.sample.hss;

import android.os.Looper;

import com.labgency.hss.views.HSSPlayerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.TimeRange;

public class IdviuInteractor implements PlayerInteractor {
    private HSSPlayerView mPlayerView = null;

    public IdviuInteractor(HSSPlayerView mPlayerView) {
        this.mPlayerView = mPlayerView;
    }

    @Override
    public double bufferTarget() {
        return this.mPlayerView.getPlayer().getMaxBufferLength();
    }

    @NotNull
    @Override
    public List<TimeRange> loadedTimeRanges() {
        long currentPosition = this.mPlayerView.getPlayer().getLivePosition();
        long bufferedPosition = currentPosition + this.mPlayerView.getPlayer().getBufferLength();
        return Collections.singletonList(new TimeRange(currentPosition, bufferedPosition));
    }

    @Nullable
    @Override
    public Looper looper() {
        return Looper.getMainLooper();
    }

    @Override
    public long playbackTime() {
        return this.mPlayerView.getPlayer().getPosition();
    }

    @Override
    public void setBufferTarget(double v) { }
}
