package com.example.aiexpensemanagementapplication.ui.ai;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class AIInsightsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvInsights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_insights);

        toolbar = findViewById(R.id.toolbar);
        rvInsights = findViewById(R.id.rvInsights);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        rvInsights.setLayoutManager(new LinearLayoutManager(this));
    }
}