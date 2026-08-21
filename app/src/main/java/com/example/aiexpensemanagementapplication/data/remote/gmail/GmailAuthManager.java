package com.example.aiexpensemanagementapplication.data.remote.gmail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

public class GmailAuthManager {

    private final Context context;

    public static final int RC_GMAIL_AUTH = 1001;

    public static final String GMAIL_READONLY_SCOPE =
            "https://www.googleapis.com/auth/gmail.readonly";

    private GoogleSignInAccount signedInAccount;

    public GmailAuthManager(Context context) {
        this.context = context;
    }

    public void connectGmail(Activity activity) {

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                        .requestEmail()
                        .requestScopes(
                                new Scope(GMAIL_READONLY_SCOPE)
                        )
                        .build();

        Intent signInIntent =
                GoogleSignIn.getClient(activity, gso)
                        .getSignInIntent();

        activity.startActivityForResult(
                signInIntent,
                RC_GMAIL_AUTH
        );
    }

    public boolean handleActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        if (requestCode != RC_GMAIL_AUTH) {
            return false;
        }

        if (data == null) {

            Toast.makeText(
                    context,
                    "Gmail connection cancelled.",
                    Toast.LENGTH_SHORT
            ).show();

            return true;
        }

        Task<GoogleSignInAccount> task =
                GoogleSignIn.getSignedInAccountFromIntent(data);

        try {

            signedInAccount =
                    task.getResult(ApiException.class);

            if (signedInAccount != null) {

                Toast.makeText(
                        context,
                        "Gmail connected successfully!",
                        Toast.LENGTH_LONG
                ).show();

                return true;
            }

        } catch (ApiException e) {

            Toast.makeText(
                    context,
                    "Gmail connection failed: "
                            + e.getStatusCode(),
                    Toast.LENGTH_LONG
            ).show();
        }

        return true;
    }

    public GoogleSignInAccount getSignedInAccount() {
        return signedInAccount;
    }

    public boolean isConnected() {
        return signedInAccount != null;
    }
}