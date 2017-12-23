package me.majiajie.android_barcode_reader;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by mjj on 2017/12/15
 */
public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
