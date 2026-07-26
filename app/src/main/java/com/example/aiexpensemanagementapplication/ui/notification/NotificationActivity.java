package com.example.aiexpensemanagementapplication.ui.notification;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.model.Notification;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView imgEmpty;
    private TextView txtEmpty;
    private TextView tvMarkAll;
    private NotificationAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ArrayList<Notification> notificationList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.rvNotifications);

        swipeRefresh = findViewById(R.id.swipeRefresh);

        ImageView btnBack = findViewById(R.id.btnBack);

        databaseHelper = new DatabaseHelper(this);

        notificationList = databaseHelper.getAllNotifications();

        imgEmpty = findViewById(R.id.imgEmpty);

        txtEmpty = findViewById(R.id.txtEmpty);

        tvMarkAll = findViewById(R.id.tvMarkAll);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        adapter = new NotificationAdapter(
                this,
                notificationList
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> {

            // Clear current list
            notificationList.clear();

            // Load latest notifications from database
            notificationList.addAll(databaseHelper.getAllNotifications());

            // Refresh RecyclerView
            adapter.notifyDataSetChanged();

            // Stop refresh animation
            swipeRefresh.setRefreshing(false);

        });

        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                         int direction) {

                        int position = viewHolder.getAdapterPosition();

                        Notification notification =
                                notificationList.get(position);

                        databaseHelper.deleteNotification(
                                notification.getId()
                        );

                        notificationList.remove(position);

                        adapter.notifyItemRemoved(position);

                    }
                };

        new ItemTouchHelper(simpleCallback)
                .attachToRecyclerView(recyclerView);

        if(notificationList.isEmpty()){

            recyclerView.setVisibility(View.GONE);

            imgEmpty.setVisibility(View.VISIBLE);

            txtEmpty.setVisibility(View.VISIBLE);

        }
        tvMarkAll.setOnClickListener(v -> {

            databaseHelper.markAllNotificationsAsRead();

            notificationList.clear();

            notificationList.addAll(databaseHelper.getAllNotifications());

            adapter.notifyDataSetChanged();

        });
        tvMarkAll.setOnClickListener(v -> {

            databaseHelper.markAllNotificationsAsRead();

            notificationList.clear();

            notificationList.addAll(databaseHelper.getAllNotifications());

            adapter.notifyDataSetChanged();

            Toast.makeText(
                    this,
                    "All notifications marked as read",
                    Toast.LENGTH_SHORT
            ).show();

        });
    }

}