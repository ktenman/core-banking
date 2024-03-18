package com.tuum.banking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuum.banking.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionRepository extends BaseMapper<Transaction> {
}
