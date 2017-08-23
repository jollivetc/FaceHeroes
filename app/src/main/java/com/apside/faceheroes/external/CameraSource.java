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
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.SystemClock;
import android.os.Build.VERSION;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import java.io.IOException;
import java.lang.Thread.State;
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
    private final Object aDD;
    private Camera aDE;
    private int aDF;
    private int zzbrh;
    private Size aDG;
    private float aDH;
    private int aDI;
    private int aDJ;
    private boolean aDK;
    private SurfaceView aDL;
    private SurfaceTexture aDM;
    private boolean aDN;
    private Thread aDO;
    private CameraSource.zzb aDP;
    private Map<byte[], ByteBuffer> aDQ;

    public void release() {
        Object var1 = this.aDD;
        synchronized(this.aDD) {
            this.stop();
            this.aDP.release();
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start() throws IOException {
        Object var1 = this.aDD;
        synchronized(this.aDD) {
            if(this.aDE != null) {
                return this;
            } else {
                this.aDE = this.zzchq();
                if(Build.VERSION.SDK_INT >= 11) {
                    this.aDM = new SurfaceTexture(100);
                    this.aDE.setPreviewTexture(this.aDM);
                    this.aDN = true;
                } else {
                    this.aDL = new SurfaceView(this.mContext);
                    this.aDE.setPreviewDisplay(this.aDL.getHolder());
                    this.aDN = false;
                }

                this.aDE.startPreview();
                this.aDO = new Thread(this.aDP);
                this.aDP.setActive(true);
                this.aDO.start();
                return this;
            }
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start(SurfaceTexture surfaceTexture)throws IOException{
        Object var2 = this.aDD;
        synchronized (this.aDD){
            if(this.aDE != null){
                return this;
            }else{
                this.aDE = this.zzchq();
                this.aDE.setPreviewTexture(surfaceTexture);
                this.aDN = true;
                this.aDE.startPreview();
                this.aDO = new Thread(this.aDP);
                this.aDP.setActive(true);
                this.aDO.start();
                return this;
            }
        }
    }

    @RequiresPermission("android.permission.CAMERA")
    public CameraSource start(SurfaceHolder var1) throws IOException {
        Object var2 = this.aDD;
        synchronized(this.aDD) {
            if(this.aDE != null) {
                return this;
            } else {
                this.aDE = this.zzchq();
                this.aDE.setPreviewDisplay(var1);
                this.aDE.startPreview();
                this.aDO = new Thread(this.aDP);
                this.aDP.setActive(true);
                this.aDO.start();
                this.aDN = false;
                return this;
            }
        }
    }

    public void stop() {
        Object var1 = this.aDD;
        synchronized(this.aDD) {
            this.aDP.setActive(false);
            if(this.aDO != null) {
                try {
                    this.aDO.join();
                } catch (InterruptedException var6) {
                    Log.d("CameraSource", "Frame processing thread interrupted on release.");
                }

                this.aDO = null;
            }

            if(this.aDE != null) {
                this.aDE.stopPreview();
                this.aDE.setPreviewCallbackWithBuffer((Camera.PreviewCallback)null);

                try {
                    if(this.aDN) {
                        this.aDE.setPreviewTexture((SurfaceTexture)null);
                    } else {
                        this.aDE.setPreviewDisplay((SurfaceHolder)null);
                    }
                } catch (Exception var5) {
                    String var3 = String.valueOf(var5);
                    Log.e("CameraSource", (new StringBuilder(32 + String.valueOf(var3).length())).append("Failed to clear camera preview: ").append(var3).toString());
                }

                this.aDE.release();
                this.aDE = null;
            }

            this.aDQ.clear();
        }
    }

    public Size getPreviewSize() {
        return this.aDG;
    }

    public int getCameraFacing() {
        return this.aDF;
    }

    public void takePicture(com.google.android.gms.vision.CameraSource.ShutterCallback var1, com.google.android.gms.vision.CameraSource.PictureCallback var2) {
        Object var3 = this.aDD;
        synchronized(this.aDD) {
            if(this.aDE != null) {
                CameraSource.zzd var4 = new CameraSource.zzd();
                var4.aDZ = var1;
                CameraSource.zzc var5 = new CameraSource.zzc();
                var5.aDY = var2;
                this.aDE.takePicture(var4, (android.hardware.Camera.PictureCallback)null, (android.hardware.Camera.PictureCallback)null, var5);
            }

        }
    }

    private CameraSource() {
        this.aDD = new Object();
        this.aDF = 0;
        this.aDH = 30.0F;
        this.aDI = 1024;
        this.aDJ = 768;
        this.aDK = false;
        this.aDQ = new HashMap();
    }

    @SuppressLint({"InlinedApi"})
    private Camera zzchq() {
        int var1 = zzzu(this.aDF);
        if(var1 == -1) {
            throw new RuntimeException("Could not find requested camera.");
        } else {
            Camera var2 = Camera.open(var1);
            CameraSource.zze var3 = zza(var2, this.aDI, this.aDJ);
            if(var3 == null) {
                throw new RuntimeException("Could not find suitable preview size.");
            } else {
                Size var4 = var3.zzchs();
                this.aDG = var3.zzchr();
                int[] var5 = zza(var2, this.aDH);
                if(var5 == null) {
                    throw new RuntimeException("Could not find suitable preview frames per second range.");
                } else {
                    Camera.Parameters var6 = var2.getParameters();
                    if(var4 != null) {
                        var6.setPictureSize(var4.getWidth(), var4.getHeight());
                    }

                    var6.setPreviewSize(this.aDG.getWidth(), this.aDG.getHeight());
                    var6.setPreviewFpsRange(var5[0], var5[1]);
                    var6.setPreviewFormat(17);
                    this.zza(var2, var6, var1);
                    if(this.aDK) {
                        if(var6.getSupportedFocusModes().contains("continuous-video")) {
                            var6.setFocusMode("continuous-video");
                        } else {
                            Log.i("CameraSource", "Camera auto focus is not supported on this device.");
                        }
                    }

                    var2.setParameters(var6);
                    var2.setPreviewCallbackWithBuffer(new CameraSource.zza());
                    var2.addCallbackBuffer(this.zza(this.aDG));
                    var2.addCallbackBuffer(this.zza(this.aDG));
                    var2.addCallbackBuffer(this.zza(this.aDG));
                    var2.addCallbackBuffer(this.zza(this.aDG));
                    return var2;
                }
            }
        }
    }

    private static int zzzu(int var0) {
        Camera.CameraInfo var1 = new Camera.CameraInfo();

        for(int var2 = 0; var2 < Camera.getNumberOfCameras(); ++var2) {
            Camera.getCameraInfo(var2, var1);
            if(var1.facing == var0) {
                return var2;
            }
        }

        return -1;
    }

    static CameraSource.zze zza(Camera var0, int var1, int var2) {
        List var3 = zza(var0);
        CameraSource.zze var4 = null;
        int var5 = 2147483647;
        Iterator var6 = var3.iterator();

        while(var6.hasNext()) {
            CameraSource.zze var7 = (CameraSource.zze)var6.next();
            Size var8 = var7.zzchr();
            int var9 = Math.abs(var8.getWidth() - var1) + Math.abs(var8.getHeight() - var2);
            if(var9 < var5) {
                var4 = var7;
                var5 = var9;
            }
        }

        return var4;
    }

    static List<CameraSource.zze> zza(Camera var0) {
        Camera.Parameters var1 = var0.getParameters();
        List var2 = var1.getSupportedPreviewSizes();
        List var3 = var1.getSupportedPictureSizes();
        ArrayList var4 = new ArrayList();
        Iterator var5 = var2.iterator();

        while(true) {
            android.hardware.Camera.Size var6;
            while(var5.hasNext()) {
                var6 = (android.hardware.Camera.Size)var5.next();
                float var7 = (float)var6.width / (float)var6.height;
                Iterator var8 = var3.iterator();

                while(var8.hasNext()) {
                    android.hardware.Camera.Size var9 = (android.hardware.Camera.Size)var8.next();
                    float var10 = (float)var9.width / (float)var9.height;
                    if(Math.abs(var7 - var10) < 0.01F) {
                        var4.add(new CameraSource.zze(var6, var9));
                        break;
                    }
                }
            }

            if(var4.size() == 0) {
                Log.w("CameraSource", "No preview sizes have a corresponding same-aspect-ratio picture size");
                var5 = var2.iterator();

                while(var5.hasNext()) {
                    var6 = (android.hardware.Camera.Size)var5.next();
                    var4.add(new CameraSource.zze(var6, (android.hardware.Camera.Size)null));
                }
            }

            return var4;
        }
    }

    @SuppressLint({"InlinedApi"})
    static int[] zza(Camera var0, float var1) {
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

    private void zza(Camera var1, Camera.Parameters var2, int var3) {
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
        Camera.getCameraInfo(var3, var7);
        int var8;
        int var9;
        if(var7.facing == 1) {
            var8 = (var7.orientation + var5) % 360;
            var9 = (360 - var8) % 360;
        } else {
            var8 = (var7.orientation - var5 + 360) % 360;
            var9 = var8;
        }

        this.zzbrh = var8 / 90;
        var1.setDisplayOrientation(var9);
        var2.setRotation(var8);
    }

    @SuppressLint({"InlinedApi"})
    private byte[] zza(Size var1) {
        int var2 = ImageFormat.getBitsPerPixel(17);
        long var3 = (long)(var1.getHeight() * var1.getWidth() * var2);
        int var5 = (int)Math.ceil((double)var3 / 8.0D) + 1;
        byte[] var6 = new byte[var5];
        ByteBuffer var7 = ByteBuffer.wrap(var6);
        if(var7.hasArray() && var7.array() == var6) {
            this.aDQ.put(var6, var7);
            return var6;
        } else {
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }
    }

    private class zzb implements Runnable {
        private Detector<?> aDR;
        private long zzczg = SystemClock.elapsedRealtime();
        private final Object zzail = new Object();
        private boolean aDU = true;
        private long aDV;
        private int aDW = 0;
        private ByteBuffer aDX;

        zzb(Detector<?> var1) {
            this.aDR = var1;
        }

        @SuppressLint({"Assert"})
        void release() {
            assert CameraSource.this.aDO.getState() == Thread.State.TERMINATED;

            this.aDR.release();
            this.aDR = null;
        }

        void setActive(boolean var1) {
            Object var2 = this.zzail;
            synchronized(this.zzail) {
                this.aDU = var1;
                this.zzail.notifyAll();
            }
        }

        void zza(byte[] var1, Camera var2) {
            Object var3 = this.zzail;
            synchronized(this.zzail) {
                if(this.aDX != null) {
                    var2.addCallbackBuffer(this.aDX.array());
                    this.aDX = null;
                }

                if(!CameraSource.this.aDQ.containsKey(var1)) {
                    Log.d("CameraSource", "Skipping frame. Could not find ByteBuffer associated with the image data from the camera.");
                } else {
                    this.aDV = SystemClock.elapsedRealtime() - this.zzczg;
                    ++this.aDW;
                    this.aDX = (ByteBuffer) CameraSource.this.aDQ.get(var1);
                    this.zzail.notifyAll();
                }
            }
        }

        @SuppressLint({"InlinedApi"})
        public void run() {
            while(true) {
                Object var3 = this.zzail;
                Frame var1;
                ByteBuffer var2;
                synchronized(this.zzail) {
                    while(this.aDU && this.aDX == null) {
                        try {
                            this.zzail.wait();
                        } catch (InterruptedException var13) {
                            Log.d("CameraSource", "Frame processing loop terminated.", var13);
                            return;
                        }
                    }

                    if(!this.aDU) {
                        return;
                    }

                    var1 = (new com.google.android.gms.vision.Frame.Builder()).setImageData(this.aDX, CameraSource.this.aDG.getWidth(), CameraSource.this.aDG.getHeight(), 17).setId(this.aDW).setTimestampMillis(this.aDV).setRotation(CameraSource.this.zzbrh).build();
                    var2 = this.aDX;
                    this.aDX = null;
                }

                try {
                    this.aDR.receiveFrame(var1);
                } catch (Throwable var11) {
                    Log.e("CameraSource", "Exception thrown from receiver.", var11);
                } finally {
                    CameraSource.this.aDE.addCallbackBuffer(var2.array());
                }
            }
        }
    }

    private class zza implements Camera.PreviewCallback {
        private zza() {
        }

        public void onPreviewFrame(byte[] var1, Camera var2) {
            CameraSource.this.aDP.zza(var1, var2);
        }
    }

    static class zze {
        private Size aEa;
        private Size aEb;

        public zze(android.hardware.Camera.Size var1, android.hardware.Camera.Size var2) {
            this.aEa = new Size(var1.width, var1.height);
            if(var2 != null) {
                this.aEb = new Size(var2.width, var2.height);
            }

        }

        public Size zzchr() {
            return this.aEa;
        }

        public Size zzchs() {
            return this.aEb;
        }
    }

    private class zzc implements android.hardware.Camera.PictureCallback {
        private com.google.android.gms.vision.CameraSource.PictureCallback aDY;

        private zzc() {
        }

        public void onPictureTaken(byte[] var1, Camera var2) {
            if(this.aDY != null) {
                this.aDY.onPictureTaken(var1);
            }

            synchronized(CameraSource.this.aDD) {
                if(CameraSource.this.aDE != null) {
                    CameraSource.this.aDE.startPreview();
                }

            }
        }
    }

    private class zzd implements android.hardware.Camera.ShutterCallback {
        private com.google.android.gms.vision.CameraSource.ShutterCallback aDZ;

        private zzd() {
        }

        public void onShutter() {
            if(this.aDZ != null) {
                this.aDZ.onShutter();
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
        private final Detector<?> aDR;
        private CameraSource aDS = new CameraSource();

        public Builder(Context var1, Detector<?> var2) {
            if(var1 == null) {
                throw new IllegalArgumentException("No context supplied.");
            } else if(var2 == null) {
                throw new IllegalArgumentException("No detector supplied.");
            } else {
                this.aDR = var2;
                this.aDS.mContext = var1;
            }
        }

        public CameraSource.Builder setRequestedFps(float var1) {
            if(var1 <= 0.0F) {
                throw new IllegalArgumentException((new StringBuilder(28)).append("Invalid fps: ").append(var1).toString());
            } else {
                this.aDS.aDH = var1;
                return this;
            }
        }

        public CameraSource.Builder setRequestedPreviewSize(int var1, int var2) {
            int var3 = 1000000;
            if(var1 > 0 && var1 <= 1000000 && var2 > 0 && var2 <= 1000000) {
                this.aDS.aDI = var1;
                this.aDS.aDJ = var2;
                return this;
            } else {
                throw new IllegalArgumentException((new StringBuilder(45)).append("Invalid preview size: ").append(var1).append("x").append(var2).toString());
            }
        }

        public CameraSource.Builder setFacing(int var1) {
            if(var1 != 0 && var1 != 1) {
                throw new IllegalArgumentException((new StringBuilder(27)).append("Invalid camera: ").append(var1).toString());
            } else {
                this.aDS.aDF = var1;
                return this;
            }
        }

        public CameraSource.Builder setAutoFocusEnabled(boolean var1) {
            this.aDS.aDK = var1;
            return this;
        }

        public CameraSource build() {
            CameraSource var10000 = this.aDS;
            CameraSource var10003 = this.aDS;
            this.aDS.getClass();
            var10000.aDP = var10003.new zzb(this.aDR);
            return this.aDS;
        }
    }
}
