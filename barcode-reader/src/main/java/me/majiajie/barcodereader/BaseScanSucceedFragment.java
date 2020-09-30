package me.majiajie.barcodereader;


import androidx.fragment.app.Fragment;

import me.majiajie.barcodereader.decode.DecodeResult;

/**
 * 用于在扫码成功时在扫码界面进行进一步操作，而不是返回数据到前一个Activity
 */
public abstract class BaseScanSucceedFragment extends Fragment {

    /**
     * 返回扫码结果
     */
    protected abstract void onDecodeSucceed(DecodeResult decodeResult);
}
