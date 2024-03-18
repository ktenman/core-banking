package com.tuum.banking.dto;

import com.tuum.banking.configuration.exception.InvalidCurrencyException;
import com.tuum.banking.configuration.exception.InvalidDirectionException;
import com.tuum.banking.domain.Transaction.TransactionDirection;
import com.tuum.banking.dto.CreateAccountRequest.BalanceCurrency;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class CreateTransactionRequest {
	
	@NotNull(message = "Account ID is required")
	private Long accountId;
	
	@NotNull(message = "Amount is required")
	@Positive(message = "Amount must be positive")
	private BigDecimal amount;
	
	@NotEmpty(message = "Currency is required")
	private String currency;
	
	@NotNull(message = "Direction is required")
	private TransactionDirection direction;
	
	@NotEmpty(message = "Description is required")
	private String description;
	
	public void setCurrency(String currency) {
		validateCurrency(currency);
		this.currency = currency;
	}
	
	public void setDirection(String direction) {
		validateDirection(direction);
		this.direction = TransactionDirection.valueOf(direction);
	}
	
	private void validateDirection(String direction) {
		try {
			TransactionDirection.valueOf(direction);
		} catch (IllegalArgumentException e) {
			throw new InvalidDirectionException("Invalid direction: " + direction);
		}
	}
	
	private void validateCurrency(String currency) {
		try {
			BalanceCurrency.valueOf(currency);
		} catch (IllegalArgumentException e) {
			throw new InvalidCurrencyException("Invalid currency: " + currency);
		}
	}
}
