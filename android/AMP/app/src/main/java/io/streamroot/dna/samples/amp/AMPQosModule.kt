package io.streamroot.dna.samples.amp

import com.akamai.amp.ads.AlternativeTimelineListener
import com.akamai.amp.media.IPlayerEventsListener
import com.akamai.amp.media.VideoPlayerView
import com.akamai.exoplayer2.ExoPlaybackException
import com.akamai.exoplayer2.PlaybackParameters
import com.akamai.exoplayer2.Player
import com.akamai.exoplayer2.Timeline
import com.akamai.exoplayer2.source.TrackGroupArray
import com.akamai.exoplayer2.trackselection.TrackSelectionArray
import com.akamai.exoplayer2.video.VideoListener
import io.streamroot.dna.core.PlaybackState
import io.streamroot.dna.core.QosModule

class AMPQosModule : QosModule(), /*Player.EventListener*/ /*, AlternativeTimelineListener, */IPlayerEventsListener/*, VideoListener*/ {

    private var player: VideoPlayerView? = null
    fun setPlayer(player: VideoPlayerView) {
        this.player = player
        //player.ampBasePlayer.addListener(this)
        //player.setTimelineListener(this)
        player.addEventsListener(this)
        //player.ampBasePlayer.addVideoListener(this)
    }


    override fun onPlayerEvent(type: Int): Boolean {
        when (type) {
            IPlayerEventsListener.PLAYER_EVENT_TYPE_PAUSE_REQUESTED
                -> playbackStateChange(PlaybackState.PAUSING)
            IPlayerEventsListener.PLAYER_EVENT_TYPE_START_REBUFFERING
                -> playbackStateChange(PlaybackState.BUFFERING)
            IPlayerEventsListener.PLAYER_EVENT_TYPE_END_REBUFFERING,
            IPlayerEventsListener.PLAYER_EVENT_TYPE_RESUME_REQUESTED,
            IPlayerEventsListener.PLAYER_EVENT_TYPE_START_PLAYING
                -> playbackStateChange(PlaybackState.PLAYING)
            IPlayerEventsListener.PLAYER_EVENT_TYPE_STOP_PLAYING,
            IPlayerEventsListener.PLAYER_EVENT_TYPE_FINISHED
                -> playbackStateChange(PlaybackState.IDLE)
            IPlayerEventsListener.PLAYER_EXTENDED_EVENT_SEEKING_STARTED
                -> playbackStateChange(PlaybackState.SEEKING)
        }
        return true
    }

    override fun onPlayerExtendedEvent(type: Int, p1: Int, p2: Int): Boolean {
        return true
    }

    /*
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

    override fun onSeekProcessed() {
        playbackStateChange(PlaybackState.SEEKING)
    }

    override fun onTracksChanged(
            trackGroups: TrackGroupArray?,
            trackSelections: TrackSelectionArray?
    ) {
        trackSwitchOccurred()
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        playbackErrorOccurred()
    }

    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onPositionDiscontinuity(reason: Int) {}

    override fun onRepeatModeChanged(repeatMode: Int) {}

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                playbackStateChange(PlaybackState.IDLE)
            }
            Player.STATE_BUFFERING -> {
                playbackStateChange(PlaybackState.BUFFERING)
            }
            Player.STATE_READY -> {
                playbackStateChange(if (playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSING)
            }
            Player.STATE_ENDED -> {
                playbackStateChange(PlaybackState.ENDED)
            }
        }
    }*/
}
