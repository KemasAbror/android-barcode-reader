package me.majiajie.android_barcode_reader;

import android.content.Context;
import android.widget.Toast;

import me.majiajie.barcodereader.BaseScanSucceedFragment;
import me.majiajie.barcodereader.decode.DecodeResult;

/**
 * 在Fragment中处理扫码结果
 */
public class ScanSucceedFragment extends BaseScanSucceedFragment{

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    protected void onDecodeSucceed(DecodeResult decodeResult) {
        Toast.makeText(mContext,decodeResult.getText(),Toast.LENGTH_SHORT).show();
    }
}
