package com.tuum.banking.service.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static java.util.concurrent.TimeUnit.SECONDS;
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
	
	@InjectMocks
	private LockService lockService;
	
	@BeforeEach
	public void setUp() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}
	
	@Test
	void shouldAcquireLockSuccessfully() {
		when(valueOperations.setIfAbsent("lock:" + DEFAULT_LOCK_IDENTIFIER, "locked", 60, SECONDS)).thenReturn(true);
		
		assertThatCode(() -> lockService.acquireLock(DEFAULT_LOCK_IDENTIFIER))
				.doesNotThrowAnyException();
		
		verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), anyLong(), any());
	}
	
	@Test
	void shouldThrowExceptionWhenLockIsAlreadyAcquired() {
		when(valueOperations.setIfAbsent("lock:" + DEFAULT_LOCK_IDENTIFIER, "locked", 60, SECONDS)).thenReturn(false);
		
		assertThatThrownBy(() -> lockService.acquireLock(DEFAULT_LOCK_IDENTIFIER))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Unable to acquire lock for identifier: " + DEFAULT_LOCK_IDENTIFIER);
		
		verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), anyLong(), any());
	}
	
	@Test
	@MockitoSettings(strictness = Strictness.LENIENT)
	void shouldReleaseLockSuccessfully() {
		lockService.releaseLock(DEFAULT_LOCK_IDENTIFIER);
		
		verify(redisTemplate, times(1)).delete("lock:" + DEFAULT_LOCK_IDENTIFIER);
	}
	
}

