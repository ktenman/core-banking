package com.tuum.banking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuum.banking.configuration.CustomPage;
import com.tuum.banking.domain.OutboxMessage;
import com.tuum.banking.domain.OutboxMessage.OutboxStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OutboxMessageMapper extends BaseMapper<OutboxMessage> {
	
	@Select("SELECT * FROM outbox_message WHERE status = 'PENDING' LIMIT #{page.size}")
	List<OutboxMessage> selectPendingMessages(@Param("page") CustomPage<?> page);
	
	@Update("UPDATE outbox_message SET status = #{status}, error_message = #{errorMessage}, updated_at = NOW() WHERE id = #{id}")
	void updateStatus(Long id, OutboxStatus status, String errorMessage);
}
