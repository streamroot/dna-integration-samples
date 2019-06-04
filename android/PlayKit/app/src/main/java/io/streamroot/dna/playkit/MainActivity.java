package io.streamroot.dna.playkit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.launchButton).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
            AutoCompleteTextView streamEditText = findViewById(R.id.streamEditText);

            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("streamUrl", streamEditText.getText().toString());

            getApplicationContext().startActivity(intent);
        });
    }
}
