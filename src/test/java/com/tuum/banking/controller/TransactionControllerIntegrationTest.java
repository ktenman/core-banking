package com.tuum.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.IntegrationTest;
import com.tuum.banking.configuration.exception.GlobalExceptionHandler.ApiError;
import com.tuum.banking.configuration.rabbitmq.RabbitMQConstants;
import com.tuum.banking.domain.Account;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.domain.Transaction;
import com.tuum.banking.dto.CreateTransactionRequest;
import com.tuum.banking.dto.TransactionDto;
import com.tuum.banking.mapper.AccountMapper;
import com.tuum.banking.mapper.BalanceMapper;
import com.tuum.banking.mapper.TransactionMapper;
import com.tuum.banking.util.TestFileUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tuum.banking.domain.Transaction.TransactionDirection.IN;
import static com.tuum.banking.domain.Transaction.TransactionDirection.OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class TransactionControllerIntegrationTest {
	
	private static final String TRANSACTION_API_ENDPOINT = "/api/transactions";
	private static final String ORIGINATOR = "core-banking";
	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	AccountMapper accountMapper;
	@Autowired
	BalanceMapper balanceMapper;
	@Autowired
	TransactionMapper transactionMapper;
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	CreateTransactionRequest createTransactionRequest = CreateTransactionRequest.builder()
			.accountId(999L)
			.amount(BigDecimal.valueOf(100))
			.currency("EUR")
			.direction(OUT)
			.description("Test transaction")
			.build();
	
	private Account createAccountWithBalance(BigDecimal availableAmount) {
		Account account = new Account();
		account.setCustomerId(1L);
		account.setCountryCode("USA");
		account.setReference(UUID.randomUUID().toString());
		accountMapper.insert(account);
		
		Balance balance = new Balance();
		balance.setAccountId(account.getId());
		balance.setCurrency("EUR");
		balance.setAvailableAmount(availableAmount);
		balanceMapper.insert(balance);
		
		return account;
	}
	
	@Nested
	@DisplayName("POST /api/transactions")
	class CreateTransaction {
		
		@Test
		void createTransaction_withValidRequest_returnsCreatedTransaction() throws Exception {
			Account account = createAccountWithBalance(BigDecimal.valueOf(1000));
			createTransactionRequest.setAccountId(account.getId());
			
			ResultActions resultActions = mockMvc.perform(post(TRANSACTION_API_ENDPOINT)
							.contentType(APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(createTransactionRequest)))
					.andExpect(status().isOk());
			
			TransactionDto response = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), TransactionDto.class);
			assertThat(response.getAccountId()).isEqualTo(account.getId());
			assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
			assertThat(response.getCurrency()).isEqualTo("EUR");
			assertThat(response.getDirection()).isEqualTo(OUT);
			assertThat(response.getDescription()).isEqualTo("Test transaction");
			assertThat(response.getBalanceAfterTransaction()).isEqualByComparingTo(BigDecimal.valueOf(900));
			assertRabbitMQMessagePublished(response);
		}
		
		@SneakyThrows(IOException.class)
		private void assertRabbitMQMessagePublished(TransactionDto response) {
			Message message = rabbitTemplate.receive(RabbitMQConstants.TRANSACTION_CREATED);
			assertThat(message).isNotNull();
			assertThat(message.getMessageProperties()).isNotNull();
			Map<String, Object> headers = message.getMessageProperties().getHeaders();
			assertThat(headers).hasSize(3).containsEntry("originator", ORIGINATOR).containsKey("createdAt").containsKey("uuid");
			assertThat(headers.get("createdAt")).isNotNull();
			assertThat(headers.get("uuid")).isNotNull();
			Transaction transaction = objectMapper.readValue(message.getBody(), Transaction.class);
			assertThat(transaction).isNotNull()
					.satisfies(t -> {
						assertThat(t.getAccountId()).isEqualTo(response.getAccountId());
						assertThat(t.getAmount()).isEqualByComparingTo(response.getAmount());
						assertThat(t.getCurrency()).isEqualTo(response.getCurrency());
						assertThat(t.getDirection()).isEqualTo(response.getDirection());
						assertThat(t.getDescription()).isEqualTo(response.getDescription());
						assertThat(t.getBalanceAfterTransaction()).isEqualByComparingTo(response.getBalanceAfterTransaction());
						assertThat(t.getReference()).isNotEmpty();
						assertThat(t.getId()).isNotNull();
						assertThat(t.getCreatedAt()).isNotNull();
						assertThat(t.getUpdatedAt()).isNotNull();
					});
		}
		
		@Test
		void createTransaction_withInvalidAccountId_returnsNotFoundWithErrorMessage() throws Exception {
			ResultActions resultActions = mockMvc.perform(post(TRANSACTION_API_ENDPOINT)
							.contentType(APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(createTransactionRequest)))
					.andExpect(status().isNotFound());
			
			ApiError apiError = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
			
			assertThat(apiError.getMessage()).isEqualTo("Account not found");
			assertThat(apiError.getDebugMessage()).isEqualTo("Account with ID 999 does not exist");
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		}
		
		@Test
		void createTransaction_withInsufficientFunds_returnsBadRequestWithErrorMessage() throws Exception {
			Account account = createAccountWithBalance(BigDecimal.valueOf(50));
			createTransactionRequest.setAccountId(account.getId());
			
			ResultActions resultActions = mockMvc.perform(post(TRANSACTION_API_ENDPOINT)
							.contentType(APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(createTransactionRequest)))
					.andExpect(status().isBadRequest());
			
			ApiError apiError = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
			assertThat(apiError.getMessage()).isEqualTo("Insufficient funds");
			assertThat(apiError.getDebugMessage()).isEqualTo("Insufficient funds for account ID: %s and currency: EUR", account.getId());
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
		
		@Test
		void createTransaction_withInvalidCurrency_returnsBadRequestWithErrorMessage() throws Exception {
			String invalidCurrencyTransactionJson = TestFileUtil.readFileAsString("invalid_currency_transaction_request.json");
			
			ResultActions resultActions = mockMvc.perform(post(TRANSACTION_API_ENDPOINT)
							.contentType(APPLICATION_JSON)
							.content(invalidCurrencyTransactionJson))
					.andExpect(status().isBadRequest());
			
			ApiError apiError = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
			
			assertThat(apiError.getMessage()).isEqualTo("Invalid currency");
			assertThat(apiError.getDebugMessage()).isEqualTo("Invalid currency: INVALID");
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
		
		@Test
		void createTransaction_withNegativeAmount_returnsBadRequestWithErrorMessage() throws Exception {
			createTransactionRequest.setAmount(BigDecimal.valueOf(-100));
			
			ResultActions resultActions = mockMvc.perform(post(TRANSACTION_API_ENDPOINT)
							.contentType(APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(createTransactionRequest)))
					.andExpect(status().isBadRequest());
			
			ApiError apiError = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
			
			assertThat(apiError.getMessage()).isEqualTo("Validation error");
			assertThat(apiError.getDebugMessage()).isEqualTo("One or more fields have an error");
			assertThat(apiError.getValidationErrors()).containsEntry("amount", "Amount must be positive");
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
		
		@Test
		void createTransaction_withMissingDescription_returnsBadRequestWithErrorMessage() throws Exception {
			createTransactionRequest.setDescription("");
			
			ResultActions resultActions = mockMvc.perform(post(TRANSACTION_API_ENDPOINT)
							.contentType(APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(createTransactionRequest)))
					.andExpect(status().isBadRequest());
			
			ApiError apiError = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
			
			assertThat(apiError.getMessage()).isEqualTo("Validation error");
			assertThat(apiError.getDebugMessage()).isEqualTo("One or more fields have an error");
			assertThat(apiError.getValidationErrors()).containsEntry("description", "Description is required");
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
		
		@Test
		void createTransaction_withInvalidTransactionData_returnsMultipleValidationErrors() throws Exception {
			ResultActions resultActions = mockMvc.perform(post(TRANSACTION_API_ENDPOINT)
					.contentType(APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
			
			ApiError apiError = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
			assertThat(apiError.getMessage()).isEqualTo("Validation error");
			assertThat(apiError.getDebugMessage()).isEqualTo("One or more fields have an error");
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(apiError.getValidationErrors()).hasSize(5)
					.containsEntry("accountId", "Account ID is required")
					.containsEntry("amount", "Amount is required")
					.containsEntry("currency", "Currency is required")
					.containsEntry("description", "Description is required")
					.containsEntry("direction", "Direction is required");
		}
	}
	
	@Nested
	@DisplayName("GET /api/transactions/{accountId}")
	class GetTransactions {
		
		@Test
		void getTransactions_withExistingAccountId_returnsTransactionList() throws Exception {
			Account account = createAccountWithBalance(BigDecimal.valueOf(1000));
			transactionMapper.insert(Transaction.builder()
					.accountId(account.getId())
					.balanceId(1L)
					.amount(BigDecimal.valueOf(100))
					.currency("EUR")
					.direction(OUT)
					.description("Test transaction 1")
					.balanceAfterTransaction(BigDecimal.valueOf(800))
					.reference(UUID.randomUUID().toString())
					.build());
			transactionMapper.insert(Transaction.builder()
					.accountId(account.getId())
					.balanceId(1L)
					.amount(BigDecimal.valueOf(200))
					.currency("EUR")
					.direction(IN)
					.description("Test transaction 2")
					.balanceAfterTransaction(BigDecimal.valueOf(1000))
					.reference(UUID.randomUUID().toString())
					.build());
			
			ResultActions resultActions = mockMvc.perform(get(TRANSACTION_API_ENDPOINT + "/{accountId}", account.getId()))
					.andExpect(status().isOk());
			
			List<TransactionDto> response = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(),
					objectMapper.getTypeFactory().constructCollectionType(List.class, TransactionDto.class));
			
			assertThat(response).hasSize(2);
			assertThat(response).extracting(TransactionDto::getAccountId).containsOnly(account.getId());
			assertThat(response).extracting(TransactionDto::getAmount)
					.satisfiesExactlyInAnyOrder(
							amount -> assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(100)),
							amount -> assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(200))
					);
			assertThat(response).extracting(TransactionDto::getCurrency).containsOnly("EUR");
			assertThat(response).extracting(TransactionDto::getDirection).containsExactlyInAnyOrder(OUT, IN);
			assertThat(response).extracting(TransactionDto::getDescription)
					.containsExactlyInAnyOrder("Test transaction 1", "Test transaction 2");
		}
		
		@Test
		void getTransactions_withNonExistingAccountId_returnsEmptyList() throws Exception {
			ResultActions resultActions = mockMvc.perform(get(TRANSACTION_API_ENDPOINT + "/{accountId}", 999L))
					.andExpect(status().isOk());
			
			List<TransactionDto> response = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(),
					objectMapper.getTypeFactory().constructCollectionType(List.class, TransactionDto.class));
			
			assertThat(response).isEmpty();
		}
	}
}
