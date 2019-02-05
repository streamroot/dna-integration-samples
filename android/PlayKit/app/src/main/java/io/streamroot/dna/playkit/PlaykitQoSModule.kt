package io.streamroot.dna.playkit

import com.kaltura.playkit.PKEvent
import com.kaltura.playkit.Player
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import io.streamroot.dna.core.PlaybackState
import io.streamroot.dna.core.QosModule

class PlayKitQoSModule(
        player: Player
) : QosModule() {
    private val mPlayer = player
    private var mLastVideoBitrate: Long? = null
    private var mLastAudioBitrate: Long? = null
    private var currentPlayerState: PlayerState? = null

    init {
        // Register useful player events
        mPlayer.addEventListener((PKEvent.Listener {
            val playbackInfo = (it as PlayerEvent.PlaybackInfoUpdated).playbackInfo

            if (mLastVideoBitrate != playbackInfo.videoBitrate
                    || mLastAudioBitrate != playbackInfo.audioBitrate) {
                trackSwitchOccurred()
            }

            mLastVideoBitrate = playbackInfo.videoBitrate
            mLastAudioBitrate = playbackInfo.audioBitrate
        }), PlayerEvent.Type.PLAYBACK_INFO_UPDATED)

        mPlayer.addStateChangeListener {
            val eventDetails = it as PlayerEvent.StateChanged

            when (eventDetails.newState) {
                PlayerState.IDLE -> {
                    playbackStateChange(PlaybackState.IDLE)
                }
                PlayerState.BUFFERING -> {
                    playbackStateChange(PlaybackState.BUFFERING)
                }
                PlayerState.READY -> {
                    when (mPlayer.isPlaying) {
                        true -> PlaybackState.PLAYING
                        false -> PlaybackState.PAUSING
                    }
                }
            }

            currentPlayerState = it.newState
        }
    }
}