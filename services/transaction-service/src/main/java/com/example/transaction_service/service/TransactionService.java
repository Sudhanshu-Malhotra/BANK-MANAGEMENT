package com.example.transaction_service.service;

import com.example.transaction_service.client.AccountClient;
import com.example.transaction_service.dto.TransactionDto;
import com.example.transaction_service.entity.Transaction;
import com.example.transaction_service.mapper.TransactionMapper;
import com.example.transaction_service.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private TransactionMapper transactionMapper;

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(
        name = "ACCOUNT-SERVICE",
        fallbackMethod = "transferFallback"
    )
    @Transactional
    public TransactionDto transfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
        log.info("Initiating transfer of {} from account {} to account {}",
            amount, sourceAccountId, destinationAccountId);

        Transaction tx = new Transaction();
        tx.setSourceAccountId(sourceAccountId);
        tx.setDestinationAccountId(destinationAccountId);
        tx.setAmount(amount);
        tx.setType("TRANSFER");
        tx.setStatus(Transaction.TransactionStatus.PENDING);
        Transaction savedTx = transactionRepository.save(tx);

        try {
            accountClient.withdraw(sourceAccountId, amount);
            accountClient.deposit(destinationAccountId, amount);

            savedTx.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(savedTx);

            String message = String.format(
                "TRANSFER COMPLETED | TX#%d | Amount: $%.2f | From: %d | To: %d",
                savedTx.getId(), amount, sourceAccountId, destinationAccountId
            );
            kafkaProducerService.sendTransactionEvent(message);

            log.info("Transfer completed successfully. TX id: {}", savedTx.getId());
            return transactionMapper.transactionToTransactionDto(savedTx);

        } catch (Exception e) {
            log.error("Transfer failed for TX id: {}. Rolling back status to FAILED.", savedTx.getId(), e);
            savedTx.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(savedTx);
            throw e;
        }
    }

    public TransactionDto transferFallback(Long sourceAccountId, Long destinationAccountId, BigDecimal amount, Throwable t) {
        log.error("Circuit breaker OPEN for ACCOUNT-SERVICE. Transfer of {} from {} to {} cannot proceed. Reason: {}",
            amount, sourceAccountId, destinationAccountId, t.getMessage());
        throw new RuntimeException("Transfer service temporarily unavailable. Account service is down. Please try again later.");
    }
}
