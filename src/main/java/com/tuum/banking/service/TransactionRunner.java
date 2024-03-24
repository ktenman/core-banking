package com.tuum.banking.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class TransactionRunner {
	@Transactional
	public <T> T execute(Supplier<T> supplier) {
		return supplier.get();
	}
}
