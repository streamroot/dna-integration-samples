package io.streamroot.dna.samples.videoview;

import android.os.Looper;
import android.widget.VideoView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.TimeRange;

final class VideoViewPlayerInteractor implements PlayerInteractor {
    private VideoView videoView;

    VideoViewPlayerInteractor(VideoView mVideoView) {
        videoView = mVideoView;
    }

    @Nullable
    @Override
    public Looper looper() {
        return null;
    }

    @Override
    public long playbackTime() {
        return videoView.getCurrentPosition();
    }

    @NotNull
    @Override
    public List<TimeRange> loadedTimeRanges() {
        return Collections.emptyList();
    }

    @Override
    public double bufferTarget() {
        return 0;
    }

    @Override
    public void setBufferTarget(double v) {
    }
}
