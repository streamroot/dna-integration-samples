package io.streamroot.dna.samples.playkit.ovpstarter;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.player.PlayerSettings;
import com.kaltura.playkit.plugins.kava.KavaAnalyticsConfig;
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin;
import com.kaltura.playkit.providers.ovp.KalturaOvpMediaProvider;

import io.streamroot.dna.core.BandwidthListener;
import io.streamroot.dna.core.DnaClient;
import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.QosModule;
import io.streamroot.dna.core.log.LogLevel;
import io.streamroot.dna.utils.stats.StatsView;
import io.streamroot.dna.utils.stats.StreamStatsManager;

public class PlayerActivity extends PlayerActivityBase {
    public static final String ENTRY_ID = "1_djnefl4e";
    public static final int MSEC_IN_SEC = 1000;
    public static final int SEEKBAR_MSEC_FACTOR = 200;
    private static final String BASE_URL = "https://cdnapisec.kaltura.com";
    private static final int PARTNER_ID = 1424501;
    private static final String KAVA_BASE_URL = "https://analytics.kaltura.com/api_v3/index.php";
    // UIConf id -- optional for KAVA
    private static final int UI_CONF_ID = 0;
    private ImageButton playPauseButton;
    private Player player;
    private String ks;
    private String entryId = ENTRY_ID;
    private boolean ended;

    private DnaClient dnaClient;
    private StatsView streamrootStatsView;
    private StreamStatsManager streamStatsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerPlugins();

        playPauseButton = findViewById(R.id.play_pause_button);

        setupPlayer();

        fullscreenContentContainer.setKeepScreenOn(true);

        streamrootStatsView = findViewById(R.id.streamrootStatsView);


        loadMedia();
    }

    public static String formatSeconds(long timeInSeconds) {
        long secondsLeft = timeInSeconds % 60;
        long minutes = timeInSeconds % 3600 / 60;
        long hours = timeInSeconds / 3600;

        String HH = hours < 10 ? "0" + hours : "" + hours;
        String MM = minutes < 10 ? "0" + minutes : "" + minutes;
        String SS = secondsLeft < 10 ? "0" + secondsLeft : "" + secondsLeft;

        return HH + ":" + MM + ":" + SS;
    }

    public void setKs(String ks) {
        this.ks = ks;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    private void setupPlayer() {
        player = PlayKitManager.loadPlayer(this, createPluginConfigs());
        fullscreenContentContainer.addView(player.getView());

        final SeekBar seekBar = findViewById(R.id.mediacontroller_progress);
        final TextView currentTimeLabel = findViewById(R.id.time_current);
        currentTimeLabel.setText(formatSeconds(0));
        final TextView durationLabel = findViewById(R.id.time);
        durationLabel.setText(formatSeconds(0));

        player.addEventListener(event -> {
                    switch (((PlayerEvent) event).type) {

                        // Update the seekbar position and the position label
                        case PLAYHEAD_UPDATED:
                            final long position = ((PlayerEvent.PlayheadUpdated) event).position;
                            seekBar.setProgress((int) (position / SEEKBAR_MSEC_FACTOR));
                            currentTimeLabel.setText(formatSeconds(position / MSEC_IN_SEC));
                            break;

                        // Update the seekbar size and the duration label
                        case DURATION_CHANGE:
                            final long duration = ((PlayerEvent.DurationChanged) event).duration;
                            seekBar.setMax((int) (duration / SEEKBAR_MSEC_FACTOR));
                            durationLabel.setText(formatSeconds(duration / MSEC_IN_SEC));
                            break;

                        // Change play/pause button to PAUSE
                        case PLAYING:
                            fullscreenContentContainer.setKeepScreenOn(true);
                            playPauseButton.setImageResource(R.drawable.ic_pause_black_24dp);
                            break;

                        // Change play/pause button to PLAY
                        case PAUSE:
                            fullscreenContentContainer.setKeepScreenOn(false);
                            playPauseButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                            break;

                        case ENDED:
                            fullscreenContentContainer.setKeepScreenOn(false);
                            playPauseButton.setImageResource(R.drawable.ic_replay_black_24dp);
                            ended = true;
                            player.pause();
                            break;
                    }
                }, PlayerEvent.Type.DURATION_CHANGE, PlayerEvent.Type.PLAYHEAD_UPDATED,
                PlayerEvent.Type.PLAYING, PlayerEvent.Type.PAUSE, PlayerEvent.Type.ENDED);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress * SEEKBAR_MSEC_FACTOR);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        playPauseButton.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
            } else if (ended) {
                player.replay();
            } else {
                player.play();
            }
        });
    }

    private void loadMedia() {

        new KalturaOvpMediaProvider(BASE_URL, PARTNER_ID, this.ks)
                .setEntryId(entryId)
                .load(response -> runOnUiThread(() -> {
                    if (response.isSuccess()) {
                        // Find the preferred source format and replace it with DNA URL
                        PKMediaEntry mediaEntry = response.getResponse();
                        PKMediaFormat preferredFormat = ((PlayerSettings) player.getSettings()).getPreferredMediaFormat();

                        for (PKMediaSource source : mediaEntry.getSources()) {
                            if (source.getMediaFormat().name().equals(preferredFormat.name())) {
                                source.setUrl(loadStreamroot(source));
                                break;
                            }
                        }

                        // Update with entryId and KS
                        player.updatePluginConfig(KavaAnalyticsPlugin.factory.getName(), getKavaConfig());

                        player.getSettings().setAllowCrossProtocolRedirect(true);

                        player.prepare(new PKMediaConfig().setMediaEntry(mediaEntry));
                        player.play();  // Will play when ready

                        startStatsView();
                    } else {
                        Toast.makeText(PlayerActivity.this, "Failed loading media: " + response.getError(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private String loadStreamroot(final PKMediaSource source) {
        PlayerInteractor playerInteractor = new PlayKitInteractor(player);
        QosModule qosModule = new PlayKitQoSModule(player);
        BandwidthListener bandwidthListener = new PlayKitBandwidthListener(player);

        dnaClient = DnaClient.newBuilder()
                .context(getApplicationContext())
                .playerInteractor(playerInteractor)
                .qosModule(qosModule)
                .bandwidthListener(bandwidthListener)
                .latency(30) // Recommended setting
                .contentId(source.getId())
                .logLevel(LogLevel.OFF) // Default level: ERROR. Available: [VERBOSE, DEBUG, INFO, WARN, ERROR, OFF]
                .start(Uri.parse(source.getUrl()));

        return dnaClient.getManifestUrl().toString();
    }

    private PKPluginConfigs createPluginConfigs() {

        PKPluginConfigs pluginConfigs = new PKPluginConfigs();

        KavaAnalyticsConfig kavaAnalyticsConfig = getKavaConfig();


        // Set plugin entry to the plugin configs.
        pluginConfigs.setPluginConfig(KavaAnalyticsPlugin.factory.getName(), kavaAnalyticsConfig);

        return pluginConfigs;
    }

    private KavaAnalyticsConfig getKavaConfig() {
        return new KavaAnalyticsConfig()
                .setBaseUrl(KAVA_BASE_URL)
                .setPartnerId(PARTNER_ID)
                .setEntryId(entryId)
                .setKs(ks)
                .setUiConfId(UI_CONF_ID);
    }

    private void registerPlugins() {
        PlayKitManager.registerPlugins(this, KavaAnalyticsPlugin.factory);
    }

    private void startStatsView() {
        if (dnaClient != null) {
            streamrootStatsView.setVisibility(View.VISIBLE);
            streamStatsManager = new StreamStatsManager(1_000L, dnaClient, streamrootStatsView);
        }
    }

    @Override
    protected void onPause() {

        player.onApplicationPaused();

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        player.destroy();

        dnaClient.close();
        streamStatsManager.close();

        super.onDestroy();
    }
}
