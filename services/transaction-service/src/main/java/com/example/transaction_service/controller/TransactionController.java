package com.example.transaction_service.controller;

import com.example.transaction_service.dto.TransferRequest;
import com.example.transaction_service.entity.Transaction;
import com.example.transaction_service.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(
                request.getSourceAccountId(), 
                request.getDestinationAccountId(), 
                request.getAmount()
        ));
    }
}
