package com.example.aiexpensemanagementapplication.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.aiexpensemanagementapplication.R;

import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChangePasswordActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private MaterialButton btnUpdatePassword;
    private MaterialButton btnCancel;

    private TextView tvForgotPassword;

    private CircularProgressIndicator progressBar;

    private TextInputLayout tilCurrentPassword;
    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private TextView checkMinCharacters;
    private TextView checkUppercase;
    private TextView checkLowercase;
    private TextView checkNumber;
    private TextView checkSpecialCharacter;
    private TextView checkPasswordsMatch;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initializeViews();

        initializeFirebase();

        setListeners();

        setupPasswordValidation();

        setupErrorClearListeners();

        getOnBackPressedDispatcher().addCallback(
                this,
                new androidx.activity.OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {
                        showExitDialog();
                    }
                }
        );
    }

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnCancel = findViewById(R.id.btnCancel);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        progressBar = findViewById(R.id.progressBar);

        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        checkMinCharacters = findViewById(R.id.checkMinCharacters);
        checkUppercase = findViewById(R.id.checkUppercase);
        checkLowercase = findViewById(R.id.checkLowercase);
        checkNumber = findViewById(R.id.checkNumber);
        checkSpecialCharacter = findViewById(R.id.checkSpecialCharacter);
        checkPasswordsMatch = findViewById(R.id.checkPasswordsMatch);

        updatePasswordRequirements();
    }

    private void initializeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        email = currentUser.getEmail();

        if (email == null) {

            Toast.makeText(
                    this,
                    "Password change is only available for email accounts.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }
    }

    private void setListeners() {

        btnBack.setOnClickListener(v -> showExitDialog());

        btnCancel.setOnClickListener(v -> showExitDialog());

        btnUpdatePassword.setOnClickListener(v -> validateInputs());

        tvForgotPassword.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void setupPasswordValidation() {

        TextWatcher watcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordRequirements();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etNewPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);
    }

    private void updatePasswordRequirements() {

        String password = etNewPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        updateRequirement(checkMinCharacters, password.length() >= 8);

        updateRequirement(checkUppercase,
                password.matches(".*[A-Z].*"));

        updateRequirement(checkLowercase,
                password.matches(".*[a-z].*"));

        updateRequirement(checkNumber,
                password.matches(".*\\d.*"));

        updateRequirement(checkSpecialCharacter,
                password.matches(".*[@$!%*?&^#()_+=<>.,:;\\-].*"));

        updateRequirement(checkPasswordsMatch,
                !confirm.isEmpty() && password.equals(confirm));
    }

    private void updateRequirement(TextView view, boolean completed) {

        if (completed) {

            view.setText("✓");
            view.setTextColor(Color.parseColor("#22C55E"));

        } else {

            view.setText("✓");
            view.setTextColor(Color.parseColor("#9CA3AF"));
        }
    }

    private void validateInputs() {

        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            tilCurrentPassword.setError("Current password is required");
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return;
        }

        if (!isValidPassword(newPassword)) {
            tilNewPassword.setError(
                    "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character."
            );
            etNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (currentPassword.equals(newPassword)) {
            tilNewPassword.setError("New password must be different from the current password");
            etNewPassword.requestFocus();
            return;
        }

        reAuthenticateUser(currentPassword, newPassword);
    }

    private boolean isValidPassword(String password) {

        String PASSWORD_PATTERN =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&^#()_+=<>.,:;\\-]).{8,}$";

        return password.matches(PASSWORD_PATTERN);
    }

    private void setupErrorClearListeners() {

        etCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilCurrentPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilNewPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    private void showLoading(boolean loading) {

        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        btnUpdatePassword.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        btnBack.setEnabled(!loading);

        tvForgotPassword.setEnabled(!loading);

        etCurrentPassword.setEnabled(!loading);
        etNewPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
    }

    private void reAuthenticateUser(String currentPassword,
                                    String newPassword) {

        showLoading(true);

        AuthCredential credential =
                EmailAuthProvider.getCredential(
                        email,
                        currentPassword
                );

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        updatePassword(newPassword);

                    } else {

                        showLoading(false);

                        tilCurrentPassword.setError("Current password is incorrect");

                        etCurrentPassword.requestFocus();
                    }

                });

    }

    private void updatePassword(String newPassword) {

        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {

                    showLoading(false);

                    if (task.isSuccessful()) {

                        clearFields();

                        Toast.makeText(
                                this,
                                "Password updated successfully.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                    } else {

                        Exception exception = task.getException();

                        if (exception != null) {

                            Toast.makeText(
                                    this,
                                    exception.getLocalizedMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                        } else {

                            Toast.makeText(
                                    this,
                                    "Unable to update password.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                });
    }

    private void showExitDialog() {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Discard Changes?")
                .setMessage("Your password changes have not been saved.")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Stay", null)
                .show();
    }



    private void sendPasswordResetEmail() {

        if (currentUser == null || email == null) {

            Toast.makeText(
                    this,
                    "Email address not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        showLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    showLoading(false);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                ChangePasswordActivity.this,
                                "Password reset email sent to "
                                        + email,
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                ChangePasswordActivity.this,
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unable to send reset email.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                });
    }


    private void clearFields() {

        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");

        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

}