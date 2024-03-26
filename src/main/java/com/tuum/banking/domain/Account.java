package com.tuum.banking.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Account extends BaseDomain implements Aggregate {
	private Long customerId;
	private String countryCode;
	private List<Balance> balances;
	private String reference;
}
