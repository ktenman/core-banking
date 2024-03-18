package com.tuum.banking.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public abstract class BaseEntity {
	@TableId(type = IdType.AUTO)
	private Long id;
	
	@TableField(fill = FieldFill.INSERT)
	private Instant createdAt;
	
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Instant updatedAt;
}
