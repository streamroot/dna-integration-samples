package io.streamroot.dna.exoplayer

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
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
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.streamroot.dna.core.DnaClient
import io.streamroot.dna.utils.stats.StatsView
import io.streamroot.dna.utils.stats.StreamStatsManager
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity(), Player.EventListener {

    private lateinit var exoplayerView: PlayerView
    private lateinit var streamrootDnaStatsView: StatsView
    private lateinit var bandwidthMeter: ExoplayerBandwidthMeter

    private var mStreamUrl: String? = null
    private var player: SimpleExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private var dnaClient: DnaClient? = null
    private var streamStatsManager: StreamStatsManager? = null

    private val latency: Int = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        mStreamUrl = intent.extras?.getString("streamUrl")
        exoplayerView = findViewById(R.id.exoplayerView)
        streamrootDnaStatsView = findViewById(R.id.streamrootDnaStatsView)
        bandwidthMeter = ExoplayerBandwidthMeter()
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
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
            trackSelector = DefaultTrackSelector(adaptiveTrackSelectionFactory)

            val extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
            val renderersFactory = DefaultRenderersFactory(applicationContext, extensionRendererMode)
            val newPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl())
            newPlayer.playWhenReady = true
            newPlayer.playWhenReady = true
            newPlayer.addListener(this)

            player = newPlayer

            dnaClient = initStreamroot()
            val manifestUri = dnaClient?.manifestUrl ?: Uri.parse(mStreamUrl)
            newPlayer.prepare(LoopingMediaSource(buildMediaSource(manifestUri)), true, false)

            exoplayerView.player = newPlayer
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null

        stopStreamroot()
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val defaultDataSourceFactory = DefaultDataSourceFactory(
                applicationContext,
                Util.getUserAgent(applicationContext, "StreamrootQA")
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
                throw IllegalStateException("Unsupported type for url: " + uri)
            }
        }
    }

    private fun initStreamroot(): DnaClient? {
        var mSdk: DnaClient? = null
        try {
            mSdk = DnaClient.newBuilder()
                    .context(applicationContext)
                    .playerInteractor(ExoPlayerInteractor(player!!))
                    .latency(latency)
                    .start(Uri.parse(mStreamUrl))

            streamStatsManager = StreamStatsManager.newStatsManager(mSdk, streamrootDnaStatsView)
            streamStatsManager?.start()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG)
                    .show()
        }

        return mSdk
    }

    private fun loadControl(): LoadControl {
//        Prior ExoPlayer V2.8
//        DefaultLoadControl(
//            DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
//            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
//            DnaClient.generateBufferTarget(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS, DefaultLoadControl.DEFAULT_MAX_BUFFER_MS, latency, TimeUnit.SECONDS),
//            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
//            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
//            DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES,
//            DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS)

//      ExoPlayer V2.8+
        return DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
            .setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DnaClient.generateBufferTarget(
                    DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                    latency,
                    TimeUnit.SECONDS),
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS)
            .createDefaultLoadControl()
    }

    private fun stopStreamroot() {
        dnaClient?.close()
        dnaClient = null

        streamStatsManager?.stop()
        streamStatsManager = null
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
                errorString = if (cause.decoderName == null) {
                    when {
                        cause.cause is MediaCodecUtil.DecoderQueryException -> getString(io.streamroot.dna.exoplayer.R.string.error_querying_decoders)
                        cause.secureDecoderRequired -> getString(
                                io.streamroot.dna.exoplayer.R.string.error_no_secure_decoder,
                                cause.mimeType
                        )
                        else -> getString(
                                io.streamroot.dna.exoplayer.R.string.error_no_decoder,
                                cause.mimeType
                        )
                    }
                } else {
                    getString(
                            io.streamroot.dna.exoplayer.R.string.error_instantiating_decoder,
                            cause.decoderName
                    )
                }
            }
        }

        if (errorString != null) {
            showToast(errorString)
        }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}
    override fun onSeekProcessed() {}
    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}
    override fun onLoadingChanged(isLoading: Boolean) {}
    override fun onPositionDiscontinuity(reason: Int) {}
    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}
}