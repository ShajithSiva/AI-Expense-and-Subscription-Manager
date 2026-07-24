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
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Subscription> subscriptionList;

    public SubscriptionAdapter(Context context,
                               ArrayList<Subscription> subscriptionList) {

        this.context = context;
        this.subscriptionList = subscriptionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_subscription, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Subscription subscription = subscriptionList.get(position);

        holder.tvServiceName.setText(subscription.getServiceName());

        holder.tvPlanType.setText(subscription.getBillingCycle());

        holder.tvAmount.setText(
                "Rs " + subscription.getAmount());

        holder.tvDueDate.setText(
                subscription.getNextBillingDate());

        holder.chipStatus.setText("Active");

        holder.tvUsage.setText("Subscription Active");
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivLogo;

        TextView tvServiceName;
        TextView tvPlanType;
        TextView tvAmount;
        TextView tvUsage;
        TextView tvDueDate;

        Chip chipStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivLogo = itemView.findViewById(R.id.ivLogo);

            tvServiceName = itemView.findViewById(R.id.tvServiceName);

            tvPlanType = itemView.findViewById(R.id.tvPlanType);

            tvAmount = itemView.findViewById(R.id.tvAmount);

            tvUsage = itemView.findViewById(R.id.tvUsage);

            tvDueDate = itemView.findViewById(R.id.tvDueDate);

            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }



}