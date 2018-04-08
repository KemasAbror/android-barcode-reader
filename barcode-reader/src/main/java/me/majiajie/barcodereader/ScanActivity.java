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
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import me.majiajie.barcodereader.decode.DecodeResult;
import me.majiajie.barcodereader.helper.RequestPermissionFragment;
import me.majiajie.barcodereader.ui.ScanController;
import me.majiajie.barcodereader.ui.ScanFragment;

/**
 * 扫码
 */
public class ScanActivity extends AppCompatActivity implements ScanFragment.ScanCallBack, RequestPermissionFragment.RequestPermissionsCallback {

    public static final int REQUEST_CODE = 110;

    public static final String ARG_DECODE_RESULT = "ARG_DECODE_RESULT";

    public static final String ARG_THEME = "ARG_THEME";

    public static final String ARG_SCAN_FORMAT = "ARG_SCAN_FORMAT";

    public static final String ARG_FRAGMENT = "ARG_FRAGMENT";


    /**
     * 启动扫码Activity
     *
     * @param activity   {@link Activity}
     * @param theme      主题,如果传0就使用应用默认主题
     * @param scanFormat 扫码类型{@link BarcodeFormat BarcodeFormat}. 传null就默认扫描QRCODE和CODE128.
     */
    public static void startActivityForResult(Activity activity, @StyleRes int theme, @Nullable int[] scanFormat, @Nullable Class<? extends BaseScanSucceedFragment> clz) {
        startActivityForResult(activity,theme,scanFormat,clz,REQUEST_CODE);

    }

    /**
     * 启动扫码Activity
     *
     * @param activity   {@link Activity}
     * @param theme      主题,如果传0就使用应用默认主题
     * @param scanFormat 扫码类型{@link BarcodeFormat BarcodeFormat}. 传null就默认扫描QRCODE和CODE128.
     */
    public static void startActivityForResult(Activity activity, @StyleRes int theme, @Nullable int[] scanFormat) {
        startActivityForResult(activity,theme,scanFormat,null,REQUEST_CODE);
    }

    /**
     * 启动扫码Activity
     *
     * @param activity      {@link Activity}
     * @param theme         主题,如果传0就使用应用默认主题
     * @param scanFormat    扫码类型{@link BarcodeFormat BarcodeFormat}. 传null就默认扫描QRCODE和CODE128.
     * @param requestCode   请求代码
     */
    public static void startActivityForResult(Activity activity, @StyleRes int theme, @Nullable int[] scanFormat, @Nullable Class<? extends BaseScanSucceedFragment> clz,int requestCode) {
        Intent intent = new Intent(activity, ScanActivity.class);
        intent.putExtra(ARG_THEME, theme);
        intent.putExtra(ARG_SCAN_FORMAT, scanFormat);
        intent.putExtra(ARG_FRAGMENT, clz == null ? "":clz.getName());
        activity.startActivityForResult(intent, requestCode);
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
     * 用于处理结果的Fragment tag
     */
    private static final String TAG_COMPLETE_FRAGMENT = "TAG_COMPLETE_FRAGMENT";

    /**
     * 请求权限的Fragment的TAG
     */
    private static final String TAG_REQUEST_PERMISSION = "TAG_REQUEST_PERMISSION";

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

    /**
     * 用于处理结果的Fragment(不一定存在)
     */
    private BaseScanSucceedFragment mBaseScanSucceedFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // 先解析传入的参数
        Intent intent = getIntent();
        int theme = intent.getIntExtra(ARG_THEME, 0);
        if (theme != 0) {
            setTheme(theme);
        }
        mBarFormat = intent.getIntArrayExtra(ARG_SCAN_FORMAT);
        String fragmentName = intent.getStringExtra(ARG_FRAGMENT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // 显示返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 添加请求权限的Fragment
        mRequestPermissionFragment = (RequestPermissionFragment) getSupportFragmentManager().findFragmentByTag(TAG_REQUEST_PERMISSION);
        if (mRequestPermissionFragment == null) {
            mRequestPermissionFragment = RequestPermissionFragment.newInstance(CAMERA_PERMISSION, getString(R.string.dialog_hint_camera_permission));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(mRequestPermissionFragment, TAG_REQUEST_PERMISSION).commit();
        }

        // 如果传入了Fragment类名字段就添加Fragment
        if (!TextUtils.isEmpty(fragmentName)){
            try {
                mBaseScanSucceedFragment = (BaseScanSucceedFragment) getSupportFragmentManager().findFragmentByTag(TAG_COMPLETE_FRAGMENT);
                if (mBaseScanSucceedFragment == null){
                    mBaseScanSucceedFragment = (BaseScanSucceedFragment) Class.forName(fragmentName).newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .add(mBaseScanSucceedFragment,TAG_COMPLETE_FRAGMENT)
                            .commit();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
    public void onDecodeFailed() {
        // 没有扫描到条码继续扫码
        if (mScanController != null) {
            mScanController.scanAgain();
        }
    }

    @Override
    public void onDecodeSucceed(DecodeResult result) {
        if (mBaseScanSucceedFragment != null){
            mBaseScanSucceedFragment.onDecodeSucceed(result);
        } else {
            Intent intent = new Intent();
            intent.putExtra(ARG_DECODE_RESULT, result);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
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
