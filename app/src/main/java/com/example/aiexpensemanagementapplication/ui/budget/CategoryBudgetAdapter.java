package com.example.aiexpensemanagementapplication.ui.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

public class CategoryBudgetAdapter
        extends RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder> {

    private final List<CategoryBudget> categoryList;

    public CategoryBudgetAdapter(
            List<CategoryBudget> categoryList) {

        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_category_budget,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        CategoryBudget category =
                categoryList.get(position);

        holder.tvCategoryName.setText(
                category.getCategoryName()
        );

        holder.tvCategoryAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "Rs. %.2f / Rs. %.2f",
                        category.getSpentAmount(),
                        category.getBudgetAmount()
                )
        );

        int percentage =
                category.getPercentageUsed();

        holder.categoryProgress.setProgress(
                percentage
        );

        holder.tvCategoryPercentage.setText(
                percentage + "% used"
        );
    }

    @Override
    public int getItemCount() {

        return categoryList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCategoryName;

        TextView tvCategoryAmount;

        TextView tvCategoryPercentage;

        LinearProgressIndicator categoryProgress;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            tvCategoryName =
                    itemView.findViewById(
                            R.id.tvCategoryName
                    );

            tvCategoryAmount =
                    itemView.findViewById(
                            R.id.tvCategoryAmount
                    );

            tvCategoryPercentage =
                    itemView.findViewById(
                            R.id.tvCategoryPercentage
                    );

            categoryProgress =
                    itemView.findViewById(
                            R.id.categoryProgress
                    );
        }
    }
}