package com.example.aiexpensemanagementapplication.ui.subscription;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

        btnSave.setOnClickListener(v->saveSubscription());

        btnCancel.setOnClickListener(v->finish());

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

    private void saveSubscription(){

        if(!validateInputs())
            return;

        long result=databaseHelper.insertSubscription(

                userId,

                etServiceName.getText().toString().trim(),

                Double.parseDouble(
                        etAmount.getText().toString()),

                actBillingCycle.getText().toString(),

                etNextBillingDate.getText().toString()

        );

        if(result!=-1){

            Toast.makeText(
                    this,
                    "Subscription Added Successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        }else{

            Toast.makeText(
                    this,
                    "Failed to Save",
                    Toast.LENGTH_SHORT
            ).show();

        }

    }




}