package com.tuum.banking.service.lock;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LockAspect {
	private final LockService lockService;
	private final ExpressionParser parser = new SpelExpressionParser();
	
	@Around("@annotation(lock)")
	public Object aroundLockedMethod(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
		String keyExpression = lock.key();
		String lockKey = extractLockKey(keyExpression, joinPoint.getArgs());
		lockService.acquireLock(lockKey);
		try {
			return joinPoint.proceed();
		} finally {
			lockService.releaseLock(lockKey);
		}
	}
	
	private String extractLockKey(String keyExpression, Object[] args) {
		return keyExpression.contains(".") ? getKeyFromNestedProperty(keyExpression, args) : getKeyFromDirectVariable(keyExpression, args);
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
	
	private String getKeyFromDirectVariable(String keyExpression, Object[] args) {
		for (Object arg : args) {
			if (arg != null && getVariableName(arg).equalsIgnoreCase(keyExpression)) {
				return arg.toString();
			}
		}
		throw new IllegalArgumentException("No argument found with name: " + keyExpression);
	}
}
