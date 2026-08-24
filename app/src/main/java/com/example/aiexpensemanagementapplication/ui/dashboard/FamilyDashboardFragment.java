package com.example.aiexpensemanagementapplication.ui.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.data.remote.FamilyFirestoreService;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseModel;
import com.example.aiexpensemanagementapplication.ui.family.CreateFamilyActivity;
import com.example.aiexpensemanagementapplication.ui.family.InviteMemberActivity;
import com.example.aiexpensemanagementapplication.ui.family.JoinFamilyActivity;
import com.example.aiexpensemanagementapplication.ui.family.SetFamilyBudgetActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FamilyDashboardFragment extends Fragment {

    // =====================================================
    // DATABASE
    // =====================================================

    private DatabaseHelper databaseHelper;

    private FamilyFirestoreService familyFirestoreService;

    private ListenerRegistration familyMembershipListener;
    private boolean familyAccessRevoked = false;


    // =====================================================
    // CURRENT USER / SELECTED FAMILY
    // =====================================================

    private int currentUserId = -1;
    private int selectedFamilyId = -1;

    private boolean userHasFamily = false;

    private String selectedFamilyRole = null;


    // =====================================================
    // FAMILY LIST
    // =====================================================

    private final List<Integer> familyIds = new ArrayList<>();
    private final List<String> familyNames = new ArrayList<>();
    private final List<String> familyRoles = new ArrayList<>();


    // =====================================================
    // NO FAMILY SCREEN
    // =====================================================

    private MaterialCardView cardCreateFamily;
    private MaterialCardView cardJoinFamily;


    // =====================================================
    // FAMILY DASHBOARD HEADER
    // =====================================================

    private View layoutFamilySelector;

    private TextView tvFamilyName;
    private TextView tvMemberCount;

    private LinearLayout familyMembersContainer;
    private TextView tvNoMembers;

    private ImageButton btnFamilyOptions;
    private MaterialButton btnInviteMember;


    // =====================================================
    // FAMILY EXPENSE UI
    // =====================================================

    private TextView tvFamilySpending;

    private TextView tvFamilySharedIncome;

    private LinearLayout familyTransactionsContainer;

    private PieChart familyPieChart;
    private TextView tvNoCategoryData;

    private MaterialButton btnSetFamilyBudget;
    private TextView tvFamilyBudgetRemaining;
    private ProgressBar progressFamilyBudget;
    private TextView tvBudgetAmount;
    private TextView tvBudgetPercentage;

    private TextView tvAIInsight;

    private TextView tvViewSubscriptions;
    private TextView tvViewTransactions;

    private boolean showAllSubscriptions = false;
    private boolean showAllTransactions = false;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FamilyDashboardFragment() {
        // Required empty constructor

    }


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        databaseHelper =
                new DatabaseHelper(requireContext());

        familyFirestoreService =
                new FamilyFirestoreService();

        loadCurrentUser();

        loadUserFamilies();
    }


    // =====================================================
    // LOAD CURRENT USER
    // =====================================================

    private void loadCurrentUser() {

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {

            currentUserId = -1;

            return;
        }

        currentUserId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                firebaseUser.getUid()
                        );
    }


    // =====================================================
    // LOAD USER FAMILIES
    // =====================================================

    private void loadUserFamilies() {

        int previousSelectedFamilyId =
                selectedFamilyId;

        familyIds.clear();
        familyNames.clear();
        familyRoles.clear();

        selectedFamilyRole = null;
        userHasFamily = false;


        // -------------------------------------------------
        // USER CHECK
        // -------------------------------------------------

        if (currentUserId == -1) {

            selectedFamilyId = -1;

            return;
        }


        // -------------------------------------------------
        // LOAD FAMILIES
        // -------------------------------------------------

        Cursor cursor =
                databaseHelper
                        .getFamiliesForUser(
                                currentUserId
                        );

        if (cursor == null) {

            selectedFamilyId = -1;

            return;
        }


        try {

            int familyIdIndex =
                    cursor.getColumnIndex(
                            "FamilyID"
                    );

            int familyNameIndex =
                    cursor.getColumnIndex(
                            "FamilyName"
                    );

            int familyRoleIndex =
                    cursor.getColumnIndex(
                            "Role"
                    );


            if (familyIdIndex == -1 ||
                    familyNameIndex == -1 ||
                    familyRoleIndex == -1) {

                selectedFamilyId = -1;

                return;
            }


            while (cursor.moveToNext()) {

                int familyId =
                        cursor.getInt(
                                familyIdIndex
                        );

                String familyName =
                        cursor.getString(
                                familyNameIndex
                        );

                String familyRole =
                        cursor.getString(
                                familyRoleIndex
                        );


                familyIds.add(familyId);
                familyNames.add(familyName);
                familyRoles.add(familyRole);
            }

        } finally {

            cursor.close();
        }


        // -------------------------------------------------
        // FAMILY STATUS
        // -------------------------------------------------

        userHasFamily =
                !familyIds.isEmpty();


        if (!userHasFamily) {

            selectedFamilyId = -1;
            selectedFamilyRole = null;

            return;
        }


        // -------------------------------------------------
        // KEEP PREVIOUS FAMILY SELECTION
        // -------------------------------------------------

        int previousIndex =
                familyIds.indexOf(
                        previousSelectedFamilyId
                );


        if (previousIndex != -1) {

            selectedFamilyId =
                    familyIds.get(
                            previousIndex
                    );

            selectedFamilyRole =
                    familyRoles.get(
                            previousIndex
                    );

        } else {

            selectedFamilyId =
                    familyIds.get(0);

            selectedFamilyRole =
                    familyRoles.get(0);
        }
    }


    // =====================================================
    // CREATE VIEW
    // =====================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        if (userHasFamily) {

            return inflater.inflate(
                    R.layout.fragment_family_dashboard,
                    container,
                    false
            );

        } else {

            return inflater.inflate(
                    R.layout.view_no_family,
                    container,
                    false
            );
        }
    }


    // =====================================================
    // VIEW CREATED
    // =====================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );


        if (userHasFamily) {

            setupFamilyDashboard(view);

        } else {

            setupNoFamilyScreen(view);
        }
    }


    // =====================================================
    // NO FAMILY SCREEN
    // =====================================================

    private void setupNoFamilyScreen(View view) {

        cardCreateFamily =
                view.findViewById(
                        R.id.cardCreateFamily
                );

        cardJoinFamily =
                view.findViewById(
                        R.id.cardJoinFamily
                );


        if (cardCreateFamily != null) {

            cardCreateFamily.setOnClickListener(
                    v -> openCreateFamilyActivity()
            );
        }


        if (cardJoinFamily != null) {

            cardJoinFamily.setOnClickListener(
                    v -> openJoinFamilyActivity()
            );
        }
    }


    // =====================================================
    // SETUP FAMILY DASHBOARD
    // =====================================================

    private void setupFamilyDashboard(View view) {

        // -------------------------------------------------
        // HEADER
        // -------------------------------------------------

        layoutFamilySelector =
                view.findViewById(
                        R.id.layoutFamilySelector
                );

        tvFamilyName =
                view.findViewById(
                        R.id.tvFamilyName
                );

        tvMemberCount =
                view.findViewById(
                        R.id.tvMemberCount
                );

        familyMembersContainer =
                view.findViewById(
                        R.id.familyMembersContainer
                );

        tvNoMembers =
                view.findViewById(
                        R.id.tvNoMembers
                );

        btnFamilyOptions =
                view.findViewById(
                        R.id.btnFamilyOptions
                );
        tvFamilySharedIncome =
                view.findViewById(
                        R.id.tvFamilySharedIncome
                );

        btnInviteMember =
                view.findViewById(
                        R.id.btnInviteMember
                );
        btnSetFamilyBudget =
                view.findViewById(R.id.btnSetFamilyBudget);

        if (btnSetFamilyBudget != null) {

            btnSetFamilyBudget.setOnClickListener(v -> {

                if (!isPrimaryUser()) {

                    showPrimaryOnlyAlert(
                            "Only the Family Head can manage the family budget."
                    );

                    return;
                }

                Intent intent = new Intent(
                        requireContext(),
                        SetFamilyBudgetActivity.class
                );

                intent.putExtra(
                        "familyId",
                        selectedFamilyId
                );

                startActivity(intent);
            });
        }


        // -------------------------------------------------
        // FAMILY SUBSCRIPTIONS
        // -------------------------------------------------

        tvSharedSubscriptionTotal =
                view.findViewById(
                        R.id.tvSharedSubscriptionTotal
                );

        sharedSubscriptionsContainer =
                view.findViewById(
                        R.id.sharedSubscriptionsContainer
                );

        tvNoSharedSubscriptions =
                view.findViewById(
                        R.id.tvNoSharedSubscriptions
                );






        // -------------------------------------------------
        // FAMILY EXPENSE UI
        // -------------------------------------------------

        tvFamilySpending =
                view.findViewById(
                        R.id.tvFamilySpending
                );

        tvAIInsight =
                view.findViewById(
                        R.id.tvAIInsight
                );

        familyTransactionsContainer =
                view.findViewById(
                        R.id.familyTransactionsContainer
                );

        tvViewSubscriptions =
                view.findViewById(R.id.tvViewSubscriptions);

        tvViewTransactions =
                view.findViewById(R.id.tvViewTransactions);

        // -------------------------------------------------
// VIEW ALL SUBSCRIPTIONS
// -------------------------------------------------

        if (tvViewSubscriptions != null) {

            tvViewSubscriptions.setOnClickListener(v -> {

                showAllSubscriptions = !showAllSubscriptions;

                tvViewSubscriptions.setText(
                        showAllSubscriptions
                                ? "Show Less"
                                : "View All"
                );

                loadSharedSubscriptions();
            });
        }


// -------------------------------------------------
// VIEW ALL TRANSACTIONS
// -------------------------------------------------

        if (tvViewTransactions != null) {

            tvViewTransactions.setOnClickListener(v -> {

                showAllTransactions = !showAllTransactions;

                tvViewTransactions.setText(
                        showAllTransactions
                                ? "Show Less"
                                : "View All"
                );

                loadFamilyTransactionsOnly();
            });
        }


        familyPieChart =
                view.findViewById(R.id.familyPieChart);

        tvNoCategoryData =
                view.findViewById(R.id.tvNoCategoryData);


        tvFamilyBudgetRemaining =
                view.findViewById(R.id.tvFamilyBudgetRemaining);

        progressFamilyBudget =
                view.findViewById(R.id.progressFamilyBudget);

        tvBudgetAmount =
                view.findViewById(R.id.tvBudgetAmount);

        tvBudgetPercentage =
                view.findViewById(R.id.tvBudgetPercentage);


        // -------------------------------------------------
        // FAMILY SELECTOR
        // -------------------------------------------------

        if (layoutFamilySelector != null) {

            layoutFamilySelector.setOnClickListener(
                    v -> showFamilySelectorMenu()
            );
        }


        // -------------------------------------------------
        // OPTIONS
        // -------------------------------------------------

        if (btnFamilyOptions != null) {

            btnFamilyOptions.setOnClickListener(
                    v -> showFamilyOptionsMenu()
            );
        }


        // -------------------------------------------------
        // INVITE MEMBER
        // -------------------------------------------------

        if (btnInviteMember != null) {

            btnInviteMember.setOnClickListener(
                    v -> openInviteMemberActivity()
            );
        }


        // -------------------------------------------------
        // LOAD DASHBOARD
        // -------------------------------------------------

        loadSelectedFamilyDashboard();
    }


    // =====================================================
    // LOAD SELECTED FAMILY DASHBOARD
    // =====================================================

    private void loadSelectedFamilyDashboard() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }

        if (familyAccessRevoked) {
            return;
        }

        // Start monitoring family membership
        startFamilyMembershipListener();

        // FAMILY NAME


        // -------------------------------------------------
        // FAMILY NAME
        // -------------------------------------------------

        String familyName =
                databaseHelper
                        .getFamilyName(
                                selectedFamilyId
                        );


        if (familyName == null ||
                familyName.trim().isEmpty()) {

            familyName = "Family";
        }


        if (tvFamilyName != null) {

            tvFamilyName.setText(
                    familyName
            );
        }


        // -------------------------------------------------
        // USER ROLE
        // -------------------------------------------------

        selectedFamilyRole =
                databaseHelper
                        .getFamilyRole(
                                currentUserId,
                                selectedFamilyId
                        );



        // -------------------------------------------------
        // LOAD FIRESTORE FAMILY MEMBERS
        // -------------------------------------------------

        loadFamilyMembers();

        // -------------------------------------------------
        // ROLE UI
        // -------------------------------------------------

        updateRoleBasedUI();


        // -------------------------------------------------
        // EXPENSES + GRAPH
        // -------------------------------------------------

        loadSharedExpenses();

        // -------------------------------------------------
        // SUBSCRIPTIONS
        // -------------------------------------------------

        loadSharedSubscriptions();
    }



    // =====================================================
    // LOAD SHARED FAMILY EXPENSES + INCOME FROM FIRESTORE
    // =====================================================

    private void loadSharedExpenses() {

        if (!canAccessFamilyDashboard()) {
            return;
        }

        String firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        selectedFamilyId
                );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Family Firestore ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        familyFirestoreService.getFamilyExpenses(
                firestoreFamilyId,
                new FamilyFirestoreService.FamilyExpensesCallback() {

                    @Override
                    public void onSuccess(
                            ArrayList<FamilyFirestoreService.FamilyExpenseData>
                                    familyExpenses
                    ) {

                        if (!isAdded()
                                || getView() == null
                                || familyAccessRevoked) {

                            return;
                        }
                        // -----------------------------------------
                        // TOTAL FAMILY EXPENSE
                        // -----------------------------------------

                        double totalExpense = 0.0;

                        if (familyExpenses != null) {

                            for (
                                    FamilyFirestoreService.FamilyExpenseData expense
                                    : familyExpenses
                            ) {

                                if (expense != null) {

                                    totalExpense +=
                                            expense.getAmount();
                                }
                            }
                        }



// IMPORTANT:
// Java inner callbacks can only access final/effectively-final variables.
                        final double finalTotalExpense = totalExpense;

                        // -----------------------------------------
                        // UPDATE TOTAL SPENDING
                        // -----------------------------------------

                        if (tvFamilySpending != null) {

                            tvFamilySpending.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "Rs %,.2f",
                                            totalExpense
                                    )
                            );
                        }

                        // -----------------------------------------
                        // CONVERT EXPENSE DATA
                        // -----------------------------------------

                        ArrayList<ExpenseModel> expenses =
                                convertFamilyExpensesToModels(
                                        familyExpenses
                                );

                        // -----------------------------------------
                        // CATEGORY GRAPH
                        // -----------------------------------------

                        loadFamilyCategorySpending(
                                expenses
                        );

                        // -----------------------------------------
                        // RECENT TRANSACTIONS
                        // -----------------------------------------

                        loadRecentFamilyTransactions(
                                expenses
                        );

                        // -----------------------------------------
                        // FAMILY BUDGET
                        // -----------------------------------------

                        loadFamilyBudget();

                        // -----------------------------------------
                        // LOAD FAMILY INCOME
                        // -----------------------------------------

                        familyFirestoreService.getFamilyIncome(
                                firestoreFamilyId,
                                new FamilyFirestoreService.FamilyIncomeListCallback() {

                                    @Override
                                    public void onSuccess(
                                            ArrayList<FamilyFirestoreService.FamilyIncomeData>
                                                    familyIncomes
                                    ) {

                                        if (!isAdded()
                                                || getView() == null
                                                || familyAccessRevoked) {

                                            return;
                                        }

                                        // -----------------------------
                                        // TOTAL FAMILY INCOME
                                        // -----------------------------

                                        double totalIncome = 0.0;

                                        if (familyIncomes != null) {

                                            for (
                                                    FamilyFirestoreService.FamilyIncomeData income
                                                    : familyIncomes
                                            ) {

                                                if (income != null) {

                                                    totalIncome +=
                                                            income.getAmount();
                                                }
                                            }
                                        }

                                        // -----------------------------
                                        // UPDATE SHARED INCOME
                                        // -----------------------------

                                        if (tvFamilySharedIncome != null) {

                                            tvFamilySharedIncome.setText(
                                                    String.format(
                                                            Locale.getDefault(),
                                                            "Rs %,.2f",
                                                            totalIncome
                                                    )
                                            );
                                        }

                                        // -----------------------------
                                        // AI INSIGHT
                                        // -----------------------------

                                        loadFamilyAIInsight(
                                                totalIncome,
                                                finalTotalExpense,
                                                expenses
                                        );
                                    }

                                    @Override
                                    public void onFailure(
                                            String message
                                    ) {

                                        if (!isAdded()) {
                                            return;
                                        }

                                        if (tvFamilySharedIncome != null) {

                                            tvFamilySharedIncome.setText(
                                                    "Rs 0.00"
                                            );
                                        }

                                        Toast.makeText(
                                                requireContext(),
                                                "Failed to load family income: "
                                                        + message,
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Failed to load family expenses: "
                                        + message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // =====================================================
// LOAD SHARED FAMILY SUBSCRIPTIONS
// =====================================================

    private void loadSharedSubscriptions() {

        if (!canAccessFamilyDashboard()) {
            return;
        }


        // -------------------------------------------------
        // GET FIRESTORE FAMILY ID
        // -------------------------------------------------

        String firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        selectedFamilyId
                );


        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            if (tvSharedSubscriptionTotal != null) {

                tvSharedSubscriptionTotal.setText(
                        "Rs 0.00"
                );
            }

            showNoSharedSubscriptions();

            return;
        }


        // -------------------------------------------------
        // GET SUBSCRIPTIONS FROM FIRESTORE
        // -------------------------------------------------

        familyFirestoreService.getFamilySubscriptions(

                firestoreFamilyId,

                new FamilyFirestoreService
                        .FamilySubscriptionsCallback() {

                    @Override
                    public void onSuccess(
                            ArrayList<FamilyFirestoreService.FamilySubscriptionData>
                                    subscriptions
                    ){

                        if (!isAdded()
                                || getView() == null
                                || familyAccessRevoked) {

                            return;
                        }


                        // -----------------------------------------
                        // TOTAL SUBSCRIPTION AMOUNT
                        // -----------------------------------------

                        double total = 0.0;


                        if (subscriptions != null) {

                            for (
                                    FamilyFirestoreService.FamilySubscriptionData subscription
                                    : subscriptions
                            ) {

                                if (subscription != null) {

                                    total +=
                                            subscription.getAmount();
                                }
                            }
                        }


                        // -----------------------------------------
                        // UPDATE TOTAL
                        // -----------------------------------------

                        if (tvSharedSubscriptionTotal != null) {

                            tvSharedSubscriptionTotal.setText(

                                    String.format(
                                            Locale.getDefault(),
                                            "Rs %,.2f",
                                            total
                                    )
                            );
                        }


                        // -----------------------------------------
                        // DISPLAY SUBSCRIPTIONS
                        // -----------------------------------------

                        displaySharedSubscriptions(
                                subscriptions
                        );
                    }


                    @Override
                    public void onFailure(
                            String message
                    ) {

                        if (!isAdded()) {
                            return;
                        }


                        if (tvSharedSubscriptionTotal != null) {

                            tvSharedSubscriptionTotal.setText(
                                    "Rs 0.00"
                            );
                        }


                        showNoSharedSubscriptions();


                        Toast.makeText(
                                requireContext(),
                                "Failed to load family subscriptions: "
                                        + message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =====================================================
// DISPLAY SHARED SUBSCRIPTIONS
// =====================================================

    private void displaySharedSubscriptions(
            ArrayList<FamilyFirestoreService.FamilySubscriptionData>
                    subscriptions
    ) {

        if (sharedSubscriptionsContainer == null) {
            return;
        }


        // -------------------------------------------------
        // CLEAR OLD DATA
        // -------------------------------------------------

        sharedSubscriptionsContainer.removeAllViews();


        // -------------------------------------------------
        // EMPTY
        // -------------------------------------------------

        if (subscriptions == null ||
                subscriptions.isEmpty()) {

            showNoSharedSubscriptions();

            return;
        }


        // -------------------------------------------------
        // HIDE EMPTY MESSAGE
        // -------------------------------------------------

        if (tvNoSharedSubscriptions != null) {

            tvNoSharedSubscriptions.setVisibility(
                    View.GONE
            );
        }


        // -------------------------------------------------
        // DISPLAY MAX 5
        // -------------------------------------------------

        int displayCount =
                showAllSubscriptions
                        ? subscriptions.size()
                        : Math.min(subscriptions.size(), 1
                );


        for (int i = 0;
             i < displayCount;
             i++) {

            FamilyFirestoreService
                    .FamilySubscriptionData subscription =
                    subscriptions.get(i);


            if (subscription == null) {
                continue;
            }


            addSubscriptionRow(
                    subscription,
                    i < displayCount - 1
            );
        }
    }

    // =====================================================
// ADD SUBSCRIPTION ROW
// =====================================================

    private void addSubscriptionRow(
            FamilyFirestoreService.FamilySubscriptionData subscription,
            boolean showDivider
    ) {

        if (subscription == null ||
                sharedSubscriptionsContainer == null) {

            return;
        }


        // -------------------------------------------------
        // MAIN ROW
        // -------------------------------------------------

        LinearLayout row =
                new LinearLayout(
                        requireContext()
                );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                dpToPx(12),
                0,
                dpToPx(12)
        );


        // -------------------------------------------------
        // LEFT SIDE
        // -------------------------------------------------

        LinearLayout informationLayout =
                new LinearLayout(
                        requireContext()
                );

        informationLayout.setOrientation(
                LinearLayout.VERTICAL
        );


        LinearLayout.LayoutParams informationParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );


        // -------------------------------------------------
        // SERVICE NAME
        // -------------------------------------------------

        TextView serviceView =
                new TextView(
                        requireContext()
                );


        String serviceName =
                subscription.getServiceName();


        if (serviceName == null ||
                serviceName.trim().isEmpty()) {

            serviceName = "Subscription";
        }


        serviceView.setText(
                serviceName
        );

        serviceView.setTextSize(14);

        serviceView.setTextColor(
                Color.parseColor(
                        "#111827"
                )
        );

        serviceView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        // -------------------------------------------------
        // BILLING DETAILS
        // -------------------------------------------------

        TextView detailView =
                new TextView(
                        requireContext()
                );


        String billingCycle =
                subscription.getBillingCycle();


        if (billingCycle == null ||
                billingCycle.trim().isEmpty()) {

            billingCycle = "Subscription";
        }


        String nextBillingDate =
                subscription.getNextBillingDate();


        if (nextBillingDate == null) {

            nextBillingDate = "";
        }


        String detailText =
                billingCycle;


        if (!nextBillingDate.trim().isEmpty()) {

            detailText +=
                    "  •  Next: "
                            + nextBillingDate;
        }


        detailView.setText(
                detailText
        );

        detailView.setTextSize(11);

        detailView.setTextColor(
                Color.parseColor(
                        "#8A909C"
                )
        );

        detailView.setPadding(
                0,
                dpToPx(4),
                0,
                0
        );


        informationLayout.addView(
                serviceView
        );

        informationLayout.addView(
                detailView
        );


        // -------------------------------------------------
        // AMOUNT
        // -------------------------------------------------

        TextView amountView =
                new TextView(
                        requireContext()
                );


        amountView.setText(

                String.format(
                        Locale.getDefault(),
                        "Rs %,.2f",
                        subscription.getAmount()
                )
        );

        amountView.setTextSize(13);

        amountView.setTextColor(
                Color.parseColor(
                        "#16A34A"
                )
        );

        amountView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        // -------------------------------------------------
        // ADD ROW
        // -------------------------------------------------

        row.addView(
                informationLayout,
                informationParams
        );

        row.addView(
                amountView
        );


        sharedSubscriptionsContainer
                .addView(
                        row
                );


        // -------------------------------------------------
        // DIVIDER
        // -------------------------------------------------

        if (showDivider) {

            View divider =
                    new View(
                            requireContext()
                    );


            divider.setBackgroundColor(
                    Color.parseColor(
                            "#E5E7EB"
                    )
            );


            LinearLayout.LayoutParams dividerParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                    );


            sharedSubscriptionsContainer
                    .addView(
                            divider,
                            dividerParams
                    );
        }
    }

    // =====================================================
// NO SHARED SUBSCRIPTIONS
// =====================================================

    private void showNoSharedSubscriptions() {

        if (sharedSubscriptionsContainer == null) {
            return;
        }


        sharedSubscriptionsContainer.removeAllViews();


        if (tvNoSharedSubscriptions != null) {

            tvNoSharedSubscriptions.setText(
                    "No shared subscriptions yet"
            );

            tvNoSharedSubscriptions.setVisibility(
                    View.VISIBLE
            );


            sharedSubscriptionsContainer.addView(
                    tvNoSharedSubscriptions
            );
        }
    }



    // =====================================================
    // CONVERT FIRESTORE FAMILY EXPENSES
    // TO EXISTING EXPENSE MODELS
    // =====================================================

    private ArrayList<ExpenseModel>
    convertFamilyExpensesToModels(
            ArrayList<FamilyFirestoreService.FamilyExpenseData>
                    familyExpenses
    ) {

        ArrayList<ExpenseModel> expenses =
                new ArrayList<>();

        if (familyExpenses == null) {
            return expenses;
        }

        for (
                FamilyFirestoreService.FamilyExpenseData familyExpense
                : familyExpenses
        ) {

            if (familyExpense == null) {
                continue;
            }

            ExpenseModel model =
                    new ExpenseModel(

                            parseTransactionId(
                                    familyExpense.getTransactionId()
                            ),

                            familyExpense.getCategoryId(),

                            familyExpense.getPaymentMethodId(),

                            familyExpense.getCategory(),

                            familyExpense.getPaymentMethod(),

                            familyExpense.getAmount(),

                            familyExpense.getDate(),

                            familyExpense.getNote(),

                            "Family"
                    );

            expenses.add(model);
        }

        return expenses;
    }

    // =====================================================
// PARSE TRANSACTION ID
// =====================================================

    private int parseTransactionId(
            String transactionId
    ) {

        if (transactionId == null) {
            return -1;
        }

        try {

            return Integer.parseInt(
                    transactionId
            );

        } catch (NumberFormatException e) {

            return -1;
        }
    }



    // =====================================================
    // AI FAMILY INSIGHT
    // =====================================================

    private void loadFamilyAIInsight(
            double totalIncome,
            double totalExpense,
            ArrayList<ExpenseModel> expenses
    ) {

        if (tvAIInsight == null) {
            return;
        }

        // -------------------------------------------------
        // NO FAMILY DATA
        // -------------------------------------------------

        if (totalIncome <= 0 && totalExpense <= 0) {

            tvAIInsight.setText(
                    "💡 Start recording family income and expenses "
                            + "to receive personalized AI financial insights."
            );

            return;
        }


        // -------------------------------------------------
        // EXPENSES WITHOUT INCOME
        // -------------------------------------------------

        if (totalIncome <= 0 && totalExpense > 0) {

            tvAIInsight.setText(
                    "⚠ Family expenses have been recorded, but "
                            + "no family income has been added yet. "
                            + "Add income to get more accurate financial insights."
            );

            return;
        }


        // -------------------------------------------------
        // CALCULATE SAVINGS
        // -------------------------------------------------

        double savings =
                totalIncome - totalExpense;


        // -------------------------------------------------
        // EXPENSE PERCENTAGE
        // -------------------------------------------------

        double expensePercentage =
                (totalExpense / totalIncome) * 100;


        // -------------------------------------------------
        // FIND HIGHEST SPENDING CATEGORY
        // -------------------------------------------------

        Map<String, Double> categoryTotals =
                new LinkedHashMap<>();


        if (expenses != null) {

            for (ExpenseModel expense : expenses) {

                if (expense == null) {
                    continue;
                }

                String category =
                        expense.getCategoryName();


                if (category == null ||
                        category.trim().isEmpty()) {

                    category = "Other";
                }


                double amount =
                        expense.getAmount();


                Double current =
                        categoryTotals.get(category);


                if (current == null) {
                    current = 0.0;
                }


                categoryTotals.put(
                        category,
                        current + amount
                );
            }
        }


        String highestCategory =
                "general spending";

        double highestCategoryAmount =
                0;


        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() >
                    highestCategoryAmount) {

                highestCategoryAmount =
                        entry.getValue();

                highestCategory =
                        entry.getKey();
            }
        }


        // =================================================
        // INSIGHT LOGIC
        // =================================================

        String insight;


        // -------------------------------------------------
        // CASE 1: EXPENSES > INCOME
        // -------------------------------------------------

        if (totalExpense > totalIncome) {

            double overspending =
                    totalExpense - totalIncome;


            insight =
                    "⚠ Your family is currently spending more "
                            + "than its recorded income by Rs "
                            + formatAmount(overspending)
                            + ". "
                            + "The highest spending category is "
                            + highestCategory
                            + ". "
                            + "Consider reducing non-essential "
                            + "expenses and reviewing this category.";


        }

        // -------------------------------------------------
        // CASE 2: VERY HIGH SPENDING
        // -------------------------------------------------

        else if (expensePercentage >= 80) {

            insight =
                    "⚠ Your family has used "
                            + formatAmount(expensePercentage)
                            + "% of its recorded income. "
                            + highestCategory
                            + " is currently the highest spending "
                            + "category. "
                            + "Try to reduce unnecessary spending "
                            + "to protect your remaining savings.";


        }

        // -------------------------------------------------
        // CASE 3: MODERATE-HIGH SPENDING
        // -------------------------------------------------

        else if (expensePercentage >= 60) {

            insight =
                    "💡 Your family has used "
                            + formatAmount(expensePercentage)
                            + "% of its recorded income. "
                            + highestCategory
                            + " is the largest expense category. "
                            + "Your family finances are manageable, "
                            + "but controlling discretionary spending "
                            + "could improve savings.";


        }

        // -------------------------------------------------
        // CASE 4: HEALTHY SPENDING
        // -------------------------------------------------

        else if (expensePercentage <= 40) {

            insight =
                    "🎯 Your family is maintaining a healthy "
                            + "spending pattern. Only "
                            + formatAmount(expensePercentage)
                            + "% of recorded income has been spent. "
                            + "Approximately Rs "
                            + formatAmount(savings)
                            + " remains after expenses. "
                            + "Keep maintaining this positive habit.";


        }

        // -------------------------------------------------
        // CASE 5: NORMAL
        // -------------------------------------------------

        else {

            insight =
                    "✓ Your family spending is currently "
                            + "under control. Approximately Rs "
                            + formatAmount(savings)
                            + " remains after recorded expenses. "
                            + "Continue monitoring "
                            + highestCategory
                            + " and maintain consistent savings.";
        }


        // -------------------------------------------------
        // DISPLAY
        // -------------------------------------------------

        tvAIInsight.setText(
                insight
        );
    }

    // =====================================================
// FORMAT AMOUNT
// =====================================================

    private String formatAmount(double amount) {

        return String.format(
                Locale.getDefault(),
                "%,.2f",
                amount
        );
    }


    // =====================================================
    // LOAD RECENT FAMILY TRANSACTIONS
    // =====================================================

    private void loadRecentFamilyTransactions(
            ArrayList<ExpenseModel> expenses
    ) {

        if (familyTransactionsContainer == null) {

            return;
        }


        familyTransactionsContainer
                .removeAllViews();


        // -------------------------------------------------
        // EMPTY
        // -------------------------------------------------

        if (expenses == null ||
                expenses.isEmpty()) {

            showNoFamilyTransactions();

            return;
        }


        // -------------------------------------------------
        // MAXIMUM 5
        // -------------------------------------------------

        int displayCount =
                showAllTransactions
                        ? expenses.size()
                        : Math.min(expenses.size(), 1
                );


        for (int i = 0;
             i < displayCount;
             i++) {

            ExpenseModel expense =
                    expenses.get(i);


            addFamilyExpenseRow(
                    expense,
                    i < displayCount - 1
            );
        }
    }

// =====================================================
// LOAD SELECTED FAMILY BUDGET
// =====================================================

    private void loadFamilyBudget() {

        if (!canAccessFamilyDashboard()) {
            return;
        }

        // -------------------------------------------------
        // GET FIRESTORE FAMILY ID
        // -------------------------------------------------

        String firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        selectedFamilyId
                );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            if (tvBudgetAmount != null) {
                tvBudgetAmount.setText("Rs. 0.00");
            }

            if (tvFamilyBudgetRemaining != null) {
                tvFamilyBudgetRemaining.setText("Rs. 0.00");
            }

            if (tvBudgetPercentage != null) {
                tvBudgetPercentage.setText("No Budget Set");
            }

            if (progressFamilyBudget != null) {
                progressFamilyBudget.setProgress(0);
            }

            return;
        }

        // -------------------------------------------------
        // GET BUDGET FROM FIRESTORE
        // -------------------------------------------------

        familyFirestoreService.getFamilyBudget(
                firestoreFamilyId,
                new FamilyFirestoreService.FamilyBudgetCallback() {

                    @Override
                    public void onSuccess(
                            FamilyFirestoreService.FamilyBudgetData budgetData
                    ) {

                        if (!isAdded()
                                || getView() == null
                                || familyAccessRevoked) {

                            return;
                        }

                        // -----------------------------------------
                        // NO BUDGET
                        // -----------------------------------------

                        if (budgetData == null ||
                                budgetData.getLimitAmount() <= 0) {

                            if (tvBudgetAmount != null) {
                                tvBudgetAmount.setText(
                                        "Rs. 0.00"
                                );
                            }

                            if (tvFamilyBudgetRemaining != null) {
                                tvFamilyBudgetRemaining.setText(
                                        "Rs. 0.00"
                                );
                            }

                            if (tvBudgetPercentage != null) {
                                tvBudgetPercentage.setText(
                                        "No Budget Set"
                                );
                            }

                            if (progressFamilyBudget != null) {
                                progressFamilyBudget.setProgress(0);
                            }

                            return;
                        }

                        double budget =
                                budgetData.getLimitAmount();

                        // -----------------------------------------
                        // GET FIRESTORE FAMILY EXPENSES
                        // -----------------------------------------

                        familyFirestoreService.getFamilyExpenses(
                                firestoreFamilyId,
                                new FamilyFirestoreService.FamilyExpensesCallback() {

                                    @Override
                                    public void onSuccess(
                                            ArrayList<FamilyFirestoreService.FamilyExpenseData>
                                                    familyExpenses
                                    ) {

                                        if (!isAdded()
                                                || getView() == null
                                                || familyAccessRevoked) {

                                            return;
                                        }

                                        // ---------------------------------
                                        // CALCULATE TOTAL SPENT
                                        // ---------------------------------

                                        double spent = 0.0;

                                        if (familyExpenses != null) {

                                            for (
                                                    FamilyFirestoreService.FamilyExpenseData expense
                                                    : familyExpenses
                                            ) {

                                                if (expense != null) {

                                                    spent +=
                                                            expense.getAmount();
                                                }
                                            }
                                        }

                                        // ---------------------------------
                                        // REMAINING
                                        // ---------------------------------

                                        double remaining =
                                                budget - spent;

                                        if (remaining < 0) {
                                            remaining = 0;
                                        }

                                        // ---------------------------------
                                        // PERCENTAGE
                                        // ---------------------------------

                                        int percentage =
                                                (int) Math.round(
                                                        (spent / budget) * 100
                                                );

                                        if (percentage < 0) {
                                            percentage = 0;
                                        }

                                        if (percentage > 100) {
                                            percentage = 100;
                                        }

                                        // ---------------------------------
                                        // UPDATE UI
                                        // ---------------------------------

                                        if (tvBudgetAmount != null) {

                                            tvBudgetAmount.setText(
                                                    String.format(
                                                            Locale.getDefault(),
                                                            "Rs. %.2f",
                                                            budget
                                                    )
                                            );
                                        }

                                        if (tvFamilyBudgetRemaining != null) {

                                            tvFamilyBudgetRemaining.setText(
                                                    String.format(
                                                            Locale.getDefault(),
                                                            "Rs. %.2f",
                                                            remaining
                                                    )
                                            );
                                        }

                                        if (tvBudgetPercentage != null) {

                                            tvBudgetPercentage.setText(
                                                    percentage + "% Used"
                                            );
                                        }

                                        if (progressFamilyBudget != null) {

                                            progressFamilyBudget.setProgress(
                                                    percentage
                                            );
                                        }
                                    }

                                    @Override
                                    public void onFailure(
                                            String message
                                    ) {

                                        if (!isAdded()) {
                                            return;
                                        }

                                        Toast.makeText(
                                                requireContext(),
                                                "Failed to load family spending.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Failed to load family budget: "
                                        + message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
    // =====================================================
    // NO TRANSACTIONS
    // =====================================================

    private void showNoFamilyTransactions() {

        if (familyTransactionsContainer == null) {

            return;
        }


        TextView emptyView =
                new TextView(
                        requireContext()
                );


        emptyView.setText(
                "No shared transactions yet"
        );

        emptyView.setTextSize(12);

        emptyView.setTextColor(
                Color.parseColor(
                        "#9CA3AF"
                )
        );

        emptyView.setGravity(
                Gravity.CENTER
        );

        emptyView.setPadding(
                0,
                dpToPx(24),
                0,
                dpToPx(24)
        );


        familyTransactionsContainer
                .addView(
                        emptyView
                );
    }


    // =====================================================
    // ADD FAMILY EXPENSE ROW
    // =====================================================

    private void addFamilyExpenseRow(
            ExpenseModel expense,
            boolean showDivider
    ) {

        if (expense == null ||
                familyTransactionsContainer == null) {

            return;
        }


        LinearLayout row =
                new LinearLayout(
                        requireContext()
                );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                dpToPx(12),
                0,
                dpToPx(12)
        );


        // -------------------------------------------------
        // LEFT INFO
        // -------------------------------------------------

        LinearLayout informationLayout =
                new LinearLayout(
                        requireContext()
                );

        informationLayout.setOrientation(
                LinearLayout.VERTICAL
        );


        LinearLayout.LayoutParams informationParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );


        // -------------------------------------------------
        // CATEGORY
        // -------------------------------------------------

        TextView categoryView =
                new TextView(
                        requireContext()
                );


        String categoryName =
                expense.getCategoryName();


        if (categoryName == null ||
                categoryName.trim().isEmpty()) {

            categoryName = "Expense";
        }


        categoryView.setText(
                categoryName
        );

        categoryView.setTextSize(14);

        categoryView.setTextColor(
                Color.parseColor(
                        "#111827"
                )
        );

        categoryView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        // -------------------------------------------------
        // DATE + NOTE
        // -------------------------------------------------

        TextView detailView =
                new TextView(
                        requireContext()
                );


        String date =
                expense.getTransactionDate();

        if (date == null) {
            date = "";
        }


        String note =
                expense.getNote();


        String detailText =
                date;


        if (note != null &&
                !note.trim().isEmpty()) {

            if (!detailText.isEmpty()) {

                detailText += "  •  ";
            }

            detailText +=
                    note.trim();
        }


        detailView.setText(
                detailText
        );

        detailView.setTextSize(11);

        detailView.setTextColor(
                Color.parseColor(
                        "#8A909C"
                )
        );

        detailView.setPadding(
                0,
                dpToPx(4),
                0,
                0
        );


        informationLayout.addView(
                categoryView
        );

        informationLayout.addView(
                detailView
        );


        // -------------------------------------------------
        // AMOUNT
        // -------------------------------------------------

        TextView amountView =
                new TextView(
                        requireContext()
                );


        amountView.setText(

                String.format(
                        Locale.getDefault(),
                        "- Rs %,.2f",
                        expense.getAmount()
                )
        );

        amountView.setTextSize(13);

        amountView.setTextColor(
                Color.parseColor(
                        "#DC2626"
                )
        );

        amountView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        // -------------------------------------------------
        // ADD
        // -------------------------------------------------

        row.addView(
                informationLayout,
                informationParams
        );

        row.addView(
                amountView
        );


        familyTransactionsContainer
                .addView(
                        row
                );


        // -------------------------------------------------
        // DIVIDER
        // -------------------------------------------------

        if (showDivider) {

            View divider =
                    new View(
                            requireContext()
                    );

            divider.setBackgroundColor(
                    Color.parseColor(
                            "#E5E7EB"
                    )
            );


            LinearLayout.LayoutParams dividerParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                    );


            familyTransactionsContainer
                    .addView(
                            divider,
                            dividerParams
                    );
        }
    }

    private void loadFamilyTransactionsOnly() {

        if (!canAccessFamilyDashboard()) {
            return;
        }

        ArrayList<ExpenseModel> expenses =
                databaseHelper.getFamilyExpenses(
                        selectedFamilyId
                );

        loadRecentFamilyTransactions(
                expenses
        );
    }
    // =====================================================
// LOAD FAMILY CATEGORY PIE CHART
// =====================================================

    private void loadFamilyCategorySpending(
            ArrayList<ExpenseModel> expenses
    ) {

        if (familyPieChart == null) {
            return;
        }

        ArrayList<PieEntry> entries =
                new ArrayList<>();


        // -------------------------------------------------
        // EMPTY CHECK
        // -------------------------------------------------

        if (expenses == null || expenses.isEmpty()) {

            showNoCategoryData();

            return;
        }


        // -------------------------------------------------
        // CALCULATE CATEGORY TOTALS
        // -------------------------------------------------

        Map<String, Float> categoryTotals =
                new LinkedHashMap<>();


        for (ExpenseModel expense : expenses) {

            if (expense == null) {
                continue;
            }

            String category =
                    expense.getCategoryName();

            if (category == null ||
                    category.trim().isEmpty()) {

                category = "Other";
            }

            float amount =
                    (float) expense.getAmount();


            if (categoryTotals.containsKey(category)) {

                Float current =
                        categoryTotals.get(category);

                if (current == null) {
                    current = 0f;
                }

                categoryTotals.put(
                        category,
                        current + amount
                );

            } else {

                categoryTotals.put(
                        category,
                        amount
                );
            }
        }


        // -------------------------------------------------
        // CREATE PIE ENTRIES
        // -------------------------------------------------

        for (Map.Entry<String, Float> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() > 0) {

                entries.add(
                        new PieEntry(
                                entry.getValue(),
                                entry.getKey()
                        )
                );
            }
        }


        if (entries.isEmpty()) {

            showNoCategoryData();

            return;
        }


        // -------------------------------------------------
        // SHOW CHART
        // -------------------------------------------------

        if (tvNoCategoryData != null) {

            tvNoCategoryData.setVisibility(
                    View.GONE
            );
        }

        familyPieChart.setVisibility(
                View.VISIBLE
        );


        // -------------------------------------------------
        // DATA SET
        // -------------------------------------------------

        PieDataSet dataSet =
                new PieDataSet(
                        entries,
                        ""
                );


        dataSet.setColors(
                ColorTemplate.MATERIAL_COLORS
        );

        dataSet.setSliceSpace(3f);

        dataSet.setSelectionShift(5f);


        // -------------------------------------------------
        // PIE DATA
        // -------------------------------------------------

        PieData pieData =
                new PieData(dataSet);

        pieData.setValueTextSize(11f);

        pieData.setValueTextColor(
                Color.WHITE
        );


        // -------------------------------------------------
        // CHART CONFIGURATION
        // -------------------------------------------------

        familyPieChart.setData(
                pieData
        );

        familyPieChart
                .getDescription()
                .setEnabled(false);

        familyPieChart
                .getLegend()
                .setEnabled(false);

        familyPieChart.setDrawHoleEnabled(
                true
        );

        familyPieChart.setHoleRadius(
                68f
        );

        familyPieChart.setTransparentCircleRadius(
                72f
        );

        familyPieChart.setDrawEntryLabels(
                false
        );

        familyPieChart.setRotationEnabled(
                true
        );

        familyPieChart.setHighlightPerTapEnabled(
                true
        );

        familyPieChart.animateY(
                700
        );

        familyPieChart.invalidate();
    }


// =====================================================
// NO CATEGORY DATA
// =====================================================

    private void showNoCategoryData() {

        if (familyPieChart != null) {

            familyPieChart.clear();

            familyPieChart.setVisibility(
                    View.GONE
            );
        }


        if (tvNoCategoryData != null) {

            tvNoCategoryData.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =====================================================
    // UPDATE ROLE BASED UI
    // =====================================================

    private void updateRoleBasedUI() {

        // Only the Family Head / PRIMARY user can invite members.
        if (btnInviteMember != null) {

            btnInviteMember.setVisibility(
                    isPrimaryUser()
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        // Keep the budget button visible for everyone.
        // Non-primary users receive a permission alert when they press it.
        if (btnSetFamilyBudget != null) {
            btnSetFamilyBudget.setVisibility(View.VISIBLE);
        }
    }


    // =====================================================
    // FAMILY SELECTOR
    // =====================================================

    private void showFamilySelectorMenu() {

        if (layoutFamilySelector == null ||
                !canAccessFamilyDashboard()) {

            return;
        }


        PopupMenu popupMenu =
                new PopupMenu(
                        requireContext(),
                        layoutFamilySelector
                );


        Menu menu =
                popupMenu.getMenu();


        // -------------------------------------------------
        // EXISTING FAMILIES
        // -------------------------------------------------

        for (int i = 0;
             i < familyIds.size();
             i++) {

            int familyId =
                    familyIds.get(i);

            String familyName =
                    familyNames.get(i);

            String role =
                    familyRoles.get(i);


            String title =
                    familyName +
                            "  •  " +
                            role;


            MenuItem item =
                    menu.add(
                            Menu.NONE,
                            familyId,
                            Menu.NONE,
                            title
                    );


            item.setCheckable(true);


            if (familyId ==
                    selectedFamilyId) {

                item.setChecked(true);
            }
        }


        // -------------------------------------------------
        // EXTRA OPTIONS
        // -------------------------------------------------

        final int MENU_CREATE_FAMILY =
                900001;

        final int MENU_JOIN_FAMILY =
                900002;


        menu.add(
                Menu.NONE,
                MENU_CREATE_FAMILY,
                Menu.NONE,
                "+ Create New Family"
        );


        menu.add(
                Menu.NONE,
                MENU_JOIN_FAMILY,
                Menu.NONE,
                "+ Join Another Family"
        );


        // -------------------------------------------------
        // MENU CLICK
        // -------------------------------------------------

        popupMenu.setOnMenuItemClickListener(
                item -> {

                    int itemId =
                            item.getItemId();


                    if (itemId ==
                            MENU_CREATE_FAMILY) {

                        openCreateFamilyActivity();

                        return true;
                    }


                    if (itemId ==
                            MENU_JOIN_FAMILY) {

                        openJoinFamilyActivity();

                        return true;
                    }


                    int index =
                            familyIds.indexOf(
                                    itemId
                            );


                    if (index != -1) {

                        selectFamily(index);

                        return true;
                    }


                    return false;
                }
        );


        popupMenu.show();
    }


    // =====================================================
    // SELECT FAMILY
    // =====================================================

    private void selectFamily(int index) {

        if (index < 0 ||
                index >= familyIds.size()) {

            return;
        }


        selectedFamilyId =
                familyIds.get(index);

        selectedFamilyRole =
                familyRoles.get(index);


        // Reload everything for selected family
        loadSelectedFamilyDashboard();
    }



    // =====================================================
    // LOAD FAMILY MEMBERS FROM FIRESTORE
    // =====================================================

    private void loadFamilyMembers() {

        if (!canAccessFamilyDashboard()
                || familyFirestoreService == null) {

            return;
        }

        String firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        selectedFamilyId
                );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            return;
        }

        familyFirestoreService.getFamilyMembers(
                firestoreFamilyId,
                new FamilyFirestoreService.FamilyMembersCallback() {

                    @Override
                    public void onSuccess(
                            List<FamilyFirestoreService.FamilyMemberData> members
                    ) {

                        if (!isAdded()
                                || getView() == null
                                || familyAccessRevoked) {

                            return;
                        }

                        displayFamilyMembers(members);
                    }

                    @Override
                    public void onFailure(String message) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Unable to load family members: "
                                        + message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // =====================================================
// DISPLAY FAMILY MEMBERS
// =====================================================


    private void displayFamilyMembers(
            List<FamilyFirestoreService.FamilyMemberData> members
    ) {

        if (familyMembersContainer == null) {
            return;
        }

        familyMembersContainer.removeAllViews();

        if (members == null || members.isEmpty()) {

            showNoFamilyMembers();
            return;
        }

        if (tvMemberCount != null) {

            tvMemberCount.setText(
                    members.size() == 1
                            ? "1 Member"
                            : members.size() + " Members"
            );
        }

        if (tvNoMembers != null) {
            tvNoMembers.setVisibility(View.GONE);
        }

        FirebaseUser currentFirebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        String currentFirebaseUid =
                currentFirebaseUser == null
                        ? ""
                        : currentFirebaseUser.getUid();


        for (int i = 0; i < members.size(); i++) {

            FamilyFirestoreService.FamilyMemberData member =
                    members.get(i);

            if (member == null) {
                continue;
            }

            String memberName =
                    member.getName();

            if (memberName == null ||
                    memberName.trim().isEmpty()) {

                memberName = "Family Member";
            }

            String role =
                    member.getRole();

            if (role == null ||
                    role.trim().isEmpty()) {

                role = "MEMBER";
            }


            // =============================================
            // MAIN MEMBER ROW
            // =============================================

            LinearLayout memberRow =
                    new LinearLayout(requireContext());

            memberRow.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            memberRow.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            memberRow.setPadding(
                    0,
                    dpToPx(10),
                    0,
                    dpToPx(10)
            );


            // =============================================
            // MEMBER INFORMATION
            // =============================================

            LinearLayout memberInfo =
                    new LinearLayout(requireContext());

            memberInfo.setOrientation(
                    LinearLayout.VERTICAL
            );

            LinearLayout.LayoutParams memberInfoParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                    );


            // =============================================
            // MEMBER NAME
            // =============================================

            TextView nameView =
                    new TextView(requireContext());

            nameView.setText(
                    getInitials(memberName)
                            + "   "
                            + memberName
            );

            nameView.setTextSize(14);

            nameView.setTextColor(
                    Color.parseColor("#111827")
            );

            nameView.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );


            // =============================================
            // MEMBER ROLE
            // =============================================

            TextView roleView =
                    new TextView(requireContext());

            String displayRole =
                    "PRIMARY".equalsIgnoreCase(role)
                            ? "Family Head"
                            : "Family Member";

            roleView.setText(displayRole);

            roleView.setTextSize(11);

            roleView.setTextColor(
                    Color.parseColor("#6B7280")
            );

            roleView.setPadding(
                    0,
                    dpToPx(3),
                    0,
                    0
            );


            memberInfo.addView(nameView);
            memberInfo.addView(roleView);

            memberRow.addView(
                    memberInfo,
                    memberInfoParams
            );


            // =============================================
            // MANAGE MEMBER / TRANSFER FAMILY HEAD
            // =============================================

            boolean isCurrentUser =
                    member.getUid() != null
                            && member.getUid()
                            .equals(currentFirebaseUid);

            boolean isFamilyHead =
                    "PRIMARY".equalsIgnoreCase(role);

            /*
             * Only the current Family Head sees the Manage button.
             * It is shown only for another member, never for the
             * current Family Head row.
             */
            if (isPrimaryUser() &&
                    !isCurrentUser &&
                    !isFamilyHead) {

                MaterialButton manageButton =
                        new MaterialButton(
                                requireContext(),
                                null,
                                com.google.android.material.R.attr.materialButtonOutlinedStyle
                        );

                manageButton.setText("Manage");
                manageButton.setTextSize(11);
                manageButton.setAllCaps(false);
                manageButton.setMinWidth(0);
                manageButton.setMinimumWidth(0);
                manageButton.setPadding(
                        dpToPx(10),
                        0,
                        dpToPx(10),
                        0
                );

                String finalManageMemberName =
                        memberName;

                manageButton.setOnClickListener(v ->
                        showMemberManagementMenu(
                                manageButton,
                                member.getUid(),
                                finalManageMemberName
                        )
                );

                memberRow.addView(manageButton);
            }


            // =============================================
            // REMOVE BUTTON
            // =============================================

            /*
             * Do not show Remove for the current user.
             * Do not show Remove for the Family Head.
             *
             * Everyone else gets the button.
             * Permission is checked again when pressed.
             */

            if (!isCurrentUser && !isFamilyHead) {

                MaterialButton removeButton =
                        new MaterialButton(
                                requireContext(),
                                null,
                                com.google.android.material.R.attr.materialButtonOutlinedStyle
                        );

                removeButton.setText("Remove");
                removeButton.setTextSize(11);
                removeButton.setAllCaps(false);

                removeButton.setMinWidth(0);
                removeButton.setMinimumWidth(0);

                removeButton.setPadding(
                        dpToPx(12),
                        0,
                        dpToPx(12),
                        0
                );

                String finalMemberName =
                        memberName;

                removeButton.setOnClickListener(v -> {

                    if (!isPrimaryUser()) {

                        showPrimaryOnlyAlert(
                                "Only the Family Head can remove family members."
                        );

                        return;
                    }

                    confirmRemoveFamilyMember(
                            member.getUid(),
                            finalMemberName
                    );
                });

                memberRow.addView(removeButton);
            }


            familyMembersContainer.addView(
                    memberRow
            );


            if (i < members.size() - 1) {

                View divider =
                        new View(requireContext());

                divider.setBackgroundColor(
                        Color.parseColor("#E5E7EB")
                );

                LinearLayout.LayoutParams dividerParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dpToPx(1)
                        );

                familyMembersContainer.addView(
                        divider,
                        dividerParams
                );
            }
        }
    }

    // =====================================================
    // MEMBER MANAGEMENT MENU
    // =====================================================

    private void showMemberManagementMenu(
            View anchor,
            String memberUid,
            String memberName
    ) {

        if (!isPrimaryUser()) {
            showPrimaryOnlyAlert(
                    "Only the Family Head can manage family members."
            );
            return;
        }

        PopupMenu popupMenu =
                new PopupMenu(
                        requireContext(),
                        anchor
                );

        final int MENU_MAKE_HEAD = 200001;
        final int MENU_REMOVE = 200002;

        popupMenu.getMenu().add(
                Menu.NONE,
                MENU_MAKE_HEAD,
                Menu.NONE,
                "Make Family Head"
        );

        popupMenu.getMenu().add(
                Menu.NONE,
                MENU_REMOVE,
                Menu.NONE,
                "Remove Member"
        );

        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == MENU_MAKE_HEAD) {

                confirmTransferFamilyHead(
                        memberUid,
                        memberName
                );

                return true;
            }

            if (item.getItemId() == MENU_REMOVE) {

                confirmRemoveFamilyMember(
                        memberUid,
                        memberName
                );

                return true;
            }

            return false;
        });

        popupMenu.show();
    }


    // =====================================================
    // CONFIRM FAMILY HEAD TRANSFER
    // =====================================================

    private void confirmTransferFamilyHead(
            String newPrimaryUid,
            String newPrimaryName
    ) {

        if (!isPrimaryUser()) {
            showPrimaryOnlyAlert(
                    "Only the Family Head can transfer the Family Head role."
            );
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Change Family Head")
                .setMessage(
                        "Make " + newPrimaryName
                                + " the new Family Head?\n\n"
                                + "You will become a normal family member. "
                                + "The new Family Head will control invitations, "
                                + "member removal, family budget and family deletion."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Make Family Head",
                        (dialog, which) ->
                                transferFamilyHead(
                                        newPrimaryUid,
                                        newPrimaryName
                                )
                )
                .show();
    }


    // =====================================================
    // TRANSFER FAMILY HEAD
    // FIRESTORE FIRST, THEN LOCAL SQLITE
    // =====================================================

    private void transferFamilyHead(
            String newPrimaryUid,
            String newPrimaryName
    ) {

        if (!isAdded() ||
                selectedFamilyId == -1 ||
                currentUserId == -1) {
            return;
        }

        if (!isPrimaryUser()) {
            showPrimaryOnlyAlert(
                    "Only the Family Head can transfer the Family Head role."
            );
            return;
        }

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(
                    requireContext(),
                    "User session not found. Please login again.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (newPrimaryUid == null ||
                newPrimaryUid.trim().isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Selected member ID is missing.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int newPrimaryUserId =
                databaseHelper
                        .getUserIdByFirebaseUid(
                                newPrimaryUid
                        );

        if (newPrimaryUserId == -1) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Unable to Change Family Head")
                    .setMessage(
                            "The selected member is not available in the local user database yet. "
                                    + "Please make sure the member has accepted the invitation and "
                                    + "their account is synchronized on this device."
                    )
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String firestoreFamilyId =
                databaseHelper
                        .getFirestoreFamilyId(
                                selectedFamilyId
                        );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Family Firestore ID not found.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        familyFirestoreService.transferFamilyHead(
                firestoreFamilyId,
                firebaseUser.getUid(),
                newPrimaryUid,
                newPrimaryName,
                new FamilyFirestoreService.MemberManagementCallback() {

                    @Override
                    public void onSuccess() {

                        if (!isAdded()) {
                            return;
                        }

                        boolean localSuccess =
                                databaseHelper
                                        .transferFamilyHead(
                                                selectedFamilyId,
                                                currentUserId,
                                                newPrimaryUserId
                                        );

                        if (!localSuccess) {

                            new AlertDialog.Builder(
                                    requireContext()
                            )
                                    .setTitle(
                                            "Family Head Changed Online"
                                    )
                                    .setMessage(
                                            "The Family Head was changed in Firestore, "
                                                    + "but the local database could not be updated. "
                                                    + "Please reload or sign in again to synchronize the family."
                                    )
                                    .setPositiveButton(
                                            "OK",
                                            (dialog, which) ->
                                                    reloadFragment()
                                    )
                                    .show();

                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                newPrimaryName
                                        + " is now the Family Head.",
                                Toast.LENGTH_SHORT
                        ).show();

                        reloadFragment();
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        new AlertDialog.Builder(
                                requireContext()
                        )
                                .setTitle(
                                        "Unable to Change Family Head"
                                )
                                .setMessage(
                                        message == null
                                                ? "Failed to change the Family Head."
                                                : message
                                )
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
                    }
                }
        );
    }


    // =====================================================
    // CONFIRM REMOVE FAMILY MEMBER
    // =====================================================

    private void confirmRemoveFamilyMember(
            String memberUid,
            String memberName
    ) {

        if (!isAdded()) {
            return;
        }

        if (!isPrimaryUser()) {

            showPrimaryOnlyAlert(
                    "Only the Family Head can remove family members."
            );

            return;
        }

        if (memberUid == null ||
                memberUid.trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Member ID is missing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Remove Family Member"
                )
                .setMessage(
                        "Are you sure you want to remove "
                                + memberName
                                + " from this family?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Remove",
                        (dialog, which) ->
                                removeFamilyMember(
                                        memberUid,
                                        memberName
                                )
                )
                .show();
    }


    // =====================================================
    // REMOVE FAMILY MEMBER
    // =====================================================

    private void removeFamilyMember(
            String memberUid,
            String memberName
    ) {

        if (!isAdded() ||
                selectedFamilyId == -1) {

            return;
        }

        if (!isPrimaryUser()) {

            showPrimaryOnlyAlert(
                    "Only the Family Head can remove family members."
            );

            return;
        }

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User session not found. Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String firestoreFamilyId =
                databaseHelper
                        .getFirestoreFamilyId(
                                selectedFamilyId
                        );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Family Firestore ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        familyFirestoreService.removeFamilyMember(
                firestoreFamilyId,
                firebaseUser.getUid(),
                memberUid,
                new FamilyFirestoreService.MemberManagementCallback() {

                    @Override
                    public void onSuccess() {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                memberName
                                        + " removed from the family.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadFamilyMembers();
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        new AlertDialog.Builder(
                                requireContext()
                        )
                                .setTitle(
                                        "Unable to Remove Member"
                                )
                                .setMessage(
                                        message == null
                                                ? "Failed to remove family member."
                                                : message
                                )
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
                    }
                }
        );
    }


// =====================================================
// GET MEMBER INITIALS
// =====================================================

    private String getInitials(String name) {

        if (name == null || name.trim().isEmpty()) {
            return "FM";
        }

        String[] parts =
                name.trim().split("\\s+");

        if (parts.length == 1) {

            return parts[0]
                    .substring(
                            0,
                            Math.min(2, parts[0].length())
                    )
                    .toUpperCase();
        }

        return (
                parts[0].substring(0, 1)
                        + parts[parts.length - 1]
                        .substring(0, 1)
        ).toUpperCase();
    }


    // =====================================================
    // NO FAMILY MEMBERS
    // =====================================================

    private void showNoFamilyMembers() {

        if (familyMembersContainer == null) {
            return;
        }

        familyMembersContainer.removeAllViews();

        if (tvNoMembers != null) {

            tvNoMembers.setText(
                    "No family members found"
            );

            tvNoMembers.setVisibility(
                    View.VISIBLE
            );

            familyMembersContainer.addView(
                    tvNoMembers
            );
        }

        if (tvMemberCount != null) {
            tvMemberCount.setText("0 Members");
        }
    }
    // =====================================================
    // FAMILY OPTIONS
    // =====================================================

    private void showFamilyOptionsMenu() {

        if (btnFamilyOptions == null ||
                !canAccessFamilyDashboard()) {

            return;
        }

        PopupMenu popupMenu =
                new PopupMenu(
                        requireContext(),
                        btnFamilyOptions
                );

        Menu menu =
                popupMenu.getMenu();

        final int MENU_INVITE =
                100001;

        final int MENU_LEAVE =
                100002;

        final int MENU_DELETE =
                100003;

        // -------------------------------------------------
        // INVITE - PRIMARY ONLY
        // -------------------------------------------------

        if (isPrimaryUser()) {

            menu.add(
                    Menu.NONE,
                    MENU_INVITE,
                    Menu.NONE,
                    "Invite Member"
            );

        } else {

            menu.add(
                    Menu.NONE,
                    MENU_LEAVE,
                    Menu.NONE,
                    "Leave Family"
            );
        }

        // -------------------------------------------------
        // DELETE FAMILY - VISIBLE TO EVERYONE
        // Only PRIMARY can actually delete.
        // -------------------------------------------------

        menu.add(
                Menu.NONE,
                MENU_DELETE,
                Menu.NONE,
                "Delete Family"
        );

        popupMenu.setOnMenuItemClickListener(
                item -> {

                    int itemId =
                            item.getItemId();

                    if (itemId == MENU_INVITE) {

                        openInviteMemberActivity();
                        return true;
                    }

                    if (itemId == MENU_LEAVE) {

                        confirmLeaveFamily();
                        return true;
                    }

                    if (itemId == MENU_DELETE) {

                        if (!isPrimaryUser()) {

                            showPrimaryOnlyAlert(
                                    "Only the Family Head can delete this family."
                            );

                            return true;
                        }

                        confirmDeleteFamily();
                        return true;
                    }

                    return false;
                }
        );

        popupMenu.show();
    }

    // =====================================================
    // CREATE FAMILY
    // =====================================================

    private void openCreateFamilyActivity() {

        Intent intent =
                new Intent(
                        requireContext(),
                        CreateFamilyActivity.class
                );

        startActivity(intent);
    }


    // =====================================================
    // JOIN FAMILY
    // =====================================================

    private void openJoinFamilyActivity() {

        Intent intent =
                new Intent(
                        requireContext(),
                        JoinFamilyActivity.class
                );

        startActivity(intent);
    }


    // =====================================================
    // INVITE MEMBER
    // =====================================================

    private void openInviteMemberActivity() {

        if (selectedFamilyId == -1) {

            Toast.makeText(
                    requireContext(),
                    "No family selected",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!isPrimaryUser()) {

            showPrimaryOnlyAlert(
                    "Only the Family Head can invite family members."
            );

            return;
        }

        Intent intent =
                new Intent(
                        requireContext(),
                        InviteMemberActivity.class
                );

        intent.putExtra(
                "FAMILY_ID",
                selectedFamilyId
        );

        startActivity(intent);
    }

    // =====================================================
    // CONFIRM LEAVE FAMILY
    // =====================================================

    private void confirmLeaveFamily() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }


        String familyName =
                databaseHelper
                        .getFamilyName(
                                selectedFamilyId
                        );


        if (familyName == null) {

            familyName = "this family";
        }


        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Leave Family"
                )

                .setMessage(
                        "Are you sure you want to leave "
                                + familyName
                                + "?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Leave",
                        (dialog, which) ->
                                leaveSelectedFamily()
                )

                .show();
    }


    // =====================================================
    // LEAVE FAMILY
    // FIRESTORE FIRST, THEN LOCAL SQLITE
    // NORMAL MEMBERS ONLY
    // =====================================================

    private void leaveSelectedFamily() {

        if (!isAdded() ||
                selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }

        // -------------------------------------------------
        // FAMILY HEAD MUST NOT LEAVE DIRECTLY
        // -------------------------------------------------

        if (isPrimaryUser()) {

            showPrimaryOnlyAlert(
                    "The Family Head cannot leave the family. "
                            + "Transfer the Family Head role to another member "
                            + "or delete the family first."
            );

            return;
        }

        // -------------------------------------------------
        // CURRENT FIREBASE USER
        // -------------------------------------------------

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User session not found. Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // FIRESTORE FAMILY ID
        // -------------------------------------------------

        String firestoreFamilyId =
                databaseHelper
                        .getFirestoreFamilyId(
                                selectedFamilyId
                        );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Family Firestore ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final int familyIdToLeave =
                selectedFamilyId;

        // -------------------------------------------------
        // REMOVE FROM FIRESTORE FIRST
        // -------------------------------------------------

        familyFirestoreService.leaveFamily(
                firestoreFamilyId,
                firebaseUser.getUid(),
                new FamilyFirestoreService.MemberManagementCallback() {

                    @Override
                    public void onSuccess() {

                        if (!isAdded()) {
                            return;
                        }

                        // -----------------------------------------
                        // THEN REMOVE LOCAL SQLITE MEMBERSHIP
                        // -----------------------------------------

                        boolean localSuccess =
                                databaseHelper
                                        .leaveFamily(
                                                familyIdToLeave,
                                                currentUserId
                                        );

                        if (!localSuccess) {

                            new AlertDialog.Builder(
                                    requireContext()
                            )
                                    .setTitle(
                                            "Family Left Online"
                                    )
                                    .setMessage(
                                            "You were removed from the family in Firestore, "
                                                    + "but the local database could not be updated. "
                                                    + "Please reopen the app or sign in again to synchronize."
                                    )
                                    .setPositiveButton(
                                            "OK",
                                            (dialog, which) ->
                                                    reloadFragment()
                                    )
                                    .show();

                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "You left the family successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Reset selection before rebuilding the screen.
                        selectedFamilyId = -1;
                        selectedFamilyRole = null;

                        reloadFragment();
                    }

                    @Override
                    public void onFailure(
                            String message
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        new AlertDialog.Builder(
                                requireContext()
                        )
                                .setTitle(
                                        "Unable to Leave Family"
                                )
                                .setMessage(
                                        message == null ||
                                                message.trim().isEmpty()
                                                ? "Unable to leave this family."
                                                : message
                                )
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
                    }
                }
        );
    }


    // =====================================================
    // CONFIRM DELETE FAMILY
    // =====================================================

    private void confirmDeleteFamily() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }

        if (!isPrimaryUser()) {

            showPrimaryOnlyAlert(
                    "Only the Family Head can delete this family."
            );

            return;
        }

        String familyName =
                databaseHelper
                        .getFamilyName(
                                selectedFamilyId
                        );

        if (familyName == null ||
                familyName.trim().isEmpty()) {

            familyName = "this family";
        }

        String message =
                "Delete \"" +
                        familyName +
                        "\"?\n\n" +
                        "All family memberships and the family group will be removed. " +
                        "This action cannot be undone.";

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Delete Family"
                )
                .setMessage(
                        message
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteSelectedFamily()
                )
                .show();
    }


    // =====================================================
    // DELETE FAMILY FROM FIRESTORE + SQLITE
    // PRIMARY ONLY
    // =====================================================

    private void deleteSelectedFamily() {

        if (selectedFamilyId == -1 ||
                currentUserId == -1) {

            return;
        }

        if (!isPrimaryUser()) {

            showPrimaryOnlyAlert(
                    "Only the Family Head can delete this family."
            );

            return;
        }

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User session not found. Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String firestoreFamilyId =
                databaseHelper
                        .getFirestoreFamilyId(
                                selectedFamilyId
                        );

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Family Firestore ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        familyFirestoreService.deleteFamily(
                firestoreFamilyId,
                firebaseUser.getUid(),
                new FamilyFirestoreService.MemberManagementCallback() {

                    @Override
                    public void onSuccess() {

                        if (!isAdded()) {
                            return;
                        }

                        boolean localSuccess =
                                databaseHelper
                                        .deleteFamily(
                                                selectedFamilyId,
                                                currentUserId
                                        );

                        if (!localSuccess) {

                            Toast.makeText(
                                    requireContext(),
                                    "Family deleted online, but local cleanup failed.",
                                    Toast.LENGTH_LONG
                            ).show();

                            reloadFragment();
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Family deleted successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        reloadFragment();
                    }

                    @Override
                    public void onFailure(String message) {

                        if (!isAdded()) {
                            return;
                        }

                        new AlertDialog.Builder(
                                requireContext()
                        )
                                .setTitle(
                                        "Unable to Delete Family"
                                )
                                .setMessage(
                                        message == null
                                                ? "Unable to delete this family."
                                                : message
                                )
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
                    }
                }
        );
    }

    private boolean canAccessFamilyDashboard() {

        return !familyAccessRevoked
                && selectedFamilyId != -1
                && currentUserId != -1;
    }


    // =====================================================
    // PRIMARY / FAMILY HEAD CHECK
    // =====================================================

    private boolean isPrimaryUser() {

        if (selectedFamilyRole == null ||
                selectedFamilyRole.trim().isEmpty()) {

            return false;
        }

        return "PRIMARY".equalsIgnoreCase(
                selectedFamilyRole.trim()
        );
    }


    // =====================================================
    // PRIMARY-ONLY PERMISSION ALERT
    // =====================================================

    private void showPrimaryOnlyAlert(String message) {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Permission Denied"
                )
                .setMessage(
                        message
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =====================================================
    // RELOAD FRAGMENT
    // =====================================================

    private void reloadFragment() {

        if (!isAdded()) {

            return;
        }


        loadCurrentUser();
        loadUserFamilies();


        getParentFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }


    // =====================================================
    // DP TO PX
    // =====================================================

    private int dpToPx(int dp) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        return Math.round(
                dp * density
        );
    }


    // =====================================================
    // ON RESUME
    // =====================================================

    @Override
    public void onResume() {

        super.onResume();

        if (familyAccessRevoked) {
            return;
        }

        if (databaseHelper == null ||
                !isAdded()) {

            return;
        }


        int oldFamilyCount =
                familyIds.size();


        loadCurrentUser();
        loadUserFamilies();


        int newFamilyCount =
                familyIds.size();


        // -------------------------------------------------
        // FAMILY LIST CHANGED
        // -------------------------------------------------

        if (oldFamilyCount !=
                newFamilyCount) {

            getParentFragmentManager()
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();

            return;
        }


        // -------------------------------------------------
        // REFRESH FAMILY DASHBOARD
        // -------------------------------------------------

        if (userHasFamily &&
                getView() != null) {

            loadSelectedFamilyDashboard();
        }
    }


    // =====================================================
    // FAMILY SUBSCRIPTION UI
    // =====================================================

    private TextView tvSharedSubscriptionTotal;

    private LinearLayout sharedSubscriptionsContainer;

    private TextView tvNoSharedSubscriptions;


    private void startFamilyMembershipListener() {

        if (!isAdded()
                || selectedFamilyId == -1
                || currentUserId == -1) {

            return;
        }

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {
            return;
        }

        String firestoreFamilyId =
                databaseHelper.getFirestoreFamilyId(
                        selectedFamilyId
                );

        if (firestoreFamilyId == null
                || firestoreFamilyId.trim().isEmpty()) {

            return;
        }

        // Remove old listener first
        stopFamilyMembershipListener();

        familyMembershipListener =
                familyFirestoreService.listenToFamilyMembership(
                        firestoreFamilyId,
                        firebaseUser.getUid(),
                        new FamilyFirestoreService.FamilyMembershipCallback() {

                            @Override
                            public void onActive() {

                                if (!isAdded()) {
                                    return;
                                }

                                // User is still a family member.
                            }

                            @Override
                            public void onRemoved() {

                                if (!isAdded()) {
                                    return;
                                }

                                handleFamilyAccessRevoked();
                            }

                            @Override
                            public void onFailure(
                                    String message
                            ) {

                                // Do not freeze the dashboard
                                // because of temporary network errors.
                            }
                        }
                );
    }

    private void stopFamilyMembershipListener() {

        if (familyMembershipListener != null) {

            familyMembershipListener.remove();

            familyMembershipListener = null;
        }
    }

    private void handleFamilyAccessRevoked() {

        if (familyAccessRevoked) {
            return;
        }

        familyAccessRevoked = true;

        // Stop receiving membership updates
        stopFamilyMembershipListener();

        // Remove local SQLite membership
        if (selectedFamilyId != -1 &&
                currentUserId != -1) {

            databaseHelper.leaveFamily(
                    selectedFamilyId,
                    currentUserId
            );
        }

        // Freeze the dashboard
        disableFamilyDashboard();

        Toast.makeText(
                requireContext(),
                "You have been removed from this family.",
                Toast.LENGTH_LONG
        ).show();
    }

    private void disableFamilyDashboard() {

        if (getView() == null) {
            return;
        }

        View root =
                getView();

        root.setAlpha(0.55f);

        root.setEnabled(false);

        root.setClickable(true);

        if (btnInviteMember != null) {
            btnInviteMember.setEnabled(false);
        }

        if (btnFamilyOptions != null) {
            btnFamilyOptions.setEnabled(false);
        }

        if (layoutFamilySelector != null) {
            layoutFamilySelector.setEnabled(false);
        }

        if (btnSetFamilyBudget != null) {
            btnSetFamilyBudget.setEnabled(false);
        }

        if (tvViewSubscriptions != null) {
            tvViewSubscriptions.setEnabled(false);
        }

        if (tvViewTransactions != null) {
            tvViewTransactions.setEnabled(false);
        }

        showFamilyRemovedMessage();
    }

    private void showFamilyRemovedMessage() {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Family Access Removed"
                )
                .setMessage(
                        "You have been removed from this family.\n\n"
                                + "This family dashboard is no longer active "
                                + "and will not receive any further updates."
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .setCancelable(false)
                .show();
    }

    // =====================================================
    // CLEANUP
    // =====================================================

    @Override
    public void onDestroy() {

        stopFamilyMembershipListener();

        super.onDestroy();

        if (databaseHelper != null) {

            databaseHelper.close();
        }
    }
}
