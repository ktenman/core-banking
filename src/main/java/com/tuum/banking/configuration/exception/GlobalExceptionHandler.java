package com.helmes.recruitment.formhandler.configuration.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler({WebExchangeBindException.class, MethodArgumentNotValidException.class})
	public ResponseEntity<ApiError> handleValidationExceptions(Exception exception) {
		return handleValidationException(exception);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleAllExceptions(Exception exception) {
		ApiError apiError = ApiError.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.message(exception.getLocalizedMessage())
				.debugMessage("An internal error occurred")
				.build();
		
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleNotFoundException(AccessDeniedException exception) {
		ApiError apiError = ApiError.builder()
				.status(HttpStatus.FORBIDDEN)
				.message(exception.getLocalizedMessage())
				.debugMessage("Access to the requested resource is denied")
				.build();
		
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}
	
	private ResponseEntity<ApiError> handleValidationException(Exception exception) {
		Map<String, String> errors = extractErrors(exception);
		
		ApiError apiError = ApiError.builder()
				.status(HttpStatus.BAD_REQUEST)
				.message("Validation error")
				.debugMessage("One or more fields have an error")
				.validationErrors(errors)
				.build();
		
		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}
	
	private Map<String, String> extractErrors(Exception exception) {
		BindingResult bindingResult = null;
		
		if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
			bindingResult = methodArgumentNotValidException.getBindingResult();
		} else if (exception instanceof WebExchangeBindException webExchangeBindException) {
			bindingResult = webExchangeBindException.getBindingResult();
		}
		
		return Optional.ofNullable(bindingResult)
				.map(BindingResult::getFieldErrors)
				.map(fieldErrors -> fieldErrors.stream()
						.collect(Collectors.toMap(
								FieldError::getField,
								fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid value"),
								(existingValue, newValue) -> existingValue
						)))
				.orElse(Collections.emptyMap());
	}
	
	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ApiError {
		private HttpStatus status;
		private String message;
		private String debugMessage;
		private Map<String, String> validationErrors;
	}
	
}
