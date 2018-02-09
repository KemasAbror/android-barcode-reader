package me.majiajie.barcodereader.encode;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.majiajie.barcodereader.BarcodeFormat;

/**
 * 条形码生成
 */
public class BarCodeGenerateHelper {

    /**
     * 条形码内容
     */
    private String mContent;

    /**
     * 条形码宽度
     */
    private int mWidth;

    /**
     * 条形码高度
     */
    private int mHeight;

    /**
     * 条形码颜色
     */
    private int mColor;

    /**
     * 条形码背景颜色
     */
    private int mBackgroundColor;

    /**
     * 条码类型{@link me.majiajie.barcodereader.BarcodeFormat BarcodeFormat}
     */
    private int mFormat;

    /**
     * Zxing 配置参数
     */
    private Map<EncodeHintType,Object> mHints;

    private BarCodeGenerateHelper(Builder builder){
        mContent = builder.content;
        mWidth = builder.width;
        mHeight = builder.height;
        mColor = builder.color;
        mFormat = builder.format;
        mBackgroundColor = builder.backgroundColor;

        mHints = new HashMap<>();
        mHints.put(EncodeHintType.CHARACTER_SET,"UTF-8");//编码
        mHints.put(EncodeHintType.MARGIN,2);//边缘空白
    }

    /**
     * 生成条形码
     */
    public Bitmap generateBitmap() throws IOException {
        BitMatrix bitMatrix = generateBitMatrix();
        return bitMatrix == null ? null : BarCodeEncodeUtils.toBitmap(bitMatrix,mColor,mBackgroundColor);
    }

    /**
     * 生成条形码
     */
    public BitMatrix generateBitMatrix() throws IOException {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = multiFormatWriter.encode(mContent, BarcodeFormat.getZxingFormat(mFormat), mWidth, mHeight,mHints);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        return bitMatrix;
    }

    public static class Builder{

        private static final int WIDTH_DEFAULT = 708;//默认宽度
        private static final int HEIGHT_DEFAULT = 354;//默认高度

        private String content;// 条码内容
        private int width;// 条码宽度
        private int height;// 条码高度
        private int color;// 条码颜色
        private int backgroundColor;// 背景颜色
        private int format;// 条码类型

        /**
         * 创建条形码码生成的构造类
         * @param content   条码内容
         * @param format    条码类型{@link me.majiajie.barcodereader.BarcodeFormat BarcodeFormat}
         */
        public Builder(String content, int format) {
            if (format == BarcodeFormat.QR_CODE){
                throw new IllegalArgumentException("format can't be QRcode");
            }
            this.content = content;
            this.format = format;
            this.color = Color.BLACK;
            this.backgroundColor = Color.TRANSPARENT;
            this.width = WIDTH_DEFAULT;
            this.height = HEIGHT_DEFAULT;
        }

        /**
         * 条形码大小
         * @param width     条形码宽度
         * @param height    条形码高度
         */
        public Builder size(int width,int height){
            this.width = width;
            this.height = height;
            return Builder.this;
        }

        /**
         * 条形码颜色
         * @param color 16进制颜色值
         */
        public Builder color(int color){
            this.color = color;
            return Builder.this;
        }

        /**
         * 条码背景颜色
         * @param backgroundColor 16进制颜色值
         */
        public Builder backgroundColor(int backgroundColor){
            this.backgroundColor = backgroundColor;
            return Builder.this;
        }

        /**
         * 完成构建
         */
        public BarCodeGenerateHelper build(){
            return new BarCodeGenerateHelper(this);
        }

    }


}
