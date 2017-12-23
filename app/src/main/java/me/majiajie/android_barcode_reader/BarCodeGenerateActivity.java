package me.majiajie.android_barcode_reader;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;

import me.majiajie.barcodereader.encode.BarCodeGenerateHelper;

/**
 * 条形码生成演示
 */
public class BarCodeGenerateActivity extends AppCompatActivity {

    private RadioGroup mRadiogroup;
    private AppCompatSeekBar mSeekbar;
    private AppCompatSeekBar mSeekbarB;
    private AppCompatEditText mInput;
    private ImageView mImg;

    private SparseIntArray mColors;

    private int mColor = Color.BLACK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_barcode);

        mRadiogroup = findViewById(R.id.radiogroup);
        mSeekbar = findViewById(R.id.seekbar);
        mSeekbarB = findViewById(R.id.seekbar_b);
        mInput = findViewById(R.id.input);
        mImg = findViewById(R.id.img);

        mColors = new SparseIntArray();
        mColors.put(R.id.radio_black, Color.BLACK);
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
            int width = 400 + 200 * mSeekbar.getProgress();
            int height = 200 + 200 * mSeekbarB.getProgress();

            BarCodeGenerateHelper helper = new BarCodeGenerateHelper(str,width,height,mColor);
            try {
                Bitmap bitmap = helper.generate();
                mImg.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
}
