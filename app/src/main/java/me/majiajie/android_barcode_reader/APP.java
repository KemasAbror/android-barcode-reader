package me.majiajie.android_barcode_reader;

import android.app.Application;

import me.majiajie.photoalbum.Album;

/**
 * Created by mjj on 2019-10-31
 */
public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Album.init(new PhotoAlbumImageLoader());
    }
}
