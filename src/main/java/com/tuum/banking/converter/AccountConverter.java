package com.tuum.banking.converter;

import com.tuum.banking.domain.Account;
import com.tuum.banking.dto.AccountDto;
import com.tuum.banking.dto.CreateAccountRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountConverter {
	public static Account toEntity(CreateAccountRequest createAccountRequest) {
		Account account = new Account();
		account.setCustomerId(createAccountRequest.getCustomerId());
		account.setCountryCode(createAccountRequest.getCountry());
		account.setReference(createAccountRequest.getReference());
		return account;
	}
	
	public static AccountDto toDto(Account account) {
		AccountDto accountDto = new AccountDto();
		accountDto.setAccountId(account.getId());
		accountDto.setCustomerId(account.getCustomerId());
		accountDto.setReference(account.getReference());
		return accountDto;
	}
}
