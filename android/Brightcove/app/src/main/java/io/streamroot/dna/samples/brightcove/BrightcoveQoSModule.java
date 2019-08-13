package io.streamroot.dna.samples.brightcove;

import com.brightcove.player.event.Default;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;

import io.streamroot.dna.core.PlaybackState;
import io.streamroot.dna.core.QosModule;

final class BrightcoveQoSModule extends QosModule {
    private BrightcoveExoPlayerVideoView brightcoveVideoView;
    BrightcoveQoSModule(BrightcoveExoPlayerVideoView brightcoveVideoView) {
        super();
        this.brightcoveVideoView = brightcoveVideoView;
        initListeners();
    }

    void initListeners() {
        {
            this.brightcoveVideoView.addListener(EventType.SOURCE_NOT_PLAYABLE, new EventListener() {
                @Default
                public void processEvent(Event event) {
                    playbackErrorOccurred();
                }
            });
            this.brightcoveVideoView.addListener(EventType.COMPLETED, new EventListener() {
                public void processEvent(Event event) {
                    playbackStateChange(PlaybackState.ENDED);
                }
            });
            this.brightcoveVideoView.addListener(EventType.SEEK_TO, new EventListener() {
                public void processEvent(Event event) {
                    playbackStateChange(PlaybackState.SEEKING);
                }
            });
            this.brightcoveVideoView.addListener(EventType.DID_PLAY, new EventListener() {
                public void processEvent(Event event) {
                    playbackStateChange(PlaybackState.PLAYING);
                }
            });
            this.brightcoveVideoView.addListener(EventType.DID_STOP, new EventListener() {
                public void processEvent(Event event) {
                    playbackStateChange(PlaybackState.ENDED);
                }
            });
            this.brightcoveVideoView.addListener(EventType.DID_PAUSE, new EventListener() {
                public void processEvent(Event event) {
                    playbackStateChange(PlaybackState.PAUSING);
                }
            });
            this.brightcoveVideoView.addListener(EventType.WILL_INTERRUPT_CONTENT, new EventListener() {
                public void processEvent(Event event) {
                    playbackErrorOccurred();
                    playbackStateChange(PlaybackState.ENDED);
                }
            });
        }
    }
}
