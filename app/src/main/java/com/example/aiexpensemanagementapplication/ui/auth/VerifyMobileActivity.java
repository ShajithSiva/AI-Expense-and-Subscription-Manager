package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class VerifyMobileActivity extends AppCompatActivity {

    private EditText etPhone;
    private Button btnSendOtp;
    private CountryCodePicker countryCodePicker;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile);

        countryCodePicker = findViewById(R.id.countryCodePicker);
        etPhone = findViewById(R.id.etPhone);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        countryCodePicker.registerCarrierNumberEditText(etPhone);

        mAuth = FirebaseAuth.getInstance();

        btnSendOtp.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this,
                    "Session expired. Please register again.",
                    Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        String phone = etPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            etPhone.setError("Enter your phone number");
            etPhone.requestFocus();
            return;
        }

        if (!countryCodePicker.isValidFullNumber()) {
            Toast.makeText(this,
                    "Enter a valid phone number",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String fullPhoneNumber = countryCodePicker.getFullNumberWithPlus();

        btnSendOtp.setEnabled(false);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(fullPhoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {

                    // Auto verification (optional)
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {

                    btnSendOtp.setEnabled(true);

                    Toast.makeText(
                            VerifyMobileActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {

                    btnSendOtp.setEnabled(true);

                    Toast.makeText(
                            VerifyMobileActivity.this,
                            "OTP sent successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            VerifyMobileActivity.this,
                            VerifyOtpActivity.class);

                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phoneNumber",
                            countryCodePicker.getFullNumberWithPlus());

                    startActivity(intent);
                }
            };
}