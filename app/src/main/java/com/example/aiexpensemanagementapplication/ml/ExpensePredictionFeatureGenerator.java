package com.example.aiexpensemanagementapplication.ml;

import android.content.Context;

import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;
import com.example.aiexpensemanagementapplication.ui.expense.ExpenseModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpensePredictionFeatureGenerator {

    private final DatabaseHelper databaseHelper;

    public ExpensePredictionFeatureGenerator(Context context) {
        databaseHelper =
                new DatabaseHelper(context);
    }

    // =========================================================
    // GENERATE REQUEST
    // =========================================================

    public ExpensePredictionRequest generateRequest(
            int userId,
            int year,
            int month
    ) {

        String monthString =
                String.format(
                        Locale.US,
                        "%04d-%02d",
                        year,
                        month
                );

        // -----------------------------------------------------
        // Monthly income
        // -----------------------------------------------------

        double monthlyIncome =
                databaseHelper.getMonthlyIncome(
                        userId,
                        monthString
                );

        // -----------------------------------------------------
        // Expenses
        // -----------------------------------------------------

        double bills = 0;
        double education = 0;
        double entertainment = 0;
        double food = 0;
        double health = 0;
        double shopping = 0;
        double transport = 0;
        double travel = 0;

        double totalExpense = 0;

        int transactionCount = 0;

        double weekendExpense = 0;

        ArrayList<ExpenseModel> expenses =
                databaseHelper.getAllExpenses(userId);

        if (expenses != null) {

            for (ExpenseModel expense : expenses) {

                if (expense == null) {
                    continue;
                }

                String date =
                        expense.getTransactionDate();

                if (date == null) {
                    continue;
                }

                // -------------------------------------------------
                // Only selected month
                // -------------------------------------------------

                if (!date.startsWith(monthString)) {
                    continue;
                }

                double amount =
                        expense.getAmount();

                if (amount < 0) {
                    amount = Math.abs(amount);
                }

                if (amount <= 0) {
                    continue;
                }

                totalExpense += amount;

                transactionCount++;

                // -------------------------------------------------
                // Category
                // -------------------------------------------------

                String category =
                        expense.getCategoryName();

                if (category != null) {

                    switch (category.trim()) {

                        case "Bills":
                            bills += amount;
                            break;

                        case "Education":
                            education += amount;
                            break;

                        case "Entertainment":
                            entertainment += amount;
                            break;

                        case "Food":
                            food += amount;
                            break;

                        case "Health":
                            health += amount;
                            break;

                        case "Shopping":
                            shopping += amount;
                            break;

                        case "Transport":
                            transport += amount;
                            break;

                        case "Travel":
                            travel += amount;
                            break;
                    }
                }

                // -------------------------------------------------
                // Weekend spending
                // -------------------------------------------------

                if (isWeekend(date)) {
                    weekendExpense += amount;
                }
            }
        }

        // =====================================================
        // DERIVED FEATURES
        // =====================================================

        double savings =
                monthlyIncome - totalExpense;

        double savingsRate = 0;

        double expenseRatio = 0;

        if (monthlyIncome != 0) {

            savingsRate =
                    savings / monthlyIncome;

            expenseRatio =
                    totalExpense / monthlyIncome;
        }

        // Dataset convention
        double averageDailyExpense =
                totalExpense / 30.0;

        double weekendSpendingPercentage = 0;

        if (totalExpense > 0) {

            weekendSpendingPercentage =
                    (weekendExpense / totalExpense)
                            * 100.0;
        }

        // -----------------------------------------------------
        // Spending profile
        //
        // This is an integration rule because the original
        // dataset's exact profile-generation rule is not
        // available in the Android project.
        // -----------------------------------------------------

        String spendingProfile;

        if (expenseRatio <= 0.40) {

            spendingProfile = "Conservative";

        } else if (expenseRatio >= 0.80) {

            spendingProfile = "Heavy";

        } else {

            spendingProfile = "Moderate";
        }

        // =====================================================
        // BUILD REQUEST
        // =====================================================

        return new ExpensePredictionRequest(

                year,
                month,

                monthlyIncome,

                bills,
                education,
                entertainment,
                food,
                health,
                shopping,
                transport,
                travel,

                totalExpense,

                savings,
                savingsRate,
                expenseRatio,

                averageDailyExpense,
                weekendSpendingPercentage,

                transactionCount,

                spendingProfile
        );
    }

    // =========================================================
    // WEEKEND CHECK
    // =========================================================

    private boolean isWeekend(String transactionDate) {

        try {

            Date date = parseDate(transactionDate);

            if (date == null) {
                return false;
            }

            Calendar calendar =
                    Calendar.getInstance();

            calendar.setTime(date);

            int day =
                    calendar.get(Calendar.DAY_OF_WEEK);

            return day == Calendar.SATURDAY
                    || day == Calendar.SUNDAY;

        } catch (Exception e) {

            return false;
        }
    }

    // =========================================================
    // DATE PARSER
    // =========================================================

    private Date parseDate(String value) {

        String[] formats = {

                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd",
                "yyyy/MM/dd HH:mm:ss"
        };

        for (String format : formats) {

            try {

                SimpleDateFormat sdf =
                        new SimpleDateFormat(
                                format,
                                Locale.US
                        );

                sdf.setLenient(false);

                return sdf.parse(value);

            } catch (ParseException ignored) {
            }
        }

        return null;
    }
}