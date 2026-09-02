package com.example.aiexpensemanagementapplication.ui.income;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.ui.ai.AIInsightCacheManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditIncomeActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private EditText etIncomeAmount;
    private EditText etIncomeDate;
    private EditText etIncomeNotes;

    private Spinner spIncomeCategory;
    private Spinner spIncomeSource;

    private Button btnUpdateIncome;
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


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_income);

        initialize();

        loadIncomeCategories();

        loadIncomeSources();

        loadIncome();

        setListeners();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initialize() {

        databaseHelper =
                new DatabaseHelper(this);


        calendar =
                Calendar.getInstance();


        transactionId =
                getIntent().getIntExtra(
                        "transactionId",
                        -1
                );


        // -----------------------------------------------------
        // Firebase Authentication
        // -----------------------------------------------------

        mAuth =
                FirebaseAuth.getInstance();

        currentUser =
                mAuth.getCurrentUser();


        // -----------------------------------------------------
        // Fields
        // -----------------------------------------------------

        etIncomeAmount =
                findViewById(
                        R.id.etIncomeAmount
                );

        etIncomeDate =
                findViewById(
                        R.id.etIncomeDate
                );

        etIncomeNotes =
                findViewById(
                        R.id.etIncomeNotes
                );


        // -----------------------------------------------------
        // Spinners
        // -----------------------------------------------------

        spIncomeCategory =
                findViewById(
                        R.id.spIncomeCategory
                );

        spIncomeSource =
                findViewById(
                        R.id.spIncomeSource
                );


        // -----------------------------------------------------
        // Buttons
        // -----------------------------------------------------

        btnUpdateIncome =
                findViewById(
                        R.id.btnUpdateIncome
                );

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        // -----------------------------------------------------
        // Validate transaction ID
        // -----------------------------------------------------

        if (transactionId == -1) {

            Toast.makeText(
                    this,
                    "Income record not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setListeners() {

        btnBack.setOnClickListener(
                view -> finish()
        );


        etIncomeDate.setOnClickListener(
                view -> showDatePicker()
        );


        btnUpdateIncome.setOnClickListener(
                view -> updateIncome()
        );
    }


    // =========================================================
    // LOAD INCOME CATEGORIES
    // =========================================================

    private void loadIncomeCategories() {

        ArrayList<String> categories =
                databaseHelper.getIncomeCategoryNames();


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spIncomeCategory.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LOAD INCOME SOURCES
    // =========================================================

    private void loadIncomeSources() {

        ArrayList<String> sources =
                databaseHelper.getPaymentMethodNames();


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        sources
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spIncomeSource.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LOAD EXISTING INCOME
    // =========================================================

    private void loadIncome() {

        Cursor cursor =
                databaseHelper.getIncomeById(
                        transactionId
                );


        if (cursor == null) {

            Toast.makeText(
                    this,
                    "Income details could not be loaded.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        try {

            if (cursor.moveToFirst()) {

                // -------------------------------------------------
                // Amount
                // -------------------------------------------------

                double amount =
                        cursor.getDouble(3);


                // -------------------------------------------------
                // Date
                // -------------------------------------------------

                String date =
                        cursor.getString(4);


                // -------------------------------------------------
                // Notes
                // -------------------------------------------------

                String note =
                        cursor.getString(5);


                // -------------------------------------------------
                // Category
                // -------------------------------------------------

                String categoryName =
                        cursor.getString(7);


                // -------------------------------------------------
                // Source
                // -------------------------------------------------

                String sourceName =
                        cursor.getString(8);


                etIncomeAmount.setText(
                        String.valueOf(amount)
                );


                etIncomeDate.setText(
                        date
                );


                if (note == null) {

                    etIncomeNotes.setText("");

                } else {

                    etIncomeNotes.setText(
                            note
                    );
                }


                setSpinnerSelection(
                        spIncomeCategory,
                        categoryName
                );


                setSpinnerSelection(
                        spIncomeSource,
                        sourceName
                );


                setCalendarFromDate(
                        date
                );


            } else {

                Toast.makeText(
                        this,
                        "Income details could not be loaded.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }

        } finally {

            cursor.close();
        }
    }


    // =========================================================
    // SET SPINNER SELECTION
    // =========================================================

    private void setSpinnerSelection(
            Spinner spinner,
            String value
    ) {

        ArrayAdapter<?> adapter =
                (ArrayAdapter<?>) spinner.getAdapter();


        if (adapter == null ||
                value == null) {

            return;
        }


        for (int i = 0;
             i < adapter.getCount();
             i++) {

            Object item =
                    adapter.getItem(i);


            if (item != null &&
                    item.toString().equalsIgnoreCase(value)) {

                spinner.setSelection(i);

                break;
            }
        }
    }


    // =========================================================
    // SET CALENDAR FROM EXISTING DATE
    // =========================================================

    private void setCalendarFromDate(
            String date
    ) {

        if (date == null ||
                date.trim().isEmpty()) {

            return;
        }


        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    );


            format.setLenient(false);


            Date parsedDate =
                    format.parse(date);


            if (parsedDate != null) {

                calendar.setTime(
                        parsedDate
                );
            }


        } catch (Exception ignored) {

            calendar =
                    Calendar.getInstance();
        }
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

                            setSelectedDate();
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


        dialog.show();
    }


    // =========================================================
    // SET SELECTED DATE
    // =========================================================

    private void setSelectedDate() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );


        etIncomeDate.setText(
                format.format(
                        calendar.getTime()
                )
        );
    }


    // =========================================================
    // UPDATE INCOME
    // =========================================================

    private void updateIncome() {

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
                etIncomeAmount
                        .getText()
                        .toString()
                        .trim();


        if (amountText.isEmpty()) {

            etIncomeAmount.setError(
                    "Enter income amount"
            );

            etIncomeAmount.requestFocus();

            return;
        }


        double amount;


        try {

            amount =
                    Double.parseDouble(
                            amountText
                    );

        } catch (NumberFormatException exception) {

            etIncomeAmount.setError(
                    "Enter a valid amount"
            );

            etIncomeAmount.requestFocus();

            return;
        }


        if (amount <= 0) {

            etIncomeAmount.setError(
                    "Amount must be greater than zero"
            );

            etIncomeAmount.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // CATEGORY
        // -----------------------------------------------------

        if (spIncomeCategory.getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Select an income category.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // SOURCE
        // -----------------------------------------------------

        if (spIncomeSource.getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Select an income source.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String categoryName =
                spIncomeCategory
                        .getSelectedItem()
                        .toString();


        String sourceName =
                spIncomeSource
                        .getSelectedItem()
                        .toString();


        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        String date =
                etIncomeDate
                        .getText()
                        .toString()
                        .trim();


        if (date.isEmpty()) {

            etIncomeDate.setError(
                    "Select income date"
            );

            return;
        }


        // -----------------------------------------------------
        // NOTES
        // -----------------------------------------------------

        String note =
                etIncomeNotes
                        .getText()
                        .toString()
                        .trim();


        // -----------------------------------------------------
        // CATEGORY ID
        // -----------------------------------------------------

        int categoryId =
                databaseHelper.getCategoryIdByName(
                        categoryName
                );


        // -----------------------------------------------------
        // PAYMENT METHOD ID
        // -----------------------------------------------------

        int paymentMethodId =
                databaseHelper.getPaymentMethodIdByName(
                        sourceName
                );


        if (categoryId == -1) {

            Toast.makeText(
                    this,
                    "Income category not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (paymentMethodId == -1) {

            Toast.makeText(
                    this,
                    "Income source not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // INCOME MODE
        // =====================================================

        /*
         * Every income record remains personal.
         */

        String incomeMode =
                "Personal";


        // =====================================================
        // UPDATE INCOME
        // =====================================================

        int result =
                databaseHelper.updateIncome(
                        transactionId,
                        paymentMethodId,
                        categoryId,
                        amount,
                        date,
                        note,
                        incomeMode
                );


        // =====================================================
        // UPDATE SUCCESS
        // =====================================================

        if (result > 0) {

            // =================================================
            // INVALIDATE AI INSIGHT CACHE
            // Income data has changed
            // =================================================

            AIInsightCacheManager.invalidate(
                    this,
                    userId
            );


            Toast.makeText(
                    this,
                    "Income updated successfully.",
                    Toast.LENGTH_SHORT
            ).show();


            finish();


        } else {

            Toast.makeText(
                    this,
                    "Failed to update income.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =========================================================
    // ON DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {

            databaseHelper.close();
        }

        super.onDestroy();
    }
}