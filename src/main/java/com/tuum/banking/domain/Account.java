package com.tuum.banking.domain;

import com.tuum.banking.configuration.rabbitmq.RabbitMQConstants;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Account extends BaseEntity implements Aggregate {
	private Long customerId;
	private String countryCode;
	private List<Balance> balances;
	private String reference;
	
	@Override
	public String getEventType() {
		return RabbitMQConstants.ACCOUNT_CREATED;
	}
}
