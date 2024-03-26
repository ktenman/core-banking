package com.tuum.banking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tuum.banking.configuration.logging.Loggable;
import com.tuum.banking.converter.TransactionConverter;
import com.tuum.banking.domain.Transaction;
import com.tuum.banking.dto.CreateTransactionRequest;
import com.tuum.banking.dto.TransactionDto;
import com.tuum.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	public IPage<TransactionDto> getTransactions(
			@Parameter(example = "1") @PathVariable Long accountId,
			@RequestParam(defaultValue = "1") long pageNumber,
			@RequestParam(defaultValue = "10") long pageSize
	) {
		IPage<Transaction> transactionsPage = transactionService.getTransactions(accountId, Page.of(pageNumber, pageSize));
		return transactionsPage.convert(TransactionConverter::toDto);
	}
	
}
