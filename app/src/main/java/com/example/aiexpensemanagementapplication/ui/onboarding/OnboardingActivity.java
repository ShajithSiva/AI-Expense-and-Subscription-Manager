package com.example.aiexpensemanagementapplication.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.ui.SplashActivity;

public class OnboardingActivity extends
        androidx.appcompat.app.AppCompatActivity {

    private static final String PREFS_NAME = "ExpenseVaultPrefs";
    private static final String KEY_ONBOARDING_COMPLETED =
            "onboarding_completed";

    private ViewPager2 viewPager;
    private LinearLayout indicators;

    private TextView btnSkip;
    private TextView btnBack;
    private TextView btnNext;

    private TextView[] dots;
    private OnboardingAdapter adapter;

    private final int green = Color.parseColor("#16A34A");
    private final int ash = Color.parseColor("#D9E0DD");

    private final ViewPager2.OnPageChangeCallback pageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updatePage(position);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        indicators = findViewById(R.id.layoutIndicators);
        btnSkip = findViewById(R.id.btnSkip);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        // Set up onboarding pages.
        adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setOffscreenPageLimit(1);

        // Create indicators based on the adapter's page count.
        dots = new TextView[adapter.getItemCount()];
        createIndicators();

        viewPager.registerOnPageChangeCallback(pageChangeCallback);
        updatePage(viewPager.getCurrentItem());

        // Next / Get Started button.
        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            int lastPage = adapter.getItemCount() - 1;

            if (current < lastPage) {
                viewPager.setCurrentItem(current + 1, true);
            } else {
                finishOnboarding();
            }
        });

        // Back button inside onboarding.
        btnBack.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();

            if (current > 0) {
                viewPager.setCurrentItem(current - 1, true);
            }
        });

        // Skip onboarding.
        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void createIndicators() {
        indicators.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            TextView dot = new TextView(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(dp(9), dp(9));

            params.setMargins(dp(5), 0, dp(5), 0);
            dot.setLayoutParams(params);
            dot.setBackground(makeDot(ash));

            dot.setContentDescription(
                    "Go to onboarding page " + (i + 1)
            );

            dot.setClickable(true);
            dot.setFocusable(true);

            final int page = i;

            dot.setOnClickListener(v ->
                    viewPager.setCurrentItem(page, true)
            );

            dots[i] = dot;
            indicators.addView(dot);
        }
    }

    private void updatePage(int position) {
        if (dots == null || adapter == null) {
            return;
        }

        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackground(
                    makeDot(i == position ? green : ash)
            );
        }

        // Hide Back on the first page.
        btnBack.setVisibility(
                position == 0 ? View.INVISIBLE : View.VISIBLE
        );

        // Hide Skip on the final page.
        btnSkip.setVisibility(
                position == adapter.getItemCount() - 1
                        ? View.INVISIBLE
                        : View.VISIBLE
        );

        // Change the button label on the final page.
        btnNext.setText(
                position == adapter.getItemCount() - 1
                        ? "Get Started  →"
                        : "Next  →"
        );
    }

    private GradientDrawable makeDot(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private void finishOnboarding() {
        // Save that onboarding has been completed.
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        boolean saved = preferences.edit()
                .putBoolean(KEY_ONBOARDING_COMPLETED, true)
                .commit();

        if (!saved) {
            // Do not continue if the preference could not be saved.
            return;
        }

        // Continue through the existing splash/authentication flow.
        Intent intent = new Intent(
                OnboardingActivity.this,
                SplashActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (viewPager != null) {
            viewPager.unregisterOnPageChangeCallback(
                    pageChangeCallback
            );
        }

        super.onDestroy();
    }
}
