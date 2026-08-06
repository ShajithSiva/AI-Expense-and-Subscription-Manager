package com.example.aiexpensemanagementapplication.ui.family;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
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

    private TextView tvFamilyName;

    private TextInputEditText etMemberEmail;

    private RadioButton radioMember;
    private RadioButton radioViewer;

    private MaterialButton btnSendInvitation;

    // =====================================================
    // DATABASE / FIRESTORE
    // =====================================================

    private DatabaseHelper databaseHelper;
    private FamilyFirestoreService familyFirestoreService;

    // =====================================================
    // FAMILY
    // =====================================================

    private int localFamilyId = -1;

    private String firestoreFamilyId = null;
    private String familyName = null;

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_member);

        databaseHelper =
                new DatabaseHelper(this);

        familyFirestoreService =
                new FamilyFirestoreService();

        initializeViews();

        loadSelectedFamily();

        setupListeners();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        tvFamilyName =
                findViewById(R.id.tvFamilyName);

        etMemberEmail =
                findViewById(R.id.etMemberEmail);

        radioMember =
                findViewById(R.id.radioMember);

        radioViewer =
                findViewById(R.id.radioViewer);

        btnSendInvitation =
                findViewById(R.id.btnSendInvitation);
    }

    // =====================================================
    // LOAD SELECTED FAMILY
    // =====================================================

    private void loadSelectedFamily() {

        localFamilyId =
                getIntent().getIntExtra(
                        "FAMILY_ID",
                        -1
                );

        if (localFamilyId == -1) {

            Toast.makeText(
                    this,
                    "Selected family was not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        familyName =
                databaseHelper.getFamilyName(
                        localFamilyId
                );

        firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        localFamilyId
                );

        if (familyName == null ||
                familyName.trim().isEmpty()) {

            familyName = "Family";
        }

        if (tvFamilyName != null) {

            tvFamilyName.setText(
                    "Invite someone to " +
                            familyName
            );
        }

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "This family is not connected to Firestore.",
                    Toast.LENGTH_LONG
            ).show();

            btnSendInvitation.setEnabled(false);
        }
    }

    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        btnBack.setOnClickListener(v ->
                finish()
        );

        btnSendInvitation.setOnClickListener(v ->
                validateInvitation()
        );
    }

    // =====================================================
    // VALIDATE INVITATION
    // =====================================================

    private void validateInvitation() {

        String email = "";

        if (etMemberEmail.getText() != null) {

            email = etMemberEmail
                    .getText()
                    .toString()
                    .trim()
                    .toLowerCase();
        }

        if (email.isEmpty()) {

            etMemberEmail.setError(
                    "Please enter member email"
            );

            etMemberEmail.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etMemberEmail.setError(
                    "Please enter a valid email address"
            );

            etMemberEmail.requestFocus();

            return;
        }

        if (firestoreFamilyId == null ||
                firestoreFamilyId
                        .trim()
                        .isEmpty()) {

            Toast.makeText(
                    this,
                    "Family connection was not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String selectedRole;

        if (radioViewer.isChecked()) {

            selectedRole = "VIEWER";

        } else {

            selectedRole = "MEMBER";
        }

        sendInvitation(
                email,
                selectedRole
        );
    }

    // =====================================================
    // SEND INVITATION
    // =====================================================

    private void sendInvitation(
            String memberEmail,
            String selectedRole
    ) {

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {

            Toast.makeText(
                    this,
                    "User session not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String firebaseUid =
                firebaseUser.getUid();

        int localUserId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                firebaseUid
                        );

        if (localUserId == -1) {

            Toast.makeText(
                    this,
                    "Current user was not found locally.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String inviterName =
                getLocalUserName(
                        localUserId
                );

        btnSendInvitation.setEnabled(false);
        btnSendInvitation.setText("Sending...");

        familyFirestoreService
                .sendFamilyInvitation(
                        firestoreFamilyId,
                        familyName,
                        memberEmail,
                        firebaseUid,
                        inviterName,
                        selectedRole,
                        new FamilyFirestoreService
                                .SendInvitationCallback() {

                            @Override
                            public void onSuccess(
                                    String invitationId,
                                    String invitationCode
                            ) {

                                resetSendButton();

                                Toast.makeText(
                                        InviteMemberActivity.this,
                                        "Invitation sent successfully!",
                                        Toast.LENGTH_LONG
                                ).show();

                                etMemberEmail.setText("");

                                radioMember.setChecked(true);
                                radioViewer.setChecked(false);
                            }

                            @Override
                            public void onUserNotFound() {

                                resetSendButton();

                                Toast.makeText(
                                        InviteMemberActivity.this,
                                        "No registered user found with this email.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onAlreadyMember() {

                                resetSendButton();

                                Toast.makeText(
                                        InviteMemberActivity.this,
                                        "This user is already a member of the family.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onInvitationAlreadyPending() {

                                resetSendButton();

                                Toast.makeText(
                                        InviteMemberActivity.this,
                                        "An invitation is already pending for this user.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onCannotInviteYourself() {

                                resetSendButton();

                                Toast.makeText(
                                        InviteMemberActivity.this,
                                        "You cannot invite your own account.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onFailure(
                                    String message
                            ) {

                                resetSendButton();

                                Toast.makeText(
                                        InviteMemberActivity.this,
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

    private String getLocalUserName(
            int userId
    ) {

        Cursor cursor =
                databaseHelper.getUserById(
                        userId
                );

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
    // RESET BUTTON
    // =====================================================

    private void resetSendButton() {

        btnSendInvitation.setEnabled(true);

        btnSendInvitation.setText(
                "Send Invitation"
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