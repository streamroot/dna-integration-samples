package io.streamroot.dna.playkit

import android.os.Looper
import com.kaltura.playkit.Player
import io.streamroot.dna.core.PlayerInteractor
import io.streamroot.dna.core.TimeRange
import java.util.*

class PlayKitInteractor(
        player : Player
) : PlayerInteractor {
    private val mPlayer = player

    override fun looper(): Looper? = Looper.getMainLooper()

    override fun playbackTime() : Long {
        val position = mPlayer.currentPosition
        return position
    }

    override fun loadedTimeRanges(): List<TimeRange> {
        val currentPosition = mPlayer.currentPosition
        val bufferedPosition = mPlayer.bufferedPosition
        return Collections.singletonList(TimeRange(currentPosition, bufferedPosition))
    }
}