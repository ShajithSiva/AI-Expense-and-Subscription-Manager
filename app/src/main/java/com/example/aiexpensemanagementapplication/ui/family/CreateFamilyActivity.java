package com.example.aiexpensemanagementapplication.ui.family;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;
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
    private MaterialButton btnCreateFamily;

    // =====================================================
    // DATABASE / FIRESTORE
    // =====================================================

    private DatabaseHelper databaseHelper;
    private FamilyFirestoreService familyFirestoreService;

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_family);

        databaseHelper = new DatabaseHelper(this);
        familyFirestoreService = new FamilyFirestoreService();

        initializeViews();
        setupListeners();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);
        etFamilyName = findViewById(R.id.etFamilyName);
        btnCreateFamily = findViewById(R.id.btnCreateFamily);
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

            familyName = etFamilyName
                    .getText()
                    .toString()
                    .trim();
        }

        if (familyName.isEmpty()) {

            etFamilyName.setError(
                    "Please enter a family name"
            );

            etFamilyName.requestFocus();

            return;
        }

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

        String creatorName =
                getLocalUserName(userId);

        btnCreateFamily.setEnabled(false);
        btnCreateFamily.setText("Creating...");

        familyFirestoreService.createFamily(
                familyName,
                firebaseUid,
                creatorName,
                new FamilyFirestoreService.CreateFamilyCallback() {

                    @Override
                    public void onSuccess(
                            String firestoreFamilyId,
                            String inviteCode
                    ) {

                        long localFamilyId =
                                databaseHelper.createFamilyWithFirestoreId(
                                        familyName,
                                        firestoreFamilyId,
                                        userId
                                );

                        btnCreateFamily.setEnabled(true);
                        btnCreateFamily.setText("Create Family");

                        if (localFamilyId == -1) {

                            Toast.makeText(
                                    CreateFamilyActivity.this,
                                    "Family created online, but local save failed.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        Toast.makeText(
                                CreateFamilyActivity.this,
                                "Family created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onFailure(String message) {

                        btnCreateFamily.setEnabled(true);
                        btnCreateFamily.setText("Create Family");

                        Toast.makeText(
                                CreateFamilyActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // GET LOCAL USER NAME
    // =====================================================

    private String getLocalUserName(int userId) {

        Cursor cursor =
                databaseHelper.getUserById(userId);

        String userName = "";

        if (cursor != null) {

            try {

                if (cursor.moveToFirst()) {

                    int nameIndex =
                            cursor.getColumnIndex(
                                    DatabaseHelper.USER_NAME
                            );

                    if (nameIndex != -1) {

                        userName =
                                cursor.getString(
                                        nameIndex
                                );
                    }
                }

            } finally {

                cursor.close();
            }
        }

        return userName == null
                ? ""
                : userName;
    }

    // =====================================================
    // CLEANUP
    // =====================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}