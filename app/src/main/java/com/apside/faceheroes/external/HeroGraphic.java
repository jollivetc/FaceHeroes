package com.apside.faceheroes;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import com.apside.faceheroes.external.GraphicOverlay;


class HeroGraphic extends GraphicOverlay.Graphic {

    private volatile PointF mLeftPosition;
    private volatile PointF mRightPosition;
    private final int INTER_EYES = 60;
    private final int LEFT_BORDER = -90;
    private final int TOP_BORDER = -144;
    private final int RIGHT_BORDER = 146;
    private final int BOTTOM_BORDER = 163;

    private Drawable drawable;


    HeroGraphic(GraphicOverlay overlay, Drawable drawable) {
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

        float factor = (rightPosition.x-leftPosition.x)/INTER_EYES;
        float top = leftPosition.y+TOP_BORDER*factor;
        float left = leftPosition.x+LEFT_BORDER*factor;
        float right = leftPosition.x+RIGHT_BORDER*factor;
        float bottom = leftPosition.y+BOTTOM_BORDER*factor;
        drawable.setBounds(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
        drawable.draw(canvas);

    }

    void updateEyes(PointF leftPosition, PointF rightPosition) {
        mLeftPosition = leftPosition;
        mRightPosition = rightPosition;

        postInvalidate();
    }
}
