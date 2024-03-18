package com.tuum.banking.converter;

import com.tuum.banking.domain.Balance;
import com.tuum.banking.dto.AccountDto;
import com.tuum.banking.dto.CreateAccountRequest.BalanceRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceConverter {
	public static AccountDto.BalanceResponseDto toResponseDto(Balance balance) {
		return new AccountDto.BalanceResponseDto(balance.getId(), balance.getCurrency(), balance.getAvailableAmount());
	}
	
	public static Balance toEntity(BalanceRequestDto balanceResponseDto) {
		Balance balance = new Balance();
		balance.setCurrency(balanceResponseDto.getCurrency());
		balance.setAvailableAmount(balanceResponseDto.getAvailableAmount());
		return balance;
	}
	
	public static List<Balance> toEntities(List<BalanceRequestDto> balances) {
		return balances.stream()
				.map(BalanceConverter::toEntity)
				.toList();
	}
}
