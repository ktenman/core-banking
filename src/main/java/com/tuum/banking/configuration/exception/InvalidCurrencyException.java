package com.tuum.banking.configuration.exception;

public class InvalidCurrencyException extends RuntimeException {
	public InvalidCurrencyException(String message) {
		super(message);
	}
}
