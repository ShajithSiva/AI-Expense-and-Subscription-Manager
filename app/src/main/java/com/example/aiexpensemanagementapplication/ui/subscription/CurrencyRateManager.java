package com.example.aiexpensemanagementapplication.ui.subscription;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fetches daily exchange rates from ExchangeRate-API's free open endpoint.
 * Rates are quoted as 1 USD = rates[code]. Conversion to LKR is therefore
 * amount / rates[sourceCurrency] * rates[LKR].
 */
public class CurrencyRateManager {
    private static final String TAG = "CurrencyRateManager";
    private static final String URL_RATES = "https://open.er-api.com/v6/latest/USD";
    private static final String PREFS = "subscription_currency_rates";
    private static final String KEY_RATES = "rates_json";
    private static final String KEY_UPDATED = "provider_updated";
    private static final String KEY_FETCHED_AT = "fetched_at";
    private static final long CACHE_MAX_AGE_MS = 24L * 60L * 60L * 1000L;

    public interface Callback {
        void onRates(Map<String, Double> rates, String updatedAt, boolean fromCache, String error);
    }

    private final Context appContext;
    private final SharedPreferences preferences;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile Map<String, Double> currentRates = Collections.emptyMap();
    private volatile String updatedAt = "";

    public CurrencyRateManager(Context context) {
        appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        loadCachedRates();
    }

    public Map<String, Double> getRates() {
        return currentRates;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void refresh(boolean force, Callback callback) {
        long fetchedAt = preferences.getLong(KEY_FETCHED_AT, 0L);
        boolean cacheFresh = !currentRates.isEmpty() &&
                (System.currentTimeMillis() - fetchedAt) < CACHE_MAX_AGE_MS;

        if (cacheFresh && !force) {
            if (callback != null) callback.onRates(currentRates, updatedAt, true, null);
            return;
        }

        // Return cached values immediately while refreshing in the background.
        if (!currentRates.isEmpty() && callback != null) {
            callback.onRates(currentRates, updatedAt, true, null);
        }

        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(URL_RATES).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(12000);
                connection.setRequestProperty("Accept", "application/json");
                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                if (stream == null) throw new IllegalStateException("No response from exchange-rate service (HTTP " + status + ")");
                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }
                if (status < 200 || status >= 300) throw new IllegalStateException("Exchange-rate service HTTP " + status);

                JSONObject root = new JSONObject(body.toString());
                if (!"success".equalsIgnoreCase(root.optString("result"))) {
                    throw new IllegalStateException("Exchange-rate service returned an unsuccessful result");
                }
                JSONObject ratesObject = root.optJSONObject("rates");
                if (ratesObject == null || !ratesObject.has("LKR")) {
                    throw new IllegalStateException("LKR rate is missing from the exchange-rate response");
                }
                Map<String, Double> parsed = new HashMap<>();
                Iterator<String> keys = ratesObject.keys();
                while (keys.hasNext()) {
                    String code = keys.next().toUpperCase(java.util.Locale.ROOT);
                    double value = ratesObject.optDouble(code, Double.NaN);
                    if (!Double.isNaN(value) && !Double.isInfinite(value) && value > 0) parsed.put(code, value);
                }
                if (!parsed.containsKey("USD") || !parsed.containsKey("LKR")) {
                    throw new IllegalStateException("Required USD/LKR rates are missing");
                }
                String providerUpdated = root.optString("time_last_update_utc", "");
                JSONObject cache = new JSONObject();
                for (Map.Entry<String, Double> entry : parsed.entrySet()) cache.put(entry.getKey(), entry.getValue());
                preferences.edit()
                        .putString(KEY_RATES, cache.toString())
                        .putString(KEY_UPDATED, providerUpdated)
                        .putLong(KEY_FETCHED_AT, System.currentTimeMillis())
                        .apply();
                currentRates = Collections.unmodifiableMap(parsed);
                updatedAt = providerUpdated;
                notifyCallback(callback, currentRates, updatedAt, false, null);
            } catch (Exception e) {
                Log.w(TAG, "Unable to refresh exchange rates", e);
                String error = e.getMessage() == null ? "Exchange rates are temporarily unavailable" : e.getMessage();
                notifyCallback(callback, currentRates, updatedAt, true, error);
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private void loadCachedRates() {
        String json = preferences.getString(KEY_RATES, "");
        updatedAt = preferences.getString(KEY_UPDATED, "");
        if (json.isEmpty()) return;
        try {
            JSONObject object = new JSONObject(json);
            Map<String, Double> parsed = new HashMap<>();
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String code = keys.next();
                double value = object.optDouble(code, Double.NaN);
                if (!Double.isNaN(value) && !Double.isInfinite(value) && value > 0) parsed.put(code, value);
            }
            currentRates = Collections.unmodifiableMap(parsed);
        } catch (Exception e) {
            Log.w(TAG, "Ignoring invalid cached exchange rates", e);
        }
    }

    private void notifyCallback(Callback callback, Map<String, Double> rates,
                                String updateTime, boolean fromCache, String error) {
        if (callback == null) return;
        mainHandler.post(() -> callback.onRates(rates, updateTime, fromCache, error));
    }

    public static Double convertToLkr(double amount, String currency, Map<String, Double> rates) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0 || currency == null || rates == null) return null;
        String code = currency.trim().toUpperCase(java.util.Locale.ROOT);
        if (code.equals("LKR") || code.equals("RS") || code.equals("RS.")) return amount;
        if (code.equals("US$")) code = "USD";
        else if (code.equals("€")) code = "EUR";
        else if (code.equals("£")) code = "GBP";
        else if (code.equals("₹")) code = "INR";
        else if (code.equals("A$")) code = "AUD";
        else if (code.equals("C$")) code = "CAD";
        else if (code.equals("S$")) code = "SGD";
        // A bare '$' is ambiguous (USD, CAD, AUD, SGD, etc.); never guess silently.
        if (code.isEmpty() || code.equals("$") || code.length() != 3) return null;
        Double sourceRate = rates.get(code);
        Double lkrRate = rates.get("LKR");
        if (sourceRate == null || lkrRate == null || sourceRate <= 0 || lkrRate <= 0) return null;
        return amount / sourceRate * lkrRate;
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
