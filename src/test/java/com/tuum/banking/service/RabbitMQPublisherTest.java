package com.tuum.banking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.configuration.exception.JsonConversionException;
import com.tuum.banking.domain.Account;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class RabbitMQPublisherTest {
	
	@Mock
	ObjectMapper objectMapper;
	
	@InjectMocks
	RabbitMQPublisher rabbitMQPublisher;
	
	@Test
	@SneakyThrows
	void publishAccountCreated_WhenJsonProcessingExceptionThrown_ShouldThrowJsonConversionException() {
		Mockito.when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
		
		Throwable thrown = catchThrowable(() -> rabbitMQPublisher.publishAccountCreated(new Account()));
		
		assertThat(thrown).isInstanceOf(JsonConversionException.class)
				.hasMessage("Failed to convert object to JSON");
	}
}
