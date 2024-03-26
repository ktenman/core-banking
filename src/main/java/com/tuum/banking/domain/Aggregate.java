package com.tuum.banking.domain;

public interface Aggregate {
	Long getId();
	
	String getEventType();
}
