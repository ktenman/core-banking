package com.tuum.banking.configuration.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {
	
	private static final String TRANSACTION_ID = "transactionId";
	private final ObjectMapper objectMapper;
	
	private static void setTransactionId() {
		UUID uuid = UUID.randomUUID();
		MDC.put(TRANSACTION_ID, String.format("[%s] ", uuid));
	}
	
	private static void clearTransactionId() {
		MDC.remove(TRANSACTION_ID);
	}
	
	@Around("@annotation(Loggable)")
	public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		
		if (method.getReturnType().equals(Void.TYPE)) {
			log.warn("Loggable annotation should not be used on methods without return type: {}", method.getName());
			return joinPoint.proceed();
		}
		
		return logInvocation(joinPoint);
	}
	
	private Object logInvocation(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.nanoTime();
		setTransactionId();
		try {
			logEntry(joinPoint);
			Object result = joinPoint.proceed();
			logExit(joinPoint, result, startTime);
			return result;
		} finally {
			clearTransactionId();
		}
	}
	
	private void logEntry(ProceedingJoinPoint joinPoint) throws Throwable {
		String argsJson = objectMapper.writeValueAsString(joinPoint.getArgs());
		log.info("{} entered with arguments: {}", joinPoint.getSignature().toShortString(), argsJson);
	}
	
	private void logExit(ProceedingJoinPoint joinPoint, Object result, long startTime) throws Throwable {
		String resultJson = objectMapper.writeValueAsString(result);
		log.info("{} exited with result: {} in {} seconds", joinPoint.getSignature().toShortString(), resultJson, durationInSeconds(startTime));
	}
	
	private String durationInSeconds(long startTime) {
		double duration = (System.nanoTime() - startTime) / 1_000_000_000.0;
		return String.format("%.3f", duration);
	}
	
}
