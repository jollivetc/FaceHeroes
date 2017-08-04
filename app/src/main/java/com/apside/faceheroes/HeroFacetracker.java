package com.apside.faceheroes;

import android.graphics.PointF;

import com.apside.faceheroes.external.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.HashMap;
import java.util.Map;


class HeroFacetracker extends Tracker<Face> {

    private GraphicOverlay mOverlay;
    private HeroGraphic mHeroGraphic;

    // Record the previously seen proportions of the landmark locations relative to the bounding box
    // of the face.  These proportions can be used to approximate where the landmarks are within the
    // face bounding box if the eye landmark is missing in a future update.
    private Map<Integer, PointF> mPreviousProportions = new HashMap<>();

    HeroFacetracker(GraphicOverlay mGraphicOverlay) {
        mOverlay = mGraphicOverlay;
    }

    @Override
    public void onNewItem(int i, Face face) {
        mHeroGraphic = new HeroGraphic(mOverlay);
    }

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        mOverlay.add(mHeroGraphic);

        updatePreviousProportions(face);

        PointF leftPosition = getLandmarkPosition(face, Landmark.LEFT_EYE);
        PointF rightPosition = getLandmarkPosition(face, Landmark.RIGHT_EYE);

        mHeroGraphic.updateEyes(leftPosition, rightPosition);
    }

    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mHeroGraphic);
    }

    @Override
    public void onDone() {
        mOverlay.remove(mHeroGraphic);
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
