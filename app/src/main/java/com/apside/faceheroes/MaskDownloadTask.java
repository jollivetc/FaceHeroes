package com.apside.faceheroes;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;


public class MaskDownloadTask extends AsyncTask<JSONArray, Integer, Long> {

    private final Context context;

    public MaskDownloadTask(Context context) {
        this.context = context;
    }

    @Override
    protected Long doInBackground(JSONArray... masks) {
        long counter = 0;
        for(int maskIndex = 0 ; maskIndex<masks[0].length(); maskIndex++) {
            try {
                JSONObject mask = masks[0].getJSONObject(maskIndex);
                String filename = mask.getString("filename");

                File storage = Environment.getExternalStorageDirectory();
                File folder = new File(storage.getAbsoluteFile(), "masks");//the dot makes this directory hidden to the user
                Log.i("FaceHeroes", "folder is " + folder.getAbsolutePath());
                if (folder.exists()) {
                    boolean created = folder.mkdir();
                    Log.i("FaceHeroes", "folder created");
                }
                File file = new File(folder.getAbsoluteFile(), filename) ;
                if (file.exists()) {
                    Log.i("FaceHeroes", filename + " already downloaded");
                    continue;
                }

                String maskUrl = MaskRequester.BASE_URL + MaskRequester.MASK_URL + filename;
                Bitmap maskBitmap = downloadBitmap(maskUrl);
                if (maskBitmap != null) {
                    try {
                        FileOutputStream out = new FileOutputStream(file.getAbsoluteFile());
                        maskBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        Log.i("FaceHeroes", filename + " downloaded");
                    } catch (Exception e) {
                        Log.e("FaceHeroes", "Error on mask file save", e);
                    }
                }
            } catch (JSONException e) {
                Log.e("FaceHerores", "error on parsing json", e);
            }
        }

        return counter;
    }

    private Bitmap downloadBitmap(String maskUrl){

        try {
            URL url = new URL(maskUrl);
            URLConnection conn = url.openConnection();
            return BitmapFactory.decodeStream(conn.getInputStream());
        } catch (Exception ex) {
            Log.e("FaceHeroes", "Error on mask download for url " + maskUrl, ex);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.i("FaceHero", "progress update " + values[0]);
        super.onProgressUpdate(values);
    }
}
