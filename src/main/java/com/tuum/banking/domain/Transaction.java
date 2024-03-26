package com.tuum.banking.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends BaseDomain implements Aggregate {
	private Long accountId;
	private Long balanceId;
	private BigDecimal amount;
	private TransactionDirection direction;
	private String description;
	private BigDecimal balanceAfterTransaction;
	private String currency;
	private String reference;
	
	public enum TransactionDirection {
		IN, OUT
	}
}
