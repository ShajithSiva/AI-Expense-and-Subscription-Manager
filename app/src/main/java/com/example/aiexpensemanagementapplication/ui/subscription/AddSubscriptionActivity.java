package com.example.aiexpensemanagementapplication.ui.subscription;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddSubscriptionActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private MaterialToolbar toolbar;

    private TextInputEditText etServiceName;
    private TextInputEditText etAmount;
    private TextInputEditText etNextBillingDate;

    private AutoCompleteTextView actCurrency;
    private AutoCompleteTextView actBillingCycle;

    private MaterialButton btnSave;
    private MaterialButton btnCancel;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // USER
    // =========================================================

    private int userId = -1;


    // =========================================================
    // FAMILY SHARING
    // =========================================================

    private MaterialSwitch switchShareFamily;
    private LinearLayout layoutFamilySelection;
    private Spinner spFamily;

    private FamilyFirestoreService familyFirestoreService;

    private int selectedFamilyId = -1;

    private ArrayList<Integer> familyIds =
            new ArrayList<>();

    private ArrayList<String> familyNames =
            new ArrayList<>();


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_subscription
        );


        initializeViews();

        setupToolbar();


        databaseHelper =
                new DatabaseHelper(this);


        familyFirestoreService =
                new FamilyFirestoreService();


        initializeUser();

        loadUserFamilies();

        setupCurrency();

        setupBillingCycle();

        setupDatePicker();

        setupListeners();

        setupFamilySharing();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );


        etServiceName =
                findViewById(
                        R.id.etServiceName
                );


        etAmount =
                findViewById(
                        R.id.etAmount
                );


        etNextBillingDate =
                findViewById(
                        R.id.etNextBillingDate
                );


        actCurrency =
                findViewById(
                        R.id.actCurrency
                );


        actBillingCycle =
                findViewById(
                        R.id.actBillingCycle
                );


        btnSave =
                findViewById(
                        R.id.btnSave
                );


        btnCancel =
                findViewById(
                        R.id.btnCancel
                );


        switchShareFamily =
                findViewById(
                        R.id.switchShareFamily
                );


        layoutFamilySelection =
                findViewById(
                        R.id.layoutFamilySelection
                );


        spFamily =
                findViewById(
                        R.id.spFamily
                );
    }


    // =========================================================
    // TOOLBAR
    // =========================================================

    private void setupToolbar() {

        setSupportActionBar(
                toolbar
        );


        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // INITIALIZE USER
    // =========================================================

    private void initializeUser() {

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

            finish();

            return;
        }


        userId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                firebaseUser.getUid()
                        );


        if (userId <= 0) {

            Toast.makeText(
                    this,
                    "Local user record not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }


    // =========================================================
    // CURRENCY DROPDOWN
    // =========================================================

    private void setupCurrency() {

        /*
         * Currency must be selected by the user.
         *
         * IMPORTANT:
         * Do not automatically default unknown currencies
         * to LKR.
         */

        String[] currencies = {

                "LKR",
                "USD",
                "EUR",
                "GBP",
                "AUD",
                "CAD",
                "SGD",
                "INR",
                "JPY"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        currencies
                );


        actCurrency.setAdapter(
                adapter
        );


        // User must explicitly select currency.
        actCurrency.setText(
                "",
                false
        );
    }


    // =========================================================
    // BILLING CYCLE DROPDOWN
    // =========================================================

    private void setupBillingCycle() {

        String[] cycles = {

                "Monthly",
                "Yearly",
                "Quarterly",
                "Weekly"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        cycles
                );


        actBillingCycle.setAdapter(
                adapter
        );
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void setupDatePicker() {

        etNextBillingDate.setOnClickListener(
                v -> {

                    Calendar calendar =
                            Calendar.getInstance();


                    DatePickerDialog picker =
                            new DatePickerDialog(

                                    this,

                                    (view,
                                     year,
                                     month,
                                     day) -> {

                                        String date =
                                                String.format(
                                                        Locale.US,
                                                        "%04d-%02d-%02d",
                                                        year,
                                                        month + 1,
                                                        day
                                                );


                                        etNextBillingDate.setText(
                                                date
                                        );
                                    },

                                    calendar.get(
                                            Calendar.YEAR
                                    ),

                                    calendar.get(
                                            Calendar.MONTH
                                    ),

                                    calendar.get(
                                            Calendar.DAY_OF_MONTH
                                    )
                            );


                    picker.show();
                }
        );
    }


    // =========================================================
    // BUTTON LISTENERS
    // =========================================================

    private void setupListeners() {

        btnSave.setOnClickListener(
                v -> saveSubscription()
        );


        btnCancel.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // VALIDATE INPUTS
    // =========================================================

    private boolean validateInputs() {

        String serviceName =
                getText(
                        etServiceName
                );


        String amountText =
                getText(
                        etAmount
                );


        String currency =
                safeString(
                        actCurrency
                                .getText()
                                .toString()
                );


        String billingCycle =
                safeString(
                        actBillingCycle
                                .getText()
                                .toString()
                );


        String nextBillingDate =
                getText(
                        etNextBillingDate
                );


        // =====================================================
        // SERVICE
        // =====================================================

        if (serviceName.isEmpty()) {

            etServiceName.setError(
                    "Service name is required"
            );

            etServiceName.requestFocus();

            return false;
        }


        // =====================================================
        // AMOUNT
        // =====================================================

        if (amountText.isEmpty()) {

            etAmount.setError(
                    "Amount is required"
            );

            etAmount.requestFocus();

            return false;
        }


        try {

            double amount =
                    Double.parseDouble(
                            amountText
                    );


            if (amount <= 0) {

                etAmount.setError(
                        "Amount must be greater than 0"
                );

                etAmount.requestFocus();

                return false;
            }


        } catch (NumberFormatException e) {

            etAmount.setError(
                    "Enter a valid amount"
            );

            etAmount.requestFocus();

            return false;
        }


        // =====================================================
        // CURRENCY
        // =====================================================

        if (currency.isEmpty()) {

            actCurrency.setError(
                    "Please select currency"
            );

            actCurrency.requestFocus();

            return false;
        }


        // =====================================================
        // BILLING CYCLE
        // =====================================================

        if (billingCycle.isEmpty()) {

            actBillingCycle.setError(
                    "Please select billing cycle"
            );

            actBillingCycle.requestFocus();

            return false;
        }


        // =====================================================
        // NEXT BILLING DATE
        // =====================================================

        if (nextBillingDate.isEmpty()) {

            etNextBillingDate.setError(
                    "Next billing date is required"
            );

            etNextBillingDate.requestFocus();

            return false;
        }


        return true;
    }


    // =========================================================
    // SAVE SUBSCRIPTION
    // =========================================================

    private void saveSubscription() {

        if (!validateInputs()) {

            return;
        }


        // =====================================================
        // CURRENT FIREBASE USER
        // =====================================================

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


        if (userId <= 0) {

            Toast.makeText(
                    this,
                    "Local user record not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // GET INPUT VALUES
        // =====================================================

        String serviceName =
                getText(
                        etServiceName
                );


        double amount;


        try {

            amount =
                    Double.parseDouble(
                            getText(
                                    etAmount
                            )
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


        // =====================================================
        // ORIGINAL CURRENCY
        // =====================================================

        String currency =
                normalizeCurrency(
                        actCurrency
                                .getText()
                                .toString()
                );


        if (currency.isEmpty()) {

            actCurrency.setError(
                    "Please select currency"
            );

            return;
        }


        String billingCycle =
                safeString(
                        actBillingCycle
                                .getText()
                                .toString()
                );


        String nextBillingDate =
                getText(
                        etNextBillingDate
                );


        // =====================================================
        // FAMILY VALIDATION
        // =====================================================

        if (switchShareFamily.isChecked() &&
                selectedFamilyId == -1) {

            Toast.makeText(
                    this,
                    "Please select a family.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // DUPLICATE CHECK
        // =====================================================

        boolean alreadyExists =
                databaseHelper
                        .subscriptionExists(
                                userId,
                                serviceName
                        );


        if (alreadyExists) {

            Toast.makeText(
                    this,
                    "This subscription already exists.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // SAVE TO SQLITE
        //
        // Currency is now stored with original amount.
        // =====================================================

        long result =
                databaseHelper
                        .insertSubscription(

                                userId,

                                serviceName,

                                amount,

                                currency,

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


        // =====================================================
        // DEBUG LOG
        // =====================================================

        System.out.println(
                "========================================"
        );

        System.out.println(
                "MANUAL SUBSCRIPTION SAVED"
        );

        System.out.println(
                "ID: " + result
        );

        System.out.println(
                "SERVICE: " + serviceName
        );

        System.out.println(
                "AMOUNT: "
                        + currency
                        + " "
                        + amount
        );

        System.out.println(
                "CYCLE: "
                        + billingCycle
        );

        System.out.println(
                "NEXT BILLING: "
                        + nextBillingDate
        );

        System.out.println(
                "========================================"
        );


        // =====================================================
        // PERSONAL SUBSCRIPTION
        // =====================================================

        if (!switchShareFamily.isChecked()) {

            Toast.makeText(
                    this,
                    "Subscription added successfully.",
                    Toast.LENGTH_SHORT
            ).show();


            finish();

            return;
        }


        // =====================================================
        // GET FIRESTORE FAMILY ID
        // =====================================================

        String firestoreFamilyId =
                databaseHelper
                        .getFirestoreFamilyId(
                                selectedFamilyId
                        );


        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Subscription saved locally, but family Firestore ID was not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // OWNER NAME
        // =====================================================

        String ownerName =
                firebaseUser.getDisplayName();


        if (ownerName == null ||
                ownerName.trim().isEmpty()) {

            ownerName =
                    "Family Member";
        }


        // =====================================================
        // DISABLE SAVE BUTTON
        // =====================================================

        btnSave.setEnabled(
                false
        );


        btnSave.setText(
                "Saving..."
        );


        // =====================================================
        // FAMILY FIRESTORE
        //
        // Existing method signature is kept unchanged here
        // because FamilyFirestoreService currently receives:
        //
        // serviceName,
        // amount,
        // billingCycle,
        // nextBillingDate
        //
        // Currency support can be added to family Firestore
        // separately.
        // =====================================================

        familyFirestoreService
                .addFamilySubscription(

                        firestoreFamilyId,

                        String.valueOf(
                                result
                        ),

                        firebaseUser.getUid(),

                        ownerName,

                        serviceName,

                        amount,

                        billingCycle,

                        nextBillingDate,

                        new FamilyFirestoreService
                                .FamilySubscriptionCallback() {

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

                                btnSave.setEnabled(
                                        true
                                );


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


    // =========================================================
    // FAMILY SHARING
    // =========================================================

    private void setupFamilySharing() {

        switchShareFamily
                .setOnCheckedChangeListener(

                        (buttonView, isChecked) -> {

                            if (isChecked) {

                                if (familyIds == null ||
                                        familyIds.isEmpty()) {

                                    switchShareFamily
                                            .setChecked(
                                                    false
                                            );


                                    Toast.makeText(
                                            this,
                                            "You are not a member of any family group.",
                                            Toast.LENGTH_LONG
                                    ).show();


                                    return;
                                }


                                layoutFamilySelection
                                        .setVisibility(
                                                View.VISIBLE
                                        );


                            } else {

                                layoutFamilySelection
                                        .setVisibility(
                                                View.GONE
                                        );


                                selectedFamilyId =
                                        -1;
                            }
                        }
                );
    }


    // =========================================================
    // LOAD USER FAMILIES
    // =========================================================

    private void loadUserFamilies() {

        FirebaseUser currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (currentUser == null) {

            switchShareFamily
                    .setEnabled(
                            false
                    );

            return;
        }


        int localUserId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                currentUser.getUid()
                        );


        if (localUserId <= 0) {

            switchShareFamily
                    .setEnabled(
                            false
                    );

            return;
        }


        // =====================================================
        // FAMILY IDS
        // =====================================================

        familyIds =
                databaseHelper
                        .getFamilyIdsForUser(
                                localUserId
                        );


        // =====================================================
        // FAMILY NAMES
        // =====================================================

        familyNames =
                databaseHelper
                        .getFamilyNamesForUser(
                                localUserId
                        );


        // =====================================================
        // SAFETY
        // =====================================================

        if (familyIds == null ||
                familyNames == null ||
                familyIds.size() !=
                        familyNames.size()) {


            familyIds =
                    new ArrayList<>();


            familyNames =
                    new ArrayList<>();


            switchShareFamily
                    .setChecked(
                            false
                    );


            switchShareFamily
                    .setEnabled(
                            false
                    );


            layoutFamilySelection
                    .setVisibility(
                            View.GONE
                    );


            return;
        }


        // =====================================================
        // NO FAMILY
        // =====================================================

        if (familyIds.isEmpty()) {

            switchShareFamily
                    .setChecked(
                            false
                    );


            switchShareFamily
                    .setEnabled(
                            false
                    );


            layoutFamilySelection
                    .setVisibility(
                            View.GONE
                    );


            return;
        }


        // =====================================================
        // FAMILY AVAILABLE
        // =====================================================

        switchShareFamily
                .setEnabled(
                        true
                );


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        familyNames
                );


        adapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
        );


        spFamily.setAdapter(
                adapter
        );


        // =====================================================
        // FAMILY SELECTION
        // =====================================================

        spFamily.setOnItemSelectedListener(

                new android.widget.AdapterView
                        .OnItemSelectedListener() {


                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        if (position >= 0 &&
                                position <
                                        familyIds.size()) {

                            selectedFamilyId =
                                    familyIds.get(
                                            position
                                    );
                        }
                    }


                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {

                        selectedFamilyId =
                                -1;
                    }
                }
        );
    }


    // =========================================================
    // NORMALIZE CURRENCY
    // =========================================================

    private String normalizeCurrency(
            String value
    ) {

        String currency =
                safeString(
                        value
                );


        if (currency.isEmpty()) {

            return "";
        }


        String upper =
                currency.toUpperCase(
                        Locale.US
                );


        if (upper.equals("RS") ||
                upper.equals("RS.")) {

            return "LKR";
        }


        if (currency.equals("€")) {

            return "EUR";
        }


        if (currency.equals("£")) {

            return "GBP";
        }


        if (currency.equals("₹")) {

            return "INR";
        }


        /*
         * Never automatically turn "$" into USD.
         * Manual dropdown uses ISO codes,
         * so normally "$" should not occur here.
         */

        return upper;
    }


    // =========================================================
    // GET TEXT
    // =========================================================

    private String getText(
            TextInputEditText editText
    ) {

        if (editText == null ||
                editText.getText() == null) {

            return "";
        }


        return editText
                .getText()
                .toString()
                .trim();
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safeString(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value.trim();
    }
}