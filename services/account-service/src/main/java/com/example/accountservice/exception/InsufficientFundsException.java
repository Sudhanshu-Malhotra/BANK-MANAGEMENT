package com.example.accountservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long accountId, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format(
            "Insufficient funds in account %d. Requested: %.2f, Available: %.2f",
            accountId, requestedAmount, availableBalance
        ));
    }
}
