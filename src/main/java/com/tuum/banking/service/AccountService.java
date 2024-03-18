package com.tuum.banking.service;

import com.tuum.banking.configuration.exception.AccountNotFoundException;
import com.tuum.banking.converter.AccountConverter;
import com.tuum.banking.converter.BalanceConverter;
import com.tuum.banking.domain.Account;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.dto.AccountDto;
import com.tuum.banking.dto.CreateAccountRequest;
import com.tuum.banking.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountMapper accountMapper;
	private final BalanceService balanceService;
	
	@Transactional
	public AccountDto createAccount(CreateAccountRequest createAccountRequest) {
		Account account = AccountConverter.toEntity(createAccountRequest);
		accountMapper.insert(account);
		List<Balance> balances = balanceService.createBalances(account.getId(), createAccountRequest.getBalances());
		AccountDto accountDto = AccountConverter.toModel(account);
		accountDto.setBalances(balances.stream().map(BalanceConverter::toResponseDto).toList());
		return accountDto;
	}
	
	public AccountDto getAccount(Long accountId) {
		Account account = accountMapper.getAccountWithBalances(accountId).orElseThrow(
				() -> new AccountNotFoundException(String.format("Account with ID %s does not exist", accountId))
		);
		AccountDto accountDto = AccountConverter.toModel(account);
		accountDto.setBalances(account.getBalances().stream().map(BalanceConverter::toResponseDto).toList());
		return accountDto;
	}
	
}
