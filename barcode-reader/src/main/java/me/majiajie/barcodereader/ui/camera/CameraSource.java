package me.majiajie.barcodereader.ui.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.majiajie.barcodereader.R;
import me.majiajie.barcodereader.decode.DecodeHandlerHelper;

/**
 * 相机参数设置管理
 */
public class CameraSource {

    @StringDef({
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
            Camera.Parameters.FOCUS_MODE_AUTO,
            Camera.Parameters.FOCUS_MODE_EDOF,
            Camera.Parameters.FOCUS_MODE_FIXED,
            Camera.Parameters.FOCUS_MODE_INFINITY,
            Camera.Parameters.FOCUS_MODE_MACRO
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface FocusMode {}

    @StringDef({
            Camera.Parameters.FLASH_MODE_ON,
            Camera.Parameters.FLASH_MODE_OFF,
            Camera.Parameters.FLASH_MODE_AUTO,
            Camera.Parameters.FLASH_MODE_RED_EYE,
            Camera.Parameters.FLASH_MODE_TORCH
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface FlashMode {}

    /**
     * 相机支持预览尺寸和图片尺寸的纵横比最小容差
     */
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;

    private Context mContext;

    private final Object mCameraLock = new Object();

    private Camera mCamera;

    /**
     * 用于控制摄像头是前置还是后置的
     */
    private int mFacing;

    /**
     * FPS
     */
    private float mRequestedFps;

    /**
     * 对焦模式(默认连续对焦)
     */
    private String mFocusMode;

    /**
     * 最大预览尺寸限制
     */
    private float mMaxPreviewWidth;
    private float mMaxPreviewHeight;

    /**
     * 预览视图的宽高(期望的最佳预览尺寸)
     */
    private int mRequestedPreviewWidth;
    private int mRequestedPreviewHeight;

    /**
     * 预览尺寸
     */
    private Camera.Size mPreviewSize;

    /**
     * 旋转角度
     */
    private int mRotation;

    /**
     * 闪光灯模式
     */
    private String mFlashMode;

    /**
     * 相机字节流缓冲
     */
    private Map<byte[], ByteBuffer> mBytesToByteBuffer = new HashMap<>();

    /**
     * 相机预览帧解码处理的相关线程和运行实例.
     */
    private Thread mProcessingThread;
    private FrameProcessingRunnable mFrameProcessor;

    public static class Builder {

        private final DecodeHandlerHelper mDecoder;
        private CameraSource mCameraSource;

        public Builder(@NonNull Context context, @NonNull DecodeHandlerHelper decoder) {
            mDecoder = decoder;
            mCameraSource = new CameraSource(context);
            mCameraSource.mMaxPreviewWidth = 2160;
            mCameraSource.mMaxPreviewHeight = 1080;
            mCameraSource.mRequestedFps = 30.0f;
            mCameraSource.mFocusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
            mCameraSource.mFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
            mCameraSource.mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
        }

        /**
         * 设置相机预览帧的最大宽高限制,默认 2160x1080
         */
        public Builder setMaxPreviewSize(int width, int height) {
            final int MAX = 1000000;
            if ((width <= 0) || (width > MAX) || (height <= 0) || (height > MAX)) {
                throw new IllegalArgumentException("Invalid max size: " + width + "x" + height);
            }
            mCameraSource.mMaxPreviewWidth = width;
            mCameraSource.mMaxPreviewHeight = height;
            return this;
        }

        /**
         * 设置FPS,默认30
         */
        public Builder setRequestedFps(float fps) {
            if (fps <= 0) {
                throw new IllegalArgumentException("Invalid fps: " + fps);
            }
            mCameraSource.mRequestedFps = fps;
            return this;
        }

        /**
         * 设置变焦模式,默认{@link Camera.Parameters#FOCUS_MODE_CONTINUOUS_PICTURE}
         */
        public Builder setFocusMode(@FocusMode String mode) {
            mCameraSource.mFocusMode = mode;
            return this;
        }

        /**
         * 设置使用后置还是前置摄像头,默认true后置
         */
        public Builder setFacing(boolean facingBack) {
            mCameraSource.mFacing = facingBack ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
            return this;
        }

        /**
         * 构建完成
         */
        public CameraSource build() {
            mCameraSource.mFrameProcessor = mCameraSource.new FrameProcessingRunnable(mDecoder);
            return mCameraSource;
        }
    }

    private CameraSource(Context context) {
        mContext = context;
    }

    /**
     * 暂停相机预览并释放资源.
     */
    public void release() {
        synchronized (mCameraLock) {
            stop();
            mFrameProcessor.release();
        }
    }

    /**
     * 开始预览
     */
    public CameraSource start(SurfaceHolder surfaceHolder) throws IOException {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                return this;
            }

            mRequestedPreviewWidth = surfaceHolder.getSurfaceFrame().width();
            mRequestedPreviewHeight = surfaceHolder.getSurfaceFrame().height();

            mCamera = createCamera();
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();

            mProcessingThread = new Thread(mFrameProcessor);
            mFrameProcessor.setActive(true);
            mProcessingThread.start();
        }
        return this;
    }

    /**
     * 暂停预览
     */
    public void stop() {
        synchronized (mCameraLock) {
            mFrameProcessor.setActive(false);
            if (mProcessingThread != null) {
                try {
                    // 等待线程执行完成
                    mProcessingThread.join();
                } catch (InterruptedException e) {
                    // nothing
                }
                mProcessingThread = null;
            }

            // 清除缓冲区
            mBytesToByteBuffer.clear();

            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                try {
                    mCamera.setPreviewTexture(null);
                } catch (Exception e) {
                    // 清除相机预览视图异常
                }
                mCamera.release();
                mCamera = null;
            }
        }
    }

    /**
     * 返回当前的预览大小
     */
    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    /**
     * 获取当前闪光灯模式
     */
    @Nullable
    @FlashMode
    public String getFlashMode() {
        return mFlashMode;
    }

    /**
     * 设置闪光灯模式
     * @return true 表示设置成功
     */
    public boolean setFlashMode(@FlashMode String mode) {
        synchronized (mCameraLock) {
            if (mCamera != null && mode != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getSupportedFlashModes().contains(mode)) {
                    parameters.setFlashMode(mode);
                    mCamera.setParameters(parameters);
                    mFlashMode = mode;
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * 打开相机
     */
    @SuppressLint("InlinedApi")
    private Camera createCamera() {
        int requestedCameraId = getIdForRequestedCamera(mFacing);
        if (requestedCameraId == -1) {
            throw new RuntimeException(mContext.getString(R.string.no_rear_camera));
        }
        Camera camera = Camera.open(requestedCameraId);

        // 计算相机预览尺寸和图片只存
        SizePair sizePair = selectSizePair(camera, mRequestedPreviewWidth, mRequestedPreviewHeight);
        if (sizePair == null) {
            throw new RuntimeException("Could not find suitable preview size.");
        }
        Camera.Size pictureSize = sizePair.pictureSize();
        mPreviewSize = sizePair.previewSize();

        int[] previewFpsRange = selectPreviewFpsRange(camera, mRequestedFps);
        if (previewFpsRange == null) {
            throw new RuntimeException("Could not find suitable preview frames per second range.");
        }

        Camera.Parameters parameters = camera.getParameters();

        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
        }

        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        parameters.setPreviewFpsRange(
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        parameters.setPreviewFormat(ImageFormat.NV21);

        setRotation(camera, parameters, requestedCameraId);

        // 对焦模式
        if (mFocusMode != null) {
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null && focusModes.contains(mFocusMode)) {
                parameters.setFocusMode(mFocusMode);
            }
        }

        camera.setParameters(parameters);

        // Four frame buffers are needed for working with the camera:
        //
        //   one for the frame that is currently being executed upon in doing detection
        //   one for the next pending frame to process immediately upon completing detection
        //   two for the frames that the camera uses to populate future preview images
        camera.setPreviewCallbackWithBuffer(new CameraPreviewCallback());
        camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));

        return camera;
    }

    /**
     * 获取摄像头ID,如果不存在就返回-1
     *
     * @param facing {@link Camera.CameraInfo#CAMERA_FACING_BACK} or {@link Camera.CameraInfo#CAMERA_FACING_FRONT}
     */
    private int getIdForRequestedCamera(int facing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 选择合适的预览尺寸
     */
    private SizePair selectSizePair(Camera camera, int desiredWidth, int desiredHeight) {

        List<SizePair> validPreviewSizes = generateValidPreviewSizeList(camera);

        int tmpWidth = desiredWidth;
        int tmpHeight = desiredHeight;
        if (isPortraitMode()) {
            tmpWidth = desiredHeight;
            tmpHeight = desiredWidth;
        }

        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (SizePair sizePair : validPreviewSizes) {
            if ((sizePair.previewSize().width == tmpWidth) &&
                    (sizePair.previewSize().height == tmpHeight)) {
                return sizePair;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) tmpWidth) / tmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        SizePair selectedPair = null;
        for (SizePair sizePair : validPreviewSizes) {
            Camera.Size size = sizePair.mPreview;
            if (size.width <= mMaxPreviewWidth && size.height <= mMaxPreviewHeight) {
                curRatio = ((float) size.width) / size.height;
                deltaRatio = Math.abs(reqRatio - curRatio);
                if (deltaRatio < deltaRatioMin) {
                    deltaRatioMin = deltaRatio;
                    selectedPair = sizePair;
                }
            }
        }

        return selectedPair;
    }

    /**
     * 从相机始终提取合适的预览尺寸与图片尺寸的集合
     */
    private static List<SizePair> generateValidPreviewSizeList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes =
                parameters.getSupportedPreviewSizes();
        List<android.hardware.Camera.Size> supportedPictureSizes =
                parameters.getSupportedPictureSizes();
        List<SizePair> validPreviewSizes = new ArrayList<>();
        for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;

            // 从支持的图片尺寸中选择最大的与预览尺寸纵横比相同的尺寸
            for (android.hardware.Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new SizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        // 如果不存在和预览尺寸同比例的图片尺寸,就应用所有的预览尺寸.
        if (validPreviewSizes.size() == 0) {
            for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
                // 这里图片尺寸为空
                validPreviewSizes.add(new SizePair(previewSize, null));
            }
        }
        return validPreviewSizes;
    }

    /**
     * 选择合适的预览帧.
     */
    private int[] selectPreviewFpsRange(Camera camera, float desiredPreviewFps) {
        int desiredPreviewFpsScaled = (int) (desiredPreviewFps * 1000.0f);

        int[] selectedFpsRange = null;
        int minDiff = Integer.MAX_VALUE;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();
        for (int[] range : previewFpsRangeList) {
            int deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int diff = Math.abs(deltaMin) + Math.abs(deltaMax);
            if (diff < minDiff) {
                selectedFpsRange = range;
                minDiff = diff;
            }
        }
        return selectedFpsRange;
    }

    /**
     * 设置摄像头的旋转
     */
    private void setRotation(Camera camera, Camera.Parameters parameters, int cameraId) {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        int degrees = 0;
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int angle;
        int displayAngle;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 前置摄像头
            angle = (cameraInfo.orientation + degrees) % 360;
            displayAngle = (360 - angle) % 360; // 前置摄像头镜像处理
        } else {  // 后置摄像头
            angle = (cameraInfo.orientation - degrees + 360) % 360;
            displayAngle = angle;
        }

        mRotation = angle / 90;

        camera.setDisplayOrientation(displayAngle);
        parameters.setRotation(angle);
    }

    /**
     * 创建预览缓存.
     *
     * @return 按相机的参数返回一个合适的缓存字节数组
     */
    private byte[] createPreviewBuffer(Camera.Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = previewSize.height * previewSize.width * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }

        mBytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }

    /**
     * 判断是否为垂直布局
     */
    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        return orientation != Configuration.ORIENTATION_LANDSCAPE && orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 相机预览尺寸模型
     */
    private static class SizePair {
        private Camera.Size mPreview;
        private Camera.Size mPicture;

        SizePair(Camera.Size previewSize, Camera.Size pictureSize) {
            mPreview = previewSize;
            mPicture = pictureSize;
        }

        Camera.Size previewSize() {
            return mPreview;
        }

        Camera.Size pictureSize() {
            return mPicture;
        }
    }

    /**
     * 相机预览帧回调.
     */
    private class CameraPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mFrameProcessor.setNextFrame(data, camera);
        }
    }

    private class FrameProcessingRunnable implements Runnable {

        private final Object mFrameLock = new Object();
        private boolean mActive = true;

        private ByteBuffer mPendingFrameData;

        private DecodeHandlerHelper mDecoder;

        FrameProcessingRunnable(DecodeHandlerHelper decoder) {
            mDecoder = decoder;
            mDecoder.start();
        }

        @SuppressLint("Assert")
        void release() {
            assert (mProcessingThread.getState() == Thread.State.TERMINATED);
            mDecoder.stopThread();
            mDecoder = null;
        }

        /**
         * 设置是否可以执行
         */
        void setActive(boolean active) {
            synchronized (mFrameLock) {
                mActive = active;
                mFrameLock.notifyAll();
            }
        }

        /**
         * 设置下一帧的数据
         */
        void setNextFrame(byte[] data, Camera camera) {
            synchronized (mFrameLock) {
                if (mPendingFrameData != null) {
                    camera.addCallbackBuffer(mPendingFrameData.array());
                    mPendingFrameData = null;
                }

                if (!mBytesToByteBuffer.containsKey(data)) {
                    // 相机预览帧与缓存数据不匹配
                    return;
                }

                mPendingFrameData = mBytesToByteBuffer.get(data);

                mFrameLock.notifyAll();
            }
        }

        @Override
        public void run() {
            ByteBuffer data;
            while (true) {
                synchronized (mFrameLock) {
                    while (mActive && (mPendingFrameData == null)) {
                        try {
                            // 等待下一帧.
                            mFrameLock.wait();
                        } catch (InterruptedException e) {
                            // 线程终止,退出循环
                            return;
                        }
                    }

                    if (!mActive) {
                        // 检查是否需要执行.
                        return;
                    }

                    data = mPendingFrameData;
                    mPendingFrameData = null;
                }

                byte[] array = data.array();

                mDecoder.decode(array, mPreviewSize.width, mPreviewSize.height, new Rect(0, 0, mPreviewSize.width, mPreviewSize.height), true);

                mCamera.addCallbackBuffer(array);
            }
        }
    }

}
