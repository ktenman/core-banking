package com.tuum.banking.configuration.exception;

public class InvalidAccountException extends RuntimeException {
	
	public InvalidAccountException(Long accountId) {
		super(String.format("Account with ID %s does not exist", accountId));
	}
}
