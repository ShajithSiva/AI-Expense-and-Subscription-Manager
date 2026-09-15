package com.example.aiexpensemanagementapplication.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.card.MaterialCardView;

public class HelpSupportActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private MaterialCardView cardGettingStarted;
    private MaterialCardView cardExpenses;
    private MaterialCardView cardIncome;
    private MaterialCardView cardBudget;
    private MaterialCardView cardSubscriptions;
    private MaterialCardView cardFamily;
    private MaterialCardView cardAIAdvisor;
    private MaterialCardView cardNotifications;
    private MaterialCardView cardTroubleshooting;

    private TextView answerGettingStarted;
    private TextView answerExpenses;
    private TextView answerIncome;
    private TextView answerBudget;
    private TextView answerSubscriptions;
    private TextView answerFamily;
    private TextView answerAIAdvisor;
    private TextView answerNotifications;
    private TextView answerTroubleshooting;

    private ImageView iconGettingStarted;
    private ImageView iconExpenses;
    private ImageView iconIncome;
    private ImageView iconBudget;
    private ImageView iconSubscriptions;
    private ImageView iconFamily;
    private ImageView iconAIAdvisor;
    private ImageView iconNotifications;
    private ImageView iconTroubleshooting;

    private View layoutSupportEmail;
    private View layoutSupportPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help_support);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        cardGettingStarted = findViewById(R.id.cardGettingStarted);
        cardExpenses = findViewById(R.id.cardExpenses);
        cardIncome = findViewById(R.id.cardIncome);
        cardBudget = findViewById(R.id.cardBudget);
        cardSubscriptions = findViewById(R.id.cardSubscriptions);
        cardFamily = findViewById(R.id.cardFamily);
        cardAIAdvisor = findViewById(R.id.cardAIAdvisor);
        cardNotifications = findViewById(R.id.cardNotifications);
        cardTroubleshooting = findViewById(R.id.cardTroubleshooting);

        answerGettingStarted = findViewById(R.id.answerGettingStarted);
        answerExpenses = findViewById(R.id.answerExpenses);
        answerIncome = findViewById(R.id.answerIncome);
        answerBudget = findViewById(R.id.answerBudget);
        answerSubscriptions = findViewById(R.id.answerSubscriptions);
        answerFamily = findViewById(R.id.answerFamily);
        answerAIAdvisor = findViewById(R.id.answerAIAdvisor);
        answerNotifications = findViewById(R.id.answerNotifications);
        answerTroubleshooting = findViewById(R.id.answerTroubleshooting);

        iconGettingStarted = findViewById(R.id.iconGettingStarted);
        iconExpenses = findViewById(R.id.iconExpenses);
        iconIncome = findViewById(R.id.iconIncome);
        iconBudget = findViewById(R.id.iconBudget);
        iconSubscriptions = findViewById(R.id.iconSubscriptions);
        iconFamily = findViewById(R.id.iconFamily);
        iconAIAdvisor = findViewById(R.id.iconAIAdvisor);
        iconNotifications = findViewById(R.id.iconNotifications);
        iconTroubleshooting = findViewById(R.id.iconTroubleshooting);

        layoutSupportEmail = findViewById(R.id.layoutSupportEmail);
        layoutSupportPhone = findViewById(R.id.layoutSupportPhone);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        setupFaq(
                cardGettingStarted,
                answerGettingStarted,
                iconGettingStarted
        );

        setupFaq(
                cardExpenses,
                answerExpenses,
                iconExpenses
        );

        setupFaq(
                cardIncome,
                answerIncome,
                iconIncome
        );

        setupFaq(
                cardBudget,
                answerBudget,
                iconBudget
        );

        setupFaq(
                cardSubscriptions,
                answerSubscriptions,
                iconSubscriptions
        );

        setupFaq(
                cardFamily,
                answerFamily,
                iconFamily
        );

        setupFaq(
                cardAIAdvisor,
                answerAIAdvisor,
                iconAIAdvisor
        );

        setupFaq(
                cardNotifications,
                answerNotifications,
                iconNotifications
        );

        setupFaq(
                cardTroubleshooting,
                answerTroubleshooting,
                iconTroubleshooting
        );

        layoutSupportEmail.setOnClickListener(v ->
                openSupportEmail()
        );

        layoutSupportPhone.setOnClickListener(v ->
                callSupport()
        );
    }

    private void setupFaq(
            MaterialCardView card,
            TextView answer,
            ImageView icon
    ) {

        card.setOnClickListener(v -> {

            boolean shouldExpand =
                    answer.getVisibility() != View.VISIBLE;

            answer.setVisibility(
                    shouldExpand
                            ? View.VISIBLE
                            : View.GONE
            );

            icon.setRotation(
                    shouldExpand ? 180f : 0f
            );
        });
    }

    private void openSupportEmail() {

        Intent emailIntent = new Intent(
                Intent.ACTION_SENDTO
        );

        emailIntent.setData(
                Uri.parse("mailto:Expensevalut@gmail.com")
        );

        emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "ExpenseVault Support Request"
        );

        try {

            startActivity(
                    Intent.createChooser(
                            emailIntent,
                            "Contact Support"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "No email application found.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void callSupport() {

        Intent phoneIntent = new Intent(
                Intent.ACTION_DIAL
        );

        phoneIntent.setData(
                Uri.parse("tel:0771234567")
        );

        try {

            startActivity(phoneIntent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open phone application.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}