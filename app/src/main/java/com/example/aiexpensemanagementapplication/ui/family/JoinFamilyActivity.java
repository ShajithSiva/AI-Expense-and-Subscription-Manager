package com.example.aiexpensemanagementapplication.ui.family;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class JoinFamilyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText etInviteCode;

    private MaterialButton btnFindFamily;
    private MaterialButton btnAcceptInvitation;
    private MaterialButton btnDeclineInvitation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_family);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        etInviteCode = findViewById(R.id.etInviteCode);

        btnFindFamily = findViewById(R.id.btnFindFamily);

        btnAcceptInvitation =
                findViewById(R.id.btnAcceptInvitation);

        btnDeclineInvitation =
                findViewById(R.id.btnDeclineInvitation);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnFindFamily.setOnClickListener(v -> findFamily());

        btnAcceptInvitation.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Accept invitation will be connected to database",
                    Toast.LENGTH_SHORT
            ).show();

        });

        btnDeclineInvitation.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Decline invitation will be connected to database",
                    Toast.LENGTH_SHORT
            ).show();

        });
    }

    private void findFamily() {

        String inviteCode = "";

        if (etInviteCode.getText() != null) {

            inviteCode = etInviteCode
                    .getText()
                    .toString()
                    .trim();
        }

        if (inviteCode.isEmpty()) {

            etInviteCode.setError(
                    "Please enter invitation code"
            );

            etInviteCode.requestFocus();

            return;
        }

        Toast.makeText(
                this,
                "Searching invitation: " + inviteCode,
                Toast.LENGTH_SHORT
        ).show();
    }
}