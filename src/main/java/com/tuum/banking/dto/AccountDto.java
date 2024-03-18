package com.tuum.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountModel {
	private Long accountId;
	private Long customerId;
	private List<BalanceResponseDto> balances;
	
	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
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
