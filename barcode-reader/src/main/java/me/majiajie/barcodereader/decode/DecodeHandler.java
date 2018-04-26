package me.majiajie.barcodereader.decode;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import me.majiajie.barcodereader.BarcodeFormat;
import me.majiajie.barcodereader.R;

/**
 * 实际进行解码操作的类
 */
public class DecodeHandler extends Handler {

    static final String BARCODE_BITMAP = "barcode_bitmap";
    static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";
    static final String BARCODE_RAW_RESULT = "barcode_raw_result";
    static final String BARCODE_FORMAT = "barcode_format";

    private final Handler resultHandler;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    DecodeHandler(Looper looper, Handler callBackHandler, Map<DecodeHintType, Object> hints) {
        super(looper);
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        resultHandler = callBackHandler;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }

        int what = message.what;

        if (what == R.id.decode) {
            decode((DecodeBean) message.obj);
        } else if (what == R.id.decode_vertical) {
            decodeVertical((DecodeBean) message.obj);
        } else if (what == R.id.decode_file) {
            decodeFile((String) message.obj);
        } else if (what == R.id.quit) {
            running = false;
            Looper looper = Looper.myLooper();
            if (looper != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    looper.quitSafely();
                } else {
                    looper.quit();
                }
            }
        }
    }

    /**
     * 将相机获取的数据变为竖向的然后解码
     */
    private void decodeVertical(DecodeBean decodeBean) {

        final int oldWidth = decodeBean.getWidth();
        byte[] oldData = decodeBean.getData();
        Rect oldRect = decodeBean.getRect();
        final int rectHeight = oldRect.height();
        final int rectWidth = oldRect.width();
        final int data_max_size = oldData.length;
        final int rect_max_size = rectHeight * rectWidth;

        //将横向的图像数据改为竖向(只取需要解码的区域)
        byte[] newData = new byte[rect_max_size];
        for (int y = 0; y < rectHeight; y++) {
            for (int x = 0; x < rectWidth; x++) {
                //防止在特殊情况下宽高不准
                int tem_a = (x + 1) * rectHeight - y - 1;
                int tem_b = x + (oldRect.top + y) * oldWidth + oldRect.left;
                if (tem_a >= rect_max_size || tem_b >= data_max_size) {
                    continue;
                }
                newData[tem_a] = oldData[tem_b];
            }
        }

        // 设置变换过的数据（长宽颠倒的）
        decode(new DecodeBean(newData, rectHeight, rectWidth, new Rect(0, 0, rectHeight, rectWidth)));
    }

    /**
     * 解码数据，每次都使用同一个解码实例。
     */
    private void decode(DecodeBean decodeBean) {
        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource
                (decodeBean.getData(), decodeBean.getWidth(), decodeBean.getHeight(), decodeBean.getRect());
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        if (resultHandler != null) {
            if (rawResult != null) {
                Message message = resultHandler.obtainMessage(R.id.decode_succeeded);
                Bundle bundle = new Bundle();
                bundleThumbnail(source, bundle);
                bundle.putString(BARCODE_RAW_RESULT, rawResult.getText());
                bundle.putInt(BARCODE_FORMAT, BarcodeFormat.conversion(rawResult.getBarcodeFormat()));
                message.setData(bundle);
                message.sendToTarget();
            } else {
                Message message = resultHandler.obtainMessage(R.id.decode_failed);
                message.sendToTarget();
            }
        }
    }

    /**
     * 解码图片文件
     */
    private void decodeFile(String file) {
        Result rawResult = null;
        int[] data = new int[0];
        int width = 0;
        int height = 0;
        try {
            Bitmap bitmap = getFileBitmap(file, 1000, 1000);

            width = bitmap.getWidth();
            height = bitmap.getHeight();
            data = new int[width * height];
            bitmap.getPixels(data, 0, width, 0, 0, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            rawResult = multiFormatReader.decodeWithState(binaryBitmap);
        } catch (NotFoundException e) {
            //continue
        }

        if (resultHandler != null) {
            if (rawResult != null) {
                Message message = resultHandler.obtainMessage(R.id.decode_succeeded);
                Bundle bundle = new Bundle();
                bundle.putByteArray(BARCODE_BITMAP, rawResult.getRawBytes());
                bundle.putFloat(BARCODE_SCALED_FACTOR, 1);
                bundle.putString(BARCODE_RAW_RESULT, rawResult.getText());
                bundle.putInt(BARCODE_FORMAT, BarcodeFormat.conversion(rawResult.getBarcodeFormat()));
                message.setData(bundle);
                message.sendToTarget();
            } else {
                Message message = resultHandler.obtainMessage(R.id.decode_failed);
                message.sendToTarget();
            }
        }
    }

    /**
     * 创建缩略图并存放到Bundle
     */
    private void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }

    /**
     * 构建解码需要的数据
     */
    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height, Rect rect) {
        if (rect == null) {
            return null;
        }

        Rect newRect = new Rect(rect);

        //扫描框数据越界修正
        if (newRect.right > width) {
            newRect.right = width;
        }
        if (newRect.left < 0) {
            newRect.left = 0;
        }
        if (newRect.top < 0) {
            newRect.top = 0;
        }
        if (newRect.bottom > height) {
            newRect.bottom = height;
        }

        return new PlanarYUVLuminanceSource(data, width, height, newRect.left, newRect.top,
                newRect.width(), newRect.height(), false);
    }

    /**
     * 加载图片
     */
    private Bitmap getFileBitmap(String path, int reqWidth, int reqHeight) {
        // 检查图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // 计算缩放大小
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 解析图片到内存
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 计算缩放大小
     */
    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // 图片资源的大小
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
