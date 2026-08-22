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
import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


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
    private FirebaseUser currentUser;
    private FamilyFirestoreService familyFirestoreService;

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_member);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        familyFirestoreService = new FamilyFirestoreService();

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

        // -------------------------------------------------
        // SECURITY CHECK
        // Only the Family Head / PRIMARY user can invite.
        // Do this before showing an operational invite screen.
        // -------------------------------------------------

        if (!isCurrentUserPrimary()) {

            showPrimaryOnlyAlert();

            finish();
            return;
        }

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
        // SECURITY CHECK
        // Re-check permission before sending.
        // -------------------------------------------------

        if (!isCurrentUserPrimary()) {

            showPrimaryOnlyAlert();
            return;
        }

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
        // FINAL VALUES
        // -------------------------------------------------

        final String finalEmail = email;
        final String finalRole = role;
        final String finalInviterUid = inviterUid;
        final String finalFirestoreFamilyId =
                firestoreFamilyId;

        // -------------------------------------------------
        // DISABLE BUTTON WHILE REQUEST IS RUNNING
        // -------------------------------------------------

        btnSendInvitation.setEnabled(false);
        btnSendInvitation.setText("Sending...");

        // =================================================
        // SEND THROUGH SECURED FIRESTORE SERVICE
        // The service checks:
        // 1. Family exists
        // 2. inviterUid == ownerUid
        // 3. No duplicate pending invitation
        // =================================================

        familyFirestoreService.sendInvitation(
                finalFirestoreFamilyId,
                familyName,
                finalEmail,
                finalInviterUid,
                finalRole,
                new FamilyFirestoreService.InvitationCallback() {

                    @Override
                    public void onSuccess(
                            String invitationId
                    ) {

                        btnSendInvitation.setEnabled(true);
                        btnSendInvitation.setText(
                                "Send Invitation"
                        );

                        Toast.makeText(
                                InviteMemberActivity.this,
                                "Invitation sent successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        btnSendInvitation.setEnabled(true);
                        btnSendInvitation.setText(
                                "Send Invitation"
                        );

                        Toast.makeText(
                                InviteMemberActivity.this,
                                message == null ||
                                        message.trim().isEmpty()
                                        ? "Failed to send invitation."
                                        : message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // CHECK WHETHER CURRENT USER IS FAMILY HEAD / PRIMARY
    // =====================================================

    private boolean isCurrentUserPrimary() {

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (user == null) {
            return false;
        }

        int userId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                user.getUid()
                        );

        if (userId == -1) {
            return false;
        }

        String role =
                databaseHelper
                        .getFamilyRole(
                                userId,
                                familyId
                        );

        return role != null &&
                "PRIMARY".equalsIgnoreCase(
                        role.trim()
                );
    }


    // =====================================================
    // PRIMARY-ONLY ALERT
    // =====================================================

    private void showPrimaryOnlyAlert() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage(
                        "Only the Family Head can invite family members."
                )
                .setPositiveButton("OK", null)
                .show();
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
