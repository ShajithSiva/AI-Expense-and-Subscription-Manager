package com.example.aiexpensemanagementapplication.data.local;
import com.example.aiexpensemanagementapplication.model.User;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // ✅ DAO
    public abstract UserDao userDao();

    // ✅ Singleton instance
    private static AppDatabase instance;

    // ✅ THIS is what your error is missing
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "expense_db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}