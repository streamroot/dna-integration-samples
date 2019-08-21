package io.streamroot.dna.samples.playkit;

import com.kaltura.android.exoplayer2.LoadControl;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class DynamicBufferLoadControl {
    private LoadControl mLoadControl;

    private long mMinBufferUs = -1;
    private boolean mIsSameMinMax = false;
    private boolean mIsAVTargetSplit = false;

    DynamicBufferLoadControl(LoadControl loadControl) {
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
                if (reflectedMinVideoBuffer.equals(reflectedMaxBuffer)) {
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

    public double bufferTarget() {
        Long reflectedMaxBuffer = getLoadControlPropertyLong(mLoadControl, "maxBufferUs");
        return TimeUnit.MICROSECONDS.toSeconds(reflectedMaxBuffer != null ? reflectedMaxBuffer : 0);
    }

    public void setBufferTarget(double target) {
        long targetUs = TimeUnit.SECONDS.toMicros((long) target);
        if (targetUs > mMinBufferUs) {
            if (mIsAVTargetSplit == true && mIsSameMinMax == true) {
                setLoadControlPropertyLong(mLoadControl, "minBufferVideoUs", targetUs);
            }
            setLoadControlPropertyLong(mLoadControl, "maxBufferUs", targetUs);
        }
    }
}
