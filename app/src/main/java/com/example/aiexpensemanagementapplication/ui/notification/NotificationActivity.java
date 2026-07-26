package com.example.aiexpensemanagementapplication.ui.notification;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private ArrayList<Notification> notificationList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.rvNotifications);

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