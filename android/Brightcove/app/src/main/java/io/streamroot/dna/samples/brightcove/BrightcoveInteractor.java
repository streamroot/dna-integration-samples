package io.streamroot.dna.samples.brightcove;

import android.os.Looper;

import com.brightcove.player.display.ExoPlayerVideoDisplayComponent;
import com.brightcove.player.display.VideoDisplayComponent;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.TimeRange;

final class BrightcoveInteractor implements PlayerInteractor {
    private ExoPlayer mPlayer;
    private BrightcoveExoPlayerVideoView brightcoveVideoView;
    BrightcoveInteractor(BrightcoveExoPlayerVideoView brightcoveVideoView) {
        this.brightcoveVideoView = brightcoveVideoView;
        setExoWhenAvailable(brightcoveVideoView);
    }

    private void setExoWhenAvailable(BrightcoveExoPlayerVideoView brightcoveVideoView) {
        this.brightcoveVideoView.addListener(EventType.DID_SET_VIDEO,new EventListener()  {
            @Override
            public void processEvent(Event event) {
                VideoDisplayComponent videoDisplayComponent = brightcoveVideoView.getVideoDisplay();
                if (videoDisplayComponent instanceof ExoPlayerVideoDisplayComponent) {
                    // Get ExoPlayer
                    ExoPlayer exoPlayer = ((ExoPlayerVideoDisplayComponent) videoDisplayComponent).getExoPlayer();
                    if (exoPlayer instanceof SimpleExoPlayer) {
                        // Get SimpleExoPlayer
                        mPlayer = (SimpleExoPlayer) exoPlayer;
                    }
                }
            }
        });
    }

    @NotNull
    @Override
    public List<TimeRange> loadedTimeRanges() {
        if (mPlayer == null) {  return Collections.emptyList(); }
        long shift = getCurrentWindowShift();
        long rangeDurationMs = mPlayer.getBufferedPosition() - mPlayer.getCurrentPosition();
        return (rangeDurationMs > 0) ?
                Collections.singletonList(new TimeRange(shift +
                        mPlayer.getCurrentPosition(), rangeDurationMs))
                : Collections.emptyList();
    }

    @Nullable
    @Override
    public Looper looper() {
        return (mPlayer == null) ? null : mPlayer.getPlaybackLooper();
    }

    @Override
    public long playbackTime() {
        return (mPlayer == null)
                ? brightcoveVideoView.getCurrentPosition()
                : getCurrentWindowShift() + mPlayer.getCurrentPosition();
    }

    private long getCurrentWindowShift() {
        Timeline current = mPlayer.getCurrentTimeline();
        Timeline.Window timeLimeWindow = new Timeline.Window();
        long shift = 0;
        if (mPlayer.getCurrentWindowIndex() < current.getWindowCount()) {
            mPlayer.getCurrentTimeline().getWindow(mPlayer.getCurrentWindowIndex(), timeLimeWindow);
            shift = timeLimeWindow.getPositionInFirstPeriodMs();
        }
        return shift;
    }

    @Override
    public double bufferTarget() { return 0; }

    @Override
    public void setBufferTarget(double target) {}
}
