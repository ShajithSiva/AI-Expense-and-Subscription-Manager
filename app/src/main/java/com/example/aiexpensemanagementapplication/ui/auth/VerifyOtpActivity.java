package com.example.aiexpensemanagementapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;

import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerify;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private TextView tvPhoneNumber;
    private TextView tvTimer;
    private TextView tvResendOtp;

    private ImageView btnBack;

    private CountDownTimer countDownTimer;

    private String verificationId;
    private String phoneNumber;

    private boolean isVerifying = false;

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

        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvTimer = findViewById(R.id.tvTimer);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber != null) {
            tvPhoneNumber.setText(maskPhoneNumber(phoneNumber));
        }
        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> verifyOtp());

        setupOtpInputs();

        startTimer();

        tvResendOtp.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Resend OTP will be implemented next.",
                    Toast.LENGTH_SHORT
            ).show();

        });
        otp1.requestFocus();

        otp1.postDelayed(() -> {

            InputMethodManager imm =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            if (imm != null) {

                imm.showSoftInput(otp1, InputMethodManager.SHOW_IMPLICIT);
            }

        },200);
    }

    private void verifyOtp() {

        if (isVerifying) {
            return;
        }

        isVerifying = true;

        if (verificationId == null || verificationId.isEmpty()) {
            isVerifying = false;
            Toast.makeText(this,
                    "Verification session expired. Please request a new OTP.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String otp =
                otp1.getText().toString().trim() +
                        otp2.getText().toString().trim() +
                        otp3.getText().toString().trim() +
                        otp4.getText().toString().trim() +
                        otp5.getText().toString().trim() +
                        otp6.getText().toString().trim();

        if (otp.length() != 6) {
            isVerifying = false;
            Toast.makeText(this,
                    "Please enter the complete 6-digit OTP.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            isVerifying = false;
            Toast.makeText(this,
                    "Session expired.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, otp);

        currentUser.linkWithCredential(credential)
                .addOnCompleteListener(task -> {

                    btnVerify.setEnabled(true);

                    if (task.isSuccessful()) {

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("phoneVerified", true);
                        updates.put("phone", phoneNumber);

                        firestore.collection("users")
                                .document(currentUser.getUid())
                                .update(updates)
                                .addOnSuccessListener(unused -> {

                                    isVerifying = false;

                                    Toast.makeText(
                                            VerifyOtpActivity.this,
                                            "Phone number verified successfully.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    Intent intent = new Intent(
                                            VerifyOtpActivity.this,
                                            CreatePasswordActivity.class);

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    startActivity(intent);
                                    finish();

                                })
                                .addOnFailureListener(e -> {

                                    isVerifying = false;

                                    Toast.makeText(
                                            VerifyOtpActivity.this,
                                            e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        isVerifying = false;

                        Exception exception = task.getException();

                        String message = "OTP verification failed.";

                        if (exception instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {

                            message = "This phone number is already linked to another account.";

                        } else if (exception instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {

                            message = "Invalid OTP. Please try again.";

                        } else if (exception != null) {

                            message = exception.getMessage();
                        }

                        Toast.makeText(
                                VerifyOtpActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }

                });
    }

    private void setupOtpInputs() {

        EditText[] otp = {
                otp1,
                otp2,
                otp3,
                otp4,
                otp5,
                otp6
        };

        for (int i = 0; i < otp.length; i++) {

            final int index = i;

            otp[i].addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    // Handle pasting the entire OTP into the first field
                    if (index == 0 && s.length() == 6) {

                        otp1.setText(String.valueOf(s.charAt(0)));
                        otp2.setText(String.valueOf(s.charAt(1)));
                        otp3.setText(String.valueOf(s.charAt(2)));
                        otp4.setText(String.valueOf(s.charAt(3)));
                        otp5.setText(String.valueOf(s.charAt(4)));
                        otp6.setText(String.valueOf(s.charAt(5)));

                        otp6.requestFocus();
                        verifyOtp();
                        return;
                    }

                    if (s.length() == 1) {

                        if (index < otp.length - 1) {

                            otp[index + 1].requestFocus();

                        } else {

                            verifyOtp();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

            });

            otp[i].setOnKeyListener((v, keyCode, event) -> {

                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_DEL &&
                        otp[index].getText().toString().isEmpty()) {

                    if (index > 0) {

                        otp[index - 1].requestFocus();
                        otp[index - 1].setSelection(
                                otp[index - 1].getText().length()
                        );
                    }
                }

                return false;
            });
        }
    }
    private String maskPhoneNumber(String phone) {

        if (phone == null || phone.length() < 7)
            return phone;

        return phone.substring(0, 6)
                + "*****"
                + phone.substring(phone.length() - 3);
    }
    private void startTimer() {

        tvResendOtp.setEnabled(false);

        countDownTimer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                long seconds = millisUntilFinished / 1000;

                tvTimer.setText(String.format("00:%02d", seconds));
            }

            @Override
            public void onFinish() {

                tvTimer.setText("00:00");

                tvResendOtp.setEnabled(true);

                tvResendOtp.setTextColor(
                        getResources().getColor(android.R.color.holo_green_dark)
                );
            }

        }.start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}