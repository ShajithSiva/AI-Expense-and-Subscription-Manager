package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;

public class WelcomeActivity extends AppCompatActivity {

    private android.widget.Button btnSignIn;
    private android.widget.Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize buttons
        btnSignIn = findViewById(R.id.btnSignIn);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Sign In button
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(
                    WelcomeActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
        });

        // Create Account button
        btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(
                    WelcomeActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);
        });
    }
}