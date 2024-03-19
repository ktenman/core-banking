package com.tuum.banking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuum.banking.domain.Balance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BalanceMapper extends BaseMapper<Balance> {
	
	@Select("SELECT * FROM balance WHERE account_id = #{accountId} AND currency = #{currency}")
	Optional<Balance> findByAccountIdAndCurrency(Long accountId, String currency);
	
	@Select("SELECT * FROM balance WHERE account_id = #{accountId}")
	List<Balance> findByAccountId(Long accountId);
}
