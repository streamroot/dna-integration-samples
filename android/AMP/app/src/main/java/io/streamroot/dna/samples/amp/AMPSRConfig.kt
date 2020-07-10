package io.streamroot.dna.samples.amp

import android.content.Context
import io.streamroot.dna.core.Configure
import io.streamroot.dna.core.log.LogLevel
import java.io.BufferedReader
import java.io.InputStreamReader

object AMPSRConfig {
    fun AMP_LICENSE(context: Context) = {
        var inputStream: BufferedReader? = null
        var license: String?
        try {
            inputStream = BufferedReader(InputStreamReader(context.assets.open("amp.lic")))
            license = inputStream.readLine()
        } finally {
            inputStream?.close()
        }
        if (license == null || license.isEmpty()) null else license
    }() ?: "or put your license here"

    const val DEFAULT_VIDEO_URL = "http://wowza-test.streamroot.io/liveOrigin/Sintel1/playlist.m3u8"
    const val DNA_ENABLED = true

    // You can build DNA instance here
    fun configureStreamroot(baseConfig: Configure) = baseConfig.apply {
        latency(30)
        logLevel(LogLevel.DEBUG)
    }
}