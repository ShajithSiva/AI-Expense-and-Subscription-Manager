package com.example.aiexpensemanagementapplication.ui.ai;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class AdvisorMessageAdapter
        extends RecyclerView.Adapter<AdvisorMessageAdapter.MessageViewHolder> {

    private final ArrayList<AdvisorMessage> messages;

    public AdvisorMessageAdapter(ArrayList<AdvisorMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_advisor_message,
                        parent,
                        false
                );

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MessageViewHolder holder,
            int position) {

        AdvisorMessage message = messages.get(position);

        holder.tvMessage.setText(message.getMessage());

        if (message.getType() == AdvisorMessage.TYPE_AI) {

            // -----------------------------------------
            // AI MESSAGE
            // -----------------------------------------

            holder.cardAIIcon.setVisibility(View.VISIBLE);

            holder.cardMessage.setCardBackgroundColor(
                    Color.WHITE
            );

            holder.tvMessage.setTextColor(
                    Color.parseColor("#111827")
            );

            holder.itemView.setLayoutDirection(
                    View.LAYOUT_DIRECTION_LTR
            );

        } else {

            // -----------------------------------------
            // USER MESSAGE
            // -----------------------------------------

            holder.cardAIIcon.setVisibility(View.GONE);

            holder.cardMessage.setCardBackgroundColor(
                    Color.parseColor("#DCFCE7")
            );

            holder.tvMessage.setTextColor(
                    Color.parseColor("#166534")
            );

            holder.itemView.setLayoutDirection(
                    View.LAYOUT_DIRECTION_RTL
            );
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(AdvisorMessage message) {

        messages.add(message);

        notifyItemInserted(
                messages.size() - 1
        );
    }

    static class MessageViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvMessage;

        MaterialCardView cardMessage;
        MaterialCardView cardAIIcon;

        public MessageViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvMessage =
                    itemView.findViewById(
                            R.id.tvMessage
                    );

            cardMessage =
                    itemView.findViewById(
                            R.id.cardMessage
                    );

            cardAIIcon =
                    itemView.findViewById(
                            R.id.cardAIIcon
                    );
        }
    }
}