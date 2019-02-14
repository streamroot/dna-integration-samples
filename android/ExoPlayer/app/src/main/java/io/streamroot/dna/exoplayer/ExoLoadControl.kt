package io.streamroot.dna.app.demo.player.exoplayer


import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.Allocator
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.PriorityTaskManager
import com.google.android.exoplayer2.util.Util
import java.util.concurrent.TimeUnit

/**
 * The default [LoadControl] implementation.
 */
class ExoLoadControl protected constructor(
        private val allocator: DefaultAllocator,
        minBufferMs: Int,
        var maxBufferMs: Int,
        private var bufferForPlaybackMs: Int,
        bufferForPlaybackAfterRebufferMs: Int,
        private val targetBufferBytesOverwrite: Int,
        private val prioritizeTimeOverSizeThresholds: Boolean,
        private val priorityTaskManager: PriorityTaskManager?,
        backBufferDurationMs: Int,
        private val retainBackBufferFromKeyframe: Boolean) : LoadControl {

    private val minBufferUs: Long
    var maxBufferUs: Long
    private val bufferForPlaybackUs: Long
    private val bufferForPlaybackAfterRebufferUs: Long
    private val backBufferDurationUs: Long

    private var targetBufferSize: Int = 0
    private var isBuffering: Boolean = false

    /** Builder for [DefaultLoadControl].  */
    class Builder {

        private var allocator: DefaultAllocator? = null
        private var minBufferMs: Int = 0
        private var maxBufferMs: Int = 0
        private var bufferForPlaybackMs: Int = 0
        private var bufferForPlaybackAfterRebufferMs: Int = 0
        private var targetBufferBytes: Int = 0
        private var prioritizeTimeOverSizeThresholds: Boolean = false
        private var priorityTaskManager: PriorityTaskManager? = null
        private var backBufferDurationMs: Int = 0
        private var retainBackBufferFromKeyframe: Boolean = false
        private var createDefaultLoadControlCalled: Boolean = false

        /** Constructs a new instance.  */
        init {
            allocator = null
            minBufferMs = DEFAULT_MIN_BUFFER_MS
            maxBufferMs = DEFAULT_MAX_BUFFER_MS
            bufferForPlaybackMs = DEFAULT_BUFFER_FOR_PLAYBACK_MS
            bufferForPlaybackAfterRebufferMs = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            targetBufferBytes = DEFAULT_TARGET_BUFFER_BYTES
            prioritizeTimeOverSizeThresholds = DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS
            priorityTaskManager = null
            backBufferDurationMs = DEFAULT_BACK_BUFFER_DURATION_MS
            retainBackBufferFromKeyframe = DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME
        }

        /**
         * Sets the [DefaultAllocator] used by the loader.
         *
         * @param allocator The [DefaultAllocator].
         * @return This builder, for convenience.
         * @throws IllegalStateException If [.createDefaultLoadControl] has already been called.
         */
        fun setAllocator(allocator: DefaultAllocator): Builder {
            Assertions.checkState(!createDefaultLoadControlCalled)
            this.allocator = allocator
            return this
        }

        /**
         * Sets the buffer duration parameters.
         *
         * @param minBufferMs The minimum duration of media that the player will attempt to ensure is
         * buffered at all times, in milliseconds.
         * @param maxBufferMs The maximum duration of media that the player will attempt to buffer, in
         * milliseconds.
         * @param bufferForPlaybackMs The duration of media that must be buffered for playback to start
         * or resume following a user action such as a seek, in milliseconds.
         * @param bufferForPlaybackAfterRebufferMs The default duration of media that must be buffered
         * for playback to resume after a rebuffer, in milliseconds. A rebuffer is defined to be
         * caused by buffer depletion rather than a user action.
         * @return This builder, for convenience.
         * @throws IllegalStateException If [.createDefaultLoadControl] has already been called.
         */
        fun setBufferDurationsMs(
                minBufferMs: Int,
                maxBufferMs: Int,
                bufferForPlaybackMs: Int,
                bufferForPlaybackAfterRebufferMs: Int): Builder {
            Assertions.checkState(!createDefaultLoadControlCalled)
            this.minBufferMs = minBufferMs
            this.maxBufferMs = maxBufferMs
            this.bufferForPlaybackMs = bufferForPlaybackMs
            this.bufferForPlaybackAfterRebufferMs = bufferForPlaybackAfterRebufferMs
            return this
        }

        /**
         * Sets the target buffer size in bytes. If set to [C.LENGTH_UNSET], the target buffer
         * size will be calculated based on the selected tracks.
         *
         * @param targetBufferBytes The target buffer size in bytes.
         * @return This builder, for convenience.
         * @throws IllegalStateException If [.createDefaultLoadControl] has already been called.
         */
        fun setTargetBufferBytes(targetBufferBytes: Int): Builder {
            Assertions.checkState(!createDefaultLoadControlCalled)
            this.targetBufferBytes = targetBufferBytes
            return this
        }

        /**
         * Sets whether the load control prioritizes buffer time constraints over buffer size
         * constraints.
         *
         * @param prioritizeTimeOverSizeThresholds Whether the load control prioritizes buffer time
         * constraints over buffer size constraints.
         * @return This builder, for convenience.
         * @throws IllegalStateException If [.createDefaultLoadControl] has already been called.
         */
        fun setPrioritizeTimeOverSizeThresholds(prioritizeTimeOverSizeThresholds: Boolean): Builder {
            Assertions.checkState(!createDefaultLoadControlCalled)
            this.prioritizeTimeOverSizeThresholds = prioritizeTimeOverSizeThresholds
            return this
        }

        /**
         * Sets the [PriorityTaskManager] to use.
         *
         * @param priorityTaskManager The [PriorityTaskManager] to use.
         * @return This builder, for convenience.
         * @throws IllegalStateException If [.createDefaultLoadControl] has already been called.
         */
        fun setPriorityTaskManager(priorityTaskManager: PriorityTaskManager): Builder {
            Assertions.checkState(!createDefaultLoadControlCalled)
            this.priorityTaskManager = priorityTaskManager
            return this
        }

        /**
         * Sets the back buffer duration, and whether the back buffer is retained from the previous
         * keyframe.
         *
         * @param backBufferDurationMs The back buffer duration in milliseconds.
         * @param retainBackBufferFromKeyframe Whether the back buffer is retained from the previous
         * keyframe.
         * @return This builder, for convenience.
         * @throws IllegalStateException If [.createDefaultLoadControl] has already been called.
         */
        fun setBackBuffer(backBufferDurationMs: Int, retainBackBufferFromKeyframe: Boolean): Builder {
            Assertions.checkState(!createDefaultLoadControlCalled)
            this.backBufferDurationMs = backBufferDurationMs
            this.retainBackBufferFromKeyframe = retainBackBufferFromKeyframe
            return this
        }

        /** Creates a [DefaultLoadControl].  */
        fun createDefaultLoadControl(): ExoLoadControl {
            createDefaultLoadControlCalled = true
            if (allocator == null) {
                allocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE)
            }
            return ExoLoadControl(
                    allocator!!,
                    minBufferMs,
                    maxBufferMs,
                    bufferForPlaybackMs,
                    bufferForPlaybackAfterRebufferMs,
                    targetBufferBytes,
                    prioritizeTimeOverSizeThresholds,
                    priorityTaskManager,
                    backBufferDurationMs,
                    retainBackBufferFromKeyframe)
        }
    }

    /** Constructs a new instance, using the `DEFAULT_*` constants defined in this class.  */
    constructor() : this(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE)) {}

    fun setMaxBufferTarget(target: Double) {
        if (target.toLong() <= this.minBufferUs) return
        this.maxBufferUs = TimeUnit.SECONDS.toMicros(target.toLong())
        this.maxBufferMs = TimeUnit.SECONDS.toMillis(target.toLong()).toInt()
    }

    fun getMaxBufferTarget(): Double {
         return TimeUnit.MICROSECONDS.toSeconds(this.maxBufferUs).toDouble()
    }

    @Deprecated("Use {@link Builder} instead. ")
    @JvmOverloads
    constructor(
            allocator: DefaultAllocator,
            minBufferMs: Int = DEFAULT_MIN_BUFFER_MS,
            maxBufferMs: Int = DEFAULT_MAX_BUFFER_MS,
            bufferForPlaybackMs: Int = DEFAULT_BUFFER_FOR_PLAYBACK_MS,
            bufferForPlaybackAfterRebufferMs: Int = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
            targetBufferBytes: Int = DEFAULT_TARGET_BUFFER_BYTES,
            prioritizeTimeOverSizeThresholds: Boolean = DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS) : this(
            allocator,
            minBufferMs,
            maxBufferMs,
            bufferForPlaybackMs,
            bufferForPlaybackAfterRebufferMs,
            targetBufferBytes,
            prioritizeTimeOverSizeThresholds, null)

    @Deprecated("Use {@link Builder} instead. ")
    constructor(
            allocator: DefaultAllocator,
            minBufferMs: Int,
            maxBufferMs: Int,
            bufferForPlaybackMs: Int,
            bufferForPlaybackAfterRebufferMs: Int,
            targetBufferBytes: Int,
            prioritizeTimeOverSizeThresholds: Boolean,
            priorityTaskManager: PriorityTaskManager?) : this(
            allocator,
            minBufferMs,
            maxBufferMs,
            bufferForPlaybackMs,
            bufferForPlaybackAfterRebufferMs,
            targetBufferBytes,
            prioritizeTimeOverSizeThresholds,
            priorityTaskManager,
            DEFAULT_BACK_BUFFER_DURATION_MS,
            DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME) {
    }

    init {
        assertGreaterOrEqual(bufferForPlaybackMs, 0, "bufferForPlaybackMs", "0")
        assertGreaterOrEqual(
                bufferForPlaybackAfterRebufferMs, 0, "bufferForPlaybackAfterRebufferMs", "0")
        assertGreaterOrEqual(minBufferMs, bufferForPlaybackMs, "minBufferMs", "bufferForPlaybackMs")
        assertGreaterOrEqual(
                minBufferMs,
                bufferForPlaybackAfterRebufferMs,
                "minBufferMs",
                "bufferForPlaybackAfterRebufferMs")
        assertGreaterOrEqual(maxBufferMs, minBufferMs, "maxBufferMs", "minBufferMs")
        assertGreaterOrEqual(backBufferDurationMs, 0, "backBufferDurationMs", "0")
        this.minBufferUs = C.msToUs(minBufferMs.toLong())
        this.maxBufferUs = C.msToUs(maxBufferMs.toLong())
        this.bufferForPlaybackUs = C.msToUs(bufferForPlaybackMs.toLong())
        this.bufferForPlaybackAfterRebufferUs = C.msToUs(bufferForPlaybackAfterRebufferMs.toLong())
        this.backBufferDurationUs = C.msToUs(backBufferDurationMs.toLong())
    }

    override fun onPrepared() {
        reset(false)
    }

    override fun onTracksSelected(renderers: Array<Renderer>, trackGroups: TrackGroupArray,
                                  trackSelections: TrackSelectionArray) {
        targetBufferSize = if (targetBufferBytesOverwrite == C.LENGTH_UNSET)
            calculateTargetBufferSize(renderers, trackSelections)
        else
            targetBufferBytesOverwrite
        allocator.setTargetBufferSize(targetBufferSize)
    }

    override fun onStopped() {
        reset(true)
    }

    override fun onReleased() {
        reset(true)
    }

    override fun getAllocator(): Allocator {
        return allocator
    }

    override fun getBackBufferDurationUs(): Long {
        return backBufferDurationUs
    }

    override fun retainBackBufferFromKeyframe(): Boolean {
        return retainBackBufferFromKeyframe
    }

    override fun shouldContinueLoading(bufferedDurationUs: Long, playbackSpeed: Float): Boolean {
        val targetBufferSizeReached = allocator.totalBytesAllocated >= targetBufferSize
        val wasBuffering = isBuffering
        var minBufferUs = this.minBufferUs
        if (playbackSpeed > 1) {
            // The playback speed is faster than real time, so scale up the minimum required media
            // duration to keep enough media buffered for a playout duration of minBufferUs.
            val mediaDurationMinBufferUs = Util.getMediaDurationForPlayoutDuration(minBufferUs, playbackSpeed)
            minBufferUs = Math.min(mediaDurationMinBufferUs, maxBufferUs)
        }
        if (bufferedDurationUs < minBufferUs) {
            isBuffering = prioritizeTimeOverSizeThresholds || !targetBufferSizeReached
        } else if (bufferedDurationUs >= maxBufferUs || targetBufferSizeReached) {
            isBuffering = false
        } // Else don't change the buffering state
        if (priorityTaskManager != null && isBuffering != wasBuffering) {
            if (isBuffering) {
                priorityTaskManager.add(C.PRIORITY_PLAYBACK)
            } else {
                priorityTaskManager.remove(C.PRIORITY_PLAYBACK)
            }
        }
        return isBuffering
    }

    override fun shouldStartPlayback(
            bufferedDurationUs: Long, playbackSpeed: Float, rebuffering: Boolean): Boolean {
        var bufferedDurationUs = bufferedDurationUs
        bufferedDurationUs = Util.getPlayoutDurationForMediaDuration(bufferedDurationUs, playbackSpeed)
        val minBufferDurationUs = if (rebuffering) bufferForPlaybackAfterRebufferUs else bufferForPlaybackUs
        return (minBufferDurationUs <= 0
                || bufferedDurationUs >= minBufferDurationUs
                || !prioritizeTimeOverSizeThresholds && allocator.totalBytesAllocated >= targetBufferSize)
    }

    /**
     * Calculate target buffer size in bytes based on the selected tracks. The player will try not to
     * exceed this target buffer. Only used when `targetBufferBytes` is [C.LENGTH_UNSET].
     *
     * @param renderers The renderers for which the track were selected.
     * @param trackSelectionArray The selected tracks.
     * @return The target buffer size in bytes.
     */
    protected fun calculateTargetBufferSize(
            renderers: Array<Renderer>, trackSelectionArray: TrackSelectionArray): Int {
        var targetBufferSize = 0
        for (i in renderers.indices) {
            if (trackSelectionArray.get(i) != null) {
                targetBufferSize += Util.getDefaultBufferSize(renderers[i].trackType)
            }
        }
        return targetBufferSize
    }

    private fun reset(resetAllocator: Boolean) {
        targetBufferSize = 0
        if (priorityTaskManager != null && isBuffering) {
            priorityTaskManager.remove(C.PRIORITY_PLAYBACK)
        }
        isBuffering = false
        if (resetAllocator) {
            allocator.reset()
        }
    }

    companion object {

        /**
         * The default minimum duration of media that the player will attempt to ensure is buffered at all
         * times, in milliseconds.
         */
        val DEFAULT_MIN_BUFFER_MS = 10000

        /**
         * The default maximum duration of media that the player will attempt to buffer, in milliseconds.
         */
        val DEFAULT_MAX_BUFFER_MS = 50000

        /**
         * The default duration of media that must be buffered for playback to start or resume following a
         * user action such as a seek, in milliseconds.
         */
        val DEFAULT_BUFFER_FOR_PLAYBACK_MS = 2500

        /**
         * The default duration of media that must be buffered for playback to resume after a rebuffer, in
         * milliseconds. A rebuffer is defined to be caused by buffer depletion rather than a user action.
         */
        val DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5000

        /**
         * The default target buffer size in bytes. The value ([C.LENGTH_UNSET]) means that the load
         * control will calculate the target buffer size based on the selected tracks.
         */
        val DEFAULT_TARGET_BUFFER_BYTES = C.LENGTH_UNSET

        /** The default prioritization of buffer time constraints over size constraints.  */
        val DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS = true

        /** The default back buffer duration in milliseconds.  */
        val DEFAULT_BACK_BUFFER_DURATION_MS = 0

        /** The default for whether the back buffer is retained from the previous keyframe.  */
        val DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME = false

        private fun assertGreaterOrEqual(value1: Int, value2: Int, name1: String, name2: String) {
            Assertions.checkArgument(value1 >= value2, "$name1 cannot be less than $name2")
        }
    }
}
