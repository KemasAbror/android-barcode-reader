package me.majiajie.barcodereader.ui.camera;

import android.Manifest;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.RequiresPermission;

import java.io.IOException;

/**
 * 相机预览视图
 */
public class CameraSourcePreview extends SurfaceView implements SurfaceHolder.Callback  {

    private static final String TAG = "CameraSourcePreview";

    private Context mContext;

    /**
     * 判断是否有启动预览的请求
     */
    private boolean mStartRequested;

    /**
     * 判断SurfaceView是否准备完毕
     */
    private boolean mSurfaceAvailable;

    private CameraSource mCameraSource;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        getHolder().addCallback(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        try {
            startIfReady();
        } catch (SecurityException se) {
            Log.e(TAG,"Do not have permission to start the camera", se);
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource) throws IOException, SecurityException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startIfReady() throws IOException, SecurityException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(getHolder());
            mStartRequested = false;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surface) {
        mSurfaceAvailable = true;
        try {
            startIfReady();
        } catch (SecurityException se) {
            Log.e(TAG,"Do not have permission to start the camera", se);
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surface) {
        mSurfaceAvailable = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

//    private void updateTextureViewSizeCenterCrop(int videoWidth,int videoHeight){
//
//        float sx = (float) getWidth() / (float) videoWidth;
//        float sy = (float) getHeight() / (float) videoHeight;
//
//        Matrix matrix = new Matrix();
//        float maxScale = Math.min(sx, sy);
//
//        //第1步:把视频区移动到View区,使两者中心点重合.
//        matrix.preTranslate((getWidth() - videoWidth) / 2, (getHeight() - videoHeight) / 2);
//
//        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
//        matrix.preScale(videoWidth / (float) getWidth(), videoHeight / (float) getHeight());
//
//        //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
//        matrix.postScale(maxScale, maxScale, getWidth() / 2, getHeight() / 2);//后两个参数坐标是以整个View的坐标系以参考的
//
////        setTransform(matrix);
//        postInvalidate();
//
//        TextureView textureView = new TextureView();
//        textureView.setTransform();
//    }

}
