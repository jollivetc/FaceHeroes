package com.apside.faceheroes;

import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import com.apside.faceheroes.external.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.apside.faceheroes.BatmanGraphic;

import java.util.HashMap;
import java.util.Map;


class BatmanFacetracker extends Tracker<Face> {

    private GraphicOverlay mOverlay;
    private BatmanGraphic mBatmanGraphic;
    private Drawable drawable;

    // Record the previously seen proportions of the landmark locations relative to the bounding box
    // of the face.  These proportions can be used to approximate where the landmarks are within the
    // face bounding box if the eye landmark is missing in a future update.
    private Map<Integer, PointF> mPreviousProportions = new HashMap<>();

    BatmanFacetracker(GraphicOverlay mGraphicOverlay) {
        mOverlay = mGraphicOverlay;
    }

    public void setDrawable(Drawable drawable){
        this.drawable = drawable;
    }

    @Override
    public void onNewItem(int i, Face face) {
        mBatmanGraphic = new BatmanGraphic(mOverlay, drawable);
    }

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        mOverlay.add(mBatmanGraphic);

        updatePreviousProportions(face);

        PointF leftPosition = getLandmarkPosition(face, Landmark.LEFT_EYE);
        PointF rightPosition = getLandmarkPosition(face, Landmark.RIGHT_EYE);

        mBatmanGraphic.updateEyes(leftPosition, rightPosition);
    }

    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mBatmanGraphic);
    }

    @Override
    public void onDone() {
        mOverlay.remove(mBatmanGraphic);
    }

    private void updatePreviousProportions(Face face) {
        for (Landmark landmark : face.getLandmarks()) {
            PointF position = landmark.getPosition();
            float xProp = (position.x - face.getPosition().x) / face.getWidth();
            float yProp = (position.y - face.getPosition().y) / face.getHeight();
            mPreviousProportions.put(landmark.getType(), new PointF(xProp, yProp));
        }
    }

    private PointF getLandmarkPosition(Face face, int landmarkId) {
        for (Landmark landmark : face.getLandmarks()) {
            if (landmark.getType() == landmarkId) {
                return landmark.getPosition();
            }
        }

        PointF prop = mPreviousProportions.get(landmarkId);
        if (prop == null) {
            return null;
        }

        float x = face.getPosition().x + (prop.x * face.getWidth());
        float y = face.getPosition().y + (prop.y * face.getHeight());
        return new PointF(x, y);
    }
}
