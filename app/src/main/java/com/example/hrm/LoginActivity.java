package com.example.hrm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUserFromDatabase(emailText, passwordText);
        });
    }

    /**
     * Login by querying Supabase 'users' table directly (no email verification).
     */
    private void loginUserFromDatabase(String email, String password) {
        try {
            String encodedEmail = URLEncoder.encode(email, "UTF-8");
            String query = "email=eq." + encodedEmail + "&select=*";

            SupabaseHelper.get("users", query, null, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show());
                        return;
                    }

                    String responseBody = response.body() != null ? response.body().string() : "[]";
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        if (jsonArray.length() == 0) {
                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        JSONObject userObject = jsonArray.getJSONObject(0);
                        String storedPassword = userObject.optString("password", "");
                        String userId = userObject.optString("id", "");
                        String userRole = userObject.optString("role", "").toLowerCase();
                        String fullName = userObject.optString("name", "User");

                        if (!password.equals(storedPassword)) {
                            runOnUiThread(() ->
                                    Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        runOnUiThread(() -> {
                            Intent intent;
                            switch (userRole) {
                                case "engineer":
                                    intent = new Intent(LoginActivity.this, EngineerActivity.class);
                                    break;
                                case "employee":
                                    intent = new Intent(LoginActivity.this, EmployeeActivity.class);
                                    break;

                                default:
                                    Toast.makeText(LoginActivity.this, "Invalid role: " + userRole, Toast.LENGTH_SHORT).show();
                                    return;
                            }

                            intent.putExtra("USER_ID", userId);
                            intent.putExtra("USER_EMAIL", email);
                            intent.putExtra("USER_NAME", fullName);

                            Toast.makeText(LoginActivity.this, "Welcome " + fullName, Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Encoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}