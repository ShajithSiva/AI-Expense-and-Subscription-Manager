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
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditExpenseActivity extends AppCompatActivity {

    private EditText etAmount;
    private RadioGroup rgExpenseType;

    private RadioButton rbPersonal;

    private RadioButton rbFamily;
    private EditText etDate;
    private EditText etNotes;

    private Spinner spCategory;
    private Spinner spPaymentMethod;

    private Button btnUpdateExpense;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Calendar calendar;

    private int transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_expense);

        initialize();

        loadCategories();

        loadPaymentMethods();

        loadExpense();

        listeners();
    }
    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        transactionId = getIntent().getIntExtra(
                "transactionId",
                -1);

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

        btnUpdateExpense = findViewById(R.id.btnUpdateExpense);

        btnBack = findViewById(R.id.btnBack);

    }

    private void listeners() {

        btnBack.setOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> showDatePicker());

        btnUpdateExpense.setOnClickListener(v -> updateExpense());

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
    private void loadExpense() {

        Cursor cursor =
                databaseHelper.getExpenseById(transactionId);

        if(cursor.moveToFirst()){

            etAmount.setText(
                    String.valueOf(cursor.getDouble(3)));

            etDate.setText(
                    cursor.getString(4));

            etNotes.setText(
                    cursor.getString(5));

            String mode =
                    cursor.getString(6);

            if(mode.equals("Personal")){

                rbPersonal.setChecked(true);

            }else{

                rbFamily.setChecked(true);

            }

            String category =
                    cursor.getString(7);

            String paymentMethod =
                    cursor.getString(8);

            setSpinnerSelection(
                    spCategory,
                    category);

            setSpinnerSelection(
                    spPaymentMethod,
                    paymentMethod);

        }

        cursor.close();

    }

    private void setSpinnerSelection(Spinner spinner,
                                     String value){

        ArrayAdapter adapter =
                (ArrayAdapter) spinner.getAdapter();

        for(int i=0;i<adapter.getCount();i++){

            if(adapter.getItem(i).toString().equals(value)){

                spinner.setSelection(i);

                break;

            }

        }

    }

    private void updateExpense() {

        String amountText =
                etAmount.getText().toString().trim();

        if(amountText.isEmpty()){

            etAmount.setError("Enter Amount");

            return;

        }

        double amount;

        try{

            amount =
                    Double.parseDouble(amountText);

        }catch(Exception e){

            etAmount.setError("Invalid Amount");

            return;

        }

        String category =
                spCategory.getSelectedItem().toString();

        String payment =
                spPaymentMethod.getSelectedItem().toString();

        String date =
                etDate.getText().toString();

        String note =
                etNotes.getText().toString();

        String expenseMode =
                rbPersonal.isChecked()
                        ? "Personal"
                        : "Family";

        int categoryId =
                databaseHelper.getCategoryIdByName(
                        category);

        int paymentMethodId =
                databaseHelper.getPaymentMethodIdByName(
                        payment);

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

        if(result>0){

            Toast.makeText(

                    this,

                    "Expense Updated Successfully",

                    Toast.LENGTH_SHORT

            ).show();

            finish();

        }else{

            Toast.makeText(

                    this,

                    "Failed to Update Expense",

                    Toast.LENGTH_SHORT

            ).show();

        }

    }


}

