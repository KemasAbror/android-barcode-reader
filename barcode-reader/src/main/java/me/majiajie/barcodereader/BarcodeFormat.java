package me.majiajie.barcodereader;

import android.util.SparseArray;

/**
 * 支持的条码类型,从{@link com.google.zxing.BarcodeFormat BarcodeFormat} 复制
 */
public class BarcodeFormat {

    /** Aztec 2D barcode format. */
    public static final int AZTEC = 0;

    /** CODABAR 1D format. */
    public static final int CODABAR = 1;

    /** Code 39 1D format. */
    public static final int CODE_39 = 2;

    /** Code 93 1D format. */
    public static final int CODE_93 = 3;

    /** Code 128 1D format. */
    public static final int CODE_128 = 4;

    /** Data Matrix 2D barcode format. */
    public static final int DATA_MATRIX = 5;

    /** EAN-8 1D format. */
    public static final int EAN_8 = 6;

    /** EAN-13 1D format. */
    public static final int EAN_13 = 7;

    /** ITF (Interleaved Two of Five) 1D format. */
    public static final int ITF = 8;

    /** MaxiCode 2D barcode format. */
    public static final int MAXICODE = 9;

    /** PDF417 format. */
    public static final int PDF_417 = 10;

    /** QR Code 2D barcode format. */
    public static final int QR_CODE = 11;

    /** RSS 14 */
    public static final int RSS_14 = 12;

    /** RSS EXPANDED */
    public static final int RSS_EXPANDED = 13;

    /** UPC-A 1D format. */
    public static final int UPC_A = 14;

    /** UPC-E 1D format. */
    public static final int UPC_E = 15;

    /** UPC/EAN extension format. Not a stand-alone format. */
    public static final int UPC_EAN_EXTENSION = 16;

    private static final SparseArray<com.google.zxing.BarcodeFormat> ZXING_FORMAT;

    static{
        ZXING_FORMAT = new SparseArray<>();
        ZXING_FORMAT.put(AZTEC,com.google.zxing.BarcodeFormat.AZTEC);
        ZXING_FORMAT.put(CODABAR,com.google.zxing.BarcodeFormat.CODABAR);
        ZXING_FORMAT.put(CODE_39,com.google.zxing.BarcodeFormat.CODE_39);
        ZXING_FORMAT.put(CODE_93,com.google.zxing.BarcodeFormat.CODE_93);
        ZXING_FORMAT.put(CODE_128,com.google.zxing.BarcodeFormat.CODE_128);
        ZXING_FORMAT.put(DATA_MATRIX,com.google.zxing.BarcodeFormat.DATA_MATRIX);
        ZXING_FORMAT.put(EAN_8,com.google.zxing.BarcodeFormat.EAN_8);
        ZXING_FORMAT.put(EAN_13,com.google.zxing.BarcodeFormat.EAN_13);
        ZXING_FORMAT.put(ITF,com.google.zxing.BarcodeFormat.ITF);
        ZXING_FORMAT.put(MAXICODE,com.google.zxing.BarcodeFormat.MAXICODE);
        ZXING_FORMAT.put(PDF_417,com.google.zxing.BarcodeFormat.PDF_417);
        ZXING_FORMAT.put(QR_CODE,com.google.zxing.BarcodeFormat.QR_CODE);
        ZXING_FORMAT.put(RSS_14,com.google.zxing.BarcodeFormat.RSS_14);
        ZXING_FORMAT.put(RSS_EXPANDED,com.google.zxing.BarcodeFormat.RSS_EXPANDED);
        ZXING_FORMAT.put(UPC_A,com.google.zxing.BarcodeFormat.UPC_A);
        ZXING_FORMAT.put(UPC_E,com.google.zxing.BarcodeFormat.UPC_E);
        ZXING_FORMAT.put(UPC_EAN_EXTENSION,com.google.zxing.BarcodeFormat.UPC_EAN_EXTENSION);
    }

    public static int conversion(com.google.zxing.BarcodeFormat barcodeFormat){
        return ZXING_FORMAT.indexOfValue(barcodeFormat);
    }

    public static com.google.zxing.BarcodeFormat getZxingFormat(int type){
        return ZXING_FORMAT.get(type);
    }
}
