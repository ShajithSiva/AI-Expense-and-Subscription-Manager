package com.example.aiexpensemanagementapplication.ui.report;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;

public class ReportActivity extends AppCompatActivity {

    private TextView btnBack;
    private ImageView btnCalendar, btnFilter;
    private TextView btnGenerateReport, btnExportPdf, btnViewAllSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        bindViews();
        setupClicks();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCalendar = findViewById(R.id.btnCalendar);
        btnFilter = findViewById(R.id.btnFilter);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnViewAllSubscriptions = findViewById(R.id.btnViewAllSubscriptions);
    }

    private void setupClicks() {
        btnBack.setOnClickListener(v -> finish());

        btnCalendar.setOnClickListener(v ->
                Toast.makeText(this, "Calendar filter will be implemented later", Toast.LENGTH_SHORT).show()
        );

        btnFilter.setOnClickListener(v ->
                Toast.makeText(this, "Report filters will be implemented later", Toast.LENGTH_SHORT).show()
        );

        btnGenerateReport.setOnClickListener(v ->
                Toast.makeText(this, "Report generation will be connected later", Toast.LENGTH_SHORT).show()
        );

        btnExportPdf.setOnClickListener(v ->
                Toast.makeText(this, "PDF export will be implemented later", Toast.LENGTH_SHORT).show()
        );

        btnViewAllSubscriptions.setOnClickListener(v ->
                Toast.makeText(this, "Subscription report list will be implemented later", Toast.LENGTH_SHORT).show()
        );
    }
}