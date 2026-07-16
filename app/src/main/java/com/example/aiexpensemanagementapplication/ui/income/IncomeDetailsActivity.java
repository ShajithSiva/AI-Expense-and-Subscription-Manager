package com.example.aiexpensemanagementapplication.ui.income;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class IncomeDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private ImageView imgCategory;

    private TextView tvCategory;
    private TextView tvAmount;
    private TextView tvIncomeSource;
    private TextView tvDate;
    private TextView tvNotes;

    private Button btnEditIncome;
    private Button btnDeleteIncome;

    private DatabaseHelper databaseHelper;

    private int transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_income_details);

        initialize();

        if (transactionId == -1) {

            Toast.makeText(
                    this,
                    "Income record not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        setListeners();
    }

    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        transactionId = getIntent().getIntExtra(
                "transactionId",
                -1
        );

        btnBack = findViewById(R.id.btnBack);

        imgCategory = findViewById(R.id.imgCategory);

        tvCategory = findViewById(R.id.tvCategory);
        tvAmount = findViewById(R.id.tvAmount);
        tvIncomeSource = findViewById(R.id.tvIncomeSource);
        tvDate = findViewById(R.id.tvDate);
        tvNotes = findViewById(R.id.tvNotes);

        btnEditIncome = findViewById(R.id.btnEditIncome);
        btnDeleteIncome = findViewById(R.id.btnDeleteIncome);
    }

    private void setListeners() {

        btnBack.setOnClickListener(
                view -> finish()
        );

        btnEditIncome.setOnClickListener(view -> {

            Intent intent = new Intent(
                    IncomeDetailsActivity.this,
                    EditIncomeActivity.class
            );

            intent.putExtra(
                    "transactionId",
                    transactionId
            );

            startActivity(intent);
        });

        btnDeleteIncome.setOnClickListener(
                view -> showDeleteConfirmation()
        );
    }

    private void loadIncome() {

        Cursor cursor =
                databaseHelper.getIncomeById(
                        transactionId
                );

        if (!cursor.moveToFirst()) {

            cursor.close();

            Toast.makeText(
                    this,
                    "Income details could not be loaded.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        double amount =
                cursor.getDouble(3);

        String date =
                cursor.getString(4);

        String notes =
                cursor.getString(5);

        String category =
                cursor.getString(7);

        String incomeSource =
                cursor.getString(8);

        tvCategory.setText(category);

        tvAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "Rs. %.2f",
                        amount
                )
        );

        tvIncomeSource.setText(incomeSource);

        tvDate.setText(date);

        if (notes == null || notes.trim().isEmpty()) {

            tvNotes.setText("No notes added");

        } else {

            tvNotes.setText(notes);
        }

        setCategoryIcon(category);

        cursor.close();
    }

    private void setCategoryIcon(String category) {

        if (category == null) {

            imgCategory.setImageResource(
                    R.drawable.money
            );

            return;
        }

        switch (category) {

            case "Salary":
            case "Business":
            case "Investment":
            case "Gift":
            default:
                imgCategory.setImageResource(
                        R.drawable.money
                );
                break;
        }
    }

    private void showDeleteConfirmation() {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Income")
                .setMessage(
                        "Are you sure you want to delete this income?\n\n" +
                                "This action cannot be undone."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteIncome()
                )
                .show();
    }

    private void deleteIncome() {

        int result =
                databaseHelper.deleteTransaction(
                        transactionId
                );

        if (result > 0) {

            Toast.makeText(
                    this,
                    "Income deleted successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Failed to delete income.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (transactionId != -1) {

            loadIncome();
        }
    }
}