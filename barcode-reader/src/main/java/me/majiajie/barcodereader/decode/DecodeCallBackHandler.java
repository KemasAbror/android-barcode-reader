package me.majiajie.barcodereader.decode;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import me.majiajie.barcodereader.R;


/**
 * 解码结果回调
 */
public class DecodeCallBackHandler extends Handler {

    private Callback mCallBack;

    DecodeCallBackHandler(Looper looper, Callback mCallBack) {
        super(looper);
        this.mCallBack = mCallBack;
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        if (what == R.id.decode_succeeded) {//扫码成功
            Bundle bundle = msg.getData();
            byte[] b = bundle.getByteArray(DecodeHandler.BARCODE_BITMAP);
            float scaledFactor = bundle.getFloat(DecodeHandler.BARCODE_SCALED_FACTOR);
            String text = bundle.getString(DecodeHandler.BARCODE_RAW_RESULT);
            int format = bundle.getInt(DecodeHandler.BARCODE_FORMAT);
            mCallBack.onSucceed(new DecodeResult(text,b,scaledFactor,format));
        } else if (what == R.id.decode_failed) {//扫码失败
            mCallBack.onFailed();
        }
    }

    public interface Callback {

        /**
         * 扫码失败
         */
        void onFailed();

        /**
         * 扫码成功
         * @param result    扫码信息
         */
        void onSucceed(DecodeResult result);
    }
}
