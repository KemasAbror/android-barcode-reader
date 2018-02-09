package me.majiajie.barcodereader.encode;

import android.graphics.Bitmap;

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
}
