package me.majiajie.android_barcode_reader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import me.majiajie.barcodereader.decode.DecodeResult;
import me.majiajie.barcodereader.ui.ScanController;
import me.majiajie.barcodereader.ui.ScanFragment;
import me.majiajie.photoalbum.AlbumActivity;

/**
 * 自定义扫码并加入相册识别
 */
public class CustomScanActivity extends AppCompatActivity implements ScanFragment.ScanCallBack {

    private static final int REQUEST_SCAN_PHOTO = 211;

    private FrameLayout mParentLayout;
    private Toolbar mToolbar;

    private ScanController mScanController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        mParentLayout = findViewById(R.id.parentLayout);

        // 标题栏
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 添加预览扫码的Fragment
        ScanFragment fragment = (ScanFragment) getSupportFragmentManager().findFragmentById(me.majiajie.barcodereader.R.id.layout_fragment);
        if (fragment == null) {
            fragment = ScanFragment.newInstance(null);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.framelayout, fragment).commitAllowingStateLoss();
        }

        mScanController = fragment;

        androidx.core.view.OnApplyWindowInsetsListener insetsListener = new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                mToolbar.setPadding(0,insets.getSystemWindowInsetTop(),0,0);
                return insets.consumeSystemWindowInsets();
            }
        };
        ViewCompat.setOnApplyWindowInsetsListener(mParentLayout, insetsListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photos,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_photos:// 调用相册
                AlbumActivity.RequestData requestData = new AlbumActivity.RequestData();
                requestData.setFilterImageMimeType(new String[]{"image/gif"});
                requestData.setTheme(R.style.PhotoAlbumDarkTheme);
                requestData.setShowFullImageBtn(false);
                requestData.setMaxPhotoNumber(1);
                requestData.setFragmentClassName(CustomScanPhotoFragment.class.getName());
                AlbumActivity.startActivityForResult(this,requestData,REQUEST_SCAN_PHOTO);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SCAN_PHOTO){
            DecodeResult result = data.getParcelableExtra("result");
            showToast("扫码成功: " + result.getText());
        }
    }

    @Override
    public void onDecodeFailed() {}

    @Override
    public void onDecodeSucceed(DecodeResult result) {
        showToast("扫码成功: " + result.getText());
    }

    private Toast mToast;

    /**
     * 显示提示
     */
    protected void showToast(String msg){
        if (mToast == null){
            mToast = Toast.makeText(this,msg,Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.cancel();
            mToast = Toast.makeText(this,msg,Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

}
