package com.tuum.banking.configuration;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ConfigurationConstants {
	public static final String TEST_PROFILE = "test";
	public static final String NOT_TEST_PROFILE = "!" + TEST_PROFILE;
}
