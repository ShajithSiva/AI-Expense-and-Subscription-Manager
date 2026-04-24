package com.example.aiexpensemanagementapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String fullName;
    private String email;
    private boolean isEmailVerified;

    private String mobileNumber;
    private boolean isMobileVerified;

    // Constructor
    public User(String fullName, String email, boolean isEmailVerified,
                String mobileNumber, boolean isMobileVerified) {
        this.fullName = fullName;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
        this.mobileNumber = mobileNumber;
        this.isMobileVerified = isMobileVerified;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEmailVerified() { return isEmailVerified; }
    public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public boolean isMobileVerified() { return isMobileVerified; }
    public void setMobileVerified(boolean mobileVerified) { isMobileVerified = mobileVerified; }
}