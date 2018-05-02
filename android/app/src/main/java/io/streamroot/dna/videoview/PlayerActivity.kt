package io.streamroot.dna.videoview

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import io.streamroot.dna.core.StreamrootDNA
import io.streamroot.dna.utils.stats.StatsView
import io.streamroot.dna.utils.stats.StreamStatsManager

class PlayerActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var streamrootDnaStatsView: StatsView

    private var mStreamrootKey: String? = null
    private var mStreamUrl: String? = null

    private var streamrootDNA: StreamrootDNA? = null
    private var streamStatsManager: StreamStatsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        mStreamrootKey = intent.extras?.getString("streamrootKey")
        mStreamUrl = intent.extras?.getString("streamUrl")

        videoView = findViewById(R.id.videoView)
        streamrootDnaStatsView = findViewById(R.id.streamrootDnaStatsView)
    }

    override fun onResume() {
        val streamrootDNA = initStreamroot()

        videoView.setVideoPath(streamrootDNA?.manifestUrl.toString())
        videoView.setOnPreparedListener({ mp -> mp.setVolume(.5f, .5f) })
        videoView.setMediaController(MediaController(this))
        videoView.requestFocus()
        videoView.start()

        super.onResume()
    }

    override fun onPause() {
        videoView.pause()

        super.onPause()
    }

    override fun onDestroy() {
        videoView.stopPlayback()
        stopStreamroot()

        super.onDestroy()
    }

    private fun initStreamroot(): StreamrootDNA? {
        var mSdk: StreamrootDNA? = null
        try {
            mSdk = StreamrootDNA.newBuilder()
                    .context(applicationContext)
                    .streamrootKey(mStreamrootKey)
                    .interactor(VideoViewPlayerInteractor(videoView))
                    .start(Uri.parse(mStreamUrl))

            streamStatsManager = StreamStatsManager.newStatsManager(mSdk, streamrootDnaStatsView)
            streamStatsManager?.start()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG)
                    .show()
        }

        return mSdk
    }

    private fun stopStreamroot() {
        streamrootDNA?.destroy()
        streamrootDNA = null

        streamStatsManager?.stop()
        streamStatsManager = null
    }
}
