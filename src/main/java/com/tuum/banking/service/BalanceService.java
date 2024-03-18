package com.tuum.banking.service;

import com.tuum.banking.converter.BalanceConverter;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.dto.CreateAccountRequest.BalanceRequestDto;
import com.tuum.banking.mapper.BalanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
