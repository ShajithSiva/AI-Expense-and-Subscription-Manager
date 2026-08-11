package com.example.aiexpensemanagementapplication.ui.subscription;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.view.View;
import android.widget.Spinner;

import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;

import java.util.ArrayList;

import android.widget.LinearLayout;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Calendar;

public class AddSubscriptionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    private TextInputEditText etServiceName;
    private TextInputEditText etAmount;
    private TextInputEditText etNextBillingDate;

    private AutoCompleteTextView actBillingCycle;

    private MaterialButton btnSave;
    private MaterialButton btnCancel;

    private DatabaseHelper databaseHelper;

    private int userId;

    private MaterialSwitch switchShareFamily;
    private LinearLayout layoutFamilySelection;
    private Spinner spFamily;

    private FamilyFirestoreService familyFirestoreService;

    private int selectedFamilyId = -1;

    private ArrayList<Integer> familyIds =
            new ArrayList<>();
    private ArrayList<String> familyNames =
            new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subscription);

        initializeViews();

        setupToolbar();

        databaseHelper =
                new DatabaseHelper(this);

        familyFirestoreService =
                new FamilyFirestoreService();


        loadUserFamilies();

        initializeUser();

        setupBillingCycle();

        setupDatePicker();

        setupListeners();

        setupFamilySharing();


    }

    private void initializeViews(){

        toolbar=findViewById(R.id.toolbar);

        etServiceName=findViewById(R.id.etServiceName);

        etAmount=findViewById(R.id.etAmount);

        etNextBillingDate=findViewById(R.id.etNextBillingDate);

        actBillingCycle=findViewById(R.id.actBillingCycle);

        btnSave=findViewById(R.id.btnSave);

        btnCancel=findViewById(R.id.btnCancel);

        switchShareFamily =
                findViewById(R.id.switchShareFamily);

        layoutFamilySelection =
                findViewById(R.id.layoutFamilySelection);

        spFamily =
                findViewById(R.id.spFamily);

    }


    private void setupFamilySharing() {

        switchShareFamily.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        if (familyIds.isEmpty()) {

                            switchShareFamily.setChecked(false);

                            Toast.makeText(
                                    this,
                                    "You are not a member of any family group.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        layoutFamilySelection.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        layoutFamilySelection.setVisibility(
                                View.GONE
                        );

                        selectedFamilyId = -1;
                    }
                }
        );
    }

    // =====================================================
    // LOAD USER FAMILIES
    // =====================================================

    private void loadUserFamilies() {

        FirebaseUser currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (currentUser == null) {

            switchShareFamily.setEnabled(false);

            return;
        }

        int localUserId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );

        if (localUserId == -1) {

            switchShareFamily.setEnabled(false);

            return;
        }

        // -------------------------------------------------
        // GET FAMILY IDs
        // -------------------------------------------------

        familyIds =
                databaseHelper.getFamilyIdsForUser(
                        localUserId
                );

        // -------------------------------------------------
        // GET FAMILY NAMES
        // -------------------------------------------------

        familyNames =
                databaseHelper.getFamilyNamesForUser(
                        localUserId
                );

        // -------------------------------------------------
        // SAFETY CHECK
        // -------------------------------------------------

        if (familyIds == null ||
                familyNames == null ||
                familyIds.size() != familyNames.size()) {

            familyIds.clear();
            familyNames.clear();

            switchShareFamily.setEnabled(false);

            return;
        }

        // -------------------------------------------------
        // NO FAMILY
        // -------------------------------------------------

        if (familyIds.isEmpty()) {

            switchShareFamily.setChecked(false);

            switchShareFamily.setEnabled(false);

            layoutFamilySelection.setVisibility(
                    View.GONE
            );

            return;
        }

        // -------------------------------------------------
        // FAMILY AVAILABLE
        // -------------------------------------------------

        switchShareFamily.setEnabled(true);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        familyNames
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spFamily.setAdapter(adapter);

        // -------------------------------------------------
        // FAMILY SELECTION
        // -------------------------------------------------

        spFamily.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        if (position >= 0 &&
                                position < familyIds.size()) {

                            selectedFamilyId =
                                    familyIds.get(position);
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {

                        selectedFamilyId = -1;
                    }
                }
        );
    }

    private void setupToolbar(){

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v->finish());

    }

    private void initializeUser(){

        FirebaseUser firebaseUser=
                FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser==null){

            finish();

            return;

        }

        userId=databaseHelper.getUserIdByFirebaseUid(
                firebaseUser.getUid());

    }

    private void setupBillingCycle(){

        String[] cycles={
                "Monthly",
                "Yearly",
                "Weekly"
        };

        ArrayAdapter<String> adapter=
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        cycles
                );

        actBillingCycle.setAdapter(adapter);

    }

    private void setupDatePicker(){

        etNextBillingDate.setOnClickListener(v->{

            Calendar calendar=Calendar.getInstance();

            DatePickerDialog picker=new DatePickerDialog(

                    this,

                    (view,year,month,day)->{

                        String date=

                                year+"-"+

                                        String.format("%02d",month+1)+"-"+

                                        String.format("%02d",day);

                        etNextBillingDate.setText(date);

                    },

                    calendar.get(Calendar.YEAR),

                    calendar.get(Calendar.MONTH),

                    calendar.get(Calendar.DAY_OF_MONTH)

            );

            picker.show();

        });

    }

    private void setupListeners(){

        btnSave.setOnClickListener(v->saveSubscription());

        btnCancel.setOnClickListener(v->finish());

    }

    private boolean validateInputs(){

        if(etServiceName.getText().toString().trim().isEmpty()){

            etServiceName.setError("Required");

            return false;

        }

        if(etAmount.getText().toString().trim().isEmpty()){

            etAmount.setError("Required");

            return false;

        }

        if(actBillingCycle.getText().toString().trim().isEmpty()){

            actBillingCycle.setError("Required");

            return false;

        }

        if(etNextBillingDate.getText().toString().trim().isEmpty()){

            etNextBillingDate.setError("Required");

            return false;

        }

        return true;

    }

    // =====================================================
// SAVE SUBSCRIPTION
// =====================================================

    private void saveSubscription() {

        if (!validateInputs()) {
            return;
        }

        // -------------------------------------------------
        // GET CURRENT FIREBASE USER
        // -------------------------------------------------

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

        // -------------------------------------------------
        // GET INPUT VALUES
        // -------------------------------------------------

        String serviceName =
                etServiceName
                        .getText()
                        .toString()
                        .trim();

        double amount;

        try {

            amount =
                    Double.parseDouble(
                            etAmount
                                    .getText()
                                    .toString()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            etAmount.setError(
                    "Enter a valid amount"
            );

            return;
        }

        if (amount <= 0) {

            etAmount.setError(
                    "Amount must be greater than 0"
            );

            return;
        }

        String billingCycle =
                actBillingCycle
                        .getText()
                        .toString()
                        .trim();

        String nextBillingDate =
                etNextBillingDate
                        .getText()
                        .toString()
                        .trim();

        // -------------------------------------------------
        // FAMILY VALIDATION
        // -------------------------------------------------

        if (switchShareFamily.isChecked() &&
                selectedFamilyId == -1) {

            Toast.makeText(
                    this,
                    "Please select a family.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // SAVE TO SQLITE
        // -------------------------------------------------

        long result =
                databaseHelper.insertSubscription(

                        userId,

                        serviceName,

                        amount,

                        billingCycle,

                        nextBillingDate
                );

        if (result == -1) {

            Toast.makeText(
                    this,
                    "Failed to save subscription.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // -------------------------------------------------
        // PERSONAL SUBSCRIPTION
        // -------------------------------------------------

        if (!switchShareFamily.isChecked()) {

            Toast.makeText(
                    this,
                    "Subscription added successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        // -------------------------------------------------
        // GET FIRESTORE FAMILY ID
        // -------------------------------------------------

        String firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        selectedFamilyId
                );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Subscription saved, but family Firestore ID was not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // -------------------------------------------------
        // GET OWNER NAME
        // -------------------------------------------------

        String ownerName =
                firebaseUser.getDisplayName();

        if (ownerName == null ||
                ownerName.trim().isEmpty()) {

            ownerName = "Family Member";
        }

        // -------------------------------------------------
        // DISABLE BUTTON
        // -------------------------------------------------

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // -------------------------------------------------
        // SAVE FAMILY SUBSCRIPTION TO FIRESTORE
        // -------------------------------------------------

        familyFirestoreService.addFamilySubscription(

                firestoreFamilyId,

                String.valueOf(result),

                firebaseUser.getUid(),

                ownerName,

                serviceName,

                amount,

                billingCycle,

                nextBillingDate,

                new FamilyFirestoreService.FamilySubscriptionCallback() {

                    @Override
                    public void onSuccess() {

                        Toast.makeText(
                                AddSubscriptionActivity.this,
                                "Family subscription added successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        btnSave.setEnabled(true);
                        btnSave.setText(
                                "Save Subscription"
                        );

                        Toast.makeText(
                                AddSubscriptionActivity.this,
                                "Subscription saved locally, but family sync failed: "
                                        + message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }




}