package com.example.aiexpensemanagementapplication.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.button.MaterialButton;

public class TermsConditionsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnContactSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_terms_conditions
        );

        initializeViews();
        setupListeners();
    }


    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnContactSupport =
                findViewById(
                        R.id.btnContactSupport
                );
    }


    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        // Back button
        btnBack.setOnClickListener(v -> finish());


        // Contact Support
        btnContactSupport.setOnClickListener(
                v -> openSupportEmail()
        );
    }


    // =====================================================
    // OPEN SUPPORT EMAIL
    // =====================================================

    private void openSupportEmail() {

        Intent emailIntent =
                new Intent(
                        Intent.ACTION_SENDTO
                );

        emailIntent.setData(
                Uri.parse(
                        "mailto:Expensevalut@gmail.com"
                )
        );

        emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "ExpenseVault Support Request"
        );

        try {

            startActivity(
                    Intent.createChooser(
                            emailIntent,
                            "Contact Support"
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