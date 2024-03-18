package com.tuum.banking.dto;

import com.tuum.banking.domain.Transaction.TransactionDirection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class TransactionDto {
	private Long transactionId;
	private Long accountId;
	private BigDecimal amount;
	private String currency;
	private TransactionDirection direction;
	private String description;
	private BigDecimal balanceAfterTransaction;
}
