package com.pocket.pocket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    private String name;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "checking|savings|credit", message = "Account type must be checking, savings, or credit")
    private String accountType;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}
