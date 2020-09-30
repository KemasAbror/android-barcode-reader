package me.majiajie.android_barcode_reader;

import android.widget.ImageView;

import me.majiajie.photoalbum.IAlbumImageLoader;

/**
 * Created by mjj on 2019-10-31
 */
public class PhotoAlbumImageLoader implements IAlbumImageLoader {

    @Override
    public void loadLocalImageOrVideo(ImageView imageView, String path) {
        GlideApp.with(imageView).load(path).into(imageView);
    }
}
