package com.snxy.pay.dao.mapper;

import com.snxy.pay.domain.TradeLog;

public interface TradeLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TradeLog record);

    int insertSelective(TradeLog record);

    TradeLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TradeLog record);

    int updateByPrimaryKey(TradeLog record);
}