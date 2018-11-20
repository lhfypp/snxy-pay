package com.snxy.pay.serviceImpl;

import com.snxy.pay.dao.mapper.TradeLogMapper;
import com.snxy.pay.domain.TradeLog;
import com.snxy.pay.service.TradeLogService;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by 24398 on 2018/11/19.
 */

@Data
@Service
public class TradeLogServiceImpl implements TradeLogService{

    @Resource
    private TradeLogMapper tradeLogMapper;

    @Override
    public void logTrade(TradeLog tradeLog) {
        this.tradeLogMapper.insertSelective(tradeLog);
    }
}
