package com.tuum.banking.configuration.rabbitmq;

import lombok.Getter;

@Getter
public enum EventType {
	ACCOUNT_CREATED(RabbitMQConstants.ACCOUNT_CREATED),
	TRANSACTION_CREATED(RabbitMQConstants.TRANSACTION_CREATED);
	
	private final String value;
	
	EventType(String value) {
		this.value = value;
	}
}
