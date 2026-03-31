package com.example.accountservice.service;

import com.example.accountservice.dto.AccountDto;
import com.example.accountservice.entity.Account;
import com.example.accountservice.exception.InsufficientFundsException;
import com.example.accountservice.exception.ResourceNotFoundException;
import com.example.accountservice.mapper.AccountMapper;
import com.example.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        account = new Account(1L, "ACC12345678", new BigDecimal("1000.00"));
        account.setId(1L);

        accountDto = new AccountDto();
        accountDto.setId(1L);
        accountDto.setUserId(1L);
        accountDto.setAccountNumber("ACC12345678");
        accountDto.setBalance(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("createAccount - should create account with zero balance")
    void createAccount_ShouldCreateAccountWithZeroBalance() {
        given(accountRepository.save(any(Account.class))).willReturn(account);
        given(accountMapper.accountToAccountDto(account)).willReturn(accountDto);

        AccountDto result = accountService.createAccount(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        then(accountRepository).should(times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("getAccountById - should return account when found")
    void getAccountById_WhenFound_ShouldReturnAccountDto() {
        given(accountRepository.findById(1L)).willReturn(Optional.of(account));
        given(accountMapper.accountToAccountDto(account)).willReturn(accountDto);

        AccountDto result = accountService.getAccountById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAccountNumber()).isEqualTo("ACC12345678");
    }

    @Test
    @DisplayName("getAccountById - should throw ResourceNotFoundException when not found")
    void getAccountById_WhenNotFound_ShouldThrowException() {
        given(accountRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deposit - should increase balance correctly")
    void deposit_ShouldIncreaseBalance() {
        given(accountRepository.findById(1L)).willReturn(Optional.of(account));
        Account afterDeposit = new Account(1L, "ACC12345678", new BigDecimal("1500.00"));
        afterDeposit.setId(1L);
        given(accountRepository.save(any(Account.class))).willReturn(afterDeposit);
        AccountDto afterDto = new AccountDto();
        afterDto.setBalance(new BigDecimal("1500.00"));
        given(accountMapper.accountToAccountDto(afterDeposit)).willReturn(afterDto);

        AccountDto result = accountService.deposit(1L, new BigDecimal("500.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("withdraw - should decrease balance when sufficient funds")
    void withdraw_WhenSufficientFunds_ShouldDecreaseBalance() {
        given(accountRepository.findById(1L)).willReturn(Optional.of(account));
        Account afterWithdraw = new Account(1L, "ACC12345678", new BigDecimal("700.00"));
        afterWithdraw.setId(1L);
        given(accountRepository.save(any(Account.class))).willReturn(afterWithdraw);
        AccountDto afterDto = new AccountDto();
        afterDto.setBalance(new BigDecimal("700.00"));
        given(accountMapper.accountToAccountDto(afterWithdraw)).willReturn(afterDto);

        AccountDto result = accountService.withdraw(1L, new BigDecimal("300.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("withdraw - should throw InsufficientFundsException when balance is too low")
    void withdraw_WhenInsufficientFunds_ShouldThrowException() {
        given(accountRepository.findById(1L)).willReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("5000.00")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds")
                .hasMessageContaining("1")
                .hasMessageContaining("5000.00");
    }

    @Test
    @DisplayName("getAccountsByUserId - should return all accounts for user")
    void getAccountsByUserId_ShouldReturnAllAccounts() {
        given(accountRepository.findByUserId(1L)).willReturn(List.of(account));
        given(accountMapper.accountToAccountDto(account)).willReturn(accountDto);

        List<AccountDto> result = accountService.getAccountsByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("ACC12345678");
    }
}
