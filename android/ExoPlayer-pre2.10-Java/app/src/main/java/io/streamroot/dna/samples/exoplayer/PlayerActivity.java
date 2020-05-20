package io.streamroot.dna.samples.exoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2StreamrootAdapter;
import com.npaw.youbora.lib6.plugin.Options;
import com.npaw.youbora.lib6.plugin.Plugin;

import org.jetbrains.annotations.Nullable;

import io.streamroot.dna.core.BandwidthListener;
import io.streamroot.dna.core.DnaClient;
import io.streamroot.dna.core.log.LogLevel;
import io.streamroot.dna.utils.stats.StatsView;
import io.streamroot.dna.utils.stats.StreamStatsManager;

public class PlayerActivity extends AppCompatActivity implements Player.EventListener {

    public static final class PlayerActivityArgs {
        @Nullable final String url;
        @Nullable final String youboraAccount;
        public PlayerActivityArgs(@Nullable String url, @Nullable String youboraAccount) {
            this.url = url;
            this.youboraAccount = youboraAccount;
        }
    }

    private static final String ARG_STREAM_URL = "streamUrl";
    private static final String ARG_YB_ACCOUNT = "youboraAccount";

    public static Intent makeIntent(Context ctx, PlayerActivityArgs args) {
        return new Intent(ctx, PlayerActivity.class)
            .putExtra(ARG_STREAM_URL, args.url)
            .putExtra(ARG_YB_ACCOUNT, args.youboraAccount);
    }

    public static PlayerActivityArgs extractArgs(Intent i) {
        return new PlayerActivityArgs(
                i.getStringExtra(ARG_STREAM_URL),
                i.getStringExtra(ARG_YB_ACCOUNT));
    }

    @Nullable private PlayerView exoPlayerView = null;
    @Nullable private StatsView streamrootDnaStatsView = null;

    @Nullable private String mStreamUrl = null;
    @Nullable private String mYBAccount = null;

    @Nullable private ExoPlayer player = null;
    @Nullable private Plugin youboraPlugin = null;

    @Nullable private DnaClient dnaClient = null;
    @Nullable private StreamStatsManager streamStatsManager = null;

    private final int latency = 30;

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        final PlayerActivityArgs args = extractArgs(getIntent());
        mStreamUrl = args.url;
        {
            final String tmpYB = args.youboraAccount;
            mYBAccount = (tmpYB != null && !tmpYB.trim().isEmpty()) ? tmpYB : null;
        }

        exoPlayerView = findViewById(R.id.exoplayerView);
        streamrootDnaStatsView = findViewById(R.id.streamrootDnaStatsView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Util.SDK_INT > 23) {
            initPlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Util.SDK_INT <= 23 || player == null) {
            initPlayer();
        }
    }

    @Override
    protected void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }

        super.onStop();
    }

    private void initPlayer() {
        if (player == null) {
            final ExoPlayerBandwidthMeter bandwidthMeter = new ExoPlayerBandwidthMeter();
            final DefaultLoadControl loadControl = new DefaultLoadControl();

            final TrackSelector trackSelector = new DefaultTrackSelector();
            final RenderersFactory renderersFactory = new DefaultRenderersFactory(getApplicationContext());

            final SimpleExoPlayer newPlayer = ExoPlayerFactory.newSimpleInstance(
                    this,
                    renderersFactory,
                    trackSelector,
                    loadControl,
                    null, // DrmSessionManager
                    bandwidthMeter
            );

            newPlayer.setPlayWhenReady(true);
            newPlayer.addListener(this);

            dnaClient = initStreamroot(newPlayer, loadControl, bandwidthMeter);
            final Uri manifestUri = dnaClient != null ? dnaClient.getManifestUrl() : Uri.parse(mStreamUrl);

            newPlayer.prepare(new LoopingMediaSource(buildMediaSource(manifestUri)), true, false);

            player = newPlayer;
            exoPlayerView.setPlayer(newPlayer);
            startYoubora();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        stopStreamroot();
        stopYoubora();
    }

    @SuppressLint("SwitchIntDef")
    private MediaSource buildMediaSource(Uri uri) {
        final DefaultHttpDataSourceFactory defaultDataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(getApplicationContext(), "StreamrootQA"),
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
        );

        switch (Util.inferContentType(uri)) {
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(defaultDataSourceFactory).createMediaSource(uri);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(
                                defaultDataSourceFactory
                        ), defaultDataSourceFactory
                ).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type for url: $uri");
        }
    }

    @Nullable private DnaClient initStreamroot(
            SimpleExoPlayer newPlayer,
            LoadControl loadControl,
            BandwidthListener bandwidthListener
    ) {
        @Nullable DnaClient mSdk = null;
        try {
            mSdk = DnaClient.newBuilder()
                    .context(getApplicationContext())
                    .playerInteractor(new ExoPlayerInteractor(newPlayer, loadControl, false))
                    .latency(latency)
                    .qosModule(new ExoPlayerQosModule(newPlayer))
                    .bandwidthListener(bandwidthListener)
                    .logLevel(LogLevel.OFF) // Default level: ERROR. Available: [VERBOSE, DEBUG, INFO, WARN, ERROR, OFF]
                    .start(Uri.parse(mStreamUrl));

            streamStatsManager = StreamStatsManager.newStatsManager(mSdk, streamrootDnaStatsView);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return mSdk;
    }

    private void stopStreamroot() {
        if (dnaClient != null) {
            dnaClient.close();
            dnaClient = null;
        }
        if (streamStatsManager != null) {
            streamStatsManager.close();
            streamStatsManager = null;
        }
    }

    /**
     * Youbora
     */

    private void startYoubora() {
        if (mYBAccount != null && dnaClient != null && player != null) {
            final Exoplayer2StreamrootAdapter adapter =
                    new Exoplayer2StreamrootAdapter(dnaClient, player);
            final Options options = new Options();
            options.setAccountCode(mYBAccount);
            youboraPlugin = new Plugin(options, this);
            youboraPlugin.setAdapter(adapter);
        }
    }

    private void stopYoubora() {
        if (youboraPlugin != null) {
            youboraPlugin.fireStop();
            youboraPlugin = null;
        }
    }

    /**
     * Utils
     */

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Player EventListener
     */

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        @Nullable String errorString = null;
        if (error.type == ExoPlaybackException.TYPE_RENDERER) {
            final Exception cause = error.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                final MediaCodecRenderer.DecoderInitializationException castedCause =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                // Special case for decoder initialization failures.

                if (castedCause.decoderName != null) {
                    errorString = getString(
                            R.string.error_instantiating_decoder,
                            castedCause.decoderName
                    );
                } else if (castedCause.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = getString(R.string.error_querying_decoders);
                } else if (castedCause.secureDecoderRequired) {
                    errorString = getString(
                            R.string.error_no_secure_decoder,
                            castedCause.mimeType);
                } else {
                    errorString = getString(R.string.error_no_decoder, castedCause.mimeType);
                }
            }
        }

        if (errorString != null) {
            showToast(errorString);
        }
    }
}
