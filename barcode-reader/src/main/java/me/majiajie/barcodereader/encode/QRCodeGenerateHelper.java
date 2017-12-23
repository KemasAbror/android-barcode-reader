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

    private static final int SIZE_DEFAULT = 354;//默认大小

    private static final int COLOR_DEFAULT = Color.BLACK;//默认颜色

    private static final int BACKGROUND_COLOR_DEFAULT = Color.WHITE;//图片背景色

    private String mContent;
    private int mSize;
    private int mColor;

    private Map<EncodeHintType,Object> mHints;

    public QRCodeGenerateHelper(String content) {
        this(content,SIZE_DEFAULT);
    }

    public QRCodeGenerateHelper(String content, int size) {
        this(content,size,COLOR_DEFAULT);
    }

    public QRCodeGenerateHelper(String content, int size, int color) {
        this.mContent = content;
        this.mSize = size;
        this.mColor = color;

        mHints = new HashMap<>();
        mHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//容差30%
        mHints.put(EncodeHintType.CHARACTER_SET,"UTF-8");//编码
        mHints.put(EncodeHintType.MARGIN,2);//边缘空白
        mHints.put(EncodeHintType.DATA_MATRIX_SHAPE,SymbolShapeHint.FORCE_SQUARE);//限制形状为正方形
    }

    /**
     * 生成二维码
     */
    public Bitmap generate() throws IOException {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(mContent, BarcodeFormat.QR_CODE, mSize, mSize,mHints);
        } catch (WriterException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        return bitMatrix == null ? null : EncodeUtils.toBitmap(bitMatrix,mColor,BACKGROUND_COLOR_DEFAULT);
    }


}
