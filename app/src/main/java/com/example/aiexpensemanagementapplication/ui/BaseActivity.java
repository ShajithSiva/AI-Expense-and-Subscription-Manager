package com.example.aiexpensemanagementapplication.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Common setup for all activities
    }

    protected void showMessage(String message) {
        // Can be replaced with Toast or Snackbar later
        System.out.println(message);
    }
}