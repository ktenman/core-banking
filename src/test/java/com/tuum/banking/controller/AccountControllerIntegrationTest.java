package com.tuum.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.IntegrationTest;
import com.tuum.banking.configuration.exception.GlobalExceptionHandler.ApiError;
import com.tuum.banking.configuration.rabbitmq.RabbitMQConstants;
import com.tuum.banking.domain.Account;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.dto.AccountDto;
import com.tuum.banking.dto.AccountDto.BalanceResponseDto;
import com.tuum.banking.dto.CreateAccountRequest;
import com.tuum.banking.dto.CreateAccountRequest.BalanceRequestDto;
import com.tuum.banking.mapper.AccountMapper;
import com.tuum.banking.mapper.BalanceMapper;
import com.tuum.banking.util.TestFileUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.tuum.banking.dto.CreateAccountRequest.BalanceCurrency.EUR;
import static com.tuum.banking.dto.CreateAccountRequest.BalanceCurrency.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AccountControllerIntegrationTest {
	
	private static final String ACCOUNT_API_ENDPOINT = "/api/accounts";
	private static final String ORIGINATOR = "core-banking";
	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	RabbitTemplate rabbitTemplate;
	@Autowired
	AccountMapper accountMapper;
	@Autowired
	BalanceMapper balanceMapper;
	
	private Account createAccountWithBalances() {
		Account account = new Account();
		account.setCustomerId(1L);
		account.setCountryCode("USA");
		accountMapper.insert(account);
		
		Balance balance1 = new Balance();
		balance1.setAccountId(account.getId());
		balance1.setCurrency("USD");
		balance1.setAvailableAmount(BigDecimal.valueOf(100));
		balanceMapper.insert(balance1);
		
		Balance balance2 = new Balance();
		balance2.setAccountId(account.getId());
		balance2.setCurrency("EUR");
		balance2.setAvailableAmount(BigDecimal.valueOf(200));
		balanceMapper.insert(balance2);
		
		return account;
	}
	
	private void assertAccountCreatedWithBalances(AccountDto accountDto, List<BalanceRequestDto> balanceRequests) {
		assertThat(accountDto.getAccountId()).isEqualTo(1L);
		assertThat(accountDto.getCustomerId()).isEqualTo(1L);
		assertThat(accountDto.getBalances()).hasSize(balanceRequests.size())
				.satisfiesExactlyInAnyOrder(assertBalanceResponsesForRequests(balanceRequests));
	}
	
	@SuppressWarnings("unchecked")
	private Consumer<? super BalanceResponseDto>[] assertBalanceResponsesForRequests(List<BalanceRequestDto> balanceRequests) {
		return balanceRequests.stream().map(this::assertBalanceResponseForRequest).toArray(Consumer[]::new);
	}
	
	private Consumer<BalanceResponseDto> assertBalanceResponseForRequest(BalanceRequestDto balanceRequest) {
		return balanceResponseDto -> {
			assertThat(balanceResponseDto.getCurrency()).isEqualTo(balanceRequest.getCurrency().name());
			assertThat(balanceResponseDto.getAvailableAmount()).isEqualByComparingTo(balanceRequest.getAvailableAmount());
		};
	}
	
	@SneakyThrows(IOException.class)
	private void assertRabbitMQMessagePublished(AccountDto response) {
		Message message = rabbitTemplate.receive(RabbitMQConstants.ACCOUNT_CREATED);
		assertThat(message).isNotNull();
		assertThat(message.getMessageProperties()).isNotNull();
		Map<String, Object> headers = message.getMessageProperties().getHeaders();
		assertThat(headers).hasSize(3).containsEntry("originator", ORIGINATOR).containsKey("createdAt").containsKey("uuid");
		assertThat(headers.get("createdAt")).isNotNull();
		assertThat(headers.get("uuid")).isNotNull();
		AccountDto receivedAccountDto = objectMapper.readValue(message.getBody(), AccountDto.class);
		assertThat(receivedAccountDto).isEqualTo(response);
	}
	
	@SneakyThrows(IOException.class)
	private ApiError apiError(ResultActions resultActions) {
		return objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
	}
	
	@Nested
	@DisplayName("POST /api/accounts")
	class CreateAccount {
		
		private static Stream<List<BalanceRequestDto>> provideBalanceRequestData() {
			return Stream.of(
					List.of(new BalanceRequestDto(USD, BigDecimal.valueOf(100)), new BalanceRequestDto(EUR, BigDecimal.valueOf(200))),
					List.of(new BalanceRequestDto(EUR, BigDecimal.valueOf(300)))
			);
		}
		
		@ParameterizedTest(name = "Should create account with balances: {0}")
		@MethodSource("provideBalanceRequestData")
		void createAccount_withValidRequest_returnsCreatedAccountWithBalances(List<BalanceRequestDto> balanceRequests) throws Exception {
			var accountRequestDto = CreateAccountRequest.builder()
					.customerId(1L)
					.country("USA")
					.balances(balanceRequests)
					.build();
			
			ResultActions resultActions = mockMvc.perform(post(ACCOUNT_API_ENDPOINT)
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(accountRequestDto))).andExpect(status().isOk());
			
			AccountDto response = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), AccountDto.class);
			assertAccountCreatedWithBalances(response, balanceRequests);
			assertRabbitMQMessagePublished(response);
		}
		
		@Test
		void createAccount_withInvalidCurrency_returnsBadRequestWithErrorMessage() throws Exception {
			String invalidAccountRequestJson = TestFileUtil.readFileAsString("invalid_currency_account_request.json");
			
			ResultActions resultActions = mockMvc.perform(post(ACCOUNT_API_ENDPOINT)
					.contentType(APPLICATION_JSON).content(invalidAccountRequestJson)).andExpect(status().isBadRequest());
			
			ApiError apiError = apiError(resultActions);
			assertThat(apiError.getMessage()).isEqualTo("Invalid request payload");
			assertThat(apiError.getDebugMessage()).contains("JSON parse error", "not one of the values accepted for Enum class: [EUR, GBP, USD]");
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
		
		@Test
		void createAccount_withInvalidAccountData_returnsMultipleValidationErrors() throws Exception {
			ResultActions resultActions = mockMvc.perform(post(ACCOUNT_API_ENDPOINT)
					.contentType(APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
			
			ApiError apiError = apiError(resultActions);
			assertThat(apiError.getMessage()).isEqualTo("Validation error");
			assertThat(apiError.getDebugMessage()).isEqualTo("One or more fields have an error");
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(apiError.getValidationErrors()).hasSize(3)
					.containsEntry("balances", "At least one balance is required")
					.containsEntry("customerId", "Customer ID is required")
					.containsEntry("country", "Country is required");
		}
	}
	
	@Nested
	@DisplayName("GET /api/accounts/{accountId}")
	class GetAccount {
		
		@Test
		void getAccount_withExistingAccountId_returnsAccountWithBalances() throws Exception {
			Account account = createAccountWithBalances();
			
			ResultActions resultActions = mockMvc.perform(get(ACCOUNT_API_ENDPOINT + "/{accountId}", account.getId()))
					.andExpect(status().isOk());
			
			AccountDto response = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), AccountDto.class);
			assertThat(response.getAccountId()).isEqualTo(1L);
			assertThat(response.getCustomerId()).isEqualTo(1L);
			assertAccountCreatedWithBalances(response, List.of(
					new BalanceRequestDto(USD, BigDecimal.valueOf(100)),
					new BalanceRequestDto(EUR, BigDecimal.valueOf(200)))
			);
		}
		
		@Test
		void getAccount_withNonExistingAccountId_returnsNotFoundWithErrorMessage() throws Exception {
			ResultActions resultActions = mockMvc.perform(get(ACCOUNT_API_ENDPOINT + "/{accountId}", 999L))
					.andExpect(status().isNotFound());
			
			ApiError apiError = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
			assertThat(apiError.getMessage()).isEqualTo("Account not found");
			assertThat(apiError.getDebugMessage()).isEqualTo("Account with ID 999 does not exist");
			assertThat(apiError.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		}
	}
}
