package io.streamroot.dna.samples.brightcove;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_ACCOUNT_ID = "io.streamroot.dna.samples.brightcove.acountId";
    public static final String EXTRA_VIDEO_ID = "io.streamroot.dna.samples.brightcove.videoId";
    public static final String EXTRA_POLICY_ID = "io.streamroot.dna.samples.brightcove.policyId";

    private EditText accountIdText;
    private EditText videoIdText;
    private EditText policyIdText;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accountIdText = (EditText) findViewById(R.id.accountId);
        videoIdText = (EditText) findViewById(R.id.videoId);
        policyIdText = (EditText) findViewById(R.id.policyId);
        playButton = (Button) findViewById(R.id.playButton);

        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra(EXTRA_ACCOUNT_ID, accountIdText.getText().toString());
            intent.putExtra(EXTRA_VIDEO_ID, videoIdText.getText().toString());
            intent.putExtra(EXTRA_POLICY_ID, policyIdText.getText().toString());
            startActivity(intent);
        });
    }
}
