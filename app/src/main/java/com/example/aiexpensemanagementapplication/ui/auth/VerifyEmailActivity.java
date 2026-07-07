package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerifyEmailActivity extends AppCompatActivity {

    private TextView tvUserEmail;
    private TextView tvResendEmail;
    private TextView tvChangeEmail;
    private Button btnVerified;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnVerified = findViewById(R.id.btnVerified);

        // Only use these after adding the ids in XML
        tvResendEmail = findViewById(R.id.tvResendEmail);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        email = getIntent().getStringExtra("email");

        if (email != null) {
            tvUserEmail.setText(email);
        }

        btnVerified.setOnClickListener(v -> checkVerification());

        tvResendEmail.setOnClickListener(v -> resendVerificationEmail());

        tvChangeEmail.setOnClickListener(v -> {
            startActivity(new Intent(
                    VerifyEmailActivity.this,
                    RegisterActivity.class));
            finish();
        });
    }

    private void checkVerification() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(this,
                    "Session expired. Please register again.",
                    Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        btnVerified.setEnabled(false);

        user.reload().addOnCompleteListener(task -> {

            btnVerified.setEnabled(true);

            if (!task.isSuccessful()) {

                Toast.makeText(this,
                        "Unable to verify email.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (user.isEmailVerified()) {

                firestore.collection("users")
                        .document(user.getUid())
                        .update("emailVerified", true)
                        .addOnSuccessListener(unused -> {

                            Toast.makeText(
                                    VerifyEmailActivity.this,
                                    "Email verified successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    VerifyEmailActivity.this,
                                    VerifyMobileActivity.class);

                            startActivity(intent);
                            finish();

                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(
                                        VerifyEmailActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show());

            } else {

                Toast.makeText(this,
                        "Please verify your email first.",
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void resendVerificationEmail() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(this,
                    "Session expired.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        user.sendEmailVerification()
                .addOnSuccessListener(unused ->

                        Toast.makeText(
                                VerifyEmailActivity.this,
                                "Verification email sent again.",
                                Toast.LENGTH_LONG
                        ).show())

                .addOnFailureListener(e ->

                        Toast.makeText(
                                VerifyEmailActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
    }
}