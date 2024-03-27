package com.tuum.banking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tuum.banking.domain.Transaction;
import com.tuum.banking.dto.TransactionDto;
import com.tuum.banking.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.tuum.banking.domain.Transaction.TransactionDirection.IN;
import static com.tuum.banking.domain.Transaction.TransactionDirection.OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
	
	@Mock
	private TransactionService transactionService;
	
	@InjectMocks
	private TransactionController transactionController;
	
	@Test
	void testGetTransactions_Success() {
		Transaction transaction1 = Transaction.builder()
				.accountId(1L)
				.balanceId(1L)
				.amount(BigDecimal.TEN)
				.direction(IN)
				.currency("USD")
				.build();
		Transaction transaction2 = Transaction.builder()
				.accountId(1L)
				.balanceId(1L)
				.amount(BigDecimal.valueOf(123.32))
				.direction(OUT)
				.currency("EUR")
				.build();
		Page<Transaction> page = new Page<>(1, 10);
		page.setRecords(List.of(transaction1, transaction2));
		page.setTotal(2);
		
		when(transactionService.getTransactions(eq(1L), any())).thenReturn(page);
		
		IPage<TransactionDto> result = transactionController.getTransactions(1L, page.getCurrent(), page.getSize());
		
		assertThat(result.getRecords())
				.hasSize(2)
				.satisfiesExactlyInAnyOrder(
						transactionDto -> {
							assertThat(transactionDto.getAmount()).isEqualTo(BigDecimal.TEN);
							assertThat(transactionDto.getDirection()).isEqualTo(IN);
							assertThat(transactionDto.getCurrency()).isEqualTo("USD");
						},
						transactionDto -> {
							assertThat(transactionDto.getAmount()).isEqualTo(BigDecimal.valueOf(123.32));
							assertThat(transactionDto.getDirection()).isEqualTo(OUT);
							assertThat(transactionDto.getCurrency()).isEqualTo("EUR");
						}
				);
	}
}
