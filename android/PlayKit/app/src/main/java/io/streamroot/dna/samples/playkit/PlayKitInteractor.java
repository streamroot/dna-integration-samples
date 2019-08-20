package io.streamroot.dna.samples.playkit;

import android.os.Looper;
import com.kaltura.android.exoplayer2.LoadControl;
import com.kaltura.playkit.Player;
import io.streamroot.dna.core.PlayerInteractor;
import io.streamroot.dna.core.TimeRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class PlayKitInteractor implements PlayerInteractor {
    private Player mPlayer;
    private LoadControl mLoadControl;

    private long mMinBufferUs = -1;
    private boolean mIsSameMinMax = false;
    private boolean mIsAVTargetSplit = false;

    PlayKitInteractor(Player player, LoadControl loadControl) {
        mPlayer = player;
        mLoadControl = loadControl;

        reflectLoadControl();
    }

    private Long getLoadControlPropertyLong(LoadControl loadControl, String propertyName) {
        try {
            Field loadControlField = loadControl.getClass().getDeclaredField(propertyName);
            loadControlField.setAccessible(true);
            return loadControlField.getLong(loadControl);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean setLoadControlPropertyLong(LoadControl loadControl, String propertyName, long value) {
        try {
            Field loadControlField = loadControl.getClass().getDeclaredField(propertyName);
            loadControlField.setAccessible(true);
            loadControlField.setLong(loadControl, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void reflectLoadControl() {
        Long reflectedMinBuffer = getLoadControlPropertyLong(mLoadControl, "minBufferUs");
        if (reflectedMinBuffer == null) {
            mIsAVTargetSplit = true;
            Long reflectedMaxBuffer = getLoadControlPropertyLong(mLoadControl, "maxBufferUs");
            Long reflectedMinVideoBuffer = getLoadControlPropertyLong(mLoadControl, "minBufferVideoUs");
            if (reflectedMaxBuffer != null && reflectedMinVideoBuffer != null) {
                if (reflectedMinVideoBuffer == reflectedMaxBuffer) {
                    mIsSameMinMax = true;
                } else {
                    mMinBufferUs = reflectedMinVideoBuffer;
                }
            } else {
                throw new IllegalArgumentException("Incompatible LoadControl used");
            }
        } else {
            mMinBufferUs = reflectedMinBuffer;
        }
    }

    @Override
    public double bufferTarget() {
        if (!mIsAVTargetSplit) {
            Long reflectedMinBuffer = getLoadControlPropertyLong(mLoadControl, "minBufferUs");
            return TimeUnit.MICROSECONDS.toSeconds(reflectedMinBuffer != null ? reflectedMinBuffer : 0);
        } else {
            Long reflectedMinVideoBuffer = getLoadControlPropertyLong(mLoadControl, "minBufferVideoUs");
            return TimeUnit.MICROSECONDS.toSeconds(reflectedMinVideoBuffer != null ? reflectedMinVideoBuffer : 0);
        }
    }

    @Override
    public void setBufferTarget(double target) {
        long targetUs = TimeUnit.SECONDS.toMicros((long) target);
        if (targetUs > mMinBufferUs) {
            if (!mIsAVTargetSplit) {
                setLoadControlPropertyLong(mLoadControl, "minBufferUs", targetUs);
            } else {
                setLoadControlPropertyLong(mLoadControl, "minBufferVideoUs", targetUs);
                if (mIsSameMinMax) {
                    setLoadControlPropertyLong(mLoadControl, "maxBufferUs", targetUs);
                }
            }
        }
    }

    @NotNull
    @Override
    public List<TimeRange> loadedTimeRanges() {
        long currentPosition = mPlayer.getCurrentPosition();
        long bufferedPosition = mPlayer.getBufferedPosition();
        return Collections.singletonList(new TimeRange(currentPosition, bufferedPosition));
    }

    @Nullable
    @Override
    public Looper looper() {
        return Looper.getMainLooper();
    }

    @Override
    public long playbackTime() {
        return mPlayer.getCurrentPosition();
    }
}
