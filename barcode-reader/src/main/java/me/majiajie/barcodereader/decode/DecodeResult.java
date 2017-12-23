package me.majiajie.barcodereader.decode;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 解码结果
 */
public class DecodeResult implements Parcelable{

    /**
     * 条码内容
     */
    private String text;

    /**
     * 扫码图像的缩略图数据
     */
    private byte[] thumbnail;

    /**
     * 缩略图缩放比例
     */
    private float scaledFactor;

    /**
     * 条码类型
     */
    private int format;

    public DecodeResult(String text, byte[] thumbnail, float scaledFactor, int format) {
        this.text = text;
        this.thumbnail = thumbnail;
        this.scaledFactor = scaledFactor;
        this.format = format;
    }

    protected DecodeResult(Parcel in) {
        text = in.readString();
        thumbnail = in.createByteArray();
        scaledFactor = in.readFloat();
        format = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeByteArray(thumbnail);
        dest.writeFloat(scaledFactor);
        dest.writeInt(format);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DecodeResult> CREATOR = new Creator<DecodeResult>() {
        @Override
        public DecodeResult createFromParcel(Parcel in) {
            return new DecodeResult(in);
        }

        @Override
        public DecodeResult[] newArray(int size) {
            return new DecodeResult[size];
        }
    };

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public float getScaledFactor() {
        return scaledFactor;
    }

    public void setScaledFactor(float scaledFactor) {
        this.scaledFactor = scaledFactor;
    }
}
