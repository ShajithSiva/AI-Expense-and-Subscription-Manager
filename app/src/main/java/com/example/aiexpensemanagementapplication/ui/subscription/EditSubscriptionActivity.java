package com.example.aiexpensemanagementapplication.ui.subscription;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class EditSubscriptionActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private MaterialToolbar toolbar;

    private TextInputEditText etServiceName;
    private TextInputEditText etAmount;
    private TextInputEditText etNextBillingDate;

    private AutoCompleteTextView actCurrency;
    private AutoCompleteTextView actBillingCycle;

    private MaterialButton btnUpdate;
    private MaterialButton btnDelete;
    private MaterialButton btnCancel;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // SUBSCRIPTION
    // =========================================================

    private int subscriptionId = -1;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_edit_subscription
        );


        initializeViews();

        setupToolbar();


        databaseHelper =
                new DatabaseHelper(this);


        setupCurrency();

        setupBillingCycle();

        setupDatePicker();

        setupListeners();


        // =====================================================
        // GET SUBSCRIPTION ID
        // =====================================================

        subscriptionId =
                getIntent()
                        .getIntExtra(
                                "subscriptionId",
                                -1
                        );


        if (subscriptionId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid subscription.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        loadSubscription();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );


        etServiceName =
                findViewById(
                        R.id.etServiceName
                );


        etAmount =
                findViewById(
                        R.id.etAmount
                );


        actCurrency =
                findViewById(
                        R.id.actCurrency
                );


        actBillingCycle =
                findViewById(
                        R.id.actBillingCycle
                );


        etNextBillingDate =
                findViewById(
                        R.id.etNextBillingDate
                );


        btnUpdate =
                findViewById(
                        R.id.btnUpdate
                );


        btnDelete =
                findViewById(
                        R.id.btnDelete
                );


        btnCancel =
                findViewById(
                        R.id.btnCancel
                );
    }


    // =========================================================
    // TOOLBAR
    // =========================================================

    private void setupToolbar() {

        setSupportActionBar(
                toolbar
        );


        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // CURRENCY DROPDOWN
    // =========================================================

    private void setupCurrency() {

        String[] currencies = {

                "LKR",
                "USD",
                "EUR",
                "GBP",
                "AUD",
                "CAD",
                "SGD",
                "INR",
                "JPY"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        currencies
                );


        actCurrency.setAdapter(
                adapter
        );
    }


    // =========================================================
    // BILLING CYCLE DROPDOWN
    // =========================================================

    private void setupBillingCycle() {

        String[] cycles = {

                "Monthly",
                "Yearly",
                "Quarterly",
                "Weekly"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        cycles
                );


        actBillingCycle.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LOAD SUBSCRIPTION
    // =========================================================

    private void loadSubscription() {

        Cursor cursor = null;


        try {

            cursor =
                    databaseHelper
                            .getSubscription(
                                    subscriptionId
                            );


            if (cursor == null ||
                    !cursor.moveToFirst()) {

                Toast.makeText(
                        this,
                        "Subscription not found.",
                        Toast.LENGTH_LONG
                ).show();

                finish();

                return;
            }


            // =================================================
            // SERVICE NAME
            // =================================================

            String serviceName =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.SERVICE_NAME
                            )
                    );


            etServiceName.setText(
                    serviceName
            );


            // =================================================
            // AMOUNT
            // =================================================

            double amount =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.AMOUNT
                            )
                    );


            etAmount.setText(
                    String.valueOf(
                            amount
                    )
            );


            // =================================================
            // CURRENCY
            // =================================================

            int currencyIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper
                                    .SUBSCRIPTION_CURRENCY
                    );


            if (currencyIndex != -1 &&
                    !cursor.isNull(currencyIndex)) {

                String currency =
                        cursor.getString(
                                currencyIndex
                        );


                actCurrency.setText(
                        normalizeCurrency(
                                currency
                        ),
                        false
                );

            } else {

                /*
                 * Old records may not have a currency.
                 *
                 * IMPORTANT:
                 * Do not automatically set LKR.
                 *
                 * User must select the correct currency.
                 */

                actCurrency.setText(
                        "",
                        false
                );
            }


            // =================================================
            // BILLING CYCLE
            // =================================================

            String billingCycle =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper.BILLING_CYCLE
                            )
                    );


            actBillingCycle.setText(
                    safeString(
                            billingCycle
                    ),
                    false
            );


            // =================================================
            // NEXT BILLING DATE
            // =================================================

            String nextBillingDate =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DatabaseHelper
                                            .NEXT_BILLING_DATE
                            )
                    );


            etNextBillingDate.setText(
                    safeString(
                            nextBillingDate
                    )
            );


        } catch (Exception e) {

            e.printStackTrace();


            Toast.makeText(
                    this,
                    "Failed to load subscription.",
                    Toast.LENGTH_LONG
            ).show();


        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void setupDatePicker() {

        etNextBillingDate.setOnClickListener(
                v -> {

                    Calendar calendar =
                            Calendar.getInstance();


                    // =================================================
                    // USE CURRENT SAVED DATE IF AVAILABLE
                    // =================================================

                    String currentDate =
                            getText(
                                    etNextBillingDate
                            );


                    if (!currentDate.isEmpty()) {

                        try {

                            String[] parts =
                                    currentDate.split("-");


                            if (parts.length == 3) {

                                int year =
                                        Integer.parseInt(
                                                parts[0]
                                        );

                                int month =
                                        Integer.parseInt(
                                                parts[1]
                                        ) - 1;

                                int day =
                                        Integer.parseInt(
                                                parts[2]
                                        );


                                calendar.set(
                                        year,
                                        month,
                                        day
                                );
                            }

                        } catch (Exception ignored) {

                            // Use today's date if parsing fails.
                        }
                    }


                    DatePickerDialog picker =
                            new DatePickerDialog(

                                    this,

                                    (view,
                                     year,
                                     month,
                                     day) -> {

                                        String date =
                                                String.format(
                                                        Locale.US,
                                                        "%04d-%02d-%02d",
                                                        year,
                                                        month + 1,
                                                        day
                                                );


                                        etNextBillingDate
                                                .setText(
                                                        date
                                                );
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


                    picker.show();
                }
        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnUpdate.setOnClickListener(
                v -> updateSubscription()
        );


        btnDelete.setOnClickListener(
                v -> deleteSubscription()
        );


        btnCancel.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private boolean validateInputs() {

        String serviceName =
                getText(
                        etServiceName
                );


        String amountText =
                getText(
                        etAmount
                );


        String currency =
                safeString(
                        actCurrency
                                .getText()
                                .toString()
                );


        String billingCycle =
                safeString(
                        actBillingCycle
                                .getText()
                                .toString()
                );


        String nextBillingDate =
                getText(
                        etNextBillingDate
                );


        // =====================================================
        // SERVICE
        // =====================================================

        if (serviceName.isEmpty()) {

            etServiceName.setError(
                    "Service name is required"
            );

            etServiceName.requestFocus();

            return false;
        }


        // =====================================================
        // AMOUNT
        // =====================================================

        if (amountText.isEmpty()) {

            etAmount.setError(
                    "Amount is required"
            );

            etAmount.requestFocus();

            return false;
        }


        try {

            double amount =
                    Double.parseDouble(
                            amountText
                    );


            if (amount <= 0) {

                etAmount.setError(
                        "Amount must be greater than 0"
                );

                etAmount.requestFocus();

                return false;
            }


        } catch (NumberFormatException e) {

            etAmount.setError(
                    "Enter a valid amount"
            );

            etAmount.requestFocus();

            return false;
        }


        // =====================================================
        // CURRENCY
        // =====================================================

        if (currency.isEmpty()) {

            actCurrency.setError(
                    "Please select currency"
            );

            actCurrency.requestFocus();

            return false;
        }


        // =====================================================
        // BILLING CYCLE
        // =====================================================

        if (billingCycle.isEmpty()) {

            actBillingCycle.setError(
                    "Please select billing cycle"
            );

            actBillingCycle.requestFocus();

            return false;
        }


        // =====================================================
        // DATE
        // =====================================================

        if (nextBillingDate.isEmpty()) {

            etNextBillingDate.setError(
                    "Next billing date is required"
            );

            etNextBillingDate.requestFocus();

            return false;
        }


        return true;
    }


    // =========================================================
    // UPDATE SUBSCRIPTION
    // =========================================================

    private void updateSubscription() {

        if (!validateInputs()) {

            return;
        }


        if (subscriptionId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid subscription.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // SERVICE NAME
        // =====================================================

        String serviceName =
                getText(
                        etServiceName
                );


        // =====================================================
        // AMOUNT
        // =====================================================

        double amount;


        try {

            amount =
                    Double.parseDouble(
                            getText(
                                    etAmount
                            )
                    );


        } catch (NumberFormatException e) {

            etAmount.setError(
                    "Enter a valid amount"
            );

            return;
        }


        // =====================================================
        // CURRENCY
        // =====================================================

        String currency =
                normalizeCurrency(
                        actCurrency
                                .getText()
                                .toString()
                );


        // =====================================================
        // BILLING CYCLE
        // =====================================================

        String billingCycle =
                safeString(
                        actBillingCycle
                                .getText()
                                .toString()
                );


        // =====================================================
        // NEXT BILLING DATE
        // =====================================================

        String nextBillingDate =
                getText(
                        etNextBillingDate
                );


        // =====================================================
        // UPDATE DATABASE
        // =====================================================

        int rowsAffected =
                databaseHelper
                        .updateSubscription(

                                subscriptionId,

                                serviceName,

                                amount,

                                currency,

                                billingCycle,

                                nextBillingDate
                        );


        // =====================================================
        // RESULT
        // =====================================================

        if (rowsAffected > 0) {

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "SUBSCRIPTION UPDATED"
            );

            System.out.println(
                    "ID: "
                            + subscriptionId
            );

            System.out.println(
                    "SERVICE: "
                            + serviceName
            );

            System.out.println(
                    "AMOUNT: "
                            + currency
                            + " "
                            + amount
            );

            System.out.println(
                    "CYCLE: "
                            + billingCycle
            );

            System.out.println(
                    "NEXT BILLING: "
                            + nextBillingDate
            );

            System.out.println(
                    "========================================"
            );


            Toast.makeText(
                    this,
                    "Subscription updated successfully.",
                    Toast.LENGTH_SHORT
            ).show();


            finish();


        } else {

            Toast.makeText(
                    this,
                    "Failed to update subscription.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =========================================================
    // DELETE SUBSCRIPTION
    // =========================================================

    private void deleteSubscription() {

        if (subscriptionId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid subscription.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        new MaterialAlertDialogBuilder(
                this
        )

                .setTitle(
                        "Delete Subscription"
                )

                .setMessage(
                        "Are you sure you want to delete this subscription?"
                )

                .setPositiveButton(
                        "Delete",

                        (dialog, which) -> {

                            int rowsDeleted =
                                    databaseHelper
                                            .deleteSubscription(
                                                    subscriptionId
                                            );


                            if (rowsDeleted > 0) {

                                Toast.makeText(
                                        this,
                                        "Subscription deleted.",
                                        Toast.LENGTH_SHORT
                                ).show();


                                finish();


                            } else {

                                Toast.makeText(
                                        this,
                                        "Failed to delete subscription.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .show();
    }


    // =========================================================
    // NORMALIZE CURRENCY
    // =========================================================

    private String normalizeCurrency(
            String value
    ) {

        String currency =
                safeString(
                        value
                );


        if (currency.isEmpty()) {

            return "";
        }


        String upper =
                currency.toUpperCase(
                        Locale.US
                );


        if (upper.equals("RS") ||
                upper.equals("RS.")) {

            return "LKR";
        }


        if (currency.equals("€")) {

            return "EUR";
        }


        if (currency.equals("£")) {

            return "GBP";
        }


        if (currency.equals("₹")) {

            return "INR";
        }


        /*
         * Do not automatically convert "$" to USD.
         *
         * For manual edit, user should select
         * the exact ISO currency code.
         */

        return upper;
    }


    // =========================================================
    // GET EDIT TEXT
    // =========================================================

    private String getText(
            TextInputEditText editText
    ) {

        if (editText == null ||
                editText.getText() == null) {

            return "";
        }


        return editText
                .getText()
                .toString()
                .trim();
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safeString(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value.trim();
    }
}