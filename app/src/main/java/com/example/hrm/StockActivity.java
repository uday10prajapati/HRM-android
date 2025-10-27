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

public class StockActivity extends AppCompatActivity implements StockAdapter.OnUseStockListener {

    private RecyclerView stockRecyclerView;
    private StockAdapter adapter;
    private List<Stock> stockItems = new ArrayList<>();

    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userToken = getIntent().getStringExtra("USER_TOKEN");

        stockRecyclerView = findViewById(R.id.stockRecyclerView);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StockAdapter(stockItems, this);
        stockRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStock();
    }

    private void fetchStock() {
        String query = "select=*";
        SupabaseHelper.get("stock_items", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(StockActivity.this, "Failed to fetch stock: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    final String errorBody = response.body().string();
                    runOnUiThread(() -> Toast.makeText(StockActivity.this, "Error fetching stock: " + errorBody, Toast.LENGTH_LONG).show());
                    return;
                }

                try {
                    final String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);
                    stockItems.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject stockObject = jsonArray.getJSONObject(i);
                        // **FINAL FIX: Use 'quantity' consistently, which matches your Stock.java data class.**
                        stockItems.add(new Stock(
                                stockObject.getInt("id"),
                                stockObject.getString("name"), 
                                stockObject.getInt("quantity") 
                        ));
                    }

                    runOnUiThread(() -> {
                        if(stockItems.isEmpty()){
                            Toast.makeText(StockActivity.this, "No stock items found in the database.", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(StockActivity.this, "Failed to parse stock data. Check column names.", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    @Override
    public void onUseStock(Stock stockItem) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("p_stock_id", stockItem.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SupabaseHelper.rpc("use_stock_item_by_id", userToken, jsonBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(StockActivity.this, "Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    final String errorBody = response.body().string();
                    runOnUiThread(() -> Toast.makeText(StockActivity.this, "Database Error: " + errorBody, Toast.LENGTH_LONG).show());
                    return;
                }

                runOnUiThread(() -> {
                    Toast.makeText(StockActivity.this, "Stock item used.", Toast.LENGTH_SHORT).show();
                    fetchStock(); // Refresh the list
                });
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
