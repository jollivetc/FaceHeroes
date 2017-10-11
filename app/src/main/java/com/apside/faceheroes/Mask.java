package com.apside.faceheroes;

import android.graphics.drawable.Drawable;


class Mask {

    private Drawable drawable;
    private String name;

    Mask(Drawable drawable, String name) {
        this.drawable = drawable;
        this.name = name;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public String toString() {
        return name;
    }
}
