package com.example.aiexpensemanagementapplication.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.button.MaterialButton;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnContactPrivacyTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_privacy_policy);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnContactPrivacyTeam = findViewById(R.id.btnContactPrivacyTeam);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnContactPrivacyTeam.setOnClickListener(v -> openPrivacyEmail());
    }

    private void openPrivacyEmail() {

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

        emailIntent.setData(
                Uri.parse("mailto:Expensevalut@gmail.com")
        );

        emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "ExpenseVault Privacy Inquiry"
        );

        try {
            startActivity(
                    Intent.createChooser(
                            emailIntent,
                            "Contact Privacy Team"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "No email application found.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}