package com.example.aiexpensemanagementapplication.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.CurrencyModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import com.example.aiexpensemanagementapplication.data.remote.CurrencyApiService;
import com.example.aiexpensemanagementapplication.data.remote.RetrofitClient;

import java.util.Collections;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrencySettingsActivity extends AppCompatActivity {

    // Toolbar
    private MaterialToolbar toolbarCurrency;

    // Current currency
    private TextView tvCurrentCurrencyName;
    private TextView tvCurrentCurrencyCode;
    private TextView tvCurrentCurrencySymbol;

    // Search
    private TextInputEditText etSearchCurrency;

    // Currency list
    private RecyclerView recyclerViewCurrencies;
    private CurrencyAdapter currencyAdapter;
    private final List<CurrencyModel> currencyList = new ArrayList<>();

    // Error section
    private LinearLayout layoutCurrencyError;
    private TextView tvCurrencyError;
    private MaterialButton btnRetryCurrencies;

    // Save and loading
    private MaterialButton btnSaveCurrency;
    private CircularProgressIndicator progressCurrency;

    // Database and Firebase
    private DatabaseHelper databaseHelper;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    // Current local user
    private int currentUserId = -1;

    // Saved currency
    private String savedCurrencyCode = "LKR";

    // Temporarily selected currency
    private CurrencyModel selectedCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_settings);

        initializeViews();

        initializeServices();

        getCurrentUserId();

        setupToolbar();

        setupRecyclerView();

        setupSearch();

        setupButtons();

        loadSavedCurrency();

        loadCurrenciesFromApi();
    }

    private void initializeViews() {

        toolbarCurrency = findViewById(R.id.toolbarCurrency);

        tvCurrentCurrencyName =
                findViewById(R.id.tvCurrentCurrencyName);

        tvCurrentCurrencyCode =
                findViewById(R.id.tvCurrentCurrencyCode);

        tvCurrentCurrencySymbol =
                findViewById(R.id.tvCurrentCurrencySymbol);

        etSearchCurrency =
                findViewById(R.id.etSearchCurrency);

        recyclerViewCurrencies =
                findViewById(R.id.recyclerViewCurrencies);

        layoutCurrencyError =
                findViewById(R.id.layoutCurrencyError);

        tvCurrencyError =
                findViewById(R.id.tvCurrencyError);

        btnRetryCurrencies =
                findViewById(R.id.btnRetryCurrencies);

        btnSaveCurrency =
                findViewById(R.id.btnSaveCurrency);

        progressCurrency =
                findViewById(R.id.progressCurrency);
    }

    private void initializeServices() {

        databaseHelper = new DatabaseHelper(this);

        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();
    }

    private void getCurrentUserId() {

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User session not found. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        currentUserId =
                databaseHelper.getUserIdByFirebaseUid(
                        currentUser.getUid()
                );

        if (currentUserId == -1) {

            Toast.makeText(
                    this,
                    "Local user profile not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }

    private void setupToolbar() {

        toolbarCurrency.setNavigationOnClickListener(
                view -> finish()
        );
    }

    private void setupRecyclerView() {

        recyclerViewCurrencies.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewCurrencies.setHasFixedSize(true);

        currencyAdapter = new CurrencyAdapter(
                currencyList,
                savedCurrencyCode,
                currency -> {

                    selectedCurrency = currency;

                    updateCurrentCurrencyPreview(currency);

                    btnSaveCurrency.setEnabled(true);
                }
        );

        recyclerViewCurrencies.setAdapter(currencyAdapter);
    }

    private void setupSearch() {

        etSearchCurrency.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence text,
                            int start,
                            int count,
                            int after
                    ) {
                        // No action required
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence text,
                            int start,
                            int before,
                            int count
                    ) {

                        if (currencyAdapter != null) {
                            currencyAdapter
                                    .getFilter()
                                    .filter(text);
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable
                    ) {
                        // No action required
                    }
                }
        );
    }

    private void setupButtons() {

        btnSaveCurrency.setEnabled(false);

        btnRetryCurrencies.setOnClickListener(
                view -> loadCurrenciesFromApi()
        );

        btnSaveCurrency.setOnClickListener(
                view -> saveSelectedCurrency()
        );
    }

    private void saveSelectedCurrency() {

        if (currentUserId == -1) {

            Toast.makeText(
                    this,
                    "User profile not available.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (selectedCurrency == null) {

            Toast.makeText(
                    this,
                    "Please select a currency.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnSaveCurrency.setEnabled(false);

        boolean saved =
                databaseHelper.saveCurrencyPreference(
                        currentUserId,
                        selectedCurrency.getIsoCode(),
                        selectedCurrency.getName(),
                        selectedCurrency.getSymbol()
                );

        if (saved) {

            savedCurrencyCode =
                    selectedCurrency.getIsoCode();

            currencyAdapter.setSelectedCurrencyCode(
                    savedCurrencyCode
            );

            Toast.makeText(
                    this,
                    "Currency updated successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            btnSaveCurrency.setEnabled(true);

            Toast.makeText(
                    this,
                    "Failed to save currency.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void updateCurrentCurrencyPreview(
            CurrencyModel currency
    ) {

        if (currency == null) {
            return;
        }

        tvCurrentCurrencyName.setText(
                currency.getName()
        );

        tvCurrentCurrencyCode.setText(
                currency.getIsoCode()
        );

        tvCurrentCurrencySymbol.setText(
                currency.getSymbol()
        );
    }

    private void showLoading(boolean loading) {

        progressCurrency.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        layoutCurrencyError.setVisibility(View.GONE);

        recyclerViewCurrencies.setVisibility(
                loading ? View.INVISIBLE : View.VISIBLE
        );

        btnSaveCurrency.setEnabled(
                !loading && selectedCurrency != null
        );
    }

    private void showError(String message) {

        progressCurrency.setVisibility(View.GONE);

        recyclerViewCurrencies.setVisibility(View.GONE);

        layoutCurrencyError.setVisibility(View.VISIBLE);

        tvCurrencyError.setText(message);
    }

    private void showCurrencyList() {

        progressCurrency.setVisibility(View.GONE);

        layoutCurrencyError.setVisibility(View.GONE);

        recyclerViewCurrencies.setVisibility(View.VISIBLE);
    }

    private void loadSavedCurrency() {

        if (currentUserId == -1) {
            return;
        }

        savedCurrencyCode =
                databaseHelper.getSavedCurrencyCode(
                        currentUserId
                );

        currencyAdapter.setSelectedCurrencyCode(
                savedCurrencyCode
        );
    }

    private void loadCurrenciesFromApi() {

        showLoading(true);

        layoutCurrencyError.setVisibility(View.GONE);

        CurrencyApiService apiService =
                RetrofitClient.getCurrencyApiService();

        apiService.getCurrencies().enqueue(
                new Callback<List<CurrencyModel>>() {

                    @Override
                    public void onResponse(
                            Call<List<CurrencyModel>> call,
                            Response<List<CurrencyModel>> response
                    ) {

                        if (!response.isSuccessful()) {

                            showError(
                                    "Unable to load currencies. Error code: "
                                            + response.code()
                            );

                            return;
                        }

                        List<CurrencyModel> apiCurrencies =
                                response.body();

                        if (apiCurrencies == null
                                || apiCurrencies.isEmpty()) {

                            showError(
                                    "No currency data received."
                            );

                            return;
                        }

                        Collections.sort(
                                apiCurrencies,
                                Comparator.comparing(
                                        CurrencyModel::getName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        );

                        currencyList.clear();
                        currencyList.addAll(apiCurrencies);

                        currencyAdapter.updateCurrencies(
                                currencyList
                        );

                        currencyAdapter.setSelectedCurrencyCode(
                                savedCurrencyCode
                        );

                        CurrencyModel savedCurrency =
                                findCurrencyByCode(
                                        savedCurrencyCode
                                );

                        if (savedCurrency != null) {

                            selectedCurrency = savedCurrency;

                            updateCurrentCurrencyPreview(
                                    savedCurrency
                            );

                        } else {

                            CurrencyModel defaultCurrency =
                                    findCurrencyByCode("LKR");

                            if (defaultCurrency != null) {

                                selectedCurrency =
                                        defaultCurrency;

                                savedCurrencyCode = "LKR";

                                currencyAdapter
                                        .setSelectedCurrencyCode(
                                                "LKR"
                                        );

                                updateCurrentCurrencyPreview(
                                        defaultCurrency
                                );
                            }
                        }

                        showCurrencyList();

                        btnSaveCurrency.setEnabled(
                                selectedCurrency != null
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<List<CurrencyModel>> call,
                            Throwable throwable
                    ) {

                        showError(
                                "Unable to connect to the currency service. "
                                        + throwable.getMessage()
                        );
                    }
                }
        );
    }

    private CurrencyModel findCurrencyByCode(
            String currencyCode
    ) {

        if (currencyCode == null) {
            return null;
        }

        for (CurrencyModel currency : currencyList) {

            if (currency.getIsoCode() != null
                    && currency.getIsoCode()
                    .equalsIgnoreCase(currencyCode)) {

                return currency;
            }
        }

        return null;
    }
}