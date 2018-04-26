package me.majiajie.android_barcode_reader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.zxing.DecodeHintType;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import me.majiajie.barcodereader.decode.DecodeCallback;
import me.majiajie.barcodereader.decode.DecodeHandlerHelper;
import me.majiajie.barcodereader.decode.DecodeResult;
import me.majiajie.photoalbum.BaseCompleteFragment;
import me.majiajie.photoalbum.PhotoAlbumActivity;
import me.majiajie.photoalbum.photo.Photo;

/**
 * 用于接收图片返回,并识别图片
 */
public class CustomScanPhotoFragment extends BaseCompleteFragment{

    private static final int REQUEST_CODE_CROP_IMAGE = 666;

    private Context mContext;
    private Activity mActivity;

    // 需要识别的图片地址
    private String mPhotoPath;

    // 记录扫描的本地图片是否裁剪过
    private boolean mIsCrop;

    // 裁剪图片的缓存地址
    private String mCropTmpPath;

    // 识别本地图片的回调
    private DecodeCallback mPhotoDecodeCallback = new DecodeCallback() {
        @Override
        public void onDecodeFailed() {
            hideLoading();
            if (mIsCrop || !goCropImage(mPhotoPath)){
                showToast("图片无法识别");
            }
        }
        @Override
        public void onDecodeSucceed(DecodeResult result) {
            hideLoading();
            Intent intent = new Intent();
            intent.putExtra("result",result);
            mActivity.setResult(Activity.RESULT_OK,intent);
            mActivity.finish();
        }
    };

    @Override
    protected void onResultData(PhotoAlbumActivity.ResultData resultData) {
        mIsCrop = false;
        ArrayList<Photo> photos = resultData.getPhotos();
        mPhotoPath = photos.get(0).getPath();
        scanPhoto();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
        // 存储剪切图片的地址
        mCropTmpPath = context.getExternalCacheDir() + File.separator + "cropImg.jpg";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDecodePhotoHelper != null) {
            mDecodePhotoHelper.stopThread();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_CROP_IMAGE:// 裁剪图片返回
                    mIsCrop = true;
                    mPhotoPath = mCropTmpPath;
                    scanPhoto();
                    break;
            }
        }
    }

    /**
     * 扫描图片
     */
    private void scanPhoto() {
        showLoading("正在识别图片...");
        DecodeHandlerHelper decodeHandlerHelper = getScanCodeHelper();
        decodeHandlerHelper.decode(new File(mPhotoPath));
    }

    /**
     * 去裁剪图片
     */
    private boolean goCropImage(String file) {
        Intent intent = new Intent();
        intent.setAction("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(new File(file)), "image/*");
        intent.putExtra("aspectX", 1);  //裁剪方框宽的比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);  //是否保持比例
        intent.putExtra("return-data", false);  //是否返回bitmap
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCropTmpPath))); //保存图片到指定uri
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());  //输出格式
        intent = Intent.createChooser(intent, "裁剪图片");
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
            return true;
        }
        return false;
    }

    private DecodeHandlerHelper mDecodePhotoHelper;

    private DecodeHandlerHelper getScanCodeHelper(){
        if (mDecodePhotoHelper == null){
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            // 设置解码类型
            hints.put(DecodeHintType.POSSIBLE_FORMATS, null);
            hints.put(DecodeHintType.TRY_HARDER, false);
            mDecodePhotoHelper = new DecodeHandlerHelper(mPhotoDecodeCallback, hints);
        }
        if (!mDecodePhotoHelper.isStart()){
            mDecodePhotoHelper.start();
        }
        return mDecodePhotoHelper;
    }

    private ProgressDialog mLoadingDialog;

    /**
     * 显示加载的提示框
     */
    protected void showLoading(String message) {
        if (mLoadingDialog == null) {
            ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            mLoadingDialog = progressDialog;
        }
        mLoadingDialog.setMessage(message);
        mLoadingDialog.show();
    }

    /**
     * 隐藏加载提示框
     */
    protected void hideLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.cancel();
        }
    }

    private Toast mToast;

    /**
     * 显示提示
     */
    protected void showToast(String msg){
        if (mToast == null){
            mToast = Toast.makeText(mContext,msg,Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.cancel();
            mToast = Toast.makeText(mContext,msg,Toast.LENGTH_SHORT);
            mToast.show();
        }
    }
}
