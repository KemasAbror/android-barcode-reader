package me.majiajie.barcodereader.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.majiajie.barcodereader.R;
import me.majiajie.barcodereader.decode.DecodeCallBackHandler;
import me.majiajie.barcodereader.decode.DecodeHandlerHelper;
import me.majiajie.barcodereader.helper.RotationEventHelper;
import me.majiajie.barcodereader.ui.view.CameraPreview;
import me.majiajie.barcodereader.ui.view.ScanView;

/**
 * 相机预览和图像解码
 */
public class ScanFragment extends Fragment implements ScanController {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        // 相机90度对应正常的垂直方向
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    protected Context mContext;

    // 相机预览视图的容器（父布局）,如果没有权限就不需要添加相机预览视图了
    protected FrameLayout mCameraPreview;
    // 预览层之上的扫描框UI
    protected ScanView mScanView;
    // 闪光灯开关
    private CheckBox mCheckBoxLight;

    /**
     * 解码格式列表
     */
    private Collection<BarcodeFormat> mBarcodeFormatList;

    /**
     * 屏幕旋转事件监听帮助
     */
    private RotationEventHelper mRotationEventHelper;

    /**
     * 摄像头控制实例
     */
    private Camera mCamera;

    /**
     * 判断是否支持闪光灯
     */
    private boolean mFlashSupported;

    /**
     * 解码回调
     */
    private DecodeCallBackHandler.Callback mDecodeCallBack;

    /**
     * 持续性解码帮助类
     */
    protected DecodeHandlerHelper mDecodeHandlerHelper;

    /**
     * 闪光灯开关事件
     */
    private CompoundButton.OnCheckedChangeListener mFlashListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!openFlash(isChecked)) {
                buttonView.setChecked(!isChecked);
                Toast.makeText(mContext, R.string.hint_no_flash, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 相机预览视图启动事件
     */
    private CameraPreview.CameraPreviewListener mCameraPreviewListener = new CameraPreview.CameraPreviewListener() {
        @Override
        public void onStartPreview() {
            scanAgain();
        }
    };

    /**
     * 屏幕方向旋转事件(不主动控制旋转事件，只在直接旋转180度时起作用)
     */
    private RotationEventHelper.RotationEventListener mRotationEventListener = new RotationEventHelper.RotationEventListener() {
        @Override
        public void onRotationChanged(int rotation) {
            if (mCamera != null) {
                mCamera.setDisplayOrientation(ORIENTATIONS.get(rotation));
            }
        }
    };

    private static final String ARG_FORMATS = "ARG_FORMATS";

    public static ScanFragment newInstance(int[] barcodeFormats) {
        Bundle args = new Bundle();
        args.putIntArray(ARG_FORMATS,barcodeFormats);
        ScanFragment fragment = new ScanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof DecodeCallBackHandler.Callback) {
            mDecodeCallBack = (DecodeCallBackHandler.Callback) context;
        } else {
            throw new ClassCastException(context.toString() + " must implements DecodeCallBackHandler.Callback");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null){
            int[] formats = bundle.getIntArray(ARG_FORMATS);
            mBarcodeFormatList = new ArrayList<>();
            if (formats != null && formats.length > 0) {
                for (int n : formats) {
                    mBarcodeFormatList.add(me.majiajie.barcodereader.BarcodeFormat.getZxingFormat(n));
                }
            } else {// 如果传入数据为空,就默认扫描二维码和CODE128
                mBarcodeFormatList.add(BarcodeFormat.QR_CODE);
                mBarcodeFormatList.add(BarcodeFormat.CODE_128);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCameraPreview = view.findViewById(R.id.camera_preview);
        mScanView = view.findViewById(R.id.scanView);
        mCheckBoxLight = view.findViewById(R.id.checkBox_light);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initDecodeThread();
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDecodeHandlerHelper.start();
        startPreview();

        // 手机旋转角度监听
        mRotationEventHelper = new RotationEventHelper(mContext);
        mRotationEventHelper.setListener(mRotationEventListener);
        mRotationEventHelper.enable();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRotationEventHelper.disable();
        mRotationEventHelper = null;
        mCheckBoxLight.setChecked(false);
        releaseCamera();
        mDecodeHandlerHelper.stopThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    @Override
    public void scanAgain() {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    int width = size.width;
                    int height = size.height;
                    boolean isVertical = mContext.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    Rect rect = mScanView.getFramingRect(width, height, isVertical);
                    mDecodeHandlerHelper.decode(data, width, height, rect,isVertical);
                }
            });
        }
    }

    /**
     * 初始化解码线程
     */
    private void initDecodeThread() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);

        // 设置解码类型
        hints.put(DecodeHintType.POSSIBLE_FORMATS, mBarcodeFormatList);
        hints.put(DecodeHintType.TRY_HARDER, false);

        mDecodeHandlerHelper = new DecodeHandlerHelper(mDecodeCallBack, hints);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        mCheckBoxLight.setOnCheckedChangeListener(mFlashListener);
    }

    /**
     * 闪光灯控制
     * @param open true 打开闪光灯
     */
    private boolean openFlash(boolean open) {
        if (mFlashSupported && mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(open ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
            return true;
        }
        return false;
    }

    /**
     * 开始显示相机预览
     */
    private void startPreview() {
        if (mCamera == null) {
            // 创建后置摄像头实例
            mCamera = CameraUtils.getCameraInstance();

            //后置摄像头不存在或异常
            if (mCamera == null) {
                Toast.makeText(mContext, R.string.hint_no_background_camera, Toast.LENGTH_SHORT).show();
                return;
            }

            // 根据屏幕方向做调整
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                int displayRotation = windowManager.getDefaultDisplay().getRotation();
                // 修改相机方向
                mCamera.setDisplayOrientation(ORIENTATIONS.get(displayRotation));
            }

            Camera.Parameters params = mCamera.getParameters();
            // 检查是否支持连续对焦,支持就设置
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(params);
                mCamera.cancelAutoFocus();
            }

            // 检查是否支持闪光灯
            List<String> flashModes = params.getSupportedFlashModes();
            if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH) &&
                    flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                mFlashSupported = true;
            }

            // 创建预览视图
            CameraPreview preview = new CameraPreview(mContext, mCamera);
            preview.setCameraPreviewListener(mCameraPreviewListener);
            // 添加到布局
            mCameraPreview.addView(preview);
        }
    }

    /**
     * 释放相机
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mCameraPreview.removeAllViews();
        }
    }
}
