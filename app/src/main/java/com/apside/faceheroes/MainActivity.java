package com.apside.faceheroes;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.samples.vision.barcodereader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FaceTracker";
    public static final String PHOTO_ID = "PHOTO_ID";

    public final static String MASK_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath()+"/masks";

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private RecyclerView mRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private MaskRequester mMaskRequester;
    private BroadcastReceiver downloadReceiver;

    public static List<Mask> mListMask = Collections.synchronizedList(new ArrayList<Mask>());
    private Map<String, Mask> maskMap = new HashMap<>();

    private MaskAdapter mMaskAdapter;
    private final List<HeroFacetracker> trackerList = new ArrayList<>();


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
        } else {
            requestCameraPermission();
        }
        int rs = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rs != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        try {
            checkForNewMasks();
        } catch (IOException e) {
            Log.e("FaceHeroes", "Error downloading masks", e);
        }
        loadMask();
        mListMask.clear();
        mListMask.addAll(maskMap.values());

        Log.i("FaceHeroes", "size map " + maskMap.size());
        Log.i("FaceHeroes", "size list " + mListMask.size());


        mMaskAdapter = new MaskAdapter(mListMask, trackerList);
        mRecyclerView.setAdapter(mMaskAdapter);
        mMaskAdapter.notifyItemInserted(mListMask.size());

        findViewById(R.id.captureBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        Snackbar.make(mGraphicOverlay, "Souriez dans " + millisUntilFinished / 1000, 800)
                                .show();
                    }

                    @Override
                    public void onFinish() {
                        Date now = new Date();
                        final String formattedDate = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now).toString();

                        Bitmap cameraPreviewBitmap = mPreview.getCameraPreviewBitmap();

                        View overlayView = getWindow().getDecorView().findViewById(R.id.faceOverlay);
                        overlayView.setDrawingCacheEnabled(true);
                        Bitmap drawingCache = overlayView.getDrawingCache();

                        Log.i("FaceHero", "cameraBitmap is " + cameraPreviewBitmap.getWidth() + " " + cameraPreviewBitmap.getHeight());
                        Log.i("FaceHero", "overlayBitmap is " + drawingCache.getWidth() + " " + drawingCache.getHeight());
                        Bitmap bmOverlay = Bitmap.createBitmap(cameraPreviewBitmap);
                        Canvas canvas = new Canvas(bmOverlay);
                        canvas.drawBitmap(cameraPreviewBitmap, new Matrix(), null);
                        int left = (cameraPreviewBitmap.getWidth() - drawingCache.getWidth()) / 2;
                        int top = (cameraPreviewBitmap.getHeight() - drawingCache.getHeight()) / 2;

                        Log.i("FaceHero", "offsets are " + left + " " + top);
                        canvas.drawBitmap(drawingCache, 0, 0, null);
                        MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), bmOverlay, formattedDate + ".jpg", "nice screenshot");
                        overlayView.setDrawingCacheEnabled(false);

                        Context context = view.getContext();
                        Intent showForm = new Intent(context, MailActivity.class);
                        showForm.putExtra(PHOTO_ID, formattedDate + ".jpg");
                        context.startActivity(showForm);
                    }
                }.start();
            }
        });
        getMaskNameList();
    }

    private void loadMask() {
        List<String> maskNameList = getMaskNameList();

        for (String maskName : maskNameList) {
            if (maskMap.containsKey(maskName)) {
                continue;
            }
            Mask mask = new Mask(Drawable.createFromPath(MASK_DIRECTORY+"/"+maskName), maskName);
            maskMap.put(maskName, mask);
        }
    }

    private void checkForNewMasks() throws IOException {
        mMaskRequester = new MaskRequester(this);
        mMaskRequester.getMasksList();
    }

    private List<String> getMaskNameList(){
        List<String> fileNames = new ArrayList<>();
        File directory = new File(MASK_DIRECTORY);
        File[] files = directory.listFiles();
        for (File file : files) {
            fileNames.add(file.getName());
        }
        Collections.sort(fileNames);
        return fileNames;
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
                .setRequestedPreviewSize(1440, 1080)
                .setRequestedFps(60.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
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
        MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>() {
            @Override
            public Tracker<Face> create(Face face) {
                int rand = (int) Math.round(Math.random() * mListMask.size());
                HeroFacetracker tracker = new HeroFacetracker(mGraphicOverlay, mListMask.get(rand).getDrawable(), getResources());
                trackerList.add(tracker);
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

        View.OnClickListener listener = new View.OnClickListener() {
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

    private void requestStoragePermission() {
        Log.w(TAG, "storage permission is not granted, Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_WRITE_STORAGE_PERM);
            return;
        }
        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
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
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

}
