package com.example.aiexpensemanagementapplication.ui.ai;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

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

        holder.tvMessage.setText(
                formatAIMessage(message.getMessage())
        );

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

    // =====================================================
    // FORMAT AI MESSAGE
    // =====================================================

    private Spanned formatAIMessage(String message) {

        if (message == null || message.trim().isEmpty()) {

            return Html.fromHtml(
                    "",
                    Html.FROM_HTML_MODE_LEGACY
            );
        }

        String formatted =
                TextUtils.htmlEncode(message);

        // Convert numbered lists to separate lines
        formatted = formatted.replaceAll(
                "\\s+(?=\\d+\\.\\s)",
                "<br><br>"
        );

        // Convert bullet separators (*) to new lines
        formatted = formatted.replaceAll(
                "\\s+\\*\\s+(?=\\*\\*)",
                "<br><br>"
        );

        // Convert **bold** text
        formatted = formatted.replaceAll(
                "\\*\\*(.+?)\\*\\*",
                "<b>$1</b>"
        );

        return Html.fromHtml(
                formatted,
                Html.FROM_HTML_MODE_LEGACY
        );
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