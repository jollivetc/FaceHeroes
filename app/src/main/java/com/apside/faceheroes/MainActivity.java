package com.apside.faceheroes;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.apside.faceheroes.external.CameraSourcePreview;
import com.apside.faceheroes.external.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG="FaceTracker";

    private CameraSource mCameraSource=null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_WRITE_STORAGE_PERM = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        }else{
            requestCameraPermission();
        }
        int rs = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rs != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        }


        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date now = new Date();
                final String formattedDate = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now).toString();

                try {
                    // image naming and path  to include sd card  appending name you choose for file
                    String mPath = Environment.getExternalStorageDirectory().toString() + "/" + formattedDate + ".jpg";
                    Log.i("FaceHero", "Path = " + mPath);
                    Log.i("FaceHero", "Preview Size " + mCameraSource.getPreviewSize().toString());

                    mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes) {
                            Bitmap cameraBitmap = decodeCameraBitmap(bytes);
                            // create bitmap screen capture
                            View overlayView = getWindow().getDecorView().findViewById(R.id.topLayout);
                            overlayView.setDrawingCacheEnabled(true);
                            Bitmap overlayBitmap = getOverlayBitmap(overlayView);
                            Log.i("FaceHero", "RootView is " + overlayView.getWidth() + " " + overlayView.getHeight());
                            Bitmap bmFinal = Bitmap.createBitmap((int)(Math.round(cameraBitmap.getWidth()/3.2)),
                                    (int)(Math.round(cameraBitmap.getHeight()/3.2)),
                                    overlayBitmap.getConfig());
                            Bitmap mirroredCameraBitmap = getResizedAndMirroredCameraBitmap(cameraBitmap, bmFinal);

                            Log.i("FaceHero", "Camera is " + cameraBitmap.getWidth() + " " + cameraBitmap.getHeight());
                            Log.i("FaceHero", "Overlay is " + overlayBitmap.getWidth() + " " + overlayBitmap.getHeight());

                            Canvas canvas = new Canvas(bmFinal);
                            //canvas.drawBitmap(mirroredCameraBitmap, new Matrix(), null);
                            canvas.drawBitmap(mirroredCameraBitmap, 0,0, null);
                            //BitmapDrawable overlayDrawable = new BitmapDrawable(getResources(), overlayBitmap);
                            //overlayDrawable.setBounds(-180,0,bmFinal.getWidth()-180, bmFinal.getHeight());
                            //Bitmap croppedOverlay = Bitmap.createBitmap(overlayBitmap, 0, overlayBitmap.getHeight()-mirroredCameraBitmap.getHeight(), mirroredCameraBitmap.getWidth(), overlayBitmap.getHeight());
                            //Bitmap croppedOverlay = Bitmap.createBitmap(overlayBitmap, 0, 0, mirroredCameraBitmap.getWidth(), mirroredCameraBitmap.getHeight());
                            //overlayDrawable.draw(canvas);
                            canvas.drawBitmap(overlayBitmap, 180, 0, null);
                            Log.i("FaceHero", "mirroredCameraBitmap is " + mirroredCameraBitmap.getWidth() + " " + mirroredCameraBitmap.getHeight());
                            //Log.i("FaceHero", "croppedOverlay is " + croppedOverlay.getWidth() + " " + croppedOverlay.getHeight());
                            MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), bmFinal,formattedDate + "3.jpg", "nice screenshot");
                            overlayView.setDrawingCacheEnabled(false);
                        }

                        private Bitmap getResizedAndMirroredCameraBitmap(Bitmap cameraBitmap, Bitmap bmFinal) {
                            Bitmap resizeCameraImage = Bitmap.createScaledBitmap(cameraBitmap, bmFinal.getWidth(), bmFinal.getHeight(), false);
                            Matrix m = new Matrix();
                            m.preScale(-1, 1);
                            return Bitmap.createBitmap(resizeCameraImage, 0, 0, resizeCameraImage.getWidth(), resizeCameraImage.getHeight(), m, false);
                        }

                        private Bitmap getOverlayBitmap(View rootView) {
                            return rootView.getDrawingCache();
                        }

                        private Bitmap decodeCameraBitmap(byte[] bytes) {
                            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        }
                    });
                    Log.i("FaceHero", "Screenshot !");

                } catch (Throwable e) {
                    // Several error may come out with file handling or DOM
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }



    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = createFaceDetector(context);

        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(320, 240)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();

    }

    private FaceDetector createFaceDetector(Context context) {
        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(false)
                .setMinFaceSize(0.15f)
                .build();
        Detector.Processor<Face> processor;
        MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>(){
            @Override
            public Tracker<Face> create (Face face){
                 HeroFacetracker tracker;
                int rand = (int) Math.round(Math.random()*4);
                switch (rand) {
                    case 0 :
                        tracker = new HeroFacetracker(mGraphicOverlay, getResources().getDrawable(R.drawable.batman), getResources());
                        break;
                    case 1 :
                        tracker = new HeroFacetracker(mGraphicOverlay, getResources().getDrawable(R.drawable.flash), getResources());
                        break;
                    case 2 :
                        tracker = new HeroFacetracker(mGraphicOverlay, getResources().getDrawable(R.drawable.greenlantern), getResources());
                        break;
                   default :
                        tracker = new HeroFacetracker(mGraphicOverlay, getResources().getDrawable(R.drawable.wonderwoman), getResources());
                        break;
                }
                return tracker;
            }
        };
        processor = new MultiProcessor.Builder<>(factory).build();

        detector.setProcessor(processor);

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Face detector cannot be downloaded due to ...", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Face detector dependencies cannot be downloaded due to ...");
            }
        }
        return detector;
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted, Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }
        final Activity thisActivity = this;

        View.OnClickListener listener =new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, "Access to the camera is needed for detection",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show();
    }

    private void requestStoragePermission(){
        Log.w(TAG, "storage permission is not granted, Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_WRITE_STORAGE_PERM);
            return;
        }
        final Activity thisActivity = this;

        View.OnClickListener listener =new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_WRITE_STORAGE_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, "Access to the storage is needed for screenshot",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCameraSource != null){
            mCameraSource.release();
        }
    }

    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null){
            try{
                mPreview.start(mCameraSource, mGraphicOverlay);
            }catch(IOException e){
                Log.e(TAG, "Unable to start camera source.",e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }
}
