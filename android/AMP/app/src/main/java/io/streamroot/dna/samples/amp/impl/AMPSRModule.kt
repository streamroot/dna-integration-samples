package io.streamroot.dna.samples.amp.impl

import android.content.Context
import android.net.Uri
import com.akamai.amp.media.VideoPlayerContainer
import com.akamai.amp.media.VideoPlayerView
import com.akamai.exoplayer2.DefaultLoadControl
import io.streamroot.dna.core.DnaClient
import io.streamroot.dna.core.InformationCallback
import io.streamroot.dna.samples.amp.AMPSRConfig
import io.streamroot.dna.utils.stats.StreamStatsManager

/**
 * Created by Boris Borgobello on 2020-03-26.
 */

class AMPSRModule(
        context: Context,
        playerC: VideoPlayerContainer,
        loadControl: DefaultLoadControl,
        url: String
) {
    private data class DelayedSRConfig(
            val ampPlayerInteractor: AMPPlayerInteractor,
            val ampQosModule: AMPQosModule,
            val ampBandwidthMeter: AMPBandwidthMeter = AMPBandwidthMeter()
    )

    private var streamStatsManager: StreamStatsManager? = null

    private val dnaDelayedSRConfig = DelayedSRConfig(AMPPlayerInteractor(loadControl), AMPQosModule(playerC))
    private var dnaClient = runCatching { DnaClient.newBuilder()
            .context(context)
            .playerInteractor(dnaDelayedSRConfig.ampPlayerInteractor)
            .also { AMPSRConfig.configureStreamroot(it) }
            .qosModule(dnaDelayedSRConfig.ampQosModule)
            .bandwidthListener(dnaDelayedSRConfig.ampBandwidthMeter)
            .start(Uri.parse(url))
    }.getOrNull()

    fun finalUrl() = dnaClient?.manifestUrl

    fun enableStats(callback: InformationCallback)
            = dnaClient?.let { streamStatsManager = StreamStatsManager.newStatsManager(it, callback) }

    fun setPlayerView(videoPlayerView: VideoPlayerView) {
        dnaDelayedSRConfig.apply {
            ampBandwidthMeter.setPlayer(videoPlayerView)
            ampPlayerInteractor.setPlayer(videoPlayerView)
        }
    }

    fun terminate() {
        dnaClient?.close()
        dnaClient = null

        streamStatsManager?.close()
        streamStatsManager = null
    }
}