package com.snxy.pay.service;

import com.alibaba.fastjson.JSON;
import com.netflix.loadbalancer.InterruptTask;
import com.snxy.common.exception.BizException;
import com.snxy.pay.PayChannelEnum;
import com.snxy.pay.config.BusinessTypeEnum;
import com.snxy.pay.config.TradeTypeEnum;
import com.snxy.pay.config.ali.*;
import com.snxy.pay.domain.TradeLog;
import com.snxy.pay.domain.TradeResult;
import com.snxy.pay.service.resp.*;
import com.snxy.pay.service.vo.ali.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by lvhai on 2018/11/9.
 * 阿里反扫支付
 */
@Component
@Slf4j
public class AliMicropayDecorator {
    private static final int firstWaitTime = 5 * 1000;
    private static final int interval = 10 * 1000;
    private static final int overtime = 30 * 1000;
    //创建一个可重用固定线程数的线程池
    @Autowired
    ExecutorService threadPool;
    @Autowired
    AliMicropay aliMicropay;
    @Resource
    private TradeLogService tradeLogService;
    @Resource
    private TradeResultService tradeResultService;


    public Map<String, Object> pay(AliPayPara aliPayPara,Integer businessType) throws Exception {
        ///TODO 记录交易日志
        Map<String,Object> map = new HashedMap();
        Boolean payResult = false;
        AliPayResp aliPayResp = this.aliMicropay.pay(aliPayPara);
        if(aliPayResp == null){
            log.error("订单号 : [{}] 缴费失败 ; 服务降级",aliPayPara.getOut_trade_no());
             // 不记录进日志
            throw new BizException("服务异常，请重试");
        }
        // 先对return_code 进行判断
       TradeLog tradeLog = new TradeLog();
            tradeLog.setAppid(aliPayPara.getAppid());
            tradeLog.setMchId(aliPayPara.getMch_id());
            tradeLog.setBusinessTypeId(businessType);
            tradeLog.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessType));
            tradeLog.setReturnCode(aliPayResp.getReturn_code());
            tradeLog.setReturnMsg(aliPayResp.getReturn_msg());
            tradeLog.setBody(aliPayPara.getBody());
            tradeLog.setChannel(PayChannelEnum.ZXALI.getChannelId());
            tradeLog.setOutTradeNo(aliPayPara.getOut_trade_no());
            tradeLog.setTradeMethod(AliMethodNameConst.Ali_MICROPAY);
            tradeLog.setTradeType(TradeTypeEnum.PAY.getDesc());
            tradeLog.setTotalFee(aliPayPara.getTotal_fee());
            tradeLog.setErrCode(aliPayResp.getErr_code());
            tradeLog.setErrCodeDes(aliPayResp.getErr_code_des());
            tradeLog.setResultCode(aliPayResp.getResult_code());
            tradeLog.setGmtCreate(new Date());
            tradeLog.setTradeTime(aliPayResp.getTime_end());

        TradeResult tradeResult = new TradeResult();
            tradeResult.setAppid(aliPayPara.getAppid());
            tradeResult.setMchId(aliPayPara.getMch_id());
            tradeResult.setBusinessTypeId(businessType.longValue());
            tradeResult.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessType));
            tradeResult.setReturnCode(aliPayResp.getReturn_code());
            tradeResult.setReturnMsg(aliPayResp.getReturn_msg());
            tradeResult.setBody(aliPayPara.getBody());
            tradeResult.setChannel(PayChannelEnum.ZXALI.getChannelId());
            tradeResult.setOutTradeNo(aliPayPara.getOut_trade_no());
            tradeResult.setTradeMethod(AliMethodNameConst.Ali_MICROPAY);
            tradeResult.setTradeType(TradeTypeEnum.PAY.getDesc());
            tradeResult.setTotalFee(aliPayPara.getTotal_fee());
            tradeResult.setErrCode(aliPayResp.getErr_code());
            tradeResult.setErrCodeDes(aliPayResp.getErr_code_des());
            tradeResult.setResultCode(aliPayResp.getResult_code());
            tradeResult.setGmtCreate(new Date());

        log.info("tradeLog ------>: [{}]",tradeLog);
        log.info("tradeResult ------>: [{}]",tradeResult);
        tradeResult.setTradeTime(aliPayResp.getTime_end());
        if ("FAIL".equals(aliPayResp.getReturn_code())) {
            log.error("订单号 : [{}] 缴费失败 ：[{}]",aliPayPara.getOut_trade_no(),aliPayResp.getReturn_msg());
            // 记录日志
            this.tradeLogService.logTrade(tradeLog);
            // 记录交易结果
            this.tradeResultService.logResult(tradeResult);
            throw new BizException(aliPayResp.getReturn_msg());
        }

        String resultCode = aliPayResp.getResult_code(); // SUCCESS PAYING  FAIL
        log.debug("支付返回结果码值resultCode:[{}]", resultCode);
        //result_code 是 PAYING 等待 5 秒，然后调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作
        if ("PAYING".equalsIgnoreCase(resultCode)) {
            // 记录交易日志
            this.tradeLogService.logTrade(tradeLog);
            // 记录交易结果
            this.tradeResultService.logResult(tradeResult);
            // 异步查询
            this.asyncQuery(aliPayPara, true,tradeResult);
            map.put("code",1001);// 支付中
            map.put("successMsg","支付中");
            map.put("aliPayResp",aliPayResp);
        } else if("SUCCESS".equalsIgnoreCase(resultCode)){
            // 解析fund_bill_list
            List<AliPayResp.InnerFundBill> innerFundBills
                            = JSON.parseArray(aliPayResp.getFund_bill_list(),AliPayResp.InnerFundBill.class);
            log.info("innerFundBills : [{}]",innerFundBills);

            tradeLog.setOpenid(aliPayResp.getOpenid());
            tradeLog.setTransactionId(aliPayResp.getTransaction_id());
            tradeLog.setCouponFee(aliPayResp.getCoupon_fee());
            tradeLog.setExtend1(aliPayResp.getBuyer_logon_id());

            tradeResult.setOpenid(aliPayResp.getOpenid());
            tradeResult.setTransactionId(aliPayResp.getTransaction_id());
            tradeResult.setCouponFee(aliPayResp.getCoupon_fee());
            tradeResult.setExtend1(aliPayResp.getBuyer_logon_id());
            // 记录交易日志
            this.tradeLogService.logTrade(tradeLog);
            // 记录结果
            this.tradeResultService.logResult(tradeResult);
            map.put("code",0);  // 支付成功
            map.put("successMsg","支付成功");
            map.put("aliPayResp",aliPayResp);
        }else {
            String errCode = aliPayResp.getErr_code();
            if("ACQ.SYSTEM_ERROR".equalsIgnoreCase(errCode)){
                // 记录
                this.tradeLogService.logTrade(tradeLog);
                // 记录结果
                this.tradeResultService.logResult(tradeResult);
               //SYSTEMERROR 立即调用被扫订单结果查询 API  异步查询
                this.asyncQuery(aliPayPara, false,tradeResult);
                map.put("code",1001);// 支付中
                map.put("successMsg","支付中");
                map.put("aliPayResp",aliPayResp);
            }else{
                // 支付失败
                this.tradeLogService.logTrade(tradeLog);
                log.error("订单号 ：[{}] ,支付失败 : [{}]",aliPayPara.getOut_trade_no(),aliPayResp.getErr_code_des());
                throw new BizException(aliPayResp.getErr_code_des());
            }
            ///TODO 更新业务订单状态
        }


        return map;
    }

    private void asyncQuery(AliPayPara aliPayPara, boolean isFirstWait,TradeResult tradeResult)  {
        threadPool.execute(() -> {
            long start = System.currentTimeMillis();
            InterruptTask task = new InterruptTask(overtime);
            AliPayQueryResp aliPayQueryResp = null;
            if (isFirstWait) {
                try {
                    log.debug("停止一段时间开始执行:{}", firstWaitTime);
                    Thread.sleep(firstWaitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String payResult = "FAIL";
            while (!Thread.interrupted()) {
                ///TODO 由wxPayPara生成查询参数
                AliPayQueryPara aliPayQueryPara = AliPayQueryPara.builder()
                                       .appid(aliPayPara.getAppid())
                                       .mch_id(aliPayPara.getMch_id())
                                       .out_trade_no(aliPayPara.getOut_trade_no())
                                       .build();

                try {
                    aliPayQueryResp = this.aliMicropay.payQuery(aliPayQueryPara);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ///TODO 需要判断 return_code 和 result_code
                String tradeState = aliPayQueryResp.getTrade_state();
                log.debug("查询交易状态trade_state:[{}]", tradeState);
                //USERPAYING--用户支付中
                ///TODO 需要确认支付宝是USERPAYING 还是 PAYING
              /*  if (!("USERPAYING".equalsIgnoreCase(tradeState))) {
                    log.debug("其它交易状态跳出:[{}]", tradeState);
                    break;
                 }*/
                 if(aliPayQueryResp == null){
                     // 服务降级
                     continue;
                 }
                // 根据err_code 判断
                String err_code = aliPayQueryResp.getErr_code();
                if(!AliQueryTradeStateEnum.USERPAYING.getTradeState().equalsIgnoreCase(tradeState)){
                       // 得到确定的支付结果
                      // 支付成功或者支付失败跳出 或者不是超时
                     log.debug("跳出循环 ：tradeState [{}] ; err_code : [{}] ; return_msg : [{}]",tradeState,
                             aliPayQueryResp.getErr_code(),aliPayQueryResp.getReturn_msg());
                    break;
                }

                try {
                    log.debug("准备休息时长:{}", interval);
                    Thread.sleep(interval); //休息
                } catch (InterruptedException e) {
                    log.debug("睡眠被打断，跳出循环");
                    break;
                }
            }
            task.cancel();
            log.debug("执行循环查询耗时:{}", (System.currentTimeMillis() - start));
            if(payResult.equalsIgnoreCase("SUCCESS")){
                tradeResult.setResultCode("SUCCESS");
                tradeResult.setOpenid(aliPayQueryResp.getOpenid());
                tradeResult.setCouponFee(aliPayQueryResp.getCoupon_fee());
                tradeResult.setTransactionId(aliPayQueryResp.getTransaction_id());
                tradeResult.setGmtModified(new Date());

                this.tradeResultService.updateSelective(tradeResult);
            }
            // 修改记录结果
            ///TODO 更新业务订单状态, 非必要继续查询状态或者超时了
         //   refreshOrderState(null);
        });

    }

    private void refreshOrderState(Map<String, String> map) {
        log.debug("更新订单状态");
    }

    public Map<String,Object> payQuery(AliPayQueryPara aliPayQueryPara) throws Exception {
        AliPayQueryResp aliPayQueryResp = this.aliMicropay.payQuery(aliPayQueryPara);
        if(aliPayQueryResp == null){
            log.error("订单号 : [{}] ;查询失败 ：[{}]",aliPayQueryPara.getOut_trade_no(),"服务降级");
            throw new BizException("查询支付失败,请重试");
        }

        if("FAIL".equalsIgnoreCase(aliPayQueryResp.getReturn_code())){
            log.error("订单号 : [{}] ;return_msg ：[{}]",aliPayQueryPara.getOut_trade_no(),
                    aliPayQueryResp.getReturn_msg());
            throw new BizException(aliPayQueryResp.getReturn_msg());
        }

        if("FAIL".equalsIgnoreCase(aliPayQueryResp.getResult_code())){
            // 判断err_code
            log.error("订单号 : [{}] ;err_code_desc ：[{}]",aliPayQueryPara.getOut_trade_no(),
                    aliPayQueryResp.getErr_code_des());
            throw new BizException(aliPayQueryResp.getErr_code_des());
        }
        // return_code 和 result_code 均为SUCCESS
        Map<String,Object>  map = new HashMap();
           // 判断trade_state
          String trade_state = aliPayQueryResp.getTrade_state();
          Integer code ;
          String successMsg = null;
          if(AliQueryTradeStateEnum.SUCCESS.getTradeState().equalsIgnoreCase(trade_state)){
              // 支付成功
              code = 0 ;
              successMsg = "支付成功";
          }else if(AliQueryTradeStateEnum.CLOSED.getTradeState().equalsIgnoreCase(trade_state)){
              // 订单已关闭   可能是未付款交易超时关闭   可能是支付完成后已经全额退款
              code = 1002;
              successMsg = "订单关闭";
          }else{
              // 用户支付中
              successMsg = "用户支付中";
              code =  1001;
          }

          map.put("code",code);
          map.put("successMsg",successMsg);
          map.put("obj",aliPayQueryResp);

        return map;
    }

    public AliRefundResp refund(AliRefundPara aliRefundPara,Integer businessType) throws Exception{
        AliRefundResp aliRefundResp = this.aliMicropay.refund(aliRefundPara);
        if(aliRefundResp == null){
            log.error("订单号 : [{}] ；申请退款单号 : [{}] ;退款失败 ：[{}]",aliRefundPara.getOut_trade_no(),
                    aliRefundPara.getOut_refund_no(),"服务降级");
            throw new BizException("申请退款失败,请重试");
        }
        // 先对return_code 进行判断
      //  Integer businessType = 3;  // 退押金
        TradeLog tradeLog = new TradeLog();
            tradeLog.setAppid(aliRefundPara.getAppid());
            tradeLog.setMchId(aliRefundPara.getMch_id());
            tradeLog.setBusinessTypeId(businessType);
            tradeLog.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessType));
            tradeLog.setReturnCode(aliRefundResp.getReturn_code());
            tradeLog.setReturnMsg(aliRefundResp.getReturn_msg());
            tradeLog.setChannel(PayChannelEnum.ZXALI.getChannelId());
            tradeLog.setOutTradeNo(aliRefundPara.getOut_trade_no());
            tradeLog.setOutRefundNo(aliRefundPara.getOut_refund_no());
            tradeLog.setTradeMethod(AliMethodNameConst.Ali_REFUND);
            tradeLog.setTradeType(TradeTypeEnum.REFUND.getDesc());
            tradeLog.setRefundFee(aliRefundResp.getRefund_fee());
            tradeLog.setTradeTime(aliRefundResp.getGmt_refund_pay());
            tradeLog.setResultCode(aliRefundResp.getResult_code());
            tradeLog.setErrCode(aliRefundResp.getErr_code());
            tradeLog.setErrCodeDes(aliRefundResp.getErr_code_des());
            tradeLog.setOperator(aliRefundPara.getOp_user_id());
            tradeLog.setGmtCreate(new Date());

        TradeResult tradeResult = new TradeResult();
            tradeResult.setAppid(aliRefundPara.getAppid());
            tradeResult.setMchId(aliRefundPara.getMch_id());
            tradeResult.setBusinessTypeId(businessType.longValue());
            tradeResult.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessType));
            tradeResult.setReturnCode(aliRefundResp.getReturn_code());
            tradeResult.setReturnMsg(aliRefundResp.getReturn_msg());
            tradeResult.setChannel(PayChannelEnum.ZXALI.getChannelId());
            tradeResult.setOutTradeNo(aliRefundPara.getOut_trade_no());
            tradeResult.setOutRefundNo(aliRefundPara.getOut_refund_no());
            tradeResult.setRefundFee(aliRefundResp.getRefund_fee());
            tradeResult.setTradeTime(aliRefundResp.getGmt_refund_pay());
            tradeResult.setTradeMethod(AliMethodNameConst.Ali_REFUND);
            tradeResult.setTradeType(TradeTypeEnum.REFUND.getDesc());
            tradeResult.setErrCode(aliRefundResp.getErr_code());
            tradeResult.setErrCodeDes(aliRefundResp.getErr_code_des());
            tradeResult.setResultCode(aliRefundResp.getResult_code());
            tradeResult.setOperator(aliRefundPara.getOp_user_id());
            tradeResult.setGmtCreate(new Date());

            log.info("tradeResult : [{}]",tradeResult);
            log.info("tradeLog : [{}]",tradeLog);

        if("FAIL".equalsIgnoreCase(aliRefundResp.getReturn_code())){
            log.error("订单号 : [{}] ；退款单号 : [{}] ;return_msg ：[{}]",aliRefundPara.getOut_trade_no(),
                    aliRefundPara.getOut_refund_no(),aliRefundResp.getReturn_msg());
            // 记录退款申请日志
             this.tradeLogService.logTrade(tradeLog);
             // 记录退款结果
            this.tradeResultService.logResult(tradeResult);
            throw new BizException(aliRefundResp.getReturn_msg());
        }

        if("FAIL".equalsIgnoreCase(aliRefundResp.getResult_code())){
            // 判断err_code
            log.error("订单号 : [{}] ；退款单号 : [{}] ;err_code_desc ：[{}]",aliRefundPara.getOut_trade_no(),
                    aliRefundPara.getOut_refund_no(),aliRefundResp.getErr_code_des());
            // 记录退款申请日志
            this.tradeLogService.logTrade(tradeLog);
            // 记录退款结果
            this.tradeResultService.logResult(tradeResult);
            throw new BizException(aliRefundResp.getErr_code_des());
        }


        tradeLog.setTransactionId(aliRefundResp.getTransaction_id());
        tradeLog.setOutRefundNo(aliRefundResp.getOut_refund_no());
        tradeLog.setPassRefundNo(aliRefundResp.getPass_refund_no());

        tradeResult.setTransactionId(aliRefundResp.getTransaction_id());
        tradeResult.setOutRefundNo(aliRefundResp.getOut_refund_no());
        tradeResult.setPassRefundNo(aliRefundResp.getPass_refund_no());

        this.tradeLogService.logTrade(tradeLog);
        this.tradeResultService.logResult(tradeResult);

        return aliRefundResp;
    }

    public Map<String,Object> refundQuery(AliRefundQueryPara aliRefundQueryPara)  throws Exception{
        AliRefundQueryResp aliRefundQueryResp = this.aliMicropay.refundQuery(aliRefundQueryPara);
        if(aliRefundQueryResp == null){
            log.error("订单号 : [{}] ；退款单号 : [{}] ;退款失败 ：[{}]",aliRefundQueryPara.getOut_trade_no(),
                    aliRefundQueryPara.getOut_refund_no(),"服务降级");
            throw new BizException("申请退款失败,请重试");
        }

        if("FAIL".equalsIgnoreCase(aliRefundQueryResp.getReturn_code())){
            log.error("订单号 : [{}] ; 退款单号 : [{}] ;return_msg ：[{}]",aliRefundQueryPara.getOut_trade_no(),
                    aliRefundQueryPara.getOut_refund_no(),aliRefundQueryResp.getReturn_msg());
            throw new BizException(aliRefundQueryResp.getReturn_msg());
        }

        if("FAIL".equalsIgnoreCase(aliRefundQueryResp.getResult_code())){
            // 判断err_code
            log.error("订单号 : [{}] ；退款单号 : [{}] ;err_code_desc ：[{}]",aliRefundQueryPara.getOut_trade_no(),
                    aliRefundQueryPara.getOut_refund_no(),aliRefundQueryResp.getErr_code_des());

            throw new BizException(aliRefundQueryResp.getErr_code_des());
        }
        // return_code 和 result_code 均为SUCCESS
           // 判断  refund_status
        String refund_status = aliRefundQueryResp.getRefund_status();
        Map<String,Object> map = new HashMap<>();
           Integer code ;
           String successMsg = null;
        if(AliRefundStatusEnum.SUCCESS.getDesc().equalsIgnoreCase(refund_status)){
              // 退款成功
            code = 0;
            successMsg = "退款成功";
        }else if(AliRefundStatusEnum.PROCESSING.getDesc().equalsIgnoreCase(refund_status)){
            // 退款处理中
           code = 1001;
           successMsg = "退款处理中";
        }else{
            code = 1002;
            successMsg = "退款失败";
        }
          map.put("code",code);
          map.put("successMsg",successMsg);
          map.put("obj",aliRefundQueryResp);
        return map;
    }

    public AliCancelResp cancel(AliCancelPara aliCancelPara)  throws Exception{
        AliCancelResp aliCancelResp = this.aliMicropay.cancel(aliCancelPara);
        if(aliCancelResp == null){
            log.error("订单号 : [{}] ;撤销失败 ：[{}]",aliCancelPara.getOut_trade_no(),"服务降级");
            throw new BizException("申请撤销失败,请重试");
        }

        TradeLog tradeLog = new TradeLog();
            tradeLog.setAppid(aliCancelPara.getAppid());
            tradeLog.setMchId(aliCancelPara.getMch_id());
            tradeLog.setBusinessTypeId(BusinessTypeEnum.CANCEL_PAY.getBusinessTypeId());
            tradeLog.setBusinessTypeDesc(BusinessTypeEnum.getDesc(BusinessTypeEnum.CANCEL_PAY.getBusinessTypeId()));
            tradeLog.setReturnCode(aliCancelResp.getReturn_code());
            tradeLog.setReturnMsg(aliCancelResp.getReturn_msg());
            tradeLog.setResultCode(aliCancelResp.getResult_code());
            tradeLog.setErrCode(aliCancelResp.getErr_code());
            tradeLog.setErrCodeDes(aliCancelResp.getErr_code_des());
            tradeLog.setChannel(PayChannelEnum.ZXALI.getChannelId());
            tradeLog.setOutTradeNo(aliCancelPara.getOut_trade_no());
            tradeLog.setTradeMethod(AliMethodNameConst.Ali_REVERSE);
            tradeLog.setTradeType(TradeTypeEnum.REVERSE.getDesc());
            tradeLog.setRecall(aliCancelResp.getRecall());
        //    tradeLog.setOperator(aliCancelPara.getOp_user_id());
            tradeLog.setGmtCreate(new Date());
        TradeResult tradeResult = new TradeResult();
            tradeResult.setAppid(aliCancelPara.getAppid());
            tradeResult.setMchId(aliCancelPara.getMch_id());
            tradeResult.setBusinessTypeId(BusinessTypeEnum.CANCEL_PAY.getBusinessTypeId().longValue());
            tradeResult.setBusinessTypeDesc(BusinessTypeEnum.getDesc(BusinessTypeEnum.CANCEL_PAY.getBusinessTypeId()));
            tradeResult.setReturnCode(aliCancelResp.getReturn_code());
            tradeResult.setReturnMsg(aliCancelResp.getReturn_msg());
            tradeResult.setChannel(PayChannelEnum.ZXALI.getChannelId());
            tradeResult.setOutTradeNo(aliCancelPara.getOut_trade_no());
            tradeResult.setTradeMethod(AliMethodNameConst.Ali_REVERSE);
            tradeResult.setTradeType(TradeTypeEnum.REVERSE.getDesc());
            tradeResult.setRecall(aliCancelResp.getRecall());
            tradeResult.setResultCode(aliCancelResp.getResult_code());
            tradeResult.setErrCode(aliCancelResp.getErr_code());
            tradeResult.setErrCodeDes(aliCancelResp.getErr_code_des());
            tradeResult.setGmtCreate(new Date());

        if("FAIL".equalsIgnoreCase(aliCancelResp.getReturn_code())){
            log.error("订单号 : [{}]  撤销失败;return_msg ：[{}]",aliCancelPara.getOut_trade_no(),
                               aliCancelResp.getReturn_msg());

            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            throw new BizException(aliCancelResp.getReturn_msg());
        }

        if("FAIL".equalsIgnoreCase(aliCancelResp.getResult_code())){
            // 判断err_code
            log.error("订单号 : [{}] 撤销失败;err_code_desc ：[{}]",aliCancelPara.getOut_trade_no(),
                   aliCancelResp.getErr_code_des());
            String code = "1";  // 不需要再次调用
            String recall = aliCancelResp.getRecall();
            if("Y".equalsIgnoreCase(recall)){
                // 需要再次调
               code = "4001";
            }
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            if(AliReverseCodeEnum.AQC_SYSTEM_ERROR.getCode().equalsIgnoreCase(aliCancelResp.getErr_code())){
                 throw new BizException(AliReverseCodeEnum.AQC_SYSTEM_ERROR.getReturnMsg());
            }
            throw new BizException(code,aliCancelResp.getErr_code_des());
        }
        this.tradeLogService.logTrade(tradeLog);
        this.tradeResultService.logResult(tradeResult);
        return aliCancelResp;
    }
}