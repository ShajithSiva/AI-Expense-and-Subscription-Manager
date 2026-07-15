package com.example.aiexpensemanagementapplication.ui.expense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private ArrayList<ExpenseModel> expenseList;

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseModel expense);
        void onEditClick(ExpenseModel expense);
        void onDeleteClick(ExpenseModel expense);
    }

    private OnExpenseClickListener listener;

    public ExpenseAdapter(Context context,
                          ArrayList<ExpenseModel> expenseList,
                          OnExpenseClickListener listener) {

        this.context = context;
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_expense, parent, false);

        return new ExpenseViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {

        ExpenseModel expense = expenseList.get(position);

        holder.tvCategory.setText(expense.getCategoryName());

        holder.tvAmount.setText(
                String.format("Rs. %.2f", expense.getAmount()));

        holder.tvNote.setText(expense.getNote());

        holder.tvPayment.setText(expense.getPaymentMethod());

        holder.tvMode.setText(expense.getExpenseMode());

        holder.tvDate.setText(expense.getTransactionDate());

        // Category Icons

        switch (expense.getCategoryName()) {

            case "Food":
                holder.imgCategory.setImageResource(R.drawable.ic_food);
                break;

            case "Transport":
                holder.imgCategory.setImageResource(R.drawable.ic_transport);
                break;

            case "Shopping":
                holder.imgCategory.setImageResource(R.drawable.ic_shopping);
                break;

            case "Bills":
                holder.imgCategory.setImageResource(R.drawable.current_bill);
                break;

            default:
                holder.imgCategory.setImageResource(R.drawable.expense);
                break;
        }

        holder.itemView.setOnClickListener(v ->
                listener.onExpenseClick(expense));

        holder.btnMenu.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context, holder.btnMenu);

            MenuInflater inflater = popupMenu.getMenuInflater();

            inflater.inflate(R.menu.menu_expense, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.actionEdit) {

                    listener.onEditClick(expense);

                    return true;

                } else if (item.getItemId() == R.id.actionDelete) {

                    listener.onDeleteClick(expense);

                    return true;
                }

                return false;
            });

            popupMenu.show();

        });

    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCategory;

        TextView tvCategory;
        TextView tvAmount;
        TextView tvNote;
        TextView tvPayment;
        TextView tvMode;
        TextView tvDate;

        ImageButton btnMenu;

        public ExpenseViewHolder(@NonNull View itemView) {

            super(itemView);

            imgCategory = itemView.findViewById(R.id.imgCategory);

            tvCategory = itemView.findViewById(R.id.tvCategory);

            tvAmount = itemView.findViewById(R.id.tvAmount);

            tvNote = itemView.findViewById(R.id.tvNote);

            tvPayment = itemView.findViewById(R.id.tvPayment);

            tvMode = itemView.findViewById(R.id.tvMode);

            tvDate = itemView.findViewById(R.id.tvDate);

            btnMenu = itemView.findViewById(R.id.btnMenu);

        }
    }
    public void updateList(ArrayList<ExpenseModel> list){

        expenseList = list;

        notifyDataSetChanged();

    }
}