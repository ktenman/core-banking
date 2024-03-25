package com.tuum.banking.converter;

import com.tuum.banking.domain.Transaction;
import com.tuum.banking.dto.CreateTransactionRequest;
import com.tuum.banking.dto.TransactionDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionConverter {
	public static Transaction toDomain(CreateTransactionRequest createTransactionRequest) {
		Transaction transaction = new Transaction();
		transaction.setCurrency(createTransactionRequest.getCurrency());
		transaction.setAccountId(createTransactionRequest.getAccountId());
		transaction.setAmount(createTransactionRequest.getAmount());
		transaction.setDirection(createTransactionRequest.getDirection());
		transaction.setDescription(createTransactionRequest.getDescription());
		return transaction;
	}
	
	public static TransactionDto toDto(Transaction transaction) {
		TransactionDto transactionDto = new TransactionDto();
		transactionDto.setTransactionId(transaction.getId());
		transactionDto.setAccountId(transaction.getAccountId());
		transactionDto.setAmount(transaction.getAmount());
		transactionDto.setCurrency(transaction.getCurrency());
		transactionDto.setDirection(transaction.getDirection());
		transactionDto.setDescription(transaction.getDescription());
		transactionDto.setBalanceAfterTransaction(transaction.getBalanceAfterTransaction());
		return transactionDto;
	}
}
