package com.example.aiexpensemanagementapplication.model;

public class FamilyInvitation {

    private String invitationId;

    private Object familyId;

    private String familyName;
    private String invitedEmail;
    private String invitedBy;
    private String role;
    private String status;

    private long createdAt;


    public FamilyInvitation() {
    }


    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }


    public Object getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Object familyId) {
        this.familyId = familyId;
    }


    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }


    public String getInvitedEmail() {
        return invitedEmail;
    }

    public void setInvitedEmail(String invitedEmail) {
        this.invitedEmail = invitedEmail;
    }


    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}