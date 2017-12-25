package me.majiajie.barcodereader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import me.majiajie.barcodereader.decode.DecodeCallBackHandler;
import me.majiajie.barcodereader.decode.DecodeResult;
import me.majiajie.barcodereader.helper.RequestPermissionFragment;
import me.majiajie.barcodereader.ui.ScanController;
import me.majiajie.barcodereader.ui.ScanFragment;

/**
 * 扫码
 */
public class ScanActivity extends AppCompatActivity implements DecodeCallBackHandler.Callback, RequestPermissionFragment.RequestPermissionsCallback {

    public static final int REQUEST_CODE = 110;

    public static final String ARG_DECODE_RESULT = "ARG_DECODE_RESULT";

    public static final String ARG_THEME = "ARG_THEME";

    public static final String ARG_SCAN_FORMAT = "ARG_SCAN_FORMAT";

    /**
     * 启动扫码Activity
     *
     * @param activity   {@link Activity}
     * @param theme      主题,如果传0就使用应用默认主题
     * @param scanFormat 扫码类型{@link BarcodeFormat BarcodeFormat}. 传null就默认扫描QRCODE和CODE128.
     */
    public static void startActivityForResult(Activity activity, @StyleRes int theme, @Nullable int[] scanFormat) {
        Intent intent = new Intent(activity, ScanActivity.class);
        intent.putExtra(ARG_THEME, theme);
        intent.putExtra(ARG_SCAN_FORMAT, scanFormat);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 解析ScanActivity成功返回的数据，方便使用。
     *
     * @param data onActivityResult的第三个参数
     * @return 扫码成功的数据
     */
    public static DecodeResult getResult(Intent data) {
        return data.getParcelableExtra(ARG_DECODE_RESULT);
    }

    /**
     * 请求权限的Fragment的TAG
     */
    private static final String REQUEST_PERMISSION_TAG = "REQUEST_PERMISSION_TAG";

    /**
     * 相机权限
     */
    private final String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};

    /**
     * 用于权限请求的Fragment
     */
    private RequestPermissionFragment mRequestPermissionFragment;

    /**
     * 扫码控制
     */
    private ScanController mScanController;

    /**
     * 扫码格式
     */
    private int[] mBarFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // 先解析传入的参数
        Intent intent = getIntent();
        int theme = intent.getIntExtra(ARG_THEME, 0);
        if (theme != 0) {
            setTheme(theme);
        }
        mBarFormat = intent.getIntArrayExtra(ARG_SCAN_FORMAT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // 显示返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 添加请求权限的Fragment
        mRequestPermissionFragment = (RequestPermissionFragment) getSupportFragmentManager().findFragmentByTag(REQUEST_PERMISSION_TAG);
        if (mRequestPermissionFragment == null) {
            mRequestPermissionFragment = RequestPermissionFragment.newInstance(CAMERA_PERMISSION, getString(R.string.dialog_hint_camera_permission));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(mRequestPermissionFragment, REQUEST_PERMISSION_TAG).commit();
        }
    }

    private boolean once = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (once) {
            once = false;
            // 检查相机权限
            if (mRequestPermissionFragment.checkPermissions()) {
                startScanBarcode();
            } else {
                mRequestPermissionFragment.requestPermissions();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFailed() {
        // 没有扫描到条码继续扫码
        if (mScanController != null) {
            mScanController.scanAgain();
        }
    }

    @Override
    public void onSucceed(DecodeResult result) {
        Intent intent = new Intent();
        intent.putExtra(ARG_DECODE_RESULT, result);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(boolean grantResult) {
        if (grantResult) {
            startScanBarcode();
        } else {
            Toast.makeText(this, R.string.hint_no_camera_permission, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 开始进行扫码操作
     */
    private void startScanBarcode() {
        ScanFragment fragment = (ScanFragment) getSupportFragmentManager().findFragmentById(R.id.layout_fragment);
        if (fragment == null) {
            fragment = ScanFragment.newInstance(mBarFormat);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.layout_fragment, fragment).commitAllowingStateLoss();
        }
        mScanController = fragment;
    }
}
