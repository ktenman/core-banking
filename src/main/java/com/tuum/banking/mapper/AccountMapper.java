package com.tuum.banking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuum.banking.domain.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
	
	@Select("SELECT a.*, b.id as balance_id, b.currency, b.available_amount " +
			"FROM account a LEFT JOIN balance b ON a.id = b.account_id WHERE a.id = #{accountId}")
	@ResultMap("AccountResultMap")
	Optional<Account> getAccountWithBalances(Long accountId);
	
}
