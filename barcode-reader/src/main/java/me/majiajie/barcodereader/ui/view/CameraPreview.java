package me.majiajie.barcodereader.ui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * 相机预览视图
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    /**
     * 预览图宽的最大值
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * 预览图高的最大值
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private Context mContext;
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private CameraPreviewListener mCameraPreviewListener;

    public void setCameraPreviewListener(CameraPreviewListener listener) {
        mCameraPreviewListener = listener;
    }

    public interface CameraPreviewListener {

        /**
         * 相机预览视图开始
         */
        void onStartPreview();
    }

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // 判断屏幕显示方向是否垂直
            boolean isVertical = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            // 设置预览视图的宽高
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = getCloselyPreSize(isVertical, holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height(), MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, parameters.getSupportedPreviewSizes());
            holder.setFixedSize(size.width, size.height);

            // 启动预览
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null) {
            return;
        }

        // 修改相机参数之前先停止预览
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // 忽略停止预览时的异常
        }

        try {
            // 启动预览
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

            if (mCameraPreviewListener != null) {
                mCameraPreviewListener.onStartPreview();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // nothing.
    }

    /**
     * 获取和显示视图宽高最接近的预览图尺寸
     *
     * @param vertical      布局是否垂直
     * @param surfaceWidth  预览视图的宽
     * @param surfaceHeight 预览视图的高
     * @param maxWidth      限制最大宽度
     * @param maxHeight     限制最大高度
     * @param preSizeList   手机相机支持的预览图尺寸列表
     */
    private Camera.Size getCloselyPreSize(boolean vertical, int surfaceWidth, int surfaceHeight, int maxWidth, int maxHeight, List<Camera.Size> preSizeList) {

        int tmpWidth;
        int tmpHeight;
        if (vertical) {// 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
            tmpWidth = surfaceHeight;
            tmpHeight = surfaceWidth;
        } else {
            tmpWidth = surfaceWidth;
            tmpHeight = surfaceHeight;
        }

        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Camera.Size size : preSizeList) {
            if ((size.width == tmpWidth) && (size.height == tmpHeight)) {
                return size;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) tmpWidth) / tmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            if (size.width <= maxWidth && size.height <= maxHeight) {
                curRatio = ((float) size.width) / size.height;
                deltaRatio = Math.abs(reqRatio - curRatio);
                if (deltaRatio < deltaRatioMin) {
                    deltaRatioMin = deltaRatio;
                    retSize = size;
                }
            }
        }

        return retSize;
    }
}
