package com.apside.faceheroes;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MaskDownloadTask extends AsyncTask<JSONArray, Integer, Long> {
    @Override
    protected Long doInBackground(JSONArray... masks) {
        long counter = 0;
        for(int maskIndex = 0 ; maskIndex<masks[0].length(); maskIndex++) {
            try {
                JSONObject mask = masks[0].getJSONObject(maskIndex);
                Object created_at = null;
                created_at = mask.get("created_at");
                Log.i("FaceHero", "type of mask " + created_at.getClass());
                publishProgress(maskIndex*100/masks[0].length());
            } catch (JSONException e) {
                e.printStackTrace();
                //TODO treat error
            }
        }

        return counter;
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
