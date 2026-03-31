package com.example.transaction_service.controller;

import com.example.transaction_service.dto.TransactionDto;
import com.example.transaction_service.dto.TransferRequest;
import com.example.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("POST /api/transactions/transfer | from: {} to: {} amount: {}",
            request.getSourceAccountId(), request.getDestinationAccountId(), request.getAmount());
        TransactionDto result = transactionService.transfer(
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
