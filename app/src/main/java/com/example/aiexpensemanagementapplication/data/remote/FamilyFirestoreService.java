package com.example.aiexpensemanagementapplication.data.remote;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FamilyFirestoreService {

    private final FirebaseFirestore firestore;

    private static final String FAMILY_COLLECTION =
            "families";

    private static final String INVITATION_COLLECTION =
            "familyInvitations";


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FamilyFirestoreService() {

        firestore =
                FirebaseFirestore.getInstance();
    }


    // =====================================================
    // CREATE FAMILY
    // =====================================================

    public void createFamily(
            String familyName,
            String creatorUid,
            String creatorName,
            CreateFamilyCallback callback
    ) {

        if (familyName == null ||
                familyName.trim().isEmpty()) {

            callback.onFailure(
                    "Family name is required."
            );

            return;
        }

        if (creatorUid == null ||
                creatorUid.trim().isEmpty()) {

            callback.onFailure(
                    "User ID is missing."
            );

            return;
        }

        if (creatorName == null) {
            creatorName = "";
        }

        final String finalCreatorName =
                creatorName.trim();

        DocumentReference familyRef =
                firestore
                        .collection(FAMILY_COLLECTION)
                        .document();

        String familyId =
                familyRef.getId();

        String inviteCode =
                generateInviteCode();

        // -------------------------------------------------
        // FAMILY DATA
        // -------------------------------------------------

        Map<String, Object> familyData =
                new HashMap<>();

        familyData.put(
                "familyId",
                familyId
        );

        familyData.put(
                "familyName",
                familyName.trim()
        );

        familyData.put(
                "ownerUid",
                creatorUid
        );

        familyData.put(
                "ownerName",
                finalCreatorName
        );

        familyData.put(
                "inviteCode",
                inviteCode
        );

        familyData.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        // -------------------------------------------------
        // CREATOR MEMBER
        // -------------------------------------------------

        Map<String, Object> creatorMember =
                new HashMap<>();

        creatorMember.put(
                "uid",
                creatorUid
        );

        creatorMember.put(
                "name",
                finalCreatorName
        );

        creatorMember.put(
                "role",
                "OWNER"
        );

        creatorMember.put(
                "joinedAt",
                FieldValue.serverTimestamp()
        );

        Map<String, Object> members =
                new HashMap<>();

        members.put(
                creatorUid,
                creatorMember
        );

        familyData.put(
                "members",
                members
        );

        // -------------------------------------------------
        // SAVE FAMILY
        // -------------------------------------------------

        familyRef
                .set(familyData)
                .addOnSuccessListener(unused -> {

                    callback.onSuccess(
                            familyId,
                            inviteCode
                    );
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to create family."
                    );
                });
    }


    // =====================================================
    // SEND FAMILY INVITATION
    // =====================================================

    public void sendInvitation(
            String familyId,
            String familyName,
            String invitedEmail,
            String invitedBy,
            String role,
            InvitationCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        if (invitedEmail == null ||
                invitedEmail.trim().isEmpty()) {

            callback.onFailure(
                    "Email address is required."
            );

            return;
        }

        if (role == null ||
                role.trim().isEmpty()) {

            role = "member";
        }

        final String finalEmail =
                invitedEmail
                        .trim()
                        .toLowerCase();

        final String finalRole =
                role
                        .trim()
                        .toLowerCase();

        firestore
                .collection(INVITATION_COLLECTION)
                .whereEqualTo(
                        "familyId",
                        familyId
                )
                .whereEqualTo(
                        "invitedEmail",
                        finalEmail
                )
                .whereEqualTo(
                        "status",
                        "pending"
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!querySnapshot.isEmpty()) {

                        callback.onFailure(
                                "A pending invitation already exists for this email."
                        );

                        return;
                    }

                    createInvitation(
                            familyId,
                            familyName,
                            finalEmail,
                            invitedBy,
                            finalRole,
                            callback
                    );
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to check existing invitations."
                    );
                });
    }


    // =====================================================
    // CREATE INVITATION
    // =====================================================

    private void createInvitation(
            String familyId,
            String familyName,
            String invitedEmail,
            String invitedBy,
            String role,
            InvitationCallback callback
    ) {

        DocumentReference invitationRef =
                firestore
                        .collection(
                                INVITATION_COLLECTION
                        )
                        .document();

        Map<String, Object> invitation =
                new HashMap<>();

        invitation.put(
                "familyId",
                familyId
        );

        invitation.put(
                "familyName",
                familyName
        );

        invitation.put(
                "invitedEmail",
                invitedEmail
        );

        invitation.put(
                "invitedBy",
                invitedBy
        );

        invitation.put(
                "role",
                role
        );

        invitation.put(
                "status",
                "pending"
        );

        invitation.put(
                "createdAt",
                System.currentTimeMillis()
        );

        invitationRef
                .set(invitation)
                .addOnSuccessListener(unused -> {

                    callback.onSuccess(
                            invitationRef.getId()
                    );
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to send invitation."
                    );
                });
    }


    // =====================================================
    // ACCEPT INVITATION
    // =====================================================

    public void acceptInvitation(
            String invitationId,
            String userUid,
            String userName,
            AcceptInvitationCallback callback
    ) {

        if (invitationId == null ||
                invitationId.trim().isEmpty()) {

            callback.onFailure(
                    "Invitation ID is missing."
            );

            return;
        }

        if (userUid == null ||
                userUid.trim().isEmpty()) {

            callback.onFailure(
                    "User ID is missing."
            );

            return;
        }

        DocumentReference invitationRef =
                firestore
                        .collection(
                                INVITATION_COLLECTION
                        )
                        .document(invitationId);

        invitationRef
                .get()
                .addOnSuccessListener(
                        invitationSnapshot -> {

                            if (!invitationSnapshot.exists()) {

                                callback.onFailure(
                                        "Invitation not found."
                                );

                                return;
                            }

                            String status =
                                    invitationSnapshot.getString(
                                            "status"
                                    );

                            if (status == null ||
                                    !status.equalsIgnoreCase(
                                            "pending"
                                    )) {

                                callback.onFailure(
                                        "This invitation is no longer pending."
                                );

                                return;
                            }

                            Object familyIdValue =
                                    invitationSnapshot.get("familyId");

                            String familyId;

                            if (familyIdValue instanceof Number) {

                                familyId =
                                        String.valueOf(
                                                ((Number) familyIdValue).longValue()
                                        );

                            } else {

                                familyId =
                                        String.valueOf(familyIdValue);
                            }

                            String familyName =
                                    invitationSnapshot.getString(
                                            "familyName"
                                    );

                            String role =
                                    invitationSnapshot.getString(
                                            "role"
                                    );

                            if (familyId == null ||
                                    familyId.trim().isEmpty()) {

                                callback.onFailure(
                                        "Family ID is missing."
                                );

                                return;
                            }

                            if (familyName == null ||
                                    familyName.trim().isEmpty()) {

                                familyName = "Family";
                            }

                            if (role == null ||
                                    role.trim().isEmpty()) {

                                role = "member";
                            }

                            addMemberToFamily(
                                    familyId,
                                    familyName,
                                    userUid,
                                    userName,
                                    role,
                                    invitationId,
                                    callback
                            );
                        }
                )
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to read invitation."
                    );
                });
    }


    // =====================================================
    // ADD MEMBER TO FAMILY
    // =====================================================

    private void addMemberToFamily(
            String familyId,
            String familyName,
            String userUid,
            String userName,
            String role,
            String invitationId,
            AcceptInvitationCallback callback
    ) {

        DocumentReference familyRef =
                firestore
                        .collection(FAMILY_COLLECTION)
                        .document(familyId);

        Map<String, Object> member =
                new HashMap<>();

        member.put(
                "uid",
                userUid
        );

        member.put(
                "name",
                userName == null
                        ? ""
                        : userName
        );

        member.put(
                "role",
                role.toUpperCase()
        );

        member.put(
                "joinedAt",
                FieldValue.serverTimestamp()
        );

        Map<String, Object> update =
                new HashMap<>();

        update.put(
                "members." + userUid,
                member
        );

        final String finalFamilyName =
                familyName;

        final String finalRole =
                role;

        familyRef
                .update(update)
                .addOnSuccessListener(unused -> {

                    Map<String, Object> invitationUpdate =
                            new HashMap<>();

                    invitationUpdate.put(
                            "status",
                            "accepted"
                    );

                    invitationUpdate.put(
                            "acceptedBy",
                            userUid
                    );

                    invitationUpdate.put(
                            "acceptedAt",
                            System.currentTimeMillis()
                    );

                    firestore
                            .collection(
                                    INVITATION_COLLECTION
                            )
                            .document(invitationId)
                            .update(invitationUpdate)
                            .addOnSuccessListener(
                                    unused2 -> {

                                        callback.onSuccess(
                                                familyId,
                                                finalFamilyName,
                                                finalRole
                                        );
                                    }
                            )
                            .addOnFailureListener(e ->
                                    callback.onFailure(
                                            e.getMessage() != null
                                                    ? e.getMessage()
                                                    : "Member added but invitation update failed."
                                    )
                            );
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to add member to family."
                    );
                });
    }


    // =====================================================
    // REJECT INVITATION
    // =====================================================

    public void rejectInvitation(
            String invitationId,
            String userUid,
            RejectInvitationCallback callback
    ) {

        if (invitationId == null ||
                invitationId.trim().isEmpty()) {

            callback.onFailure(
                    "Invitation ID is missing."
            );

            return;
        }

        Map<String, Object> update =
                new HashMap<>();

        update.put(
                "status",
                "rejected"
        );

        update.put(
                "rejectedBy",
                userUid
        );

        update.put(
                "rejectedAt",
                System.currentTimeMillis()
        );

        firestore
                .collection(INVITATION_COLLECTION)
                .document(invitationId)
                .update(update)
                .addOnSuccessListener(
                        unused ->
                                callback.onSuccess()
                )
                .addOnFailureListener(e ->
                        callback.onFailure(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Failed to reject invitation."
                        )
                );
    }


    // =====================================================
    // GENERATE INVITE CODE
    // =====================================================

    private String generateInviteCode() {

        String random =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 5)
                        .toUpperCase();

        return "FAM-" + random;
    }


    // =====================================================
    // CALLBACKS
    // =====================================================

    public interface CreateFamilyCallback {

        void onSuccess(
                String firestoreFamilyId,
                String inviteCode
        );

        void onFailure(
                String message
        );
    }


    public interface InvitationCallback {

        void onSuccess(
                String invitationId
        );

        void onFailure(
                String message
        );
    }


    public interface AcceptInvitationCallback {

        void onSuccess(
                String familyId,
                String familyName,
                String role
        );

        void onAlreadyAccepted();

        void onFailure(
                String message
        );
    }


    public interface RejectInvitationCallback {

        void onSuccess();

        void onFailure(
                String message
        );
    }
}