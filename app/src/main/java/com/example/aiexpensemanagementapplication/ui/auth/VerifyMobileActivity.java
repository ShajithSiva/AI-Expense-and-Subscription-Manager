package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aiexpensemanagementapplication.R;



public class VerifyMobileActivity extends AppCompatActivity {

    EditText etPhoneNumber;
    Button btnSendOtp;

    FirebaseAuth mAuth;
    String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile);

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        mAuth = FirebaseAuth.getInstance();

        btnSendOtp.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {

        String phone = etPhoneNumber.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {}

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(VerifyMobileActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(String id,
                                       PhoneAuthProvider.ForceResendingToken token) {

                    verificationId = id;

                    Intent intent = new Intent(VerifyMobileActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("verificationId", id);
                    startActivity(intent);
                }
            };
}
