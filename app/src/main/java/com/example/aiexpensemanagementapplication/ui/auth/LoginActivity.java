package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.ui.BaseActivity;
import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.widget.Toast;

public class LoginActivity extends BaseActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot Password not implemented yet", Toast.LENGTH_SHORT).show()
        );
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 🔹 Validation
        if (!validateInputs(email, password)) return;

        // 🔹 Firebase Login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {

                            if (user.isEmailVerified()) {

                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(this, PersonalDashboardActivity.class));
                                finish();

                            } else {

                                Toast.makeText(this,
                                        "Please verify your email first",
                                        Toast.LENGTH_LONG).show();

                                startActivity(new Intent(this, VerifyEmailActivity.class));
                            }
                        }

                    } else {
                        Toast.makeText(this,
                                "Login Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError("Email required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            return false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password required");
            return false;
        }

        if (password.length() < 6) {
            tilPassword.setError("Minimum 6 characters");
            return false;
        }

        return true;
    }
}