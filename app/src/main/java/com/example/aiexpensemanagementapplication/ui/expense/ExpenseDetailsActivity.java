package com.example.aiexpensemanagementapplication.ui.expense;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;

public class ExpenseDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private ImageView imgCategory;

    private TextView tvCategory;
    private TextView tvAmount;
    private TextView tvPaymentMethod;
    private TextView tvExpenseMode;
    private TextView tvDate;
    private TextView tvNotes;

    private Button btnEditExpense;
    private Button btnDeleteExpense;

    private DatabaseHelper databaseHelper;

    private int transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expense_details);

        initialize();

        loadExpense();

        listeners();

    }

    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        btnBack = findViewById(R.id.btnBack);

        imgCategory = findViewById(R.id.imgCategory);

        tvCategory = findViewById(R.id.tvCategory);

        tvAmount = findViewById(R.id.tvAmount);

        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);

        tvExpenseMode = findViewById(R.id.tvExpenseMode);

        tvDate = findViewById(R.id.tvDate);

        tvNotes = findViewById(R.id.tvNotes);

        btnEditExpense = findViewById(R.id.btnEditExpense);

        btnDeleteExpense = findViewById(R.id.btnDeleteExpense);

        transactionId =
                getIntent().getIntExtra(
                        "transactionId",
                        -1);

    }

    private void listeners() {

        btnBack.setOnClickListener(v -> finish());

        btnEditExpense.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            ExpenseDetailsActivity.this,
                            EditExpenseActivity.class);

            intent.putExtra(
                    "transactionId",
                    transactionId);

            startActivity(intent);

        });

        btnDeleteExpense.setOnClickListener(v ->

                deleteExpense()

        );

    }

    private void loadExpense() {

        Cursor cursor =
                databaseHelper.getExpenseById(
                        transactionId);

        if (!cursor.moveToFirst()) {

            cursor.close();

            finish();

            return;

        }

        String category = cursor.getString(7);

        String payment = cursor.getString(8);

        double amount = cursor.getDouble(3);

        String date = cursor.getString(4);

        String notes = cursor.getString(5);

        String mode = cursor.getString(6);

        tvCategory.setText(category);

        tvAmount.setText(
                String.format("Rs. %.2f", amount));

        tvPaymentMethod.setText(payment);

        tvExpenseMode.setText(mode);

        tvDate.setText(date);

        tvNotes.setText(notes);

        setCategoryIcon(category);

        cursor.close();

    }

    private void setCategoryIcon(String category) {

        switch (category) {

            case "Food":
                imgCategory.setImageResource(R.drawable.ic_food);
                break;

            case "Transport":
                imgCategory.setImageResource(R.drawable.ic_transport);
                break;

            case "Shopping":
                imgCategory.setImageResource(R.drawable.ic_shopping);
                break;

            case "Bills":
                imgCategory.setImageResource(R.drawable.current_bill);
                break;

            default:
                imgCategory.setImageResource(R.drawable.expense);
                break;

        }

    }
    private void deleteExpense() {

        new MaterialAlertDialogBuilder(this)

                .setTitle("Delete Expense")

                .setMessage(
                        "Are you sure you want to delete this expense?\n\nThis action cannot be undone.")

                .setNegativeButton("Cancel", null)

                .setPositiveButton("Delete", (dialog, which) -> {

                    int result =
                            databaseHelper.deleteExpense(
                                    transactionId);

                    if(result > 0){

                        Toast.makeText(

                                ExpenseDetailsActivity.this,

                                "Expense Deleted Successfully",

                                Toast.LENGTH_SHORT

                        ).show();

                        finish();

                    }else{

                        Toast.makeText(

                                ExpenseDetailsActivity.this,

                                "Delete Failed",

                                Toast.LENGTH_SHORT

                        ).show();

                    }

                })

                .show();

    }
    @Override
    protected void onResume() {

        super.onResume();

        loadExpense();

    }

}