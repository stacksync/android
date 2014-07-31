package com.stacksync.android.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

public class Member {

    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("joined_at")
    private Date joinedAt;
    @JsonProperty("is_owner")
    private Boolean isOwner;

    public Member() {
    }

    public Member(String name, String email, Date joinedAt, Boolean isOwner) {
        this.name = name;
        this.email = email;
        this.joinedAt = joinedAt;
        this.isOwner = isOwner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Boolean getIsOwner() {
        return isOwner;
    }

    public void setIsOwner(Boolean isOwner) {
        this.isOwner = isOwner;
    }
}
