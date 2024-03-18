package com.tuum.banking.service.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.function.UnaryOperator;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class LockService {
	
	private static final UnaryOperator<String> LOCK_KEY = identifier -> "lock:" + identifier;
	private final StringRedisTemplate redisTemplate;
	
	public void acquireLock(String identifier) {
		Boolean acquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY.apply(identifier), "locked", 60, SECONDS);
		if (!Boolean.TRUE.equals(acquired)) {
			throw new IllegalStateException("Unable to acquire lock for identifier: " + identifier);
		}
	}
	
	public void releaseLock(String identifier) {
		redisTemplate.delete(LOCK_KEY.apply(identifier));
	}
	
}
