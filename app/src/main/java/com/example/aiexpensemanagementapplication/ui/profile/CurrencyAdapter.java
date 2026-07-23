package com.example.aiexpensemanagementapplication.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.model.CurrencyModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurrencyAdapter
        extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>
        implements Filterable {

    private final List<CurrencyModel> fullCurrencyList;
    private final List<CurrencyModel> displayedCurrencyList;

    private final OnCurrencyClickListener listener;

    private String selectedCurrencyCode;

    public CurrencyAdapter(
            List<CurrencyModel> currencyList,
            String selectedCurrencyCode,
            OnCurrencyClickListener listener
    ) {
        this.fullCurrencyList = new ArrayList<>(currencyList);
        this.displayedCurrencyList = new ArrayList<>(currencyList);
        this.selectedCurrencyCode = selectedCurrencyCode;
        this.listener = listener;
    }

    public interface OnCurrencyClickListener {
        void onCurrencyClick(CurrencyModel currency);
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_currency,
                        parent,
                        false
                );

        return new CurrencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CurrencyViewHolder holder,
            int position
    ) {

        CurrencyModel currency =
                displayedCurrencyList.get(position);

        holder.tvCurrencyName.setText(
                currency.getName()
        );

        holder.tvCurrencyCode.setText(
                currency.getIsoCode()
        );

        holder.tvCurrencySymbol.setText(
                currency.getSymbol()
        );

        boolean isSelected =
                selectedCurrencyCode != null
                        && selectedCurrencyCode.equalsIgnoreCase(
                        currency.getIsoCode()
                );

        holder.imgSelected.setVisibility(
                isSelected ? View.VISIBLE : View.GONE
        );

        holder.cardCurrency.setStrokeWidth(
                isSelected ? 2 : 1
        );

        holder.cardCurrency.setStrokeColor(
                holder.itemView.getContext().getColor(
                        isSelected
                                ? R.color.currency_selected_border
                                : R.color.currency_normal_border
                )
        );

        holder.itemView.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            CurrencyModel clickedCurrency =
                    displayedCurrencyList.get(
                            adapterPosition
                    );

            selectedCurrencyCode =
                    clickedCurrency.getIsoCode();

            notifyDataSetChanged();

            listener.onCurrencyClick(
                    clickedCurrency
            );
        });
    }

    @Override
    public int getItemCount() {
        return displayedCurrencyList.size();
    }

    public void updateCurrencies(
            List<CurrencyModel> newCurrencyList
    ) {

        fullCurrencyList.clear();
        fullCurrencyList.addAll(newCurrencyList);

        displayedCurrencyList.clear();
        displayedCurrencyList.addAll(newCurrencyList);

        notifyDataSetChanged();
    }

    public void setSelectedCurrencyCode(
            String currencyCode
    ) {

        selectedCurrencyCode = currencyCode;

        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {

            @Override
            protected FilterResults performFiltering(
                    CharSequence constraint
            ) {

                List<CurrencyModel> filteredList =
                        new ArrayList<>();

                String query = constraint == null
                        ? ""
                        : constraint.toString()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                if (query.isEmpty()) {

                    filteredList.addAll(
                            fullCurrencyList
                    );

                } else {

                    for (CurrencyModel currency
                            : fullCurrencyList) {

                        String name =
                                currency.getName() == null
                                        ? ""
                                        : currency.getName()
                                        .toLowerCase(Locale.ROOT);

                        String code =
                                currency.getIsoCode() == null
                                        ? ""
                                        : currency.getIsoCode()
                                        .toLowerCase(Locale.ROOT);

                        String symbol =
                                currency.getSymbol() == null
                                        ? ""
                                        : currency.getSymbol()
                                        .toLowerCase(Locale.ROOT);

                        if (name.contains(query)
                                || code.contains(query)
                                || symbol.contains(query)) {

                            filteredList.add(
                                    currency
                            );
                        }
                    }
                }

                FilterResults results =
                        new FilterResults();

                results.values = filteredList;
                results.count = filteredList.size();

                return results;
            }

            @Override
            protected void publishResults(
                    CharSequence constraint,
                    FilterResults results
            ) {

                displayedCurrencyList.clear();

                if (results.values != null) {

                    @SuppressWarnings("unchecked")
                    List<CurrencyModel> filteredList =
                            (List<CurrencyModel>)
                                    results.values;

                    displayedCurrencyList.addAll(
                            filteredList
                    );
                }

                notifyDataSetChanged();
            }
        };
    }

    static class CurrencyViewHolder
            extends RecyclerView.ViewHolder {

        private final MaterialCardView cardCurrency;
        private final TextView tvCurrencyName;
        private final TextView tvCurrencyCode;
        private final TextView tvCurrencySymbol;
        private final ImageView imgSelected;

        public CurrencyViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            cardCurrency =
                    itemView.findViewById(
                            R.id.cardCurrency
                    );

            tvCurrencyName =
                    itemView.findViewById(
                            R.id.tvCurrencyName
                    );

            tvCurrencyCode =
                    itemView.findViewById(
                            R.id.tvCurrencyCode
                    );

            tvCurrencySymbol =
                    itemView.findViewById(
                            R.id.tvCurrencySymbol
                    );

            imgSelected =
                    itemView.findViewById(
                            R.id.imgSelected
                    );
        }
    }
}