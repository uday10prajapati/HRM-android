package com.example.hrm;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SupabaseHelper {

    // Replace with your actual Supabase URL and Key
    public static final String SUPABASE_URL = "https://zssdfdngclkuamzpyeyz.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpzc2RmZG5nY2xrdWFtenB5ZXl6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA1MDAyMDUsImV4cCI6MjA3NjA3NjIwNX0.zTw94vFDsPQ79rbHAkeTEveeQRuGtADVh50MmMhnm0c";

    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void get(String tableName, String query, String authToken, @NonNull Callback callback) {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + tableName + "?" + query)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void post(String tableName, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + tableName)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + authToken)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void patch(String tableName, String query, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + tableName + "?" + query) // The query identifies which row(s) to update
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + authToken)
                .patch(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void rpc(String functionName, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/rpc/" + functionName)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + authToken)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
