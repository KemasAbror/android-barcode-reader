package me.majiajie.android_barcode_reader;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSeekBar;

import java.io.IOException;

import me.majiajie.barcodereader.encode.QRCodeGenerateHelper;

/**
 * <p>
 *     二维码生成演示
 * </p>
 */
public class QRCodeGenerateActivity extends AppCompatActivity{

    private RadioGroup mRadiogroup;
    private AppCompatSeekBar mSeekbar;
    private AppCompatEditText mInput;
    private ImageView mImg;

    private SparseIntArray mColors;

    private int mColor = Color.BLACK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);
        mRadiogroup = findViewById(R.id.radiogroup);
        mSeekbar = findViewById(R.id.seekbar);
        mInput = findViewById(R.id.input);
        mImg = findViewById(R.id.img);

        mColors = new SparseIntArray();
        mColors.put(R.id.radio_black,Color.BLACK);
        mColors.put(R.id.radio_red,Color.RED);
        mColors.put(R.id.radio_blue,Color.BLUE);

        mRadiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                mColor = mColors.get(id);
            }
        });

        mInput.setText("123456");
    }

    public void generate(View view) {
        String str = mInput.getText().toString();
        if (TextUtils.isEmpty(str)){
            Toast.makeText(this,"no content",Toast.LENGTH_SHORT).show();
        } else {
            int size = 177 * mSeekbar.getProgress();
            QRCodeGenerateHelper helper = new QRCodeGenerateHelper.Builder(str)
                    .size(size).color(mColor).build();

            try {
                Bitmap bitmap = helper.generateBitmap();
                mImg.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
}
