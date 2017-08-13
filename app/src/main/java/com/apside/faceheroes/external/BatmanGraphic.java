package com.apside.faceheroes;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import com.apside.faceheroes.external.GraphicOverlay;



class BatmanGraphic extends GraphicOverlay.Graphic {

    private volatile PointF mLeftPosition;
    private volatile PointF mRightPosition;

    private Drawable drawable;


    BatmanGraphic(GraphicOverlay overlay, Drawable drawable) {
        super(overlay);
        this.drawable = drawable;
    }

    @Override
    public void draw(Canvas canvas) {
        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null)) {
            return;
        }

        PointF leftPosition =
                new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightPosition =
                new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));




        drawable.setBounds(Math.round(leftPosition.x-200), Math.round(leftPosition.y-250), Math.round(rightPosition.x+200), Math.round(leftPosition.y+280));
        drawable.draw(canvas);

    }

    void updateEyes(PointF leftPosition, PointF rightPosition) {
        mLeftPosition = leftPosition;

        mRightPosition = rightPosition;

        postInvalidate();
    }
}
