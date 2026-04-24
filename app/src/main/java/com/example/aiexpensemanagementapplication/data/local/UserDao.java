package com.example.aiexpensemanagementapplication.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aiexpensemanagementapplication.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    long insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    // Update Email Verification
    @Query("UPDATE users SET isEmailVerified = 1 WHERE email = :email")
    void verifyEmail(String email);

    // Update Mobile Number
    @Query("UPDATE users SET mobileNumber = :mobile WHERE email = :email")
    void updateMobile(String email, String mobile);

    // Verify Mobile
    @Query("UPDATE users SET isMobileVerified = 1 WHERE email = :email")
    void verifyMobile(String email);
}