package io.streamroot.dna.samples.playkit.ovpstarter;

import com.kaltura.playkit.PlaybackInfo;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;

import io.streamroot.dna.core.PlaybackState;
import io.streamroot.dna.core.QosModule;

final class PlayKitQoSModule extends QosModule {
    private Long mLastAudioBitrate = 0L;
    private Long mLastVideoBitrate = 0L;

    PlayKitQoSModule(Player player) {
        super();

        // Listen for track switches
        player.addListener(this, PlayerEvent.playbackInfoUpdated, event -> {
            PlaybackInfo playbackInfo = event.playbackInfo;

            if (mLastVideoBitrate != playbackInfo.getVideoBitrate() ||
                    mLastAudioBitrate != playbackInfo.getAudioBitrate()) {
                trackSwitchOccurred();
            }

            mLastAudioBitrate = playbackInfo.getAudioBitrate();
            mLastVideoBitrate = playbackInfo.getVideoBitrate();
        });

        // Listen for playback end
        player.addListener(this, PlayerEvent.ended, event -> playbackStateChange(PlaybackState.ENDED));

        // Listen for state changes
        player.addListener(this, PlayerEvent.stateChanged, event -> {
            switch (event.newState) {
                case IDLE:
                    playbackStateChange(PlaybackState.IDLE);
                    break;
                case BUFFERING:
                    playbackStateChange(PlaybackState.BUFFERING);
                    break;
                case READY:
                    if (player.isPlaying()) {
                        playbackStateChange(PlaybackState.PLAYING);
                    } else {
                        playbackStateChange(PlaybackState.PAUSING);
                    }
                    break;
                default:
                    break;
            }
        });

        // Listen for dropped frames
        player.addListener(this, PlayerEvent.videoFramesDropped, event -> droppedFrameOccurred((int) event.droppedVideoFrames, event.droppedVideoFramesPeriod));
    }
}