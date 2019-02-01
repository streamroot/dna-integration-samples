package io.streamroot.dna.playkit

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import com.kaltura.playkit.*
import io.streamroot.dna.core.DnaClient

class PlayerActivity : AppCompatActivity() {
    private var mStreamUrl: String? = null
    private var mDnaClient: DnaClient? = null
    private var mPlayer: Player? = null
    private var mMediaConfig: PKMediaConfig? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        mStreamUrl = intent.extras?.getString("streamUrl")

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Set-up the player
        mPlayer = PlayKitManager.loadPlayer(this, null)
        val playerRoot : FrameLayout = findViewById(R.id.playkitContainer)
        playerRoot.addView(mPlayer!!.view)

        // Set-up media and DnaClient
        createMediaConfig()
        mPlayer!!.prepare(mMediaConfig!!)
        mPlayer!!.play()
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
