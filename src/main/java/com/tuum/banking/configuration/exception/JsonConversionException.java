package com.tuum.banking.configuration.exception;

public class JsonConversionException extends RuntimeException {
	public JsonConversionException(Throwable cause) {
		super("Failed to convert object to JSON", cause);
	}
}
