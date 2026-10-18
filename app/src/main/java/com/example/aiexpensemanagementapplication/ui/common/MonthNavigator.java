
package com.example.aiexpensemanagementapplication.ui.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class MonthNavigator {

    private static String selectedMonth = null;
    private static String calendarMonth = null;

    private MonthNavigator() {
    }

    private static String formatMonth(Calendar calendar) {
        return String.format(
                Locale.US,
                "%04d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1
        );
    }

    public static String getSelectedMonth() {
        String currentMonth = formatMonth(Calendar.getInstance());

        if (selectedMonth == null ||
                !currentMonth.equals(calendarMonth)) {
            selectedMonth = currentMonth;
            calendarMonth = currentMonth;
        }

        return selectedMonth;
    }

    public static void changeMonth(int offset) {
        String month = getSelectedMonth();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        calendar.set(
                Integer.parseInt(month.substring(0, 4)),
                Integer.parseInt(month.substring(5, 7)) - 1,
                1
        );

        calendar.add(Calendar.MONTH, offset);

        String newMonth = formatMonth(calendar);
        String currentMonth = formatMonth(Calendar.getInstance());

        // Prevent navigating into future months
        if (newMonth.compareTo(currentMonth) <= 0) {
            selectedMonth = newMonth;
        }
    }

    public static String getMonthTitle() {
        String month = getSelectedMonth();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        calendar.set(
                Integer.parseInt(month.substring(0, 4)),
                Integer.parseInt(month.substring(5, 7)) - 1,
                1
        );

        return new SimpleDateFormat(
                "MMMM yyyy",
                Locale.ENGLISH
        ).format(calendar.getTime());
    }
}
