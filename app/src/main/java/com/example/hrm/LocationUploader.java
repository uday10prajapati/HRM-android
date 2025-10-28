package com.example.hrm;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocationUploader {

    private static final String TAG = "LocationUploader";
    // IMPORTANT: This is your actual Supabase Function URL
    private static final String SERVER_URL = "https://zssdfdngclkuamzpyeyz.supabase.co/functions/v1/upload-location"; 
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // The userToken (JWT) is now required for Supabase authentication
    public static void uploadLocationData(Context context, List<LocationPoint> locationPoints, String userId, String userToken) {
        if (locationPoints.isEmpty() || userToken == null) {
            Log.w(TAG, "No location points or user token. Skipping upload.");
            return;
        }

        JSONArray jsonArray = new JSONArray();
        for (LocationPoint point : locationPoints) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", point.getUserId());
                jsonObject.put("latitude", point.getLatitude());
                jsonObject.put("longitude", point.getLongitude());
                jsonObject.put("updated_at", point.getTimestamp());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON object", e);
            }
        }

        // Send the JSON data directly in the request body
        RequestBody body = RequestBody.create(jsonArray.toString(), JSON);
        
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SERVER_URL)
                // Add Supabase-required headers for authentication
                .addHeader("apikey", SupabaseHelper.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + userToken)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to upload location data to Supabase Function", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Location data uploaded successfully to Supabase");
                    locationPoints.clear();
                } else {
                    Log.e(TAG, "Failed to upload to Supabase: " + response.code() + " " + response.body().string());
                }
            }
        });
    }
}
