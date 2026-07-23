package com.example.aiexpensemanagementapplication.data.local;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseModel;
import com.example.aiexpensemanagementapplication.ui.income.IncomeModel;
import com.example.aiexpensemanagementapplication.model.NotificationPreferences;


import android.content.Context;
import com.github.mikephil.charting.data.PieEntry;
import java.util.List;
import java.util.ArrayList;

import com.github.mikephil.charting.data.Entry;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    //========================================================
    // DATABASE INFO
    //========================================================

    private static final String DATABASE_NAME = "ExpenseVaultDB.db";
    private static final int DATABASE_VERSION = 5;

    //========================================================
    // USER TABLE
    //========================================================

    public static final String FIREBASE_UID = "FirebaseUid";
    public static final String TABLE_USER = "User";

    public static final String USER_ID = "UserID";
    public static final String USER_NAME = "Name";
    public static final String USER_EMAIL = "Email";
    public static final String USER_MOBILE = "MobileNumber";
    public static final String USER_PASSWORD = "PasswordHash";
    public static final String USER_STATUS = "Status";
    public static final String USER_CREATED_AT = "CreatedAt";

    //========================================================
    // PERSONAL USER
    //========================================================

    public static final String TABLE_PERSONAL_USER = "PersonalUser";

    //========================================================
    // FAMILY USER
    //========================================================

    public static final String TABLE_FAMILY_USER = "FamilyUser";

    //========================================================
    // FAMILY
    //========================================================

    public static final String TABLE_FAMILY = "Family";

    public static final String FAMILY_ID = "FamilyID";
    public static final String FAMILY_NAME = "FamilyName";
    public static final String FAMILY_CREATED_AT = "CreatedAt";

    //========================================================
    // FAMILY MEMBER
    //========================================================

    public static final String TABLE_FAMILY_MEMBER = "FamilyMember";

    public static final String FAMILY_ROLE = "Role";

    //========================================================
    // CATEGORY
    //========================================================

    public static final String TABLE_CATEGORY = "Category";

    public static final String CATEGORY_ID = "CategoryID";
    public static final String CATEGORY_NAME = "CategoryName";
    public static final String CATEGORY_TYPE = "CategoryType";

    //========================================================
    // PAYMENTMETHOD
    //========================================================
    public static final String TABLE_PAYMENT_METHOD = "PaymentMethod";

    public static final String PAYMENT_METHOD_ID = "PaymentMethodID";

    public static final String METHOD_NAME = "MethodName";


    //========================================================
    // TRANSACTION
    //========================================================

    public static final String TABLE_TRANSACTION = "Transactions";

    public static final String TRANSACTION_ID = "TransactionID";
    public static final String AMOUNT = "Amount";
    public static final String EXPENSE_MODE = "ExpenseMode";
    public static final String TRANSACTION_TYPE = "TransactionType";
    public static final String TRANSACTION_DATE = "TransactionDate";
    public static final String SOURCE = "Source";

    //========================================================
    // BUDGET
    //========================================================

    public static final String TABLE_BUDGET = "Budget";

    public static final String BUDGET_ID = "BudgetID";
    public static final String LIMIT_AMOUNT = "LimitAmount";
    public static final String START_DATE = "StartDate";
    public static final String END_DATE = "EndDate";

    //========================================================
    // SUBSCRIPTION
    //========================================================

    public static final String TABLE_SUBSCRIPTION = "Subscription";

    public static final String SUBSCRIPTION_ID = "SubscriptionID";
    public static final String SERVICE_NAME = "ServiceName";
    public static final String BILLING_CYCLE = "BillingCycle";
    public static final String NEXT_BILLING_DATE = "NextBillingDate";

    //========================================================
    // USAGE DATA
    //========================================================

    public static final String TABLE_USAGE_DATA = "UsageData";

    public static final String USAGE_ID = "UsageID";
    public static final String APP_NAME = "AppName";
    public static final String USAGE_FREQUENCY = "UsageFrequency";
    public static final String COST_PER_USE = "CostPerUse";
    public static final String LAST_USED_DATE = "LastUsedDate";
    public static final String INACTIVITY_DAYS = "InactivityDays";

    //========================================================
    // ALERT
    //========================================================

    public static final String TABLE_ALERT = "Alert";

    public static final String ALERT_ID = "AlertID";
    public static final String ALERT_TITLE = "Title";
    public static final String ALERT_MESSAGE = "Message";
    public static final String ALERT_TYPE = "AlertType";
    public static final String ALERT_DATE = "AlertDate";

    //========================================================
    // REPORT
    //========================================================

    public static final String TABLE_REPORT = "Report";

    public static final String REPORT_ID = "ReportID";
    public static final String REPORT_TYPE = "ReportType";
    public static final String REPORT_PATH = "ReportPath";
    public static final String GENERATED_DATE = "GeneratedDate";

    //========================================================
    // Notification Preferences Table
    //========================================================
    private static final String TABLE_NOTIFICATION_PREFERENCES = "notification_preferences";

    private static final String COLUMN_USER_ID = "user_id";

    private static final String COLUMN_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String COLUMN_EXPENSE_REMINDER = "expense_reminder";
    private static final String COLUMN_BUDGET_ALERT = "budget_alert";
    private static final String COLUMN_LARGE_TRANSACTION_ALERT = "large_transaction_alert";

    private static final String COLUMN_SUBSCRIPTION_REMINDER = "subscription_reminder";
    private static final String COLUMN_RENEWAL_REMINDER = "renewal_reminder";

    private static final String COLUMN_WEEKLY_REPORT = "weekly_report";
    private static final String COLUMN_MONTHLY_REPORT = "monthly_report";

    private static final String COLUMN_REMINDER_HOUR = "reminder_hour";
    private static final String COLUMN_REMINDER_MINUTE = "reminder_minute";

    private static final String COLUMN_UPDATED_AT = "updated_at";

    //========================================================
    // FINANCIAL PREFERENCES TABLE
    //========================================================

    public static final String TABLE_FINANCIAL_PREFERENCES =
            "FinancialPreferences";

    public static final String PREF_USER_ID =
            "UserID";

    public static final String PREF_CURRENCY_CODE =
            "CurrencyCode";

    public static final String PREF_CURRENCY_NAME =
            "CurrencyName";

    public static final String PREF_CURRENCY_SYMBOL =
            "CurrencySymbol";

    public static final String PREF_BUDGET_PERIOD =
            "BudgetPeriod";

    public static final String PREF_UPDATED_AT =
            "UpdatedAt";

    //========================================================
    // CONSTRUCTOR
    //========================================================

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_USER_TABLE);

        db.execSQL(CREATE_PERSONAL_USER_TABLE);

        db.execSQL(CREATE_FAMILY_USER_TABLE);

        db.execSQL(CREATE_FAMILY_TABLE);

        db.execSQL(CREATE_FAMILY_MEMBER_TABLE);

        db.execSQL(CREATE_CATEGORY_TABLE);

        db.execSQL(CREATE_PAYMENT_METHOD_TABLE);

        db.execSQL(CREATE_TRANSACTION_TABLE);

        db.execSQL(CREATE_BUDGET_TABLE);

        db.execSQL(CREATE_SUBSCRIPTION_TABLE);

        db.execSQL(CREATE_USAGE_DATA_TABLE);

        db.execSQL(CREATE_ALERT_TABLE);

        db.execSQL(CREATE_REPORT_TABLE);

        insertDefaultCategories(db);

        insertDefaultPaymentMethods(db);

        db.execSQL(CREATE_NOTIFICATION_PREFERENCES_TABLE);

        db.execSQL(CREATE_FINANCIAL_PREFERENCES_TABLE);
    }

    private static final String CREATE_USER_TABLE =
            "CREATE TABLE " + TABLE_USER + " (" +
                    USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_NAME + " TEXT NOT NULL," +
                    USER_EMAIL + " TEXT UNIQUE NOT NULL," +
                    FIREBASE_UID + " TEXT UNIQUE," +
                    USER_MOBILE + " TEXT," +
                    USER_PASSWORD + " TEXT NOT NULL," +
                    USER_STATUS + " TEXT," +
                    USER_CREATED_AT + " TEXT" +
                    ");";
    private static final String CREATE_PERSONAL_USER_TABLE =
            "CREATE TABLE " + TABLE_PERSONAL_USER + " (" +
                    USER_ID + " INTEGER PRIMARY KEY," +
                    "FOREIGN KEY(" + USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")" +
                    ");";
    private static final String CREATE_FAMILY_USER_TABLE =
            "CREATE TABLE " + TABLE_FAMILY_USER + " (" +
                    USER_ID + " INTEGER PRIMARY KEY," +
                    "FOREIGN KEY(" + USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")" +
                    ");";
    private static final String CREATE_FAMILY_TABLE =
            "CREATE TABLE " + TABLE_FAMILY + " (" +
                    FAMILY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FAMILY_NAME + " TEXT NOT NULL," +
                    FAMILY_CREATED_AT + " TEXT" +
                    ");";
    private static final String CREATE_FAMILY_MEMBER_TABLE =
            "CREATE TABLE " + TABLE_FAMILY_MEMBER + " (" +
                    FAMILY_ID + " INTEGER," +
                    USER_ID + " INTEGER," +
                    FAMILY_ROLE + " TEXT," +
                    "PRIMARY KEY(" + FAMILY_ID + "," + USER_ID + ")," +
                    "FOREIGN KEY(" + FAMILY_ID + ") REFERENCES " +
                    TABLE_FAMILY + "(" + FAMILY_ID + ")," +
                    "FOREIGN KEY(" + USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")" +
                    ");";
    private static final String CREATE_CATEGORY_TABLE =
            "CREATE TABLE " + TABLE_CATEGORY + " (" +
                    CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CATEGORY_NAME + " TEXT NOT NULL," +
                    CATEGORY_TYPE + " TEXT NOT NULL" +
                    ");";

    private static final String CREATE_PAYMENT_METHOD_TABLE =
            "CREATE TABLE " + TABLE_PAYMENT_METHOD + " (" +
                    PAYMENT_METHOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    METHOD_NAME + " TEXT NOT NULL" +
                    ");";

    private static final String CREATE_TRANSACTION_TABLE =
            "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                    TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_ID + " INTEGER," +
                    PAYMENT_METHOD_ID + " INTEGER," +
                    CATEGORY_ID + " INTEGER," +
                    AMOUNT + " REAL," +
                    TRANSACTION_TYPE + " TEXT," +
                    TRANSACTION_DATE + " TEXT," +
                    SOURCE + " TEXT," +
                    EXPENSE_MODE + " TEXT," +
                    "FOREIGN KEY(" + USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")," +
                    "FOREIGN KEY(" + PAYMENT_METHOD_ID + ") REFERENCES " +
                    TABLE_PAYMENT_METHOD + "(" + PAYMENT_METHOD_ID + ")," +
                    "FOREIGN KEY(" + CATEGORY_ID + ") REFERENCES " +
                    TABLE_CATEGORY + "(" + CATEGORY_ID + ")" +
                    ");";
    private static final String CREATE_BUDGET_TABLE =
            "CREATE TABLE " + TABLE_BUDGET + " (" +
                    BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_ID + " INTEGER," +
                    CATEGORY_ID + " INTEGER," +
                    LIMIT_AMOUNT + " REAL," +
                    START_DATE + " TEXT," +
                    END_DATE + " TEXT," +
                    "FOREIGN KEY(" + USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")," +
                    "FOREIGN KEY(" + CATEGORY_ID + ") REFERENCES " +
                    TABLE_CATEGORY + "(" + CATEGORY_ID + ")" +
                    ");";
    private static final String CREATE_SUBSCRIPTION_TABLE =
            "CREATE TABLE " + TABLE_SUBSCRIPTION + " (" +
                    SUBSCRIPTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_ID + " INTEGER," +
                    SERVICE_NAME + " TEXT," +
                    AMOUNT + " REAL," +
                    BILLING_CYCLE + " TEXT," +
                    NEXT_BILLING_DATE + " TEXT," +
                    "FOREIGN KEY(" + USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")" +
                    ");";
    private static final String CREATE_USAGE_DATA_TABLE =
            "CREATE TABLE " + TABLE_USAGE_DATA + " (" +
                    USAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SUBSCRIPTION_ID + " INTEGER," +
                    APP_NAME + " TEXT," +
                    USAGE_FREQUENCY + " INTEGER," +
                    COST_PER_USE + " REAL," +
                    LAST_USED_DATE + " TEXT," +
                    INACTIVITY_DAYS + " INTEGER," +
                    "FOREIGN KEY(" + SUBSCRIPTION_ID + ") REFERENCES " +
                    TABLE_SUBSCRIPTION + "(" + SUBSCRIPTION_ID + ")" +
                    ");";
    private static final String CREATE_ALERT_TABLE =
            "CREATE TABLE " + TABLE_ALERT + " (" +
                    ALERT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_ID + " INTEGER," +
                    ALERT_TITLE + " TEXT," +
                    ALERT_MESSAGE + " TEXT," +
                    ALERT_TYPE + " TEXT," +
                    ALERT_DATE + " TEXT," +
                    "FOREIGN KEY(" + USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")" +
                    ");";
    private static final String CREATE_REPORT_TABLE =
            "CREATE TABLE " + TABLE_REPORT + " (" +
                    REPORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_ID + " INTEGER," +
                    REPORT_TYPE + " TEXT," +
                    REPORT_PATH + " TEXT," +
                    GENERATED_DATE + " TEXT," +
                    "FOREIGN KEY(" + USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")" +
                    ");";

    private static final String CREATE_NOTIFICATION_PREFERENCES_TABLE =
            "CREATE TABLE " + TABLE_NOTIFICATION_PREFERENCES + " (" +

                    COLUMN_USER_ID + " TEXT PRIMARY KEY," +

                    COLUMN_NOTIFICATIONS_ENABLED + " INTEGER," +
                    COLUMN_EXPENSE_REMINDER + " INTEGER," +
                    COLUMN_BUDGET_ALERT + " INTEGER," +
                    COLUMN_LARGE_TRANSACTION_ALERT + " INTEGER," +

                    COLUMN_SUBSCRIPTION_REMINDER + " INTEGER," +
                    COLUMN_RENEWAL_REMINDER + " INTEGER," +

                    COLUMN_WEEKLY_REPORT + " INTEGER," +
                    COLUMN_MONTHLY_REPORT + " INTEGER," +

                    COLUMN_REMINDER_HOUR + " INTEGER," +
                    COLUMN_REMINDER_MINUTE + " INTEGER," +

                    COLUMN_UPDATED_AT + " INTEGER" +

                    ")";

    private static final String CREATE_FINANCIAL_PREFERENCES_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_FINANCIAL_PREFERENCES + " (" +

                    PREF_USER_ID + " INTEGER PRIMARY KEY," +

                    PREF_CURRENCY_CODE +
                    " TEXT NOT NULL DEFAULT 'LKR'," +

                    PREF_CURRENCY_NAME +
                    " TEXT NOT NULL DEFAULT 'Sri Lankan Rupee'," +

                    PREF_CURRENCY_SYMBOL +
                    " TEXT NOT NULL DEFAULT '₨'," +

                    PREF_BUDGET_PERIOD +
                    " TEXT NOT NULL DEFAULT 'Monthly'," +

                    PREF_UPDATED_AT + " INTEGER," +

                    "FOREIGN KEY(" + PREF_USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + USER_ID + ")" +

                    ");";

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 5) {
            db.execSQL(CREATE_FINANCIAL_PREFERENCES_TABLE);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db,
                            int oldVersion,
                            int newVersion) {

        onUpgrade(db, oldVersion, newVersion);
    }

    private void insertDefaultPaymentMethods(SQLiteDatabase db){

        db.execSQL("INSERT INTO PaymentMethod(MethodName) VALUES('Cash')");

        db.execSQL("INSERT INTO PaymentMethod(MethodName) VALUES('Debit Card')");

        db.execSQL("INSERT INTO PaymentMethod(MethodName) VALUES('Credit Card')");

        db.execSQL("INSERT INTO PaymentMethod(MethodName) VALUES('Online Banking')");

        db.execSQL("INSERT INTO PaymentMethod(MethodName) VALUES('Mobile Wallet')");

        db.execSQL("INSERT INTO PaymentMethod(MethodName) VALUES('Bank Transfer')");
    }
    private void insertDefaultCategories(SQLiteDatabase db) {

        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Food','Expense')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Transport','Expense')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Shopping','Expense')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Bills','Expense')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Health','Expense')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Education','Expense')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Entertainment','Expense')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Travel','Expense')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Salary','Income')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Business','Income')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Investment','Income')");
        db.execSQL("INSERT INTO Category(CategoryName,CategoryType) VALUES('Gift','Income')");
    }

    public long insertUser(String name,
                           String email,
                           String firebaseUid,
                           String mobile,
                           String passwordHash,
                           String status,
                           String createdAt) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_NAME, name);
        values.put(USER_EMAIL, email);
        values.put(FIREBASE_UID, firebaseUid);
        values.put(USER_MOBILE, mobile);
        values.put(USER_PASSWORD, passwordHash);
        values.put(USER_STATUS, status);
        values.put(USER_CREATED_AT, createdAt);

        return db.insert(TABLE_USER, null, values);
    }
    public boolean isFirebaseUserExists(String firebaseUid) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USER +
                        " WHERE " + FIREBASE_UID + "=?",
                new String[]{firebaseUid});

        boolean exists = cursor.moveToFirst();

        cursor.close();

        return exists;
    }
    public int getUserIdByFirebaseUid(String firebaseUid) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT " + USER_ID +
                        " FROM " + TABLE_USER +
                        " WHERE " + FIREBASE_UID + "=?",

                new String[]{firebaseUid});

        int userId = -1;

        if(cursor.moveToFirst()){
            userId = cursor.getInt(0);
        }

        cursor.close();

        return userId;
    }

    public boolean isUserExists(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USER +
                        " WHERE " + USER_EMAIL + "=?",
                new String[]{email});

        boolean exists = cursor.getCount() > 0;

        cursor.close();

        return exists;
    }

    public Cursor getUserByEmail(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_USER +
                        " WHERE " + USER_EMAIL + "=?",
                new String[]{email});
    }

    public Cursor getUserById(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_USER +
                        " WHERE " + USER_ID + "=?",
                new String[]{
                        String.valueOf(userId)
                });
    }

    public int updateUser(int userId,
                          String name,
                          String mobile,
                          String status) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_NAME, name);
        values.put(USER_MOBILE, mobile);
        values.put(USER_STATUS, status);

        return db.update(
                TABLE_USER,
                values,
                USER_ID + "=?",
                new String[]{
                        String.valueOf(userId)
                });
    }

    public int updatePassword(int userId,
                              String passwordHash) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_PASSWORD, passwordHash);

        return db.update(
                TABLE_USER,
                values,
                USER_ID + "=?",
                new String[]{
                        String.valueOf(userId)
                });
    }

    public int deleteUser(int userId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(
                TABLE_USER,
                USER_ID + "=?",
                new String[]{
                        String.valueOf(userId)
                });
    }

    public int getTotalUsers() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USER,
                null);

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    public Cursor getAllUsers() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_USER,
                null);
    }

    public long insertCategory(String categoryName,
                               String categoryType) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CATEGORY_NAME, categoryName);
        values.put(CATEGORY_TYPE, categoryType);

        return db.insert(TABLE_CATEGORY, null, values);
    }

    public Cursor getAllCategories() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_CATEGORY +
                        " ORDER BY " + CATEGORY_NAME,
                null);
    }

    public Cursor getExpenseCategories() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_CATEGORY +
                        " WHERE " + CATEGORY_TYPE + "=? " +
                        "ORDER BY " + CATEGORY_NAME,
                new String[]{"Expense"});
    }

    public Cursor getIncomeCategories() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_CATEGORY +
                        " WHERE " + CATEGORY_TYPE + "=? " +
                        "ORDER BY " + CATEGORY_NAME,
                new String[]{"Income"});
    }

    public Cursor getCategoryById(int categoryId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_CATEGORY +
                        " WHERE " + CATEGORY_ID + "=?",
                new String[]{
                        String.valueOf(categoryId)
                });
    }

    public int updateCategory(int categoryId,
                              String categoryName,
                              String categoryType) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CATEGORY_NAME, categoryName);
        values.put(CATEGORY_TYPE, categoryType);

        return db.update(
                TABLE_CATEGORY,
                values,
                CATEGORY_ID + "=?",
                new String[]{
                        String.valueOf(categoryId)
                });
    }

    public int deleteCategory(int categoryId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(
                TABLE_CATEGORY,
                CATEGORY_ID + "=?",
                new String[]{
                        String.valueOf(categoryId)
                });
    }

    public boolean categoryExists(String categoryName) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_CATEGORY +
                        " WHERE " + CATEGORY_NAME + "=?",
                new String[]{categoryName});

        boolean exists = cursor.getCount() > 0;

        cursor.close();

        return exists;
    }

    public int getTotalCategories() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_CATEGORY,
                null);

        int total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    public Cursor getAllPaymentMethods(){

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_PAYMENT_METHOD +
                        " ORDER BY " + METHOD_NAME,

                null);

    }

    public int getPaymentMethodIdByName(String methodName){

        SQLiteDatabase db = getReadableDatabase();

        int id = -1;

        Cursor cursor = db.rawQuery(

                "SELECT " + PAYMENT_METHOD_ID +
                        " FROM " + TABLE_PAYMENT_METHOD +
                        " WHERE " + METHOD_NAME + "=?",

                new String[]{methodName});

        if(cursor.moveToFirst()){

            id = cursor.getInt(0);

        }

        cursor.close();

        return id;
    }

    public ArrayList<String> getPaymentMethodNames(){

        ArrayList<String> list = new ArrayList<>();

        Cursor cursor = getAllPaymentMethods();

        while(cursor.moveToNext()){

            list.add(

                    cursor.getString(

                            cursor.getColumnIndexOrThrow(METHOD_NAME)

                    )

            );

        }

        cursor.close();

        return list;
    }



    public long insertTransaction(int userId,
                                  int paymentMethodId,
                                  int categoryId,
                                  double amount,
                                  String transactionType,
                                  String transactionDate,
                                  String source,
                                  String expenseMode) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_ID, userId);
        values.put(PAYMENT_METHOD_ID, paymentMethodId);
        values.put(CATEGORY_ID, categoryId);
        values.put(AMOUNT, amount);
        values.put(TRANSACTION_TYPE, transactionType);
        values.put(TRANSACTION_DATE, transactionDate);
        values.put(SOURCE, source);
        values.put(EXPENSE_MODE, expenseMode);

        return db.insert(TABLE_TRANSACTION, null, values);
    }

    public boolean saveNotificationPreferences(
            String userId,
            NotificationPreferences preferences) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_ID, userId);

        values.put(COLUMN_NOTIFICATIONS_ENABLED,
                preferences.isNotificationsEnabled() ? 1 : 0);

        values.put(COLUMN_EXPENSE_REMINDER,
                preferences.isExpenseReminder() ? 1 : 0);

        values.put(COLUMN_BUDGET_ALERT,
                preferences.isBudgetAlert() ? 1 : 0);

        values.put(COLUMN_LARGE_TRANSACTION_ALERT,
                preferences.isLargeTransactionAlert() ? 1 : 0);

        values.put(COLUMN_SUBSCRIPTION_REMINDER,
                preferences.isSubscriptionReminder() ? 1 : 0);

        values.put(COLUMN_RENEWAL_REMINDER,
                preferences.isRenewalReminder() ? 1 : 0);

        values.put(COLUMN_WEEKLY_REPORT,
                preferences.isWeeklyReport() ? 1 : 0);

        values.put(COLUMN_MONTHLY_REPORT,
                preferences.isMonthlyReport() ? 1 : 0);

        values.put(COLUMN_REMINDER_HOUR,
                preferences.getReminderHour());

        values.put(COLUMN_REMINDER_MINUTE,
                preferences.getReminderMinute());

        values.put(COLUMN_UPDATED_AT,
                System.currentTimeMillis());

        long result = db.replace(
                TABLE_NOTIFICATION_PREFERENCES,
                null,
                values);

        db.close();

        return result != -1;
    }

    public NotificationPreferences getNotificationPreferences(String userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NOTIFICATION_PREFERENCES,
                null,
                COLUMN_USER_ID + "=?",
                new String[]{userId},
                null,
                null,
                null);

        if (cursor.moveToFirst()) {

            NotificationPreferences preferences =
                    new NotificationPreferences();

            preferences.setNotificationsEnabled(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_NOTIFICATIONS_ENABLED)) == 1);

            preferences.setExpenseReminder(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_EXPENSE_REMINDER)) == 1);

            preferences.setBudgetAlert(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_BUDGET_ALERT)) == 1);

            preferences.setLargeTransactionAlert(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_LARGE_TRANSACTION_ALERT)) == 1);

            preferences.setSubscriptionReminder(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_SUBSCRIPTION_REMINDER)) == 1);

            preferences.setRenewalReminder(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_RENEWAL_REMINDER)) == 1);

            preferences.setWeeklyReport(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_WEEKLY_REPORT)) == 1);

            preferences.setMonthlyReport(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_MONTHLY_REPORT)) == 1);

            preferences.setReminderHour(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_REMINDER_HOUR)));

            preferences.setReminderMinute(
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                            COLUMN_REMINDER_MINUTE)));

            cursor.close();
            db.close();

            return preferences;
        }

        cursor.close();
        db.close();

        return null;
    }

    public Cursor getTransactions(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_TRANSACTION +
                        " WHERE " + USER_ID + "=?" +
                        " ORDER BY " + TRANSACTION_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getTransaction(int transactionId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_TRANSACTION +
                        " WHERE " + TRANSACTION_ID + "=?",

                new String[]{
                        String.valueOf(transactionId)
                });

    }

    public int updateTransaction(int transactionId,
                                 int categoryId,
                                 double amount,
                                 String transactionDate,
                                 String source,
                                 String expenseMode) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CATEGORY_ID, categoryId);
        values.put(AMOUNT, amount);
        values.put(TRANSACTION_DATE, transactionDate);
        values.put(SOURCE, source);
        values.put(EXPENSE_MODE, expenseMode);

        return db.update(

                TABLE_TRANSACTION,

                values,

                TRANSACTION_ID + "=?",

                new String[]{
                        String.valueOf(transactionId)
                });

    }

    public int deleteTransaction(int transactionId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(

                TABLE_TRANSACTION,

                TRANSACTION_ID + "=?",

                new String[]{
                        String.valueOf(transactionId)
                });

    }

    public Cursor getIncomeTransactions(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_TRANSACTION +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        TRANSACTION_TYPE + "='Income'" +

                        " ORDER BY " + TRANSACTION_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getExpenseTransactions(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_TRANSACTION +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        TRANSACTION_TYPE + "='Expense'" +

                        " ORDER BY " + TRANSACTION_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public double getTotalIncome(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT SUM(" + AMOUNT + ")" +

                        " FROM " + TABLE_TRANSACTION +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        TRANSACTION_TYPE + "='Income'",

                new String[]{
                        String.valueOf(userId)
                });

        double total = 0;

        if (cursor.moveToFirst()) {

            total = cursor.getDouble(0);

        }

        cursor.close();

        return total;

    }

    public int getTransactionCount(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT COUNT(*) FROM " + TABLE_TRANSACTION +

                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        int total = 0;

        if (cursor.moveToFirst()) {

            total = cursor.getInt(0);

        }

        cursor.close();

        return total;

    }

    public Cursor getRecentTransactions(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_TRANSACTION +

                        " WHERE " + USER_ID + "=?" +

                        " ORDER BY " + TRANSACTION_DATE +

                        " DESC LIMIT 10",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public long insertBudget(int userId,
                             int categoryId,
                             double limitAmount,
                             String startDate,
                             String endDate) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_ID, userId);
        values.put(CATEGORY_ID, categoryId);
        values.put(LIMIT_AMOUNT, limitAmount);
        values.put(START_DATE, startDate);
        values.put(END_DATE, endDate);

        return db.insert(TABLE_BUDGET, null, values);
    }

    public Cursor getBudgets(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_BUDGET +

                        " WHERE " + USER_ID + "=?" +

                        " ORDER BY " + START_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getBudget(int budgetId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_BUDGET +

                        " WHERE " + BUDGET_ID + "=?",

                new String[]{
                        String.valueOf(budgetId)
                });

    }

    public int updateBudget(int budgetId,
                            double limitAmount,
                            String startDate,
                            String endDate) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(LIMIT_AMOUNT, limitAmount);
        values.put(START_DATE, startDate);
        values.put(END_DATE, endDate);

        return db.update(

                TABLE_BUDGET,

                values,

                BUDGET_ID + "=?",

                new String[]{
                        String.valueOf(budgetId)
                });

    }

    public int deleteBudget(int budgetId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(

                TABLE_BUDGET,

                BUDGET_ID + "=?",

                new String[]{
                        String.valueOf(budgetId)
                });

    }

    public double getBudgetLimit(int categoryId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT " + LIMIT_AMOUNT +

                        " FROM " + TABLE_BUDGET +

                        " WHERE " + CATEGORY_ID + "=?",

                new String[]{
                        String.valueOf(categoryId)
                });

        double amount = 0;

        if (cursor.moveToFirst()) {

            amount = cursor.getDouble(0);

        }

        cursor.close();

        return amount;

    }

    public double getTotalBudget(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT SUM(" + LIMIT_AMOUNT + ")" +

                        " FROM " + TABLE_BUDGET +

                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        double total = 0;

        if (cursor.moveToFirst()) {

            total = cursor.getDouble(0);

        }

        cursor.close();

        return total;

    }

    public double getBudgetUsed(int categoryId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT SUM(" + AMOUNT + ")" +

                        " FROM " + TABLE_TRANSACTION +

                        " WHERE " + CATEGORY_ID + "=?" +

                        " AND " + TRANSACTION_TYPE + "='Expense'",

                new String[]{
                        String.valueOf(categoryId)
                });

        double used = 0;

        if (cursor.moveToFirst()) {

            used = cursor.getDouble(0);

        }

        cursor.close();

        return used;

    }

    public double getRemainingBudget(int categoryId) {

        double budget = getBudgetLimit(categoryId);

        double used = getBudgetUsed(categoryId);

        return budget - used;

    }

    public int getBudgetPercentage(int categoryId) {

        double budget = getBudgetLimit(categoryId);

        double used = getBudgetUsed(categoryId);

        if (budget == 0) {

            return 0;

        }

        return (int) ((used / budget) * 100);

    }

    public boolean isBudgetExceeded(int categoryId) {

        return getBudgetUsed(categoryId) >= getBudgetLimit(categoryId);

    }

    public int getBudgetCount(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT COUNT(*) FROM " + TABLE_BUDGET +

                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        int count = 0;

        if (cursor.moveToFirst()) {

            count = cursor.getInt(0);

        }

        cursor.close();

        return count;

    }

    public long insertSubscription(int userId,
                                   String serviceName,
                                   double amount,
                                   String billingCycle,
                                   String nextBillingDate) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_ID, userId);
        values.put(SERVICE_NAME, serviceName);
        values.put(AMOUNT, amount);
        values.put(BILLING_CYCLE, billingCycle);
        values.put(NEXT_BILLING_DATE, nextBillingDate);

        return db.insert(TABLE_SUBSCRIPTION, null, values);
    }

    public Cursor getSubscriptions(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_SUBSCRIPTION +
                        " WHERE " + USER_ID + "=?" +
                        " ORDER BY " + NEXT_BILLING_DATE,

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getSubscription(int subscriptionId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_SUBSCRIPTION +

                        " WHERE " +

                        SUBSCRIPTION_ID + "=?",

                new String[]{
                        String.valueOf(subscriptionId)
                });

    }

    public int updateSubscription(int subscriptionId,
                                  String serviceName,
                                  double amount,
                                  String billingCycle,
                                  String nextBillingDate) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(SERVICE_NAME, serviceName);
        values.put(AMOUNT, amount);
        values.put(BILLING_CYCLE, billingCycle);
        values.put(NEXT_BILLING_DATE, nextBillingDate);

        return db.update(

                TABLE_SUBSCRIPTION,

                values,

                SUBSCRIPTION_ID + "=?",

                new String[]{
                        String.valueOf(subscriptionId)
                });

    }

    public int deleteSubscription(int subscriptionId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(

                TABLE_SUBSCRIPTION,

                SUBSCRIPTION_ID + "=?",

                new String[]{
                        String.valueOf(subscriptionId)
                });

    }

    public double getTotalSubscriptionAmount(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT SUM(" + AMOUNT + ")" +

                        " FROM " + TABLE_SUBSCRIPTION +

                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        double total = 0;

        if (cursor.moveToFirst()) {

            total = cursor.getDouble(0);

        }

        cursor.close();

        return total;

    }

    public int getSubscriptionCount(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT COUNT(*)" +

                        " FROM " + TABLE_SUBSCRIPTION +

                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        int count = 0;

        if (cursor.moveToFirst()) {

            count = cursor.getInt(0);

        }

        cursor.close();

        return count;

    }

    public boolean subscriptionExists(int userId,
                                      String serviceName) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT * FROM " + TABLE_SUBSCRIPTION +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        SERVICE_NAME + "=?",

                new String[]{
                        String.valueOf(userId),
                        serviceName
                });

        boolean exists = cursor.getCount() > 0;

        cursor.close();

        return exists;

    }

    public Cursor getNextSubscription(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_SUBSCRIPTION +

                        " WHERE " + USER_ID + "=?" +

                        " ORDER BY " + NEXT_BILLING_DATE +

                        " LIMIT 1",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getSubscriptionsByDate(String date) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_SUBSCRIPTION +

                        " WHERE " +

                        NEXT_BILLING_DATE + "=?",

                new String[]{
                        date
                });

    }

    public long insertUsageData(int subscriptionId,
                                String appName,
                                int usageFrequency,
                                double costPerUse,
                                String lastUsedDate,
                                int inactivityDays) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(SUBSCRIPTION_ID, subscriptionId);
        values.put(APP_NAME, appName);
        values.put(USAGE_FREQUENCY, usageFrequency);
        values.put(COST_PER_USE, costPerUse);
        values.put(LAST_USED_DATE, lastUsedDate);
        values.put(INACTIVITY_DAYS, inactivityDays);

        return db.insert(TABLE_USAGE_DATA, null, values);
    }

    public Cursor getUsageData() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_USAGE_DATA +
                        " ORDER BY " + LAST_USED_DATE + " DESC",

                null);
    }

    public Cursor getUsageBySubscription(int subscriptionId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_USAGE_DATA +

                        " WHERE " +

                        SUBSCRIPTION_ID + "=?",

                new String[]{
                        String.valueOf(subscriptionId)
                });

    }

    public int updateUsageData(int usageId,
                               int usageFrequency,
                               double costPerUse,
                               String lastUsedDate,
                               int inactivityDays) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USAGE_FREQUENCY, usageFrequency);
        values.put(COST_PER_USE, costPerUse);
        values.put(LAST_USED_DATE, lastUsedDate);
        values.put(INACTIVITY_DAYS, inactivityDays);

        return db.update(

                TABLE_USAGE_DATA,

                values,

                USAGE_ID + "=?",

                new String[]{
                        String.valueOf(usageId)
                });

    }

    public int deleteUsageData(int usageId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(

                TABLE_USAGE_DATA,

                USAGE_ID + "=?",

                new String[]{
                        String.valueOf(usageId)
                });

    }

    public Cursor getInactiveSubscriptions() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_USAGE_DATA +

                        " WHERE " +

                        INACTIVITY_DAYS + ">=30",

                null);

    }

    public double getAverageCostPerUse() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT AVG(" + COST_PER_USE + ")" +

                        " FROM " + TABLE_USAGE_DATA,

                null);

        double average = 0;

        if (cursor.moveToFirst()) {

            average = cursor.getDouble(0);

        }

        cursor.close();

        return average;

    }

    public Cursor getMostUsedApp() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_USAGE_DATA +

                        " ORDER BY " +

                        USAGE_FREQUENCY +

                        " DESC LIMIT 1",

                null);

    }

    public Cursor getLeastUsedApp() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_USAGE_DATA +

                        " ORDER BY " +

                        USAGE_FREQUENCY +

                        " ASC LIMIT 1",

                null);

    }

    public int getUsageRecordCount() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT COUNT(*) FROM " +

                        TABLE_USAGE_DATA,

                null);

        int count = 0;

        if (cursor.moveToFirst()) {

            count = cursor.getInt(0);

        }

        cursor.close();

        return count;

    }

    public long insertAlert(int userId,
                            String title,
                            String message,
                            String alertType,
                            String alertDate) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_ID, userId);
        values.put(ALERT_TITLE, title);
        values.put(ALERT_MESSAGE, message);
        values.put(ALERT_TYPE, alertType);
        values.put(ALERT_DATE, alertDate);

        return db.insert(TABLE_ALERT, null, values);
    }

    public Cursor getAlerts(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_ALERT +

                        " WHERE " +

                        USER_ID + "=?" +

                        " ORDER BY " +

                        ALERT_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getAlert(int alertId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_ALERT +

                        " WHERE " +

                        ALERT_ID + "=?",

                new String[]{
                        String.valueOf(alertId)
                });

    }

    public int updateAlert(int alertId,
                           String title,
                           String message,
                           String alertType,
                           String alertDate) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(ALERT_TITLE, title);
        values.put(ALERT_MESSAGE, message);
        values.put(ALERT_TYPE, alertType);
        values.put(ALERT_DATE, alertDate);

        return db.update(

                TABLE_ALERT,

                values,

                ALERT_ID + "=?",

                new String[]{
                        String.valueOf(alertId)
                });

    }

    public int deleteAlert(int alertId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(

                TABLE_ALERT,

                ALERT_ID + "=?",

                new String[]{
                        String.valueOf(alertId)
                });

    }

    public Cursor getBudgetAlerts(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_ALERT +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        ALERT_TYPE + "='Budget'" +

                        " ORDER BY " +

                        ALERT_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getSubscriptionAlerts(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_ALERT +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        ALERT_TYPE + "='Subscription'" +

                        " ORDER BY " +

                        ALERT_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getAIAlerts(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " + TABLE_ALERT +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        ALERT_TYPE + "='AI'" +

                        " ORDER BY " +

                        ALERT_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public int getAlertCount(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT COUNT(*) FROM " +

                        TABLE_ALERT +

                        " WHERE " +

                        USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        int count = 0;

        if (cursor.moveToFirst()) {

            count = cursor.getInt(0);

        }

        cursor.close();

        return count;

    }

    public int deleteAllAlerts(int userId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(

                TABLE_ALERT,

                USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public long insertReport(int userId,
                             String reportType,
                             String startDate,
                             String endDate,
                             String generatedDate,
                             String filePath) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_ID, userId);
        values.put(REPORT_TYPE, reportType);
        values.put(START_DATE, startDate);
        values.put(END_DATE, endDate);
        values.put(GENERATED_DATE, generatedDate);
        values.put(REPORT_PATH, filePath);

        return db.insert(TABLE_REPORT, null, values);
    }

    public Cursor getReports(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " +
                        TABLE_REPORT +
                        " WHERE " +
                        USER_ID + "=?" +
                        " ORDER BY " +
                        GENERATED_DATE +
                        " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getReport(int reportId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " +
                        TABLE_REPORT +
                        " WHERE " +
                        REPORT_ID + "=?",

                new String[]{
                        String.valueOf(reportId)
                });

    }

    public int deleteReport(int reportId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(

                TABLE_REPORT,

                REPORT_ID + "=?",

                new String[]{
                        String.valueOf(reportId)
                });

    }

    public int deleteAllReports(int userId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(

                TABLE_REPORT,

                USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getMonthlyReports(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " +
                        TABLE_REPORT +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        REPORT_TYPE + "='Monthly'" +

                        " ORDER BY " +

                        GENERATED_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getYearlyReports(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " +
                        TABLE_REPORT +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        REPORT_TYPE + "='Yearly'" +

                        " ORDER BY " +

                        GENERATED_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public Cursor getCustomReports(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " +
                        TABLE_REPORT +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        REPORT_TYPE + "='Custom'" +

                        " ORDER BY " +

                        GENERATED_DATE + " DESC",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public int getReportCount(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT COUNT(*) FROM " +
                        TABLE_REPORT +
                        " WHERE " +
                        USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;

    }

    public Cursor getLatestReport(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT * FROM " +
                        TABLE_REPORT +

                        " WHERE " +

                        USER_ID + "=?" +

                        " ORDER BY " +

                        GENERATED_DATE +

                        " DESC LIMIT 1",

                new String[]{
                        String.valueOf(userId)
                });

    }

    public double getTotalExpense(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT IFNULL(SUM(" + AMOUNT + "),0) FROM " +
                        TABLE_TRANSACTION +
                        " WHERE " +
                        USER_ID + "=? AND " +
                        TRANSACTION_TYPE + "='Expense'",

                new String[]{
                        String.valueOf(userId)
                });

        double total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();

        return total;
    }

    public double getTotalSavings(int userId) {

        return getTotalIncome(userId) - getTotalExpense(userId);

    }

    public double getTotalBalance(int userId) {

        return getTotalIncome(userId) - getTotalExpense(userId);

    }

    public ArrayList<String> getExpenseCategoryNames() {

        ArrayList<String> list = new ArrayList<>();

        Cursor cursor = getExpenseCategories();

        while (cursor.moveToNext()) {

            list.add(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    CATEGORY_NAME)));

        }

        cursor.close();

        return list;
    }

    public ArrayList<String> getIncomeCategoryNames() {

        ArrayList<String> list = new ArrayList<>();

        Cursor cursor = getIncomeCategories();

        while (cursor.moveToNext()) {

            list.add(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    CATEGORY_NAME
                            )
                    )
            );
        }

        cursor.close();

        return list;
    }

    public double getMonthlyIncome(int userId, String month) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT IFNULL(SUM(" + AMOUNT + "),0) FROM " +
                        TABLE_TRANSACTION +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        TRANSACTION_TYPE + "='Income' AND " +

                        "strftime('%Y-%m'," + TRANSACTION_DATE + ")=?",

                new String[]{
                        String.valueOf(userId),
                        month
                });

        double income = 0;

        if (cursor.moveToFirst()) {
            income = cursor.getDouble(0);
        }

        cursor.close();

        return income;

    }

    public double getMonthlyExpense(int userId, String month) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT IFNULL(SUM(" + AMOUNT + "),0) FROM " +
                        TABLE_TRANSACTION +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        TRANSACTION_TYPE + "='Expense' AND " +

                        "strftime('%Y-%m'," + TRANSACTION_DATE + ")=?",

                new String[]{
                        String.valueOf(userId),
                        month
                });

        double expense = 0;

        if (cursor.moveToFirst()) {
            expense = cursor.getDouble(0);
        }

        cursor.close();

        return expense;

    }

    public Cursor getExpenseByCategory(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(

                "SELECT " +

                        CATEGORY_ID +

                        ", SUM(" +

                        AMOUNT +

                        ") AS Total FROM " +

                        TABLE_TRANSACTION +

                        " WHERE " +

                        USER_ID + "=? AND " +

                        TRANSACTION_TYPE + "='Expense'" +

                        " GROUP BY " +

                        CATEGORY_ID,

                new String[]{
                        String.valueOf(userId)
                });

    }
    public double getCategoryExpense(int userId, int categoryId) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT IFNULL(SUM(" + AMOUNT + "),0) " +
                        "FROM " + TABLE_TRANSACTION +
                        " WHERE " + USER_ID + "=?" +
                        " AND " + CATEGORY_ID + "=?" +
                        " AND " + TRANSACTION_TYPE + "='Expense'",

                new String[]{
                        String.valueOf(userId),
                        String.valueOf(categoryId)
                });

        double total = 0;

        if(cursor.moveToFirst()){
            total = cursor.getDouble(0);
        }

        cursor.close();

        return total;
    }

    public double getCategoryExpense(int userId,String categoryName){

        SQLiteDatabase db=getReadableDatabase();

        Cursor cursor=db.rawQuery(

                "SELECT IFNULL(SUM(t."+AMOUNT+"),0) " +
                        "FROM "+TABLE_TRANSACTION+" t " +
                        "INNER JOIN "+TABLE_CATEGORY+" c " +
                        "ON t."+CATEGORY_ID+"=c."+CATEGORY_ID+
                        " WHERE t."+USER_ID+"=?" +
                        " AND c."+CATEGORY_NAME+"=?" +
                        " AND t."+TRANSACTION_TYPE+"='Expense'",

                new String[]{
                        String.valueOf(userId),
                        categoryName
                });

        double total=0;

        if(cursor.moveToFirst())
            total=cursor.getDouble(0);

        cursor.close();

        return total;
    }

    public List<Entry> getWeeklyExpenseEntries(int userId){

        ArrayList<Entry> entries = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT strftime('%w'," + TRANSACTION_DATE + "), " +
                        "SUM(" + AMOUNT + ") " +
                        "FROM " + TABLE_TRANSACTION +
                        " WHERE " + USER_ID + "=?" +
                        " AND " + TRANSACTION_TYPE + "='Expense'" +
                        " GROUP BY strftime('%w'," + TRANSACTION_DATE + ")",

                new String[]{
                        String.valueOf(userId)
                });

        while(cursor.moveToNext()){

            entries.add(
                    new Entry(
                            cursor.getFloat(0),
                            cursor.getFloat(1)
                    )
            );

        }

        cursor.close();

        return entries;
    }
    public int getActiveSubscriptionCount(int userId){

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT COUNT(*) FROM " + TABLE_SUBSCRIPTION +
                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        int count = 0;

        if(cursor.moveToFirst())
            count = cursor.getInt(0);

        cursor.close();

        return count;
    }
    public double getMonthlySubscriptionAmount(int userId){

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT IFNULL(SUM(" + AMOUNT + "),0) FROM " +
                        TABLE_SUBSCRIPTION +
                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        double total = 0;

        if(cursor.moveToFirst())
            total = cursor.getDouble(0);

        cursor.close();

        return total;
    }
    public String generateAIInsight(int userId) {

        double income = getTotalIncome(userId);

        double expense = getTotalExpense(userId);

        if(income==0){

            return "Add your first income to start receiving AI insights.";

        }

        if(expense>income){

            return "Warning! Your expenses are higher than your income.";

        }

        if(expense>income*0.80){

            return "You have spent more than 80% of your income this month.";

        }

        return "Great! Your spending is under control.";

    }

    // =====================================================
    // DASHBOARD METHODS
    // =====================================================
    public int getUserIdByEmail(String email) {

        SQLiteDatabase db = getReadableDatabase();

        int userId = -1;

        Cursor cursor = db.rawQuery(

                "SELECT " + USER_ID +
                        " FROM " + TABLE_USER +
                        " WHERE " + USER_EMAIL + "=?",

                new String[]{email}
        );

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();

        return userId;
    }

    public double getDashboardRemainingBudget(int userId) {

        SQLiteDatabase db = getReadableDatabase();

        double totalBudget = 0;

        Cursor cursor = db.rawQuery(

                "SELECT IFNULL(SUM(" + LIMIT_AMOUNT + "),0) " +
                        " FROM " + TABLE_BUDGET +
                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        if(cursor.moveToFirst()){

            totalBudget = cursor.getDouble(0);

        }

        cursor.close();

        double totalExpense = getTotalExpense(userId);

        return totalBudget - totalExpense;

    }

    public double getDashboardBudgetUsed(int userId){

        SQLiteDatabase db = getReadableDatabase();

        double totalBudget = 0;

        Cursor cursor = db.rawQuery(

                "SELECT IFNULL(SUM(" + LIMIT_AMOUNT + "),0) " +
                        " FROM " + TABLE_BUDGET +
                        " WHERE " + USER_ID + "=?",

                new String[]{
                        String.valueOf(userId)
                });

        if(cursor.moveToFirst()){

            totalBudget = cursor.getDouble(0);

        }

        cursor.close();

        if(totalBudget==0)
            return 0;

        double expense = getTotalExpense(userId);

        return (expense / totalBudget) * 100;

    }

    public ArrayList<PieEntry> getCategoryPieEntries(int userId) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String sql =
                "SELECT c." + CATEGORY_NAME +
                        ", IFNULL(SUM(t." + AMOUNT + "),0) " +
                        "FROM " + TABLE_TRANSACTION + " t " +
                        "INNER JOIN " + TABLE_CATEGORY + " c " +
                        "ON t." + CATEGORY_ID + " = c." + CATEGORY_ID +
                        " WHERE t." + USER_ID + "=? " +
                        "AND t." + TRANSACTION_TYPE + "='Expense' " +
                        "GROUP BY c." + CATEGORY_NAME;

        Cursor cursor = db.rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
        );

        while (cursor.moveToNext()) {

            String category = cursor.getString(0);

            float amount = cursor.getFloat(1);

            entries.add(new PieEntry(amount, category));
        }

        cursor.close();

        return entries;
    }
    public int getCategoryIdByName(String categoryName) {

        SQLiteDatabase db = getReadableDatabase();

        int categoryId = -1;

        Cursor cursor = db.rawQuery(

                "SELECT " + CATEGORY_ID +
                        " FROM " + TABLE_CATEGORY +
                        " WHERE " + CATEGORY_NAME + "=?",

                new String[]{categoryName});

        if (cursor.moveToFirst()) {

            categoryId = cursor.getInt(0);

        }

        cursor.close();

        return categoryId;
    }
    public ArrayList<ExpenseModel> getAllExpenses(int userId) {

        ArrayList<ExpenseModel> expenseList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT t." + TRANSACTION_ID +
                        ", c." + CATEGORY_NAME +
                        ", p." + METHOD_NAME +
                        ", t." + AMOUNT +
                        ", t." + TRANSACTION_DATE +
                        ", t." + SOURCE +
                        ", t." + EXPENSE_MODE +

                        " FROM " + TABLE_TRANSACTION + " t " +

                        " INNER JOIN " + TABLE_CATEGORY + " c " +
                        " ON t." + CATEGORY_ID + " = c." + CATEGORY_ID +

                        " INNER JOIN " + TABLE_PAYMENT_METHOD + " p " +
                        " ON t." + PAYMENT_METHOD_ID + " = p." + PAYMENT_METHOD_ID +

                        " WHERE t." + USER_ID + "=? " +
                        " AND t." + TRANSACTION_TYPE + "='Expense' " +

                        " ORDER BY t." + TRANSACTION_DATE + " DESC";

        Cursor cursor = db.rawQuery(
                query,
                new String[]{String.valueOf(userId)}
        );

        while (cursor.moveToNext()) {

            ExpenseModel expense = new ExpenseModel();

            expense.setTransactionId(cursor.getInt(0));

            expense.setCategoryName(cursor.getString(1));

            expense.setPaymentMethod(cursor.getString(2));

            expense.setAmount(cursor.getDouble(3));

            expense.setTransactionDate(cursor.getString(4));

            expense.setNote(cursor.getString(5));

            expense.setExpenseMode(cursor.getString(6));

            expenseList.add(expense);
        }

        cursor.close();

        return expenseList;
    }
    public int deleteExpense(int transactionId) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_TRANSACTION,
                TRANSACTION_ID + "=?",
                new String[]{String.valueOf(transactionId)}
        );
    }
    public Cursor getExpenseById(int transactionId) {

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT " +

                        "t." + TRANSACTION_ID + "," +

                        "t." + CATEGORY_ID + "," +

                        "t." + PAYMENT_METHOD_ID + "," +

                        "t." + AMOUNT + "," +

                        "t." + TRANSACTION_DATE + "," +

                        "t." + SOURCE + "," +

                        "t." + EXPENSE_MODE + "," +

                        "c." + CATEGORY_NAME + "," +

                        "p." + METHOD_NAME +

                        " FROM " + TABLE_TRANSACTION + " t " +

                        " INNER JOIN " + TABLE_CATEGORY + " c " +

                        " ON t." + CATEGORY_ID +

                        "=c." + CATEGORY_ID +

                        " INNER JOIN " + TABLE_PAYMENT_METHOD + " p " +

                        " ON t." + PAYMENT_METHOD_ID +

                        "=p." + PAYMENT_METHOD_ID +

                        " WHERE t." + TRANSACTION_ID + "=?";

        return db.rawQuery(

                query,

                new String[]{

                        String.valueOf(transactionId)

                });

    }

    public Cursor getIncomeById(int transactionId) {

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT " +

                        "t." + TRANSACTION_ID + "," +
                        "t." + CATEGORY_ID + "," +
                        "t." + PAYMENT_METHOD_ID + "," +
                        "t." + AMOUNT + "," +
                        "t." + TRANSACTION_DATE + "," +
                        "t." + SOURCE + "," +
                        "t." + EXPENSE_MODE + "," +
                        "c." + CATEGORY_NAME + "," +
                        "p." + METHOD_NAME +

                        " FROM " + TABLE_TRANSACTION + " t" +

                        " INNER JOIN " + TABLE_CATEGORY + " c" +
                        " ON t." + CATEGORY_ID +
                        "=c." + CATEGORY_ID +

                        " INNER JOIN " + TABLE_PAYMENT_METHOD + " p" +
                        " ON t." + PAYMENT_METHOD_ID +
                        "=p." + PAYMENT_METHOD_ID +

                        " WHERE t." + TRANSACTION_ID + "=?" +

                        " AND t." + TRANSACTION_TYPE + "='Income'";

        return db.rawQuery(
                query,
                new String[]{String.valueOf(transactionId)}
        );
    }
    public int updateExpense(int transactionId,
                             int paymentMethodId,
                             int categoryId,
                             double amount,
                             String transactionDate,
                             String source,
                             String expenseMode) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(PAYMENT_METHOD_ID, paymentMethodId);
        values.put(CATEGORY_ID, categoryId);
        values.put(AMOUNT, amount);
        values.put(TRANSACTION_DATE, transactionDate);
        values.put(SOURCE, source);
        values.put(EXPENSE_MODE, expenseMode);

        return db.update(
                TABLE_TRANSACTION,
                values,
                TRANSACTION_ID + "=?",
                new String[]{String.valueOf(transactionId)}
        );
    }

    public int updateIncome(int transactionId,
                            int paymentMethodId,
                            int categoryId,
                            double amount,
                            String transactionDate,
                            String source,
                            String incomeMode) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(PAYMENT_METHOD_ID, paymentMethodId);
        values.put(CATEGORY_ID, categoryId);
        values.put(AMOUNT, amount);
        values.put(TRANSACTION_DATE, transactionDate);
        values.put(SOURCE, source);
        values.put(EXPENSE_MODE, incomeMode);
        values.put(TRANSACTION_TYPE, "Income");

        return db.update(
                TABLE_TRANSACTION,
                values,
                TRANSACTION_ID + "=?",
                new String[]{String.valueOf(transactionId)}
        );
    }

    public int getExpenseCount(int userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(

                "SELECT COUNT(*) FROM " + TABLE_TRANSACTION +
                        " WHERE " + USER_ID + "=? " +
                        " AND " + TRANSACTION_TYPE + "='Expense'",

                new String[]{String.valueOf(userId)}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    public ArrayList<ExpenseModel> searchExpenses(int userId, String keyword) {

        ArrayList<ExpenseModel> expenseList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT t." + TRANSACTION_ID +
                        ", c." + CATEGORY_NAME +
                        ", p." + METHOD_NAME +
                        ", t." + AMOUNT +
                        ", t." + TRANSACTION_DATE +
                        ", t." + SOURCE +
                        ", t." + EXPENSE_MODE +

                        " FROM " + TABLE_TRANSACTION + " t " +

                        " INNER JOIN " + TABLE_CATEGORY + " c ON " +
                        "t." + CATEGORY_ID + "=c." + CATEGORY_ID +

                        " INNER JOIN " + TABLE_PAYMENT_METHOD + " p ON " +
                        "t." + PAYMENT_METHOD_ID + "=p." + PAYMENT_METHOD_ID +

                        " WHERE t." + USER_ID + "=?" +
                        " AND t." + TRANSACTION_TYPE + "='Expense'" +

                        " AND (" +

                        "c." + CATEGORY_NAME + " LIKE ?" +

                        " OR " +

                        "p." + METHOD_NAME + " LIKE ?" +

                        " OR " +

                        "t." + SOURCE + " LIKE ?" +

                        " OR " +

                        "t." + TRANSACTION_DATE + " LIKE ? )" +

                        " ORDER BY t." + TRANSACTION_DATE + " DESC";

        String search = "%" + keyword + "%";

        Cursor cursor = db.rawQuery(query,

                new String[]{

                        String.valueOf(userId),

                        search,

                        search,

                        search,

                        search

                });

        while (cursor.moveToNext()) {

            ExpenseModel expense = new ExpenseModel();

            expense.setTransactionId(cursor.getInt(0));
            expense.setCategoryName(cursor.getString(1));
            expense.setPaymentMethod(cursor.getString(2));
            expense.setAmount(cursor.getDouble(3));
            expense.setTransactionDate(cursor.getString(4));
            expense.setNote(cursor.getString(5));
            expense.setExpenseMode(cursor.getString(6));

            expenseList.add(expense);

        }

        cursor.close();

        return expenseList;

    }

    public ArrayList<ExpenseModel> getExpensesByCategory(int userId,
                                                         String category) {

        ArrayList<ExpenseModel> expenseList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT t." + TRANSACTION_ID +
                        ", c." + CATEGORY_NAME +
                        ", p." + METHOD_NAME +
                        ", t." + AMOUNT +
                        ", t." + TRANSACTION_DATE +
                        ", t." + SOURCE +
                        ", t." + EXPENSE_MODE +

                        " FROM " + TABLE_TRANSACTION + " t " +

                        " INNER JOIN " + TABLE_CATEGORY + " c " +
                        " ON t." + CATEGORY_ID + "=c." + CATEGORY_ID +

                        " INNER JOIN " + TABLE_PAYMENT_METHOD + " p " +
                        " ON t." + PAYMENT_METHOD_ID + "=p." + PAYMENT_METHOD_ID +

                        " WHERE t." + USER_ID + "=?" +

                        " AND t." + TRANSACTION_TYPE + "='Expense'" +

                        " AND c." + CATEGORY_NAME + "=?" +

                        " ORDER BY t." + TRANSACTION_DATE + " DESC";

        Cursor cursor = db.rawQuery(

                query,

                new String[]{

                        String.valueOf(userId),

                        category

                });

        while (cursor.moveToNext()) {

            ExpenseModel expense = new ExpenseModel();

            expense.setTransactionId(cursor.getInt(0));
            expense.setCategoryName(cursor.getString(1));
            expense.setPaymentMethod(cursor.getString(2));
            expense.setAmount(cursor.getDouble(3));
            expense.setTransactionDate(cursor.getString(4));
            expense.setNote(cursor.getString(5));
            expense.setExpenseMode(cursor.getString(6));

            expenseList.add(expense);
        }

        cursor.close();

        return expenseList;
    }

    public ArrayList<ExpenseModel> filterExpenses(int userId,
                                                  String category,
                                                  String paymentMethod,
                                                  String expenseMode,
                                                  String sort) {

        ArrayList<ExpenseModel> expenseList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        StringBuilder query = new StringBuilder();

        query.append("SELECT t.")
                .append(TRANSACTION_ID)
                .append(", c.")
                .append(CATEGORY_NAME)
                .append(", p.")
                .append(METHOD_NAME)
                .append(", t.")
                .append(AMOUNT)
                .append(", t.")
                .append(TRANSACTION_DATE)
                .append(", t.")
                .append(SOURCE)
                .append(", t.")
                .append(EXPENSE_MODE)
                .append(" FROM ")
                .append(TABLE_TRANSACTION)
                .append(" t ")
                .append("INNER JOIN ")
                .append(TABLE_CATEGORY)
                .append(" c ON t.")
                .append(CATEGORY_ID)
                .append(" = c.")
                .append(CATEGORY_ID)
                .append(" INNER JOIN ")
                .append(TABLE_PAYMENT_METHOD)
                .append(" p ON t.")
                .append(PAYMENT_METHOD_ID)
                .append(" = p.")
                .append(PAYMENT_METHOD_ID)
                .append(" WHERE t.")
                .append(USER_ID)
                .append("=?")
                .append(" AND t.")
                .append(TRANSACTION_TYPE)
                .append("='Expense'");

        ArrayList<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));

        if (!category.equals("All")) {
            query.append(" AND c.").append(CATEGORY_NAME).append("=?");
            args.add(category);
        }

        if (!paymentMethod.equals("All")) {
            query.append(" AND p.").append(METHOD_NAME).append("=?");
            args.add(paymentMethod);
        }

        if (!expenseMode.equals("All")) {
            query.append(" AND t.").append(EXPENSE_MODE).append("=?");
            args.add(expenseMode);
        }

        switch (sort) {

            case "Oldest":
                query.append(" ORDER BY t.")
                        .append(TRANSACTION_DATE)
                        .append(" ASC");
                break;

            case "Highest Amount":
                query.append(" ORDER BY t.")
                        .append(AMOUNT)
                        .append(" DESC");
                break;

            case "Lowest Amount":
                query.append(" ORDER BY t.")
                        .append(AMOUNT)
                        .append(" ASC");
                break;

            default:
                query.append(" ORDER BY t.")
                        .append(TRANSACTION_DATE)
                        .append(" DESC");
                break;
        }

        Cursor cursor = db.rawQuery(
                query.toString(),
                args.toArray(new String[0]));

        while (cursor.moveToNext()) {

            ExpenseModel expense = new ExpenseModel();

            expense.setTransactionId(cursor.getInt(0));
            expense.setCategoryName(cursor.getString(1));
            expense.setPaymentMethod(cursor.getString(2));
            expense.setAmount(cursor.getDouble(3));
            expense.setTransactionDate(cursor.getString(4));
            expense.setNote(cursor.getString(5));
            expense.setExpenseMode(cursor.getString(6));

            expenseList.add(expense);
        }

        cursor.close();

        return expenseList;
    }

    public ArrayList<IncomeModel> getAllIncome(int userId) {

        ArrayList<IncomeModel> incomeList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT t." + TRANSACTION_ID +
                        ", c." + CATEGORY_NAME +
                        ", p." + METHOD_NAME +
                        ", t." + AMOUNT +
                        ", t." + TRANSACTION_DATE +
                        ", t." + SOURCE +
                        ", t." + EXPENSE_MODE +

                        " FROM " + TABLE_TRANSACTION + " t " +

                        " INNER JOIN " + TABLE_CATEGORY + " c " +
                        " ON t." + CATEGORY_ID + " = c." + CATEGORY_ID +

                        " INNER JOIN " + TABLE_PAYMENT_METHOD + " p " +
                        " ON t." + PAYMENT_METHOD_ID + " = p." + PAYMENT_METHOD_ID +

                        " WHERE t." + USER_ID + "=? " +
                        " AND t." + TRANSACTION_TYPE + "='Income' " +

                        " ORDER BY t." + TRANSACTION_DATE + " DESC";

        Cursor cursor = db.rawQuery(
                query,
                new String[]{String.valueOf(userId)}
        );

        while (cursor.moveToNext()) {

            IncomeModel income = createIncomeFromCursor(cursor);

            incomeList.add(income);
        }

        cursor.close();

        return incomeList;
    }


    public int getIncomeCount(int userId) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TRANSACTION +
                        " WHERE " + USER_ID + "=?" +
                        " AND " + TRANSACTION_TYPE + "='Income'",
                new String[]{String.valueOf(userId)}
        );

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }


    public ArrayList<IncomeModel> searchIncome(int userId,
                                               String keyword) {

        ArrayList<IncomeModel> incomeList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT t." + TRANSACTION_ID +
                        ", c." + CATEGORY_NAME +
                        ", p." + METHOD_NAME +
                        ", t." + AMOUNT +
                        ", t." + TRANSACTION_DATE +
                        ", t." + SOURCE +
                        ", t." + EXPENSE_MODE +

                        " FROM " + TABLE_TRANSACTION + " t " +

                        " INNER JOIN " + TABLE_CATEGORY + " c " +
                        " ON t." + CATEGORY_ID + "=c." + CATEGORY_ID +

                        " INNER JOIN " + TABLE_PAYMENT_METHOD + " p " +
                        " ON t." + PAYMENT_METHOD_ID + "=p." + PAYMENT_METHOD_ID +

                        " WHERE t." + USER_ID + "=?" +
                        " AND t." + TRANSACTION_TYPE + "='Income'" +

                        " AND (" +
                        "c." + CATEGORY_NAME + " LIKE ?" +
                        " OR p." + METHOD_NAME + " LIKE ?" +
                        " OR t." + SOURCE + " LIKE ?" +
                        " OR t." + TRANSACTION_DATE + " LIKE ?)" +

                        " ORDER BY t." + TRANSACTION_DATE + " DESC";

        String search = "%" + keyword + "%";

        Cursor cursor = db.rawQuery(
                query,
                new String[]{
                        String.valueOf(userId),
                        search,
                        search,
                        search,
                        search
                }
        );

        while (cursor.moveToNext()) {

            IncomeModel income = createIncomeFromCursor(cursor);

            incomeList.add(income);
        }

        cursor.close();

        return incomeList;
    }


    public ArrayList<IncomeModel> getIncomeByCategory(int userId,
                                                      String category) {

        ArrayList<IncomeModel> incomeList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT t." + TRANSACTION_ID +
                        ", c." + CATEGORY_NAME +
                        ", p." + METHOD_NAME +
                        ", t." + AMOUNT +
                        ", t." + TRANSACTION_DATE +
                        ", t." + SOURCE +
                        ", t." + EXPENSE_MODE +

                        " FROM " + TABLE_TRANSACTION + " t " +

                        " INNER JOIN " + TABLE_CATEGORY + " c " +
                        " ON t." + CATEGORY_ID + "=c." + CATEGORY_ID +

                        " INNER JOIN " + TABLE_PAYMENT_METHOD + " p " +
                        " ON t." + PAYMENT_METHOD_ID + "=p." + PAYMENT_METHOD_ID +

                        " WHERE t." + USER_ID + "=?" +
                        " AND t." + TRANSACTION_TYPE + "='Income'" +
                        " AND c." + CATEGORY_NAME + "=?" +

                        " ORDER BY t." + TRANSACTION_DATE + " DESC";

        Cursor cursor = db.rawQuery(
                query,
                new String[]{
                        String.valueOf(userId),
                        category
                }
        );

        while (cursor.moveToNext()) {

            IncomeModel income = createIncomeFromCursor(cursor);

            incomeList.add(income);
        }

        cursor.close();

        return incomeList;
    }


    public ArrayList<IncomeModel> filterIncome(int userId,
                                               String category,
                                               String incomeSource,
                                               String incomeMode,
                                               String sort) {

        ArrayList<IncomeModel> incomeList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        StringBuilder query = new StringBuilder();

        query.append("SELECT t.")
                .append(TRANSACTION_ID)
                .append(", c.")
                .append(CATEGORY_NAME)
                .append(", p.")
                .append(METHOD_NAME)
                .append(", t.")
                .append(AMOUNT)
                .append(", t.")
                .append(TRANSACTION_DATE)
                .append(", t.")
                .append(SOURCE)
                .append(", t.")
                .append(EXPENSE_MODE)
                .append(" FROM ")
                .append(TABLE_TRANSACTION)
                .append(" t ")
                .append("INNER JOIN ")
                .append(TABLE_CATEGORY)
                .append(" c ON t.")
                .append(CATEGORY_ID)
                .append("=c.")
                .append(CATEGORY_ID)
                .append(" INNER JOIN ")
                .append(TABLE_PAYMENT_METHOD)
                .append(" p ON t.")
                .append(PAYMENT_METHOD_ID)
                .append("=p.")
                .append(PAYMENT_METHOD_ID)
                .append(" WHERE t.")
                .append(USER_ID)
                .append("=?")
                .append(" AND t.")
                .append(TRANSACTION_TYPE)
                .append("='Income'");

        ArrayList<String> arguments = new ArrayList<>();

        arguments.add(String.valueOf(userId));

        if (!category.equals("All")) {

            query.append(" AND c.")
                    .append(CATEGORY_NAME)
                    .append("=?");

            arguments.add(category);
        }

        if (!incomeSource.equals("All")) {

            query.append(" AND p.")
                    .append(METHOD_NAME)
                    .append("=?");

            arguments.add(incomeSource);
        }

        if (!incomeMode.equals("All")) {

            query.append(" AND t.")
                    .append(EXPENSE_MODE)
                    .append("=?");

            arguments.add(incomeMode);
        }

        switch (sort) {

            case "Oldest":

                query.append(" ORDER BY t.")
                        .append(TRANSACTION_DATE)
                        .append(" ASC");

                break;

            case "Highest Amount":

                query.append(" ORDER BY t.")
                        .append(AMOUNT)
                        .append(" DESC");

                break;

            case "Lowest Amount":

                query.append(" ORDER BY t.")
                        .append(AMOUNT)
                        .append(" ASC");

                break;

            default:

                query.append(" ORDER BY t.")
                        .append(TRANSACTION_DATE)
                        .append(" DESC");

                break;
        }

        Cursor cursor = db.rawQuery(
                query.toString(),
                arguments.toArray(new String[0])
        );

        while (cursor.moveToNext()) {

            IncomeModel income = createIncomeFromCursor(cursor);

            incomeList.add(income);
        }

        cursor.close();

        return incomeList;
    }


    private IncomeModel createIncomeFromCursor(Cursor cursor) {

        IncomeModel income = new IncomeModel();

        income.setTransactionId(cursor.getInt(0));
        income.setCategoryName(cursor.getString(1));
        income.setIncomeSource(cursor.getString(2));
        income.setAmount(cursor.getDouble(3));
        income.setTransactionDate(cursor.getString(4));
        income.setNote(cursor.getString(5));
        income.setIncomeMode(cursor.getString(6));

        return income;
    }

    public boolean updateUserProfile(int userId,
                                     String name,
                                     String mobile) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_NAME, name);

        values.put(USER_MOBILE, mobile);

        int rows = db.update(

                TABLE_USER,

                values,

                USER_ID + "=?",

                new String[]{String.valueOf(userId)}

        );

        return rows > 0;
    }

    public boolean updateUserMobile(int userId, String mobile) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(USER_MOBILE, mobile);

        int rows = db.update(
                TABLE_USER,
                values,
                USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );

        return rows > 0;
    }

    public boolean isMobileExists(String mobile) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USER,
                new String[]{USER_ID},
                USER_MOBILE + "=?",
                new String[]{mobile},
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();

        cursor.close();

        return exists;
    }

    public boolean saveBudgetPeriod(
            int userId,
            String budgetPeriod
    ) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(PREF_BUDGET_PERIOD, budgetPeriod);

        values.put(PREF_UPDATED_AT,
                System.currentTimeMillis());

        int updatedRows = db.update(
                TABLE_FINANCIAL_PREFERENCES,
                values,
                PREF_USER_ID + "=?",
                new String[]{
                        String.valueOf(userId)
                }
        );

        if (updatedRows > 0) {
            return true;
        }

        values.put(PREF_USER_ID, userId);

        values.put(PREF_CURRENCY_CODE, "LKR");

        values.put(PREF_CURRENCY_NAME,
                "Sri Lankan Rupee");

        values.put(PREF_CURRENCY_SYMBOL, "₨");

        long insertedRow = db.insert(
                TABLE_FINANCIAL_PREFERENCES,
                null,
                values
        );

        return insertedRow != -1;
    }

    public boolean saveCurrencyPreference(
            int userId,
            String currencyCode,
            String currencyName,
            String currencySymbol
    ) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(PREF_CURRENCY_CODE, currencyCode);
        values.put(PREF_CURRENCY_NAME, currencyName);
        values.put(PREF_CURRENCY_SYMBOL, currencySymbol);
        values.put(PREF_UPDATED_AT, System.currentTimeMillis());

        int updatedRows = db.update(
                TABLE_FINANCIAL_PREFERENCES,
                values,
                PREF_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );

        if (updatedRows > 0) {
            return true;
        }

        values.put(PREF_USER_ID, userId);
        values.put(PREF_BUDGET_PERIOD, "Monthly");

        long insertedRow = db.insert(
                TABLE_FINANCIAL_PREFERENCES,
                null,
                values
        );

        return insertedRow != -1;
    }

    public Cursor getFinancialPreferences(int userId) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_FINANCIAL_PREFERENCES,
                null,
                PREF_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );
    }

    public String getSavedCurrencyCode(int userId) {

        String currencyCode = "LKR";

        Cursor cursor = getFinancialPreferences(userId);

        if (cursor.moveToFirst()) {

            currencyCode = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            PREF_CURRENCY_CODE
                    )
            );
        }

        cursor.close();

        return currencyCode;
    }

    public String getSavedCurrencyDisplay(int userId) {

        String display = "₨ (LKR)";

        Cursor cursor = getFinancialPreferences(userId);

        if (cursor.moveToFirst()) {

            String symbol = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            PREF_CURRENCY_SYMBOL
                    )
            );

            String code = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            PREF_CURRENCY_CODE
                    )
            );

            display = symbol + " (" + code + ")";
        }

        cursor.close();

        return display;
    }

    public String getSavedBudgetPeriod(
            int userId
    ) {

        String budgetPeriod = "Monthly";

        Cursor cursor =
                getFinancialPreferences(userId);

        if (cursor.moveToFirst()) {

            budgetPeriod = cursor.getString(

                    cursor.getColumnIndexOrThrow(
                            PREF_BUDGET_PERIOD
                    )

            );

        }

        cursor.close();

        return budgetPeriod;
    }

}
