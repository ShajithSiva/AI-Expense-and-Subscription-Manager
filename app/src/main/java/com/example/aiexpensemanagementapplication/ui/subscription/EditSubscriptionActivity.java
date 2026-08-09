package com.example.aiexpensemanagementapplication.ui.subscription;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class EditSubscriptionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    private TextInputEditText etServiceName;
    private TextInputEditText etAmount;
    private TextInputEditText etNextBillingDate;

    private AutoCompleteTextView actBillingCycle;

    private MaterialButton btnUpdate;
    private MaterialButton btnDelete;
    private MaterialButton btnCancel;

    private DatabaseHelper databaseHelper;

    private int subscriptionId;

    private MaterialSwitch switchShareFamily;
    private LinearLayout layoutFamilySelection;
    private Spinner spFamily;
    private int userId = -1;

    private ArrayList<Integer> familyIds =
            new ArrayList<>();

    private ArrayList<String> familyNames =
            new ArrayList<>();

    private int selectedFamilyId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_subscription);

        toolbar=findViewById(R.id.toolbar);

        etServiceName=findViewById(R.id.etServiceName);
        etAmount=findViewById(R.id.etAmount);
        etNextBillingDate=findViewById(R.id.etNextBillingDate);

        actBillingCycle=findViewById(R.id.actBillingCycle);

        btnUpdate=findViewById(R.id.btnUpdate);
        btnDelete=findViewById(R.id.btnDelete);
        btnCancel=findViewById(R.id.btnCancel);

        databaseHelper=new DatabaseHelper(this);

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v->finish());

        String[] cycles={"Monthly","Weekly","Yearly"};

        actBillingCycle.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        cycles));

        setupDatePicker();

        subscriptionId=getIntent().getIntExtra(
                "subscriptionId",-1);

        loadSubscription();

        btnUpdate.setOnClickListener(v->updateSubscription());

        btnDelete.setOnClickListener(v->deleteSubscription());

        btnCancel.setOnClickListener(v->finish());
        switchShareFamily =
                findViewById(R.id.switchShareFamily);

        layoutFamilySelection =
                findViewById(R.id.layoutFamilySelection);

        spFamily =
                findViewById(R.id.spFamily);
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

    private void loadSubscription(){

        Cursor cursor=
                databaseHelper.getSubscription(subscriptionId);

        if(cursor.moveToFirst()){

            etServiceName.setText(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SERVICE_NAME)));

            etAmount.setText(
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.AMOUNT))));

            actBillingCycle.setText(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BILLING_CYCLE)),
                    false);

            etNextBillingDate.setText(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NEXT_BILLING_DATE)));
        }

        cursor.close();
    }

    private void setupDatePicker(){

        etNextBillingDate.setOnClickListener(v->{

            Calendar calendar=Calendar.getInstance();

            new DatePickerDialog(
                    this,
                    (view,year,month,day)->

                            etNextBillingDate.setText(
                                    year+"-"+
                                            String.format("%02d",month+1)+"-"+
                                            String.format("%02d",day)),

                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)

            ).show();

        });

    }

    private void loadFamilies() {

        familyIds.clear();
        familyNames.clear();

        if (userId == -1) {

            Toast.makeText(
                    this,
                    "User not found.",
                    Toast.LENGTH_SHORT
            ).show();

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
    private void updateSubscription(){

        databaseHelper.updateSubscription(

                subscriptionId,

                etServiceName.getText().toString(),

                Double.parseDouble(
                        etAmount.getText().toString()),

                actBillingCycle.getText().toString(),

                etNextBillingDate.getText().toString()

        );

        Toast.makeText(
                this,
                "Subscription Updated",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private void deleteSubscription(){

        new MaterialAlertDialogBuilder(this)

                .setTitle("Delete Subscription")

                .setMessage("Are you sure?")

                .setPositiveButton("Delete",(dialog,which)->{

                    databaseHelper.deleteSubscription(subscriptionId);

                    Toast.makeText(
                            this,
                            "Subscription Deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                })

                .setNegativeButton("Cancel",null)

                .show();

    }

}