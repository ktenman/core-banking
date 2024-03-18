package com.tuum.banking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
	
	@NotNull(message = "Customer ID is required")
	private Long customerId;
	
	@NotEmpty(message = "Reference is required")
	private String reference;
	
	@Length(min = 3, max = 3, message = "Country must be 3 characters long")
	@NotEmpty(message = "Country is required")
	private String country;
	
	@NotEmpty(message = "At least one balance is required")
	@Valid
	private List<BalanceRequestDto> balances;
	
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
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
