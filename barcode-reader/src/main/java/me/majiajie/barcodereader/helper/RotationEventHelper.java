package me.majiajie.barcodereader.helper;

import android.content.Context;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

/**
 * 屏幕旋转监听
 */
public class RotationEventHelper extends OrientationEventListener {

    private Context mContext;

    private RotationEventListener mListener;

    private int mRotation;

    public void setListener(RotationEventListener listener) {
        mListener = listener;
    }

    public interface RotationEventListener {

        /**
         * 旋转方向变更
         *
         * @param rotation {@link android.view.Surface#ROTATION_0 ROTATION_0}、{@link android.view.Surface#ROTATION_180 ROTATION_180}、
         *                 {@link android.view.Surface#ROTATION_270 ROTATION_270}、{@link android.view.Surface#ROTATION_90 ROTATION_90}、
         */
        void onRotationChanged(int rotation);

    }

    public RotationEventHelper(Context context) {
        super(context);
        mContext = context;
        mRotation = getWindowRotation();
    }

    @Override
    public void enable() {
        super.enable();

    }

    @Override
    public void disable() {
        super.disable();
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation < 0) {
            return;
        }

        int newRotation;
        if (orientation >= 0 && orientation <= 60) {
            newRotation = Surface.ROTATION_0;
        } else if (orientation > 60 && orientation < 120) {
            newRotation = Surface.ROTATION_270;
        } else if (orientation >= 120 && orientation <= 240) {
            newRotation = Surface.ROTATION_180;
        } else if (orientation > 240 && orientation < 300) {
            newRotation = Surface.ROTATION_90;
        } else if (orientation >= 300 && orientation <= 360) {
            newRotation = Surface.ROTATION_0;
        } else {
            return;
        }

        if (mListener != null && mRotation != newRotation  && getWindowRotation() == newRotation) {
            mRotation = newRotation;
            mListener.onRotationChanged(mRotation);
        }
    }

    private int getWindowRotation() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            return windowManager.getDefaultDisplay().getRotation();
        }
        return Surface.ROTATION_0;
    }
}
