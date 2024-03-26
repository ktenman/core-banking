package com.tuum.banking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tuum.banking.domain.OutboxMessage;
import com.tuum.banking.domain.OutboxMessage.OutboxStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OutboxMessageMapper extends BaseMapper<OutboxMessage> {
	
	@Select("SELECT * FROM outbox_message WHERE status = 'PENDING'")
	IPage<OutboxMessage> selectPendingMessages(Page<?> page);
	
	@Update("UPDATE outbox_message SET status = #{status}, error_message = #{errorMessage}, updated_at = NOW() WHERE id = #{id}")
	void updateStatus(Long id, OutboxStatus status, String errorMessage);
}
