package com.tuum.banking.service.lock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {
	private static final String DEFAULT_LOCK_IDENTIFIER = "testLock";
	
	@Mock
	private StringRedisTemplate redisTemplate;
	
	@Mock
	private ValueOperations<String, String> valueOperations;
	
	@Mock
	private Clock clock;
	
	@InjectMocks
	private LockService lockService;
	
	@Test
	void shouldAcquireLockSuccessfully() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent("lock:" + DEFAULT_LOCK_IDENTIFIER, "locked", 60_000, MILLISECONDS))
				.thenReturn(true);
		
		assertThatCode(() -> lockService.acquireLock(DEFAULT_LOCK_IDENTIFIER, 60_000))
				.doesNotThrowAnyException();
		
		verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), anyLong(), any());
	}
	
	@Test
	void shouldThrowExceptionWhenLockIsAlreadyAcquired() {
		when(clock.millis())
				.thenReturn(0L)
				.thenReturn(5000L);
		
		assertThatThrownBy(() -> lockService.acquireLock(DEFAULT_LOCK_IDENTIFIER, 60_000))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Unable to acquire lock for identifier: " + DEFAULT_LOCK_IDENTIFIER);
	}
	
	@Test
	void shouldReleaseLockSuccessfully() {
		lockService.releaseLock(DEFAULT_LOCK_IDENTIFIER);
		
		verify(redisTemplate, times(1)).delete("lock:" + DEFAULT_LOCK_IDENTIFIER);
	}
	
	@Test
	void shouldRetryAcquiringLockWithFibonacciBackoff() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(true);
		when(clock.millis())
				.thenReturn(0L)   // Start time
				.thenReturn(30L)  // Retry 1
				.thenReturn(60L)  // Retry 2
				.thenReturn(90L)  // Retry 3
				.thenReturn(120L); // Lock acquired
		
		assertThatCode(() -> lockService.acquireLock(DEFAULT_LOCK_IDENTIFIER, 60_000))
				.doesNotThrowAnyException();
		
		verify(valueOperations, times(4)).setIfAbsent(anyString(), anyString(), anyLong(), any());
		verify(clock, times(8)).millis();
	}
	
}
