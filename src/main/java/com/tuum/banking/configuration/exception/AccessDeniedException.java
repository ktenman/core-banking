package com.tuum.banking.configuration.exception;

public class AccessDeniedException extends RuntimeException {
	
	public AccessDeniedException(String message) {
		super(message);
	}
}
