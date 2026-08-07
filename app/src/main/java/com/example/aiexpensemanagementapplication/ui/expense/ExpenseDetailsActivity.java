package com.example.aiexpensemanagementapplication.ui.expense;

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

public class ExpenseDetailsActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private ImageButton btnBack;

    private ImageView imgCategory;

    private TextView tvCategory;
    private TextView tvAmount;
    private TextView tvPaymentMethod;

    // NEW FAMILY SHARING UI
    private TextView tvFamilySharing;
    private TextView tvFamilySharingHint;

    private TextView tvDate;
    private TextView tvNotes;

    private Button btnEditExpense;
    private Button btnDeleteExpense;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;

    private int transactionId;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_expense_details
        );

        initialize();

        // -----------------------------------------------------
        // Validate transaction ID
        // -----------------------------------------------------

        if (transactionId == -1) {

            Toast.makeText(
                    this,
                    "Invalid expense.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        loadExpense();

        listeners();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initialize() {

        databaseHelper =
                new DatabaseHelper(this);


        transactionId =
                getIntent().getIntExtra(
                        "transactionId",
                        -1
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );


        imgCategory =
                findViewById(
                        R.id.imgCategory
                );


        tvCategory =
                findViewById(
                        R.id.tvCategory
                );


        tvAmount =
                findViewById(
                        R.id.tvAmount
                );


        tvPaymentMethod =
                findViewById(
                        R.id.tvPaymentMethod
                );


        // -----------------------------------------------------
        // FAMILY SHARING
        // -----------------------------------------------------

        tvFamilySharing =
                findViewById(
                        R.id.tvFamilySharing
                );


        tvFamilySharingHint =
                findViewById(
                        R.id.tvFamilySharingHint
                );


        tvDate =
                findViewById(
                        R.id.tvDate
                );


        tvNotes =
                findViewById(
                        R.id.tvNotes
                );


        btnEditExpense =
                findViewById(
                        R.id.btnEditExpense
                );


        btnDeleteExpense =
                findViewById(
                        R.id.btnDeleteExpense
                );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void listeners() {

        // -----------------------------------------------------
        // BACK
        // -----------------------------------------------------

        btnBack.setOnClickListener(
                v -> finish()
        );


        // -----------------------------------------------------
        // EDIT
        // -----------------------------------------------------

        btnEditExpense.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    ExpenseDetailsActivity.this,
                                    EditExpenseActivity.class
                            );


                    intent.putExtra(
                            "transactionId",
                            transactionId
                    );


                    startActivity(
                            intent
                    );
                }
        );


        // -----------------------------------------------------
        // DELETE
        // -----------------------------------------------------

        btnDeleteExpense.setOnClickListener(
                v -> deleteExpense()
        );
    }


    // =========================================================
    // LOAD EXPENSE
    // =========================================================

    private void loadExpense() {

        if (transactionId == -1) {

            return;
        }


        Cursor cursor =
                databaseHelper.getExpenseById(
                        transactionId
                );


        if (cursor == null) {

            Toast.makeText(
                    this,
                    "Unable to load expense.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        try {

            if (!cursor.moveToFirst()) {

                Toast.makeText(
                        this,
                        "Expense not found.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

                return;
            }


            // -------------------------------------------------
            // READ EXPENSE
            // -------------------------------------------------

            double amount =
                    cursor.getDouble(3);


            String date =
                    cursor.getString(4);


            String notes =
                    cursor.getString(5);


            /*
             * cursor.getString(6) = old ExpenseMode
             *
             * We deliberately ignore it.
             *
             * Family sharing is now determined using
             * ExpenseFamilyShare.
             */


            String category =
                    cursor.getString(7);


            String payment =
                    cursor.getString(8);


            // -------------------------------------------------
            // BASIC INFORMATION
            // -------------------------------------------------

            tvCategory.setText(
                    category != null
                            ? category
                            : "Expense"
            );


            tvAmount.setText(
                    String.format(
                            Locale.getDefault(),
                            "Rs. %.2f",
                            amount
                    )
            );


            tvPaymentMethod.setText(
                    payment != null
                            ? payment
                            : "-"
            );


            tvDate.setText(
                    date != null
                            ? date
                            : "-"
            );


            if (notes == null ||
                    notes.trim().isEmpty()) {

                tvNotes.setText(
                        "No notes"
                );

            } else {

                tvNotes.setText(
                        notes
                );
            }


            // -------------------------------------------------
            // CATEGORY ICON
            // -------------------------------------------------

            setCategoryIcon(
                    category
            );


            // -------------------------------------------------
            // FAMILY SHARING STATUS
            // -------------------------------------------------

            loadFamilySharingStatus();

        } finally {

            cursor.close();
        }
    }


    // =========================================================
    // LOAD FAMILY SHARING STATUS
    // =========================================================

    private void loadFamilySharingStatus() {

        int familyId =
                databaseHelper.getFamilyIdForExpense(
                        transactionId
                );


        // -----------------------------------------------------
        // NOT SHARED
        // -----------------------------------------------------

        if (familyId == -1) {

            tvFamilySharing.setText(
                    "Not Shared"
            );


            tvFamilySharingHint.setText(
                    "Visible only in your personal expenses"
            );

            return;
        }


        // -----------------------------------------------------
        // SHARED
        // -----------------------------------------------------

        String familyName =
                databaseHelper.getFamilyNameById(
                        familyId
                );


        if (familyName == null ||
                familyName.trim().isEmpty()) {

            /*
             * Share relationship exists, but family information
             * could not be loaded.
             */

            tvFamilySharing.setText(
                    "Shared with Family"
            );


            tvFamilySharingHint.setText(
                    "Visible on the family dashboard"
            );

            return;
        }


        tvFamilySharing.setText(
                "Shared with " + familyName
        );


        tvFamilySharingHint.setText(
                "Visible on the "
                        + familyName
                        + " dashboard"
        );
    }


    // =========================================================
    // CATEGORY ICON
    // =========================================================

    private void setCategoryIcon(
            String category
    ) {

        if (category == null) {

            imgCategory.setImageResource(
                    R.drawable.expense
            );

            return;
        }


        switch (category) {

            case "Food":

                imgCategory.setImageResource(
                        R.drawable.ic_food
                );

                break;


            case "Transport":

                imgCategory.setImageResource(
                        R.drawable.ic_transport
                );

                break;


            case "Shopping":

                imgCategory.setImageResource(
                        R.drawable.ic_shopping
                );

                break;


            case "Bills":

                imgCategory.setImageResource(
                        R.drawable.current_bill
                );

                break;


            default:

                imgCategory.setImageResource(
                        R.drawable.expense
                );

                break;
        }
    }


    // =========================================================
    // DELETE EXPENSE
    // =========================================================

    private void deleteExpense() {

        new MaterialAlertDialogBuilder(this)

                .setTitle(
                        "Delete Expense"
                )

                .setMessage(
                        "Are you sure you want to delete this expense?\n\n" +
                                "If this expense is shared with a family, " +
                                "it will also disappear from that family dashboard.\n\n" +
                                "This action cannot be undone."
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            int result =
                                    databaseHelper.deleteExpense(
                                            transactionId
                                    );


                            if (result > 0) {

                                Toast.makeText(
                                        ExpenseDetailsActivity.this,
                                        "Expense Deleted Successfully",
                                        Toast.LENGTH_SHORT
                                ).show();


                                finish();

                            } else {

                                Toast.makeText(
                                        ExpenseDetailsActivity.this,
                                        "Delete Failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )

                .show();
    }


    // =========================================================
    // REFRESH AFTER EDIT
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        /*
         * onResume() also runs immediately after onCreate().
         * That is okay here.
         *
         * More importantly, when EditExpenseActivity closes,
         * the latest expense + sharing information is loaded.
         */

        if (transactionId != -1) {

            loadExpense();
        }
    }
}