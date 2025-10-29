package com.example.hrm;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LocationUploader {

    private static final String TAG = "LocationUploader";

    public static void uploadLocationData(Context context, List<LocationPoint> locationPoints, String userId, String userToken) {
        if (locationPoints.isEmpty() || userToken == null) {
            Log.w(TAG, "No location points or user token. Skipping upload.");
            return;
        }

        for (LocationPoint point : locationPoints) {
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("user_id", point.getUserId());
                jsonBody.put("latitude", point.getLatitude());
                jsonBody.put("longitude", point.getLongitude());

                SupabaseHelper.post("live_locations", userToken, jsonBody, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Failed to upload location data to Supabase", e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Location data point uploaded successfully");
                        } else {
                            Log.e(TAG, "Failed to upload to Supabase: " + response.code() + " " + response.body().string());
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON for location point", e);
            }
        }        // Clear the points after attempting to upload them
        locationPoints.clear();
    }
}
