// src/main/java/com/tuum/banking/service/OutboxMessageService.java
package com.tuum.banking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.domain.Aggregate;
import com.tuum.banking.domain.OutboxMessage;
import com.tuum.banking.mapper.OutboxMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxMessageService {
	private final OutboxMessageMapper outboxMessageMapper;
	private final ObjectMapper objectMapper;
	
	public <T extends Aggregate> void createOutboxMessage(T aggregate) {
		OutboxMessage outboxMessage = OutboxMessage.builder()
				.aggregateType(aggregate.getClass().getSimpleName())
				.aggregateId(aggregate.getId())
				.eventType(aggregate.getEventType())
				.payload(convertToJson(aggregate))
				.status(OutboxMessage.OutboxStatus.PENDING)
				.build();
		
		outboxMessageMapper.insert(outboxMessage);
	}
	
	@SneakyThrows(JsonProcessingException.class)
	private String convertToJson(Object object) {
		return objectMapper.writeValueAsString(object);
	}
	
	public List<OutboxMessage> selectPendingMessages() {
		return outboxMessageMapper.selectPendingMessages();
	}
	
	public void updateStatus(Long id, OutboxMessage.OutboxStatus status, String errorMessage) {
		outboxMessageMapper.updateStatus(id, status, errorMessage);
	}
	
}
