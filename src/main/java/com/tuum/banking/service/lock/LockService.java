package com.tuum.banking.service.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LockService {
	static final String LOCK_PREFIX = "lock:";
	private static final long DEFAULT_LOCK_WAIT_MILLIS = 5000;
	private static final long DEFAULT_LOCK_RETRY_INTERVAL_MILLIS = 30;
	
	private final StringRedisTemplate redisTemplate;
	private final Clock clock;
	
	public void acquireLock(String identifier, long timeoutMillis) {
		long retryIntervalMillis = DEFAULT_LOCK_RETRY_INTERVAL_MILLIS;
		long startTime = clock.millis();
		long previous = 0;
		long current = 1;
		
		while (clock.millis() - startTime < DEFAULT_LOCK_WAIT_MILLIS) {
			if (tryAcquireLock(identifier, timeoutMillis)) {
				return;
			}
			sleep(retryIntervalMillis);
			long next = previous + current;
			previous = current;
			current = next;
			retryIntervalMillis = calculateRetryInterval(current, retryIntervalMillis, startTime, DEFAULT_LOCK_WAIT_MILLIS);
		}
		throw new IllegalStateException("Unable to acquire lock for identifier: " + identifier);
	}
	
	private boolean tryAcquireLock(String identifier, long timeoutMillis) {
		String lockKey = LOCK_PREFIX + identifier;
		return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", timeoutMillis, TimeUnit.SECONDS));
	}
	
	private void sleep(long millis) {
		try {
			TimeUnit.MILLISECONDS.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Lock acquisition interrupted", e);
		}
	}
	
	private long calculateRetryInterval(long current, long retryIntervalMillis, long startTime, long waitMillis) {
		return Math.min(current * retryIntervalMillis, waitMillis - (clock.millis() - startTime));
	}
	
	public void releaseLock(String identifier) {
		String lockKey = LOCK_PREFIX + identifier;
		redisTemplate.delete(lockKey);
	}
}
