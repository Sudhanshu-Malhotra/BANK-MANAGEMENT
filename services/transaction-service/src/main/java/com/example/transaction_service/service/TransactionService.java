package com.example.transaction_service.service;

import com.example.transaction_service.client.AccountClient;
import com.example.transaction_service.entity.Transaction;
import com.example.transaction_service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountClient accountClient;
    
    @Autowired
    private KafkaProducerService kafkaProducerService;

    public Transaction transfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
        // 1. Synchronously withdraw from source using Feign
        accountClient.withdraw(sourceAccountId, amount);
        
        // 2. Synchronously deposit to destination using Feign
        accountClient.deposit(destinationAccountId, amount);

        // 3. Save comprehensive transaction record to DB
        Transaction tx = new Transaction();
        tx.setSourceAccountId(sourceAccountId);
        tx.setDestinationAccountId(destinationAccountId);
        tx.setAmount(amount);
        tx.setType("TRANSFER");
        Transaction savedTx = transactionRepository.save(tx);
        
        // 4. Asynchronously notify user via Kafka
        String message = String.format("Successful transfer of $%s from Account %d to Account %d (TX ID: %d)", 
                amount, sourceAccountId, destinationAccountId, savedTx.getId());
        kafkaProducerService.sendTransactionEvent(message);
        
        return savedTx;
    }
}
