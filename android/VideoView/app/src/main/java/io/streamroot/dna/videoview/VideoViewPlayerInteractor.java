package io.streamroot.dna.videoview;

import android.widget.VideoView;

import org.jetbrains.annotations.NotNull;

import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.TimeRange;

import java.util.Collections;
import java.util.List;

final class VideoViewPlayerInteractor implements PlayerInteractor {
    private VideoView videoView;

    VideoViewPlayerInteractor(VideoView mVideoView) {
        videoView = mVideoView;
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
}
