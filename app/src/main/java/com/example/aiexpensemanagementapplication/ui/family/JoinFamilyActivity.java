package com.example.aiexpensemanagementapplication.ui.family;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class JoinFamilyActivity extends AppCompatActivity {

    // =====================================================
    // UI
    // =====================================================

    private ImageButton btnBack;

    private TextInputEditText etInviteCode;
    private MaterialButton btnFindFamily;

    private MaterialButton btnFamilyInvitations;

    // =====================================================
    // ACTIVITY
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_family);

        initializeViews();
        setupInitialState();
        setupListeners();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        btnBack =
                findViewById(R.id.btnBack);

        etInviteCode =
                findViewById(R.id.etInviteCode);

        btnFindFamily =
                findViewById(R.id.btnFindFamily);

        btnFamilyInvitations =
                findViewById(R.id.btnFamilyInvitations);
    }

    // =====================================================
    // INITIAL STATE
    // =====================================================

    private void setupInitialState() {

        /*
         * Family joining is handled through
         * email invitations.
         *
         * Therefore manual invitation-code
         * joining is currently hidden.
         */

        if (etInviteCode != null) {

            etInviteCode.setVisibility(
                    View.GONE
            );
        }

        if (btnFindFamily != null) {

            btnFindFamily.setVisibility(
                    View.GONE
            );
        }
    }

    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        // -------------------------------------------------
        // BACK
        // -------------------------------------------------

        btnBack.setOnClickListener(v ->
                finish()
        );


        // -------------------------------------------------
        // VIEW FAMILY INVITATIONS
        // -------------------------------------------------

        btnFamilyInvitations.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            JoinFamilyActivity.this,
                            FamilyInvitationsActivity.class
                    );

            startActivity(intent);
        });
    }
}