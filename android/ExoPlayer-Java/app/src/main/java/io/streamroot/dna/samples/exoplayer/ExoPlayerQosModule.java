package io.streamroot.dna.samples.exoplayer;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import io.streamroot.dna.core.PlaybackState;
import io.streamroot.dna.core.QosModule;

public final class ExoPlayerQosModule extends QosModule implements EventListener {
    public ExoPlayerQosModule(ExoPlayer exoPlayer) {
        exoPlayer.addListener(this);
    }

    @Override
    public void onSeekProcessed() {
        playbackStateChange(PlaybackState.SEEKING);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        trackSwitchOccurred();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        playbackErrorOccurred();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch(playbackState) {
            case Player.STATE_IDLE:
                playbackStateChange(PlaybackState.IDLE);
                break;
            case Player.STATE_BUFFERING:
                playbackStateChange(PlaybackState.BUFFERING);
                break;
            case Player.STATE_READY:
                playbackStateChange(playWhenReady ? PlaybackState.PLAYING : PlaybackState.PAUSING);
                break;
            case Player.STATE_ENDED:
                playbackStateChange(PlaybackState.ENDED);
                break;
        }
    }

    @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
    @Override public void onLoadingChanged(boolean isLoading) {}
    @Override public void onPositionDiscontinuity(int reason) {}
    @Override public void onRepeatModeChanged(int repeatMode) {}
    @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}
    @Override public void onTimelineChanged(Timeline timeline, int reason) {}
}
