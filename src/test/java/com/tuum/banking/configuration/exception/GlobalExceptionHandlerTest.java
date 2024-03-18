package com.tuum.banking.configuration.exception;

import com.tuum.banking.configuration.exception.GlobalExceptionHandler.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

class GlobalExceptionHandlerTest {
	GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
	
	@Test
	void testHandleAllExceptions_shouldReturnInternalServerErrorResponseWithApiError() {
		String exceptionMessage = "Test Exception";
		String expectedDebugMessage = "An internal error occurred";
		
		ResponseEntity<ApiError> response = globalExceptionHandler.handleAllExceptions(new Exception(exceptionMessage));
		
		assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isEqualTo(exceptionMessage);
		assertThat(response.getBody().getDebugMessage()).isEqualTo(expectedDebugMessage);
	}
}
