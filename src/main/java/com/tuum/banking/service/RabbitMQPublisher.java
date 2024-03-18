package com.tuum.banking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.configuration.exception.JsonConversionException;
import com.tuum.banking.configuration.rabbitmq.RabbitMQConstants;
import com.tuum.banking.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQPublisher {
	
	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;
	@Value("${spring.application.name}")
	private String originator;
	
	@Async
	public void publishAccountCreated(AccountDto account) {
		publish(RabbitMQConstants.ACCOUNT_CREATED, account);
	}
	
	private void publish(String queue, Object object) {
		log.debug("Publishing message to queue [queue: {}]", queue);
		String messageBody;
		try {
			messageBody = objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			log.error("Error while converting object to JSON", e);
			throw new JsonConversionException("Failed to convert object to JSON", e);
		}
		String uuid = Optional.ofNullable(MDC.get("transactionId"))
				.map(id -> id.replace("[", "").replace("]", "").trim())
				.orElse(UUID.randomUUID().toString());
		Message message = MessageBuilder.withBody(messageBody.getBytes())
				.setContentType(MessageProperties.CONTENT_TYPE_JSON)
				.setHeader("createdAt", Instant.now().toString())
				.setHeader("uuid", uuid)
				.setHeader("originator", originator)
				.build();
		rabbitTemplate.convertAndSend(queue, message);
		log.info("Published message to queue [queue: {}]", queue);
	}
	
}
