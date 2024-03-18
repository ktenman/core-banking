package com.tuum.banking.service.lock;

import com.tuum.banking.dto.CreateAccountRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LockAspectTest {
	private static final String DEFAULT_IDENTIFIER = "123";
	private static final String INVALID_VARIABLE = "invalidVariable";
	
	@Mock
	private LockService lockService;
	
	@Mock
	private ProceedingJoinPoint joinPoint;
	
	@Mock
	private MethodSignature signature;
	
	@InjectMocks
	private LockAspect lockAspect;
	
	@Test
	void aroundLockedMethod_WithNestedProperty_ShouldAcquireAndReleaseLock() throws Throwable {
		Object[] args = new Object[]{CreateAccountRequest.builder().reference(DEFAULT_IDENTIFIER).build()};
		when(joinPoint.getArgs()).thenReturn(args);
		when(joinPoint.getArgs()).thenReturn(args);
		when(joinPoint.proceed()).thenReturn(null);
		
		lockAspect.aroundLockedMethod(joinPoint, createLock("#createAccountRequest.reference"));
		
		verify(lockService).acquireLock(DEFAULT_IDENTIFIER);
		verify(lockService).releaseLock(DEFAULT_IDENTIFIER);
		verify(joinPoint).proceed();
	}
	
	@Test
	void aroundLockedMethod_WithDirectVariable_ShouldAcquireAndReleaseLock() throws Throwable {
		Object[] args = new Object[]{DEFAULT_IDENTIFIER};
		when(joinPoint.getArgs()).thenReturn(args);
		when(signature.getMethod()).thenReturn(getMethod("testMethod"));
		when(joinPoint.proceed()).thenReturn(null);
		when(joinPoint.getSignature()).thenReturn(signature);
		
		lockAspect.aroundLockedMethod(joinPoint, createLock("#reference"));
		
		verify(lockService).acquireLock(DEFAULT_IDENTIFIER);
		verify(lockService).releaseLock(DEFAULT_IDENTIFIER);
		verify(joinPoint).proceed();
	}
	
	@Test
	void aroundLockedMethod_WithInvalidDirectVariable_ShouldThrowException() {
		Throwable thrown = catchThrowable(() -> lockAspect.aroundLockedMethod(joinPoint, createLock(INVALID_VARIABLE)));
		
		assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("No argument found with name: %s in method signature", INVALID_VARIABLE);
		verify(lockService, never()).acquireLock(anyString());
		verify(lockService, never()).releaseLock(anyString());
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
	
	private Method getMethod(String name) throws NoSuchMethodException {
		return TestClass.class.getMethod(name, String.class);
	}
	
	private static class TestClass {
		public void testMethod(String reference) {
		}
	}
}
