package com.example.aiexpensemanagementapplication.ui.expense;

import android.app.DatePickerDialog;
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
import com.example.aiexpensemanagementapplication.notification.ReminderScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

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

    private MaterialButton btnSaveExpense;
    private ImageButton btnBack;


    // =========================================================
    // DATABASE / AUTH
    // =========================================================

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    // =========================================================
    // DATE
    // =========================================================

    private Calendar calendar;


    // =========================================================
    // FAMILY DATA
    // =========================================================

    private ArrayList<Integer> familyIds;
    private ArrayList<String> familyNames;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_expense);

        initialize();

        loadCategories();

        loadPaymentMethods();

        loadFamilies();

        setCurrentDate();

        listeners();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initialize() {

        databaseHelper =
                new DatabaseHelper(this);

        mAuth =
                FirebaseAuth.getInstance();

        currentUser =
                mAuth.getCurrentUser();

        calendar =
                Calendar.getInstance();


        // -----------------------------------------------------
        // Basic fields
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

        btnSaveExpense =
                findViewById(R.id.btnSaveExpense);

        btnBack =
                findViewById(R.id.btnBack);


        // -----------------------------------------------------
        // Lists
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

        // Back
        btnBack.setOnClickListener(
                v -> finish()
        );


        // Date picker
        etDate.setOnClickListener(
                v -> showDatePicker()
        );


        // -----------------------------------------------------
        // SHARE WITH FAMILY SWITCH
        // -----------------------------------------------------

        switchShareFamily.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        // User must belong to at least one family
                        if (familyIds.isEmpty()) {

                            switchShareFamily.setChecked(false);

                            layoutFamilySelection.setVisibility(
                                    View.GONE
                            );

                            Toast.makeText(
                                    AddExpenseActivity.this,
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


        // Save
        btnSaveExpense.setOnClickListener(
                v -> saveExpense()
        );
    }


    // =========================================================
    // CURRENT DATE
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

        spCategory.setAdapter(adapter);
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

        spPaymentMethod.setAdapter(adapter);
    }


    // =========================================================
    // LOAD USER FAMILIES
    // =========================================================

    private void loadFamilies() {

        // -----------------------------------------------------
        // Check Firebase login
        // -----------------------------------------------------

        if (currentUser == null) {

            switchShareFamily.setEnabled(false);

            return;
        }


        // -----------------------------------------------------
        // Get local user ID
        // -----------------------------------------------------

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );

        if (userId == -1) {

            switchShareFamily.setEnabled(false);

            return;
        }


        // -----------------------------------------------------
        // Get families
        // -----------------------------------------------------

        familyIds =
                databaseHelper.getFamilyIdsForUser(
                        userId
                );

        familyNames =
                databaseHelper.getFamilyNamesForUser(
                        userId
                );


        // -----------------------------------------------------
        // Safety check
        // -----------------------------------------------------

        if (familyIds.size() != familyNames.size()) {

            familyIds.clear();

            familyNames.clear();

            switchShareFamily.setEnabled(false);

            return;
        }


        // -----------------------------------------------------
        // No family
        // -----------------------------------------------------

        if (familyIds.isEmpty()) {

            switchShareFamily.setChecked(false);

            switchShareFamily.setEnabled(false);

            layoutFamilySelection.setVisibility(
                    View.GONE
            );

            return;
        }


        // -----------------------------------------------------
        // Family spinner adapter
        // -----------------------------------------------------

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


        // Sharing is available
        switchShareFamily.setEnabled(true);
    }


    // =========================================================
    // SAVE EXPENSE
    // =========================================================

    private void saveExpense() {

        // -----------------------------------------------------
        // CHECK USER
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
                    "Enter expense amount"
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
                    "Enter a valid amount"
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


        String categoryName =
                spCategory
                        .getSelectedItem()
                        .toString();


        int categoryId =
                databaseHelper.getCategoryIdByName(
                        categoryName
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


        String paymentMethodName =
                spPaymentMethod
                        .getSelectedItem()
                        .toString();


        int paymentMethodId =
                databaseHelper.getPaymentMethodIdByName(
                        paymentMethodName
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
        // DATE / NOTE
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
        //
        // Every expense is PERSONAL-OWNED.
        //
        // Family sharing is handled separately through
        // ExpenseFamilyShare.
        // =====================================================

        String expenseMode =
                "Personal";


        // -----------------------------------------------------
        // VALIDATE FAMILY SHARING
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
        // INSERT EXPENSE
        // =====================================================

        long transactionId =
                databaseHelper.insertTransaction(

                        userId,

                        paymentMethodId,

                        categoryId,

                        amount,

                        "Expense",

                        date,

                        note,

                        expenseMode
                );


        // -----------------------------------------------------
        // TRANSACTION FAILED
        // -----------------------------------------------------

        if (transactionId == -1) {

            Toast.makeText(
                    this,
                    "Failed to save expense.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // FAMILY SHARING
        // =====================================================

        if (shareWithFamily) {

            boolean shared =
                    databaseHelper.shareExpenseWithFamily(

                            (int) transactionId,

                            selectedFamilyId,

                            userId
                    );


            if (!shared) {

                /*
                 * Expense itself was successfully saved.
                 *
                 * Only family sharing failed.
                 *
                 * Therefore DO NOT tell the user that the entire
                 * expense failed.
                 */

                Toast.makeText(
                        this,
                        "Expense saved, but it could not be shared with the family.",
                        Toast.LENGTH_LONG
                ).show();

            }
        }


        // =====================================================
        // AI ALERTS
        // =====================================================

        List<String> alerts =
                databaseHelper.generateAIAlerts(
                        userId
                );


        for (String alert : alerts) {

            databaseHelper.insertNotification(

                    "🤖 AI Smart Alert",

                    alert,

                    "Financial Assistant",

                    "AI",

                    System.currentTimeMillis()
            );
        }


        // =====================================================
        // REMINDERS
        // =====================================================

        ReminderScheduler scheduler =
                new ReminderScheduler(this);


        scheduler.checkBudgetReminder(
                databaseHelper,
                userId
        );


        scheduler.checkCategoryBudgetReminder(
                databaseHelper,
                userId,
                categoryName
        );


        // =====================================================
        // SUCCESS
        // =====================================================

        if (shareWithFamily) {

            Toast.makeText(
                    this,
                    "Expense added successfully.",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Expense added successfully.",
                    Toast.LENGTH_SHORT
            ).show();
        }


        finish();
    }
}