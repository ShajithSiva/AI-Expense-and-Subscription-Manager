package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreatePasswordActivity extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword;
    private Button btnCreateAccount;
    private ImageButton btnBack;

    private TextView checkMinCharacters;
    private TextView checkUppercase;
    private TextView checkLowercase;
    private TextView checkNumber;
    private TextView checkSpecialCharacter;
    private TextView checkPasswordsMatch;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBack = findViewById(R.id.btnBack);

        checkMinCharacters = findViewById(R.id.checkMinCharacters);
        checkUppercase = findViewById(R.id.checkUppercase);
        checkLowercase = findViewById(R.id.checkLowercase);
        checkNumber = findViewById(R.id.checkNumber);
        checkSpecialCharacter = findViewById(R.id.checkSpecialCharacter);
        checkPasswordsMatch = findViewById(R.id.checkPasswordsMatch);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordLive();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);

        btnBack.setOnClickListener(v -> finish());

        btnCreateAccount.setOnClickListener(v -> createPassword());
    }

    private void validatePasswordLive() {

        String password = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        updateCheck(checkMinCharacters, password.length() >= 8);
        updateCheck(checkUppercase, password.matches(".*[A-Z].*"));
        updateCheck(checkLowercase, password.matches(".*[a-z].*"));
        updateCheck(checkNumber, password.matches(".*\\d.*"));
        updateCheck(checkSpecialCharacter, password.matches(".*[@#$%^&+=!].*"));
        updateCheck(checkPasswordsMatch,
                !confirm.isEmpty() && password.equals(confirm));
    }

    private void updateCheck(TextView view, boolean valid) {

        if (valid) {
            view.setText("✓");
            view.setTextColor(Color.parseColor("#22C55E"));
        } else {
            view.setText("✗");
            view.setTextColor(Color.parseColor("#9CA3AF"));
        }
    }

    private void createPassword() {

        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (password.isEmpty()) {
            etPassword.setError("Enter password");
            return;
        }

        if (confirm.isEmpty()) {
            etConfirmPassword.setError("Confirm password");
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") ||
                !password.matches(".*\\d.*") ||
                !password.matches(".*[@#$%^&+=!].*")) {

            Toast.makeText(this,
                    "Password does not meet requirements",
                    Toast.LENGTH_LONG).show();
            etPassword.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(this,
                    "Session expired.",
                    Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (!user.isEmailVerified()) {

            Toast.makeText(this,
                    "Email is not verified.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreateAccount.setEnabled(false);
        btnCreateAccount.setText("Creating...");

        user.updatePassword(password)
                .addOnSuccessListener(unused -> {

                    firestore.collection("users")
                            .document(user.getUid())
                            .update(
                                    "accountCompleted", true,
                                    "emailVerified", true,
                                    "phoneVerified", true,
                                    "lastLogin",
                                    com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                            .addOnSuccessListener(unused1 -> {

                                Toast.makeText(
                                        CreatePasswordActivity.this,
                                        "Account created successfully!",
                                        Toast.LENGTH_LONG
                                ).show();

                                mAuth.signOut();

                                Intent intent = new Intent(
                                        CreatePasswordActivity.this,
                                        LoginActivity.class);

                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(intent);
                                finish();

                            })
                            .addOnFailureListener(e -> {

                                btnCreateAccount.setEnabled(true);
                                btnCreateAccount.setText("Create Account");

                                Toast.makeText(
                                        CreatePasswordActivity.this,
                                        "Failed to update account: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                            });

                })
                .addOnFailureListener(e -> {

                    btnCreateAccount.setEnabled(true);
                    btnCreateAccount.setText("Create Account");

                    Toast.makeText(
                            CreatePasswordActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }
}
