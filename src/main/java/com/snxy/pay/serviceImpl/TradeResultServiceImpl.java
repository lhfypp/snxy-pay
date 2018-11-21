package com.snxy.pay.serviceImpl;

import com.snxy.pay.config.BusinessTypeEnum;
import com.snxy.pay.dao.mapper.TradeResultMapper;
import com.snxy.pay.domain.TradeResult;
import com.snxy.pay.service.TradeResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 24398 on 2018/11/19.
 */
@Service
@Slf4j
public class TradeResultServiceImpl implements TradeResultService {

    @Resource
    private TradeResultMapper tradeResultMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logResult(TradeResult tradeResult) {
        // 不同的交易类型不同的方式
        String outTradeNo = tradeResult.getOutTradeNo();
        Integer businessTypeId = tradeResult.getBusinessTypeId().intValue();
        TradeResult historyTradeResult  = null;
        if(businessTypeId == BusinessTypeEnum.ENTRY_FEE.getBusinessTypeId()
                || businessTypeId == BusinessTypeEnum.ENTRY_DEPOSIT_FEE.getBusinessTypeId()){
            // 进门收费
            List<Integer> businessTypes = new ArrayList<>();
                businessTypes.add(BusinessTypeEnum.ENTRY_FEE.getBusinessTypeId());
                businessTypes.add(BusinessTypeEnum.ENTRY_DEPOSIT_FEE.getBusinessTypeId());
            historyTradeResult = this.tradeResultMapper.selectPayResult(outTradeNo,businessTypes,false);
        }else if(businessTypeId == BusinessTypeEnum.REFUND_DEPOSIT_FEE.getBusinessTypeId()
                || businessTypeId == BusinessTypeEnum.REFUND_ENTRY_FEE.getBusinessTypeId()){
          // 退进门费和押金  退押金
             // 先查寻
            String outFundNo = tradeResult.getOutRefundNo();
            historyTradeResult = this.tradeResultMapper.selectByOutTradeNoAndOutFundNo(outTradeNo,outFundNo,false);
        }else if(businessTypeId == BusinessTypeEnum.CANCEL_PAY.getBusinessTypeId()){
            // 撤销订单
            // 有无历史撤销订单
            historyTradeResult  = this.tradeResultMapper.selectByOutTradeNoAndBusinessType(outTradeNo,businessTypeId,false);
        }


        if(historyTradeResult != null){
            // 已有历史交易订单修改
          this.insertOrUpdateTradeResult(tradeResult,historyTradeResult);
        }else{
            // 新建
           this.tradeResultMapper.insertSelective(tradeResult);
        }
    }

    public void insertOrUpdateTradeResult(TradeResult tradeResult,TradeResult historyTradeResult ){
        if(historyTradeResult != null){
            String resultCode = historyTradeResult.getResultCode();
            if("SUCCESS".equalsIgnoreCase(resultCode)){
                return;
            }else{
                tradeResult.setId(historyTradeResult.getId());
                tradeResult.setGmtModified(new Date());
                this.tradeResultMapper.updateByPrimaryKeySelective(tradeResult);
                return;
            }
        }
    }



    @Override
    public void updateSelective(TradeResult tradeResult) {
        this.tradeResultMapper.updateByPrimaryKeySelective(tradeResult);
    }
}
