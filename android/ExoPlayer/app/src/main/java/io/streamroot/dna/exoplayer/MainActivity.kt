package io.streamroot.dna.exoplayer

import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var streamEditText: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        streamEditText = findViewById(R.id.streamEditText)

        findViewById<Button>(R.id.launchButton).setOnClickListener {
            val intent = Intent(applicationContext, PlayerActivity::class.java)

            intent.putExtra("streamUrl", streamEditText.text.toString())
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            applicationContext.startActivity(intent)
        }
    }
}
