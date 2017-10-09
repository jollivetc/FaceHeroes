package com.apside.faceheroes;


import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MaskRequester {


    private Context mContext;
    private OkHttpClient mClient;
    public static final String BASE_URL = "https://apside-devfest.cappuccinoo.fr/";
    public static final String MASK_LIST_URL = "api/masks/all/2";
    public static final String MASK_URL = "masks/";


    public MaskRequester(Activity listeningActivity){
        mContext = listeningActivity.getApplicationContext();
        mClient = new OkHttpClient();
    }

    public void getMasksList() throws IOException {
        Request request = new Request.Builder().url(BASE_URL+MASK_LIST_URL).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FaceHeroes", "Error on downloading the list of masks", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    JSONObject responseJSON= new JSONObject(response.body().string());
                    JSONArray masks = responseJSON.getJSONArray("masks");
                    downloadMasks(masks);
                } catch (JSONException e) {
                    Log.e("FaceHeroes", "Error on parsing result of the list of masks", e);
                }
            }
        });
    }

    private void downloadMasks(JSONArray masks){
        File storage = Environment.getExternalStorageDirectory();
        File folder = new File(storage.getAbsoluteFile(), "masks");//the dot makes this directory hidden to the user
        if (folder.exists()) {
            boolean created = folder.mkdir();
        }
        for(int maskIndex = 0 ; maskIndex<masks.length(); maskIndex++) {
            try {
                JSONObject mask = masks.getJSONObject(maskIndex);
                String filename = mask.getString("filename");

                File file = new File(folder.getAbsoluteFile(), filename) ;
                if (file.exists()) {
                    continue;
                }

                String maskUrl = MaskRequester.BASE_URL + MaskRequester.MASK_URL + filename;
                DownloadManager mgr = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

                Uri downloadUri = Uri.parse(maskUrl);
                DownloadManager.Request request = new DownloadManager.Request(
                        downloadUri);

                request.setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI
                                | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false).setTitle("Demo")
                        .setDescription("Something useful. No, really.")
                        .setDestinationInExternalPublicDir("/masks", filename);

                mgr.enqueue(request);

            } catch (JSONException e) {
                Log.e("FaceHeroes", "error on parsing json", e);
            }
        }
    }
}
