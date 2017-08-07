package com.apside.faceheroes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.apside.faceheroes.external.GraphicOverlay;



class HeroGraphic extends GraphicOverlay.Graphic {

    private volatile PointF mLeftPosition;
    private volatile PointF mRightPosition;

    private Paint mMaskColor;

    private float MASK_STROKE_SIZE = 0.2f;
    private float MASK_WIDTH_FACTOR = 0.30f;


    HeroGraphic(GraphicOverlay overlay) {
        super(overlay);
        mMaskColor = new Paint();
        mMaskColor.setColor(Color.BLACK);
        mMaskColor.setStyle(Paint.Style.STROKE);
        mMaskColor.setStrokeWidth(MASK_STROKE_SIZE);
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

        // Use the inter-eye distance to set the size of the eyes.
        float distance = (float) Math.sqrt(
                Math.pow(rightPosition.x - leftPosition.x, 2) +
                        Math.pow(rightPosition.y - leftPosition.y, 2));
        float radius = MASK_WIDTH_FACTOR * distance;
        mMaskColor.setStrokeWidth(MASK_STROKE_SIZE * distance);

        canvas.drawCircle(leftPosition.x, leftPosition.y, radius, mMaskColor);
        canvas.drawCircle(rightPosition.x, rightPosition.y, radius, mMaskColor);
    }

    void updateEyes(PointF leftPosition, PointF rightPosition) {
        mLeftPosition = leftPosition;

        mRightPosition = rightPosition;

        postInvalidate();
    }
}
