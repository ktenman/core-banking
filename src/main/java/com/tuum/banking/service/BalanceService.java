package com.tuum.banking.service;

import com.tuum.banking.configuration.exception.InsufficientFundsException;
import com.tuum.banking.configuration.exception.InvalidCurrencyException;
import com.tuum.banking.converter.BalanceConverter;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.dto.CreateAccountRequest.BalanceRequestDto;
import com.tuum.banking.dto.CreateTransactionRequest;
import com.tuum.banking.mapper.BalanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceService {
	private final BalanceMapper balanceMapper;
	
	public List<Balance> createBalances(Long accountId, List<BalanceRequestDto> balanceRequestDtos) {
		List<Balance> balances = BalanceConverter.toEntities(balanceRequestDtos);
		balances.forEach(balance -> {
			balance.setAccountId(accountId);
			balanceMapper.insert(balance);
		});
		return balances;
	}
	
	public Balance getBalanceByAccountIdAndCurrency(Long accountId, String currency) {
		return balanceMapper.findByAccountIdAndCurrency(accountId, currency)
				.orElseThrow(() -> new InvalidCurrencyException(
						"No balance found for account ID: " + accountId + " and currency: " + currency));
	}
	
	public BigDecimal calculateNewBalance(BigDecimal currentBalance, CreateTransactionRequest request) {
		return switch (request.getDirection()) {
			case IN -> currentBalance.add(request.getAmount());
			case OUT -> {
				if (currentBalance.compareTo(request.getAmount()) < 0) {
					throw new InsufficientFundsException("Insufficient funds for account ID: " + request.getAccountId() + " and currency: " + request.getCurrency());
				}
				yield currentBalance.subtract(request.getAmount());
			}
		};
	}
	
	public void updateBalance(Balance balance, BigDecimal newBalance) {
		balance.setAvailableAmount(newBalance);
		balanceMapper.updateById(balance);
	}
}
