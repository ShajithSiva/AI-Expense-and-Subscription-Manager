package com.example.aiexpensemanagementapplication.ui.family;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateFamilyActivity extends AppCompatActivity {

    // =====================================================
    // UI
    // =====================================================

    private ImageButton btnBack;

    private TextInputEditText etFamilyName;
    private TextInputEditText etFamilyDescription;

    private MaterialButton btnCreateFamily;

    // =====================================================
    // DATABASE
    // =====================================================

    private DatabaseHelper databaseHelper;

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_family);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();

        setupListeners();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        etFamilyName = findViewById(R.id.etFamilyName);


        btnCreateFamily =
                findViewById(R.id.btnCreateFamily);
    }

    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnCreateFamily.setOnClickListener(v ->
                validateFamilyDetails()
        );
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateFamilyDetails() {

        String familyName = "";

        if (etFamilyName.getText() != null) {

            familyName =
                    etFamilyName
                            .getText()
                            .toString()
                            .trim();
        }

        // Empty validation
        if (familyName.isEmpty()) {

            etFamilyName.setError(
                    "Please enter a family name"
            );

            etFamilyName.requestFocus();

            return;
        }

        // Minimum length
        if (familyName.length() < 3) {

            etFamilyName.setError(
                    "Family name must contain at least 3 characters"
            );

            etFamilyName.requestFocus();

            return;
        }

        createFamily(familyName);
    }

    // =====================================================
    // CREATE FAMILY
    // =====================================================

    private void createFamily(String familyName) {

        // Get currently logged-in Firebase user
        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {

            Toast.makeText(
                    this,
                    "User session not found. Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String firebaseUid = firebaseUser.getUid();

        // -------------------------------------------------
        // Get local SQLite UserID using Firebase UID
        // -------------------------------------------------

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        firebaseUid
                );

        if (userId == -1) {

            Toast.makeText(
                    this,
                    "User account not found in local database.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // Check whether user already belongs to a family
        // -------------------------------------------------

        if (databaseHelper.userHasFamily(userId)) {

            Toast.makeText(
                    this,
                    "You already belong to a family.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        // -------------------------------------------------
        // Create family
        // -------------------------------------------------

        long familyId =
                databaseHelper.createFamily(
                        familyName,
                        userId
                );

        if (familyId != -1) {

            Toast.makeText(
                    this,
                    "Family created successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            /*
             * We simply close CreateFamilyActivity.
             *
             * User returns to DashboardActivity /
             * FamilyDashboardFragment.
             *
             * FamilyDashboardFragment will then detect
             * that the user belongs to a family.
             */

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Failed to create family. Please try again.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}