package com.idviu.sample.hss;

import android.media.MediaPlayer;

import com.labgency.hss.views.HSSPlayerView;

import io.streamroot.dna.core.PlaybackState;
import io.streamroot.dna.core.QosModule;

final class IdviuQoSModule extends QosModule implements MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener {
    private HSSPlayerView mPlayerView = null;

    IdviuQoSModule(HSSPlayerView mPlayerView) {
        super();

        this.mPlayerView = mPlayerView;

        this.mPlayerView.getPlayer().setOnCompletionListener(this);
        this.mPlayerView.getPlayer().setOnErrorListener(this);
        this.mPlayerView.getPlayer().setOnInfoListener(this);
        this.mPlayerView.getPlayer().setOnBufferingUpdateListener(this);
        this.mPlayerView.getPlayer().setOnInfoListener(this);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        playbackErrorOccurred();
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        playbackStateChange(PlaybackState.BUFFERING);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (mp.isPlaying()) {
            playbackStateChange(PlaybackState.PLAYING);
        } else {
            playbackStateChange(PlaybackState.PAUSING);
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playbackStateChange(PlaybackState.ENDED);
    }
}
