package com.example.aiexpensemanagementapplication.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aiexpensemanagementapplication.data.local.AppDatabase;
import com.example.aiexpensemanagementapplication.model.User;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);



        // ✅ ROOM TEST CODE
        AppDatabase db = AppDatabase.getInstance(this);

        new Thread(() -> {

            User testUser = new User(
                    "Test User",
                    "test@mail.com",
                    false,
                    null,
                    false
            );

            db.userDao().insertUser(testUser);

            // Verify insert
            User user = db.userDao().getUserByEmail("test@mail.com");

            if (user != null) {
                Log.d("DB_TEST", "User inserted: " + user.getFullName());
            } else {
                Log.d("DB_TEST", "Insert failed");
            }

        }).start();
    }
}