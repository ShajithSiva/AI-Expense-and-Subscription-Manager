package com.example.aiexpensemanagementapplication.data.remote;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;

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
                "PRIMARY"
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

        if (invitedBy == null ||
                invitedBy.trim().isEmpty()) {

            callback.onFailure(
                    "Inviting user ID is missing."
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

        final String finalInvitedBy =
                invitedBy.trim();

        DocumentReference familyRef =
                firestore
                        .collection(FAMILY_COLLECTION)
                        .document(familyId);

        // -------------------------------------------------
        // VERIFY FAMILY HEAD / OWNER
        // -------------------------------------------------

        familyRef
                .get()
                .addOnSuccessListener(familySnapshot -> {

                    if (!familySnapshot.exists()) {

                        callback.onFailure(
                                "Family not found."
                        );

                        return;
                    }

                    String ownerUid =
                            familySnapshot.getString(
                                    "ownerUid"
                            );

                    if (ownerUid == null ||
                            !ownerUid.equals(finalInvitedBy)) {

                        callback.onFailure(
                                "Only the Family Head can invite family members."
                        );

                        return;
                    }

                    // -----------------------------------------
                    // CHECK DUPLICATE PENDING INVITATION
                    // -----------------------------------------

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
                                        finalInvitedBy,
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
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to verify Family Head permission."
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

                            String familyId =
                                    invitationSnapshot.getString("familyId");

                            if (familyId == null ||
                                    familyId.trim().isEmpty() ||
                                    familyId.equalsIgnoreCase("null")) {

                                callback.onFailure(
                                        "This invitation does not contain a valid Firestore family ID."
                                );

                                return;
                            }

                            familyId = familyId.trim();

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
    // FAMILY MEMBER DATA
    // =====================================================

    public static class FamilyMemberData {

        private final String uid;
        private final String name;
        private final String role;

        public FamilyMemberData(
                String uid,
                String name,
                String role
        ) {

            this.uid = uid;
            this.name = name;
            this.role = role;
        }

        public String getUid() {
            return uid;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }
    }


    // =====================================================
    // FAMILY MEMBERS CALLBACK
    // =====================================================

    public interface FamilyMembersCallback {

        void onSuccess(
                List<FamilyMemberData> members
        );

        void onFailure(
                String message
        );
    }


    // =====================================================
    // GET FAMILY MEMBERS
    // =====================================================

    public void getFamilyMembers(
            String familyId,
            FamilyMembersCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        callback.onFailure(
                                "Family not found."
                        );

                        return;
                    }

                    Object membersObject =
                            snapshot.get("members");

                    List<FamilyMemberData> members =
                            new ArrayList<>();

                    if (membersObject instanceof Map) {

                        Map<?, ?> membersMap =
                                (Map<?, ?>) membersObject;

                        for (Object value :
                                membersMap.values()) {

                            if (!(value instanceof Map)) {
                                continue;
                            }

                            Map<?, ?> memberMap =
                                    (Map<?, ?>) value;

                            String uid =
                                    getStringValue(
                                            memberMap,
                                            "uid"
                                    );

                            String name =
                                    getStringValue(
                                            memberMap,
                                            "name"
                                    );

                            String role =
                                    getStringValue(
                                            memberMap,
                                            "role"
                                    );

                            members.add(
                                    new FamilyMemberData(
                                            uid,
                                            name,
                                            role
                                    )
                            );
                        }
                    }

                    callback.onSuccess(
                            members
                    );
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to load family members."
                    );
                });
    }


    // =====================================================
    // LEAVE FAMILY
    // NORMAL MEMBERS ONLY
    // FAMILY HEAD MUST TRANSFER OR DELETE FIRST
    // =====================================================

    public void leaveFamily(
            String familyId,
            String userUid,
            MemberManagementCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
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

        DocumentReference familyRef =
                firestore
                        .collection(FAMILY_COLLECTION)
                        .document(familyId);

        familyRef
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        callback.onFailure(
                                "Family not found."
                        );

                        return;
                    }

                    String ownerUid =
                            snapshot.getString(
                                    "ownerUid"
                            );

                    // Family Head cannot simply leave.
                    if (ownerUid != null &&
                            ownerUid.equals(userUid)) {

                        callback.onFailure(
                                "The Family Head cannot leave the family. Transfer the Family Head role or delete the family first."
                        );

                        return;
                    }

                    Object membersObject =
                            snapshot.get(
                                    "members"
                            );

                    if (!(membersObject instanceof Map)) {

                        callback.onFailure(
                                "Family member data is missing."
                        );

                        return;
                    }

                    Map<?, ?> membersMap =
                            (Map<?, ?>) membersObject;

                    if (!membersMap.containsKey(userUid)) {

                        callback.onFailure(
                                "You are not a member of this family."
                        );

                        return;
                    }

                    Map<String, Object> update =
                            new HashMap<>();

                    update.put(
                            "members." + userUid,
                            FieldValue.delete()
                    );

                    familyRef
                            .update(update)
                            .addOnSuccessListener(
                                    unused ->
                                            callback.onSuccess()
                            )
                            .addOnFailureListener(e -> {

                                callback.onFailure(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Failed to leave family."
                                );
                            });
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to check family membership."
                    );
                });
    }


    // =====================================================
    // TRANSFER FAMILY HEAD / PRIMARY ROLE
    // ONLY CURRENT FAMILY HEAD CAN TRANSFER
    // =====================================================

    public void transferFamilyHead(
            String familyId,
            String currentPrimaryUid,
            String newPrimaryUid,
            String newPrimaryName,
            MemberManagementCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        if (currentPrimaryUid == null ||
                currentPrimaryUid.trim().isEmpty()) {

            callback.onFailure(
                    "Current Family Head ID is missing."
            );

            return;
        }

        if (newPrimaryUid == null ||
                newPrimaryUid.trim().isEmpty()) {

            callback.onFailure(
                    "New Family Head ID is missing."
            );

            return;
        }

        if (currentPrimaryUid.equals(newPrimaryUid)) {

            callback.onFailure(
                    "This member is already the Family Head."
            );

            return;
        }

        final String finalNewPrimaryName =
                newPrimaryName == null
                        ? ""
                        : newPrimaryName.trim();

        DocumentReference familyRef =
                firestore
                        .collection(FAMILY_COLLECTION)
                        .document(familyId);

        firestore
                .runTransaction(transaction -> {

                    com.google.firebase.firestore.DocumentSnapshot snapshot =
                            transaction.get(familyRef);

                    if (!snapshot.exists()) {

                        throw new com.google.firebase.firestore.FirebaseFirestoreException(
                                "Family not found.",
                                com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND
                        );
                    }

                    String ownerUid =
                            snapshot.getString(
                                    "ownerUid"
                            );

                    if (ownerUid == null ||
                            !ownerUid.equals(currentPrimaryUid)) {

                        throw new com.google.firebase.firestore.FirebaseFirestoreException(
                                "Only the current Family Head can transfer family ownership.",
                                com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
                        );
                    }

                    Object membersObject =
                            snapshot.get(
                                    "members"
                            );

                    if (!(membersObject instanceof Map)) {

                        throw new com.google.firebase.firestore.FirebaseFirestoreException(
                                "Family member data is missing.",
                                com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND
                        );
                    }

                    Map<?, ?> membersMap =
                            (Map<?, ?>) membersObject;

                    if (!membersMap.containsKey(
                            currentPrimaryUid
                    )) {

                        throw new com.google.firebase.firestore.FirebaseFirestoreException(
                                "Current Family Head is not listed as a family member.",
                                com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND
                        );
                    }

                    if (!membersMap.containsKey(
                            newPrimaryUid
                    )) {

                        throw new com.google.firebase.firestore.FirebaseFirestoreException(
                                "Selected user is not a member of this family.",
                                com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND
                        );
                    }

                    Map<String, Object> updates =
                            new HashMap<>();

                    // Change top-level ownership.
                    updates.put(
                            "ownerUid",
                            newPrimaryUid
                    );

                    updates.put(
                            "ownerName",
                            finalNewPrimaryName
                    );

                    // Old head becomes a normal member.
                    updates.put(
                            "members."
                                    + currentPrimaryUid
                                    + ".role",
                            "MEMBER"
                    );

                    // Selected member becomes the new Family Head.
                    updates.put(
                            "members."
                                    + newPrimaryUid
                                    + ".role",
                            "PRIMARY"
                    );

                    transaction.update(
                            familyRef,
                            updates
                    );

                    return null;
                })
                .addOnSuccessListener(unused -> {

                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to transfer Family Head role."
                    );
                });
    }


    // =====================================================
    // REMOVE FAMILY MEMBER
    // ONLY FAMILY HEAD / PRIMARY CAN REMOVE
    // =====================================================

    public void removeFamilyMember(
            String familyId,
            String requestingUid,
            String targetUid,
            MemberManagementCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        if (requestingUid == null ||
                requestingUid.trim().isEmpty()) {

            callback.onFailure(
                    "Current user is missing."
            );

            return;
        }

        if (targetUid == null ||
                targetUid.trim().isEmpty()) {

            callback.onFailure(
                    "Member ID is missing."
            );

            return;
        }

        DocumentReference familyRef =
                firestore
                        .collection(FAMILY_COLLECTION)
                        .document(familyId);

        familyRef
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        callback.onFailure(
                                "Family not found."
                        );

                        return;
                    }

                    String ownerUid =
                            snapshot.getString(
                                    "ownerUid"
                            );

                    if (ownerUid == null ||
                            !ownerUid.equals(requestingUid)) {

                        callback.onFailure(
                                "Only the Family Head can remove family members."
                        );

                        return;
                    }

                    if (requestingUid.equals(targetUid)) {

                        callback.onFailure(
                                "The Family Head cannot remove themselves."
                        );

                        return;
                    }

                    Map<String, Object> update =
                            new HashMap<>();

                    update.put(
                            "members." + targetUid,
                            FieldValue.delete()
                    );

                    familyRef
                            .update(update)
                            .addOnSuccessListener(
                                    unused ->
                                            callback.onSuccess()
                            )
                            .addOnFailureListener(e -> {

                                String message =
                                        e.getMessage();

                                callback.onFailure(
                                        message != null
                                                ? message
                                                : "Failed to remove member."
                                );
                            });
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to check family permissions."
                    );
                });
    }


    // =====================================================
    // DELETE FAMILY
    // ONLY FAMILY HEAD / PRIMARY CAN DELETE
    // =====================================================

    public void deleteFamily(
            String familyId,
            String requestingUid,
            MemberManagementCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        if (requestingUid == null ||
                requestingUid.trim().isEmpty()) {

            callback.onFailure(
                    "Current user is missing."
            );

            return;
        }

        DocumentReference familyRef =
                firestore
                        .collection(FAMILY_COLLECTION)
                        .document(familyId);

        familyRef
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        callback.onFailure(
                                "Family not found."
                        );

                        return;
                    }

                    String ownerUid =
                            snapshot.getString(
                                    "ownerUid"
                            );

                    if (ownerUid == null ||
                            !ownerUid.equals(requestingUid)) {

                        callback.onFailure(
                                "Only the Family Head can delete this family."
                        );

                        return;
                    }

                    // -----------------------------------------
                    // CLEAN DELETE ORDER
                    // 1. transactions
                    // 2. budget
                    // 3. invitations
                    // 4. family document
                    // -----------------------------------------

                    deleteQueryInBatches(
                            familyRef
                                    .collection("transactions")
                                    .limit(400),
                            new InternalDeleteCallback() {

                                @Override
                                public void onSuccess() {

                                    deleteQueryInBatches(
                                            familyRef
                                                    .collection("budget")
                                                    .limit(400),
                                            new InternalDeleteCallback() {

                                                @Override
                                                public void onSuccess() {

                                                    deleteQueryInBatches(
                                                            firestore
                                                                    .collection(INVITATION_COLLECTION)
                                                                    .whereEqualTo(
                                                                            "familyId",
                                                                            familyId
                                                                    )
                                                                    .limit(400),
                                                            new InternalDeleteCallback() {

                                                                @Override
                                                                public void onSuccess() {

                                                                    familyRef
                                                                            .delete()
                                                                            .addOnSuccessListener(
                                                                                    unused ->
                                                                                            callback.onSuccess()
                                                                            )
                                                                            .addOnFailureListener(e -> {

                                                                                callback.onFailure(
                                                                                        e.getMessage() != null
                                                                                                ? e.getMessage()
                                                                                                : "Family child data was removed, but the family document could not be deleted."
                                                                                );
                                                                            });
                                                                }

                                                                @Override
                                                                public void onFailure(
                                                                        String message
                                                                ) {

                                                                    callback.onFailure(
                                                                            message
                                                                    );
                                                                }
                                                            }
                                                    );
                                                }

                                                @Override
                                                public void onFailure(
                                                        String message
                                                ) {

                                                    callback.onFailure(
                                                            message
                                                    );
                                                }
                                            }
                                    );
                                }

                                @Override
                                public void onFailure(
                                        String message
                                ) {

                                    callback.onFailure(
                                            message
                                    );
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to check family permissions."
                    );
                });
    }


    // =====================================================
    // DELETE QUERY DOCUMENTS IN SAFE BATCHES
    // =====================================================

    private void deleteQueryInBatches(
            com.google.firebase.firestore.Query query,
            InternalDeleteCallback callback
    ) {

        query
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {

                        callback.onSuccess();
                        return;
                    }

                    com.google.firebase.firestore.WriteBatch batch =
                            firestore.batch();

                    for (
                            com.google.firebase.firestore.DocumentSnapshot document
                            : querySnapshot.getDocuments()
                    ) {

                        batch.delete(
                                document.getReference()
                        );
                    }

                    batch
                            .commit()
                            .addOnSuccessListener(unused -> {

                                // Run again until no documents remain.
                                deleteQueryInBatches(
                                        query,
                                        callback
                                );
                            })
                            .addOnFailureListener(e -> {

                                callback.onFailure(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Failed to delete family-related data."
                                );
                            });
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to load family-related data for deletion."
                    );
                });
    }


    // =====================================================
    // INTERNAL DELETE CALLBACK
    // =====================================================

    private interface InternalDeleteCallback {

        void onSuccess();

        void onFailure(
                String message
        );
    }


    // =====================================================
    // GET STRING FROM MAP
    // =====================================================

    private String getStringValue(
            Map<?, ?> map,
            String key
    ) {

        Object value =
                map.get(key);

        return value == null
                ? ""
                : String.valueOf(value);
    }



    // =====================================================
    // ADD FAMILY EXPENSE
    // =====================================================

    public void addFamilyExpense(
            String familyId,
            String transactionId,
            String ownerUid,
            String ownerName,
            double amount,
            String category,
            int categoryId,
            String paymentMethod,
            int paymentMethodId,
            String date,
            String note,
            FamilyTransactionCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        if (ownerUid == null ||
                ownerUid.trim().isEmpty()) {

            callback.onFailure(
                    "Owner UID is missing."
            );

            return;
        }

        Map<String, Object> transaction =
                new HashMap<>();

        transaction.put(
                "transactionId",
                transactionId
        );

        transaction.put(
                "ownerUid",
                ownerUid
        );

        transaction.put(
                "ownerName",
                ownerName == null
                        ? ""
                        : ownerName
        );

        transaction.put(
                "type",
                "EXPENSE"
        );

        transaction.put(
                "amount",
                amount
        );

        transaction.put(
                "category",
                category == null
                        ? ""
                        : category
        );

        transaction.put(
                "categoryId",
                categoryId
        );

        transaction.put(
                "paymentMethod",
                paymentMethod == null
                        ? ""
                        : paymentMethod
        );

        transaction.put(
                "paymentMethodId",
                paymentMethodId
        );

        transaction.put(
                "date",
                date == null
                        ? ""
                        : date
        );

        transaction.put(
                "note",
                note == null
                        ? ""
                        : note
        );

        transaction.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );


        firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .collection("transactions")
                .document(transactionId)
                .set(transaction)
                .addOnSuccessListener(unused -> {

                    callback.onSuccess();

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to save family expense."
                    );
                });
    }

    // =====================================================
    // GET FAMILY EXPENSES
    // =====================================================

    public void getFamilyExpenses(
            String familyId,
            FamilyExpensesCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .collection("transactions")
                .whereEqualTo(
                        "type",
                        "EXPENSE"
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ArrayList<FamilyExpenseData> expenses =
                            new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String transactionId =
                                document.getString(
                                        "transactionId"
                                );

                        String ownerUid =
                                document.getString(
                                        "ownerUid"
                                );

                        String ownerName =
                                document.getString(
                                        "ownerName"
                                );

                        String category =
                                document.getString(
                                        "category"
                                );

                        String paymentMethod =
                                document.getString(
                                        "paymentMethod"
                                );

                        String date =
                                document.getString(
                                        "date"
                                );

                        String note =
                                document.getString(
                                        "note"
                                );

                        Double amount =
                                document.getDouble(
                                        "amount"
                                );

                        Long categoryIdValue =
                                document.getLong(
                                        "categoryId"
                                );

                        Long paymentMethodIdValue =
                                document.getLong(
                                        "paymentMethodId"
                                );


                        double finalAmount =
                                amount == null
                                        ? 0.0
                                        : amount;


                        int categoryId =
                                categoryIdValue == null
                                        ? -1
                                        : categoryIdValue.intValue();


                        int paymentMethodId =
                                paymentMethodIdValue == null
                                        ? -1
                                        : paymentMethodIdValue.intValue();


                        expenses.add(
                                new FamilyExpenseData(

                                        transactionId,

                                        ownerUid,

                                        ownerName,

                                        category,

                                        categoryId,

                                        paymentMethod,

                                        paymentMethodId,

                                        finalAmount,

                                        date,

                                        note
                                )
                        );
                    }

                    callback.onSuccess(
                            expenses
                    );

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to load family expenses."
                    );
                });
    }

    // =====================================================
    // GET FAMILY INCOME
    // =====================================================

    public void getFamilyIncome(
            String familyId,
            FamilyIncomeListCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .collection("transactions")
                .whereEqualTo(
                        "type",
                        "INCOME"
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ArrayList<FamilyIncomeData> incomes =
                            new ArrayList<>();

                    for (
                            com.google.firebase.firestore.DocumentSnapshot document
                            : querySnapshot.getDocuments()
                    ) {

                        String transactionId =
                                document.getString(
                                        "transactionId"
                                );

                        String ownerUid =
                                document.getString(
                                        "ownerUid"
                                );

                        String ownerName =
                                document.getString(
                                        "ownerName"
                                );

                        String category =
                                document.getString(
                                        "category"
                                );

                        String incomeSource =
                                document.getString(
                                        "incomeSource"
                                );

                        String date =
                                document.getString(
                                        "date"
                                );

                        String note =
                                document.getString(
                                        "note"
                                );

                        Double amount =
                                document.getDouble(
                                        "amount"
                                );

                        Long categoryIdValue =
                                document.getLong(
                                        "categoryId"
                                );

                        Long incomeSourceIdValue =
                                document.getLong(
                                        "incomeSourceId"
                                );

                        double finalAmount =
                                amount == null
                                        ? 0.0
                                        : amount;

                        int categoryId =
                                categoryIdValue == null
                                        ? -1
                                        : categoryIdValue.intValue();

                        int incomeSourceId =
                                incomeSourceIdValue == null
                                        ? -1
                                        : incomeSourceIdValue.intValue();

                        incomes.add(
                                new FamilyIncomeData(

                                        transactionId,

                                        ownerUid,

                                        ownerName,

                                        category,

                                        categoryId,

                                        incomeSource,

                                        incomeSourceId,

                                        finalAmount,

                                        date,

                                        note
                                )
                        );
                    }

                    callback.onSuccess(
                            incomes
                    );

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to load family income."
                    );
                });
    }

    // =====================================================
    // FAMILY EXPENSE DATA
    // =====================================================

    public static class FamilyExpenseData {

        private final String transactionId;
        private final String ownerUid;
        private final String ownerName;

        private final String category;
        private final int categoryId;

        private final String paymentMethod;
        private final int paymentMethodId;

        private final double amount;

        private final String date;
        private final String note;


        public FamilyExpenseData(
                String transactionId,
                String ownerUid,
                String ownerName,
                String category,
                int categoryId,
                String paymentMethod,
                int paymentMethodId,
                double amount,
                String date,
                String note
        ) {

            this.transactionId = transactionId;
            this.ownerUid = ownerUid;
            this.ownerName = ownerName;
            this.category = category;
            this.categoryId = categoryId;
            this.paymentMethod = paymentMethod;
            this.paymentMethodId = paymentMethodId;
            this.amount = amount;
            this.date = date;
            this.note = note;
        }


        public String getTransactionId() {
            return transactionId;
        }


        public String getOwnerUid() {
            return ownerUid;
        }


        public String getOwnerName() {
            return ownerName;
        }


        public String getCategory() {
            return category;
        }


        public int getCategoryId() {
            return categoryId;
        }


        public String getPaymentMethod() {
            return paymentMethod;
        }


        public int getPaymentMethodId() {
            return paymentMethodId;
        }


        public double getAmount() {
            return amount;
        }


        public String getDate() {
            return date;
        }


        public String getNote() {
            return note;
        }
    }

    // =====================================================
    // FAMILY INCOME DATA
    // =====================================================

    public static class FamilyIncomeData {

        private final String transactionId;
        private final String ownerUid;
        private final String ownerName;

        private final String category;
        private final int categoryId;

        private final String incomeSource;
        private final int incomeSourceId;

        private final double amount;

        private final String date;
        private final String note;


        public FamilyIncomeData(
                String transactionId,
                String ownerUid,
                String ownerName,
                String category,
                int categoryId,
                String incomeSource,
                int incomeSourceId,
                double amount,
                String date,
                String note
        ) {

            this.transactionId = transactionId;
            this.ownerUid = ownerUid;
            this.ownerName = ownerName;
            this.category = category;
            this.categoryId = categoryId;
            this.incomeSource = incomeSource;
            this.incomeSourceId = incomeSourceId;
            this.amount = amount;
            this.date = date;
            this.note = note;
        }


        public String getTransactionId() {
            return transactionId;
        }

        public String getOwnerUid() {
            return ownerUid;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getCategory() {
            return category;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public String getIncomeSource() {
            return incomeSource;
        }

        public int getIncomeSourceId() {
            return incomeSourceId;
        }

        public double getAmount() {
            return amount;
        }

        public String getDate() {
            return date;
        }

        public String getNote() {
            return note;
        }
    }

    // =====================================================
    // FAMILY SUBSCRIPTION DATA
    // =====================================================

    public static class FamilySubscriptionData {

        private final String transactionId;
        private final String ownerUid;
        private final String ownerName;

        private final String serviceName;
        private final double amount;

        private final String billingCycle;
        private final String nextBillingDate;

        public FamilySubscriptionData(
                String transactionId,
                String ownerUid,
                String ownerName,
                String serviceName,
                double amount,
                String billingCycle,
                String nextBillingDate
        ) {

            this.transactionId = transactionId;
            this.ownerUid = ownerUid;
            this.ownerName = ownerName;
            this.serviceName = serviceName;
            this.amount = amount;
            this.billingCycle = billingCycle;
            this.nextBillingDate = nextBillingDate;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getOwnerUid() {
            return ownerUid;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public double getAmount() {
            return amount;
        }

        public String getBillingCycle() {
            return billingCycle;
        }

        public String getNextBillingDate() {
            return nextBillingDate;
        }
    }

    // =====================================================
    // SAVE FAMILY BUDGET
    // =====================================================

    public void saveFamilyBudget(
            String familyId,
            double limitAmount,
            String startDate,
            String endDate,
            FamilyBudgetCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        FirebaseUser firebaseUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (firebaseUser == null) {

            callback.onFailure(
                    "User session not found. Please login again."
            );

            return;
        }

        String requestingUid =
                firebaseUser.getUid();

        DocumentReference familyRef =
                firestore
                        .collection(FAMILY_COLLECTION)
                        .document(familyId);

        // -------------------------------------------------
        // VERIFY FAMILY HEAD BEFORE WRITING BUDGET
        // -------------------------------------------------

        familyRef
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        callback.onFailure(
                                "Family not found."
                        );

                        return;
                    }

                    String ownerUid =
                            snapshot.getString(
                                    "ownerUid"
                            );

                    if (ownerUid == null ||
                            !ownerUid.equals(requestingUid)) {

                        callback.onFailure(
                                "Only the Family Head can manage the family budget."
                        );

                        return;
                    }

                    Map<String, Object> budget =
                            new HashMap<>();

                    budget.put(
                            "familyId",
                            familyId
                    );

                    budget.put(
                            "limitAmount",
                            limitAmount
                    );

                    budget.put(
                            "startDate",
                            startDate == null
                                    ? ""
                                    : startDate
                    );

                    budget.put(
                            "endDate",
                            endDate == null
                                    ? ""
                                    : endDate
                    );

                    budget.put(
                            "updatedBy",
                            requestingUid
                    );

                    budget.put(
                            "updatedAt",
                            FieldValue.serverTimestamp()
                    );

                    familyRef
                            .collection("budget")
                            .document("current")
                            .set(budget)
                            .addOnSuccessListener(unused -> {

                                callback.onSuccess(
                                        new FamilyBudgetData(
                                                limitAmount,
                                                startDate,
                                                endDate
                                        )
                                );
                            })
                            .addOnFailureListener(e -> {

                                callback.onFailure(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Failed to save family budget."
                                );
                            });
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to verify Family Head permission."
                    );
                });
    }

    // =====================================================
    // ADD FAMILY INCOME
    // =====================================================

    public void addFamilyIncome(
            String familyId,
            String transactionId,
            String ownerUid,
            String ownerName,
            double amount,
            String category,
            int categoryId,
            String incomeSource,
            int incomeSourceId,
            String date,
            String note,
            FamilyIncomeCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        if (ownerUid == null ||
                ownerUid.trim().isEmpty()) {

            callback.onFailure(
                    "Owner UID is missing."
            );

            return;
        }

        Map<String, Object> transaction =
                new HashMap<>();

        transaction.put(
                "transactionId",
                transactionId
        );

        transaction.put(
                "ownerUid",
                ownerUid
        );

        transaction.put(
                "ownerName",
                ownerName == null
                        ? ""
                        : ownerName
        );

        transaction.put(
                "type",
                "INCOME"
        );

        transaction.put(
                "amount",
                amount
        );

        transaction.put(
                "category",
                category == null
                        ? ""
                        : category
        );

        transaction.put(
                "categoryId",
                categoryId
        );

        transaction.put(
                "incomeSource",
                incomeSource == null
                        ? ""
                        : incomeSource
        );

        transaction.put(
                "incomeSourceId",
                incomeSourceId
        );

        transaction.put(
                "date",
                date == null
                        ? ""
                        : date
        );

        transaction.put(
                "note",
                note == null
                        ? ""
                        : note
        );

        transaction.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .collection("transactions")
                .document(transactionId)
                .set(transaction)
                .addOnSuccessListener(unused -> {

                    callback.onSuccess();

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to save family income."
                    );
                });
    }

    // =====================================================
    // GET FAMILY BUDGET
    // =====================================================

    public void getFamilyBudget(
            String familyId,
            FamilyBudgetCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .collection("budget")
                .document("current")
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        callback.onSuccess(
                                null
                        );

                        return;
                    }

                    Double limitAmount =
                            document.getDouble(
                                    "limitAmount"
                            );

                    String startDate =
                            document.getString(
                                    "startDate"
                            );

                    String endDate =
                            document.getString(
                                    "endDate"
                            );

                    double finalLimit =
                            limitAmount == null
                                    ? 0.0
                                    : limitAmount;

                    callback.onSuccess(
                            new FamilyBudgetData(
                                    finalLimit,
                                    startDate,
                                    endDate
                            )
                    );

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to load family budget."
                    );
                });
    }

    // =====================================================
    // FAMILY BUDGET DATA
    // =====================================================

    public static class FamilyBudgetData {

        private final double limitAmount;
        private final String startDate;
        private final String endDate;

        public FamilyBudgetData(
                double limitAmount,
                String startDate,
                String endDate
        ) {

            this.limitAmount = limitAmount;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public double getLimitAmount() {
            return limitAmount;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }

    // =====================================================
    // ADD FAMILY SUBSCRIPTION
    // =====================================================

    public void addFamilySubscription(
            String familyId,
            String subscriptionId,
            String ownerUid,
            String ownerName,
            String serviceName,
            double amount,
            String billingCycle,
            String nextBillingDate,
            FamilySubscriptionCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        if (ownerUid == null ||
                ownerUid.trim().isEmpty()) {

            callback.onFailure(
                    "Owner UID is missing."
            );

            return;
        }

        Map<String, Object> subscription =
                new HashMap<>();

        subscription.put(
                "transactionId",
                subscriptionId
        );

        subscription.put(
                "ownerUid",
                ownerUid
        );

        subscription.put(
                "ownerName",
                ownerName == null
                        ? ""
                        : ownerName
        );

        subscription.put(
                "type",
                "SUBSCRIPTION"
        );

        subscription.put(
                "serviceName",
                serviceName == null
                        ? ""
                        : serviceName
        );

        subscription.put(
                "amount",
                amount
        );

        subscription.put(
                "billingCycle",
                billingCycle == null
                        ? ""
                        : billingCycle
        );

        subscription.put(
                "nextBillingDate",
                nextBillingDate == null
                        ? ""
                        : nextBillingDate
        );

        subscription.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .collection("transactions")
                .document(subscriptionId)
                .set(subscription)
                .addOnSuccessListener(unused -> {

                    callback.onSuccess();

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to save family subscription."
                    );
                });
    }

    // =====================================================
    // GET FAMILY SUBSCRIPTIONS
    // =====================================================

    public void getFamilySubscriptions(
            String familyId,
            FamilySubscriptionsCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return;
        }

        firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .collection("transactions")
                .whereEqualTo(
                        "type",
                        "SUBSCRIPTION"
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ArrayList<FamilySubscriptionData>
                            subscriptions =
                            new ArrayList<>();

                    for (
                            com.google.firebase.firestore.DocumentSnapshot document
                            : querySnapshot.getDocuments()
                    ) {

                        String transactionId =
                                document.getString(
                                        "transactionId"
                                );

                        String ownerUid =
                                document.getString(
                                        "ownerUid"
                                );

                        String ownerName =
                                document.getString(
                                        "ownerName"
                                );

                        String serviceName =
                                document.getString(
                                        "serviceName"
                                );

                        Double amount =
                                document.getDouble(
                                        "amount"
                                );

                        String billingCycle =
                                document.getString(
                                        "billingCycle"
                                );

                        String nextBillingDate =
                                document.getString(
                                        "nextBillingDate"
                                );

                        double finalAmount =
                                amount == null
                                        ? 0.0
                                        : amount;

                        subscriptions.add(
                                new FamilySubscriptionData(

                                        transactionId,

                                        ownerUid,

                                        ownerName,

                                        serviceName,

                                        finalAmount,

                                        billingCycle,

                                        nextBillingDate
                                )
                        );
                    }

                    callback.onSuccess(
                            subscriptions
                    );

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to load family subscriptions."
                    );
                });
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

    // =====================================================
    // FAMILY TRANSACTION CALLBACK
    // =====================================================

    public interface FamilyTransactionCallback {

        void onSuccess();

        void onFailure(
                String message
        );
    }

    // =====================================================
    // FAMILY EXPENSES CALLBACK
    // =====================================================

    public interface FamilyExpensesCallback {

        void onSuccess(
                ArrayList<FamilyExpenseData> expenses
        );

        void onFailure(
                String message
        );
    }


    // =====================================================
    // GET FAMILY INCOME CALLBACK
    // =====================================================

    public interface FamilyIncomeListCallback {

        void onSuccess(
                ArrayList<FamilyIncomeData> incomes
        );

        void onFailure(
                String message
        );
    }

    // =====================================================
    // FAMILY BUDGET CALLBACK
    // =====================================================

    public interface FamilyBudgetCallback {

        void onSuccess(
                FamilyBudgetData budget
        );

        void onFailure(
                String message
        );
    }

    // =====================================================
    // FAMILY INCOME CALLBACK
    // =====================================================

    public interface FamilyIncomeCallback {

        void onSuccess();

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

    // =====================================================
    // FAMILY SUBSCRIPTIONS CALLBACK
    // =====================================================

    public interface FamilySubscriptionsCallback {

        void onSuccess(
                ArrayList<FamilySubscriptionData> subscriptions
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

    // =====================================================
    // FAMILY SUBSCRIPTION SAVE CALLBACK
    // =====================================================

    public interface FamilySubscriptionCallback {

        void onSuccess();

        void onFailure(
                String message
        );
    }

    public interface FamilyMembershipCallback {

        void onActive();

        void onRemoved();

        void onFailure(String message);
    }

    public ListenerRegistration listenToFamilyMembership(
            String familyId,
            String userUid,
            FamilyMembershipCallback callback
    ) {

        if (familyId == null ||
                familyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family ID is missing."
            );

            return null;
        }

        if (userUid == null ||
                userUid.trim().isEmpty()) {

            callback.onFailure(
                    "User ID is missing."
            );

            return null;
        }

        return firestore
                .collection(FAMILY_COLLECTION)
                .document(familyId)
                .addSnapshotListener(
                        (snapshot, error) -> {

                            // -----------------------------------------
                            // FIRESTORE ERROR
                            // -----------------------------------------

                            if (error != null) {

                                callback.onFailure(
                                        error.getMessage() != null
                                                ? error.getMessage()
                                                : "Membership check failed."
                                );

                                return;
                            }

                            // -----------------------------------------
                            // FAMILY DOCUMENT DELETED
                            // -----------------------------------------

                            if (snapshot == null ||
                                    !snapshot.exists()) {

                                callback.onRemoved();

                                return;
                            }

                            // -----------------------------------------
                            // GET MEMBERS MAP
                            // -----------------------------------------

                            Object membersObject =
                                    snapshot.get("members");

                            if (!(membersObject instanceof Map)) {

                                callback.onRemoved();

                                return;
                            }

                            Map<?, ?> members =
                                    (Map<?, ?>) membersObject;

                            // -----------------------------------------
                            // CHECK CURRENT USER
                            // -----------------------------------------

                            Object currentMember =
                                    members.get(userUid);

                            if (currentMember == null) {

                                callback.onRemoved();

                                return;
                            }

                            // -----------------------------------------
                            // USER STILL ACTIVE
                            // -----------------------------------------

                            callback.onActive();
                        }
                );
    }

    // =====================================================
    // MEMBER MANAGEMENT CALLBACK
    // =====================================================

    public interface MemberManagementCallback {

        void onSuccess();

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
