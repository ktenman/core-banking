package com.tuum.banking.service;

import com.tuum.banking.converter.TransactionConverter;
import com.tuum.banking.domain.Account;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.domain.Transaction;
import com.tuum.banking.dto.CreateTransactionRequest;
import com.tuum.banking.mapper.TransactionMapper;
import com.tuum.banking.service.lock.Lock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.tuum.banking.configuration.RedisConfiguration.ACCOUNTS_CACHE;
import static com.tuum.banking.configuration.RedisConfiguration.TRANSACTIONS_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
	private final TransactionMapper transactionMapper;
	private final RabbitMQPublisher rabbitMQPublisher;
	private final AccountService accountService;
	private final BalanceService balanceService;
	private final TransactionRunner transactionRunner;
	
	@Lock(key = "#request.accountId")
	@Caching(evict = {
			@CacheEvict(value = ACCOUNTS_CACHE, key = "#request.accountId"),
			@CacheEvict(value = TRANSACTIONS_CACHE, key = "#request.accountId")
	})
	public Transaction createTransaction(CreateTransactionRequest request) {
		Account account = accountService.getAccountById(request.getAccountId());
		Balance balance = balanceService.getBalanceByAccountIdAndCurrency(request.getAccountId(), request.getCurrency());
		
		BigDecimal newBalanceAmount = balanceService.calculateNewBalance(balance.getAvailableAmount(), request);
		
		Transaction transaction = transactionRunner.execute(() -> {
			Transaction newTransaction = TransactionConverter.toDomain(request);
			newTransaction.setAccountId(account.getId());
			newTransaction.setBalanceId(balance.getId());
			newTransaction.setBalanceAfterTransaction(newBalanceAmount);
			newTransaction.setReference(UUID.randomUUID().toString());
			transactionMapper.insert(newTransaction);
			balance.setAvailableAmount(newBalanceAmount);
			balanceService.updateBalance(balance, newBalanceAmount);
			return newTransaction;
		});
		
		rabbitMQPublisher.publishTransactionCreated(transaction);
		return transaction;
	}
	
	@Cacheable(value = TRANSACTIONS_CACHE, key = "#accountId")
	public List<Transaction> getTransactions(Long accountId) {
		Account account = accountService.getAccountById(accountId);
		return transactionMapper.selectByAccountId(account.getId());
	}
}
