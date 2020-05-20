package io.streamroot.dna.samples.videoview;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import io.streamroot.dna.core.DnaClient;
import io.streamroot.dna.core.log.LogLevel;
import io.streamroot.dna.utils.stats.StatsView;
import io.streamroot.dna.utils.stats.StreamStatsManager;

public final class PlayerActivity extends AppCompatActivity {
    private VideoView videoView;
    private StatsView streamrootDnaStatsView;

    private String mStreamUrl;

    private DnaClient streamrootDNA;
    private StreamStatsManager streamStatsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);

        mStreamUrl = Objects.requireNonNull(getIntent().getExtras()).getString("streamUrl");

        videoView = findViewById(R.id.videoView);
        streamrootDnaStatsView = findViewById(R.id.streamrootDnaStatsView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initStreamroot();
        if (streamrootDNA != null) {
            videoView.setVideoPath(streamrootDNA.getManifestUrl().toString());
        } else {
            videoView.setVideoPath(mStreamUrl);
        }

        videoView.requestFocus();
        videoView.setMediaController(new MediaController(this));
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        videoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        videoView.stopPlayback();
        stopStreamroot();
    }

    private void initStreamroot() {
        try {
            streamrootDNA = DnaClient.newBuilder()
                    .context(getApplicationContext())
                    .playerInteractor(new VideoViewPlayerInteractor(videoView))
                    .logLevel(LogLevel.OFF) // Default level: ERROR. Available: [VERBOSE, DEBUG, INFO, WARN, ERROR, OFF]
                    .start(Uri.parse(mStreamUrl));

            streamStatsManager = StreamStatsManager.newStatsManager(streamrootDNA, streamrootDnaStatsView);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void stopStreamroot() {
        if (streamrootDNA != null) {
            streamrootDNA.close();
            streamrootDNA = null;
        }

        if (streamStatsManager != null) {
            streamStatsManager.close();
            streamStatsManager = null;
        }
    }
}
