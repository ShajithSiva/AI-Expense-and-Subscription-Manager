package com.example.aiexpensemanagementapplication.ui.expense;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.ui.ai.AIInsightCacheManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EditExpenseActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private EditText etAmount;
    private EditText etDate;
    private EditText etNotes;

    private Spinner spCategory;
    private Spinner spPaymentMethod;
    private Spinner spFamily;

    private MaterialSwitch switchShareFamily;
    private LinearLayout layoutFamilySelection;

    private MaterialButton btnUpdateExpense;
    private ImageButton btnBack;


    // =========================================================
    // DATABASE / AUTH
    // =========================================================

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    // =========================================================
    // DATA
    // =========================================================

    private Calendar calendar;

    private int transactionId;

    private ArrayList<Integer> familyIds;
    private ArrayList<String> familyNames;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_expense);

        initialize();

        // Validate transaction ID
        if (transactionId == -1) {

            Toast.makeText(
                    this,
                    "Invalid expense.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        loadCategories();

        loadPaymentMethods();

        loadFamilies();

        loadExpense();

        listeners();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initialize() {

        databaseHelper =
                new DatabaseHelper(this);


        transactionId =
                getIntent().getIntExtra(
                        "transactionId",
                        -1
                );


        mAuth =
                FirebaseAuth.getInstance();

        currentUser =
                mAuth.getCurrentUser();


        calendar =
                Calendar.getInstance();


        // -----------------------------------------------------
        // Fields
        // -----------------------------------------------------

        etAmount =
                findViewById(R.id.etAmount);

        etDate =
                findViewById(R.id.etDate);

        etNotes =
                findViewById(R.id.etNotes);


        // -----------------------------------------------------
        // Spinners
        // -----------------------------------------------------

        spCategory =
                findViewById(R.id.spCategory);

        spPaymentMethod =
                findViewById(R.id.spPaymentMethod);

        spFamily =
                findViewById(R.id.spFamily);


        // -----------------------------------------------------
        // Family sharing
        // -----------------------------------------------------

        switchShareFamily =
                findViewById(R.id.switchShareFamily);

        layoutFamilySelection =
                findViewById(R.id.layoutFamilySelection);


        // -----------------------------------------------------
        // Buttons
        // -----------------------------------------------------

        btnUpdateExpense =
                findViewById(R.id.btnUpdateExpense);

        btnBack =
                findViewById(R.id.btnBack);


        // -----------------------------------------------------
        // Family lists
        // -----------------------------------------------------

        familyIds =
                new ArrayList<>();

        familyNames =
                new ArrayList<>();
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void listeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );


        etDate.setOnClickListener(
                v -> showDatePicker()
        );


        // -----------------------------------------------------
        // FAMILY SHARE SWITCH
        // -----------------------------------------------------

        switchShareFamily.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        if (familyIds.isEmpty()) {

                            switchShareFamily.setChecked(false);

                            layoutFamilySelection.setVisibility(
                                    View.GONE
                            );

                            Toast.makeText(
                                    EditExpenseActivity.this,
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
                    }
                }
        );


        btnUpdateExpense.setOnClickListener(
                v -> updateExpense()
        );
    }


    // =========================================================
    // DATE
    // =========================================================

    private void setCurrentDate() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );

        etDate.setText(
                format.format(
                        calendar.getTime()
                )
        );
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void showDatePicker() {

        DatePickerDialog dialog =
                new DatePickerDialog(

                        this,

                        (view, year, month, dayOfMonth) -> {

                            calendar.set(
                                    year,
                                    month,
                                    dayOfMonth
                            );

                            setCurrentDate();
                        },

                        calendar.get(Calendar.YEAR),

                        calendar.get(Calendar.MONTH),

                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }


    // =========================================================
    // LOAD CATEGORIES
    // =========================================================

    private void loadCategories() {

        ArrayList<String> categories =
                databaseHelper.getExpenseCategoryNames();


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spCategory.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LOAD PAYMENT METHODS
    // =========================================================

    private void loadPaymentMethods() {

        ArrayList<String> methods =
                databaseHelper.getPaymentMethodNames();


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        methods
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spPaymentMethod.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LOAD FAMILIES
    // =========================================================

    private void loadFamilies() {

        if (currentUser == null) {

            switchShareFamily.setEnabled(false);

            return;
        }


        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );


        if (userId == -1) {

            switchShareFamily.setEnabled(false);

            return;
        }


        familyIds =
                databaseHelper.getFamilyIdsForUser(
                        userId
                );


        familyNames =
                databaseHelper.getFamilyNamesForUser(
                        userId
                );


        // IDs and names must match
        if (familyIds.size() != familyNames.size()) {

            familyIds.clear();

            familyNames.clear();

            switchShareFamily.setEnabled(false);

            layoutFamilySelection.setVisibility(
                    View.GONE
            );

            return;
        }


        if (familyIds.isEmpty()) {

            switchShareFamily.setChecked(false);

            switchShareFamily.setEnabled(false);

            layoutFamilySelection.setVisibility(
                    View.GONE
            );

            return;
        }


        ArrayAdapter<String> familyAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        familyNames
                );


        familyAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spFamily.setAdapter(
                familyAdapter
        );


        switchShareFamily.setEnabled(true);
    }


    // =========================================================
    // LOAD EXISTING EXPENSE
    // =========================================================

    private void loadExpense() {

        Cursor cursor =
                databaseHelper.getExpenseById(
                        transactionId
                );


        if (cursor == null) {

            Toast.makeText(
                    this,
                    "Unable to load expense.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        try {

            if (!cursor.moveToFirst()) {

                Toast.makeText(
                        this,
                        "Expense not found.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

                return;
            }


            // -------------------------------------------------
            // Amount
            // -------------------------------------------------

            etAmount.setText(
                    String.valueOf(
                            cursor.getDouble(3)
                    )
            );


            // -------------------------------------------------
            // Date
            // -------------------------------------------------

            String existingDate =
                    cursor.getString(4);


            etDate.setText(
                    existingDate
            );


            // Set Calendar to existing date so DatePicker
            // opens on the saved date.
            try {

                SimpleDateFormat format =
                        new SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                        );

                calendar.setTime(
                        format.parse(existingDate)
                );

            } catch (Exception ignored) {

            }


            // -------------------------------------------------
            // Notes
            // -------------------------------------------------

            etNotes.setText(
                    cursor.getString(5)
            );


            /*
             * cursor.getString(6) is the old ExpenseMode.
             *
             * We deliberately DO NOT use it anymore to determine
             * whether the expense is personal/family.
             *
             * Sharing comes from ExpenseFamilyShare.
             */


            // -------------------------------------------------
            // Category
            // -------------------------------------------------

            String category =
                    cursor.getString(7);


            setSpinnerSelection(
                    spCategory,
                    category
            );


            // -------------------------------------------------
            // Payment Method
            // -------------------------------------------------

            String paymentMethod =
                    cursor.getString(8);


            setSpinnerSelection(
                    spPaymentMethod,
                    paymentMethod
            );


            // -------------------------------------------------
            // FAMILY SHARING
            // -------------------------------------------------

            loadExistingFamilyShare();

        } finally {

            cursor.close();
        }
    }


    // =========================================================
    // LOAD EXISTING FAMILY SHARE
    // =========================================================

    private void loadExistingFamilyShare() {

        int sharedFamilyId =
                databaseHelper.getFamilyIdForExpense(
                        transactionId
                );


        // -----------------------------------------------------
        // NOT SHARED
        // -----------------------------------------------------

        if (sharedFamilyId == -1) {

            switchShareFamily.setChecked(false);

            layoutFamilySelection.setVisibility(
                    View.GONE
            );

            return;
        }


        // -----------------------------------------------------
        // SHARED
        // -----------------------------------------------------

        /*
         * Expense has a share record.
         *
         * Normally the current user should also have this
         * family inside familyIds.
         */

        int familyPosition =
                familyIds.indexOf(
                        sharedFamilyId
                );


        if (familyPosition != -1) {

            switchShareFamily.setChecked(true);

            layoutFamilySelection.setVisibility(
                    View.VISIBLE
            );


            spFamily.setSelection(
                    familyPosition
            );

        } else {

            /*
             * Share exists but current user can no longer access
             * that family.
             *
             * Don't allow accidental reassignment.
             */

            switchShareFamily.setChecked(false);

            layoutFamilySelection.setVisibility(
                    View.GONE
            );
        }
    }


    // =========================================================
    // SET SPINNER SELECTION
    // =========================================================

    private void setSpinnerSelection(
            Spinner spinner,
            String value
    ) {

        if (spinner.getAdapter() == null ||
                value == null) {

            return;
        }


        ArrayAdapter<?> adapter =
                (ArrayAdapter<?>) spinner.getAdapter();


        for (int i = 0;
             i < adapter.getCount();
             i++) {


            Object item =
                    adapter.getItem(i);


            if (item != null &&
                    item.toString().equals(value)) {

                spinner.setSelection(i);

                break;
            }
        }
    }


    // =========================================================
    // UPDATE EXPENSE
    // =========================================================

    private void updateExpense() {

        // -----------------------------------------------------
        // USER
        // -----------------------------------------------------

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );


        if (userId == -1) {

            Toast.makeText(
                    this,
                    "User not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // AMOUNT
        // -----------------------------------------------------

        String amountText =
                etAmount
                        .getText()
                        .toString()
                        .trim();


        if (amountText.isEmpty()) {

            etAmount.setError(
                    "Enter Amount"
            );

            etAmount.requestFocus();

            return;
        }


        double amount;


        try {

            amount =
                    Double.parseDouble(
                            amountText
                    );

        } catch (NumberFormatException e) {

            etAmount.setError(
                    "Invalid Amount"
            );

            etAmount.requestFocus();

            return;
        }


        if (amount <= 0) {

            etAmount.setError(
                    "Amount must be greater than 0"
            );

            etAmount.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // CATEGORY
        // -----------------------------------------------------

        if (spCategory.getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Please select a category.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String category =
                spCategory
                        .getSelectedItem()
                        .toString();


        int categoryId =
                databaseHelper.getCategoryIdByName(
                        category
                );


        if (categoryId == -1) {

            Toast.makeText(
                    this,
                    "Invalid category.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // PAYMENT METHOD
        // -----------------------------------------------------

        if (spPaymentMethod.getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Please select a payment method.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String payment =
                spPaymentMethod
                        .getSelectedItem()
                        .toString();


        int paymentMethodId =
                databaseHelper.getPaymentMethodIdByName(
                        payment
                );


        if (paymentMethodId == -1) {

            Toast.makeText(
                    this,
                    "Invalid payment method.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // DATE / NOTES
        // -----------------------------------------------------

        String date =
                etDate
                        .getText()
                        .toString()
                        .trim();


        String note =
                etNotes
                        .getText()
                        .toString()
                        .trim();


        // =====================================================
        // IMPORTANT:
        // Expense ownership remains personal.
        // =====================================================

        String expenseMode =
                "Personal";


        // -----------------------------------------------------
        // FAMILY SHARING SELECTION
        // -----------------------------------------------------

        boolean shareWithFamily =
                switchShareFamily.isChecked();


        int selectedFamilyId =
                -1;


        if (shareWithFamily) {

            if (familyIds.isEmpty()) {

                Toast.makeText(
                        this,
                        "You are not a member of any family group.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            int selectedPosition =
                    spFamily.getSelectedItemPosition();


            if (selectedPosition < 0 ||
                    selectedPosition >= familyIds.size()) {

                Toast.makeText(
                        this,
                        "Please select a family.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            selectedFamilyId =
                    familyIds.get(
                            selectedPosition
                    );
        }


        // =====================================================
        // UPDATE ACTUAL EXPENSE
        // =====================================================

        int result =
                databaseHelper.updateExpense(

                        transactionId,

                        paymentMethodId,

                        categoryId,

                        amount,

                        date,

                        note,

                        expenseMode
                );


        if (result <= 0) {

            Toast.makeText(
                    this,
                    "Failed to Update Expense",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // INVALIDATE AI INSIGHT CACHE
        // Expense data has changed
        // =====================================================

        AIInsightCacheManager.invalidate(
                this,
                userId
        );


        // =====================================================
        // UPDATE FAMILY SHARING
        // =====================================================

        if (shareWithFamily) {

            /*
             * This works for both:
             *
             * 1. Not shared before -> create
             * 2. Already shared -> change/update family
             */

            boolean shared =
                    databaseHelper.shareExpenseWithFamily(

                            transactionId,

                            selectedFamilyId,

                            userId
                    );


            if (!shared) {

                Toast.makeText(
                        this,
                        "Expense updated, but family sharing could not be updated.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

        } else {

            /*
             * If it was shared before, remove only the
             * sharing relationship.
             *
             * The expense itself remains untouched.
             */

            if (databaseHelper.isExpenseSharedWithFamily(
                    transactionId
            )) {

                databaseHelper.removeExpenseFromFamily(
                        transactionId
                );
            }
        }


        // =====================================================
        // SUCCESS
        // =====================================================

        Toast.makeText(
                this,
                "Expense Updated Successfully",
                Toast.LENGTH_SHORT
        ).show();


        finish();
    }
}
