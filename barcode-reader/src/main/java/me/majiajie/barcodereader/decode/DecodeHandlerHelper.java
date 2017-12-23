package me.majiajie.barcodereader.decode;


import android.graphics.Rect;
import android.os.Build;
import android.os.HandlerThread;

import com.google.zxing.DecodeHintType;

import java.util.Map;

import me.majiajie.barcodereader.R;

/**
 * 持续性解码线程管理
 */
public class DecodeHandlerHelper {

    private final String TAG = "DecodeHandlerHelper";

    // 主解码
    private HandlerThread mHandlerThread;
    private DecodeHandler mDecodeHandler;

    // 辅助解码
    private HandlerThread mSecondHandlerThread;
    private DecodeHandler mSecondDecodeHandler;

    // 传入的回调
    private final DecodeCallBackHandler.Callback mDecodeCallBack;

    // 解码属性参数
    private final Map<DecodeHintType, Object> mHints;

    /**
     * 用于判断是否停止
     */
    private boolean isStop;

    /**
     * 用于判断两个两条线程中是否有成功解码的
     */
    private boolean isSucceed;

    /**
     * 判断辅助解码是否完成
     */
    private boolean isSecondComplete = true;

    /**
     * 主解码回调
     */
    private DecodeCallBackHandler.Callback mFirstDecodeCallBack = new DecodeCallBackHandler.Callback() {
        @Override
        public void onFailed() {
            decodeFailed();
        }

        @Override
        public void onSucceed(DecodeResult result) {
            decodeSucceed(result);
        }
    };

    /**
     * 辅助解码回调
     */
    private DecodeCallBackHandler.Callback mSecondDecodeCallBack = new DecodeCallBackHandler.Callback() {
        @Override
        public void onFailed() {
            isSecondComplete = true;
        }

        @Override
        public void onSucceed(DecodeResult result) {
            isSecondComplete = true;
            decodeSucceed(result);
        }
    };

    public DecodeHandlerHelper(DecodeCallBackHandler.Callback callBack, Map<DecodeHintType, Object> hints) {
        mDecodeCallBack = callBack;
        mHints = hints;
    }

    /**
     * 开启解码线程
     */
    public void start() {
        isStop = false;

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mDecodeHandler = new DecodeHandler(mHandlerThread.getLooper(), mFirstDecodeCallBack, mHints);

        mSecondHandlerThread = new HandlerThread(TAG);
        mSecondHandlerThread.start();
        mSecondDecodeHandler = new DecodeHandler(mSecondHandlerThread.getLooper(), mSecondDecodeCallBack, mHints);
    }

    /**
     * 停止解码线程
     */
    public void stopThread() {
        isStop = true;

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

        // 辅助线程销毁
        mSecondDecodeHandler.removeMessages(R.id.decode);

        if (mSecondHandlerThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mSecondHandlerThread.quitSafely();
            } else {
                mSecondHandlerThread.quit();
            }
            try {
                mSecondHandlerThread.join();
                mSecondHandlerThread = null;
                mSecondDecodeHandler = null;
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
     * @param isVertical    判断当前显示的视图是否垂直。（在解析一维码时必须方向很重要）
     */
    public void decode(byte[] data, int width, int height, Rect rect,boolean isVertical) {
        if (!isStop) {
            isSucceed = false;
            DecodeBean bean = new DecodeBean(data, width, height, rect);
            mDecodeHandler.obtainMessage(isVertical ? R.id.decode_vertical : R.id.decode, bean).sendToTarget();
            // 辅助线程解码完成就继续解码
            if (isSecondComplete) {
                isSecondComplete = false;
                DecodeBean secondBean = new DecodeBean(data, width, height, new Rect(0, 0, width, height));
                mSecondDecodeHandler.obtainMessage(R.id.decode, secondBean).sendToTarget();
            }
        }
    }

    private void decodeSucceed(DecodeResult result) {
        if (!isSucceed) {
            isSucceed = true;
            mDecodeCallBack.onSucceed(result);
        }
    }

    private void decodeFailed() {
        mDecodeCallBack.onFailed();
    }
}
