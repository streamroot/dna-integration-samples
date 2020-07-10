package io.streamroot.dna.samples.brightcove;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.brightcove.player.config.AllocatorConfig;
import com.brightcove.player.config.LoadControlConfig;
import com.brightcove.player.display.ExoPlayerVideoDisplayComponent;
import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.model.DeliveryType;
import com.brightcove.player.model.Source;
import com.brightcove.player.model.SourceCollection;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.brightcove.player.view.BrightcovePlayer;

import java.util.Collections;

import io.streamroot.dna.core.BandwidthListener;
import io.streamroot.dna.core.DnaClient;
import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.QosModule;
import io.streamroot.dna.core.log.LogLevel;
import io.streamroot.dna.utils.stats.StatsView;
import io.streamroot.dna.utils.stats.StreamStatsManager;

public class PlayerActivity extends BrightcovePlayer {

    private final String TAG = this.getClass().getSimpleName();

    private DnaClient dnaClient;
    private StreamStatsManager streamStatsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the static buffer target
        AllocatorConfig allocatorConfig = new AllocatorConfig.Builder().build();
        LoadControlConfig loadControlConfig = new LoadControlConfig.Builder()
                .setAllocatorConfig(allocatorConfig)
                .setMinBufferMs(10000)
                .setMaxBufferMs((DnaClient.generateBufferTarget(10000, 30000, 30000)))
                .build();

        // When extending the BrightcovePlayer, we must assign the brightcoveVideoView before
        // entering the superclass. This allows for some stock video player lifecycle
        // management.  Establish the video object and use it's event emitter to get important
        // notifications and to control logging.
        setContentView(R.layout.activity_player);
        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcove_video_view);
        ExoPlayerVideoDisplayComponent displayComponent = (ExoPlayerVideoDisplayComponent) brightcoveVideoView.getVideoDisplay();
        displayComponent.setLoadControlConfig(loadControlConfig);
        super.onCreate(savedInstanceState);

        // Get Video data
        Intent intent = getIntent();
        String accountId = intent.getStringExtra(MainActivity.EXTRA_ACCOUNT_ID);
        String videoId = intent.getStringExtra(MainActivity.EXTRA_VIDEO_ID);
        String policyId = intent.getStringExtra(MainActivity.EXTRA_POLICY_ID);

        // Get the event emitter from the SDK and create a catalog request to fetch a video from the
        // Brightcove Edge service, given a video id, an account id and a policy key.
        EventEmitter eventEmitter = brightcoveVideoView.getEventEmitter();
        Catalog catalog = new Catalog(eventEmitter, accountId, policyId);
        brightcoveVideoView.pause();
        catalog.findVideoByID(videoId, new VideoListener() {

            // Add the video found to the queue with add().
            // Start playback of the video with start().
            @Override
            public void onVideo(Video video) {
                SourceCollection sources = video.getSourceCollectionByDeliveryType(DeliveryType.HLS);
                // Fallback on default implementation if
                Source firstHlsSource = sources.getSources().iterator().next();
                if (firstHlsSource == null) {
                    brightcoveVideoView.add(video);
                    return;
                }
                
                // Start streamroot components
                PlayerInteractor playerInteractor = new BrightcoveInteractor((BrightcoveExoPlayerVideoView) brightcoveVideoView);
                QosModule qosModule = new BrightcoveQoSModule((BrightcoveExoPlayerVideoView) brightcoveVideoView);
                BandwidthListener bandwidthListener = new BrightcoveBandwidthListener(displayComponent);

                // Build streamroot DNAClient
                dnaClient = DnaClient.newBuilder()
                        .context(getApplicationContext())
                        .playerInteractor(playerInteractor)
                        .qosModule(qosModule)
//                        .contentId(videoId)
                        .bandwidthListener(bandwidthListener)
                        .logLevel(LogLevel.DEBUG)
                        .latency(30)// Recommended setting (only on live)
                        .start(Uri.parse(firstHlsSource.getUrl()));

                SourceCollection dnaSourceCollection = new SourceCollection(new Source(String.valueOf(dnaClient.getManifestUrl()), DeliveryType.HLS), DeliveryType.HLS);
                brightcoveVideoView.add(new Video(video.getProperties(), Collections.singleton(dnaSourceCollection), video.getCuePoints()));

                //  Start and display stat view
                startStatsView();

                //Start video playack
                brightcoveVideoView.start();
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
    public void onDestroy() {
        super.onDestroy();
        dnaClient.close();
    }
}
