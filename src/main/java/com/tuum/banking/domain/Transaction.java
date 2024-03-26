package com.tuum.banking.domain;

import com.tuum.banking.configuration.rabbitmq.RabbitMQConstants;
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
public class Transaction extends BaseEntity implements Aggregate {
	private Long accountId;
	private Long balanceId;
	private BigDecimal amount;
	private TransactionDirection direction;
	private String description;
	private BigDecimal balanceAfterTransaction;
	private String currency;
	private String reference;
	
	@Override
	public String getEventType() {
		return RabbitMQConstants.TRANSACTION_CREATED;
	}
	
	public enum TransactionDirection {
		IN, OUT
	}
}
