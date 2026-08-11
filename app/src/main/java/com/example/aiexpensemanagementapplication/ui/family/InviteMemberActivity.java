package com.example.aiexpensemanagementapplication.ui.family;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InviteMemberActivity extends AppCompatActivity {

    // =====================================================
    // UI
    // =====================================================

    private ImageButton btnBack;

    private TextInputEditText etMemberEmail;

    private RadioGroup roleRadioGroup;

    private RadioButton radioMember;
    private RadioButton radioViewer;

    private MaterialButton btnSendInvitation;

    // =====================================================
    // DATABASE
    // =====================================================

    private DatabaseHelper databaseHelper;

    // Local SQLite family ID
    private int familyId = -1;

    // Firestore family ID
    private String firestoreFamilyId = "";

    private String familyName = "Family";

    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_member);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Local database
        databaseHelper = new DatabaseHelper(this);

        // -------------------------------------------------
        // Get LOCAL family ID
        // -------------------------------------------------

        familyId = getIntent().getIntExtra(
                "FAMILY_ID",
                -1
        );

        if (familyId == -1) {

            Toast.makeText(
                    this,
                    "Family not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // -------------------------------------------------
        // Get family name
        // -------------------------------------------------

        String localFamilyName =
                databaseHelper.getFamilyName(familyId);

        if (localFamilyName != null &&
                !localFamilyName.trim().isEmpty()) {

            familyName = localFamilyName.trim();
        }

        // -------------------------------------------------
        // Get Firestore Family ID
        // -------------------------------------------------

        firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(familyId);

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Firestore family ID not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        firestoreFamilyId =
                firestoreFamilyId.trim();

        initializeViews();

        setupListeners();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        etMemberEmail =
                findViewById(R.id.etMemberEmail);

        roleRadioGroup =
                findViewById(R.id.roleRadioGroup);

        radioMember =
                findViewById(R.id.radioMember);

        radioViewer =
                findViewById(R.id.radioViewer);

        btnSendInvitation =
                findViewById(R.id.btnSendInvitation);
    }

    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        // Back
        btnBack.setOnClickListener(v ->
                finish()
        );

        // Send invitation
        btnSendInvitation.setOnClickListener(v ->
                sendInvitation()
        );
    }

    // =====================================================
    // SEND INVITATION
    // =====================================================

    private void sendInvitation() {

        // -------------------------------------------------
        // Check login
        // -------------------------------------------------

        currentUser =
                mAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // Get email
        // -------------------------------------------------

        String email = "";

        if (etMemberEmail.getText() != null) {

            email =
                    etMemberEmail
                            .getText()
                            .toString()
                            .trim()
                            .toLowerCase();
        }

        // -------------------------------------------------
        // Validate email
        // -------------------------------------------------

        if (TextUtils.isEmpty(email)) {

            etMemberEmail.setError(
                    "Enter member email"
            );

            etMemberEmail.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etMemberEmail.setError(
                    "Enter a valid email address"
            );

            etMemberEmail.requestFocus();

            return;
        }

        // -------------------------------------------------
        // Prevent inviting yourself
        // -------------------------------------------------

        String currentUserEmail =
                currentUser.getEmail();

        if (currentUserEmail != null &&
                currentUserEmail
                        .trim()
                        .equalsIgnoreCase(email)) {

            Toast.makeText(
                    this,
                    "You cannot invite yourself.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // Get selected role
        // -------------------------------------------------

        String role;

        if (radioViewer != null &&
                radioViewer.isChecked()) {

            role = "viewer";

        } else {

            role = "member";
        }

        // -------------------------------------------------
        // Inviter UID
        // -------------------------------------------------

        String inviterUid =
                currentUser.getUid();

        // -------------------------------------------------
        // IMPORTANT
        //
        // Create final variables for lambda
        // -------------------------------------------------

        final String finalEmail = email;
        final String finalRole = role;
        final String finalInviterUid = inviterUid;
        final String finalFirestoreFamilyId =
                firestoreFamilyId;

        // -------------------------------------------------
        // Disable button
        // -------------------------------------------------

        btnSendInvitation.setEnabled(false);
        btnSendInvitation.setText("Checking...");

        // =================================================
        // CHECK DUPLICATE INVITATION
        // =================================================

        firestore
                .collection("familyInvitations")

                .whereEqualTo(
                        "familyId",
                        finalFirestoreFamilyId
                )

                .whereEqualTo(
                        "invitedEmail",
                        finalEmail
                )

                .whereEqualTo(
                        "status",
                        "pending"
                )

                .get()

                .addOnSuccessListener(
                        querySnapshot -> {

                            if (!querySnapshot.isEmpty()) {

                                btnSendInvitation
                                        .setEnabled(true);

                                btnSendInvitation
                                        .setText(
                                                "Send Invitation"
                                        );

                                Toast.makeText(
                                        InviteMemberActivity.this,
                                        "A pending invitation already exists for this email.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // -------------------------------------------------
                            // No duplicate found
                            // -------------------------------------------------

                            createInvitation(
                                    finalEmail,
                                    finalRole,
                                    finalInviterUid,
                                    finalFirestoreFamilyId
                            );
                        }
                )

                .addOnFailureListener(
                        e -> {

                            btnSendInvitation
                                    .setEnabled(true);

                            btnSendInvitation
                                    .setText(
                                            "Send Invitation"
                                    );

                            Toast.makeText(
                                    InviteMemberActivity.this,
                                    "Unable to check invitation: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =====================================================
    // CREATE INVITATION
    // =====================================================

    private void createInvitation(
            String email,
            String role,
            String inviterUid,
            String firestoreFamilyId
    ) {

        Map<String, Object> invitation =
                new HashMap<>();

        // -------------------------------------------------
        // IMPORTANT
        //
        // Store Firestore Family ID as STRING
        // -------------------------------------------------

        invitation.put(
                "familyId",
                firestoreFamilyId
        );

        invitation.put(
                "familyName",
                familyName
        );

        invitation.put(
                "invitedEmail",
                email
        );

        invitation.put(
                "invitedBy",
                inviterUid
        );

        invitation.put(
                "role",
                role
        );

        invitation.put(
                "status",
                "pending"
        );

        // Store createdAt as LONG
        // because FamilyInvitation uses long createdAt
        invitation.put(
                "createdAt",
                System.currentTimeMillis()
        );

        // -------------------------------------------------
        // Button state
        // -------------------------------------------------

        btnSendInvitation.setEnabled(false);
        btnSendInvitation.setText("Sending...");

        // =================================================
        // SAVE TO FIRESTORE
        // =================================================

        firestore
                .collection("familyInvitations")
                .add(invitation)

                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    InviteMemberActivity.this,
                                    "Invitation sent successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            btnSendInvitation
                                    .setEnabled(true);

                            btnSendInvitation
                                    .setText(
                                            "Send Invitation"
                                    );

                            Toast.makeText(
                                    InviteMemberActivity.this,
                                    "Failed to send invitation: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
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