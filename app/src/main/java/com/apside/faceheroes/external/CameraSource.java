package com.apside.faceheroes.external;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//  Decompilation of com.google.android.gms.vision.CameraSource to add start(SurfaceTexture surfaceTexture) method

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CameraSource {
    @SuppressLint({"InlinedApi"})
    public static final int CAMERA_FACING_BACK = 0;
    @SuppressLint({"InlinedApi"})
    public static final int CAMERA_FACING_FRONT = 1;
    private Context mContext;
    private final Object mCameraLock;
    private Camera mCamera;
    private int mFacing;
    private int mRotation;
    private Size mPreviewSize;
    private float mRequestedFps;
    private int mRequestedPreviewWidth;
    private int mRequestedPreviewHeight;
    private boolean mFocusMode;
    private SurfaceView surfaceView;
    private SurfaceTexture surfaceTexture;
    private boolean aDN;
    private Thread mProcessingThread;
    private FrameProcessingRunnable mFrameProcessor;
    private Map<byte[], ByteBuffer> mBytesToByteBuffer;

    private static final String TAG = "OpenCameraSource";
    /**
     * If the absolute difference between a preview size aspect ratio and a picture size aspect
     * ratio is less than this tolerance, they are considered to be the same aspect ratio.
     */
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;


    public void release() {
        Object var1 = this.mCameraLock;
        synchronized(this.mCameraLock) {
            this.stop();
            this.mFrameProcessor.release();
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start() throws IOException {
        Object var1 = this.mCameraLock;
        synchronized(this.mCameraLock) {
            if(this.mCamera != null) {
                return this;
            } else {
                this.mCamera = this.createCamera();
                if(Build.VERSION.SDK_INT >= 11) {
                    this.surfaceTexture = new SurfaceTexture(100);
                    this.mCamera.setPreviewTexture(this.surfaceTexture);
                    this.aDN = true;
                } else {
                    this.surfaceView = new SurfaceView(this.mContext);
                    this.mCamera.setPreviewDisplay(this.surfaceView.getHolder());
                    this.aDN = false;
                }

                this.mCamera.startPreview();
                this.mProcessingThread = new Thread(this.mFrameProcessor);
                this.mFrameProcessor.setActive(true);
                this.mProcessingThread.start();
                return this;
            }
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start(SurfaceTexture surfaceTexture)throws IOException{
        Object var2 = this.mCameraLock;
        synchronized (this.mCameraLock){
            if(this.mCamera != null){
                return this;
            }else{
                this.mCamera = this.createCamera();
                this.mCamera.setPreviewTexture(surfaceTexture);
                this.aDN = true;
                this.mCamera.startPreview();
                this.mProcessingThread = new Thread(this.mFrameProcessor);
                this.mFrameProcessor.setActive(true);
                this.mProcessingThread.start();
                return this;
            }
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start(SurfaceHolder var1) throws IOException {
        Object var2 = this.mCameraLock;
        synchronized(this.mCameraLock) {
            if(this.mCamera != null) {
                return this;
            } else {
                this.mCamera = this.createCamera();
                this.mCamera.setPreviewDisplay(var1);
                this.mCamera.startPreview();
                this.mProcessingThread = new Thread(this.mFrameProcessor);
                this.mFrameProcessor.setActive(true);
                this.mProcessingThread.start();
                this.aDN = false;
                return this;
            }
        }
    }

    public void stop() {
        synchronized(this.mCameraLock) {
            this.mFrameProcessor.setActive(false);
            if(this.mProcessingThread != null) {
                try {
                    this.mProcessingThread.join();
                } catch (InterruptedException var6) {
                    Log.d("CameraSource", "Frame processing thread interrupted on release.");
                }

                this.mProcessingThread = null;
            }

            if(this.mCamera != null) {
                this.mCamera.stopPreview();
                this.mCamera.setPreviewCallbackWithBuffer((Camera.PreviewCallback)null);

                try {
                    if(this.aDN) {
                        this.mCamera.setPreviewTexture(null);
                    } else {
                        this.mCamera.setPreviewDisplay(null);
                    }
                } catch (Exception var5) {
                    String var3 = String.valueOf(var5);
                    Log.e("CameraSource", (new StringBuilder(32 + String.valueOf(var3).length())).append("Failed to clear camera preview: ").append(var3).toString());
                }

                this.mCamera.release();
                this.mCamera = null;
            }

            this.mBytesToByteBuffer.clear();
        }
    }

    public Size getPreviewSize() {
        return this.mPreviewSize;
    }

    public int getCameraFacing() {
        return this.mFacing;
    }

    public void takePicture(com.google.android.gms.vision.CameraSource.ShutterCallback var1, com.google.android.gms.vision.CameraSource.PictureCallback var2) {
        synchronized(this.mCameraLock) {
            if(this.mCamera != null) {
                PictureStartCallback var4 = new PictureStartCallback();
                var4.mDelegate = var1;
                PictureDoneCallback var5 = new PictureDoneCallback();
                var5.delegate = var2;
                this.mCamera.takePicture(var4, null, null, var5);
            }

        }
    }

    private CameraSource() {
        this.mCameraLock = new Object();
        this.mFacing = 0;
        this.mRequestedFps = 30.0F;
        this.mRequestedPreviewWidth = 1024;
        this.mRequestedPreviewHeight = 768;
        this.mFocusMode = false;
        this.mBytesToByteBuffer = new HashMap();
    }

    @SuppressLint({"InlinedApi"})
    private Camera createCamera() {
        int requestedCameraId = getIdForRequestedCamera(this.mFacing);
        if(requestedCameraId == -1) {
            throw new RuntimeException("Could not find requested camera.");
        }
        Camera camera = Camera.open(requestedCameraId);

        SizePair sizePair = selectSizePair(camera, this.mRequestedPreviewWidth, this.mRequestedPreviewHeight);
        if(sizePair == null) {
            throw new RuntimeException("Could not find suitable preview size.");
        }
        Size pictureSize = sizePair.pictureSize();
        this.mPreviewSize = sizePair.previewSize();

        int[] previewFpsRange = selectPreviewFpsRange(camera, this.mRequestedFps);
        if(previewFpsRange == null) {
            throw new RuntimeException("Could not find suitable preview frames per second range.");
        }

        Camera.Parameters parameters = camera.getParameters();
        if(pictureSize != null) {
            parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        }

        parameters.setPreviewSize(this.mPreviewSize.getWidth(), this.mPreviewSize.getHeight());
        parameters.setPreviewFpsRange(previewFpsRange[0], previewFpsRange[1]);
        parameters.setPreviewFormat(17);
        this.setRotation(camera, parameters, requestedCameraId);
        if(this.mFocusMode) {
            if(parameters.getSupportedFocusModes().contains("continuous-video")) {
                parameters.setFocusMode("continuous-video");
            } else {
                Log.i("CameraSource", "Camera auto focus is not supported on this device.");
            }
        }

        camera.setParameters(parameters);
        camera.setPreviewCallbackWithBuffer(new CameraPreviewCallback());
        camera.addCallbackBuffer(this.selectSizePair(this.mPreviewSize));
        camera.addCallbackBuffer(this.selectSizePair(this.mPreviewSize));
        camera.addCallbackBuffer(this.selectSizePair(this.mPreviewSize));
        camera.addCallbackBuffer(this.selectSizePair(this.mPreviewSize));
        return camera;
    }

    private static int getIdForRequestedCamera(int var0) {
        Camera.CameraInfo var1 = new Camera.CameraInfo();

        for(int var2 = 0; var2 < Camera.getNumberOfCameras(); ++var2) {
            Camera.getCameraInfo(var2, var1);
            if(var1.facing == var0) {
                return var2;
            }
        }

        return -1;
    }

    static SizePair selectSizePair(Camera camera, int desiredWidth, int desiredHeight) {
        List<SizePair> validPreviewSizes = generateValidPreviewSizeList(camera);
        SizePair selectedPair = null;
        int minDiff = Integer.MAX_VALUE;

        for (SizePair sizePair : validPreviewSizes) {
            Size size = sizePair.previewSize();
            int diff = Math.abs(size.getWidth() - desiredWidth) + Math.abs(size.getHeight() - desiredHeight);
            if (diff < minDiff) {
                selectedPair = sizePair;
                minDiff = diff;
            }
        }

        return selectedPair;
    }

    static List<SizePair> generateValidPreviewSizeList(Camera var0) {
        Camera.Parameters parameters = var0.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();

        for(Camera.Size size : supportedPreviewSizes){
            Log.i(TAG, "preview "+size.width+" "+size.height);
        }
        for(Camera.Size size : supportedPictureSizes){
            Log.i(TAG, "picture"+size.width+" "+size.height);
        }



        List<SizePair> validPreviewSizes = new ArrayList<>();

        for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;

            // By looping through the picture sizes in order, we favor the higher resolutions.
            // We choose the highest resolution in order to support taking the full resolution
            // picture later.
            for (android.hardware.Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new SizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        // If there are no picture sizes with the same aspect ratio as any preview sizes, allow all
        // of the preview sizes and hope that the camera can handle it.  Probably unlikely, but we
        // still account for it.
        if (validPreviewSizes.size() == 0) {
            Log.w(TAG, "No preview sizes have a corresponding same-aspect-ratio picture size");
            for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
                // The null picture size will let us know that we shouldn't set a picture size.
                validPreviewSizes.add(new SizePair(previewSize, null));
            }
        }

        return validPreviewSizes;
    }

    @SuppressLint({"InlinedApi"})
    static int[] selectPreviewFpsRange(Camera var0, float var1) {
        int var2 = (int)(var1 * 1000.0F);
        int[] var3 = null;
        int var4 = 2147483647;
        List var5 = var0.getParameters().getSupportedPreviewFpsRange();
        Iterator var6 = var5.iterator();

        while(var6.hasNext()) {
            int[] var7 = (int[])var6.next();
            int var8 = var2 - var7[0];
            int var9 = var2 - var7[1];
            int var10 = Math.abs(var8) + Math.abs(var9);
            if(var10 < var4) {
                var3 = var7;
                var4 = var10;
            }
        }

        return var3;
    }

    private void setRotation(Camera camera, Camera.Parameters parameters, int cameraId) {
        WindowManager var4 = (WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE);
        short var5 = 0;
        int var6 = var4.getDefaultDisplay().getRotation();
        switch(var6) {
            case 0:
                var5 = 0;
                break;
            case 1:
                var5 = 90;
                break;
            case 2:
                var5 = 180;
                break;
            case 3:
                var5 = 270;
                break;
            default:
                Log.e("CameraSource", (new StringBuilder(31)).append("Bad rotation value: ").append(var6).toString());
        }

        Camera.CameraInfo var7 = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, var7);
        int var8;
        int var9;
        if(var7.facing == 1) {
            var8 = (var7.orientation + var5) % 360;
            var9 = (360 - var8) % 360;
        } else {
            var8 = (var7.orientation - var5 + 360) % 360;
            var9 = var8;
        }

        this.mRotation = var8 / 90;
        camera.setDisplayOrientation(var9);
        parameters.setRotation(var8);
    }

    @SuppressLint({"InlinedApi"})
    private byte[] selectSizePair(Size var1) {
        int var2 = ImageFormat.getBitsPerPixel(17);
        long var3 = (long)(var1.getHeight() * var1.getWidth() * var2);
        int var5 = (int)Math.ceil((double)var3 / 8.0D) + 1;
        byte[] var6 = new byte[var5];
        ByteBuffer var7 = ByteBuffer.wrap(var6);
        if(var7.hasArray() && var7.array() == var6) {
            this.mBytesToByteBuffer.put(var6, var7);
            return var6;
        } else {
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }
    }

    private class FrameProcessingRunnable implements Runnable {
        private Detector<?> mDetector;
        private long mStartTimeMillis = SystemClock.elapsedRealtime();
        private final Object mLock = new Object();
        private boolean mActive = true;
        private long mPendingTimeMillis;
        private int mPendingFrameId = 0;
        private ByteBuffer mPendingFrameData;

        FrameProcessingRunnable(Detector<?> var1) {
            this.mDetector = var1;
        }

        @SuppressLint({"Assert"})
        void release() {
            assert CameraSource.this.mProcessingThread.getState() == Thread.State.TERMINATED;

            this.mDetector.release();
            this.mDetector = null;
        }

        void setActive(boolean var1) {
            Object var2 = this.mLock;
            synchronized(this.mLock) {
                this.mActive = var1;
                this.mLock.notifyAll();
            }
        }

        void setNextFrame(byte[] data, Camera camera) {
            Object var3 = this.mLock;
            synchronized(this.mLock) {
                if(this.mPendingFrameData != null) {
                    camera.addCallbackBuffer(this.mPendingFrameData.array());
                    this.mPendingFrameData = null;
                }

                if(!CameraSource.this.mBytesToByteBuffer.containsKey(data)) {
                    Log.d("CameraSource", "Skipping frame. Could not find ByteBuffer associated with the image data from the camera.");
                } else {
                    this.mPendingTimeMillis = SystemClock.elapsedRealtime() - this.mStartTimeMillis;
                    ++this.mPendingFrameId;
                    this.mPendingFrameData = (ByteBuffer) CameraSource.this.mBytesToByteBuffer.get(data);
                    this.mLock.notifyAll();
                }
            }
        }

        @SuppressLint({"InlinedApi"})
        public void run() {
            while(true) {
                Object var3 = this.mLock;
                Frame outputFrame;
                ByteBuffer data;
                synchronized(this.mLock) {
                    while(this.mActive && this.mPendingFrameData == null) {
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException var13) {
                            Log.d("CameraSource", "Frame processing loop terminated.", var13);
                            return;
                        }
                    }

                    if(!this.mActive) {
                        return;
                    }

                    outputFrame = (new com.google.android.gms.vision.Frame.Builder())
                                .setImageData(this.mPendingFrameData, CameraSource.this.mPreviewSize.getWidth(), CameraSource.this.mPreviewSize.getHeight(), 17)
                                .setId(this.mPendingFrameId)
                                .setTimestampMillis(this.mPendingTimeMillis)
                                .setRotation(CameraSource.this.mRotation)
                                .build();
                    data = this.mPendingFrameData;
                    this.mPendingFrameData = null;
                }

                try {
                    this.mDetector.receiveFrame(outputFrame);
                } catch (Throwable var11) {
                    Log.e("CameraSource", "Exception thrown from receiver.", var11);
                } finally {
                    CameraSource.this.mCamera.addCallbackBuffer(data.array());
                }
            }
        }
    }

    private class CameraPreviewCallback implements Camera.PreviewCallback {
        private CameraPreviewCallback() {
        }

        public void onPreviewFrame(byte[] data, Camera camera) {
            CameraSource.this.mFrameProcessor.setNextFrame(data, camera);
        }
    }

    static class SizePair {
        private Size mPreview;
        private Size mPicture;

        public SizePair(android.hardware.Camera.Size previewSize, android.hardware.Camera.Size pictureSize) {
            this.mPreview = new Size(previewSize.width, previewSize.height);
            if(pictureSize != null) {
                this.mPicture = new Size(pictureSize.width, pictureSize.height);
            }

        }

        public Size previewSize() {
            return this.mPreview;
        }

        public Size pictureSize() {
            return this.mPicture;
        }
    }

    private class PictureDoneCallback implements android.hardware.Camera.PictureCallback {
        private com.google.android.gms.vision.CameraSource.PictureCallback delegate;

        private PictureDoneCallback() {
        }

        public void onPictureTaken(byte[] data, Camera camera) {
            if(this.delegate != null) {
                this.delegate.onPictureTaken(data);
            }

            synchronized(CameraSource.this.mCameraLock) {
                if(CameraSource.this.mCamera != null) {
                    CameraSource.this.mCamera.startPreview();
                }

            }
        }
    }

    private class PictureStartCallback implements android.hardware.Camera.ShutterCallback {
        private com.google.android.gms.vision.CameraSource.ShutterCallback mDelegate;

        private PictureStartCallback() {
        }

        public void onShutter() {
            if(this.mDelegate != null) {
                this.mDelegate.onShutter();
            }

        }
    }

    public interface PictureCallback {
        void onPictureTaken(byte[] var1);
    }

    public interface ShutterCallback {
        void onShutter();
    }

    public static class Builder {
        private final Detector<?> mDetector;
        private CameraSource mCameraSource = new CameraSource();

        public Builder(Context context, Detector<?> detector) {
            if(context == null) {
                throw new IllegalArgumentException("No context supplied.");
            } else if(detector == null) {
                throw new IllegalArgumentException("No detector supplied.");
            } else {
                this.mDetector = detector;
                this.mCameraSource.mContext = context;
            }
        }

        public CameraSource.Builder setRequestedFps(float fps) {
            if(fps <= 0.0F) {
                throw new IllegalArgumentException((new StringBuilder(28)).append("Invalid fps: ").append(fps).toString());
            } else {
                this.mCameraSource.mRequestedFps = fps;
                return this;
            }
        }

        public CameraSource.Builder setRequestedPreviewSize(int width, int height) {
            final int MAX = 1000000;
            if(width > 0 && width <= 1000000 && height > 0 && height <= 1000000) {
                this.mCameraSource.mRequestedPreviewWidth = width;
                this.mCameraSource.mRequestedPreviewHeight = height;
                return this;
            } else {
                throw new IllegalArgumentException((new StringBuilder(45)).append("Invalid preview size: ").append(width).append("x").append(height).toString());
            }
        }

        public CameraSource.Builder setFacing(int facing) {
            if(facing != 0 && facing != 1) {
                throw new IllegalArgumentException((new StringBuilder(27)).append("Invalid camera: ").append(facing).toString());
            } else {
                this.mCameraSource.mFacing = facing;
                return this;
            }
        }

        public CameraSource.Builder setAutoFocusEnabled(boolean var1) {
            this.mCameraSource.mFocusMode = var1;
            return this;
        }

        public CameraSource build() {
            CameraSource var10000 = this.mCameraSource;
            CameraSource var10003 = this.mCameraSource;
            this.mCameraSource.getClass();
            var10000.mFrameProcessor = var10003.new FrameProcessingRunnable(this.mDetector);
            return this.mCameraSource;
        }
    }
}
