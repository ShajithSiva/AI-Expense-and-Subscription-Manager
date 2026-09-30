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

    // =========================================================
    // LISTENER
    // =========================================================

    public interface OnPendingSubscriptionClickListener {

        void onPendingSubscriptionClick(
                PendingSubscription subscription
        );
    }


    // =========================================================
    // FIELDS
    // =========================================================

    private final Context context;

    private final ArrayList<PendingSubscription> pendingList;

    private final OnPendingSubscriptionClickListener listener;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public PendingSubscriptionAdapter(
            Context context,
            ArrayList<PendingSubscription> pendingList,
            OnPendingSubscriptionClickListener listener
    ) {

        this.context = context;

        this.pendingList = pendingList;

        this.listener = listener;
    }


    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(context)
                        .inflate(
                                R.layout.item_pending_subscription,
                                parent,
                                false
                        );


        return new PendingViewHolder(
                view
        );
    }


    // =========================================================
    // BIND DATA
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull PendingViewHolder holder,
            int position
    ) {

        PendingSubscription subscription =
                pendingList.get(
                        position
                );


        if (subscription == null) {

            return;
        }


        // =====================================================
        // SERVICE NAME
        // =====================================================

        String serviceName =
                safeString(
                        subscription.getServiceName()
                );


        if (serviceName.isEmpty()) {

            serviceName =
                    "Unknown Service";
        }


        holder.tvServiceName.setText(
                serviceName
        );


        // =====================================================
        // AMOUNT
        // =====================================================

        holder.tvAmount.setText(
                subscription.getDisplayAmount()
        );


        // =====================================================
        // BILLING CYCLE
        // =====================================================

        holder.tvBillingCycle.setText(
                subscription.getDisplayBillingCycle()
        );


        // =====================================================
        // NEXT BILLING DATE
        // =====================================================

        holder.tvNextBillingDate.setText(
                subscription.getDisplayNextBillingDate()
        );


        // =====================================================
        // CONFIDENCE
        // =====================================================

        holder.tvConfidence.setText(
                "AI confidence: "
                        + subscription.getDisplayConfidence()
        );


        // =====================================================
        // EMAIL SUBJECT
        // =====================================================

        String emailSubject =
                safeString(
                        subscription.getEmailSubject()
                );


        if (emailSubject.isEmpty()) {

            holder.tvEmailSubject.setText(
                    "Detected from Gmail"
            );


        } else {

            holder.tvEmailSubject.setText(
                    emailSubject
            );
        }


        // =====================================================
        // STATUS
        // =====================================================

        holder.chipStatus.setText(
                "Review"
        );


        // =====================================================
        // CLICK
        // =====================================================

        holder.cardPendingSubscription
                .setOnClickListener(
                        v -> {

                            if (listener != null) {

                                listener
                                        .onPendingSubscriptionClick(
                                                subscription
                                        );
                            }
                        }
                );
    }


    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        if (pendingList == null) {

            return 0;
        }


        return pendingList.size();
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safeString(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value.trim();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class PendingViewHolder
            extends RecyclerView.ViewHolder {

        MaterialCardView cardPendingSubscription;

        TextView tvServiceName;

        TextView tvAmount;

        TextView tvBillingCycle;

        TextView tvNextBillingDate;

        TextView tvConfidence;

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


            tvConfidence =
                    itemView.findViewById(
                            R.id.tvConfidence
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