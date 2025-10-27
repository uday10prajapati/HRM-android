package com.example.hrm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
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
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(emailText, passwordText);
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

                    if (userRole.isEmpty()) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Login successful, but could not determine user role from metadata.", Toast.LENGTH_LONG).show()
                        );
                        return;
                    }

                    final String finalRole = userRole;
                    runOnUiThread(() -> {
                        Intent intent;
                        switch (finalRole) {
                            case "engineer":
                                intent = new Intent(LoginActivity.this, EngineerActivity.class);
                                break;
                            case "employee":
                                intent = new Intent(LoginActivity.this, EmployeeActivity.class);
                                break;
                            default:
                                Toast.makeText(LoginActivity.this, "Unknown user role: " + finalRole, Toast.LENGTH_LONG).show();
                                return;
                        }
                        intent.putExtra("USER_EMAIL", email);
                        intent.putExtra("USER_ID", userId);
                        intent.putExtra("USER_TOKEN", accessToken);
                        // **FIX**: Pass the entire user object to the next activity
                        intent.putExtra("USER_OBJECT", user.toString());
                        startActivity(intent);
                        finish();
                    });

                } catch (JSONException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Login failed: Could not parse response", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
