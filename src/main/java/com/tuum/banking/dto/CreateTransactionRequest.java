package com.tuum.banking.dto;

import com.tuum.banking.configuration.exception.InvalidCurrencyException;
import com.tuum.banking.configuration.exception.InvalidDirectionException;
import com.tuum.banking.domain.Transaction.TransactionDirection;
import com.tuum.banking.dto.CreateAccountRequest.BalanceCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
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
	@Schema(description = "ID of the account", example = "1")
	private Long accountId;
	
	@NotNull(message = "Amount is required")
	@Positive(message = "Amount must be positive")
	@Schema(description = "Amount of the transaction", example = "101.23")
	private BigDecimal amount;
	
	@NotEmpty(message = "Currency is required")
	@Schema(description = "Currency of the transaction", example = "USD")
	private String currency;
	
	@NotNull(message = "Direction is required")
	@Schema(description = "Direction of the transaction", example = "IN")
	private TransactionDirection direction;
	
	@NotEmpty(message = "Description is required")
	@Schema(description = "Description of the transaction", example = "Salary")
	private String description;
	
	@NotEmpty(message = "Idempotency key is required")
	@Schema(description = "Idempotency key of the transaction", example = "123e4567-e89b-12d3-a456-426614174000")
	private String idempotencyKey;
	
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
