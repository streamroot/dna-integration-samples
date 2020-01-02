package io.streamroot.dna.samples.amp.impl

import com.akamai.amp.media.VideoPlayerContainer
import com.akamai.exoplayer2.ExoPlaybackException
import com.akamai.exoplayer2.PlaybackParameters
import com.akamai.exoplayer2.Player
import com.akamai.exoplayer2.Player.STATE_BUFFERING
import com.akamai.exoplayer2.Player.STATE_ENDED
import com.akamai.exoplayer2.Player.STATE_IDLE
import com.akamai.exoplayer2.Player.STATE_READY
import com.akamai.exoplayer2.Timeline
import com.akamai.exoplayer2.source.TrackGroupArray
import com.akamai.exoplayer2.trackselection.TrackSelectionArray
import io.streamroot.dna.core.PlaybackState
import io.streamroot.dna.core.QosModule

class AMPQosModule(videoViewContainer: VideoPlayerContainer) : QosModule(), Player.EventListener {

    init {
        videoViewContainer.setExoEventsListener(this)
    }

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
            STATE_IDLE -> {
                playbackStateChange(PlaybackState.IDLE)
            }
            STATE_BUFFERING -> {
                playbackStateChange(PlaybackState.BUFFERING)
            }
            STATE_READY -> {
                playbackStateChange(if (playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSING)
            }
            STATE_ENDED -> {
                playbackStateChange(PlaybackState.ENDED)
            }
        }
    }
}
