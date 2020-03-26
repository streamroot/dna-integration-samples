package io.streamroot.dna.samples.amp

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.streamroot.dna.core.DnaClient
import io.streamroot.dna.utils.stats.StreamStatsManager
import com.akamai.amp.media.VideoPlayerContainer
import com.akamai.amp.media.VideoPlayerView
import android.view.WindowManager
import com.akamai.amp.config.AMPConfig
import com.akamai.amp.media.elements.MediaResource
import kotlinx.android.synthetic.main.content_player.*
import com.akamai.amp.media.errors.ErrorType
import com.akamai.exoplayer2.DefaultLoadControl
import com.akamai.exoplayer2.util.AMPPreSettings

data class DelayedSRConfig(
        val ampPlayerInteractor: AMPPlayerInteractor,
        val ampQosModule: AMPQosModule,
        val ampBandwidthMeter: AMPBandwidthMeter = AMPBandwidthMeter()
)

class PlayerActivity : AppCompatActivity(), VideoPlayerContainer.VideoPlayerContainerCallback {
    private val LOG_TAG = "AMPv6"

    private var videoPlayerView: VideoPlayerView? = null

    private var dnaClient: DnaClient? = null
    private var dnaDelayedSRConfig: DelayedSRConfig? = null

    private var streamStatsManager: StreamStatsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        videoPlayerContainer.addVideoPlayerContainerCallback(this)

        val url = {
            intent.extras?.getString("streamUrl")?.let {
                if (it.isEmpty()) AMPSRConfig.DEFAULT_VIDEO_URL else it
            } ?: AMPSRConfig.DEFAULT_VIDEO_URL
        }()

        if (AMPSRConfig.DNA_ENABLED) {
            dnaClient = initStreamrootP1Start(url)
            val manifestUri = dnaClient?.manifestUrl ?: Uri.parse(url)
            prepareResourceWithUrl(manifestUri.toString())
        } else {
            streamrootDnaStatsView.visibility = View.GONE
            prepareResourceWithUrl(url)
        }
    }

    //val customHThread by lazy {  }
    val customHandler by lazy { Handler(
            HandlerThread("testdsfdsf").apply { start() }.looper
            //mainLooper
    ) }

    private fun initStreamrootP1Start(url: String) : DnaClient? {
        // Just the default value from class DefaultLoadControl
        //val loadControl = DefaultLoadControl()
        //AMPPreSettings.getPreSettingsInstance().defaultLoadControl = DefaultLoadControl()
        val loadControl = videoPlayerContainer.setBufferDimensions(5000, 30000, 2500,5000)
        //videoPlayerContainer.defaultLoadControl = loadControl
        val dnaDelayedSRConfig = DelayedSRConfig(AMPPlayerInteractor(loadControl, looper = mainLooper), AMPQosModule(videoPlayerContainer))
        this.dnaDelayedSRConfig = dnaDelayedSRConfig
        var mSdk: DnaClient? = null
        try {
            val base = DnaClient.newBuilder()
                    .context(applicationContext)
                    .playerInteractor(dnaDelayedSRConfig.ampPlayerInteractor)

            mSdk = AMPSRConfig.configureStreamroot(base).qosModule(dnaDelayedSRConfig.ampQosModule)
                    .bandwidthListener(dnaDelayedSRConfig.ampBandwidthMeter)
                    .start(Uri.parse(url))

            streamStatsManager = StreamStatsManager.newStatsManager(mSdk, streamrootDnaStatsView)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
        }

        return mSdk
    }

    private fun initStreamrootP2End() {
        dnaDelayedSRConfig?.let { delayedConfig -> videoPlayerView?.let { vp ->
            delayedConfig.ampBandwidthMeter.setPlayer(vp)
            delayedConfig.ampPlayerInteractor.setPlayer(vp)
        }}
        dnaDelayedSRConfig = null
    }


    private fun stopStreamroot() {
        dnaClient?.close()
        dnaClient = null

        streamStatsManager?.close()
        streamStatsManager = null
    }



    private fun prepareResourceWithUrl(url: String) {
        //customHandler.post {
        //object : Thread() {
        //    override fun run() {
                videoPlayerContainer.prepareResource(url)
        //    }
        //}.start()

        //}
    }

    override fun onResourceReady(mediaResource: MediaResource?) {
        //customHandler.post {

        runOnUiThread {
            videoPlayerView = videoPlayerContainer.videoPlayer
            if (dnaClient != null) { initStreamrootP2End() }

            initPlayer()

            customHandler.post {
                videoPlayerView?.play(mediaResource)
            }
        }


        //}
    }

    private fun initPlayer() {
        videoPlayerView?.progressBarControl = progressBar
        videoPlayerView?.setLicense(AMPSRConfig.AMP_LICENSE(this))
        videoPlayerView?.setLogEnabled(true)
        videoPlayerView?.isFullScreen = true
        videoPlayerView?.fullScreenMode = VideoPlayerView.FULLSCREEN_MODE_KEEP_ASPECT_RATIO
    }

    override fun onVideoPlayerCreated() {
        Log.i(LOG_TAG, "onVideoPlayerCreated()")
    }

    override fun onResourceError(errorType: ErrorType, exception: Exception) {
        Log.e(LOG_TAG, "onResourceError()")
    }

    override fun onResume() {
        Log.d(LOG_TAG, "onResume")
        videoPlayerView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        Log.d(LOG_TAG, "onPause")
        videoPlayerView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy")
        videoPlayerView?.onDestroy()
        stopStreamroot()
        super.onDestroy()
    }
}