package com.example.accountservice.dto;

import com.example.accountservice.entity.Account;
import java.math.BigDecimal;

public class AccountDto {
    private Long id;
    private Long userId;
    private String accountNumber;
    private BigDecimal balance;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.userId = account.getUserId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
}
