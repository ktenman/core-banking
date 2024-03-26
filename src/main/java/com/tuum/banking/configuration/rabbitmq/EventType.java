package com.tuum.banking.configuration.rabbitmq;

import lombok.Getter;

@Getter
public enum EventType {
	ACCOUNT_CREATED("account-created"),
	TRANSACTION_CREATED("transaction-created");
	
	private final String value;
	
	EventType(String value) {
		this.value = value;
	}
}
