# android-barcode-reader [![GitHub release](https://api.bintray.com/packages/tyzlmjj/maven/barcode-reader/images/download.svg)](https://github.com/tyzlmjj/android-barcode-reader/releases)

这个库使用[zxing](https://github.com/zxing/zxing)实现了扫码的UI，方便使用。

[Demo.apk](https://github.com/tyzlmjj/android-barcode-reader/releases/download/0.1.6/Demo.apk)

**目前实现的功能:**

- 调用Activity直接扫码
- 生成二维码和条码(颜色、大小的控制)

## 添加gradle依赖

```
implementation 'me.majiajie:barcode-reader:0.1.6'
implementation 'com.google.zxing:core:3.3.0'
implementation 'com.android.support:appcompat-v7:+'
```

## 使用

- **调用Activity扫码**

直接启动Activity。第二个参数可以设置Activity的主题，第三个参数设置扫码类型
```java
ScanActivity.startActivityForResult(context,0, new int[]{BarcodeFormat.QR_CODE});
```
在`onActivityResult`中接收扫码结果
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode){
        case ScanActivity.REQUEST_CODE:{// 扫码返回
            if (resultCode == Activity.RESULT_OK){// 扫码成功
                DecodeResult decodeResult = ScanActivity.getResult(data);
                // do something
            } else if (resultCode == Activity.RESULT_CANCELED){// 扫码取消
                // do something
            }
        }
    }
}
```

------

- **生成条码**

分两个类:

[BarCodeGenerateHelper](https://github.com/tyzlmjj/android-barcode-reader/blob/master/barcode-reader/src/main/java/me/majiajie/barcodereader/encode/BarCodeGenerateHelper.java) 用于生成条形码

[QRCodeGenerateHelper](https://github.com/tyzlmjj/android-barcode-reader/blob/master/barcode-reader/src/main/java/me/majiajie/barcodereader/encode/QRCodeGenerateHelper.java) 用于生成二维码

以生成二维码为例

```java
QRCodeGenerateHelper helper = new QRCodeGenerateHelper("二维码内容");
Bitmap bitmap = helper.generate();
```


  

