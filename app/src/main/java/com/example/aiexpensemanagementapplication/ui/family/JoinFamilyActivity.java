package com.example.aiexpensemanagementapplication.ui.family;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class JoinFamilyActivity extends AppCompatActivity {

    // =====================================================
    // UI
    // =====================================================

    private ImageButton btnBack;

    private TextInputEditText etInviteCode;
    private MaterialButton btnFindFamily;

    private MaterialButton btnAcceptInvitation;
    private MaterialButton btnDeclineInvitation;

    private MaterialCardView cardNoInvitations;
    private MaterialCardView cardInvitation;

    private TextView tvInvitationCount;
    private TextView tvInvitationFamilyName;
    private TextView tvInvitedBy;
    private TextView tvInvitationRole;

    // =====================================================
    // DATABASE / FIRESTORE
    // =====================================================

    private DatabaseHelper databaseHelper;
    private FamilyFirestoreService familyFirestoreService;

    // =====================================================
    // CURRENT INVITATION
    // =====================================================

    private FamilyFirestoreService.FamilyInvitation currentInvitation;

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_family);

        databaseHelper = new DatabaseHelper(this);
        familyFirestoreService = new FamilyFirestoreService();

        initializeViews();
        setupInitialState();
        setupListeners();
        loadPendingInvitations();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        etInviteCode = findViewById(R.id.etInviteCode);
        btnFindFamily = findViewById(R.id.btnFindFamily);

        btnAcceptInvitation =
                findViewById(R.id.btnAcceptInvitation);

        btnDeclineInvitation =
                findViewById(R.id.btnDeclineInvitation);

        cardNoInvitations =
                findViewById(R.id.cardNoInvitations);

        cardInvitation =
                findViewById(R.id.cardInvitation);

        tvInvitationCount =
                findViewById(R.id.tvInvitationCount);

        tvInvitationFamilyName =
                findViewById(R.id.tvInvitationFamilyName);

        tvInvitedBy =
                findViewById(R.id.tvInvitedBy);

        tvInvitationRole =
                findViewById(R.id.tvInvitationRole);
    }

    // =====================================================
    // INITIAL STATE
    // =====================================================

    private void setupInitialState() {

        /*
         * Manual code joining is not used.
         * Family invitations are sent using email.
         */

        if (etInviteCode != null) {
            etInviteCode.setVisibility(View.GONE);
        }

        if (btnFindFamily != null) {
            btnFindFamily.setVisibility(View.GONE);
        }

        showNoInvitations();
    }

    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        btnBack.setOnClickListener(v ->
                finish()
        );

        btnAcceptInvitation.setOnClickListener(v ->
                acceptCurrentInvitation()
        );

        btnDeclineInvitation.setOnClickListener(v ->
                declineCurrentInvitation()
        );
    }

    // =====================================================
    // LOAD PENDING INVITATIONS
    // =====================================================

    private void loadPendingInvitations() {

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

            showNoInvitations();

            return;
        }

        String firebaseUid =
                firebaseUser.getUid();

        familyFirestoreService.getPendingInvitations(
                firebaseUid,
                new FamilyFirestoreService.PendingInvitationsCallback() {

                    @Override
                    public void onSuccess(
                            List<FamilyFirestoreService.FamilyInvitation>
                                    invitations
                    ) {

                        if (invitations == null ||
                                invitations.isEmpty()) {

                            currentInvitation = null;
                            showNoInvitations();

                            return;
                        }

                        currentInvitation =
                                invitations.get(0);

                        showInvitation(
                                currentInvitation,
                                invitations.size()
                        );
                    }

                    @Override
                    public void onFailure(String message) {

                        currentInvitation = null;
                        showNoInvitations();

                        Toast.makeText(
                                JoinFamilyActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // SHOW INVITATION
    // =====================================================

    private void showInvitation(
            FamilyFirestoreService.FamilyInvitation invitation,
            int invitationCount
    ) {

        if (cardNoInvitations != null) {
            cardNoInvitations.setVisibility(View.GONE);
        }

        if (cardInvitation != null) {
            cardInvitation.setVisibility(View.VISIBLE);
        }

        if (tvInvitationCount != null) {
            tvInvitationCount.setText(
                    String.valueOf(invitationCount)
            );
        }

        String familyName =
                invitation.getFamilyName();

        if (familyName == null ||
                familyName.trim().isEmpty()) {

            familyName = "Family";
        }

        if (tvInvitationFamilyName != null) {
            tvInvitationFamilyName.setText(familyName);
        }

        String inviterName =
                invitation.getInvitedByName();

        if (inviterName == null ||
                inviterName.trim().isEmpty()) {

            inviterName = "Family administrator";
        }

        if (tvInvitedBy != null) {
            tvInvitedBy.setText(
                    "Invited by: " + inviterName
            );
        }

        String role =
                invitation.getRole();

        if (role == null ||
                role.trim().isEmpty()) {

            role = "MEMBER";
        }

        if (tvInvitationRole != null) {
            tvInvitationRole.setText(role);
        }

        btnAcceptInvitation.setEnabled(true);
        btnAcceptInvitation.setText("Accept");

        btnDeclineInvitation.setEnabled(true);
        btnDeclineInvitation.setText("Decline");
    }

    // =====================================================
    // SHOW NO INVITATIONS
    // =====================================================

    private void showNoInvitations() {

        if (cardNoInvitations != null) {
            cardNoInvitations.setVisibility(View.VISIBLE);
        }

        if (cardInvitation != null) {
            cardInvitation.setVisibility(View.GONE);
        }

        if (tvInvitationCount != null) {
            tvInvitationCount.setText("0");
        }
    }

    // =====================================================
    // ACCEPT INVITATION
    // =====================================================

    private void acceptCurrentInvitation() {

        if (currentInvitation == null) {

            Toast.makeText(
                    this,
                    "No invitation selected.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

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
                    "User account was not found locally.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String userName =
                getLocalUserName(localUserId);

        btnAcceptInvitation.setEnabled(false);
        btnDeclineInvitation.setEnabled(false);

        btnAcceptInvitation.setText("Accepting...");

        familyFirestoreService.acceptInvitation(
                currentInvitation,
                firebaseUid,
                userName,
                new FamilyFirestoreService.AcceptInvitationCallback() {

                    @Override
                    public void onSuccess(
                            String firestoreFamilyId,
                            String familyName,
                            String role
                    ) {

                        long localFamilyId =
                                databaseHelper
                                        .createJoinedFamilyWithFirestoreId(
                                                familyName,
                                                firestoreFamilyId,
                                                localUserId,
                                                role
                                        );

                        if (localFamilyId == -1) {

                            resetInvitationButtons();

                            Toast.makeText(
                                    JoinFamilyActivity.this,
                                    "Invitation accepted online, but local save failed.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        Toast.makeText(
                                JoinFamilyActivity.this,
                                "You joined " +
                                        familyName +
                                        " successfully!",
                                Toast.LENGTH_LONG
                        ).show();

                        currentInvitation = null;

                        loadPendingInvitations();
                    }

                    @Override
                    public void onAlreadyAccepted() {

                        resetInvitationButtons();

                        Toast.makeText(
                                JoinFamilyActivity.this,
                                "This invitation was already processed.",
                                Toast.LENGTH_LONG
                        ).show();

                        currentInvitation = null;

                        loadPendingInvitations();
                    }

                    @Override
                    public void onFailure(String message) {

                        resetInvitationButtons();

                        Toast.makeText(
                                JoinFamilyActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // DECLINE INVITATION
    // =====================================================

    private void declineCurrentInvitation() {

        if (currentInvitation == null) {

            Toast.makeText(
                    this,
                    "No invitation selected.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

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

        btnAcceptInvitation.setEnabled(false);
        btnDeclineInvitation.setEnabled(false);

        btnDeclineInvitation.setText("Declining...");

        familyFirestoreService.declineInvitation(
                currentInvitation.getInvitationId(),
                firebaseUser.getUid(),
                new FamilyFirestoreService.DeclineInvitationCallback() {

                    @Override
                    public void onSuccess() {

                        Toast.makeText(
                                JoinFamilyActivity.this,
                                "Invitation declined.",
                                Toast.LENGTH_SHORT
                        ).show();

                        currentInvitation = null;

                        loadPendingInvitations();
                    }

                    @Override
                    public void onFailure(String message) {

                        resetInvitationButtons();

                        Toast.makeText(
                                JoinFamilyActivity.this,
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
    // RESET BUTTONS
    // =====================================================

    private void resetInvitationButtons() {

        btnAcceptInvitation.setEnabled(true);
        btnAcceptInvitation.setText("Accept");

        btnDeclineInvitation.setEnabled(true);
        btnDeclineInvitation.setText("Decline");
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