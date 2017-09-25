package com.apside.faceheroes;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;


class Mask {

    private Bitmap bitmap;
    private Drawable drawable;
    private String name;

    Mask(Bitmap bitmap, Drawable drawable, String name) {
        this.bitmap = bitmap;
        this.drawable = drawable;
        this.name = name;
    }

    Bitmap getBitmap(){
        return bitmap;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public String toString() {
        return name;
    }
}
