package com.tuum.banking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuum.banking.domain.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
	
	@Select("SELECT * FROM account WHERE id = #{accountId}")
	Optional<Account> selectById(Long accountId);
	
	@Select("SELECT * FROM account WHERE reference = #{reference}")
	Optional<Account> selectByReference(String reference);

}
