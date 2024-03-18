package com.tuum.banking.configuration.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	
	@Bean
	public Queue accountCreatedQueue() {
		return new Queue(RabbitMQConstants.ACCOUNT_CREATED, true);
	}
	
}

