package com.tuum.banking.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfiguration {
	
	public static final String ONE_DAY_CACHE = "one-day-cache";
	public static final String ACCOUNTS_CACHE = "accounts-v1";
	public static final String TRANSACTIONS_CACHE = "transactions-v1";
	private static final Duration DEFAULT_TTL = Duration.ofMinutes(1);
	
	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		cacheConfigurations.put(ONE_DAY_CACHE, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1)));
		cacheConfigurations.put(ACCOUNTS_CACHE, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
		cacheConfigurations.put(TRANSACTIONS_CACHE, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(DEFAULT_TTL);
		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaultConfig)
				.withInitialCacheConfigurations(cacheConfigurations)
				.build();
	}
}
