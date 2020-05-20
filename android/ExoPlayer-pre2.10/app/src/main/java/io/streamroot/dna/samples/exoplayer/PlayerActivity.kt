package io.streamroot.dna.samples.exoplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2StreamrootAdapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import io.streamroot.dna.core.BandwidthListener
import io.streamroot.dna.core.DnaClient
import io.streamroot.dna.core.log.LogLevel
import io.streamroot.dna.utils.stats.StatsView
import io.streamroot.dna.utils.stats.StreamStatsManager

class PlayerActivity : AppCompatActivity(), Player.EventListener {
    data class PlayerActivityArgs(val url:String?, val youboraAccount: String?)

    companion object {
        private const val ARG_STREAM_URL = "streamUrl"
        private const val ARG_YB_ACCOUNT = "youboraAccount"

        fun makeIntent(ctx: Context, args: PlayerActivityArgs) : Intent {
            return Intent(ctx, PlayerActivity::class.java).apply {
                putExtra(ARG_STREAM_URL, args.url)
                putExtra(ARG_YB_ACCOUNT, args.youboraAccount)
            }
        }
        fun extractArgs(i: Intent) : PlayerActivityArgs {
            return PlayerActivityArgs(
                    i.getStringExtra(ARG_STREAM_URL),
                    i.getStringExtra(ARG_YB_ACCOUNT)
            )
        }
    }

    private lateinit var exoPlayerView: PlayerView
    private lateinit var streamrootDnaStatsView: StatsView

    private var mStreamUrl: String? = null
    private var mYBAccount: String? = null

    private var player: ExoPlayer? = null
    private var youboraPlugin: Plugin? = null

    private var dnaClient: DnaClient? = null
    private var streamStatsManager: StreamStatsManager? = null

    private val latency: Int = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val args = extractArgs(intent)
        mStreamUrl = args.url
        mYBAccount = args.youboraAccount?.takeUnless { it.isBlank() }

        exoPlayerView = findViewById(R.id.exoplayerView)
        streamrootDnaStatsView = findViewById(R.id.streamrootDnaStatsView)
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23 || player == null) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initPlayer() {
        if (player == null) {
            val bandwidthMeter = ExoPlayerBandwidthMeter()
            val loadControl = DefaultLoadControl()
            val trackSelector = DefaultTrackSelector()
            val renderersFactory = DefaultRenderersFactory(applicationContext)

            val newPlayer = ExoPlayerFactory.newSimpleInstance(
                this,
                renderersFactory,
                trackSelector,
                loadControl,
                null, // DrmSessionManager
                bandwidthMeter
            )
            newPlayer.playWhenReady = true
            newPlayer.addListener(this)

            dnaClient = initStreamroot(newPlayer, loadControl, bandwidthMeter)
            val manifestUri = dnaClient?.manifestUrl ?: Uri.parse(mStreamUrl)
            newPlayer.prepare(LoopingMediaSource(buildMediaSource(manifestUri)), true, false)

            player = newPlayer
            exoPlayerView.player = newPlayer
            startYoubora()
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null

        stopStreamroot()
        stopYoubora()
    }

    @SuppressLint("SwitchIntDef")
    private fun buildMediaSource(uri: Uri): MediaSource {
        val defaultDataSourceFactory =
            DefaultHttpDataSourceFactory(
                    Util.getUserAgent(applicationContext, "StreamrootQA"),
                    DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                    DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                    true
            )

        return when (Util.inferContentType(uri)) {
            C.TYPE_HLS -> HlsMediaSource.Factory(defaultDataSourceFactory)
                .createMediaSource(uri)
            C.TYPE_DASH -> DashMediaSource.Factory(
                DefaultDashChunkSource.Factory(
                    defaultDataSourceFactory
                ), defaultDataSourceFactory
            )
                .createMediaSource(uri)
            else -> {
                throw IllegalStateException("Unsupported type for url: $uri")
            }
        }
    }

    private fun initStreamroot(
        newPlayer: SimpleExoPlayer,
        loadControl: LoadControl,
        bandwidthListener: BandwidthListener
    ): DnaClient? {
        var mSdk: DnaClient? = null
        try {
            mSdk = DnaClient.newBuilder()
                .context(applicationContext)
                .playerInteractor(ExoPlayerInteractor(newPlayer, loadControl, false))
                .latency(latency)
                .qosModule(ExoPlayerQosModule(newPlayer))
                .bandwidthListener(bandwidthListener)
                .logLevel(LogLevel.OFF) // Default level: ERROR. Available: [VERBOSE, DEBUG, INFO, WARN, ERROR, OFF]
                .start(Uri.parse(mStreamUrl))

            streamStatsManager = StreamStatsManager.newStatsManager(mSdk, streamrootDnaStatsView)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
        }

        return mSdk
    }

    private fun stopStreamroot() {
        dnaClient?.close()
        dnaClient = null

        streamStatsManager?.close()
        streamStatsManager = null
    }

    /**
     * Youbora
     */

    private fun startYoubora() {
        mYBAccount?.let { ybAccount ->
            val adapter = Exoplayer2StreamrootAdapter(dnaClient!!, player!!)
            val options = Options().apply {
                accountCode = ybAccount
            }
            youboraPlugin = Plugin(options, this).apply {
                this.adapter = adapter
            }
        }
    }

    private fun stopYoubora() {
        youboraPlugin?.fireStop()
        youboraPlugin = null
    }

    /**
     * Utils
     */

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Player EventListener
     */

    override fun onPlayerError(error: ExoPlaybackException?) {
        var errorString: String? = null
        if (error?.type == ExoPlaybackException.TYPE_RENDERER) {
            val cause = error.rendererException
            if (cause is MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                errorString = when {
                    cause.decoderName != null -> getString(
                        R.string.error_instantiating_decoder,
                        cause.decoderName
                    )
                    cause.cause is MediaCodecUtil.DecoderQueryException -> getString(R.string.error_querying_decoders)
                    cause.secureDecoderRequired -> getString(
                        R.string.error_no_secure_decoder,
                        cause.mimeType
                    )
                    else -> getString(R.string.error_no_decoder, cause.mimeType)
                }
            }
        }

        if (errorString != null) {
            showToast(errorString)
        }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}
    override fun onSeekProcessed() {}
    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {}
    override fun onPositionDiscontinuity(reason: Int) {}
    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}
}