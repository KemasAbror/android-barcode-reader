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

    private static final int WIDTH_DEFAULT = 354;//默认宽度
    private static final int HEIGHT_DEFAULT = 708;//默认高度

    private static final int COLOR_DEFAULT = Color.BLACK;//默认颜色

    private static final int BACKGROUND_COLOR_DEFAULT = Color.WHITE;//图片背景色

    /**
     * 条码内容
     */
    private String mContent;

    /**
     * 条码宽度
     */
    private int mWidth;

    /**
     * 条码高度
     */
    private int mHeight;

    /**
     * 条码颜色
     */
    private int mColor;

    /**
     * 条码类型{@link me.majiajie.barcodereader.BarcodeFormat BarcodeFormat}
     */
    private int mFormat;

    private Map<EncodeHintType,Object> mHints;

    public BarCodeGenerateHelper(String content,int format) {
        this(content,format,WIDTH_DEFAULT,HEIGHT_DEFAULT);
    }

    public BarCodeGenerateHelper(String content,int format, int width, int height) {
        this(content,format,width,height,COLOR_DEFAULT);
    }

    public BarCodeGenerateHelper(String content,int format, int width, int height, int color) {
        mContent = content;
        mWidth = width;
        mHeight = height;
        mColor = color;
        mFormat = format;

        mHints = new HashMap<>();
        mHints.put(EncodeHintType.CHARACTER_SET,"UTF-8");//编码
        mHints.put(EncodeHintType.MARGIN,2);//边缘空白
    }

    /**
     * 生成条形码
     */
    public Bitmap generate() throws IOException {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(mContent, BarcodeFormat.getZxingFormat(mFormat), mWidth, mHeight,mHints);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        return bitMatrix == null ? null : EncodeUtils.toBitmap(bitMatrix,mColor,BACKGROUND_COLOR_DEFAULT);
    }


}
