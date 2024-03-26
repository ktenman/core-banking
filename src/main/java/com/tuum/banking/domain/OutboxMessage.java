package com.tuum.banking.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("outbox_message")
public class OutboxMessage extends BaseEntity {
	
	@TableField("aggregate_type")
	private String aggregateType;
	
	@TableField("aggregate_id")
	private Long aggregateId;
	
	@TableField("event_type")
	private String eventType;
	
	@TableField(value = "payload", typeHandler = JsonStringTypeHandler.class)
	private String payload;
	
	@TableField("status")
	private OutboxStatus status;
	
	@TableField("error_message")
	private String errorMessage;
	
	public enum OutboxStatus {
		PENDING, SENT, FAILED
	}
}
