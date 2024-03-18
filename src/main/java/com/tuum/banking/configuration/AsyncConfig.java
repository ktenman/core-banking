package com.tuum.banking.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import static com.tuum.banking.configuration.ConfigurationConstants.NOT_TEST_PROFILE;

@Configuration
@EnableAsync
@Profile(NOT_TEST_PROFILE)
public class AsyncConfig {
}
