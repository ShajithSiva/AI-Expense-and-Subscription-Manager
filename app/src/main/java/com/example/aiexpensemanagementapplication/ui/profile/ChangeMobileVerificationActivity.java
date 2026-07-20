package com.example.aiexpensemanagementapplication.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.View;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class ChangeMobileVerificationActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private TextView tvMobile;

    private TextInputEditText etOtp;

    private CircularProgressIndicator progressBar;
    private MaterialButton btnVerify;

    private TextView tvResend;

    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    private int userId;

    private String name;
    private String newMobile;

    private String verificationId;

    private CountDownTimer countDownTimer;

    private long timeLeft = 60000;

    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_mobile_verification);

        initialize();

        getIntentData();

        listeners();

        sendOtp();

        startResendTimer();
    }

    private void initialize() {

        databaseHelper = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        firestore = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);

        tvMobile = findViewById(R.id.tvMobile);

        btnVerify = findViewById(R.id.btnVerify);

        etOtp = findViewById(R.id.etOtp);

        tvResend = findViewById(R.id.tvResend);

        progressBar = findViewById(R.id.progressBar);

    }

    private String formatPhoneNumber(String mobile) {

        mobile = mobile.trim();

        if (mobile.startsWith("0")) {

            return "+94" + mobile.substring(1);

        }

        if (mobile.startsWith("94")) {

            return "+" + mobile;

        }

        return mobile;
    }

    private void getIntentData() {

        Intent intent = getIntent();

        userId = intent.getIntExtra("USER_ID", -1);

        name = intent.getStringExtra("NAME");

        newMobile = intent.getStringExtra("NEW_MOBILE");

        newMobile = formatPhoneNumber(newMobile);

        tvMobile.setText(newMobile);

        if (databaseHelper.isMobileExists(newMobile)) {

            Toast.makeText(
                    this,
                    "This mobile number is already in use.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }
    }



    private void listeners() {

        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> verifyOtp());

        tvResend.setOnClickListener(v -> resendOtp());

    }

    private void sendOtp() {

        if (!Patterns.PHONE.matcher(newMobile).matches()) {

            Toast.makeText(this,
                    "Invalid mobile number",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(newMobile)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(
                        @NonNull PhoneAuthCredential credential) {

                    signInWithCredential(credential);

                }

                @Override
                public void onVerificationFailed(
                        @NonNull FirebaseException e) {

                    progressBar.setVisibility(View.GONE);

                    btnVerify.setEnabled(true);

                    Toast.makeText(
                            ChangeMobileVerificationActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                }

                @Override
                public void onCodeSent(
                        @NonNull String verification,
                        @NonNull PhoneAuthProvider.ForceResendingToken token) {

                    progressBar.setVisibility(View.GONE);

                    verificationId = verification;

                    resendToken = token;

                    Toast.makeText(
                            ChangeMobileVerificationActivity.this,
                            "OTP Sent Successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                }

            };

    private void verifyOtp() {

        String code = etOtp.getText().toString().trim();

        if (verificationId == null) {

            Toast.makeText(this,
                    "Please wait until OTP is sent.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        if (code.length() != 6) {

            etOtp.setError("Enter valid OTP");

            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        btnVerify.setEnabled(false);

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(
                        verificationId,
                        code);

        signInWithCredential(credential);

    }

    private void signInWithCredential(
            PhoneAuthCredential credential) {

        currentUser.updatePhoneNumber(credential)

                .addOnSuccessListener(unused -> {

                    updateUser();

                })

                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);

                    btnVerify.setEnabled(true);

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });

    }

    private void updateUser() {

        boolean success = databaseHelper.updateUserProfile(
                userId,
                name,
                newMobile);

        if (!success) {

            progressBar.setVisibility(View.GONE);

            Toast.makeText(this,
                    "Database update failed",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        firestore.collection("users")
                .document(currentUser.getUid())
                .update(
                        "fullName", name,
                        "mobileNumber", newMobile
                )
                .addOnSuccessListener(unused -> {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(this,
                            "Profile Updated Successfully",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(
                            this,
                            ProfileActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent);

                    setResult(RESULT_OK);

                    finish();

                })

                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);

                    btnVerify.setEnabled(true);

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });

    }

    private void startResendTimer() {

        tvResend.setEnabled(false);

        countDownTimer = new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                tvResend.setText(
                        "Resend OTP (" +
                                millisUntilFinished / 1000 +
                                "s)");

            }

            @Override
            public void onFinish() {

                tvResend.setEnabled(true);

                tvResend.setText("Resend OTP");

            }

        }.start();

    }

    private void resendOtp(){

        sendOtp();

        startResendTimer();

    }
    @Override
    protected void onDestroy() {

        super.onDestroy();

        if(countDownTimer!=null){

            countDownTimer.cancel();

        }

    }
}

