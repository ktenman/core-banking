package com.tuum.banking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tuum.banking.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
	
	@Select("SELECT * FROM transaction WHERE account_id = #{accountId}")
	IPage<Transaction> selectByAccountId(Long accountId, Page<?> page);
	
}
