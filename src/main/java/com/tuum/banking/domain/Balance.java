package com.tuum.banking.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Balance extends BaseDomain {
	private Long accountId;
	private String currency;
	private BigDecimal availableAmount;
}
