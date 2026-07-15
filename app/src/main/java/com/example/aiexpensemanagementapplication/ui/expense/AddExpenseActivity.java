package com.example.aiexpensemanagementapplication.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etAmount;
    private RadioGroup rgExpenseType;

    private RadioButton rbPersonal;

    private RadioButton rbFamily;
    private EditText etDate;
    private EditText etNotes;

    private Spinner spCategory;
    private Spinner spPaymentMethod;

    private Button btnSaveExpense;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_expense);

        initialize();

        loadCategories();

        loadPaymentMethods();

        setCurrentDate();

        listeners();
    }
    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        calendar = Calendar.getInstance();

        etAmount = findViewById(R.id.etAmount);

        rgExpenseType = findViewById(R.id.rgExpenseType);

        rbPersonal = findViewById(R.id.rbPersonal);

        rbFamily = findViewById(R.id.rbFamily);

        etDate = findViewById(R.id.etDate);
        etNotes = findViewById(R.id.etNotes);

        spCategory = findViewById(R.id.spCategory);
        spPaymentMethod = findViewById(R.id.spPaymentMethod);

        btnSaveExpense = findViewById(R.id.btnSaveExpense);

        btnBack = findViewById(R.id.btnBack);

    }

    private void listeners() {

        btnBack.setOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> showDatePicker());

        btnSaveExpense.setOnClickListener(v -> saveExpense());

    }
    private void setCurrentDate() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault());

        etDate.setText(
                format.format(calendar.getTime())
        );

    }
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
    private void loadCategories() {

        ArrayList<String> categories =
                databaseHelper.getExpenseCategoryNames();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories
                );

        spCategory.setAdapter(adapter);
    }
    private void loadPaymentMethods() {

        ArrayList<String> methods =
                databaseHelper.getPaymentMethodNames();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        methods
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spPaymentMethod.setAdapter(adapter);
    }
    private void saveExpense() {

        if(currentUser==null){

            Toast.makeText(this,
                    "User not logged in.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        int userId = databaseHelper.getUserIdByFirebaseUid(currentUser.getUid());

        if(userId==-1){

            Toast.makeText(this,
                    "User not found.",
                    Toast.LENGTH_SHORT).show();

            return;
        }


        String amountText = etAmount.getText().toString().trim();

        if (amountText.isEmpty()) {

            etAmount.setError("Enter expense amount");

            etAmount.requestFocus();

            return;
        }

        String expenseMode;

        if (rbPersonal.isChecked()) {

            expenseMode = "Personal";

        } else {

            expenseMode = "Family";

        }

        double amount;


        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            etAmount.setError("Enter a valid amount");
            etAmount.requestFocus();
            return;
        }

        String categoryName = spCategory.getSelectedItem().toString();

        int categoryId = databaseHelper.getCategoryIdByName(categoryName);

        String paymentMethodName = spPaymentMethod.getSelectedItem().toString();

        String date = etDate.getText().toString();

        String note = etNotes.getText().toString().trim();

        int paymentMethodId = databaseHelper.getPaymentMethodIdByName(paymentMethodName);

        long result =
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

        if (result != -1) {

            Toast.makeText(
                    this,
                    "Expense added successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Failed to save expense.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


}

