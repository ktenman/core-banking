package com.helmes.recruitment.formhandler.configuration.exception;

public class AccessDeniedException extends RuntimeException {
	
	public AccessDeniedException(String message) {
		super(message);
	}
}
