package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.ui.dashboard.PersonalDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing In...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login →");

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user == null) {
                        Toast.makeText(
                                this,
                                "Login failed.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    user.reload().addOnCompleteListener(reloadTask -> {

                        if (!user.isEmailVerified()) {

                            Toast.makeText(
                                    this,
                                    "Please verify your email first.",
                                    Toast.LENGTH_LONG
                            ).show();

                            startActivity(new Intent(
                                    LoginActivity.this,
                                    VerifyEmailActivity.class));

                            finish();

                            return;
                        }

                        firestore.collection("users")
                                .document(user.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot ->
                                        checkUserStatus(documentSnapshot, user));
                    });

                });
    }

    private void checkUserStatus(DocumentSnapshot document, FirebaseUser user) {

        if (!document.exists()) {

            Toast.makeText(
                    this,
                    "User profile not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Boolean phoneVerified =
                document.getBoolean("phoneVerified");

        Boolean accountCompleted =
                document.getBoolean("accountCompleted");

        if (phoneVerified == null)
            phoneVerified = false;

        if (accountCompleted == null)
            accountCompleted = false;

        if (!phoneVerified) {

            mAuth.signOut();

            Toast.makeText(
                    this,
                    "Please complete phone verification.",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(
                    this,
                    VerifyMobileActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
            return;
        }

        if (!accountCompleted) {

            mAuth.signOut();

            Toast.makeText(
                    this,
                    "Please complete your account setup.",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(
                    this,
                    CreatePasswordActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .update(
                        "lastLogin",
                        com.google.firebase.firestore.FieldValue.serverTimestamp()
                );

        Toast.makeText(
                this,
                "Login Successful",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                LoginActivity.this,
                PersonalDashboardActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void resetPassword() {

        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                etEmail.setError("Enter a valid email");
                etEmail.requestFocus();
                return;
            }

            etEmail.setError("Enter your email first");
            etEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->

                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset email sent.",
                                Toast.LENGTH_LONG
                        ).show())

                .addOnFailureListener(e ->

                        Toast.makeText(
                                LoginActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && user.isEmailVerified()) {

            firestore.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {

                        Boolean accountCompleted =
                                document.getBoolean("accountCompleted");

                        Boolean phoneVerified =
                                document.getBoolean("phoneVerified");

                        if (Boolean.TRUE.equals(accountCompleted)
                                && Boolean.TRUE.equals(phoneVerified)) {

                            startActivity(new Intent(
                                    this,
                                    PersonalDashboardActivity.class));

                            finish();
                        }
                    });
        }
    }
}