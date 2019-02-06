package io.streamroot.dna.playkit

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.kaltura.playkit.*
import io.streamroot.dna.core.DnaClient
import io.streamroot.dna.utils.stats.StatsView
import io.streamroot.dna.utils.stats.StreamStatsManager

class PlayerActivity : AppCompatActivity() {
    // Player and DNA client
    private var mStreamUrl: String? = null
    private var mDnaClient: DnaClient? = null
    private var mPlayer: Player? = null
    private var mMediaConfig: PKMediaConfig? = null
    private var mSeeking: Boolean = false

    // Streamroot stats view
    private var mStreamStatsManager: StreamStatsManager? = null
    private var mStreamrootStatsView: StatsView? = null

    private fun startStatsView() {
        mDnaClient?.also {
            mStreamrootStatsView!!.visibility = View.VISIBLE
            mStreamStatsManager = StreamStatsManager(
                statsViewRefreshSequence(),
                it,
                mStreamrootStatsView!!
            )
        }
    }

    private fun statsViewRefreshSequence(): Sequence<Long> {
        var counter = 0
        return generateSequence {
            if (++counter < 120) {
                1_000L
            } else {
                20_000L
            }
        }
    }

    private fun createMediaConfig() {
        // Build PlayKit's media config object
        mMediaConfig = PKMediaConfig()
        val mediaSource = PKMediaSource()

        // Inject DnaClient manifest URL to MediaConfig
        val playerInteractor = PlayKitInteractor(mPlayer!!)
        val qosModule = PlayKitQoSModule(mPlayer!!)
        mDnaClient = DnaClient.newBuilder()
            .context(applicationContext)
            .playerInteractor(playerInteractor)
            .qosModule(qosModule)
            .start(Uri.parse(mStreamUrl))
        mediaSource.url = mDnaClient!!.manifestUrl.toString()

        val mediaSources = ArrayList<PKMediaSource>()
        mediaSources.add(mediaSource)
        val mediaEntry = PKMediaEntry()
        mediaEntry.sources = mediaSources
        mMediaConfig!!.mediaEntry = mediaEntry
    }

    private fun formatTime(msTime : Long) : String {
        val minutes = msTime / 60000
        val seconds = msTime % 60000 / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        mStreamUrl = intent.extras?.getString("streamUrl")
        mStreamrootStatsView = findViewById(R.id.streamrootStatsView)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Set-up the player
        mPlayer = PlayKitManager.loadPlayer(this, null)
        val playerRoot : FrameLayout = findViewById(R.id.playkitContainer)
        playerRoot.addView(mPlayer!!.view)

        // Add UI event handlers
        findViewById<ImageButton>(R.id.playButton).setOnClickListener {
            if (mPlayer != null) {
                mPlayer!!.play()
            }
        }
        findViewById<ImageButton>(R.id.pauseButton).setOnClickListener {
            if (mPlayer != null) {
                mPlayer!!.pause()
            }
        }
        findViewById<SeekBar>(R.id.seekBar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Do nothing
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mSeeking = false
                mPlayer!!.seekTo(seekBar.progress.toLong())
            }
        })

        // Add seek bar handlers
        mPlayer!!.addEventListener((PKEvent.Listener {
            if (!mSeeking) {
                val currentPosition = mPlayer!!.currentPosition
                val bufferedPosition = mPlayer!!.bufferedPosition
                val contentDuration = mPlayer!!.duration
                val seekBar = findViewById<SeekBar>(R.id.seekBar)

                findViewById<TextView>(R.id.currentTime).text = formatTime(currentPosition)
                findViewById<TextView>(R.id.endTime).text = formatTime(contentDuration)
                seekBar.max = contentDuration.toInt()
                seekBar.progress = currentPosition.toInt()
                seekBar.secondaryProgress = bufferedPosition.toInt()
            }
        }), PlayerEvent.Type.PLAYHEAD_UPDATED)

        // Set-up media and DnaClient
        createMediaConfig()
        mPlayer!!.prepare(mMediaConfig!!)
        mPlayer!!.play()

        // Start Streamroot stats view
        startStatsView()
    }

    override fun onResume() {
        super.onResume()

        //Resume player
        if (mPlayer != null) {
            mPlayer!!.onApplicationResumed()
            mPlayer!!.play()
        }
    }

    override fun onPause() {
        super.onPause()

        //Pause player
        if (mPlayer != null) {
            mPlayer!!.onApplicationPaused()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDnaClient!!.close()
        mPlayer!!.stop()
    }
}
