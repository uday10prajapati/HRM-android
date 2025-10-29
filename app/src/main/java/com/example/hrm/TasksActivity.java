package com.example.hrm;

import android.content.Intent;
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

public class TasksActivity extends AppCompatActivity implements TasksAdapter.OnItemClickListener {

    private RecyclerView tasksRecyclerView;
    private TasksAdapter adapter;
    private List<Task> taskList = new ArrayList<>();

    private String userId, userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userId = getIntent().getStringExtra("USER_ID");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TasksAdapter(taskList, this);
        tasksRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTasks();
    }

    private void fetchTasks() {
        if (userId == null) return;

        String query = "user_id=eq." + userId + "&select=*";
        SupabaseHelper.get("tasks", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(TasksActivity.this, "Failed to fetch tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    final String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    runOnUiThread(() -> Toast.makeText(TasksActivity.this, "Failed to fetch tasks. Code: " + response.code(), Toast.LENGTH_LONG).show());
                    return;
                }

                try {
                    final String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);
                    taskList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject taskObject = jsonArray.getJSONObject(i);
                        taskList.add(new Task(
                                taskObject.getInt("id"),
                                taskObject.optString("user_id"),
                                taskObject.optString("title"),
                                taskObject.optString("description"),
                                taskObject.optString("created_at"),
                                taskObject.optString("status"),
                                taskObject.optString("assigned_to"),
                                taskObject.optString("customer_name"),
                                taskObject.optString("customer_address"),
                                taskObject.optString("customer_mobile"),
                                taskObject.optString("due_date")
                        ));
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(TasksActivity.this, "Error parsing tasks data.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void onItemClick(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("TASK", task);
        intent.putExtra("USER_TOKEN", userToken);
        startActivity(intent);
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
