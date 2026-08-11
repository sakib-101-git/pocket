package com.pocket.pocket;

public class SyncJobMessage {

    private Long accountId;

    public SyncJobMessage() {
    }

    public SyncJobMessage(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
