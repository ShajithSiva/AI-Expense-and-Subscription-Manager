package com.example.aiexpensemanagementapplication.ui.family;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class InviteMemberActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private TextInputEditText etMemberEmail;

    private RadioButton radioMember;
    private RadioButton radioViewer;

    private MaterialButton btnSendInvitation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_member);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        etMemberEmail = findViewById(R.id.etMemberEmail);

        radioMember = findViewById(R.id.radioMember);
        radioViewer = findViewById(R.id.radioViewer);

        btnSendInvitation =
                findViewById(R.id.btnSendInvitation);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnSendInvitation.setOnClickListener(v ->
                validateInvitation()
        );
    }

    private void validateInvitation() {

        String email = "";

        if (etMemberEmail.getText() != null) {
            email = etMemberEmail
                    .getText()
                    .toString()
                    .trim();
        }

        // EMPTY EMAIL
        if (email.isEmpty()) {

            etMemberEmail.setError(
                    "Please enter member email"
            );

            etMemberEmail.requestFocus();

            return;
        }

        // INVALID EMAIL
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            etMemberEmail.setError(
                    "Please enter a valid email address"
            );

            etMemberEmail.requestFocus();

            return;
        }

        // SELECT ROLE
        String selectedRole;

        if (radioViewer.isChecked()) {

            selectedRole = "VIEWER";

        } else {

            selectedRole = "MEMBER";
        }

        /*
         * TEMPORARY
         *
         * Later this will call the backend:
         *
         * 1. Find user by email
         * 2. Check if already family member
         * 3. Check existing pending invitation
         * 4. Create family invitation
         */

        Toast.makeText(
                this,
                "Invitation ready for " +
                        email +
                        " as " +
                        selectedRole,
                Toast.LENGTH_LONG
        ).show();
    }
}