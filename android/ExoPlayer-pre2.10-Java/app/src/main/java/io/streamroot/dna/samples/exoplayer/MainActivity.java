package io.streamroot.dna.samples.exoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.launchButton).setOnClickListener(v -> {
            final Intent i = PlayerActivity.makeIntent(this,
                    new PlayerActivity.PlayerActivityArgs(
                            ((TextView)findViewById(R.id.streamEditText)).getText().toString(),
                            ((TextView)findViewById(R.id.youboraEditText)).getText().toString()
                    )
            ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
    }
}
