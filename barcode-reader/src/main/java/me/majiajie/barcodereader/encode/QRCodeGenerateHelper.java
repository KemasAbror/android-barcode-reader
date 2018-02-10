package me.majiajie.barcodereader.encode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成
 */
public class QRCodeGenerateHelper {

    /**
     * 二维码内容
     */
    private String mContent;

    /**
     * 二维码大小
     */
    private int mSize;

    /**
     * 二维码颜色
     */
    private int mColor;

    /**
     * 背景颜色
     */
    private int mBackgroundColor;

    /**
     * 图标资源
     */
    private Drawable mIconDrawable;

    /**
     * 图标资源
     */
    private Bitmap mIconBitmap;

    /**
     * 图标背景颜色
     */
    private int mIconStrokeColor;

    /**
     * 图标是否为圆角
     */
    private boolean mIconRoundAngle;

    /**
     * Zxing 配置参数
     */
    private Map<EncodeHintType, Object> mHints;

    private QRCodeGenerateHelper(Builder builder) {
        mContent = builder.content;
        mSize = builder.size;
        mColor = builder.color;
        mBackgroundColor = builder.backgroundColor;
        mIconDrawable = builder.iconDrawable;
        mIconBitmap = builder.iconBitmap;
        mIconStrokeColor = builder.iconStrokeColor;
        mIconRoundAngle = builder.iconRoundAngle;

        mHints = new HashMap<>();
        mHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//容差30%
        mHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");//编码
        mHints.put(EncodeHintType.MARGIN, 2);//边缘空白
        mHints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_SQUARE);//限制形状为正方形
    }

    /**
     * 生成二维码
     */
    public Bitmap generateBitmap() throws IOException {
        BitMatrix bitMatrix = generateBitMatrix();

        if (bitMatrix == null) {
            return null;
        }
        // 二维码的真实大小
        int codeSize = 0;
        for (int i = 0; i < mSize; i++) {
            if (bitMatrix.get(i, i)) {
                codeSize = mSize - i * 2;
                break;
            }
        }

        Bitmap bitmap = BarCodeEncodeUtils.toBitmap(bitMatrix, mColor, mBackgroundColor);

        if (mIconBitmap != null) {
            addIcon(bitmap, codeSize);
        } else if (mIconDrawable != null) {
            addIcon(BarCodeEncodeUtils.drawableToBitmap(mIconDrawable),codeSize);
        }

        return bitmap;
    }

    /**
     * 生成二维码,注意BitMatrix只带有二维码点阵信息,不具备任何颜色、图标相关属性
     */
    public BitMatrix generateBitMatrix() throws IOException {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = multiFormatWriter.encode(mContent, BarcodeFormat.QR_CODE, mSize, mSize, mHints);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        return bitMatrix;
    }

    /**
     * 添加中间小图标
     */
    private void addIcon(Bitmap bitmap, int codeSize) {

        //计算位置
        float w = codeSize * 0.28f;
        float left = (mSize - w) / 2f;
        float top = (mSize - w) / 2f;
        float right = left + w;
        float bottom = top + w;

        RectF rectF = new RectF(left, top, right, bottom);
        RectF rectF_icon;

        Canvas canvas = new Canvas(bitmap);
        //抗锯齿
//        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));

        Paint paint = new Paint();
        paint.setColor(mIconStrokeColor);
        paint.setAntiAlias(true);

        if (mIconStrokeColor == Color.TRANSPARENT) {
            // 不需要边框
            rectF_icon = new RectF(left, top, right, bottom);
        } else {
            // 画图标边框
            if (mIconRoundAngle) { // 圆角
                canvas.drawRoundRect(rectF, w * 0.17f, w * 0.17f, paint);
            } else {// 直角
                canvas.drawRect(rectF, paint);
            }

            float padding = w * 0.05f;// 边框相对图标的比例

            // 计算图片的位置
            rectF_icon = new RectF(left + padding,
                    top + padding,
                    right - padding,
                    bottom - padding);
        }

        if (mIconRoundAngle){// 圆角
            float width = rectF_icon.width();
            float height = rectF_icon.height();
            // 缩放图片大小
            Bitmap temBitmap = Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, false);
            canvas.drawBitmap(BarCodeEncodeUtils.getRounndImg(temBitmap), rectF_icon.left, rectF_icon.top, null);
        } else {// 直角
            canvas.drawBitmap(bitmap, null, rectF_icon, null);
        }
    }

    public static class Builder {

        private static final int SIZE_DEFAULT = 354;// 默认宽高

        private String content;// 二维码内容
        private int size;// 二维码宽高
        private int color;// 二维码颜色
        private int backgroundColor;// 背景颜色

        private Drawable iconDrawable;// 图标资源
        private Bitmap iconBitmap;// 图标资源(优先)
        private int iconStrokeColor;// 图标背景颜色
        private boolean iconRoundAngle;// 图标是否为圆角

        /**
         * 创建二维码生成的构造类
         *
         * @param content 条码内容
         */
        public Builder(String content) {
            this.content = content;
            this.color = Color.BLACK;
            this.backgroundColor = Color.TRANSPARENT;
            this.size = SIZE_DEFAULT;
            this.iconStrokeColor = Color.TRANSPARENT;
            this.iconRoundAngle = false;
        }

        /**
         * 二维码宽高
         *
         * @param size 二维码宽高
         */
        public Builder size(int size) {
            this.size = size;
            return Builder.this;
        }

        /**
         * 二维码颜色
         *
         * @param color 16进制颜色值
         */
        public Builder color(@ColorInt int color) {
            this.color = color;
            return Builder.this;
        }

        /**
         * 二维码背景颜色
         *
         * @param backgroundColor 16进制颜色值
         */
        public Builder backgroundColor(@ColorInt int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return Builder.this;
        }

        /**
         * 二维码中间图标
         *
         * @param drawable 本地资源
         */
        public Builder icon(Drawable drawable) {
            this.iconDrawable = drawable;
            this.iconBitmap = null;
            return Builder.this;
        }

        /**
         * 二维码中间图标
         *
         * @param bitmap 图片资源
         */
        public Builder icon(Bitmap bitmap) {
            this.iconBitmap = bitmap;
            this.iconDrawable = null;
            return Builder.this;
        }

        /**
         * 二维码中间图标的边线颜色，默认透明
         *
         * @param color 16进制颜色值
         */
        public Builder iconStroke(@ColorInt int color) {
            this.iconStrokeColor = color;
            return Builder.this;
        }

        /**
         * 二维码中间图标是否使用圆角
         *
         * @param round true圆角,false直角
         */
        public Builder iconRoundAngle(boolean round) {
            this.iconRoundAngle = round;
            return Builder.this;
        }

        /**
         * 完成构建
         */
        public QRCodeGenerateHelper build() {
            return new QRCodeGenerateHelper(this);
        }

    }


}
