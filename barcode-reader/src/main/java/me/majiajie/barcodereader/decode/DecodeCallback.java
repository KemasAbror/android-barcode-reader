package me.majiajie.barcodereader.decode;

/**
 * 扫码回调
 */
public interface DecodeCallback {

    /**
     * 扫码失败
     */
    void onDecodeFailed();

    /**
     * 扫码成功
     * @param result    扫码信息
     */
    void onDecodeSucceed(DecodeResult result);

}
