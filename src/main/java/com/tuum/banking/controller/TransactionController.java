package com.tuum.banking.controller;

import com.tuum.banking.configuration.logging.Loggable;
import com.tuum.banking.converter.TransactionConverter;
import com.tuum.banking.domain.Transaction;
import com.tuum.banking.dto.CreateTransactionRequest;
import com.tuum.banking.dto.TransactionDto;
import com.tuum.banking.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
	private final TransactionService transactionService;
	
	@Loggable
	@PostMapping
	public TransactionDto createTransaction(@Valid @RequestBody CreateTransactionRequest createTransactionRequest) {
		Transaction transaction = transactionService.createTransaction(createTransactionRequest);
		return TransactionConverter.toDto(transaction);
	}
	
	@Loggable
	@GetMapping("/{accountId}")
	public List<TransactionDto> getTransactions(@PathVariable Long accountId) {
		List<Transaction> transactions = transactionService.getTransactions(accountId);
		return transactions.stream()
				.map(TransactionConverter::toDto)
				.toList();
	}
	
}
