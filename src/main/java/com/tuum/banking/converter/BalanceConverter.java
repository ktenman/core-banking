package com.tuum.banking.converter;

import com.tuum.banking.domain.Balance;
import com.tuum.banking.model.AccountModel;
import com.tuum.banking.model.CreateAccountRequest.BalanceRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceModelMapper {
	public static AccountModel.BalanceResponseDto toResponseDto(Balance balance) {
		return new AccountModel.BalanceResponseDto(balance.getId(), balance.getCurrency(), balance.getAvailableAmount());
	}
	
	public static Balance toEntity(BalanceRequestDto balanceResponseDto) {
		Balance balance = new Balance();
		balance.setCurrency(balanceResponseDto.getCurrency().name());
		balance.setAvailableAmount(balanceResponseDto.getAvailableAmount());
		return balance;
	}
	
	public static List<Balance> toEntities(List<BalanceRequestDto> balances) {
		return balances.stream()
				.map(BalanceModelMapper::toEntity)
				.toList();
	}
}
