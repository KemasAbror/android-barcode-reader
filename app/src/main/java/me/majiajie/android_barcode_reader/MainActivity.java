package me.majiajie.android_barcode_reader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import me.majiajie.barcodereader.ScanActivity;
import me.majiajie.barcodereader.decode.DecodeResult;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ScanActivity.REQUEST_CODE:{// 扫码返回
                if (resultCode == Activity.RESULT_OK){// 扫码成功
                    DecodeResult decodeResult = ScanActivity.getResult(data);
                    showToast(decodeResult.getText());
                } else if (resultCode == Activity.RESULT_CANCELED){// 扫码取消
                    showToast("cancel");
                }
            }
        }
    }

    public void startScanActivity(View view) {
        // 只解析二维码
//        ScanActivity.startActivityForResult(this,0, new int[]{BarcodeFormat.QR_CODE});
        // 第三个参数传空，默认解析二维码和CODE128
        ScanActivity.startActivityForResult(this,R.style.FullSercen, null);
    }

    public void goQrCodeGenerateActivity(View view) {
        Intent intent = new Intent(this,QRCodeGenerateActivity.class);
        startActivity(intent);
    }

    public void goBarCodeGenerateActivity(View view) {
        Intent intent = new Intent(this,BarCodeGenerateActivity.class);
        startActivity(intent);
    }

    private void showToast(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }

}
