
package com.example.aiexpensemanagementapplication.ui.subscription;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.model.Subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class SubscriptionAdapter
        extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Subscription> subscriptionList;
    private final OnSubscriptionClickListener listener;
    private Map<String, Double> exchangeRates = Collections.emptyMap();

    public SubscriptionAdapter(
            Context context,
            ArrayList<Subscription> subscriptionList,
            OnSubscriptionClickListener listener) {

        this.context = context;
        this.subscriptionList = subscriptionList;
        this.listener = listener;
    }

    public void setExchangeRates(Map<String, Double> rates) {
        exchangeRates = rates == null
                ? Collections.emptyMap() : rates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_subscription, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Subscription subscription = subscriptionList.get(position);

        // Service name
        String serviceName = safeString(subscription.getServiceName());

        holder.tvServiceName.setText(
                serviceName.isEmpty() ? "Unknown service" : serviceName
        );

        // Subscription logo
        holder.ivLogo.setImageResource(getLogoResource(serviceName));

        // Billing cycle
        String billingCycle = safeString(subscription.getBillingCycle());

        holder.tvPlanType.setText(
                billingCycle.isEmpty()
                        ? "Billing cycle not set" : billingCycle
        );

        // Amount and currency
        String currency = safeString(subscription.getCurrency());

        String amount = String.format(
                Locale.getDefault(),
                "%.2f",
                subscription.getAmount()
        );

        holder.tvAmount.setText(
                currency.isEmpty() ? amount : currency + " " + amount
        );

        // Monthly amount calculation
        String cycle = billingCycle.toLowerCase(Locale.ROOT);
        double monthlyAmount = subscription.getAmount();

        if (cycle.contains("year") || cycle.contains("annual")) {
            monthlyAmount /= 12.0;
        } else if (cycle.contains("quarter")) {
            monthlyAmount /= 3.0;
        } else if (cycle.contains("week")) {
            monthlyAmount = monthlyAmount * 52.0 / 12.0;
        }

        Double lkr = CurrencyRateManager.convertToLkr(
                monthlyAmount, currency, exchangeRates
        );

        if (lkr == null) {
            holder.tvLkrEstimate.setText(
                    currency.equals("$") || currency.isEmpty()
                            ? "Set currency code to convert"
                            : "LKR rate unavailable"
            );
        } else {
            holder.tvLkrEstimate.setText(
                    String.format(
                            Locale.US,
                            "≈ LKR %,.2f / month",
                            lkr
                    )
            );
        }

        // Next billing date
        String nextBillingDate =
                safeString(subscription.getNextBillingDate());

        if (nextBillingDate.isEmpty()) {
            holder.tvDueDate.setText("Date not set");
            holder.tvUsage.setText("Billing date unavailable");
        } else {
            holder.tvDueDate.setText(nextBillingDate);
            holder.tvUsage.setText("Next payment scheduled");
        }

        // Open subscription details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubscriptionClick(subscription);
            }
        });
    }

    private int getLogoResource(String serviceName) {
        String name = serviceName.toLowerCase(Locale.ROOT);

        if (name.contains("netflix")) {
            return R.drawable.logo_netflix;
        } else if (name.contains("youtube")) {
            return R.drawable.logo_youtube;
        } else if (name.contains("spotify")) {
            return R.drawable.logo_spotify;
        } else if (name.contains("facebook")) {
            return R.drawable.logo_facebook;
        } else if (name.contains("instagram")) {
            return R.drawable.logo_instagram;
        } else if (name.contains("whatsapp")) {
            return R.drawable.logo_whatsapp;
        } else if (name.contains("tiktok")) {
            return R.drawable.logo_tiktok;
        } else if (name.contains("prime video")
                || name.contains("amazon prime")) {
            return R.drawable.logo_prime_video;
        } else if (name.contains("disney")) {
            return R.drawable.logo_disney;
        } else if (name.contains("apple music")) {
            return R.drawable.logo_apple_music;
        } else if (name.contains("google one")) {
            return R.drawable.logo_google_one;
        } else if (name.contains("chatgpt")
                || name.contains("openai")) {
            return R.drawable.logo_chatgpt;
        } else if (name.contains("canva")) {
            return R.drawable.logo_canva;
        } else if (name.contains("linkedin")) {
            return R.drawable.logo_linkedin;
        } else if (name.contains("zoom")) {
            return R.drawable.logo_zoom;
        } else if (name.contains("dropbox")) {
            return R.drawable.logo_dropbox;
        } else if (name.contains("microsoft 365")
                || name.contains("office 365")) {
            return R.drawable.logo_microsoft365;
        } else if (name.contains("google drive")
                || name.equals("drive")) {
            return R.drawable.logo_drive;
        } else if (name.contains("onedrive")) {
            return R.drawable.logo_onedrive;
        } else if (name.contains("adobe")) {
            return R.drawable.logo_adobe;
        } else if (name.contains("notion")) {
            return R.drawable.logo_notion;
        } else if (name.contains("slack")) {
            return R.drawable.logo_slack;
        } else if (name.contains("github")) {
            return R.drawable.logo_github;
        }

        return R.drawable.subscribtion;
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public int getItemCount() {
        return subscriptionList == null
                ? 0 : subscriptionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivLogo;
        TextView tvServiceName;
        TextView tvPlanType;
        TextView tvAmount;
        TextView tvLkrEstimate;
        TextView tvUsage;
        TextView tvDueDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvPlanType = itemView.findViewById(R.id.tvPlanType);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvLkrEstimate = itemView.findViewById(R.id.tvLkrEstimate);
            tvUsage = itemView.findViewById(R.id.tvUsage);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
        }
    }

    public interface OnSubscriptionClickListener {
        void onSubscriptionClick(Subscription subscription);
    }
}
