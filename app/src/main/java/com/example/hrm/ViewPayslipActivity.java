package com.example.hrm;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ViewPayslipActivity extends AppCompatActivity {

    private TextView payslipDetailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_payslip);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        payslipDetailsTextView = findViewById(R.id.payslipDetailsTextView);

        String payslipDetails = getIntent().getStringExtra("PAYSLIP_DETAILS");

        if (payslipDetails != null) {
            payslipDetailsTextView.setText(payslipDetails);
        } else {
            payslipDetailsTextView.setText("No details found.");
        }
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
