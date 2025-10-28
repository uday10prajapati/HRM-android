package com.example.hrm;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText email, newPassword, confirmPassword;
    
    private static final String UPDATE_PASSWORD_URL = "https://zssdfdngclkuamzpyeyz.supabase.co/functions/v1/update-password"; 
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        email = findViewById(R.id.email);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        Button confirmButton = findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String newPasswordText = newPassword.getText().toString().trim();
            String confirmPasswordText = confirmPassword.getText().toString().trim();

            if (emailText.isEmpty() || newPasswordText.isEmpty() || confirmPasswordText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // **NEW**: Enforce password length on the client-side
            if (newPasswordText.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPasswordText.equals(confirmPasswordText)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePassword(emailText, newPasswordText);
        });
    }

    private void updatePassword(String email, String newPassword) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        
        Request request = new Request.Builder()
                .url(UPDATE_PASSWORD_URL)
                .header("apikey", SupabaseHelper.SUPABASE_KEY) 
                .post(body)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body() != null ? response.body().string() : "Unknown error";
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ForgotPasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    // **NEW**: Better error reporting from the backend
                    try {
                        JSONObject errorJson = new JSONObject(responseBody);
                        String errorMessage = errorJson.optString("error", "An unknown error occurred.");
                        runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Update failed: " + errorMessage, Toast.LENGTH_LONG).show());
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Update failed: " + responseBody, Toast.LENGTH_LONG).show());
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
