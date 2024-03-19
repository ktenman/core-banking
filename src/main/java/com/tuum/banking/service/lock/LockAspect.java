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
		String keyExpression = lock.key();
		String lockKey = keyExpression.contains(".") ?
				getKeyFromNestedProperty(keyExpression, joinPoint.getArgs()) :
				getKeyFromDirectVariable(keyExpression, joinPoint);
		lockService.acquireLock(lockKey);
		log.info("Lock acquired for key {} with lock key {}", keyExpression, lockKey);
		try {
			return joinPoint.proceed();
		} finally {
			lockService.releaseLock(lockKey);
			log.info("Lock released for key {} with lock key {}", keyExpression, lockKey);
		}
	}
	
	private String getKeyFromNestedProperty(String keyExpression, Object[] args) {
		EvaluationContext context = new StandardEvaluationContext();
		setArgumentVariablesInContext(context, args);
		
		Expression expression = parser.parseExpression(keyExpression);
		return expression.getValue(context, String.class);
	}
	
	private void setArgumentVariablesInContext(EvaluationContext context, Object[] args) {
		for (Object arg : args) {
			if (arg != null) {
				String variableName = getVariableName(arg);
				context.setVariable(variableName, arg);
			}
		}
	}
	
	private String getVariableName(Object arg) {
		String argName = arg.getClass().getSimpleName();
		return argName.substring(0, 1).toLowerCase() + argName.substring(1);
	}
	
	private String getKeyFromDirectVariable(String keyExpression, ProceedingJoinPoint joinPoint) {
		if (keyExpression.startsWith("#")) {
			keyExpression = keyExpression.substring(1);
		}
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		if (signature == null) {
			throw new IllegalArgumentException("No argument found with name: " + keyExpression + " in method signature");
		}
		Method method = signature.getMethod();
		Parameter[] parameters = method.getParameters();
		Object[] args = joinPoint.getArgs();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			String parameterName = parameter.getName();
			if (parameterName.equalsIgnoreCase(keyExpression)) {
				return args[i].toString();
			}
		}
		throw new IllegalArgumentException("No argument found with name: " + keyExpression);
	}
	
}
