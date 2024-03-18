package com.tuum.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.IntegrationTest;
import com.tuum.banking.configuration.exception.GlobalExceptionHandler.ApiError;
import com.tuum.banking.dto.AccountDto;
import com.tuum.banking.dto.CreateAccountRequest;
import com.tuum.banking.dto.CreateAccountRequest.BalanceRequestDto;
import com.tuum.banking.util.TestFileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

import static com.tuum.banking.dto.CreateAccountRequest.BalanceCurrency.EUR;
import static com.tuum.banking.dto.CreateAccountRequest.BalanceCurrency.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AccountControllerIntegrationTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	void createAccount_withValidRequest_returnsCreatedAccountWithBalances() throws Exception {
		
		var accountRequestDto = CreateAccountRequest.builder()
				.customerId(1L)
				.country("USA")
				.balances(List.of(
						new BalanceRequestDto(USD, BigDecimal.valueOf(100)),
						new BalanceRequestDto(EUR, BigDecimal.valueOf(200))))
				.build();
		
		String contentAsString = mockMvc.perform(post("/api/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(accountRequestDto)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		
		AccountDto response = objectMapper.readValue(contentAsString, AccountDto.class);
		
		assertThat(response.getAccountId()).isEqualTo(1L);
		assertThat(response.getCustomerId()).isEqualTo(1L);
		assertThat(response.getBalances()).hasSize(2)
				.satisfiesExactlyInAnyOrder(
						balanceResponseDto -> {
							assertThat(balanceResponseDto.getBalanceId()).isEqualTo(1L);
							assertThat(balanceResponseDto.getCurrency()).isEqualTo("USD");
							assertThat(balanceResponseDto.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(100));
						},
						balanceResponseDto -> {
							assertThat(balanceResponseDto.getBalanceId()).isEqualTo(2L);
							assertThat(balanceResponseDto.getCurrency()).isEqualTo("EUR");
							assertThat(balanceResponseDto.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(200));
						}
				);
	}
	
	@Test
	void createAccount_withInvalidCurrency_returnsBadRequestWithErrorMessage() throws Exception {
		String invalidAccountRequestJson = TestFileUtil.readFileAsString("invalid_currency_account_request.json");
		
		ResultActions resultActions = mockMvc.perform(post("/api/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidAccountRequestJson))
				.andExpect(status().isBadRequest());
		
		ApiError apiError = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ApiError.class);
		assertThat(apiError.getMessage()).isEqualTo("Invalid request payload");
		assertThat(apiError.getDebugMessage()).isEqualTo("JSON parse error: Cannot deserialize value of type " +
				"`com.tuum.banking.dto.CreateAccountRequest$BalanceCurrency` from String \"INV\": " +
				"not one of the values accepted for Enum class: [EUR, GBP, USD]");
		assertThat(apiError.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
}
