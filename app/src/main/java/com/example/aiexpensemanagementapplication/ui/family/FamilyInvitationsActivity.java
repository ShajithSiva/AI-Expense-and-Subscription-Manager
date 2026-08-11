package com.example.aiexpensemanagementapplication.ui.family;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;
import com.example.aiexpensemanagementapplication.model.FamilyInvitation;
import com.example.aiexpensemanagementapplication.ui.dashboard.FamilyDashboardActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FamilyInvitationsActivity extends AppCompatActivity {

    // =====================================================
    // TAG
    // =====================================================

    private static final String TAG = "FAMILY_INVITATION";

    // =====================================================
    // UI
    // =====================================================

    private MaterialToolbar toolbar;

    private RecyclerView recyclerInvitations;

    private TextView tvEmptyInvitations;

    private CircularProgressIndicator progressIndicator;

    // =====================================================
    // FIREBASE
    // =====================================================

    private FirebaseAuth mAuth;

    private FirebaseFirestore firestore;

    private FirebaseUser currentUser;

    private FamilyFirestoreService familyFirestoreService;

    // =====================================================
    // LOCAL DATABASE
    // =====================================================

    private DatabaseHelper databaseHelper;

    // =====================================================
    // DATA
    // =====================================================

    private final List<FamilyInvitation> invitationList =
            new ArrayList<>();

    private FamilyInvitationAdapter adapter;

    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_family_invitations
        );

        initializeFirebase();

        initializeServices();

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        loadInvitations();
    }

    // =====================================================
    // INITIALIZE FIREBASE
    // =====================================================

    private void initializeFirebase() {

        mAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        currentUser =
                mAuth.getCurrentUser();
    }

    // =====================================================
    // INITIALIZE SERVICES
    // =====================================================

    private void initializeServices() {

        familyFirestoreService =
                new FamilyFirestoreService();

        databaseHelper =
                new DatabaseHelper(this);
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );

        recyclerInvitations =
                findViewById(
                        R.id.recyclerInvitations
                );

        tvEmptyInvitations =
                findViewById(
                        R.id.tvEmptyInvitations
                );

        progressIndicator =
                findViewById(
                        R.id.progressIndicator
                );
    }

    // =====================================================
    // TOOLBAR
    // =====================================================

    private void setupToolbar() {

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }

    // =====================================================
    // RECYCLER VIEW
    // =====================================================

    private void setupRecyclerView() {

        recyclerInvitations.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new FamilyInvitationAdapter(
                        this,
                        invitationList,
                        new FamilyInvitationAdapter.InvitationActionListener() {

                            @Override
                            public void onAccept(
                                    FamilyInvitation invitation
                            ) {

                                acceptInvitation(
                                        invitation
                                );
                            }

                            @Override
                            public void onReject(
                                    FamilyInvitation invitation
                            ) {

                                rejectInvitation(
                                        invitation
                                );
                            }
                        }
                );

        recyclerInvitations.setAdapter(
                adapter
        );
    }

    // =====================================================
    // LOAD INVITATIONS
    // =====================================================

    private void loadInvitations() {

        if (currentUser == null) {

            showLoading(false);

            showEmptyState();

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String userEmail =
                currentUser.getEmail();

        if (userEmail == null ||
                userEmail.trim().isEmpty()) {

            showLoading(false);

            showEmptyState();

            Toast.makeText(
                    this,
                    "Your account does not have an email address.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        final String email =
                userEmail
                        .trim()
                        .toLowerCase(Locale.ROOT);

        Log.d(
                TAG,
                "Current user email = " + email
        );

        showLoading(true);

        /*
         * Query only by email.
         *
         * Status is checked manually because this avoids
         * problems caused by Firestore composite indexes
         * and also lets us safely handle old invitation data.
         */

        firestore
                .collection("familyInvitations")
                .whereEqualTo(
                        "invitedEmail",
                        email
                )
                .get()

                .addOnSuccessListener(
                        querySnapshot -> {

                            invitationList.clear();

                            Log.d(
                                    TAG,
                                    "Firestore documents found = "
                                            + querySnapshot.size()
                            );

                            for (
                                    QueryDocumentSnapshot document
                                    : querySnapshot
                            ) {

                                try {

                                    String status =
                                            document.getString(
                                                    "status"
                                            );

                                    Log.d(
                                            TAG,
                                            "Document ID = "
                                                    + document.getId()
                                    );

                                    Log.d(
                                            TAG,
                                            "Status = "
                                                    + status
                                    );

                                    // Only pending invitations
                                    if (status == null ||
                                            !status
                                                    .trim()
                                                    .equalsIgnoreCase(
                                                            "pending"
                                                    )) {

                                        continue;
                                    }

                                    FamilyInvitation invitation =
                                            document.toObject(
                                                    FamilyInvitation.class
                                            );

                                    if (invitation == null) {
                                        continue;
                                    }

                                    invitation.setInvitationId(
                                            document.getId()
                                    );

                                    invitationList.add(
                                            invitation
                                    );

                                    Log.d(
                                            TAG,
                                            "Family ID = "
                                                    + invitation.getFamilyId()
                                    );

                                    Log.d(
                                            TAG,
                                            "Family Name = "
                                                    + invitation.getFamilyName()
                                    );

                                    Log.d(
                                            TAG,
                                            "Role = "
                                                    + invitation.getRole()
                                    );

                                } catch (Exception e) {

                                    Log.e(
                                            TAG,
                                            "Error reading invitation document",
                                            e
                                    );
                                }
                            }

                            adapter.notifyDataSetChanged();

                            showLoading(false);

                            updateEmptyState();

                            Log.d(
                                    TAG,
                                    "Pending invitations = "
                                            + invitationList.size()
                            );

                            Toast.makeText(
                                    FamilyInvitationsActivity.this,
                                    "Found "
                                            + invitationList.size()
                                            + " invitation(s)",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            showLoading(false);

                            Log.e(
                                    TAG,
                                    "Failed to load invitations",
                                    e
                            );

                            Toast.makeText(
                                    FamilyInvitationsActivity.this,
                                    "Failed to load invitations:\n"
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =====================================================
    // ACCEPT INVITATION
    // =====================================================

    private void acceptInvitation(
            FamilyInvitation invitation
    ) {

        // -------------------------------------------------
        // CHECK LOGIN
        // -------------------------------------------------

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // INVITATION ID
        // -------------------------------------------------

        String invitationId =
                invitation.getInvitationId();

        if (invitationId == null ||
                invitationId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid invitation.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // FIREBASE UID
        // -------------------------------------------------

        String firebaseUid =
                currentUser.getUid();

        // -------------------------------------------------
        // LOCAL USER ID
        // -------------------------------------------------

        int localUserId =
                databaseHelper.getUserIdByFirebaseUid(
                        firebaseUid
                );

        if (localUserId == -1) {

            Toast.makeText(
                    this,
                    "User account not found in local database.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // -------------------------------------------------
        // USER NAME
        // -------------------------------------------------

        String userName =
                getLocalUserName(
                        localUserId
                );

        // -------------------------------------------------
        // LOADING
        // -------------------------------------------------

        showLoading(true);

        // -------------------------------------------------
        // ACCEPT FIRESTORE INVITATION
        // -------------------------------------------------

        familyFirestoreService.acceptInvitation(
                invitationId,
                firebaseUid,
                userName,

                new FamilyFirestoreService
                        .AcceptInvitationCallback() {

                    @Override
                    public void onSuccess(
                            String firestoreFamilyId,
                            String familyName,
                            String role
                    ) {

                        Log.d(
                                TAG,
                                "Firestore family ID = "
                                        + firestoreFamilyId
                        );

                        Log.d(
                                TAG,
                                "Family name = "
                                        + familyName
                        );

                        Log.d(
                                TAG,
                                "Role = "
                                        + role
                        );

                        // ---------------------------------------------
                        // NORMALIZE ROLE
                        // ---------------------------------------------

                        String finalRole =
                                role == null ||
                                        role.trim().isEmpty()
                                        ? "MEMBER"
                                        : role
                                        .trim()
                                        .toUpperCase(
                                                Locale.ROOT
                                        );

                        // ---------------------------------------------
                        // SAVE FAMILY TO LOCAL SQLITE
                        // ---------------------------------------------

                        long localFamilyId =
                                databaseHelper
                                        .createJoinedFamilyWithFirestoreId(
                                                familyName,
                                                firestoreFamilyId,
                                                localUserId,
                                                finalRole
                                        );

                        Log.d(
                                TAG,
                                "Local SQLite family ID = "
                                        + localFamilyId
                        );

                        // ---------------------------------------------
                        // CHECK LOCAL SAVE
                        // ---------------------------------------------

                        if (localFamilyId == -1) {

                            showLoading(false);

                            Toast.makeText(
                                    FamilyInvitationsActivity.this,
                                    "Invitation accepted online, but family could not be saved locally.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        // ---------------------------------------------
                        // REMOVE INVITATION FROM LIST
                        // ---------------------------------------------

                        invitationList.remove(
                                invitation
                        );

                        adapter.notifyDataSetChanged();

                        updateEmptyState();

                        showLoading(false);

                        // ---------------------------------------------
                        // SUCCESS MESSAGE
                        // ---------------------------------------------

                        Toast.makeText(
                                FamilyInvitationsActivity.this,
                                "You joined "
                                        + familyName
                                        + " successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        // ---------------------------------------------
                        // OPEN FAMILY DASHBOARD
                        //
                        // IMPORTANT:
                        // Send LOCAL SQLite ID, NOT Firestore ID.
                        // ---------------------------------------------

                        Intent intent =
                                new Intent(
                                        FamilyInvitationsActivity.this,
                                        FamilyDashboardActivity.class
                                );

                        intent.putExtra(
                                "FAMILY_ID",
                                (int) localFamilyId
                        );

                        intent.putExtra(
                                "FAMILY_NAME",
                                familyName
                        );

                        startActivity(intent);

                        finish();
                    }

                    // ---------------------------------------------
                    // ALREADY ACCEPTED
                    // ---------------------------------------------

                    @Override
                    public void onAlreadyAccepted() {

                        showLoading(false);

                        Toast.makeText(
                                FamilyInvitationsActivity.this,
                                "This invitation has already been processed.",
                                Toast.LENGTH_LONG
                        ).show();

                        loadInvitations();
                    }

                    // ---------------------------------------------
                    // FAILURE
                    // ---------------------------------------------

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        showLoading(false);

                        Toast.makeText(
                                FamilyInvitationsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // REJECT INVITATION
    // =====================================================

    private void rejectInvitation(
            FamilyInvitation invitation
    ) {

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String invitationId =
                invitation.getInvitationId();

        if (invitationId == null ||
                invitationId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid invitation.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        showLoading(true);

        familyFirestoreService.rejectInvitation(
                invitationId,
                currentUser.getUid(),

                new FamilyFirestoreService
                        .RejectInvitationCallback() {

                    @Override
                    public void onSuccess() {

                        showLoading(false);

                        Toast.makeText(
                                FamilyInvitationsActivity.this,
                                "Invitation rejected.",
                                Toast.LENGTH_SHORT
                        ).show();

                        invitationList.remove(
                                invitation
                        );

                        adapter.notifyDataSetChanged();

                        updateEmptyState();
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        showLoading(false);

                        Toast.makeText(
                                FamilyInvitationsActivity.this,
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

        android.database.Cursor cursor =
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
    // EMPTY STATE
    // =====================================================

    private void updateEmptyState() {

        boolean empty =
                invitationList.isEmpty();

        if (empty) {

            showEmptyState();

        } else {

            recyclerInvitations.setVisibility(
                    View.VISIBLE
            );

            tvEmptyInvitations.setVisibility(
                    View.GONE
            );
        }
    }

    // =====================================================
    // SHOW EMPTY STATE
    // =====================================================

    private void showEmptyState() {

        recyclerInvitations.setVisibility(
                View.GONE
        );

        tvEmptyInvitations.setVisibility(
                View.VISIBLE
        );
    }

    // =====================================================
    // LOADING
    // =====================================================

    private void showLoading(
            boolean show
    ) {

        if (progressIndicator != null) {

            progressIndicator.setVisibility(
                    show
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        if (show) {

            recyclerInvitations.setVisibility(
                    View.GONE
            );

            tvEmptyInvitations.setVisibility(
                    View.GONE
            );
        }
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