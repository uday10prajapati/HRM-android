package com.example.hrm;

import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * ✅ SupabaseHelper.java
 * Full version with GET, POST, POST (Service Role), PATCH, and RPC.
 * Do NOT remove anything — this version includes all functions.
 */
public class SupabaseHelper {

    // ✅ Your Supabase credentials
    public static final String SUPABASE_URL = "https://zssdfdngclkuamzpyeyz.supabase.co";

    // ⚠️ Replace with your actual anon key (from Supabase Project Settings → API)
    public static final String SUPABASE_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpzc2RmZG5nY2xrdWFtenB5ZXl6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA1MDAyMDUsImV4cCI6MjA3NjA3NjIwNX0.zTw94vFDsPQ79rbHAkeTEveeQRuGtADVh50MmMhnm0c";

    // ⚠️ Replace with your Service Role key (from Supabase Project Settings → API)
    public static final String SUPABASE_SERVICE_ROLE_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpzc2RmZG5nY2xrdWFtenB5ZXl6Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2MDUwMDIwNSwiZXhwIjoyMDc2MDc2MjA1fQ._XOD44-c6Rwd6KI-RdEE0m3y9OLVuC8jkhZ-esLTGy4";

    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // ✅ GET Request
    public static void get(String tableName, String query, String authToken, @NonNull Callback callback) {
        String url = SUPABASE_URL + "/rest/v1/" + tableName;
        if (query != null && !query.isEmpty()) {
            url += query.startsWith("?") ? query : "?" + query;
        }

        Log.d("SupabaseHelper", "GET URL: " + url);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Accept", "application/json");

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        client.newCall(builder.build()).enqueue(callback);
    }

    // ✅ POST Request (normal insert)
    public static void post(String tableName, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        String url = SUPABASE_URL + "/rest/v1/" + tableName;

        Log.d("SupabaseHelper", "POST URL: " + url + " BODY: " + jsonBody);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body);

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        client.newCall(builder.build()).enqueue(callback);
    }

    // ✅ POST (Service Role, bypass RLS)
    public static void postWithServiceRole(String tableName, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        String url = SUPABASE_URL + "/rest/v1/" + tableName;

        Log.d("SupabaseHelper", "POST (Service Role) URL: " + url + " BODY: " + jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    // ✅ PATCH Request (update rows)
    public static void patch(String tableName, String query, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        String url = SUPABASE_URL + "/rest/v1/" + tableName;
        if (query != null && !query.isEmpty()) {
            url += query.startsWith("?") ? query : "?" + query;
        }

        Log.d("SupabaseHelper", "PATCH URL: " + url + " BODY: " + jsonBody);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(body);

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        client.newCall(builder.build()).enqueue(callback);
    }

    // ✅ RPC (Stored Procedure) Call
    public static void rpc(String functionName, String authToken, JSONObject jsonBody, @NonNull Callback callback) {
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        String url = SUPABASE_URL + "/rest/v1/rpc/" + functionName;

        Log.d("SupabaseHelper", "RPC URL: " + url + " BODY: " + jsonBody);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body);

        if (authToken != null && !authToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + authToken);
        }

        client.newCall(builder.build()).enqueue(callback);
    }
}
