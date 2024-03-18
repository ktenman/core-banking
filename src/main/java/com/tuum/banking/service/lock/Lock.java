package com.tuum.banking.service.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for locking the method execution based on a specified key using Redis-based distributed locking.
 * <p>
 * The @Lock annotation is used to ensure that a method is executed exclusively
 * across multiple instances or threads for a given key value. It prevents concurrent
 * execution of the annotated method for the same key in a distributed environment.
 * <p>
 * The key is specified using the 'key' attribute, which supports SpEL (Spring Expression Language)
 * expressions to extract the key value from the method parameters.
 * <p>
 * Examples:
 * <pre>
 * &#064;Lock(key = "#reference")
 * public void processAccount(String reference) {
 *     // Method body
 * }
 * </pre>
 * In the above example, the 'key' is set to "#reference", which means the lock will be
 * based on the value of the 'reference' parameter passed to the method.
 * <p>
 * <pre>
 * &#064;Lock(key = "#account.reference")
 * public void processAccount(Account account) {
 *     // Method body
 * }
 * </pre>
 * In this example, the 'key' is set to "#account.reference", which means the lock will be
 * based on the value of the 'reference' property of the 'account' parameter passed to the method.
 * <p>
 * The @Lock annotation uses Redis as the distributed locking mechanism. It leverages the
 * atomic operations provided by Redis to acquire and release locks across multiple instances
 * or threads. The lock is acquired using the Redis 'SET' command with the 'NX' and 'EX' options,
 * ensuring that only one instance or thread can acquire the lock for a specific key at a time.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {
	/**
	 * The key to lock the method execution.
	 * <p>
	 * The key supports SpEL expressions to extract the key value from the method parameters.
	 * The expression should start with "#" followed by the parameter name or a nested property.
	 * <p>
	 * Examples:
	 * <ul>
	 *     <li>"#reference" - uses the value of the 'reference' parameter as the key</li>
	 *     <li>"#account.reference" - uses the value of the 'reference' property of the 'account' parameter as the key</li>
	 * </ul>
	 *
	 * @return the SpEL expression for the key
	 */
	String key();
}
