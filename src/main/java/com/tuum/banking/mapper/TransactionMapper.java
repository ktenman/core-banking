package com.tuum.banking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuum.banking.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
	
	@Select("SELECT * FROM transaction WHERE account_id = #{accountId}")
	List<Transaction> selectByAccountId(Long accountId);
}
