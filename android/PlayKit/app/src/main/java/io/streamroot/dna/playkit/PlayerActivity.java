package io.streamroot.dna.playkit;

import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import io.streamroot.dna.core.BandwidthListener;
import io.streamroot.dna.core.DnaClient;
import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.QosModule;
import io.streamroot.dna.utils.stats.StatsView;
import io.streamroot.dna.utils.stats.StreamStatsManager;

import java.util.ArrayList;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {
    private Player mPlayer;
    private DnaClient mDnaClient;
    private StreamStatsManager mStreamStatsManager;

    private boolean mSeeking = false;

    private String mStreamUrl;
    private StatsView mStreamrootStatsView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_player);

        this.mStreamUrl = getIntent().getStringExtra("streamUrl");
        this.mStreamrootStatsView = findViewById(R.id.streamrootStatsView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set-up the player
        this.mPlayer = PlayKitManager.loadPlayer(this, null);

        FrameLayout playerRoot = findViewById(R.id.playkitContainer);
        playerRoot.addView(mPlayer.getView());

        // Add UI event handlers
        findViewById(R.id.playButton).setOnClickListener(v -> {
            if (mPlayer != null) mPlayer.play();
        });

        findViewById(R.id.pauseButton).setOnClickListener(v -> {
            if (mPlayer != null) mPlayer.pause();
        });

        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Do nothing
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeeking = false;
                mPlayer.seekTo(seekBar.getProgress());
            }
        });

        // Add seek bar handlers
        mPlayer.addListener(this, com.kaltura.playkit.PlayerEvent.playheadUpdated, event -> {
            if (mSeeking) {
                long currentPosition = mPlayer.getCurrentPosition();
                long bufferedPosition = mPlayer.getBufferedPosition();
                long contentDuration = mPlayer.getDuration();

                SeekBar seekBar = findViewById(R.id.seekBar);

                ((TextView) findViewById(R.id.currentTime)).setText(formatTime(currentPosition));
                ((TextView) findViewById(R.id.endTime)).setText(formatTime(contentDuration));

                seekBar.setMax((int) contentDuration);
                seekBar.setProgress((int) currentPosition);
                seekBar.setSecondaryProgress((int) bufferedPosition);
            }
        });

        // Set-up media and DnaClient
        PKMediaConfig mediaConfig = createMediaConfig();

        mPlayer.prepare(mediaConfig);
        mPlayer.play();

        // Start Streamroot stats view
        startStatsView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Resume player
        if (mPlayer != null) {
            mPlayer.onApplicationResumed();
            mPlayer.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mPlayer != null) {
            mPlayer.onApplicationPaused();
            mPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer = null;
        }

        if (mDnaClient != null) {
            mDnaClient.close();
            mDnaClient = null;
        }

        if (mStreamStatsManager != null) {
            mStreamStatsManager.close();
            mStreamStatsManager = null;
        }
    }

    private String formatTime(long msTime) {
        long min = msTime / 60000;
        long sec = msTime % 60000 / 1000;

        return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
    }

    private PKMediaConfig createMediaConfig() {
        // Init Streamroot
        PlayerInteractor playerInteractor = new PlayKitInteractor(mPlayer);
        QosModule qosModule = new PlayKitQoSModule(mPlayer);
        BandwidthListener bandwidthListener = new PlayKitBandwidthListener(mPlayer);

        mDnaClient = DnaClient.newBuilder()
                .context(getApplicationContext())
                .playerInteractor(playerInteractor)
                .qosModule(qosModule)
                .bandwidthListener(bandwidthListener)
                .latency(30) // Recommended setting
                .start(Uri.parse(mStreamUrl));

        // Init player config
        PKMediaEntry mediaEntry = new PKMediaEntry();
        PKMediaConfig mediaConfig = new PKMediaConfig();
        PKMediaSource mediaSource = new PKMediaSource();
        ArrayList<PKMediaSource> mediaSources = new ArrayList<>();

        mediaSource.setUrl(mDnaClient.getManifestUrl().toString());
        mediaSources.add(mediaSource);
        mediaEntry.setSources(mediaSources);
        mediaConfig.setMediaEntry(mediaEntry);

        return mediaConfig;
    }

    private void startStatsView() {
        if (mDnaClient != null) {
            mStreamrootStatsView.setVisibility(View.VISIBLE);
            mStreamStatsManager = new StreamStatsManager(1_000L, mDnaClient, mStreamrootStatsView);
        }
    }
}
