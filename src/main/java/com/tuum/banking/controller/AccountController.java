package com.tuum.banking.controller;

import com.tuum.banking.configuration.logging.Loggable;
import com.tuum.banking.converter.AccountConverter;
import com.tuum.banking.converter.BalanceConverter;
import com.tuum.banking.domain.Account;
import com.tuum.banking.dto.AccountDto;
import com.tuum.banking.dto.CreateAccountRequest;
import com.tuum.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;
	
	@Loggable
	@PostMapping
	public AccountDto createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest) {
		Account account = accountService.createAccount(createAccountRequest);
		return convertAccountToDto(account);
	}
	
	@Loggable
	@GetMapping("/{accountId}")
	public AccountDto getAccount(@PathVariable Long accountId) {
		Account account = accountService.getAccount(accountId);
		return convertAccountToDto(account);
	}
	
	private AccountDto convertAccountToDto(Account account) {
		AccountDto accountDto = AccountConverter.toDto(account);
		accountDto.setBalances(account.getBalances().stream().map(BalanceConverter::toResponseDto).toList());
		return accountDto;
	}
	
}
