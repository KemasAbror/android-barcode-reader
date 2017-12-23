package me.majiajie.barcodereader.encode;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 条形码生成
 */
public class BarCodeGenerateHelper {

    private static final int WIDTH_DEFAULT = 354;//默认大小
    private static final int HEIGHT_DEFAULT = 708;//默认大小

    private static final int COLOR_DEFAULT = Color.BLACK;//默认颜色

    private static final int BACKGROUND_COLOR_DEFAULT = Color.WHITE;//图片背景色

    private String mContent;
    private int mWidth;
    private int mHeight;
    private int mColor;

    private Map<EncodeHintType,Object> mHints;

    public BarCodeGenerateHelper(String content) {
        this(content,WIDTH_DEFAULT,HEIGHT_DEFAULT);
    }

    public BarCodeGenerateHelper(String content, int width, int height) {
        this(content,width,height,COLOR_DEFAULT);
    }

    public BarCodeGenerateHelper(String content, int width, int height, int color) {
        this.mContent = content;
        this.mWidth = width;
        this.mHeight = height;
        this.mColor = color;

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
            bitMatrix = multiFormatWriter.encode(mContent, BarcodeFormat.CODE_128, mWidth, mHeight,mHints);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        return bitMatrix == null ? null : EncodeUtils.toBitmap(bitMatrix,mColor,BACKGROUND_COLOR_DEFAULT);
    }


}
