package me.majiajie.barcodereader.encode;

import android.graphics.Bitmap;
import android.graphics.Color;

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
     * Zxing 配置参数
     */
    private Map<EncodeHintType,Object> mHints;

    private QRCodeGenerateHelper(Builder builder) {
        mContent = builder.content;
        mSize = builder.size;
        mColor = builder.color;
        mBackgroundColor = builder.backgroundColor;

        mHints = new HashMap<>();
        mHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//容差30%
        mHints.put(EncodeHintType.CHARACTER_SET,"UTF-8");//编码
        mHints.put(EncodeHintType.MARGIN,2);//边缘空白
        mHints.put(EncodeHintType.DATA_MATRIX_SHAPE,SymbolShapeHint.FORCE_SQUARE);//限制形状为正方形
    }

    /**
     * 生成二维码
     */
    public Bitmap generateBitmap() throws IOException {
        BitMatrix bitMatrix = generateBitMatrix();
        return bitMatrix == null ? null : BarCodeEncodeUtils.toBitmap(bitMatrix,mColor,mBackgroundColor);
    }

    /**
     * 生成二维码
     */
    public BitMatrix generateBitMatrix() throws IOException {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = multiFormatWriter.encode(mContent, BarcodeFormat.QR_CODE, mSize, mSize,mHints);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        return bitMatrix;
    }

    public static class Builder{

        private static final int SIZE_DEFAULT = 354;//默认宽高

        private String content;// 二维码内容
        private int size;// 二维码宽高
        private int color;// 二维码颜色
        private int backgroundColor;// 背景颜色

        /**
         * 创建二维码生成的构造类
         * @param content   条码内容
         */
        public Builder(String content) {
            this.content = content;
            this.color = Color.BLACK;
            this.backgroundColor = Color.TRANSPARENT;
            this.size = SIZE_DEFAULT;
        }

        /**
         * 二维码宽高
         * @param size     二维码宽高
         */
        public Builder size(int size){
            this.size = size;
            return Builder.this;
        }

        /**
         * 二维码颜色
         * @param color 16进制颜色值
         */
        public Builder color(int color){
            this.color = color;
            return Builder.this;
        }

        /**
         * 二维码背景颜色
         * @param backgroundColor 16进制颜色值
         */
        public Builder backgroundColor(int backgroundColor){
            this.backgroundColor = backgroundColor;
            return Builder.this;
        }

        /**
         * 完成构建
         */
        public QRCodeGenerateHelper build(){
            return new QRCodeGenerateHelper(this);
        }

    }


}
