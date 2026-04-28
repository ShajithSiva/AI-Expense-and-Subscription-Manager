package com.example.aiexpensemanagementapplication.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import com.example.aiexpensemanagementapplication.R;

public class EditExpenseActivity extends AppCompatActivity {

    EditText amount, date, description;
    Spinner categorySpinner, paymentMethod;
    RadioGroup expenseType;
    RadioButton personal, family;
    Button saveBtn, deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        // Initialize views
        amount = findViewById(R.id.amount);
        date = findViewById(R.id.date);
        description = findViewById(R.id.description);
        categorySpinner = findViewById(R.id.categorySpinner);
        paymentMethod = findViewById(R.id.paymentMethod);
        expenseType = findViewById(R.id.expenseType);
        personal = findViewById(R.id.personal);
        family = findViewById(R.id.family);
        saveBtn = findViewById(R.id.saveBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        // 📅 Date Picker
        date.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditExpenseActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        date.setText(selectedDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        // 💾 Save Button
        saveBtn.setOnClickListener(v -> {

            String amt = amount.getText().toString().trim();
            String selectedDate = date.getText().toString().trim();
            String desc = description.getText().toString().trim();

            String category = categorySpinner.getSelectedItem().toString();
            String payment = paymentMethod.getSelectedItem().toString();

            int selectedId = expenseType.getCheckedRadioButtonId();
            RadioButton selectedTypeBtn = findViewById(selectedId);
            String type = selectedTypeBtn.getText().toString();

            if (amt.isEmpty()) {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            // 👉 Here you can save to database
            Toast.makeText(this, "Expense Saved", Toast.LENGTH_SHORT).show();
        });

        // ❌ Delete Button
        deleteBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Expense Deleted", Toast.LENGTH_SHORT).show();

            // 👉 Here you can delete from database
        });
    }
}