package com.tuum.banking.service;

import com.tuum.banking.domain.Account;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.domain.Transaction;
import com.tuum.banking.domain.Transaction.TransactionDirection;
import com.tuum.banking.dto.CreateTransactionRequest;
import com.tuum.banking.mapper.TransactionMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
	
	@Mock
	private TransactionMapper transactionMapper;
	
	@Mock
	private RabbitMQPublisher rabbitMQPublisher;
	
	@Mock
	private AccountService accountService;
	
	@Mock
	private BalanceService balanceService;
	
	@InjectMocks
	private TransactionService transactionService;
	
	@ParameterizedTest
	@EnumSource(TransactionDirection.class)
	void testCreateTransaction_Success(TransactionDirection direction) {
		Balance balance = new Balance();
		balance.setId(1L);
		balance.setAvailableAmount(BigDecimal.valueOf(100));
		balance.setCurrency("USD");
		balance.setAccountId(1L);
		
		Account account = new Account();
		account.setId(1L);
		account.setBalances(List.of(balance));
		
		CreateTransactionRequest createTransactionRequest = CreateTransactionRequest.builder()
				.currency("USD")
				.amount(BigDecimal.TEN)
				.direction(direction)
				.accountId(1L)
				.description("desc")
				.build();
		
		when(accountService.getAccountById(1L)).thenReturn(account);
		when(balanceService.getBalanceByAccountIdAndCurrency(1L, "USD")).thenReturn(balance);
		when(balanceService.calculateNewBalance(balance.getAvailableAmount(), createTransactionRequest)).thenReturn(BigDecimal.valueOf(110));
		
		Transaction result = transactionService.createTransaction(createTransactionRequest);
		
		assertThat(result.getAmount()).isEqualTo(BigDecimal.TEN);
		assertThat(result.getCurrency()).isEqualTo("USD");
		assertThat(result.getDirection()).isEqualTo(direction);
		verify(transactionMapper, times(1)).insert(any(Transaction.class));
		verify(balanceService, times(1)).updateBalance(balance, BigDecimal.valueOf(110));
		verify(rabbitMQPublisher, times(1)).publishTransactionCreated(any(Transaction.class));
	}
	
}
