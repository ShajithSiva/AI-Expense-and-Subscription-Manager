package com.example.aiexpensemanagementapplication.ui.family;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aiexpensemanagementapplication.R;
import com.example.aiexpensemanagementapplication.model.FamilyInvitation;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class FamilyInvitationAdapter
        extends RecyclerView.Adapter<FamilyInvitationAdapter.ViewHolder> {

    // =====================================================
    // ACTION LISTENER
    // =====================================================

    public interface InvitationActionListener {

        void onAccept(FamilyInvitation invitation);

        void onReject(FamilyInvitation invitation);
    }

    // =====================================================
    // VARIABLES
    // =====================================================

    private final Context context;

    private final List<FamilyInvitation> invitationList;

    private final InvitationActionListener listener;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FamilyInvitationAdapter(
            Context context,
            List<FamilyInvitation> invitationList,
            InvitationActionListener listener
    ) {

        this.context = context;

        this.invitationList = invitationList;

        this.listener = listener;
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

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_family_invitation,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    // =====================================================
    // BIND DATA
    // =====================================================

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        FamilyInvitation invitation =
                invitationList.get(position);

        // =================================================
        // FAMILY NAME
        // =================================================

        String familyName =
                invitation.getFamilyName();

        if (familyName == null ||
                familyName.trim().isEmpty()) {

            familyName = "Family Group";
        }

        holder.tvFamilyName.setText(
                familyName
        );

        // =================================================
        // INVITATION MESSAGE
        // =================================================

        holder.tvInvitationMessage.setText(
                "You have been invited to join this family."
        );

        // =================================================
        // ROLE
        // =================================================

        String role =
                invitation.getRole();

        if (role != null &&
                role.equalsIgnoreCase("viewer")) {

            holder.tvRole.setText(
                    "Role: Viewer"
            );

        } else {

            holder.tvRole.setText(
                    "Role: Member"
            );
        }

        // =================================================
        // ACCEPT
        // =================================================

        holder.btnAccept.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onAccept(
                                invitation
                        );
                    }
                }
        );

        // =================================================
        // REJECT
        // =================================================

        holder.btnReject.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onReject(
                                invitation
                        );
                    }
                }
        );
    }

    // =====================================================
    // ITEM COUNT
    // =====================================================

    @Override
    public int getItemCount() {

        if (invitationList == null) {

            return 0;
        }

        return invitationList.size();
    }

    // =====================================================
    // VIEW HOLDER
    // =====================================================

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvFamilyName;

        TextView tvInvitationMessage;

        TextView tvRole;

        MaterialButton btnAccept;

        MaterialButton btnReject;

        ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvFamilyName =
                    itemView.findViewById(
                            R.id.tvFamilyName
                    );

            tvInvitationMessage =
                    itemView.findViewById(
                            R.id.tvInvitationMessage
                    );

            tvRole =
                    itemView.findViewById(
                            R.id.tvRole
                    );

            btnAccept =
                    itemView.findViewById(
                            R.id.btnAccept
                    );

            btnReject =
                    itemView.findViewById(
                            R.id.btnReject
                    );
        }
    }
}