package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail;
    private CheckBox cbTerms;
    private Button btnVerifyEmail;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private static final String TEMP_PASSWORD = "Temp@123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        cbTerms = findViewById(R.id.cbTerms);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnVerifyEmail.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validate Full Name
        if (fullName.isEmpty()) {
            etFullName.setError("Enter your full name");
            etFullName.requestFocus();
            return;
        }

        // Validate Email
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

        // Terms & Conditions
        if (!cbTerms.isChecked()) {
            Toast.makeText(this,
                    "Please accept the Terms & Conditions",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerifyEmail.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, TEMP_PASSWORD)
                .addOnCompleteListener(task -> {

                    btnVerifyEmail.setEnabled(true);

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {
                            Toast.makeText(this,
                                    "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Update display name
                        UserProfileChangeRequest profile =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();

                        user.updateProfile(profile)
                                .addOnFailureListener(e ->
                                        Toast.makeText(RegisterActivity.this,
                                                "Failed to update profile.",
                                                Toast.LENGTH_SHORT).show());

                        // Save user to Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", user.getUid());
                        userData.put("fullName", fullName);
                        userData.put("email", email);
                        userData.put("emailVerified", false);
                        userData.put("phoneVerified", false);
                        userData.put("phone", "");
                        userData.put("role", "USER");
                        userData.put("isActive", true);
                        userData.put("lastLogin", null);
                        userData.put("accountCompleted", false);
                        userData.put("createdAt",
                                com.google.firebase.firestore.FieldValue.serverTimestamp());

                        firestore.collection("users")
                                .document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(unused -> {

                                    user.sendEmailVerification()
                                            .addOnSuccessListener(unused1 -> {

                                                Toast.makeText(RegisterActivity.this,
                                                        "Verification email sent successfully.",
                                                        Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(
                                                        RegisterActivity.this,
                                                        VerifyEmailActivity.class);

                                                intent.putExtra("email", email);

                                                startActivity(intent);
                                                finish();

                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(RegisterActivity.this,
                                                            e.getMessage(),
                                                            Toast.LENGTH_LONG).show());

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(RegisterActivity.this,
                                                "Failed to save user information.",
                                                Toast.LENGTH_LONG).show());

                    } else {

                        Exception e = task.getException();

                        if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {

                            Toast.makeText(this,
                                    "This email is already registered.",
                                    Toast.LENGTH_LONG).show();

                        } else {

                            Toast.makeText(
                                    this,
                                    e != null ? e.getMessage() : "Registration failed.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                });
    }
}