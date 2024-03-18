package com.tuum.banking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuum.banking.domain.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountRepository extends BaseMapper<Account> {
}
