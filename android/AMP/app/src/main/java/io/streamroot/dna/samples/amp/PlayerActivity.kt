package io.streamroot.dna.samples.amp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.akamai.amp.media.VideoPlayerContainer
import com.akamai.amp.media.VideoPlayerView
import android.view.WindowManager
import com.akamai.amp.media.elements.MediaResource
import kotlinx.android.synthetic.main.content_player.*
import com.akamai.amp.media.errors.ErrorType
import io.streamroot.dna.samples.amp.impl.AMPSRModule

class PlayerActivity : AppCompatActivity(), VideoPlayerContainer.VideoPlayerContainerCallback {

    data class PlayerActivityArgs(val url:String?)

    companion object {
        private const val ARG_STREAM_URL = "streamUrl"

        fun makeIntent(ctx: Context, args: PlayerActivityArgs) : Intent {
            return Intent(ctx, PlayerActivity::class.java).apply {
                putExtra(ARG_STREAM_URL, args.url)
            }
        }
        fun extractArgs(i: Intent) : PlayerActivityArgs {
            return PlayerActivityArgs(
                    i.getStringExtra(ARG_STREAM_URL)
            )
        }
    }

    private var srModule: AMPSRModule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        videoPlayerContainer.addVideoPlayerContainerCallback(this)

        val args = extractArgs(intent)
        val url = args.url?.takeUnless { it.isEmpty() } ?: AMPSRConfig.DEFAULT_VIDEO_URL

        start(AMPSRConfig.DNA_ENABLED, url)
    }

    fun start(srEnabled: Boolean, url: String) = with (if (srEnabled) {
        val loadControl = videoPlayerContainer.setBufferDimensions(5000, 30000, 2500,5000)
        srModule = AMPSRModule(this, videoPlayerContainer, loadControl, url)
        srModule?.enableStats(streamrootDnaStatsView)
        srModule?.finalUrl()?.toString() ?: url
    } else {
        streamrootDnaStatsView.visibility = View.GONE
        url
    }) { videoPlayerContainer.prepareResource(this) }

    fun videoPlayer() : VideoPlayerView? { return videoPlayerContainer.videoPlayer }

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
        super.onResume()
        videoPlayer()?.onResume()
    }

    override fun onPause() {
        videoPlayer()?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        videoPlayer()?.onDestroy()
        srModule?.terminate()
        super.onDestroy()
    }
}