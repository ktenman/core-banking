package com.tuum.banking.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AccountDto {
	private Long accountId;
	private Long customerId;
	private List<BalanceResponseDto> balances;
	private String reference;
	
	@Getter
	@Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class BalanceResponseDto {
		private Long balanceId;
		private String currency;
		private BigDecimal availableAmount;
		
		public BalanceResponseDto(Long balanceId, String currency, BigDecimal availableAmount) {
			this.balanceId = balanceId;
			this.currency = currency;
			this.availableAmount = availableAmount;
		}
	}
}
