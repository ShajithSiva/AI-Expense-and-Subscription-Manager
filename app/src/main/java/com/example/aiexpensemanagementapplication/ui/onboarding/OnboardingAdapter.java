
package com.example.aiexpensemanagementapplication.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;

public class OnboardingAdapter
        extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    // All 10 onboarding illustrations
    private final int[] illustrations = {
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3,
            R.drawable.image4,
            R.drawable.image5,
            R.drawable.image6,
            R.drawable.image7,
            R.drawable.image8,
            R.drawable.image9,
            R.drawable.image10
    };

    private final String[] captions = {
            "SMART MONEY MANAGEMENT",
            "EVERYTHING IN ONE PLACE",
            "AI-POWERED DETECTION",
            "BUDGETS & FINANCIAL INSIGHTS",
            "STAY INFORMED",
            "SECURE CLOUD SYNC",
            "ACHIEVE YOUR FINANCIAL GOALS",
            "PREDICT FUTURE EXPENSES",
            "YOUR PERSONAL AI ASSISTANT",
            "START YOUR FINANCIAL JOURNEY"
    };

    private final String[] titles = {
            "Welcome to ExpenseVault",
            "Your Money, One Dashboard",
            "Smart Subscription Management",
            "Budgets & Financial Insights",
            "Stay Ahead of Upcoming Payments",
            "Your Data, Protected",
            "Achieve Your Financial Goals",
            "Predict Your Future Expenses",
            "Your Personal AI Assistant",
            "Take Control of Your Finances"
    };

    private final String[] descriptions = {
            "Take control of your finances. Track expenses, income and subscriptions in one place.",
            "See your financial picture at a glance with balances, expenses, income and budget summaries.",
            "Discover recurring payments and review subscriptions detected from supported connected sources.",
            "Set spending limits, follow category trends and make smarter financial decisions.",
            "Keep track of upcoming payments and budget alerts so you can plan ahead.",
            "Keep your financial information protected with secure account access.",
            "Create savings goals and track your progress towards the things that matter.",
            "Use spending trends to understand and plan for your future expenses.",
            "Get helpful suggestions to manage your spending and save more.",
            "Start managing your expenses and subscriptions with confidence."
    };

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);

        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull OnboardingViewHolder holder, int position) {

        holder.illustration.setImageResource(illustrations[position]);
        holder.illustration.setContentDescription(titles[position]);

        holder.caption.setText(captions[position]);
        holder.title.setText(titles[position]);
        holder.description.setText(descriptions[position]);
    }

    @Override
    public int getItemCount() {
        return illustrations.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {

        final ImageView illustration;
        final TextView caption;
        final TextView title;
        final TextView description;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);

            illustration = itemView.findViewById(
                    R.id.ivOnboardingIllustration);

            caption = itemView.findViewById(
                    R.id.tvIllustrationCaption);

            title = itemView.findViewById(
                    R.id.tvOnboardingHeading);

            description = itemView.findViewById(
                    R.id.tvOnboardingDescription);
        }
    }
}
