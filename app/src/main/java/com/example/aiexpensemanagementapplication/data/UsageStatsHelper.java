package com.example.aiexpensemanagementapplication.data;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.List;

public class UsageStatsHelper {

    private final Context context;
    private final UsageStatsManager usageStatsManager;

    public UsageStatsHelper(Context context) {

        this.context = context.getApplicationContext();

        usageStatsManager =
                (UsageStatsManager) context.getSystemService(
                        Context.USAGE_STATS_SERVICE
                );
    }


    // =========================================================
    // 1. CHECK USAGE ACCESS PERMISSION
    // =========================================================

    public boolean hasUsageAccessPermission() {

        AppOpsManager appOpsManager =
                (AppOpsManager) context.getSystemService(
                        Context.APP_OPS_SERVICE
                );

        int mode;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            mode = appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.getPackageName()
            );

        } else {

            mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.getPackageName()
            );
        }

        return mode == AppOpsManager.MODE_ALLOWED;
    }


    // =========================================================
    // 2. OPEN USAGE ACCESS SETTINGS
    // =========================================================

    public void openUsageAccessSettings() {

        Intent intent = new Intent(
                Settings.ACTION_USAGE_ACCESS_SETTINGS
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        context.startActivity(intent);
    }


    // =========================================================
    // 3. GET USAGE FOR ONE APP - LAST 30 DAYS
    // =========================================================

    public long getUsageMinutesLast30Days(
            String packageName) {

        if (!hasUsageAccessPermission()) {
            return 0;
        }

        if (packageName == null ||
                packageName.trim().isEmpty()) {
            return 0;
        }

        Calendar calendar =
                Calendar.getInstance();

        long endTime =
                calendar.getTimeInMillis();

        calendar.add(
                Calendar.DAY_OF_YEAR,
                -30
        );

        long startTime =
                calendar.getTimeInMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_WEEKLY,
                        startTime,
                        endTime
                );

        long totalUsageMillis = 0;

        if (usageStatsList != null) {

            for (UsageStats usageStats :
                    usageStatsList) {

                if (usageStats == null) {
                    continue;
                }

                if (packageName.equals(
                        usageStats.getPackageName()
                )) {

                    totalUsageMillis +=
                            usageStats
                                    .getTotalTimeInForeground();
                }
            }
        }

        return totalUsageMillis /
                (1000L * 60L);
    }
    public long testWeeklyUsageLast30Days(
            String packageName) {

        if (!hasUsageAccessPermission()) {
            return 0;
        }

        if (packageName == null ||
                packageName.trim().isEmpty()) {
            return 0;
        }

        Calendar calendar =
                Calendar.getInstance();

        long endTime =
                calendar.getTimeInMillis();

        calendar.add(
                Calendar.DAY_OF_YEAR,
                -30
        );

        long startTime =
                calendar.getTimeInMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_WEEKLY,
                        startTime,
                        endTime
                );

        long totalUsageMillis = 0;

        if (usageStatsList != null) {

            for (UsageStats usageStats :
                    usageStatsList) {

                if (usageStats == null) {
                    continue;
                }

                if (packageName.equals(
                        usageStats.getPackageName()
                )) {

                    totalUsageMillis +=
                            usageStats
                                    .getTotalTimeInForeground();
                }
            }
        }

        return totalUsageMillis /
                (1000L * 60L);
    }

    // =========================================================
    // 4. GET USAGE FOR ONE APP - LAST 7 DAYS
    // =========================================================

    public long getUsageMinutesLast7Days(
            String packageName) {

        if (!hasUsageAccessPermission()) {
            return 0;
        }

        Calendar calendar =
                Calendar.getInstance();

        long endTime =
                calendar.getTimeInMillis();

        calendar.add(
                Calendar.DAY_OF_YEAR,
                -7
        );

        long startTime =
                calendar.getTimeInMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        long totalUsageMillis = 0;

        if (usageStatsList != null) {

            for (UsageStats usageStats :
                    usageStatsList) {

                if (packageName.equals(
                        usageStats.getPackageName()
                )) {

                    totalUsageMillis +=
                            usageStats
                                    .getTotalTimeInForeground();
                }
            }
        }

        return totalUsageMillis /
                (1000 * 60);
    }


    // =========================================================
    // 5. GET ALL APPS USAGE - LAST 30 DAYS
    // =========================================================

    public List<UsageStats> getAllAppsUsageLast30Days() {

        List<UsageStats> result =
                new ArrayList<>();

        if (!hasUsageAccessPermission()) {
            return result;
        }

        Calendar calendar =
                Calendar.getInstance();

        long endTime =
                calendar.getTimeInMillis();

        calendar.add(
                Calendar.DAY_OF_YEAR,
                -30
        );

        long startTime =
                calendar.getTimeInMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        if (usageStatsList != null) {

            result.addAll(
                    usageStatsList
            );
        }

        return result;
    }


    // =========================================================
    // 6. CALCULATE AVERAGE DAILY USAGE
    // =========================================================

    public double calculateAverageDailyUsage(
            long usageMinutes,
            int numberOfDays) {

        if (numberOfDays <= 0) {
            return 0;
        }

        return (double) usageMinutes /
                numberOfDays;
    }
    // =========================================================
// 7. GET USAGE FROM SUBSCRIPTION START DATE UNTIL NOW
// =========================================================

    public long getUsageMinutesFromStartDate(
            String packageName,
            long subscriptionStartTime) {

        if (!hasUsageAccessPermission()) {
            return 0;
        }

        long endTime =
                Calendar.getInstance().getTimeInMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        subscriptionStartTime,
                        endTime
                );

        long totalUsageMillis = 0;

        if (usageStatsList != null) {

            for (UsageStats usageStats :
                    usageStatsList) {

                if (packageName.equals(
                        usageStats.getPackageName()
                )) {

                    totalUsageMillis +=
                            usageStats.getTotalTimeInForeground();
                }
            }
        }

        return totalUsageMillis / (1000 * 60);
    }
    public long getUsageMinutesLast30DaysWeekly(
            String packageName) {

        if (!hasUsageAccessPermission()) {
            return 0;
        }

        if (packageName == null ||
                packageName.trim().isEmpty()) {
            return 0;
        }

        Calendar calendar =
                Calendar.getInstance();

        long endTime =
                calendar.getTimeInMillis();

        calendar.add(
                Calendar.DAY_OF_YEAR,
                -30
        );

        long startTime =
                calendar.getTimeInMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_WEEKLY,
                        startTime,
                        endTime
                );

        long totalUsageMillis = 0;

        if (usageStatsList != null) {

            for (UsageStats usageStats :
                    usageStatsList) {

                if (usageStats == null) {
                    continue;
                }

                if (packageName.equals(
                        usageStats.getPackageName()
                )) {

                    totalUsageMillis +=
                            usageStats
                                    .getTotalTimeInForeground();
                }
            }
        }

        return totalUsageMillis /
                (1000L * 60L);
    }
}