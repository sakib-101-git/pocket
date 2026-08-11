package com.pocket.pocket;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "plaid_access_token")
    private String plaidAccessToken;

    @Column(name = "plaid_item_id")
    private String plaidItemId;

    @Column(name = "sync_status")
    private String syncStatus;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPlaidAccessToken() {
        return plaidAccessToken;
    }

    public void setPlaidAccessToken(String plaidAccessToken) {
        this.plaidAccessToken = plaidAccessToken;
    }

    public String getPlaidItemId() {
        return plaidItemId;
    }

    public void setPlaidItemId(String plaidItemId) {
        this.plaidItemId = plaidItemId;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}