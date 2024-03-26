package com.tuum.banking.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tuum.banking.configuration.JsonStringTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxMessage extends BaseDomain {
	
	private String aggregateType;
	private Long aggregateId;
	private String eventType;
	@TableField(typeHandler = JsonStringTypeHandler.class)
	private String payload;
	private OutboxStatus status;
	private String errorMessage;
	
	public enum OutboxStatus {
		PENDING, SENT, FAILED
	}
}
