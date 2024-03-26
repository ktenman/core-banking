package com.tuum.banking.service.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LockAspect {
	private final LockService lockService;
	private final ExpressionParser parser = new SpelExpressionParser();
	
	@Around("@annotation(lock)")
	public Object aroundLockedMethod(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
		if (lock.key().isBlank()) {
			throw new IllegalArgumentException("Lock key cannot be empty");
		}
		String lockKey = getKey(lock.key(), joinPoint);
		long timeoutMillis = lock.timeoutMillis();
		if (lock.retry()) {
			lockService.acquireLock(lockKey, timeoutMillis);
		} else {
			boolean lockAcquired = lockService.tryAcquireLock(lockKey, timeoutMillis);
			if (!lockAcquired) {
				throw new IllegalStateException("Unable to acquire lock for identifier: " + lockKey);
			}
		}
		log.debug("Lock acquired for key {} with lock key {}", lock.key(), lockKey);
		try {
			return joinPoint.proceed();
		} finally {
			lockService.releaseLock(lockKey);
			log.debug("Lock released for key {} with lock key {}", lock.key(), lockKey);
		}
	}
	
	private String getKey(String keyExpression, ProceedingJoinPoint joinPoint) {
		if (keyExpression.startsWith("'") && keyExpression.endsWith("'")) {
			return keyExpression.substring(1, keyExpression.length() - 1);
		}
		if (keyExpression.startsWith("#")) {
			keyExpression = keyExpression.substring(1);
		}
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		if (signature == null) {
			throw new IllegalArgumentException("No argument found in method signature");
		}
		Method method = signature.getMethod();
		Parameter[] parameters = method.getParameters();
		Object[] args = joinPoint.getArgs();
		String[] keys = keyExpression.split("\\.");
		String rootKey = keys[0];
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			String parameterName = parameter.getName();
			if (parameterName.equalsIgnoreCase(rootKey)) {
				if (keys.length > 1) {
					String nestedKey = keys[1];
					EvaluationContext context = new StandardEvaluationContext(args[i]);
					Expression expression = parser.parseExpression(nestedKey);
					return expression.getValue(context, String.class);
				} else {
					return args[i].toString();
				}
			}
		}
		throw new IllegalArgumentException("No argument found with name: " + rootKey);
	}
}
