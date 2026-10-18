package com.example.aiexpensemanagementapplication.ui.subscription;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.model.PendingSubscription;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class PendingSubscriptionAdapter
        extends RecyclerView.Adapter<
        PendingSubscriptionAdapter.PendingViewHolder> {

    public interface OnPendingSubscriptionClickListener {
        void onPendingSubscriptionClick(
                PendingSubscription subscription
        );
    }

    private final Context context;
    private final ArrayList<PendingSubscription> pendingList;
    private final OnPendingSubscriptionClickListener listener;

    public PendingSubscriptionAdapter(
            Context context,
            ArrayList<PendingSubscription> pendingList,
            OnPendingSubscriptionClickListener listener
    ) {
        this.context = context;
        this.pendingList = pendingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_pending_subscription,
                parent,
                false
        );

        return new PendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PendingViewHolder holder,
            int position
    ) {
        PendingSubscription subscription =
                pendingList.get(position);

        if (subscription == null) {
            return;
        }

        // Service name
        String serviceName = safeString(
                subscription.getServiceName()
        );

        holder.tvServiceName.setText(
                serviceName.isEmpty()
                        ? "Unknown Service"
                        : serviceName
        );

        // Amount
        holder.tvAmount.setText(
                subscription.getDisplayAmount()
        );

        // Billing cycle
        holder.tvBillingCycle.setText(
                subscription.getDisplayBillingCycle()
        );

        // Next billing date
        holder.tvNextBillingDate.setText(
                subscription.getDisplayNextBillingDate()
        );

        // Email subject
        String emailSubject = safeString(
                subscription.getEmailSubject()
        );

        holder.tvEmailSubject.setText(
                emailSubject.isEmpty()
                        ? "Detected from Gmail"
                        : emailSubject
        );

        // Review status
        holder.chipStatus.setText("Review");

        // Open review screen
        holder.cardPendingSubscription.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPendingSubscriptionClick(subscription);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingList == null
                ? 0
                : pendingList.size();
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    public static class PendingViewHolder
            extends RecyclerView.ViewHolder {

        MaterialCardView cardPendingSubscription;
        TextView tvServiceName;
        TextView tvAmount;
        TextView tvBillingCycle;
        TextView tvNextBillingDate;
        TextView tvEmailSubject;
        Chip chipStatus;

        public PendingViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            cardPendingSubscription =
                    itemView.findViewById(
                            R.id.cardPendingSubscription
                    );

            tvServiceName =
                    itemView.findViewById(
                            R.id.tvServiceName
                    );

            tvAmount =
                    itemView.findViewById(
                            R.id.tvAmount
                    );

            tvBillingCycle =
                    itemView.findViewById(
                            R.id.tvBillingCycle
                    );

            tvNextBillingDate =
                    itemView.findViewById(
                            R.id.tvNextBillingDate
                    );

            tvEmailSubject =
                    itemView.findViewById(
                            R.id.tvEmailSubject
                    );

            chipStatus =
                    itemView.findViewById(
                            R.id.chipStatus
                    );
        }
    }
}
