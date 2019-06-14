package io.streamroot.dna.samples.playkit.ovpstarter;

import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;

import io.streamroot.dna.core.PlaybackState;
import io.streamroot.dna.core.QosModule;

final class PlayKitQoSModule extends QosModule {
    PlayKitQoSModule(Player player) {
        super();

        player.addListener(this, PlayerEvent.stateChanged, event -> {
            switch (event.newState) {
                case IDLE:
                    playbackStateChange(PlaybackState.IDLE);
                case BUFFERING:
                    playbackStateChange(PlaybackState.BUFFERING);
                default:
                    break;
            }
        });

        player.addListener(this, PlayerEvent.pause, pkEvent -> playbackStateChange(PlaybackState.PAUSING));
        player.addListener(this, PlayerEvent.playing, pkEvent -> playbackStateChange(PlaybackState.PLAYING));
        player.addListener(this, PlayerEvent.seeked, pkEvent -> playbackStateChange(PlaybackState.SEEKING));
        player.addListener(this, PlayerEvent.ended, pkEvent -> playbackStateChange(PlaybackState.ENDED));
        player.addListener(this, PlayerEvent.error, pkEvent -> playbackErrorOccurred());
        player.addListener(this, PlayerEvent.videoTrackChanged, pkEvent -> trackSwitchOccurred());
        player.addListener(this, PlayerEvent.videoFramesDropped, pkEvent -> droppedFrameOccurred((int) pkEvent.droppedVideoFrames, pkEvent.droppedVideoFramesPeriod));
    }
}
