package com.example.hrm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PayslipActivity extends AppCompatActivity {

    private Spinner monthSpinner, yearSpinner;
    private Button generatePayslipButton, viewPayslipButton, downloadPdfButton;
    private LinearLayout payslipActionsLayout;

    private String userId, userToken;
    private JSONObject currentPayslipJson;

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payslip);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userId = getIntent().getStringExtra("USER_ID");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        generatePayslipButton = findViewById(R.id.generatePayslipButton);
        viewPayslipButton = findViewById(R.id.viewPayslipButton);
        downloadPdfButton = findViewById(R.id.downloadPdfButton);
        payslipActionsLayout = findViewById(R.id.payslipActionsLayout);

        setupSpinners();

        generatePayslipButton.setOnClickListener(v -> fetchPayslip());
        viewPayslipButton.setOnClickListener(v -> viewPayslip());
        downloadPdfButton.setOnClickListener(v -> downloadPayslipAsPdf());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= currentYear - 5; i--) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
    }

    private void fetchPayslip() {
        generatePayslipButton.setEnabled(false);
        payslipActionsLayout.setVisibility(View.GONE);
        String selectedMonth = String.format(Locale.US, "%02d", monthSpinner.getSelectedItemPosition() + 1);
        String selectedYear = yearSpinner.getSelectedItem().toString();

        String query = "user_id=eq." + userId + "&month=eq." + selectedMonth + "&year=eq." + selectedYear;

        SupabaseHelper.get("payslip", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(PayslipActivity.this, "Failed to fetch payslip data.", Toast.LENGTH_SHORT).show();
                    generatePayslipButton.setEnabled(true);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(PayslipActivity.this, "No payslip found for the selected period.", Toast.LENGTH_SHORT).show();
                        generatePayslipButton.setEnabled(true);
                    });
                    return;
                }

                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    if (jsonArray.length() > 0) {
                        currentPayslipJson = jsonArray.getJSONObject(0);
                        runOnUiThread(() -> {
                            payslipActionsLayout.setVisibility(View.VISIBLE);
                            generatePayslipButton.setEnabled(true);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(PayslipActivity.this, "No payslip found for the selected period.", Toast.LENGTH_SHORT).show();
                            generatePayslipButton.setEnabled(true);
                        });
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(PayslipActivity.this, "Failed to parse payslip data.", Toast.LENGTH_SHORT).show();
                        generatePayslipButton.setEnabled(true);
                    });
                }
            }
        });
    }

    private String formatPayslip(JSONObject payslip) {
        if (payslip == null) return "No data";

        StringBuilder sb = new StringBuilder();
        sb.append("\t\t\t\tSALARY SLIP\n\n");
        sb.append("Month: ").append(payslip.optString("month", "N/A"))
          .append("/").append(payslip.optString("year", "N/A")).append("\n");
        sb.append("Employee ID: ").append(payslip.optString("user_id", "N/A")).append("\n\n");

        sb.append("\t\t\t\tEARNINGS\n");
        sb.append("Basic: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("basic"))).append("\n");
        sb.append("HRA: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("hra"))).append("\n");
        sb.append("Allowances: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("allowances"))).append("\n");
        sb.append("Overtime Pay: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("overtime_pay"))).append("\n\n");

        sb.append("\t\t\t\tDEDUCTIONS\n");
        sb.append("PF: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("pf"))).append("\n");
        sb.append("ESI (Employee): \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("esi_employee"))).append("\n");
        sb.append("Professional Tax: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("professional_tax"))).append("\n");
        sb.append("TDS: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("tds"))).append("\n\n");

        sb.append("\t\t\t\tSUMMARY\n");
        sb.append("Gross: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("gross_salary"))).append("\n");
        sb.append("Net Pay: \t\t\t\t").append(String.format(Locale.US, "%.2f", payslip.optDouble("net_pay"))).append("\n");

        return sb.toString();
    }

    private void viewPayslip() {
        if (currentPayslipJson == null) {
            Toast.makeText(this, "No payslip data to view.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ViewPayslipActivity.class);
        intent.putExtra("PAYSLIP_DETAILS", formatPayslip(currentPayslipJson));
        startActivity(intent);
    }

    private void downloadPayslipAsPdf() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage permission is required to download the payslip.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            createPdf();
        }
    }

    private void createPdf() {
        if (currentPayslipJson == null) {
            Toast.makeText(this, "No payslip data to download.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "Payslip-" + monthSpinner.getSelectedItem().toString() + "-" + yearSpinner.getSelectedItem().toString() + ".pdf";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            document.add(new Paragraph(formatPayslip(currentPayslipJson)));
            document.close();

            Toast.makeText(this, "Payslip downloaded to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createPdf();
        } else {
            Toast.makeText(this, "Storage permission was denied.", Toast.LENGTH_SHORT).show();
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
