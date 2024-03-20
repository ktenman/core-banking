package com.tuum.banking.service.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LockAspectTest {
	
	@Mock
	private LockService lockService;
	
	@Mock
	private ProceedingJoinPoint joinPoint;
	
	@Mock
	private MethodSignature methodSignature;
	
	@InjectMocks
	private LockAspect lockAspect;
	
	@Test
	void aroundLockedMethod_WithDirectVariable_ShouldAcquireAndReleaseLock() throws Throwable {
		Lock lock = createLock("#account");
		TestAccount account = new TestAccount("123456");
		
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(TestService.class.getMethod("doSomething", TestAccount.class));
		when(joinPoint.getArgs()).thenReturn(new Object[]{account});
		
		lockAspect.aroundLockedMethod(joinPoint, lock);
		
		verify(lockService, times(1)).acquireLock(account.toString());
		verify(lockService, times(1)).releaseLock(account.toString());
	}
	
	@Test
	void aroundLockedMethod_WithNestedProperty_ShouldAcquireAndReleaseLock() throws Throwable {
		Lock lock = createLock("#account.accountNumber");
		String lockKey = "123456";
		TestAccount account = new TestAccount("123456");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(TestService.class.getMethod("doSomething", TestAccount.class));
		when(joinPoint.getArgs()).thenReturn(new Object[]{account});
		
		lockAspect.aroundLockedMethod(joinPoint, lock);
		
		verify(lockService, times(1)).acquireLock(lockKey);
		verify(lockService, times(1)).releaseLock(lockKey);
	}
	
	@Test
	void aroundLockedMethod_WithInvalidDirectVariable_ShouldThrowException() throws NoSuchMethodException {
		Lock lock = createLock("#invalidVariable");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(TestService.class.getMethod("doSomething", TestAccount.class));
		
		assertThatThrownBy(() -> lockAspect.aroundLockedMethod(joinPoint, lock))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No argument found with name: invalidVariable");
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
	
	record TestAccount(String accountNumber) {
	}
	
	static class TestService {
		public void doSomething(TestAccount account) {
		}
	}
}
