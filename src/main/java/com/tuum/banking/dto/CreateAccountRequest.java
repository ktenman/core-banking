package com.tuum.banking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
	
	@NotNull
	private Long customerId;
	
	@Length(min = 3, max = 3)
	private String country;
	
	@NotEmpty
	@Valid
	private List<BalanceRequestDto> balances;
	
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BalanceRequestDto {
		@NotNull
		private BalanceCurrency currency;
		
		@NotNull
		private BigDecimal availableAmount;
	}
	
	public enum BalanceCurrency {
		USD, EUR, GBP
	}
	
}
