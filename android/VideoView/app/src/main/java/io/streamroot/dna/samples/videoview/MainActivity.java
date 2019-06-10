package io.streamroot.dna.samples.videoview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public final class MainActivity extends AppCompatActivity {
    private AutoCompleteTextView streamEditText;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_main);
        streamEditText = findViewById(R.id.streamEditText);

        Button launchButton = findViewById(R.id.launchButton);
        launchButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);

            intent.putExtra("streamUrl", streamEditText.getText().toString());

            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            getApplicationContext().startActivity(intent);
        });
    }
}