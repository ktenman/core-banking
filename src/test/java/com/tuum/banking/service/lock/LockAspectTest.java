package com.tuum.banking.service.lock;

import com.tuum.banking.dto.CreateAccountRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LockAspectTest {
	
	private static final String DEFAULT_IDENTIFIER = "123";
	private static final String INVALID_VARIABLE = "invalidVariable";
	
	@Mock
	private LockService lockService;
	
	@Mock
	private ProceedingJoinPoint joinPoint;
	
	@InjectMocks
	private LockAspect lockAspect;
	
	private Object[] args;
	
	@BeforeEach
	void setUp() {
		args = new Object[]{CreateAccountRequest.builder().reference(DEFAULT_IDENTIFIER).build()};
	}
	
	@Test
	void aroundLockedMethod_WithNestedProperty_ShouldAcquireAndReleaseLock() throws Throwable {
		when(joinPoint.getArgs()).thenReturn(args);
		when(joinPoint.proceed()).thenReturn(null);
		
		lockAspect.aroundLockedMethod(joinPoint, createLock("#createAccountRequest.reference"));
		
		verify(lockService).acquireLock(DEFAULT_IDENTIFIER);
		verify(lockService).releaseLock(DEFAULT_IDENTIFIER);
		verify(joinPoint).proceed();
	}
	
	@Test
	void aroundLockedMethod_WithDirectVariable_ShouldAcquireAndReleaseLock() throws Throwable {
		when(joinPoint.getArgs()).thenReturn(args);
		when(joinPoint.proceed()).thenReturn(null);
		
		lockAspect.aroundLockedMethod(joinPoint, createLock("createAccountRequest"));
		
		verify(lockService).acquireLock(args[0].toString());
		verify(lockService).releaseLock(args[0].toString());
		verify(joinPoint).proceed();
	}
	
	@Test
	void aroundLockedMethod_WithInvalidDirectVariable_ShouldThrowException() {
		when(joinPoint.getArgs()).thenReturn(args);
		
		Throwable thrown = catchThrowable(() -> lockAspect.aroundLockedMethod(joinPoint, createLock(INVALID_VARIABLE)));
		
		assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("No argument found with name: " + INVALID_VARIABLE);
		verify(lockService, never()).acquireLock(anyString());
		verify(lockService, never()).releaseLock(anyString());
		verifyNoMoreInteractions(joinPoint);
	}
	
	private Lock createLock(String key) {
		return new Lock() {
			@Override
			public String key() {
				return key;
			}
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return Lock.class;
			}
		};
	}
	
}
