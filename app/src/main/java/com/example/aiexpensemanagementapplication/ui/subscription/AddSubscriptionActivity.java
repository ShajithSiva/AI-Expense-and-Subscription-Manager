package com.example.aiexpensemanagementapplication.ui.subscription;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.database.Cursor;
import android.view.View;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class AddSubscriptionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    private TextInputEditText etServiceName;
    private TextInputEditText etAmount;
    private TextInputEditText etNextBillingDate;

    private AutoCompleteTextView actBillingCycle;

    private MaterialButton btnSave;
    private MaterialButton btnCancel;

    private DatabaseHelper databaseHelper;

    private int userId;

    private MaterialSwitch switchShareFamily;
    private LinearLayout layoutFamilySelection;
    private Spinner spFamily;

    private ArrayList<Integer> familyIds =
            new ArrayList<>();

    private ArrayList<String> familyNames =
            new ArrayList<>();

    private int selectedFamilyId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subscription);

        initializeViews();

        setupToolbar();

        databaseHelper = new DatabaseHelper(this);

        initializeUser();

        setupBillingCycle();

        setupDatePicker();

        setupListeners();
    }

    private void initializeViews(){

        toolbar=findViewById(R.id.toolbar);

        etServiceName=findViewById(R.id.etServiceName);

        etAmount=findViewById(R.id.etAmount);

        etNextBillingDate=findViewById(R.id.etNextBillingDate);

        actBillingCycle=findViewById(R.id.actBillingCycle);

        btnSave=findViewById(R.id.btnSave);

        btnCancel=findViewById(R.id.btnCancel);

        switchShareFamily =
                findViewById(R.id.switchShareFamily);

        layoutFamilySelection =
                findViewById(R.id.layoutFamilySelection);

        spFamily =
                findViewById(R.id.spFamily);

    }

    private void setupToolbar(){

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v->finish());

    }

    private void initializeUser(){

        FirebaseUser firebaseUser=
                FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser==null){

            finish();

            return;

        }

        userId=databaseHelper.getUserIdByFirebaseUid(
                firebaseUser.getUid());

    }

    private void setupBillingCycle(){

        String[] cycles={
                "Monthly",
                "Yearly",
                "Weekly"
        };

        ArrayAdapter<String> adapter=
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        cycles
                );

        actBillingCycle.setAdapter(adapter);

    }

    private void setupDatePicker(){

        etNextBillingDate.setOnClickListener(v->{

            Calendar calendar=Calendar.getInstance();

            DatePickerDialog picker=new DatePickerDialog(

                    this,

                    (view,year,month,day)->{

                        String date=

                                year+"-"+

                                        String.format("%02d",month+1)+"-"+

                                        String.format("%02d",day);

                        etNextBillingDate.setText(date);

                    },

                    calendar.get(Calendar.YEAR),

                    calendar.get(Calendar.MONTH),

                    calendar.get(Calendar.DAY_OF_MONTH)

            );

            picker.show();

        });

    }

    private void setupListeners(){

        btnSave.setOnClickListener(
                v -> saveSubscription()
        );

        btnCancel.setOnClickListener(
                v -> finish()
        );


        // =================================================
        // FAMILY SHARING SWITCH
        // =================================================

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

    private void loadFamilies() {

        familyIds.clear();
        familyNames.clear();


        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (firebaseUser == null) {
            return;
        }


        String firebaseUid =
                firebaseUser.getUid();


        int userId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                firebaseUid
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
                                cursor.getInt(
                                        idIndex
                                )
                        );

                        familyNames.add(
                                cursor.getString(
                                        nameIndex
                                )
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
                        android.R.layout
                                .simple_spinner_item,
                        familyNames
                );


        adapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
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
                                    familyIds.get(
                                            position
                                    );
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
    private boolean validateInputs(){

        if(etServiceName.getText().toString().trim().isEmpty()){

            etServiceName.setError("Required");

            return false;

        }

        if(etAmount.getText().toString().trim().isEmpty()){

            etAmount.setError("Required");

            return false;

        }

        if(actBillingCycle.getText().toString().trim().isEmpty()){

            actBillingCycle.setError("Required");

            return false;

        }

        if(etNextBillingDate.getText().toString().trim().isEmpty()){

            etNextBillingDate.setError("Required");

            return false;

        }

        return true;

    }

    private void saveSubscription() {

        // ---------------------------------------------
        // GET INPUT VALUES
        // ---------------------------------------------

        String serviceName =
                etServiceName
                        .getText()
                        .toString()
                        .trim();

        String amountText =
                etAmount
                        .getText()
                        .toString()
                        .trim();

        String billingCycle =
                actBillingCycle
                        .getText()
                        .toString()
                        .trim();

        String nextBillingDate =
                etNextBillingDate
                        .getText()
                        .toString()
                        .trim();


        // ---------------------------------------------
        // VALIDATION
        // ---------------------------------------------

        if (serviceName.isEmpty()) {

            etServiceName.setError(
                    "Enter service name"
            );

            etServiceName.requestFocus();

            return;
        }


        if (amountText.isEmpty()) {

            etAmount.setError(
                    "Enter amount"
            );

            etAmount.requestFocus();

            return;
        }


        if (billingCycle.isEmpty()) {

            actBillingCycle.setError(
                    "Select billing cycle"
            );

            actBillingCycle.requestFocus();

            return;
        }


        if (nextBillingDate.isEmpty()) {

            etNextBillingDate.setError(
                    "Select billing date"
            );

            return;
        }


        double amount;

        try {

            amount =
                    Double.parseDouble(
                            amountText
                    );

        } catch (NumberFormatException e) {

            etAmount.setError(
                    "Enter a valid amount"
            );

            etAmount.requestFocus();

            return;
        }


        if (amount <= 0) {

            etAmount.setError(
                    "Amount must be greater than zero"
            );

            etAmount.requestFocus();

            return;
        }


        // ---------------------------------------------
        // SAVE SUBSCRIPTION
        // ---------------------------------------------

        long subscriptionId =
                databaseHelper.insertSubscription(

                        userId,

                        serviceName,

                        amount,

                        billingCycle,

                        nextBillingDate
                );


        if (subscriptionId == -1) {

            Toast.makeText(
                    this,
                    "Failed to save subscription",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ---------------------------------------------
        // FAMILY SHARING
        // ---------------------------------------------

        if (switchShareFamily.isChecked()) {

            if (selectedFamilyId == -1) {

                Toast.makeText(
                        this,
                        "Please select a family",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            boolean shared =
                    databaseHelper
                            .shareSubscriptionWithFamily(

                                    (int) subscriptionId,

                                    selectedFamilyId,

                                    userId
                            );


            if (!shared) {

                Toast.makeText(
                        this,
                        "Subscription saved, but family sharing failed",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }
        }


        // ---------------------------------------------
        // SUCCESS
        // ---------------------------------------------

        Toast.makeText(
                this,
                switchShareFamily.isChecked()
                        ? "Family subscription added successfully"
                        : "Personal subscription added successfully",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }




}