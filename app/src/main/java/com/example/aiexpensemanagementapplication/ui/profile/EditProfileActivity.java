package com.example.aiexpensemanagementapplication.ui.profile;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;

    private ImageButton btnBack;
    private ImageButton btnChangePhoto;

    private String originalName;

    private String originalMobile;

    private EditText etName;
    private EditText etEmail;
    private EditText etMobile;

    private TextView tvStatus;

    private MaterialButton btnSave;

    private DatabaseHelper databaseHelper;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        initialize();

        loadUser();

        listeners();
    }

    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        imgProfile = findViewById(R.id.imgProfile);

        btnBack = findViewById(R.id.btnBack);

        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        etName = findViewById(R.id.etName);

        firestore = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);

        etMobile = findViewById(R.id.etMobile);

        tvStatus = findViewById(R.id.tvStatus);

        btnSave = findViewById(R.id.btnSave);
    }

    private void listeners() {

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveProfile());

    }

    private void loadUser() {

        if (currentUser == null) {
            finish();
            return;
        }

        int userId = databaseHelper.getUserIdByFirebaseUid(
                currentUser.getUid());

        if (userId == -1) {
            finish();
            return;
        }

        Cursor cursor = databaseHelper.getUserById(userId);

        if (cursor != null && cursor.moveToFirst()) {

            // Name
            originalName = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.USER_NAME));
            etName.setText(originalName);

            // Mobile
            String firebaseMobile = currentUser.getPhoneNumber();

            if (firebaseMobile != null && !firebaseMobile.isEmpty()) {

                if (firebaseMobile.startsWith("+94")) {
                    firebaseMobile = "0" + firebaseMobile.substring(3);
                }

                originalMobile = firebaseMobile;

            } else {

                originalMobile = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.USER_MOBILE));
            }

            etMobile.setText(originalMobile);

            // Email
            etEmail.setText(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL)));

            // Status
            tvStatus.setText(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseHelper.USER_STATUS)));
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void saveProfile() {

        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Enter your name");
            etName.requestFocus();
            return;
        }

        if (mobile.isEmpty()) {
            etMobile.setError("Enter mobile number");
            etMobile.requestFocus();
            return;
        }

        if (!mobile.matches("^(\\+94|0)?7\\d{8}$")) {
            etMobile.setError("Enter a valid Sri Lankan mobile number");
            etMobile.requestFocus();
            return;
        }

        boolean nameChanged = !originalName.equals(name);
        boolean mobileChanged = !originalMobile.equals(mobile);

        if (!nameChanged && !mobileChanged) {

            Toast.makeText(this,
                    "No changes detected.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        int userId = databaseHelper.getUserIdByFirebaseUid(
                currentUser.getUid());

        if (mobileChanged) {

            Intent intent = new Intent(
                    EditProfileActivity.this,
                    ChangeMobileVerificationActivity.class);

            intent.putExtra("USER_ID", userId);
            intent.putExtra("NAME", name);
            intent.putExtra("OLD_MOBILE", originalMobile);
            intent.putExtra("NEW_MOBILE", mobile);

            startActivity(intent);

        } else {

            updateProfile(name, mobile);

        }
    }

    private void updateProfile(String name, String mobile) {

        int userId = databaseHelper.getUserIdByFirebaseUid(currentUser.getUid());

        boolean success = databaseHelper.updateUserProfile(
                userId,
                name,
                mobile);

        if (success) {

            firestore.collection("users")
                    .document(currentUser.getUid())
                    .update("fullName", name)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                EditProfileActivity.this,
                                "Profile Updated Successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        setResult(RESULT_OK);

                        finish();

                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                EditProfileActivity.this,
                                "SQLite updated, but Firestore update failed.",
                                Toast.LENGTH_LONG
                        ).show();

                    });

        } else {

            Toast.makeText(
                    this,
                    "Failed to update profile",
                    Toast.LENGTH_SHORT
            ).show();

        }

    }

}

