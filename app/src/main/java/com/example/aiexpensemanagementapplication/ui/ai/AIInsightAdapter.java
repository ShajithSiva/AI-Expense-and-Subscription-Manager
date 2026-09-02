package com.example.aiexpensemanagementapplication.ui.ai;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;

import java.util.ArrayList;
import java.util.List;

public class AIInsightAdapter
        extends RecyclerView.Adapter<AIInsightAdapter.ViewHolder> {

    private final List<AIInsightResult> insights;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AIInsightAdapter(
            List<AIInsightResult> insights
    ) {

        if (insights != null) {

            this.insights =
                    new ArrayList<>(
                            insights
                    );

        } else {

            this.insights =
                    new ArrayList<>();
        }
    }


    // =====================================================
    // SET INSIGHTS
    // =====================================================

    public void setInsights(
            List<AIInsightResult> newInsights
    ) {

        insights.clear();


        if (newInsights != null) {

            insights.addAll(
                    newInsights
            );
        }


        notifyDataSetChanged();
    }


    // =====================================================
    // CREATE VIEW HOLDER
    // =====================================================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_ai_insight,
                        parent,
                        false
                );

        return new ViewHolder(
                view
        );
    }


    // =====================================================
    // BIND VIEW HOLDER
    // =====================================================

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        AIInsightResult insight =
                insights.get(
                        position
                );


        if (insight == null) {

            holder.tvTitle.setText(
                    ""
            );

            holder.tvMessage.setText(
                    ""
            );

            holder.tvType.setText(
                    "AI INSIGHT"
            );

            holder.tvIcon.setText(
                    "💡"
            );

            return;
        }


        // =================================================
        // TITLE
        // =================================================

        holder.tvTitle.setText(
                insight.getTitle()
        );


        // =================================================
        // MESSAGE
        // =================================================

        holder.tvMessage.setText(
                insight.getMessage()
        );


        // =================================================
        // SEVERITY
        // =================================================

        FinancialInsight.Severity severity =
                insight.getSeverity();


        if (severity == null) {

            severity =
                    FinancialInsight.Severity.LOW;
        }


        // =================================================
        // APPLY SEVERITY UI
        // =================================================

        applySeverity(
                holder,
                severity
        );
    }


    // =====================================================
    // ITEM COUNT
    // =====================================================

    @Override
    public int getItemCount() {

        return insights.size();
    }


    // =====================================================
    // APPLY SEVERITY
    // =====================================================

    private void applySeverity(
            ViewHolder holder,
            FinancialInsight.Severity severity
    ) {

        switch (severity) {

            // =================================================
            // CRITICAL
            // =================================================

            case CRITICAL:

                holder.tvType.setText(
                        "CRITICAL"
                );

                holder.tvType.setTextColor(
                        Color.parseColor(
                                "#D32F2F"
                        )
                );

                holder.tvIcon.setText(
                        "🚨"
                );

                holder.insightIconContainer
                        .setBackgroundColor(
                                Color.parseColor(
                                        "#FFEBEE"
                                )
                        );

                break;


            // =================================================
            // HIGH
            // =================================================

            case HIGH:

                holder.tvType.setText(
                        "HIGH"
                );

                holder.tvType.setTextColor(
                        Color.parseColor(
                                "#F57C00"
                        )
                );

                holder.tvIcon.setText(
                        "⚠️"
                );

                holder.insightIconContainer
                        .setBackgroundColor(
                                Color.parseColor(
                                        "#FFF3E0"
                                )
                        );

                break;


            // =================================================
            // MEDIUM
            // =================================================

            case MEDIUM:

                holder.tvType.setText(
                        "MEDIUM"
                );

                holder.tvType.setTextColor(
                        Color.parseColor(
                                "#F9A825"
                        )
                );

                holder.tvIcon.setText(
                        "⚠️"
                );

                holder.insightIconContainer
                        .setBackgroundColor(
                                Color.parseColor(
                                        "#FFFDE7"
                                )
                        );

                break;


            // =================================================
            // LOW
            // =================================================

            case LOW:

                holder.tvType.setText(
                        "LOW"
                );

                holder.tvType.setTextColor(
                        Color.parseColor(
                                "#1976D2"
                        )
                );

                holder.tvIcon.setText(
                        "💡"
                );

                holder.insightIconContainer
                        .setBackgroundColor(
                                Color.parseColor(
                                        "#E3F2FD"
                                )
                        );

                break;


            // =================================================
            // POSITIVE
            // =================================================

            case POSITIVE:

                holder.tvType.setText(
                        "POSITIVE"
                );

                holder.tvType.setTextColor(
                        Color.parseColor(
                                "#388E3C"
                        )
                );

                holder.tvIcon.setText(
                        "✅"
                );

                holder.insightIconContainer
                        .setBackgroundColor(
                                Color.parseColor(
                                        "#E8F5E9"
                                )
                        );

                break;


            // =================================================
            // DEFAULT
            // =================================================

            default:

                holder.tvType.setText(
                        "AI INSIGHT"
                );

                holder.tvType.setTextColor(
                        Color.parseColor(
                                "#1976D2"
                        )
                );

                holder.tvIcon.setText(
                        "💡"
                );

                holder.insightIconContainer
                        .setBackgroundColor(
                                Color.parseColor(
                                        "#E3F2FD"
                                )
                        );

                break;
        }
    }


    // =====================================================
    // VIEW HOLDER
    // =====================================================

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTitle;

        TextView tvType;

        TextView tvMessage;

        TextView tvIcon;

        FrameLayout insightIconContainer;


        public ViewHolder(
                @NonNull View itemView
        ) {

            super(
                    itemView
            );


            tvTitle =
                    itemView.findViewById(
                            R.id.tvInsightTitle
                    );


            tvType =
                    itemView.findViewById(
                            R.id.tvInsightType
                    );


            tvMessage =
                    itemView.findViewById(
                            R.id.tvInsightMessage
                    );


            tvIcon =
                    itemView.findViewById(
                            R.id.tvInsightIcon
                    );


            insightIconContainer =
                    itemView.findViewById(
                            R.id.insightIconContainer
                    );
        }
    }
}