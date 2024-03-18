package com.tuum.banking.service;

import com.tuum.banking.configuration.exception.InvalidAccountException;
import com.tuum.banking.converter.AccountConverter;
import com.tuum.banking.domain.Account;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.dto.CreateAccountRequest;
import com.tuum.banking.mapper.AccountMapper;
import com.tuum.banking.service.lock.Lock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountMapper accountMapper;
	private final BalanceService balanceService;
	private final RabbitMQPublisher rabbitMQPublisher;
	
	@Transactional
	@Lock(key = "#createAccountRequest.reference")
	public Account createAccount(CreateAccountRequest createAccountRequest) {
		Account account = AccountConverter.toEntity(createAccountRequest);
		accountMapper.insert(account);
		List<Balance> balances = balanceService.createBalances(account.getId(), createAccountRequest.getBalances());
		account.setBalances(balances);
		rabbitMQPublisher.publishAccountCreated(account);
		return account;
	}
	
	public Account getAccount(Long accountId) {
		return accountMapper.getAccountWithBalances(accountId).orElseThrow(() -> new InvalidAccountException(accountId));
	}
	
	public Account getAccountById(Long accountId) {
		return accountMapper.selectById(accountId).orElseThrow(() -> new InvalidAccountException(accountId));
	}
}
