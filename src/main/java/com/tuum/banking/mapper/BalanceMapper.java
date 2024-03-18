package com.tuum.banking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuum.banking.domain.Balance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BalanceRepository extends BaseMapper<Balance> {
}
