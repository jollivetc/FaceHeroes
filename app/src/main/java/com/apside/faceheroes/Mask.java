package com.apside.faceheroes;

import android.graphics.Bitmap;


class Mask {

    private Bitmap bitmap;
    private String name;

    public Mask(Bitmap bitmap, String name) {
        this.bitmap = bitmap;
        this.name = name;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public String getName() {
        return name;
    }
}
