package com.apside.faceheroes;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MaskRequester {

    public interface MaskRequesterResponse{
        void receivedNewPhoto(Mask newMask);
    }

    private MaskRequesterResponse mResponseListener;
    private Context mContext;
    private Calendar mCalendar;
    private OkHttpClient mClient;
    private static final String BASE_URL = "https://apside-devfest.cappuccinoo.fr/";
    public static final String MASK_LIST_URL = "api/masks/all/2";
    public static final String MASK_URL = "masks/";
    private boolean mLoadingData;

    public boolean isLoadingData (){
        return mLoadingData;
    }

    public MaskRequester(Activity listeningActivity){
        mResponseListener = (MaskRequesterResponse) listeningActivity;
        mContext = listeningActivity.getApplicationContext();
        mClient = new OkHttpClient();
        mCalendar = Calendar.getInstance();
        mLoadingData = false;
    }

    public void getMask() throws IOException {
        Request request = new Request.Builder().url(BASE_URL+MASK_LIST_URL).build();
        mLoadingData = true;
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLoadingData = false;
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    JSONObject responseJSON= new JSONObject(response.body().string());
                    JSONArray masks = responseJSON.getJSONArray("masks");
                    for(int maskIndex = 0 ; maskIndex<masks.length(); maskIndex++) {
                        JSONObject mask = masks.getJSONObject(maskIndex);
                        Object created_at = mask.get("created_at");
                        Log.i("FaceHero", "type of mask " + created_at.getClass());
                    }
                } catch (JSONException e) {
                    mLoadingData = false;
                    e.printStackTrace();
                }
            }
        });
    }
}