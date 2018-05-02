package io.streamroot.dna.videoview

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AutoCompleteTextView
import android.widget.Button
import io.streamroot.dna.core.StreamrootDNA

class MainActivity : AppCompatActivity() {
    private lateinit var streamrootKeyEditText: AutoCompleteTextView
    private lateinit var streamEditText: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StreamrootDNA.init(application)

        streamrootKeyEditText = findViewById(R.id.streamrootKeyEditText)
        streamEditText = findViewById(R.id.streamEditText)

        findViewById<Button>(R.id.launchButton).setOnClickListener {
            val intent = Intent(applicationContext, PlayerActivity::class.java)

            intent.putExtra("streamrootKey", streamrootKeyEditText.text.toString())
            intent.putExtra("streamUrl", streamEditText.text.toString())

            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);


            applicationContext.startActivity(intent)
        }
    }
}
