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

    private long mMinBufferUs;
    private Field mMaxBufferField;

    PlayKitInteractor(Player player, LoadControl loadControl) {
        this.mPlayer = player;
        this.mLoadControl = loadControl;

        this.reflectLoadControl();
    }

    private void reflectLoadControl() {
        try {
            Field minBufferField = mLoadControl.getClass().getDeclaredField("minBufferVideoUs");
            minBufferField.setAccessible(true);
            mMinBufferUs = minBufferField.getLong(mLoadControl);
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible to retrieve minBuffer field");
        }

        try {
            mMaxBufferField = mLoadControl.getClass().getDeclaredField("maxBufferUs");
            mMaxBufferField.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible to retrieve maxBuffer field");
        }
    }

    @Override
    public double bufferTarget() {
        try {
            return TimeUnit.MICROSECONDS.toSeconds(mMaxBufferField.getLong(mLoadControl));
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public void setBufferTarget(double target) {
        Long maxBufferUs = TimeUnit.SECONDS.toMicros((long) target);
        if (maxBufferUs > mMinBufferUs) {
            try {
                mMaxBufferField.setLong(mLoadControl, maxBufferUs);
            } catch (Exception e) {}
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
