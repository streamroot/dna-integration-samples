package io.streamroot.dna.samples.playkit;

import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kaltura.android.exoplayer2.DefaultLoadControl;
import com.kaltura.android.exoplayer2.LoadControl;
import com.kaltura.playkit.PKDrmParams;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;

import java.util.Collections;
import java.util.List;

import io.streamroot.dna.core.DnaClient;
import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.QosModule;
import io.streamroot.dna.core.log.LogLevel;
import io.streamroot.dna.utils.stats.StatsView;
import io.streamroot.dna.utils.stats.StreamStatsManager;


public class MainActivity extends AppCompatActivity {

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final PKMediaFormat MEDIA_FORMAT = PKMediaFormat.hls;
    private static final String SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/applehttp/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.m3u8";
    private static final String LICENSE_URL = null;

    private Player player;
    private Button playPauseButton;

    private DnaClient dnaClient;
    private StreamStatsManager streamStatsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Create instance of the player without plugins.
        player = PlayKitManager.loadPlayer(this, null);

        //Initialize media config object.
        PKMediaConfig mediaConfig = createMediaConfig();

        //Add player to the view hierarchy.
        addPlayerToView();

        //Add simple play/pause button.
        addPlayPauseButton();

        //Prepare player with media configuration.
        player.prepare(mediaConfig);

        // Allow Streamroot to redirect traffic
        player.getSettings().setAllowCrossProtocolRedirect(true);

        //Start playback.
        player.play();

        // Start Streamroot stats view
        startStatsView();
    }

    /**
     * Will create {@link } object.
     */
    private PKMediaConfig createMediaConfig() {
        //First. Create PKMediaConfig object.
        PKMediaConfig mediaConfig = new PKMediaConfig();
        PlayKitCustomBandwidthMeter bandwidthListener = new PlayKitCustomBandwidthMeter();
        LoadControl loadControl = new DefaultLoadControl.Builder().createDefaultLoadControl();
        PlayerInteractor playerInteractor = new PlayKitInteractor(player, loadControl);
        QosModule qosModule = new PlayKitQoSModule(player);

        player.getSettings().setCustomLoadControlStrategy(new DnaLoadControlStrategy(loadControl, bandwidthListener));

        //Set start position of the media. This will
        //automatically start playback from specified position.
        mediaConfig.setStartPosition(START_POSITION);

        // Start Streamroot SDK
        dnaClient = DnaClient.newBuilder()
                .context(getApplicationContext())
                .playerInteractor(playerInteractor)
                .qosModule(qosModule)
                .bandwidthListener(bandwidthListener)
//                .latency(30) // Recommended setting (only on live)
                .logLevel(LogLevel.OFF) // Default level: ERROR. Available: [VERBOSE, DEBUG, INFO, WARN, ERROR, OFF]
                .start(Uri.parse(SOURCE_URL));

        //Second. Create PKMediaEntry object.
        PKMediaEntry mediaEntry = createMediaEntry();

        //Add it to the mediaConfig.
        mediaConfig.setMediaEntry(mediaEntry);

        return mediaConfig;
    }

    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId("testEntry");

        //Set media entry type. It could be Live,Vod or Unknown.
        //In this sample we use Vod.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Vod);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createMediaSources() {

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId("testSource");

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.setUrl(dnaClient.getManifestUrl().toString());

        //Set the format of the source. In our case it will be hls in case of mpd/wvm formats you have to to call mediaSource.setDrmData method as well
        mediaSource.setMediaFormat(MEDIA_FORMAT);

        // Add DRM data if required
        if (LICENSE_URL != null) {
            mediaSource.setDrmData(Collections.singletonList(
                    new PKDrmParams(LICENSE_URL, PKDrmParams.Scheme.WidevineCENC)
            ));
        }

        return Collections.singletonList(mediaSource);
    }

    /**
     * Will add player to the view.
     */
    private void addPlayerToView() {
        //Get the layout, where the player view will be placed.
        LinearLayout layout = findViewById(R.id.player_root);
        //Add player view to the layout.
        layout.addView(player.getView());
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private void addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button);
        //Add clickListener.
        playPauseButton.setOnClickListener(v -> {
            if (player.isPlaying()) {
                //If player is playing, change text of the button and pause.
                playPauseButton.setText(R.string.play_text);
                player.pause();
            } else {
                //If player is not playing, change text of the button and play.
                playPauseButton.setText(R.string.pause_text);
                player.play();
            }
        });
    }

    /**
     * Start Streamroot debug stats view
     */
    private void startStatsView() {
        if (dnaClient != null) {
            StatsView streamrootStatsView = findViewById(R.id.streamrootStatsView);
            streamrootStatsView.setVisibility(View.VISIBLE);
            streamStatsManager = new StreamStatsManager(1_000L, dnaClient, streamrootStatsView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onApplicationResumed();
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onApplicationPaused();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dnaClient != null) {
            dnaClient.close();
        }

        if (streamStatsManager != null) {
            streamStatsManager.close();
        }
    }
}