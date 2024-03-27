package com.tuum.banking.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomPage<T> {
	private long current = 1;
	private long size;
	private List<T> records;
	
	public CustomPage(long current, long size) {
		this.current = current;
		this.size = size;
	}
	
	public CustomPage(long size) {
		this.size = size;
	}
	
	public long getOffset() {
		return (current - 1) * size;
	}
}
