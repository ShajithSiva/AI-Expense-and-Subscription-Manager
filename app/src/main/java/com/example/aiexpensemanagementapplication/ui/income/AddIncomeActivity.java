package com.example.aiexpensemanagementapplication.ui.income;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.google.android.material.materialswitch.MaterialSwitch;
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
import java.util.Locale;

import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;

public class AddIncomeActivity extends AppCompatActivity {

    private EditText etIncomeAmount;
    private EditText etIncomeDate;
    private EditText etIncomeNotes;

    private Spinner spIncomeCategory;
    private Spinner spIncomeSource;

    private MaterialSwitch switchShareFamily;

    private LinearLayout layoutFamilySelection;

    private Spinner spFamily;

    private ArrayList<Integer> familyIds =
            new ArrayList<>();

    private ArrayList<String> familyNames =
            new ArrayList<>();

    private int selectedFamilyId = -1;


    private Button btnSaveIncome;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Calendar calendar;

    private FamilyFirestoreService familyFirestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_income);

        initializeViews();
        loadIncomeCategories();
        loadIncomeSources();
        setCurrentDate();
        setListeners();
    }

    /**
     * Initialize database, Firebase Authentication and XML views.
     */
    private void initializeViews() {

        databaseHelper = new DatabaseHelper(this);
        familyFirestoreService =
                new FamilyFirestoreService();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        calendar = Calendar.getInstance();

        etIncomeAmount = findViewById(R.id.etIncomeAmount);
        etIncomeDate = findViewById(R.id.etIncomeDate);
        etIncomeNotes = findViewById(R.id.etIncomeNotes);

        spIncomeCategory = findViewById(R.id.spIncomeCategory);
        spIncomeSource = findViewById(R.id.spIncomeSource);

        btnSaveIncome = findViewById(R.id.btnSaveIncome);
        btnBack = findViewById(R.id.btnBack);

        switchShareFamily =
                findViewById(R.id.switchShareFamily);

        layoutFamilySelection =
                findViewById(R.id.layoutFamilySelection);

        spFamily =
                findViewById(R.id.spFamily);
    }

    /**
     * Set click listeners.
     */
    private void setListeners() {

        btnBack.setOnClickListener(view -> finish());

        etIncomeDate.setOnClickListener(
                view -> showDatePicker()
        );

        btnSaveIncome.setOnClickListener(
                view -> saveIncome()
        );
        switchShareFamily.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        layoutFamilySelection.setVisibility(
                                View.VISIBLE
                        );

                        loadFamilies();

                    } else {

                        layoutFamilySelection.setVisibility(
                                View.GONE
                        );

                        selectedFamilyId = -1;
                    }
                }
        );
    }

    /**
     * Display today's date when the page opens.
     */
    private void setCurrentDate() {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );

        etIncomeDate.setText(
                dateFormat.format(calendar.getTime())
        );
    }

    /**
     * Open date picker when the date field is clicked.
     */
    private void showDatePicker() {

        DatePickerDialog datePickerDialog =
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

        datePickerDialog.show();
    }

    /**
     * Load income categories into the category spinner.
     */
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

        spIncomeCategory.setAdapter(adapter);
    }

    private void loadFamilies() {

        familyIds.clear();
        familyNames.clear();

        if (currentUser == null) {
            return;
        }

        int userId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );

        if (userId == -1) {
            return;
        }

        Cursor cursor =
                databaseHelper.getFamiliesForUser(
                        userId
                );

        if (cursor != null) {

            try {

                int idIndex =
                        cursor.getColumnIndex(
                                DatabaseHelper.FAMILY_ID
                        );

                int nameIndex =
                        cursor.getColumnIndex(
                                DatabaseHelper.FAMILY_NAME
                        );

                while (cursor.moveToNext()) {

                    if (idIndex != -1 &&
                            nameIndex != -1) {

                        familyIds.add(
                                cursor.getInt(idIndex)
                        );

                        familyNames.add(
                                cursor.getString(nameIndex)
                        );
                    }
                }

            } finally {

                cursor.close();
            }
        }

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

        spFamily.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
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
                            AdapterView<?> parent
                    ) {

                        selectedFamilyId = -1;
                    }
                }
        );
    }

    /**
     * Load income sources into the source spinner.
     */
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

        spIncomeSource.setAdapter(adapter);
    }

    /**
     * Validate and save income.
     */
    private void saveIncome() {

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

            amount = Double.parseDouble(amountText);

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

        if (spIncomeCategory.getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Select an income category.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

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

        int categoryId =
                databaseHelper.getCategoryIdByName(
                        categoryName
                );

        if (categoryId == -1) {

            Toast.makeText(
                    this,
                    "Income category not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String sourceName =
                spIncomeSource
                        .getSelectedItem()
                        .toString();

        int paymentMethodId =
                databaseHelper.getPaymentMethodIdByName(
                        sourceName
                );

        if (paymentMethodId == -1) {

            Toast.makeText(
                    this,
                    "Income source not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

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

        String note =
                etIncomeNotes
                        .getText()
                        .toString()
                        .trim();

        /*
         * Every income added from this page belongs
         * to the currently logged-in personal user.
         */
        String incomeMode =
                switchShareFamily.isChecked()
                        ? "Family"
                        : "Personal";

        if ("Family".equals(incomeMode) &&
                selectedFamilyId == -1) {

            Toast.makeText(
                    this,
                    "Please select a family.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        long transactionId =
                databaseHelper.insertTransaction(
                        userId,
                        paymentMethodId,
                        categoryId,
                        amount,
                        "Income",
                        date,
                        note,
                        incomeMode
                );

        if (transactionId == -1) {

            Toast.makeText(
                    this,
                    "Failed to save income.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // =====================================================
        // INVALIDATE AI INSIGHT CACHE
        // Income data has changed
        // =====================================================

        AIInsightCacheManager.invalidate(
                this,
                userId
        );

        if ("Family".equals(incomeMode)) {

            // -------------------------------------------------
            // SAVE FAMILY SHARE LOCALLY
            // -------------------------------------------------

            boolean shared =
                    databaseHelper.shareIncomeWithFamily(
                            (int) transactionId,
                            selectedFamilyId,
                            userId
                    );

            if (!shared) {

                Toast.makeText(
                        this,
                        "Income saved, but local family sharing failed.",
                        Toast.LENGTH_LONG
                ).show();

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
                        "Income saved, but family Firestore ID was not found.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            // -------------------------------------------------
            // GET OWNER NAME
            // -------------------------------------------------

            String ownerName =
                    currentUser.getDisplayName();

            if (ownerName == null ||
                    ownerName.trim().isEmpty()) {

                ownerName = "Family Member";
            }

            // -------------------------------------------------
            // SAVE FAMILY INCOME TO FIRESTORE
            // -------------------------------------------------

            familyFirestoreService.addFamilyIncome(

                    firestoreFamilyId,

                    String.valueOf(transactionId),

                    currentUser.getUid(),

                    ownerName,

                    amount,

                    categoryName,

                    categoryId,

                    sourceName,

                    paymentMethodId,

                    date,

                    note,

                    new FamilyFirestoreService.FamilyIncomeCallback() {

                        @Override
                        public void onSuccess() {

                            Toast.makeText(
                                    AddIncomeActivity.this,
                                    "Family income added successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }

                        @Override
                        public void onFailure(
                                String message
                        ) {

                            Toast.makeText(
                                    AddIncomeActivity.this,
                                    "Income saved, but family sync failed: "
                                            + message,
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        }
                    }
            );

        } else {

            Toast.makeText(
                    this,
                    "Personal income added successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }



    }
}
