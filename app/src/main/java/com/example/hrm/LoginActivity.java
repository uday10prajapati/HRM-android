package com.example.hrm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Constants for SharedPreferences
    private static final String PREFS_NAME = "HRM_PREFS";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_TOKEN = "USER_TOKEN";
    private static final String KEY_USER_ROLE = "USER_ROLE";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // **NEW**: Check for saved credentials first
        if (isUserLoggedIn()) {
            navigateToDashboard();
            return; // Skip the rest of the login setup
        }

        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        TextView forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        loginButton.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(emailText, passwordText);
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser(String email, String password) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create login request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SupabaseHelper.SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .header("apikey", SupabaseHelper.SUPABASE_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Login failed: Invalid credentials", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);

                    String accessToken = jsonObject.getString("access_token");
                    JSONObject user = jsonObject.getJSONObject("user");
                    String userId = user.getString("id");

                    JSONObject userMetadata = user.optJSONObject("user_metadata");
                    String userRole = "";
                    if (userMetadata != null) {
                        userRole = userMetadata.optString("role", "").toLowerCase();
                    }

                    // **NEW**: Save credentials on successful login
                    saveUserCredentials(userId, accessToken, userRole, email);

                    // Navigate to the correct dashboard
                    navigateToDashboard();

                } catch (JSONException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Login failed: Could not parse response", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(KEY_USER_TOKEN, null);
        return token != null;
    }

    private void saveUserCredentials(String userId, String token, String role, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TOKEN, token);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    private void navigateToDashboard() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String role = prefs.getString(KEY_USER_ROLE, "");
        Intent intent;

        switch (role) {
            case "engineer":
                intent = new Intent(LoginActivity.this, EngineerActivity.class);
                break;
            case "employee":
                intent = new Intent(LoginActivity.this, EmployeeActivity.class);
                break;
            default:
                // If role is unknown or not set, force login
                // This can also happen if navigating before credentials are saved
                return; 
        }

        // Pass the saved data to the next activity
        intent.putExtra("USER_ID", prefs.getString(KEY_USER_ID, ""));
        intent.putExtra("USER_TOKEN", prefs.getString(KEY_USER_TOKEN, ""));
        intent.putExtra("USER_EMAIL", prefs.getString(KEY_USER_EMAIL, ""));

        startActivity(intent);
        finish(); // Important: remove LoginActivity from the back stack
    }
}
