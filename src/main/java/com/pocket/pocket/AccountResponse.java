package com.pocket.pocket;

public class AccountResponse {

    private final Long id;
    private final String name;
    private final String accountType;

    public AccountResponse(Long id, String name, String accountType) {
        this.id = id;
        this.name = name;
        this.accountType = accountType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAccountType() {
        return accountType;
    }
}