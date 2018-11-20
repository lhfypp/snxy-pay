package com.snxy.pay.dao.mapper;

import com.snxy.pay.domain.TradeResult;
import org.apache.ibatis.annotations.Param;

public interface TradeResultMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TradeResult record);

    int insertSelective(TradeResult record);

    TradeResult selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TradeResult record);

    int updateByPrimaryKey(TradeResult record);

    TradeResult selectByOutTradeNoAndOutFundNo(@Param("outTradeNo") String outTradeNo, @Param("outFundNo") String outFundNo, @Param("isDelete") Boolean isDelete);

    TradeResult selectByOutTradeNoAndBusinessType(@Param("outTradeNo") String outTradeNo,@Param("businessTypeId") Integer businessTypeId, @Param("isDelete") Boolean isDelete);
}