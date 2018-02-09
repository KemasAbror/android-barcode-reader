package me.majiajie.barcodereader.decode;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.DecodeHintType;

import java.lang.ref.WeakReference;
import java.util.Map;

import me.majiajie.barcodereader.R;

/**
 * 持续性解码线程管理
 */
public class DecodeHandlerHelper {

    private final String TAG = "DecodeHandlerHelper";

    // 解码线程
    private HandlerThread mHandlerThread;
    private DecodeHandler mDecodeHandler;

    // 传入的回调
    private final DecodeCallback mDecodeCallBack;

    /**
     * 解码属性参数
     */
    private final Map<DecodeHintType, Object> mHints;

    /**
     * 用于判断是否停止
     */
    private boolean isStoped;

    /**
     * 判断是否正在解码
     */
    private boolean isDecoding;

    public DecodeHandlerHelper(DecodeCallback callBack, Map<DecodeHintType, Object> hints) {
        mDecodeCallBack = callBack;
        mHints = hints;
    }

    /**
     * 开启解码线程
     */
    public void start() {
        isStoped = false;
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();

        mDecodeHandler = new DecodeHandler(
                mHandlerThread.getLooper(), new DecodeCallbackHandler(this,mDecodeCallBack), mHints);
    }

    /**
     * 停止解码线程
     */
    public void stopThread() {
        isStoped = true;

        mDecodeHandler.removeMessages(R.id.decode);
        mDecodeHandler.removeMessages(R.id.decode_vertical);

        if (mHandlerThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mHandlerThread.quitSafely();
            } else {
                mHandlerThread.quit();
            }
            try {
                mHandlerThread.join();
                mHandlerThread = null;
                mDecodeHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解码
     *
     * @param data          图像数据
     * @param width         图像宽
     * @param height        图像高
     * @param rect          解码区域
     * @param neadRotate    是否需要旋转图像，将图像顺时针旋转90度。（在解析一维码时方向很重要）
     */
    public void decode(byte[] data, int width, int height, Rect rect,boolean neadRotate) {
        if (!isStoped && !isDecoding) {
            isDecoding = true;
            DecodeBean bean = new DecodeBean(data, width, height, rect);
            mDecodeHandler.obtainMessage(neadRotate ? R.id.decode_vertical : R.id.decode, bean).sendToTarget();
        }
    }

    /**
     * 标记解码线程暂时完成
     */
    private void done(){
        isDecoding = false;
    }

    /**
     * 解码结果回调
     */
    public static class DecodeCallbackHandler extends Handler {

        private WeakReference<DecodeHandlerHelper> mHelper;

        private DecodeCallback mCallBack;

        DecodeCallbackHandler(DecodeHandlerHelper helper,DecodeCallback mCallBack) {
            super(Looper.getMainLooper());
            this.mCallBack = mCallBack;
            mHelper = new WeakReference<>(helper);
        }

        @Override
        public void handleMessage(Message msg) {
            DecodeHandlerHelper decodeHandlerHelper = mHelper.get();
            if (decodeHandlerHelper != null){
                decodeHandlerHelper.done();

                int what = msg.what;
                if (what == R.id.decode_succeeded) {//扫码成功
                    Bundle bundle = msg.getData();
                    byte[] b = bundle.getByteArray(DecodeHandler.BARCODE_BITMAP);
                    float scaledFactor = bundle.getFloat(DecodeHandler.BARCODE_SCALED_FACTOR);
                    String text = bundle.getString(DecodeHandler.BARCODE_RAW_RESULT);
                    int format = bundle.getInt(DecodeHandler.BARCODE_FORMAT);
                    mCallBack.onDecodeSucceed(new DecodeResult(text,b,scaledFactor,format));
                } else if (what == R.id.decode_failed) {//扫码失败
                    mCallBack.onDecodeFailed();
                }
            }
        }
    }
}
