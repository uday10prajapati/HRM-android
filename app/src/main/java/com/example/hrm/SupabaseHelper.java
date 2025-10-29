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

    // ✅ Replace with your actual Supabase credentials
    public static final String SUPABASE_URL = "https://zssdfdngclkuamzpyeyz.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpzc2RmZG5nY2xrdWFtenB5ZXl6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA1MDAyMDUsImV4cCI6MjA3NjA3NjIwNX0.zTw94vFDsPQ79rbHAkeTEveeQRuGtADVh50MmMhnm0c";

    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // ✅ GET Request
    public static void get(String tableName, String query, String authToken, @NonNull Callback callback) {
        Request.Builder builder = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + tableName + "?" + query)
                .addHeader("apikey", SUPABASE_KEY);

        // ✅ Only add Authorization if provided
        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }

    // ✅ POST Request
    public static void post(String tableName, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request.Builder builder = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + tableName)
                .addHeader("apikey", SUPABASE_KEY)
                .post(body);

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }

    // ✅ PATCH Request
    public static void patch(String tableName, String query, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request.Builder builder = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + tableName + "?" + query)
                .addHeader("apikey", SUPABASE_KEY)
                .patch(body);

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }

    // ✅ RPC (Stored Procedure) Call
    public static void rpc(String functionName, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request.Builder builder = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/rpc/" + functionName)
                .addHeader("apikey", SUPABASE_KEY)
                .post(body);

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }
}