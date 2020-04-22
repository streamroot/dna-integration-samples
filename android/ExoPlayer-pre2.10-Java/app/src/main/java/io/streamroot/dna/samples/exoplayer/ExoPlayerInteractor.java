package io.streamroot.dna.samples.exoplayer;

import android.os.Looper;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Timeline;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.TimeRange;

public final class ExoPlayerInteractor implements PlayerInteractor {

    private interface BufferTargetBridge {
        double bufferTarget();
        void setBufferTarget(double bufferTarget);
    }

    private static abstract class LoadControlBufferTargetBridge implements BufferTargetBridge {
        private static final String MAX_BUFFER_FIELD_NAME = "maxBufferUs";

        private final LoadControl loadControl;

        private final Field maxBufferField;
        private final long minBufferUs;

        private LoadControlBufferTargetBridge(LoadControl loadControl, long minBufferUs) {
            this.loadControl = loadControl;
            maxBufferField = getAccessibleFieldElseThrow(loadControl.getClass(), MAX_BUFFER_FIELD_NAME);
            this.minBufferUs = minBufferUs;
        }

        private static Field getAccessibleFieldElseThrow(Class<?> clazz, String fieldName) {
            try {
                final Field minBufferField = clazz.getDeclaredField(fieldName);
                minBufferField.setAccessible(true);
                return minBufferField;
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Impossible to retrieve field `%s` value from LoadControl of type `%s`",
                        fieldName, LoadControl.class.getSimpleName()));
            }
        }

        static long getLongFromFieldElseThrow(LoadControl loadControl, String fieldName) {
            try {
                return getAccessibleFieldElseThrow(loadControl.getClass(), fieldName).getLong(loadControl);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Impossible to retrieve long field `%s` value from LoadControl of type `%s`",
                        fieldName, LoadControl.class.getSimpleName()));
            }
        }

        @Override
        public double bufferTarget() {
            try {
                return (double) TimeUnit.MICROSECONDS.toSeconds(maxBufferField.getLong(loadControl));
            } catch (Exception e) {
                return 0.0;
            }
        }

        @Override
        public void setBufferTarget(double bufferTarget) {
            final long maxBufferUs = TimeUnit.SECONDS.toMicros((long)bufferTarget);
            if (maxBufferUs > minBufferUs) try {
                maxBufferField.setLong(
                        loadControl,
                        maxBufferUs
                );
            } catch (Exception ignored) {}
        }
    }

    private static final class LoadControlBufferTargetBridgeV1 extends LoadControlBufferTargetBridge {
        private static final String MIN_BUFFER_FIELD_NAME = "minBufferUs";

        private LoadControlBufferTargetBridgeV1(LoadControl loadControl) {
            super(
                    loadControl,
                    getLongFromFieldElseThrow(loadControl, MIN_BUFFER_FIELD_NAME)
            );
        }
    }

    private static final class LoadControlBufferTargetBridgeV2 extends LoadControlBufferTargetBridge {
        private static final String MIN_BUFFER_AUDIO_FIELD_NAME = "minBufferAudioUs";
        private static final String MIN_BUFFER_VIDEO_FIELD_NAME = "minBufferVideoUs";

        private LoadControlBufferTargetBridgeV2(LoadControl loadControl, boolean audioOnly) {
            super(
                    loadControl,
                    getLongFromFieldElseThrow(
                            loadControl,
                            audioOnly ? MIN_BUFFER_AUDIO_FIELD_NAME : MIN_BUFFER_VIDEO_FIELD_NAME
                    )
            );
        }
    }

    private static final class BufferTargetBridgeFactory {
        private static BufferTargetBridge createInteractor(LoadControl loadControl, boolean audioOnly) {
            try {
                return new LoadControlBufferTargetBridgeV1(loadControl);
            } catch (Exception ignored) {
                try {
                    return new LoadControlBufferTargetBridgeV2(loadControl, audioOnly);
                } catch (Exception e) {
                    throw new RuntimeException("Unsupported ExoPlayer version");
                }
            }
        }
    }

    private final List<TimeRange> emptyTRList = Collections.unmodifiableList(Collections.emptyList());

    private final ExoPlayer player;
    private final BufferTargetBridge bridge;

    public ExoPlayerInteractor(ExoPlayer player, LoadControl loadControl, boolean audioOnly) {
        this.player = player;
        this.bridge = BufferTargetBridgeFactory.createInteractor(loadControl, audioOnly);
    }

    public ExoPlayerInteractor(ExoPlayer player, LoadControl loadControl) {
        this(player, loadControl, false);
    }

    @Override public Looper looper() { return player.getApplicationLooper(); }

    @NotNull
    @Override
    public List<TimeRange> loadedTimeRanges() {
        final long shift = getCurrentWindowShift();
        final long rangeDurationMs = player.getBufferedPosition() - player.getCurrentPosition();

        if (rangeDurationMs > 0) {
            return Collections.singletonList(new TimeRange(shift + player.getCurrentPosition(), rangeDurationMs));
        }
        return emptyTRList;
    }

    private long getCurrentWindowShift() {
        final Timeline current = player.getCurrentTimeline();
        final Timeline.Window timelineWindow = new Timeline.Window();

        if (player.getCurrentWindowIndex() < current.getWindowCount()) {
            current.getWindow(player.getCurrentWindowIndex(), timelineWindow);
            return timelineWindow.getPositionInFirstPeriodMs();
        }
        return 0L;
    }

    @Override public long playbackTime() { return getCurrentWindowShift() + player.getCurrentPosition(); }
    @Override public double bufferTarget() { return bridge.bufferTarget(); }
    @Override public void setBufferTarget(double bufferTarget) { bridge.setBufferTarget(bufferTarget); }
}
