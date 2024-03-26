package com.tuum.banking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.configuration.rabbitmq.RabbitMQConstants;
import com.tuum.banking.domain.OutboxMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitMQPublisherTest {
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	OutboxMessageService outboxMessageService;
	
	@Mock
	RabbitTemplate rabbitTemplate;
	
	@InjectMocks
	RabbitMQPublisher rabbitMQPublisher;
	
	@Test
	void publishMessages_ShouldPublishPendingMessages() throws JsonProcessingException {
		OutboxMessage message1 = new OutboxMessage();
		message1.setId(1L);
		message1.setEventType(RabbitMQConstants.ACCOUNT_CREATED);
		message1.setPayload("{\"id\": 1}");
		
		OutboxMessage message2 = new OutboxMessage();
		message2.setId(2L);
		message2.setEventType(RabbitMQConstants.TRANSACTION_CREATED);
		message2.setPayload("{\"id\": 2}");
		
		when(outboxMessageService.selectPendingMessages()).thenReturn(List.of(message1, message2));
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\": 1}").thenReturn("{\"id\": 2}");
		
		rabbitMQPublisher.publishMessages();
		
		verify(rabbitTemplate, times(2)).convertAndSend(anyString(), any(Message.class));
		verify(outboxMessageService, times(2)).updateStatus(anyLong(), eq(OutboxMessage.OutboxStatus.SENT), eq(null));
	}
	
	@Test
	void publishMessages_WhenPublishingFails_ShouldUpdateStatusToFailed() throws JsonProcessingException {
		OutboxMessage message = new OutboxMessage();
		message.setId(1L);
		message.setEventType(RabbitMQConstants.ACCOUNT_CREATED);
		message.setPayload("{\"id\": 1}");
		
		when(outboxMessageService.selectPendingMessages()).thenReturn(List.of(message));
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\": 1}");
		doThrow(new RuntimeException("Publishing failed")).when(rabbitTemplate).convertAndSend(anyString(), any(Message.class));
		
		rabbitMQPublisher.publishMessages();
		
		verify(outboxMessageService, times(1)).updateStatus(eq(1L), eq(OutboxMessage.OutboxStatus.FAILED), anyString());
	}
	
	@Test
	void publishMessages_WhenObjectMapperReturnsNull_ShouldSkipPublishing() throws JsonProcessingException {
		OutboxMessage message = new OutboxMessage();
		message.setId(1L);
		message.setEventType("account-created");
		message.setPayload(null);
		
		when(outboxMessageService.selectPendingMessages()).thenReturn(List.of(message));
		when(objectMapper.writeValueAsString(any())).thenReturn(null);
		
		rabbitMQPublisher.publishMessages();
		
		verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Message.class));
		verify(outboxMessageService, never()).updateStatus(anyLong(), any(OutboxMessage.OutboxStatus.class), anyString());
	}
}
