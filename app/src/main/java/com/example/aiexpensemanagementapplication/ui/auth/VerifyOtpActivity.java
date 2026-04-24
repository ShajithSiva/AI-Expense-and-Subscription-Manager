package com.example.aiexpensemanagementapplication.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import com.example.aiexpensemanagementapplication.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.FirebaseAuth;


public class VerifyOtpActivity extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    Button btnVerify;

    FirebaseAuth mAuth;
    String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);

        btnVerify = findViewById(R.id.btnVerifyOtp);

        mAuth = FirebaseAuth.getInstance();

        verificationId = getIntent().getStringExtra("verificationId");

        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {

        String otp = otp1.getText().toString() +
                otp2.getText().toString() +
                otp3.getText().toString() +
                otp4.getText().toString() +
                otp5.getText().toString() +
                otp6.getText().toString();

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, otp);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        startActivity(new Intent(this, LoginActivity.class));
                        finishAffinity();

                    } else {
                        Toast.makeText(this,
                                "Invalid OTP",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
