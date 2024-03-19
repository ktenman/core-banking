package com.tuum.banking.dto;

import com.tuum.banking.configuration.exception.InvalidCurrencyException;
import io.swagger.v3.oas.annotations.media.Schema;
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
	@Schema(description = "ID of the customer", example = "1")
	private Long customerId;
	
	@NotEmpty(message = "Reference is required")
	@Schema(description = "Reference of the account", example = "8faf7a98-3dc1-4fff-bb1b-90746c8a5c9c")
	private String reference;
	
	@Length(min = 3, max = 3, message = "Country must be 3 characters long")
	@NotEmpty(message = "Country is required")
	@Schema(description = "Country of the account", example = "USA")
	private String country;
	
	@NotEmpty(message = "At least one balance is required")
	@Valid
	@Schema(description = "List of balances for the account")
	private List<BalanceRequestDto> balances;
	
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	public static class BalanceRequestDto {
		
		@NotNull
		@Schema(description = "Currency of the balance", example = "USD")
		private String currency;
		
		@NotNull
		@Schema(description = "Available amount of the balance", example = "1035.79")
		private BigDecimal availableAmount;
		
		public void setCurrency(String currency) {
			validateCurrency(currency);
			this.currency = currency;
		}
		
		private void validateCurrency(String currency) {
			try {
				BalanceCurrency.valueOf(currency);
			} catch (IllegalArgumentException e) {
				throw new InvalidCurrencyException("Invalid currency: " + currency);
			}
		}
	}
	
	public enum BalanceCurrency {
		USD, EUR, GBP
	}
	
}
