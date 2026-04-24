package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.aiexpensemanagementapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    TextView tvUserEmail;
    Button btnVerified;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnVerified = findViewById(R.id.btnVerified);

        mAuth = FirebaseAuth.getInstance();

        String email = getIntent().getStringExtra("email");
        tvUserEmail.setText(email);

        btnVerified.setOnClickListener(v -> checkVerification());
    }

    private void checkVerification() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.reload().addOnCompleteListener(task -> {

                if (user.isEmailVerified()) {

                    startActivity(new Intent(this, VerifyMobileActivity.class));

                } else {
                    Toast.makeText(this,
                            "Email not verified yet",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}