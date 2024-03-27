package com.tuum.banking.configuration;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class CustomPage<T> extends Page<T> {
	public CustomPage(long current, long size) {
		this.current = current;
		this.size = size;
	}
	
	public long getOffset() {
		return (current - 1) * size;
	}
}
