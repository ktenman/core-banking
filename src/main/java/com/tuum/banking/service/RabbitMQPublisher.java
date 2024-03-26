package com.tuum.banking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.configuration.exception.JsonConversionException;
import com.tuum.banking.domain.OutboxMessage;
import com.tuum.banking.domain.OutboxMessage.OutboxStatus;
import com.tuum.banking.service.lock.Lock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
	private final OutboxMessageService outboxMessageService;
	
	@Scheduled(fixedDelay = 1000)
	@Lock(key = "'publishMessages'")
	public void publishMessages() {
		List<OutboxMessage> messages = outboxMessageService.selectPendingMessages();
		for (OutboxMessage message : messages) {
			try {
				publish(message.getEventType(), message.getPayload());
				outboxMessageService.updateStatus(message.getId(), OutboxStatus.SENT, null);
			} catch (Exception e) {
				log.error("Failed to publish message with ID: {}", message.getId(), e);
				outboxMessageService.updateStatus(message.getId(), OutboxStatus.FAILED, e.getMessage());
			}
		}
	}
	
	private void publish(String queue, Object object) {
		log.debug("Publishing message to queue [queue: {}]", queue);
		String messageBody;
		try {
			messageBody = objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			log.error("Error while converting object to JSON", e);
			throw new JsonConversionException(e);
		}
		if (messageBody == null) {
			log.warn("Skipping message publishing due to null message body");
			return;
		}
		Message message = MessageBuilder.withBody(messageBody.getBytes())
				.setContentType(MessageProperties.CONTENT_TYPE_JSON)
				.copyHeaders(createHeaders())
				.build();
		rabbitTemplate.convertAndSend(queue, message);
		String headersJson;
		try {
			headersJson = objectMapper.writeValueAsString(createHeaders());
		} catch (JsonProcessingException e) {
			throw new JsonConversionException(e);
		}
		log.info("Published message to queue [{}], {} {}", queue, headersJson, messageBody);
	}
	
	private Map<String, Object> createHeaders() {
		return Map.of(
				"createdAt", Instant.now().toString(),
				"uuid", UUID.randomUUID().toString(),
				"originator", Optional.ofNullable(originator).orElse("")
		);
	}
	
}
