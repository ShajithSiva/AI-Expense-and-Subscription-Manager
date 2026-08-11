package com.example.aiexpensemanagementapplication.ui.family;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;

public class SetFamilyBudgetActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    private TextInputEditText etFamilyBudget;
    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;

    private MaterialButton btnSaveFamilyBudget;

    private DatabaseHelper databaseHelper;

    // IMPORTANT
    // This identifies which family owns this budget.
    private int familyId;

    private Calendar startCalendar;
    private Calendar endCalendar;

    private FamilyFirestoreService familyFirestoreService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_set_family_budget
        );

        databaseHelper =
                new DatabaseHelper(this);

        familyFirestoreService =
                new FamilyFirestoreService();


        // -------------------------------------------------
        // GET SELECTED FAMILY ID
        // -------------------------------------------------

        familyId =
                getIntent().getIntExtra(
                        "familyId",
                        -1
                );


        if (familyId == -1) {

            Toast.makeText(
                    this,
                    "Family not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        initializeViews();

        setupDates();

        setupListeners();

        // If this family already has a budget,
        // show it in the edit screen.
        loadExistingBudget();
    }


    // =====================================================
    // INITIALIZE
    // =====================================================

    private void initializeViews() {

        toolbar =
                findViewById(R.id.toolbar);

        etFamilyBudget =
                findViewById(R.id.etFamilyBudget);

        etStartDate =
                findViewById(R.id.etStartDate);

        etEndDate =
                findViewById(R.id.etEndDate);

        btnSaveFamilyBudget =
                findViewById(
                        R.id.btnSaveFamilyBudget
                );
    }


    // =====================================================
    // DEFAULT MONTH
    // =====================================================

    private void setupDates() {

        startCalendar =
                Calendar.getInstance();

        endCalendar =
                Calendar.getInstance();


        // First day of current month

        startCalendar.set(
                Calendar.DAY_OF_MONTH,
                1
        );


        // Last day of current month

        endCalendar.set(
                Calendar.DAY_OF_MONTH,
                endCalendar.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                )
        );


        etStartDate.setText(
                formatDate(startCalendar)
        );

        etEndDate.setText(
                formatDate(endCalendar)
        );
    }


    // =====================================================
    // DATE FORMAT
    // =====================================================

    private String formatDate(
            Calendar calendar
    ) {

        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(
                calendar.getTime()
        );
    }


    // =====================================================
    // LISTENERS
    // =====================================================

    private void setupListeners() {

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );


        etStartDate.setOnClickListener(
                v -> showStartDatePicker()
        );


        etEndDate.setOnClickListener(
                v -> showEndDatePicker()
        );


        btnSaveFamilyBudget.setOnClickListener(
                v -> saveBudget()
        );
    }


    // =====================================================
    // START DATE
    // =====================================================

    private void showStartDatePicker() {

        DatePickerDialog dialog =
                new DatePickerDialog(

                        this,

                        (view, year, month, day) -> {

                            startCalendar.set(
                                    year,
                                    month,
                                    day
                            );

                            etStartDate.setText(
                                    formatDate(
                                            startCalendar
                                    )
                            );
                        },

                        startCalendar.get(
                                Calendar.YEAR
                        ),

                        startCalendar.get(
                                Calendar.MONTH
                        ),

                        startCalendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }


    // =====================================================
    // END DATE
    // =====================================================

    private void showEndDatePicker() {

        DatePickerDialog dialog =
                new DatePickerDialog(

                        this,

                        (view, year, month, day) -> {

                            endCalendar.set(
                                    year,
                                    month,
                                    day
                            );

                            etEndDate.setText(
                                    formatDate(
                                            endCalendar
                                    )
                            );
                        },

                        endCalendar.get(
                                Calendar.YEAR
                        ),

                        endCalendar.get(
                                Calendar.MONTH
                        ),

                        endCalendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }


    // =====================================================
    // LOAD EXISTING FAMILY BUDGET
    // =====================================================

    private void loadExistingBudget() {

        double budget =
                databaseHelper.getFamilyBudgetLimit(
                        familyId
                );


        if (budget > 0) {

            etFamilyBudget.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.2f",
                            budget
                    )
            );
        }
    }


// =====================================================
// SAVE / UPDATE FAMILY BUDGET
// =====================================================

    private void saveBudget() {

        String amountText =
                etFamilyBudget
                        .getText()
                        .toString()
                        .trim();

        if (amountText.isEmpty()) {

            etFamilyBudget.setError(
                    "Enter family budget"
            );

            etFamilyBudget.requestFocus();

            return;
        }

        double amount;

        try {

            amount =
                    Double.parseDouble(
                            amountText
                    );

        } catch (NumberFormatException e) {

            etFamilyBudget.setError(
                    "Enter a valid amount"
            );

            etFamilyBudget.requestFocus();

            return;
        }

        if (amount <= 0) {

            etFamilyBudget.setError(
                    "Budget must be greater than 0"
            );

            etFamilyBudget.requestFocus();

            return;
        }

        String startDate =
                etStartDate
                        .getText()
                        .toString()
                        .trim();

        String endDate =
                etEndDate
                        .getText()
                        .toString()
                        .trim();

        if (startDate.isEmpty()) {

            etStartDate.setError(
                    "Select start date"
            );

            return;
        }

        if (endDate.isEmpty()) {

            etEndDate.setError(
                    "Select end date"
            );

            return;
        }

        // -------------------------------------------------
        // GET FIRESTORE FAMILY ID
        // -------------------------------------------------

        String firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        familyId
                );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Family Firestore ID not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // -------------------------------------------------
        // DISABLE BUTTON
        // -------------------------------------------------

        btnSaveFamilyBudget.setEnabled(false);

        btnSaveFamilyBudget.setText(
                "Saving..."
        );

        // -------------------------------------------------
        // SAVE TO SQLITE
        // -------------------------------------------------

        boolean localSuccess =
                databaseHelper.saveFamilyBudget(

                        familyId,

                        amount,

                        startDate,

                        endDate
                );

        if (!localSuccess) {

            btnSaveFamilyBudget.setEnabled(true);

            btnSaveFamilyBudget.setText(
                    "Save Family Budget"
            );

            Toast.makeText(
                    this,
                    "Failed to save family budget locally.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // -------------------------------------------------
        // SAVE TO FIRESTORE
        // -------------------------------------------------

        familyFirestoreService.saveFamilyBudget(

                firestoreFamilyId,

                amount,

                startDate,

                endDate,

                new FamilyFirestoreService.FamilyBudgetCallback() {

                    @Override
                    public void onSuccess(
                            FamilyFirestoreService.FamilyBudgetData budget
                    ) {

                        Toast.makeText(
                                SetFamilyBudgetActivity.this,
                                "Family budget saved successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        btnSaveFamilyBudget.setEnabled(true);

                        btnSaveFamilyBudget.setText(
                                "Save Family Budget"
                        );

                        Toast.makeText(
                                SetFamilyBudgetActivity.this,
                                "Budget saved locally, but Firestore sync failed: "
                                        + message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}