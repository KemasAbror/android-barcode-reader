package me.majiajie.barcodereader.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import me.majiajie.barcodereader.R;
import me.majiajie.barcodereader.decode.DecodeCallback;
import me.majiajie.barcodereader.decode.DecodeHandlerHelper;
import me.majiajie.barcodereader.decode.DecodeResult;
import me.majiajie.barcodereader.ui.camera.CameraSource;
import me.majiajie.barcodereader.ui.camera.CameraSourcePreview;
import me.majiajie.barcodereader.ui.view.ScanView;

/**
 * 相机预览和图像解码
 */
public class ScanFragment extends Fragment implements ScanController{

    private static final String ARG_FORMATS = "ARG_FORMATS";

    public static ScanFragment newInstance(int[] barcodeFormats) {
        Bundle args = new Bundle();
        args.putIntArray(ARG_FORMATS,barcodeFormats);
        ScanFragment fragment = new ScanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final int REQUEST_CAMERA_PERMISSION = 10086;

    private Context mContext;
    private Activity mActivity;

    /**
     * 相机预览视图
     */
    protected CameraSourcePreview mCameraPreview;

    /**
     * 预览层之上的扫描框UI
     */
    protected ScanView mScanView;

    /**
     * 闪光灯开关
     */
    private CheckBox mCheckBoxLight;

    /**
     * 解码格式列表
     */
    private Collection<BarcodeFormat> mBarcodeFormatList;

    /**
     * 相机管理
     */
    private CameraSource mCameraSource;

    /**
     * 记录是否暂停
     */
    private boolean mScanOver;

    /**
     * 扫码回调
     */
    private ScanCallBack mScanCallBack;

    /**
     * 扫码回调
     */
    public interface ScanCallBack{

        /**
         * 扫码失败
         */
        void onDecodeFailed();

        /**
         * 扫码成功
         * @param result    扫码信息
         */
        void onDecodeSucceed(DecodeResult result);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
        if (context instanceof ScanCallBack) {
            mScanCallBack = (ScanCallBack) context;
        } else {
            throw new ClassCastException(context.toString() + " must implements ScanCallBack");
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

        initEvent();

        // 检查相机权限
        int rc = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 相机权限获取成功
            createCameraSource();
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mActivity.finish();
            }
        };

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.notice)
                .setMessage(R.string.dialog_hint_camera_permission)
                .setPositiveButton(android.R.string.ok, listener)
                .setCancelable(false)
                .show();
    }

    /**
     * 请求相机权限
     */
    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!shouldShowRequestPermissionRationale( Manifest.permission.CAMERA)) {
            requestPermissions(permissions, REQUEST_CAMERA_PERMISSION);
            return;
        }

        Dialog.OnClickListener listener = new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(permissions,REQUEST_CAMERA_PERMISSION);
            }
        };

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.notice)
                .setMessage(R.string.dialog_hint_camera_permission)
                .setPositiveButton(android.R.string.ok, listener)
                .setCancelable(false)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mScanOver) {
            startCameraSource();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraPreview != null) {
            mCameraPreview.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraPreview != null) {
            mCameraPreview.release();
        }
    }

    @Override
    public void startScan() {
        mScanOver = false;
        startCameraSource();
    }

    @Override
    public void stopScan() {
        mScanOver = true;
        if (mCameraPreview != null) {
            mCameraPreview.stop();
        }
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        // 闪光灯开关
        mCheckBoxLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!openFlash(isChecked)) {
                    buttonView.setChecked(!isChecked);
                    Toast.makeText(mContext, R.string.hint_no_flash, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 闪光灯控制
     * @param open true 打开闪光灯
     */
    private boolean openFlash(boolean open) {
        return mCameraSource != null && mCameraSource.setFlashMode(open ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
    }

    /**
     * 创建相机预览
     */
    private void createCameraSource(){
        mCameraPreview.release();

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        // 设置解码类型
        hints.put(DecodeHintType.POSSIBLE_FORMATS, mBarcodeFormatList);
        hints.put(DecodeHintType.TRY_HARDER, false);
        DecodeHandlerHelper decodeHandlerHelper = new DecodeHandlerHelper(new DecodeCallback() {
            @Override
            public void onDecodeFailed() {
                mScanCallBack.onDecodeFailed();
            }
            @Override
            public void onDecodeSucceed(DecodeResult result) {
                mScanCallBack.onDecodeSucceed(result);
            }
        }, hints);

        mCameraSource = new CameraSource
                .Builder(mContext.getApplicationContext(),decodeHandlerHelper)
                .setRequestedFps(15.0f)
                .build();
    }

    /**
     * 启动预览
     */
    private void startCameraSource() throws SecurityException {
        if (mCameraSource != null) {
            try {
                mCameraPreview.start(mCameraSource);
            } catch (IOException e) {
                e.printStackTrace();
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

}
