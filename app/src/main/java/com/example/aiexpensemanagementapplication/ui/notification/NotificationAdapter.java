package com.example.aiexpensemanagementapplication.ui.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.format.DateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.Notification;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private DatabaseHelper db;
    private final Context context;
    private final ArrayList<Notification> list;

    public NotificationAdapter(Context context,
                               ArrayList<Notification> list) {
        this.context = context;
        this.list = list;
        db = new DatabaseHelper(context);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, message, subtitle, time;
        ImageView icon, delete;
        View unread;

        ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.txtTitle);
            message = itemView.findViewById(R.id.txtMessage);
            subtitle = itemView.findViewById(R.id.txtSubtitle);
            time = itemView.findViewById(R.id.txtTime);
            icon = itemView.findViewById(R.id.imgType);
            unread = itemView.findViewById(R.id.viewUnread);
            delete = itemView.findViewById(R.id.imgDelete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Notification notification = list.get(position);

        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());
        holder.subtitle.setText(notification.getSubtitle());

        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                notification.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );

        holder.time.setText(relativeTime);

        if (notification.isRead()) {
            holder.unread.setVisibility(View.GONE);
        } else {
            holder.unread.setVisibility(View.VISIBLE);
        }

        switch (notification.getType()) {

            case "budget":
                holder.icon.setImageResource(R.drawable.budget_warning);
                break;

            case "subscription":
                holder.icon.setImageResource(R.drawable.subscribtion);
                break;

            case "report":
                holder.icon.setImageResource(R.drawable.report);
                break;

            default:
                holder.icon.setImageResource(R.drawable.notification);
        }

        holder.itemView.setOnClickListener(v -> {

            if (!notification.isRead()) {

                db.markNotificationAsRead(
                        notification.getId()
                );

                notification.setRead(true);

                notifyItemChanged(position);

            }
        });

        holder.delete.setOnClickListener(v -> {

            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Notification")
                    .setMessage("Are you sure you want to delete this notification?\n\nThis action cannot be undone.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {

                        db.deleteNotification(notification.getId());

                        list.remove(position);

                        notifyItemRemoved(position);

                        notifyItemRangeChanged(position, list.size());

                    })
                    .show();

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}