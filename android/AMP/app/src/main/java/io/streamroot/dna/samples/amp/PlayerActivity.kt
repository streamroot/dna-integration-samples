package io.streamroot.dna.samples.amp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.streamroot.dna.core.DnaClient
import io.streamroot.dna.utils.stats.StreamStatsManager
import com.akamai.amp.media.VideoPlayerContainer
import com.akamai.amp.media.VideoPlayerView
import android.view.WindowManager
import com.akamai.amp.media.elements.MediaResource
import kotlinx.android.synthetic.main.content_player.*
import com.akamai.amp.media.errors.ErrorType
import com.akamai.exoplayer2.DefaultLoadControl
import io.streamroot.dna.core.InformationCallback

class SRModule(
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

class PlayerActivity : AppCompatActivity(), VideoPlayerContainer.VideoPlayerContainerCallback {

    private var srModule: SRModule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        videoPlayerContainer.addVideoPlayerContainerCallback(this)

        val url = intent.extras?.getString("streamUrl")?.takeUnless { it.isEmpty() }
                ?: AMPSRConfig.DEFAULT_VIDEO_URL

        start(AMPSRConfig.DNA_ENABLED, url)
    }

    fun start(srEnabled: Boolean, url: String) = with (if (srEnabled) {
        val loadControl = videoPlayerContainer.setBufferDimensions(5000, 30000, 2500,5000)
        srModule = SRModule(this, videoPlayerContainer, loadControl, url)
        srModule?.enableStats(streamrootDnaStatsView)
        srModule?.finalUrl()?.toString() ?: url
    } else {
        streamrootDnaStatsView.visibility = View.GONE
        url
    }) { videoPlayerContainer.prepareResource(this) }

    fun videoPlayer() = videoPlayerContainer.videoPlayer

    override fun onResourceReady(mediaResource: MediaResource?) {
        videoPlayer()?.let {
            srModule?.setPlayerView(it)
            initPlayer(it)
            it.play(mediaResource)
        }
    }

    private fun initPlayer(player: VideoPlayerView) = player.apply {
        progressBarControl = progressBar
        setLicense(AMPSRConfig.AMP_LICENSE(applicationContext))
        setLogEnabled(true)
        isFullScreen = true
        fullScreenMode = VideoPlayerView.FULLSCREEN_MODE_KEEP_ASPECT_RATIO
    }

    override fun onVideoPlayerCreated() {}

    override fun onResourceError(errorType: ErrorType, exception: Exception) {}

    override fun onResume() {
        videoPlayer().onResume()
        super.onResume()
    }

    override fun onPause() {
        videoPlayer().onPause()
        super.onPause()
    }

    override fun onDestroy() {
        videoPlayer().onDestroy()
        srModule?.terminate()
        super.onDestroy()
    }
}