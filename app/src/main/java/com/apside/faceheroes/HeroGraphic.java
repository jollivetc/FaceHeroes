package com.apside.faceheroes;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Size;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;


class HeroGraphic extends GraphicOverlay.Graphic {

    private volatile PointF mLeftPosition;
    private volatile PointF mRightPosition;
    private final static int TEMPLATE_WIDTH = 227;
    private final static int TEMPLATE_HEIGHT = 288;

    private final static int TEMPLATE_INTER_EYES_DISTANCE = 60;

    private Drawable drawable;
    private Resources resources;


    HeroGraphic(GraphicOverlay overlay, Drawable drawable, Resources resources) {
        super(overlay);
        this.drawable = drawable;
        this.resources = resources;
    }

    @Override
    public void draw(Canvas canvas) {
        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null)) {
            return;
        }

        PointF leftEyePosition =
                new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightEyePosition =
                new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));

        double rotationAngle = Math.atan((leftEyePosition.y-rightEyePosition.y)/(leftEyePosition.x-rightEyePosition.x));

        float eyesDistance = (float)(rightEyePosition.y-leftEyePosition.y==0 ?
                rightEyePosition.x-leftEyePosition.x :
                Math.sqrt(Math.pow((rightEyePosition.x-leftEyePosition.x), 2)+ Math.pow((rightEyePosition.y - leftEyePosition.y), 2)));

        float resizeFactor = (eyesDistance)/ TEMPLATE_INTER_EYES_DISTANCE;

        PointF faceCenter = new PointF(leftEyePosition.x + (rightEyePosition.x - leftEyePosition.x) / 2, leftEyePosition.y + (rightEyePosition.y - leftEyePosition.y) / 2);

        Size finalDrawableSize = computeNewDrawableSize(resizeFactor, Math.abs(rotationAngle));

        Drawable drawableToDraw = rotateDrawable(rotationAngle);

        float top = faceCenter.y - finalDrawableSize.getHeight()/2;
        float left = faceCenter.x- finalDrawableSize.getWidth()/2;
        float right = faceCenter.x+finalDrawableSize.getWidth()/2;
        float bottom = faceCenter.y+ finalDrawableSize.getHeight()/2;

        drawableToDraw.setBounds(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
        drawableToDraw.draw(canvas);

    }

    @NonNull
    private Drawable rotateDrawable(double rotationAngle) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate((float)(Math.toDegrees(rotationAngle)));
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        return new BitmapDrawable(resources, rotatedBitmap);
    }

    private Size computeNewDrawableSize(float resizeFactor, double rotationAngle){
        float resizeWidth = TEMPLATE_WIDTH * resizeFactor;
        float resizeHeight = TEMPLATE_HEIGHT * resizeFactor;
        float finalWidth = (float) (resizeWidth * Math.cos(rotationAngle) + resizeHeight * Math.sin(rotationAngle));
        float finalHeight = (float) (resizeWidth * Math.sin(rotationAngle) + resizeHeight * Math.cos(rotationAngle));

        return new Size(Math.round(finalWidth), Math.round(finalHeight));
    }

    void updateEyes(PointF leftPosition, PointF rightPosition) {
        mLeftPosition = leftPosition;
        mRightPosition = rightPosition;

        postInvalidate();
    }

}
