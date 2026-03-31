package com.example.transaction_service.service;

import com.example.transaction_service.client.AccountClient;
import com.example.transaction_service.dto.TransactionDto;
import com.example.transaction_service.entity.Transaction;
import com.example.transaction_service.mapper.TransactionMapper;
import com.example.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountClient accountClient;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction pendingTx;
    private Transaction completedTx;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        pendingTx = new Transaction();
        pendingTx.setId(1L);
        pendingTx.setSourceAccountId(1L);
        pendingTx.setDestinationAccountId(2L);
        pendingTx.setAmount(new BigDecimal("500.00"));
        pendingTx.setType("TRANSFER");
        pendingTx.setStatus(Transaction.TransactionStatus.PENDING);

        completedTx = new Transaction();
        completedTx.setId(1L);
        completedTx.setSourceAccountId(1L);
        completedTx.setDestinationAccountId(2L);
        completedTx.setAmount(new BigDecimal("500.00"));
        completedTx.setType("TRANSFER");
        completedTx.setStatus(Transaction.TransactionStatus.COMPLETED);

        transactionDto = new TransactionDto();
        transactionDto.setId(1L);
        transactionDto.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactionDto.setAmount(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("transfer - should complete successfully when account service is available")
    void transfer_WhenAccountServiceAvailable_ShouldCompleteSuccessfully() {
        given(transactionRepository.save(any(Transaction.class)))
            .willReturn(pendingTx)
            .willReturn(completedTx);
        given(transactionMapper.transactionToTransactionDto(any(Transaction.class))).willReturn(transactionDto);

        TransactionDto result = transactionService.transfer(1L, 2L, new BigDecimal("500.00"));

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
        assertThat(result.getAmount()).isEqualByComparingTo("500.00");

        then(accountClient).should(times(1)).withdraw(1L, new BigDecimal("500.00"));
        then(accountClient).should(times(1)).deposit(2L, new BigDecimal("500.00"));
        then(kafkaProducerService).should(times(1)).sendTransactionEvent(any(String.class));
    }

    @Test
    @DisplayName("transfer - should save FAILED status when account service throws exception")
    void transfer_WhenAccountServiceFails_ShouldSaveFailedStatus() {
        given(transactionRepository.save(any(Transaction.class))).willReturn(pendingTx);
        willThrow(new RuntimeException("Account service unavailable"))
            .given(accountClient).withdraw(any(Long.class), any(BigDecimal.class));

        assertThatThrownBy(() -> transactionService.transfer(1L, 2L, new BigDecimal("500.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account service unavailable");

        // Verify FAILED status was saved to DB
        then(transactionRepository).should(times(2)).save(any(Transaction.class));
        then(kafkaProducerService).should(times(0)).sendTransactionEvent(any());
    }

    @Test
    @DisplayName("transferFallback - should throw RuntimeException with informative message")
    void transferFallback_ShouldThrowRuntimeExceptionWithMessage() {
        Throwable cause = new RuntimeException("Connection refused");

        assertThatThrownBy(() ->
                transactionService.transferFallback(1L, 2L, new BigDecimal("100.00"), cause))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("temporarily unavailable");
    }
}
