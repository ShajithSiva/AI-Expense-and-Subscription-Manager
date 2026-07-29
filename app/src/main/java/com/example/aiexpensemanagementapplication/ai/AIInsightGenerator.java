package com.example.aiexpensemanagementapplication.ai;

import android.content.Context;

import com.example.aiexpensemanagementapplication.data.local.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AIInsightGenerator {

    private DatabaseHelper databaseHelper;

    public AIInsightGenerator(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public List<AIInsight> generateInsights(int userId) {

        List<AIInsight> insights = new ArrayList<>();

        // AI rules will be added here

        return insights;
    }
}