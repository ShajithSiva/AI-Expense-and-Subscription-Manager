package com.example.aiexpensemanagementapplication.ui.auth;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.aiexpensemanagementapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterActivity extends AppCompatActivity {
    EditText etFullName, etEmail;
    CheckBox cbTerms;
    Button btnVerifyEmail;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        cbTerms = findViewById(R.id.cbTerms);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);

        mAuth = FirebaseAuth.getInstance();

        btnVerifyEmail.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Accept Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Temporary password (required by Firebase)
        String tempPassword = "Temp@123";

        mAuth.createUserWithEmailAndPassword(email, tempPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnSuccessListener(unused -> {

                                        Toast.makeText(this,
                                                "Verification email sent",
                                                Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(this, VerifyEmailActivity.class);
                                        intent.putExtra("email", email);
                                        startActivity(intent);

                                    });
                        }

                    } else {
                        Toast.makeText(this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
