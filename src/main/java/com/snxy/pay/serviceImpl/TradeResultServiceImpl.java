package com.snxy.pay.serviceImpl;

import com.snxy.pay.config.BusinessTypeEnum;
import com.snxy.pay.dao.mapper.TradeResultMapper;
import com.snxy.pay.domain.TradeResult;
import com.snxy.pay.service.TradeResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

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
        if(businessTypeId == BusinessTypeEnum.ENTRY_FEE.getBusinessTypeId()
                || businessTypeId == BusinessTypeEnum.ENTRY_DEPOSIT_FEE.getBusinessTypeId()){
            // 进门收费
        }else if(businessTypeId == BusinessTypeEnum.REFUND_DEPOSIT_FEE.getBusinessTypeId()
                || businessTypeId == BusinessTypeEnum.REFUND_ENTRY_FEE.getBusinessTypeId()){
          // 退进门费和押金  退押金
             // 先查寻
            String outFundNo = tradeResult.getOutRefundNo();
            TradeResult historyTradeResult = this.tradeResultMapper.selectByOutTradeNoAndOutFundNo(outTradeNo,outFundNo,false);
            if(historyTradeResult != null){
                // 有历史退费交易
                String resultCode = historyTradeResult.getResultCode();
                if("SUCCESS".equalsIgnoreCase(resultCode)){
                    // 历史交易成功  不处理
                    return;
                }else{
                    // 历史交易失败  ，修改
                    tradeResult.setId(historyTradeResult.getId());
                    tradeResult.setGmtModified( new Date());
                    this.tradeResultMapper.updateByPrimaryKeySelective(tradeResult);
                    return;
                }
            }


        }else if(businessTypeId == BusinessTypeEnum.CANCEL_PAY.getBusinessTypeId()){
            // 撤销订单
            // 有无历史撤销订单
            TradeResult cancelTradeResult = this.tradeResultMapper.selectByOutTradeNoAndBusinessType(outTradeNo,businessTypeId,false);
            if(cancelTradeResult != null){
                String resultCode = cancelTradeResult.getResultCode();
                if("SUCCESS".equalsIgnoreCase(resultCode)){
                    return;
                }else{
                    tradeResult.setGmtModified(new Date());
                    tradeResult.setId(cancelTradeResult.getId());
                    this.tradeResultMapper.updateByPrimaryKeySelective(tradeResult);
                    return;
                }
            }

        }
        this.tradeResultMapper.insertSelective(tradeResult);
    }

    @Override
    public void updateSelective(TradeResult tradeResult) {
        this.tradeResultMapper.updateByPrimaryKeySelective(tradeResult);
    }
}
