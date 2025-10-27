package com.example.hrm;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, mobileTextView, roleTextView;
    private String userId, userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        mobileTextView = findViewById(R.id.mobileTextView);
        roleTextView = findViewById(R.id.roleTextView);

        userId = getIntent().getStringExtra("USER_ID");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        fetchProfileData();
    }

    private void fetchProfileData() {
        if (userId == null || userToken == null) {
            Toast.makeText(this, "User information not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String query = "id=eq." + userId;
        SupabaseHelper.get("users", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to fetch profile data.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to fetch profile data.", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    if (jsonArray.length() > 0) {
                        JSONObject userObject = jsonArray.getJSONObject(0);

                        // Use optString for safety, providing a default value of "N/A"
                        String name = userObject.optString("name", "N/A");
                        String email = userObject.optString("email", "N/A");
                        String mobileNumber = userObject.optString("mobile_number", "N/A");
                        String role = userObject.optString("role", "N/A");

                        runOnUiThread(() -> {
                            nameTextView.setText("Name: " + name);
                            emailTextView.setText("Email: " + email);
                            mobileTextView.setText("Mobile Number: " + mobileNumber);
                            roleTextView.setText("Role: " + role);
                        });

                    } else {
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "No profile data found in database.", Toast.LENGTH_SHORT).show());
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to parse profile data.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
