package com.example.aiexpensemanagementapplication.ui.expense;

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
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;

import java.util.ArrayList;
import java.util.Locale;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class ExpenseAdapter
        extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    // =========================================================
    // VARIABLES
    // =========================================================

    private final Context context;

    private ArrayList<ExpenseModel> expenseList;

    private final OnExpenseClickListener listener;

    private final DatabaseHelper databaseHelper;


    // =========================================================
    // CLICK LISTENER
    // =========================================================

    public interface OnExpenseClickListener {

        void onExpenseClick(ExpenseModel expense);

        void onEditClick(ExpenseModel expense);

        void onDeleteClick(ExpenseModel expense);
    }


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ExpenseAdapter(
            Context context,
            ArrayList<ExpenseModel> expenseList,
            OnExpenseClickListener listener
    ) {

        this.context = context;

        this.expenseList = expenseList;

        this.listener = listener;

        this.databaseHelper =
                new DatabaseHelper(context);
    }


    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(context)
                        .inflate(
                                R.layout.item_expense,
                                parent,
                                false
                        );

        return new ExpenseViewHolder(view);
    }


    // =========================================================
    // BIND VIEW HOLDER
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull ExpenseViewHolder holder,
            int position
    ) {

        ExpenseModel expense =
                expenseList.get(position);


        // -----------------------------------------------------
        // CATEGORY
        // -----------------------------------------------------

        String categoryName =
                expense.getCategoryName();

        holder.tvCategory.setText(
                categoryName != null
                        ? categoryName
                        : "Expense"
        );


        // -----------------------------------------------------
        // AMOUNT
        // -----------------------------------------------------

        holder.tvAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "Rs. %.2f",
                        expense.getAmount()
                )
        );


        // -----------------------------------------------------
        // NOTE
        // -----------------------------------------------------

        String note =
                expense.getNote();

        if (note == null ||
                note.trim().isEmpty()) {

            holder.tvNote.setVisibility(
                    View.GONE
            );

        } else {

            holder.tvNote.setVisibility(
                    View.VISIBLE
            );

            holder.tvNote.setText(
                    note
            );
        }


        // -----------------------------------------------------
        // PAYMENT METHOD
        // -----------------------------------------------------

        String paymentMethod =
                expense.getPaymentMethod();

        holder.tvPayment.setText(
                paymentMethod != null
                        ? paymentMethod
                        : ""
        );


        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        holder.tvDate.setText(
                expense.getTransactionDate()
        );


        // =====================================================
        // FAMILY SHARING
        // =====================================================

        int familyId =
                databaseHelper.getFamilyIdForExpense(
                        expense.getTransactionId()
                );


        if (familyId != -1) {

            String familyName =
                    databaseHelper.getFamilyNameById(
                            familyId
                    );


            if (familyName != null &&
                    !familyName.trim().isEmpty()) {

                holder.tvFamilyShare.setVisibility(
                        View.VISIBLE
                );

                holder.tvFamilyShare.setText(
                        "Shared with " + familyName
                );

            } else {

                holder.tvFamilyShare.setVisibility(
                        View.GONE
                );
            }

        } else {

            // Personal-only expense.
            // No badge is necessary.

            holder.tvFamilyShare.setVisibility(
                    View.GONE
            );
        }


        // =====================================================
        // CATEGORY ICON
        // =====================================================

        if (categoryName != null) {

            switch (categoryName) {

                case "Food":

                    holder.imgCategory.setImageResource(
                            R.drawable.ic_food
                    );

                    break;


                case "Transport":

                    holder.imgCategory.setImageResource(
                            R.drawable.ic_transport
                    );

                    break;


                case "Shopping":

                    holder.imgCategory.setImageResource(
                            R.drawable.ic_shopping
                    );

                    break;


                case "Bills":

                    holder.imgCategory.setImageResource(
                            R.drawable.current_bill
                    );

                    break;


                default:

                    holder.imgCategory.setImageResource(
                            R.drawable.expense
                    );

                    break;
            }

        } else {

            holder.imgCategory.setImageResource(
                    R.drawable.expense
            );
        }


        // =====================================================
        // OPEN EXPENSE DETAILS
        // =====================================================

        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onExpenseClick(
                                expense
                        );
                    }
                }
        );


        // =====================================================
        // MENU
        // =====================================================

        holder.btnMenu.setOnClickListener(
                v -> showExpenseMenu(
                        holder,
                        expense
                )
        );
    }


    // =========================================================
    // EXPENSE MENU
    // =========================================================

    private void showExpenseMenu(
            ExpenseViewHolder holder,
            ExpenseModel expense
    ) {

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


        popupMenu.setOnMenuItemClickListener(
                item -> {

                    int itemId =
                            item.getItemId();


                    // -------------------------------------------------
                    // EDIT
                    // -------------------------------------------------

                    if (itemId == R.id.actionEdit) {

                        if (listener != null) {

                            listener.onEditClick(
                                    expense
                            );
                        }

                        return true;
                    }


                    // -------------------------------------------------
                    // DELETE
                    // -------------------------------------------------

                    if (itemId == R.id.actionDelete) {

                        if (listener != null) {

                            listener.onDeleteClick(
                                    expense
                            );
                        }

                        return true;
                    }


                    return false;
                }
        );


        popupMenu.show();
    }


    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return expenseList != null
                ? expenseList.size()
                : 0;
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class ExpenseViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgCategory;

        TextView tvCategory;
        TextView tvAmount;
        TextView tvNote;
        TextView tvPayment;

        // NEW
        TextView tvFamilyShare;

        TextView tvDate;

        ImageButton btnMenu;


        public ExpenseViewHolder(
                @NonNull View itemView
        ) {

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


            // -------------------------------------------------
            // NEW FAMILY SHARE LABEL
            // -------------------------------------------------

            tvFamilyShare =
                    itemView.findViewById(
                            R.id.tvFamilyShare
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


    // =========================================================
    // UPDATE LIST
    // =========================================================

    public void updateList(
            ArrayList<ExpenseModel> list
    ) {

        if (list == null) {

            expenseList =
                    new ArrayList<>();

        } else {

            expenseList =
                    list;
        }


        notifyDataSetChanged();
    }
}