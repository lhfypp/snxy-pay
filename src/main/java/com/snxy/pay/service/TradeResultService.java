package com.snxy.pay.service;

import com.snxy.pay.domain.TradeResult;

/**
 * Created by 24398 on 2018/11/19.
 */
public interface TradeResultService {
    void logResult(TradeResult tradeResult);

    void updateSelective(TradeResult tradeResult);
}
