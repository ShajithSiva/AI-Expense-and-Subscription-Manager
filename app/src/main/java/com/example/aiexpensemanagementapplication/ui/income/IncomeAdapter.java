package com.example.aiexpensemanagementapplication.ui.income;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;

import java.util.ArrayList;
import java.util.Locale;

public class IncomeAdapter
        extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    private final Context context;
    private ArrayList<IncomeModel> incomeList;

    public interface OnIncomeClickListener {

        void onIncomeClick(IncomeModel income);

        void onEditClick(IncomeModel income);

        void onDeleteClick(IncomeModel income);
    }

    private final OnIncomeClickListener listener;

    public IncomeAdapter(
            Context context,
            ArrayList<IncomeModel> incomeList,
            OnIncomeClickListener listener) {

        this.context = context;
        this.incomeList = incomeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater
                .from(context)
                .inflate(
                        R.layout.item_income,
                        parent,
                        false
                );

        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull IncomeViewHolder holder,
            int position) {

        IncomeModel income = incomeList.get(position);

        holder.tvCategory.setText(
                income.getCategoryName()
        );

        holder.tvAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "Rs. %.2f",
                        income.getAmount()
                )
        );

        String note = income.getNote();

        if (note == null || note.trim().isEmpty()) {

            holder.tvNote.setText("No notes");

        } else {

            holder.tvNote.setText(note);
        }

        String incomeSource =
                income.getIncomeSource();

        if (incomeSource == null ||
                incomeSource.trim().isEmpty()) {

            holder.tvPayment.setText(
                    "Income source not specified"
            );

        } else {

            holder.tvPayment.setText(
                    incomeSource
            );
        }

        holder.tvDate.setText(
                income.getTransactionDate()
        );

        setCategoryIcon(
                holder.imgCategory,
                income.getCategoryName()
        );

        holder.itemView.setOnClickListener(
                view -> listener.onIncomeClick(income)
        );

        holder.btnMenu.setOnClickListener(view -> {

            PopupMenu popupMenu =
                    new PopupMenu(
                            context,
                            holder.btnMenu
                    );

            MenuInflater inflater =
                    popupMenu.getMenuInflater();

            inflater.inflate(
                    R.menu.menu_expense,
                    popupMenu.getMenu()
            );

            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.actionEdit) {

                    listener.onEditClick(income);

                    return true;

                } else if (item.getItemId() ==
                        R.id.actionDelete) {

                    listener.onDeleteClick(income);

                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    private void setCategoryIcon(
            ImageView imageView,
            String category) {

        /*
         * Currently all income categories use the
         * same common money icon.
         */
        imageView.setImageResource(
                R.drawable.money
        );
    }

    @Override
    public int getItemCount() {

        return incomeList == null
                ? 0
                : incomeList.size();
    }

    public void updateList(
            ArrayList<IncomeModel> list) {

        if (list == null) {

            incomeList = new ArrayList<>();

        } else {

            incomeList = list;
        }

        notifyDataSetChanged();
    }

    static class IncomeViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgCategory;

        TextView tvCategory;
        TextView tvAmount;
        TextView tvNote;
        TextView tvPayment;
        TextView tvDate;

        ImageButton btnMenu;

        public IncomeViewHolder(
                @NonNull View itemView) {

            super(itemView);

            imgCategory =
                    itemView.findViewById(
                            R.id.imgCategory
                    );

            tvCategory =
                    itemView.findViewById(
                            R.id.tvCategory
                    );

            tvAmount =
                    itemView.findViewById(
                            R.id.tvAmount
                    );

            tvNote =
                    itemView.findViewById(
                            R.id.tvNote
                    );

            tvPayment =
                    itemView.findViewById(
                            R.id.tvPayment
                    );

            tvDate =
                    itemView.findViewById(
                            R.id.tvDate
                    );

            btnMenu =
                    itemView.findViewById(
                            R.id.btnMenu
                    );
        }
    }
}