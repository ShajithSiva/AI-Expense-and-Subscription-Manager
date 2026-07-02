package com.example.aiexpensemanagementapplication.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import com.example.aiexpensemanagementapplication.R;

public class AddExpenseActivity extends AppCompatActivity {

    EditText amount, date, description;
    Spinner categorySpinner, paymentMethod;
    RadioGroup expenseType;
    RadioButton personal, family;
    Button saveBtn;
    TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // 🔗 Initialize Views
        amount = findViewById(R.id.amount);
        date = findViewById(R.id.date);
        description = findViewById(R.id.description);
        categorySpinner = findViewById(R.id.categorySpinner);
        paymentMethod = findViewById(R.id.paymentMethod);
        expenseType = findViewById(R.id.expenseType);
        personal = findViewById(R.id.personal);
        family = findViewById(R.id.family);
        saveBtn = findViewById(R.id.saveBtn);
        btnBack = findViewById(R.id.btnBack);

        // 🔙 Back Button
        btnBack.setOnClickListener(v -> finish());

        // 📅 Date Picker
        date.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    AddExpenseActivity.this,
                    (view, y, m, d) -> date.setText(d + "/" + (m + 1) + "/" + y),
                    year, month, day
            );

            dialog.show();
        });

        // 📌 Spinner Data (Sample)
        String[] categories = {"Food", "Transport", "Shopping", "Bills"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(catAdapter);

        String[] payments = {"Cash", "Card", "Bank"};
        ArrayAdapter<String> payAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, payments);
        paymentMethod.setAdapter(payAdapter);

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

            // 🔴 Validation
            if (amt.isEmpty()) {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show();
                return;
            }

            // 👉 Here you will insert into database later

            Toast.makeText(this, "Expense Added Successfully", Toast.LENGTH_SHORT).show();

            // 🔄 Clear fields after save
            amount.setText("");
            date.setText("");
            description.setText("");
            personal.setChecked(true);
            categorySpinner.setSelection(0);
            paymentMethod.setSelection(0);
        });
    }
}