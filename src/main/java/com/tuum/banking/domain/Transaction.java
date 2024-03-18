package com.tuum.banking.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Transaction extends BaseEntity {
	private Long accountId;
	private Long balanceId;
	private BigDecimal amount;
	private TransactionDirection direction;
	private String description;
	private BigDecimal balanceAfterTransaction;
	
	public enum TransactionDirection {
		IN, OUT
	}
}
