package com.tuum.banking.configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
@MapperScan(value = "com.tuum.banking.mapper", sqlSessionTemplateRef = "sqlSessionTemplate")
public class MyBatisConfig {
	
	@Bean
	public MetaObjectHandler metaObjectHandler() {
		return new TimestampMetaObjectHandler();
	}
	
	public static class TimestampMetaObjectHandler implements MetaObjectHandler {
		
		@Override
		public void insertFill(MetaObject metaObject) {
			Instant now = Instant.now();
			this.setFieldValByName("createdAt", now, metaObject);
			this.setFieldValByName("updatedAt", now, metaObject);
		}
		
		@Override
		public void updateFill(MetaObject metaObject) {
			this.setFieldValByName("updatedAt", Instant.now(), metaObject);
		}
	}
	
}
