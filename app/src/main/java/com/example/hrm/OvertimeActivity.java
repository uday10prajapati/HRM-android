package com.example.hrm;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class OvertimeActivity extends AppCompatActivity {

    private RecyclerView overtimeRecyclerView;
    private OvertimeAdapter adapter;
    private List<OvertimeRecord> overtimeRecords = new ArrayList<>();

    private String userEmail, userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overtime);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        overtimeRecyclerView = findViewById(R.id.overtimeRecyclerView);
        overtimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OvertimeAdapter(overtimeRecords);
        overtimeRecyclerView.setAdapter(adapter);

        fetchOvertimeData();
    }

    private void fetchOvertimeData() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("p_user_id", userEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SupabaseHelper.rpc("calculate_overtime", userToken, jsonBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // ...
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Parsing and UI update logic remains the same
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
