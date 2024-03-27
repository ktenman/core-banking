package com.tuum.banking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tuum.banking.configuration.CustomPage;
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
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import static com.tuum.banking.configuration.RedisConfiguration.ACCOUNTS_CACHE;
import static com.tuum.banking.configuration.RedisConfiguration.TRANSACTIONS_CACHE;
import static com.tuum.banking.configuration.rabbitmq.EventType.TRANSACTION_CREATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
	private final TransactionMapper transactionMapper;
	private final AccountService accountService;
	private final BalanceService balanceService;
	private final TransactionRunner transactionRunner;
	private final OutboxMessageService outboxMessageService;
	
	@Lock(key = "#request.idempotencyKey")
	@Caching(evict = {
			@CacheEvict(value = ACCOUNTS_CACHE, key = "#request.accountId"),
			@CacheEvict(value = TRANSACTIONS_CACHE, allEntries = true, condition = "#request.accountId != null")
	})
	public Transaction createTransaction(CreateTransactionRequest request) {
		Account account = accountService.getAccountById(request.getAccountId());
		Balance balance = balanceService.getBalanceByAccountIdAndCurrency(request.getAccountId(), request.getCurrency());
		
		BigDecimal newBalanceAmount = balanceService.calculateNewBalance(balance.getAvailableAmount(), request);
		
		Transaction newTransaction = TransactionConverter.toDomain(request);
		newTransaction.setAccountId(account.getId());
		newTransaction.setBalanceId(balance.getId());
		newTransaction.setBalanceAfterTransaction(newBalanceAmount);
		newTransaction.setReference(UUID.randomUUID().toString());
		
		return transactionRunner.execute(() -> {
			transactionMapper.insert(newTransaction);
			outboxMessageService.createOutboxMessage(newTransaction, TRANSACTION_CREATED);
			balance.setAvailableAmount(newBalanceAmount);
			balanceService.updateBalance(balance, newBalanceAmount);
			return newTransaction;
		});
	}
	
	//	@Cacheable(value = TRANSACTIONS_CACHE, key = "{ #accountId, #page.current, #page.size }")
	public IPage<Transaction> getTransactions(Long accountId, CustomPage<Transaction> page) {
		Account account = accountService.getAccountById(accountId);
		return transactionMapper.selectByAccountId(account.getId(), page);
	}
}
