package com.tuum.banking.converter;

import com.tuum.banking.domain.Account;
import com.tuum.banking.model.AccountModel;
import com.tuum.banking.model.CreateAccountRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountModelMapper {
	public static Account toEntity(CreateAccountRequest createAccountRequest) {
		Account account = new Account();
		account.setCustomerId(createAccountRequest.getCustomerId());
		account.setCountryCode(createAccountRequest.getCountry());
		return account;
	}
	
	public static AccountModel toModel(Account account) {
		AccountModel accountModel = new AccountModel();
		accountModel.setAccountId(account.getId());
		accountModel.setCustomerId(account.getCustomerId());
		return accountModel;
	}
}
