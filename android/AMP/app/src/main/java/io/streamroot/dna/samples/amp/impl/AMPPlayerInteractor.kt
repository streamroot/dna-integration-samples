package io.streamroot.dna.samples.amp.impl

import android.os.Looper
import com.akamai.amp.media.VideoPlayerView
import com.akamai.exoplayer2.AmpBasePlayer
import com.akamai.exoplayer2.DefaultLoadControl
import com.akamai.exoplayer2.LoadControl
import com.akamai.exoplayer2.Timeline
import io.streamroot.dna.core.PlayerInteractor
import io.streamroot.dna.core.TimeRange
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Boris Borgobello on 2020-03-26.
 */

/**
 * ExoPlayer common
 */

private interface BufferTargetBridge {
    fun bufferTarget() : Double = 0.0
    fun setBufferTarget(bufferTarget: Double) {}
}

private class BufferTargetBridgeDefault : BufferTargetBridge

private abstract class LoadControlBufferTargetBridge(protected val loadControl: LoadControl) : BufferTargetBridge {

    protected fun LoadControl.getAccessibleFieldElseThrow(fieldName: String) = runCatching {
        val minBufferField = this::class.java.getDeclaredField(fieldName)
        minBufferField.isAccessible = true
        minBufferField
    }.getOrNull() ?: throw IllegalArgumentException("Impossible to retrieve field `$fieldName` value from LoadControl of type `${this::class.java.simpleName}`")

    protected fun LoadControl.getLongFromFieldElseThrow(fieldName: String) = runCatching {
        getAccessibleFieldElseThrow(fieldName).getLong(this)
    }.getOrNull() ?: throw IllegalArgumentException("Impossible to retrieve long `$fieldName` value from LoadControl of type `${this::class.java.simpleName}`")

    companion object {
        private const val MAX_BUFFER_FIELD_NAME = "maxBufferUs"
    }

    protected val maxBufferField = loadControl.getAccessibleFieldElseThrow(MAX_BUFFER_FIELD_NAME)
    protected abstract val minBufferUs: Long

    override fun bufferTarget(): Double {
        return runCatching {
            maxBufferField.getLong(loadControl).let { TimeUnit.MICROSECONDS.toSeconds(it) }.toDouble()
        }.getOrNull() ?: super.bufferTarget()
    }

    override fun setBufferTarget(bufferTarget: Double) {
        val maxBufferUs = TimeUnit.SECONDS.toMicros(bufferTarget.toLong())
        if (maxBufferUs > minBufferUs) runCatching {
            maxBufferField.setLong(
                    loadControl,
                    maxBufferUs
            )
        }
    }
}

private class LoadControlBufferTargetBridgeV1(loadControl: LoadControl)
    : LoadControlBufferTargetBridge(loadControl) {
    companion object {
        private const val MIN_BUFFER_FIELD_NAME = "minBufferUs"
    }

    override val minBufferUs = loadControl.getLongFromFieldElseThrow(MIN_BUFFER_FIELD_NAME)
}

private class LoadControlBufferTargetBridgeV2(loadControl: LoadControl, audioOnly: Boolean)
    : LoadControlBufferTargetBridge(loadControl) {
    companion object {
        private const val MIN_BUFFER_AUDIO_FIELD_NAME = "minBufferAudioUs"
        private const val MIN_BUFFER_VIDEO_FIELD_NAME = "minBufferVideoUs"
    }

    override val minBufferUs = loadControl.getLongFromFieldElseThrow(
            if (audioOnly) MIN_BUFFER_AUDIO_FIELD_NAME else MIN_BUFFER_VIDEO_FIELD_NAME
    )
}

private object BufferTargetBridgeFactory {
    fun createInteractor(loadControl: LoadControl, audioOnly: Boolean) : BufferTargetBridge {
        return runCatching { LoadControlBufferTargetBridgeV1(loadControl) }.getOrNull()
                ?: runCatching { LoadControlBufferTargetBridgeV2(loadControl, audioOnly) }.getOrNull()
                ?: BufferTargetBridgeDefault()
    }
}

/**
 * AMP specific
 */

class AMPPlayerInteractor(loadControl: DefaultLoadControl, audioOnly: Boolean = false) : PlayerInteractor {

    private val playerViewRef = AtomicReference<VideoPlayerView?>(null)
    private val bridge = BufferTargetBridgeFactory.createInteractor(loadControl, audioOnly)

    override fun bufferTarget() = bridge.bufferTarget()
    override fun setBufferTarget(bufferTarget: Double) = bridge.setBufferTarget(bufferTarget)
    override fun looper(): Looper? = Looper.getMainLooper() // UI thread Exo compatible only

    fun setPlayer(playerView: VideoPlayerView) = playerViewRef.set(playerView)

    private fun <T> tryPlayer(lambda: (AmpBasePlayer)->T?) : T? = playerViewRef.get()?.let { lambda(it.ampBasePlayer) }

    override fun playbackTime() = tryPlayer { getCurrentWindowShift() + it.currentPosition } ?: 0L

    override fun loadedTimeRanges() = tryPlayer {
        val shift = getCurrentWindowShift()
        val rangeDurationMs = it.bufferedPosition - it.currentPosition
        if (rangeDurationMs > 0) {
            val tr = TimeRange(shift + it.currentPosition, rangeDurationMs)
            arrayListOf(tr)
        } else null
    } ?: emptyList<TimeRange>()

    private fun getCurrentWindowShift() = tryPlayer {
        val current = it.currentTimeline
        val timelineWindow = Timeline.Window()
        var shift: Long = 0

        if (it.currentWindowIndex < current?.windowCount!!) {
            it.currentTimeline?.getWindow(it.currentWindowIndex, timelineWindow)
            shift = timelineWindow.positionInFirstPeriodMs
        }

        shift
    } ?: 0L
}