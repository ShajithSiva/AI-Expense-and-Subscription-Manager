package com.example.aiexpensemanagementapplication.ui.family;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.model.FamilyInvitation;
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

    private static final String TAG =
            "FAMILY_INVITATION";

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

    private FamilyFirestoreService familyFirestoreService;

    private FirebaseFirestore firestore;

    private FirebaseUser currentUser;

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

        familyFirestoreService =
                new FamilyFirestoreService();

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        loadInvitations();
    }

    // =====================================================
    // FIREBASE
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
    // VIEWS
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

        // -------------------------------------------------
        // CHECK LOGIN
        // -------------------------------------------------

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

        // -------------------------------------------------
        // GET EMAIL
        // -------------------------------------------------

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

        // -------------------------------------------------
        // START LOADING
        // -------------------------------------------------

        showLoading(true);

        // -------------------------------------------------
        // IMPORTANT
        //
        // Query ONLY by invitedEmail.
        //
        // We check status manually below.
        // -------------------------------------------------

        firestore
                .collection("familyInvitations")
                .whereEqualTo(
                        "invitedEmail",
                        email
                )
                .get()

                // =================================================
                // SUCCESS
                // =================================================

                .addOnSuccessListener(
                        querySnapshot -> {

                            invitationList.clear();

                            Log.d(
                                    TAG,
                                    "Firestore documents found = "
                                            + querySnapshot.size()
                            );

                            // -----------------------------------------
                            // LOOP DOCUMENTS
                            // -----------------------------------------

                            for (
                                    QueryDocumentSnapshot document
                                    : querySnapshot
                            ) {

                                try {

                                    Log.d(
                                            TAG,
                                            "--------------------------------"
                                    );

                                    Log.d(
                                            TAG,
                                            "Document ID = "
                                                    + document.getId()
                                    );

                                    // ---------------------------------
                                    // READ RAW STATUS
                                    // ---------------------------------

                                    String status =
                                            document.getString(
                                                    "status"
                                            );

                                    Log.d(
                                            TAG,
                                            "Firestore status = "
                                                    + status
                                    );

                                    // ---------------------------------
                                    // ONLY PENDING
                                    // ---------------------------------

                                    if (status == null ||
                                            !status
                                                    .trim()
                                                    .equalsIgnoreCase(
                                                            "pending"
                                                    )) {

                                        Log.d(
                                                TAG,
                                                "Skipping document because "
                                                        + "status is not pending."
                                        );

                                        continue;
                                    }

                                    // ---------------------------------
                                    // CONVERT TO MODEL
                                    // ---------------------------------

                                    FamilyInvitation invitation =
                                            document.toObject(
                                                    FamilyInvitation.class
                                            );

                                    if (invitation == null) {

                                        continue;
                                    }

                                    // ---------------------------------
                                    // SET DOCUMENT ID
                                    // ---------------------------------

                                    invitation.setInvitationId(
                                            document.getId()
                                    );

                                    // ---------------------------------
                                    // ADD TO LIST
                                    // ---------------------------------

                                    invitationList.add(
                                            invitation
                                    );

                                    // ---------------------------------
                                    // DEBUG
                                    // ---------------------------------

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
                                            "Invited Email = "
                                                    + invitation.getInvitedEmail()
                                    );

                                    Log.d(
                                            TAG,
                                            "Invited By = "
                                                    + invitation.getInvitedBy()
                                    );

                                    Log.d(
                                            TAG,
                                            "Role = "
                                                    + invitation.getRole()
                                    );

                                    Log.d(
                                            TAG,
                                            "Status = "
                                                    + invitation.getStatus()
                                    );

                                } catch (Exception e) {

                                    Log.e(
                                            TAG,
                                            "Error reading invitation document",
                                            e
                                    );
                                }
                            }

                            // -----------------------------------------
                            // UPDATE RECYCLER VIEW
                            // -----------------------------------------

                            adapter.notifyDataSetChanged();

                            // -----------------------------------------
                            // STOP LOADING
                            // -----------------------------------------

                            showLoading(false);

                            // -----------------------------------------
                            // EMPTY STATE
                            // -----------------------------------------

                            updateEmptyState();

                            // -----------------------------------------
                            // RESULT
                            // -----------------------------------------

                            Log.d(
                                    TAG,
                                    "Pending invitations = "
                                            + invitationList.size()
                            );

                            Toast.makeText(
                                    this,
                                    "Found "
                                            + invitationList.size()
                                            + " invitation(s)",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )

                // =================================================
                // FAILURE
                // =================================================

                .addOnFailureListener(
                        e -> {

                            showLoading(false);

                            Log.e(
                                    TAG,
                                    "Failed to load invitations",
                                    e
                            );

                            Toast.makeText(
                                    this,
                                    "Failed to load invitations:\n"
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =====================================================
    // EMPTY STATE
    // =====================================================

    private void updateEmptyState() {

        if (invitationList.isEmpty()) {

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
    // ACCEPT
    // =====================================================

    private void acceptInvitation(
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

        String uid =
                currentUser.getUid();

        String userName =
                currentUser.getDisplayName();

        if (userName == null) {
            userName = "";
        }

        showLoading(true);

        familyFirestoreService.acceptInvitation(
                invitation.getInvitationId(),
                uid,
                userName,
                new FamilyFirestoreService.AcceptInvitationCallback() {

                    @Override
                    public void onSuccess(
                            String familyId,
                            String familyName,
                            String role
                    ) {

                        showLoading(false);

                        Toast.makeText(
                                FamilyInvitationsActivity.this,
                                "You joined "
                                        + familyName
                                        + " successfully!",
                                Toast.LENGTH_LONG
                        ).show();

                        invitationList.remove(
                                invitation
                        );

                        adapter.notifyDataSetChanged();

                        updateEmptyState();
                    }

                    @Override
                    public void onAlreadyAccepted() {

                        showLoading(false);

                        Toast.makeText(
                                FamilyInvitationsActivity.this,
                                "This invitation has already been accepted.",
                                Toast.LENGTH_LONG
                        ).show();

                        loadInvitations();
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
    // REJECT
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

        showLoading(true);

        familyFirestoreService.rejectInvitation(
                invitation.getInvitationId(),
                currentUser.getUid(),
                new FamilyFirestoreService.RejectInvitationCallback() {

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
}