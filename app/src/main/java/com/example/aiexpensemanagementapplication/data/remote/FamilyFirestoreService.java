package com.example.aiexpensemanagementapplication.data.remote;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class FamilyFirestoreService {

    // =====================================================
    // COLLECTION NAMES
    // =====================================================

    private static final String COLLECTION_FAMILIES =
            "families";

    private static final String COLLECTION_MEMBERS =
            "members";

    private static final String COLLECTION_USERS =
            "users";

    private static final String COLLECTION_USER_FAMILIES =
            "families";

    private static final String COLLECTION_INVITATIONS =
            "familyInvitations";

    // =====================================================
    // FIRESTORE
    // =====================================================

    private final FirebaseFirestore firestore;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FamilyFirestoreService() {

        firestore =
                FirebaseFirestore.getInstance();
    }

    // =====================================================
    // CREATE FAMILY CALLBACK
    // =====================================================

    public interface CreateFamilyCallback {

        void onSuccess(
                String firestoreFamilyId,
                String unusedInviteCode
        );

        void onFailure(String message);
    }

    // =====================================================
    // SEND INVITATION CALLBACK
    // =====================================================

    public interface SendInvitationCallback {

        void onSuccess(
                String invitationId,
                String invitationCode
        );

        void onUserNotFound();

        void onAlreadyMember();

        void onInvitationAlreadyPending();

        void onCannotInviteYourself();

        void onFailure(String message);
    }

    // =====================================================
    // LOAD INVITATIONS CALLBACK
    // =====================================================

    public interface PendingInvitationsCallback {

        void onSuccess(
                List<FamilyInvitation> invitations
        );

        void onFailure(String message);
    }

    // =====================================================
    // ACCEPT INVITATION CALLBACK
    // =====================================================

    public interface AcceptInvitationCallback {

        void onSuccess(
                String firestoreFamilyId,
                String familyName,
                String role
        );

        void onAlreadyAccepted();

        void onFailure(String message);
    }

    // =====================================================
    // DECLINE INVITATION CALLBACK
    // =====================================================

    public interface DeclineInvitationCallback {

        void onSuccess();

        void onFailure(String message);
    }

    // =====================================================
    // FAMILY INVITATION MODEL
    // =====================================================

    public static class FamilyInvitation {

        private String invitationId;
        private String invitationCode;

        private String familyId;
        private String familyName;

        private String invitedEmail;
        private String invitedUserUid;
        private String invitedUserName;

        private String invitedByUid;
        private String invitedByName;

        private String role;
        private String status;

        public FamilyInvitation() {
        }

        public String getInvitationId() {
            return invitationId;
        }

        public void setInvitationId(
                String invitationId
        ) {
            this.invitationId =
                    invitationId;
        }

        public String getInvitationCode() {
            return invitationCode;
        }

        public void setInvitationCode(
                String invitationCode
        ) {
            this.invitationCode =
                    invitationCode;
        }

        public String getFamilyId() {
            return familyId;
        }

        public void setFamilyId(
                String familyId
        ) {
            this.familyId =
                    familyId;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(
                String familyName
        ) {
            this.familyName =
                    familyName;
        }

        public String getInvitedEmail() {
            return invitedEmail;
        }

        public void setInvitedEmail(
                String invitedEmail
        ) {
            this.invitedEmail =
                    invitedEmail;
        }

        public String getInvitedUserUid() {
            return invitedUserUid;
        }

        public void setInvitedUserUid(
                String invitedUserUid
        ) {
            this.invitedUserUid =
                    invitedUserUid;
        }

        public String getInvitedUserName() {
            return invitedUserName;
        }

        public void setInvitedUserName(
                String invitedUserName
        ) {
            this.invitedUserName =
                    invitedUserName;
        }

        public String getInvitedByUid() {
            return invitedByUid;
        }

        public void setInvitedByUid(
                String invitedByUid
        ) {
            this.invitedByUid =
                    invitedByUid;
        }

        public String getInvitedByName() {
            return invitedByName;
        }

        public void setInvitedByName(
                String invitedByName
        ) {
            this.invitedByName =
                    invitedByName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(
                String role
        ) {
            this.role =
                    role;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(
                String status
        ) {
            this.status =
                    status;
        }
    }

    // =====================================================
    // CREATE FAMILY
    // =====================================================

    public void createFamily(
            String familyName,
            String firebaseUid,
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

        if (firebaseUid == null ||
                firebaseUid.trim().isEmpty()) {

            callback.onFailure(
                    "User account was not found."
            );

            return;
        }

        String cleanedFamilyName =
                familyName.trim();

        String safeCreatorName =
                creatorName == null
                        ? ""
                        : creatorName.trim();

        DocumentReference familyReference =
                firestore
                        .collection(
                                COLLECTION_FAMILIES
                        )
                        .document();

        String firestoreFamilyId =
                familyReference.getId();

        DocumentReference memberReference =
                familyReference
                        .collection(
                                COLLECTION_MEMBERS
                        )
                        .document(firebaseUid);

        DocumentReference userFamilyReference =
                firestore
                        .collection(
                                COLLECTION_USERS
                        )
                        .document(firebaseUid)
                        .collection(
                                COLLECTION_USER_FAMILIES
                        )
                        .document(firestoreFamilyId);

        Map<String, Object> familyData =
                new HashMap<>();

        familyData.put(
                "familyId",
                firestoreFamilyId
        );

        familyData.put(
                "familyName",
                cleanedFamilyName
        );

        familyData.put(
                "createdByUid",
                firebaseUid
        );

        familyData.put(
                "createdByName",
                safeCreatorName
        );

        familyData.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        Map<String, Object> memberData =
                new HashMap<>();

        memberData.put(
                "userUid",
                firebaseUid
        );

        memberData.put(
                "userName",
                safeCreatorName
        );

        memberData.put(
                "role",
                "PRIMARY"
        );

        memberData.put(
                "joinedAt",
                FieldValue.serverTimestamp()
        );

        Map<String, Object> userFamilyData =
                new HashMap<>();

        userFamilyData.put(
                "familyId",
                firestoreFamilyId
        );

        userFamilyData.put(
                "familyName",
                cleanedFamilyName
        );

        userFamilyData.put(
                "role",
                "PRIMARY"
        );

        userFamilyData.put(
                "joinedAt",
                FieldValue.serverTimestamp()
        );

        WriteBatch batch =
                firestore.batch();

        batch.set(
                familyReference,
                familyData
        );

        batch.set(
                memberReference,
                memberData
        );

        batch.set(
                userFamilyReference,
                userFamilyData
        );

        batch.commit()
                .addOnSuccessListener(unused -> {

                    /*
                     * Second parameter is kept only because
                     * CreateFamilyActivity currently expects it.
                     *
                     * Family creation no longer displays or uses
                     * a manual family invite code.
                     */

                    callback.onSuccess(
                            firestoreFamilyId,
                            ""
                    );
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // SEND EMAIL INVITATION
    // =====================================================

    public void sendFamilyInvitation(
            String firestoreFamilyId,
            String familyName,
            String invitedEmail,
            String invitedByUid,
            String invitedByName,
            String role,
            SendInvitationCallback callback
    ) {

        if (firestoreFamilyId == null ||
                firestoreFamilyId.trim().isEmpty()) {

            callback.onFailure(
                    "Family was not found."
            );

            return;
        }

        if (invitedEmail == null ||
                invitedEmail.trim().isEmpty()) {

            callback.onFailure(
                    "Member email is required."
            );

            return;
        }

        if (invitedByUid == null ||
                invitedByUid.trim().isEmpty()) {

            callback.onFailure(
                    "Current user account was not found."
            );

            return;
        }

        String cleanedEmail =
                invitedEmail
                        .trim()
                        .toLowerCase(Locale.ROOT);

        String safeFamilyName =
                familyName == null ||
                        familyName.trim().isEmpty()
                        ? "Family"
                        : familyName.trim();

        String safeInviterName =
                invitedByName == null
                        ? ""
                        : invitedByName.trim();

        String safeRole =
                "VIEWER".equalsIgnoreCase(role)
                        ? "VIEWER"
                        : "MEMBER";

        // -------------------------------------------------
        // FIND REGISTERED USER USING EMAIL
        // -------------------------------------------------

        firestore
                .collection(COLLECTION_USERS)
                .whereEqualTo(
                        "email",
                        cleanedEmail
                )
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {

                        callback.onUserNotFound();

                        return;
                    }

                    DocumentSnapshot userDocument =
                            querySnapshot
                                    .getDocuments()
                                    .get(0);

                    String invitedUserUid =
                            userDocument.getString(
                                    "uid"
                            );

                    if (invitedUserUid == null ||
                            invitedUserUid.trim().isEmpty()) {

                        invitedUserUid =
                                userDocument.getId();
                    }

                    String invitedUserName =
                            userDocument.getString(
                                    "fullName"
                            );

                    if (invitedUserUid.equals(
                            invitedByUid
                    )) {

                        callback.onCannotInviteYourself();

                        return;
                    }

                    checkExistingMembership(
                            firestoreFamilyId,
                            safeFamilyName,
                            cleanedEmail,
                            invitedUserUid,
                            invitedUserName,
                            invitedByUid,
                            safeInviterName,
                            safeRole,
                            callback
                    );
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // CHECK EXISTING MEMBERSHIP
    // =====================================================

    private void checkExistingMembership(
            String firestoreFamilyId,
            String familyName,
            String invitedEmail,
            String invitedUserUid,
            String invitedUserName,
            String invitedByUid,
            String invitedByName,
            String role,
            SendInvitationCallback callback
    ) {

        firestore
                .collection(COLLECTION_FAMILIES)
                .document(firestoreFamilyId)
                .collection(COLLECTION_MEMBERS)
                .document(invitedUserUid)
                .get()
                .addOnSuccessListener(memberDocument -> {

                    if (memberDocument.exists()) {

                        callback.onAlreadyMember();

                        return;
                    }

                    checkPendingInvitation(
                            firestoreFamilyId,
                            familyName,
                            invitedEmail,
                            invitedUserUid,
                            invitedUserName,
                            invitedByUid,
                            invitedByName,
                            role,
                            callback
                    );
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // CHECK DUPLICATE PENDING INVITATION
    // =====================================================

    private void checkPendingInvitation(
            String firestoreFamilyId,
            String familyName,
            String invitedEmail,
            String invitedUserUid,
            String invitedUserName,
            String invitedByUid,
            String invitedByName,
            String role,
            SendInvitationCallback callback
    ) {

        String invitationDocumentId =
                createInvitationDocumentId(
                        firestoreFamilyId,
                        invitedUserUid
                );

        DocumentReference invitationReference =
                firestore
                        .collection(
                                COLLECTION_INVITATIONS
                        )
                        .document(invitationDocumentId);

        invitationReference
                .get()
                .addOnSuccessListener(invitationDocument -> {

                    if (invitationDocument.exists()) {

                        String currentStatus =
                                invitationDocument.getString(
                                        "status"
                                );

                        if ("PENDING".equalsIgnoreCase(
                                currentStatus
                        )) {

                            callback.onInvitationAlreadyPending();

                            return;
                        }
                    }

                    createPendingInvitation(
                            invitationReference,
                            invitationDocumentId,
                            firestoreFamilyId,
                            familyName,
                            invitedEmail,
                            invitedUserUid,
                            invitedUserName,
                            invitedByUid,
                            invitedByName,
                            role,
                            callback
                    );
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // CREATE PENDING INVITATION
    // =====================================================

    private void createPendingInvitation(
            DocumentReference invitationReference,
            String invitationId,
            String firestoreFamilyId,
            String familyName,
            String invitedEmail,
            String invitedUserUid,
            String invitedUserName,
            String invitedByUid,
            String invitedByName,
            String role,
            SendInvitationCallback callback
    ) {

        String invitationCode =
                generateInvitationCode();

        Map<String, Object> invitationData =
                new HashMap<>();

        invitationData.put(
                "invitationId",
                invitationId
        );

        invitationData.put(
                "invitationCode",
                invitationCode
        );

        invitationData.put(
                "familyId",
                firestoreFamilyId
        );

        invitationData.put(
                "familyName",
                familyName
        );

        invitationData.put(
                "invitedEmail",
                invitedEmail
        );

        invitationData.put(
                "invitedUserUid",
                invitedUserUid
        );

        invitationData.put(
                "invitedUserName",
                invitedUserName == null
                        ? ""
                        : invitedUserName
        );

        invitationData.put(
                "invitedByUid",
                invitedByUid
        );

        invitationData.put(
                "invitedByName",
                invitedByName
        );

        invitationData.put(
                "role",
                role
        );

        invitationData.put(
                "status",
                "PENDING"
        );

        invitationData.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        invitationData.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        invitationReference
                .set(invitationData)
                .addOnSuccessListener(unused -> {

                    callback.onSuccess(
                            invitationId,
                            invitationCode
                    );
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // GET PENDING INVITATIONS
    // =====================================================

    public void getPendingInvitations(
            String firebaseUid,
            PendingInvitationsCallback callback
    ) {

        if (firebaseUid == null ||
                firebaseUid.trim().isEmpty()) {

            callback.onFailure(
                    "User account was not found."
            );

            return;
        }

        firestore
                .collection(
                        COLLECTION_INVITATIONS
                )
                .whereEqualTo(
                        "invitedUserUid",
                        firebaseUid
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<FamilyInvitation> invitations =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot document
                            : querySnapshot) {

                        String status =
                                document.getString(
                                        "status"
                                );

                        if (!"PENDING".equalsIgnoreCase(
                                status
                        )) {

                            continue;
                        }

                        FamilyInvitation invitation =
                                new FamilyInvitation();

                        invitation.setInvitationId(
                                document.getId()
                        );

                        invitation.setInvitationCode(
                                document.getString(
                                        "invitationCode"
                                )
                        );

                        invitation.setFamilyId(
                                document.getString(
                                        "familyId"
                                )
                        );

                        invitation.setFamilyName(
                                document.getString(
                                        "familyName"
                                )
                        );

                        invitation.setInvitedEmail(
                                document.getString(
                                        "invitedEmail"
                                )
                        );

                        invitation.setInvitedUserUid(
                                document.getString(
                                        "invitedUserUid"
                                )
                        );

                        invitation.setInvitedUserName(
                                document.getString(
                                        "invitedUserName"
                                )
                        );

                        invitation.setInvitedByUid(
                                document.getString(
                                        "invitedByUid"
                                )
                        );

                        invitation.setInvitedByName(
                                document.getString(
                                        "invitedByName"
                                )
                        );

                        invitation.setRole(
                                document.getString(
                                        "role"
                                )
                        );

                        invitation.setStatus(
                                status
                        );

                        invitations.add(invitation);
                    }

                    callback.onSuccess(
                            invitations
                    );
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // ACCEPT INVITATION
    // =====================================================

    public void acceptInvitation(
            FamilyInvitation invitation,
            String firebaseUid,
            String userName,
            AcceptInvitationCallback callback
    ) {

        if (invitation == null) {

            callback.onFailure(
                    "Invitation was not found."
            );

            return;
        }

        if (firebaseUid == null ||
                firebaseUid.trim().isEmpty()) {

            callback.onFailure(
                    "User account was not found."
            );

            return;
        }

        if (!firebaseUid.equals(
                invitation.getInvitedUserUid()
        )) {

            callback.onFailure(
                    "This invitation does not belong to your account."
            );

            return;
        }

        String invitationId =
                invitation.getInvitationId();

        String firestoreFamilyId =
                invitation.getFamilyId();

        String familyName =
                invitation.getFamilyName();

        String role =
                "VIEWER".equalsIgnoreCase(
                        invitation.getRole()
                )
                        ? "VIEWER"
                        : "MEMBER";

        DocumentReference invitationReference =
                firestore
                        .collection(
                                COLLECTION_INVITATIONS
                        )
                        .document(invitationId);

        invitationReference
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        callback.onFailure(
                                "Invitation no longer exists."
                        );

                        return;
                    }

                    String status =
                            document.getString(
                                    "status"
                            );

                    if (!"PENDING".equalsIgnoreCase(
                            status
                    )) {

                        callback.onAlreadyAccepted();

                        return;
                    }

                    addAcceptedMember(
                            invitationReference,
                            firestoreFamilyId,
                            familyName,
                            firebaseUid,
                            userName,
                            role,
                            callback
                    );
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // ADD ACCEPTED MEMBER
    // =====================================================

    private void addAcceptedMember(
            DocumentReference invitationReference,
            String firestoreFamilyId,
            String familyName,
            String firebaseUid,
            String userName,
            String role,
            AcceptInvitationCallback callback
    ) {

        DocumentReference familyReference =
                firestore
                        .collection(
                                COLLECTION_FAMILIES
                        )
                        .document(firestoreFamilyId);

        DocumentReference memberReference =
                familyReference
                        .collection(
                                COLLECTION_MEMBERS
                        )
                        .document(firebaseUid);

        DocumentReference userFamilyReference =
                firestore
                        .collection(
                                COLLECTION_USERS
                        )
                        .document(firebaseUid)
                        .collection(
                                COLLECTION_USER_FAMILIES
                        )
                        .document(firestoreFamilyId);

        String safeFamilyName =
                familyName == null ||
                        familyName.trim().isEmpty()
                        ? "Family"
                        : familyName;

        String safeUserName =
                userName == null
                        ? ""
                        : userName;

        Map<String, Object> memberData =
                new HashMap<>();

        memberData.put(
                "userUid",
                firebaseUid
        );

        memberData.put(
                "userName",
                safeUserName
        );

        memberData.put(
                "role",
                role
        );

        memberData.put(
                "joinedAt",
                FieldValue.serverTimestamp()
        );

        Map<String, Object> userFamilyData =
                new HashMap<>();

        userFamilyData.put(
                "familyId",
                firestoreFamilyId
        );

        userFamilyData.put(
                "familyName",
                safeFamilyName
        );

        userFamilyData.put(
                "role",
                role
        );

        userFamilyData.put(
                "joinedAt",
                FieldValue.serverTimestamp()
        );

        Map<String, Object> invitationUpdate =
                new HashMap<>();

        invitationUpdate.put(
                "status",
                "ACCEPTED"
        );

        invitationUpdate.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        WriteBatch batch =
                firestore.batch();

        batch.set(
                memberReference,
                memberData
        );

        batch.set(
                userFamilyReference,
                userFamilyData
        );

        batch.update(
                invitationReference,
                invitationUpdate
        );

        batch.commit()
                .addOnSuccessListener(unused -> {

                    callback.onSuccess(
                            firestoreFamilyId,
                            safeFamilyName,
                            role
                    );
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // DECLINE INVITATION
    // =====================================================

    public void declineInvitation(
            String invitationId,
            String firebaseUid,
            DeclineInvitationCallback callback
    ) {

        if (invitationId == null ||
                invitationId.trim().isEmpty()) {

            callback.onFailure(
                    "Invitation was not found."
            );

            return;
        }

        if (firebaseUid == null ||
                firebaseUid.trim().isEmpty()) {

            callback.onFailure(
                    "User account was not found."
            );

            return;
        }

        DocumentReference invitationReference =
                firestore
                        .collection(
                                COLLECTION_INVITATIONS
                        )
                        .document(invitationId);

        invitationReference
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        callback.onFailure(
                                "Invitation no longer exists."
                        );

                        return;
                    }

                    String invitedUserUid =
                            document.getString(
                                    "invitedUserUid"
                            );

                    if (!firebaseUid.equals(
                            invitedUserUid
                    )) {

                        callback.onFailure(
                                "This invitation does not belong to your account."
                        );

                        return;
                    }

                    Map<String, Object> update =
                            new HashMap<>();

                    update.put(
                            "status",
                            "DECLINED"
                    );

                    update.put(
                            "updatedAt",
                            FieldValue.serverTimestamp()
                    );

                    invitationReference
                            .update(update)
                            .addOnSuccessListener(unused -> {

                                callback.onSuccess();
                            })
                            .addOnFailureListener(exception -> {

                                callback.onFailure(
                                        getErrorMessage(
                                                exception
                                        )
                                );
                            });
                })
                .addOnFailureListener(exception -> {

                    callback.onFailure(
                            getErrorMessage(exception)
                    );
                });
    }

    // =====================================================
    // CREATE INVITATION DOCUMENT ID
    // =====================================================

    private String createInvitationDocumentId(
            String familyId,
            String invitedUserUid
    ) {

        return familyId +
                "_" +
                invitedUserUid;
    }

    // =====================================================
    // GENERATE INVITATION CODE
    // =====================================================

    private String generateInvitationCode() {

        String randomPart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase(Locale.ROOT);

        return "INV-" + randomPart;
    }

    // =====================================================
    // ERROR MESSAGE
    // =====================================================

    private String getErrorMessage(
            Exception exception
    ) {

        if (exception == null ||
                exception.getMessage() == null ||
                exception.getMessage()
                        .trim()
                        .isEmpty()) {

            return "Something went wrong. Please try again.";
        }

        return exception.getMessage();
    }
}