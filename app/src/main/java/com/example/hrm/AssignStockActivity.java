package com.example.hrm;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AssignStockActivity extends AppCompatActivity {

    private RecyclerView engineerRecyclerView;
    private EngineerAdapter adapter;
    private List<User> engineerList = new ArrayList<>();

    private String userToken; // Admin's token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_stock);

        // In a real app, you would get the admin's token after they log in
        userToken = getIntent().getStringExtra("USER_TOKEN"); 

        engineerRecyclerView = findViewById(R.id.engineerRecyclerView);
        engineerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EngineerAdapter(engineerList);
        engineerRecyclerView.setAdapter(adapter);

        fetchEngineers();
    }

    private void fetchEngineers() {
        // This query selects all users where the 'role' is 'engineer'
        String query = "role=eq.engineer&select=id,name,email";

        // We assume the admin has a service_role key to bypass RLS for this query.
        // For now, we will use the user's token. A better approach is a dedicated admin user.
        SupabaseHelper.get("users", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AssignStockActivity.this, "Failed to fetch engineers", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(AssignStockActivity.this, "Failed to fetch engineers", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    engineerList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject userObject = jsonArray.getJSONObject(i);
                        engineerList.add(new User(
                            userObject.getString("id"),
                            userObject.getString("name"),
                            userObject.getString("email")
                        ));
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } catch (JSONException e) {
                    // handle error
                }
            }
        });
    }
}
