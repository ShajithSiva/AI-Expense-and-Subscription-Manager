
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
import java.util.Locale;
import java.util.Collections;
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
        exchangeRates = rates == null ? Collections.emptyMap() : rates;
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

        // Billing cycle
        String billingCycle = safeString(subscription.getBillingCycle());
        holder.tvPlanType.setText(
                billingCycle.isEmpty() ? "Billing cycle not set" : billingCycle
        );

        // Amount and currency
        String currency = safeString(subscription.getCurrency());

        String amount = String.format(
                Locale.getDefault(),
                "%.2f",
                subscription.getAmount()
        );

        holder.tvAmount.setText(
                currency.isEmpty()
                        ? amount
                        : currency + " " + amount
        );

        String cycle = safeString(subscription.getBillingCycle()).toLowerCase(Locale.ROOT);
        double monthlyAmount = subscription.getAmount();
        if (cycle.contains("year") || cycle.contains("annual")) monthlyAmount /= 12.0;
        else if (cycle.contains("quarter")) monthlyAmount /= 3.0;
        else if (cycle.contains("week")) monthlyAmount = monthlyAmount * 52.0 / 12.0;
        Double lkr = CurrencyRateManager.convertToLkr(monthlyAmount, currency, exchangeRates);
        if (lkr == null) {
            holder.tvLkrEstimate.setText(currency.equals("$") || currency.isEmpty()
                    ? "Set currency code to convert"
                    : "LKR rate unavailable");
        } else {
            holder.tvLkrEstimate.setText(String.format(Locale.US, "≈ LKR %,.2f / month", lkr));
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

        // Open subscription details when the card is clicked
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubscriptionClick(subscription);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionList == null ? 0 : subscriptionList.size();
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
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
