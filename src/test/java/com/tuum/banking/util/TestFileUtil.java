package com.tuum.banking.util;

import java.nio.file.Files;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.ResourceUtils;

import java.io.IOException;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TestFileUtil {
	
	@SneakyThrows(IOException.class)
	public static String readFileAsString(String fileName) {
		return Files.readString(ResourceUtils.getFile("classpath:__files/" + fileName).toPath());
	}
}
