package com.tuum.banking.configuration.rabbitmq;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class RabbitMQConstants {
	public static final String ACCOUNT_CREATED = "account-created";
	public static final String TRANSACTION_CREATED = "transaction-created";
}
