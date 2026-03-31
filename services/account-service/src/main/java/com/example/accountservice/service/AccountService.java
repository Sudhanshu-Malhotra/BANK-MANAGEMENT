package com.example.accountservice.service;

import com.example.accountservice.dto.AccountDto;
import com.example.accountservice.entity.Account;
import com.example.accountservice.exception.InsufficientFundsException;
import com.example.accountservice.exception.ResourceNotFoundException;
import com.example.accountservice.mapper.AccountMapper;
import com.example.accountservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Transactional
    public AccountDto createAccount(Long userId) {
        log.info("Creating account for userId: {}", userId);
        Account account = new Account(
            userId,
            "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            BigDecimal.ZERO
        );
        Account saved = accountRepository.save(account);
        log.info("Account created: {} for userId: {}", saved.getAccountNumber(), userId);
        return accountMapper.accountToAccountDto(saved);
    }

    @Cacheable(value = "accounts", key = "#id")
    public AccountDto getAccountById(Long id) {
        log.debug("Fetching account by id: {}", id);
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return accountMapper.accountToAccountDto(account);
    }

    public List<AccountDto> getAccountsByUserId(Long userId) {
        log.debug("Fetching accounts for userId: {}", userId);
        return accountRepository.findByUserId(userId).stream()
                .map(accountMapper::accountToAccountDto)
                .toList();
    }

    @CacheEvict(value = "accounts", key = "#id")
    @Transactional
    public AccountDto deposit(Long id, BigDecimal amount) {
        log.info("Processing deposit of {} to accountId: {}", amount, id);
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        account.setBalance(account.getBalance().add(amount));
        Account saved = accountRepository.save(account);
        log.info("Deposit completed. AccountId: {}, New balance: {}", id, saved.getBalance());
        return accountMapper.accountToAccountDto(saved);
    }

    @CacheEvict(value = "accounts", key = "#id")
    @Transactional
    public AccountDto withdraw(Long id, BigDecimal amount) {
        log.info("Processing withdrawal of {} from accountId: {}", amount, id);
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds in accountId: {}. Requested: {}, Available: {}",
                id, amount, account.getBalance());
            throw new InsufficientFundsException(id, amount, account.getBalance());
        }
        account.setBalance(account.getBalance().subtract(amount));
        Account saved = accountRepository.save(account);
        log.info("Withdrawal completed. AccountId: {}, New balance: {}", id, saved.getBalance());
        return accountMapper.accountToAccountDto(saved);
    }
}
