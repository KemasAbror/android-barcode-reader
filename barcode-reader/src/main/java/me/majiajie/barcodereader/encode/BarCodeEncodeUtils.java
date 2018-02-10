package me.majiajie.barcodereader.encode;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.google.zxing.common.BitMatrix;

/**
 * 条码生成的工具类
 */
public class BarCodeEncodeUtils {

    private BarCodeEncodeUtils() {}

    /**
     * 将BitMatrix转换成Bitmap
     *
     * @param bitMatrix        生成的条码数据
     * @param color            条码颜色
     * @param color_background 条码背景色
     * @return 条码图片
     */
    public static Bitmap toBitmap(BitMatrix bitMatrix, int color, int color_background) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[y * width + x] = bitMatrix.get(x, y) ? color : color_background;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    /**
     * 获取圆角图片
     */
    static Bitmap getRounndImg(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        BitmapShader shader;
        shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        RectF rect = new RectF(0.0f, 0.0f, w, h);

        Bitmap roundBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundBitmap);

        canvas.drawRoundRect(rect, w*0.17f, h*0.17f, paint);

        return roundBitmap;
    }

    /**
     * drawable 转换为 Bitmap
     */
    static Bitmap drawableToBitmap(Drawable drawable){
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
}
