package com.snxy.pay.service;

import com.netflix.loadbalancer.InterruptTask;
import com.snxy.common.exception.BizException;
import com.snxy.pay.config.ali.AliQueryCodeEnum;
import com.snxy.pay.config.ali.AliQueryTradeStateEnum;
import com.snxy.pay.service.resp.*;
import com.snxy.pay.service.vo.ali.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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


    public Map<String, Object> pay(AliPayPara aliPayPara) throws Exception {
        ///TODO 记录交易日志
        Map<String,Object> map = new HashedMap();
        Boolean payResult = false;
        AliPayResp aliPayResp = this.aliMicropay.pay(aliPayPara);
        if(aliPayResp == null){
            log.error("订单号 : [{}] 缴费失败 ; 服务降级",aliPayPara.getOut_trade_no());
            throw new BizException("服务异常，请重试");
        }
        // 先对return_code 进行判断
        if ("FAIL".equals(aliPayResp.getReturn_code())) {
            log.error("订单号 : [{}] 缴费失败 ：[{}]",aliPayPara.getOut_trade_no(),aliPayResp.getReturn_msg());
            throw new BizException(aliPayResp.getReturn_msg());
        }

        String resultCode = aliPayResp.getResult_code(); // SUCCESS PAYING  FAIL
        log.debug("支付返回结果码值resultCode:[{}]", resultCode);
        //result_code 是 PAYING 等待 5 秒，然后调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作
        if ("PAYING".equalsIgnoreCase(resultCode)) {
            asyncQuery(aliPayPara, true);
            map.put("code",1001);// 支付中
            map.put("aliPayResp",aliPayResp);
        } else if("SUCCESS".equalsIgnoreCase(resultCode)){
            map.put("code",0);  // 支付成功
            map.put("aliPayResp",aliPayResp);
        }else {
            String errCode = aliPayResp.getErr_code();
            if("ACQ.SYSTEM_ERROR".equalsIgnoreCase(errCode)){
               //SYSTEMERROR 立即调用被扫订单结果查询 API
                asyncQuery(aliPayPara, false);
                map.put("code",1001);// 支付中
                map.put("aliPayResp",aliPayResp);
            }else{
                // 支付失败
                log.error("订单号 ：[{}] ,支付失败 : [{}]",aliPayPara.getOut_trade_no(),aliPayResp.getErr_code_des());
                throw new BizException(aliPayResp.getErr_code_des());
            }
            ///TODO 更新业务订单状态
        }

        refreshOrderState(null);  // 针对每种情况均flush
        return map;
    }

    private void asyncQuery(AliPayPara aliPayPara, boolean isFirstWait)  {
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
            while (!Thread.interrupted()) {//(System.currentTimeMillis()< deadline) {
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

                if(!AliQueryTradeStateEnum.USERPAYING.getTradeState().equalsIgnoreCase(AliQueryTradeStateEnum.USERPAYING.getTradeState())
                        && !AliQueryCodeEnum.ACQ_SYSTEM_ERROR.getCode().equalsIgnoreCase(err_code)){

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
////                    e.printStackTrace();
                    break;
                }
            }
            task.cancel();
            log.debug("执行循环查询耗时:{}", (System.currentTimeMillis() - start));
            ///TODO 更新业务订单状态, 非必要继续查询状态或者超时了
            refreshOrderState(null);
        });

    }

    private void refreshOrderState(Map<String, String> map) {
        log.debug("更新订单状态");
    }

    public AliPayQueryResp payQuery(AliPayQueryPara aliPayQueryPara) throws Exception {
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
        return aliPayQueryResp;
    }

    public AliRefundResp refund(AliRefundPara aliRefundPara) throws Exception{
        AliRefundResp aliRefundResp = this.aliMicropay.refund(aliRefundPara);
        if(aliRefundResp == null){
            log.error("订单号 : [{}] ；退款单号 : [{}] ;退款失败 ：[{}]",aliRefundPara.getOut_trade_no(),
                    aliRefundPara.getOut_refund_no(),"服务降级");

            throw new BizException("申请退款失败,请重试");
        }

        if("FAIL".equalsIgnoreCase(aliRefundResp.getReturn_code())){
            log.error("订单号 : [{}] ；退款单号 : [{}] ;return_msg ：[{}]",aliRefundPara.getOut_trade_no(),
                    aliRefundPara.getOut_refund_no(),aliRefundResp.getReturn_msg());

            throw new BizException(aliRefundResp.getReturn_msg());
        }

        if("FAIL".equalsIgnoreCase(aliRefundResp.getResult_code())){
            // 判断err_code
            log.error("订单号 : [{}] ；退款单号 : [{}] ;err_code_desc ：[{}]",aliRefundPara.getOut_trade_no(),
                    aliRefundPara.getOut_refund_no(),aliRefundResp.getErr_code_des());

            throw new BizException(aliRefundResp.getErr_code_des());
        }

        return aliRefundResp;
    }

    public AliRefundQueryResp refundQuery(AliRefundQueryPara aliRefundQueryPara)  throws Exception{
        AliRefundQueryResp aliRefundQueryResp = this.aliMicropay.refundQuery(aliRefundQueryPara);
        if(aliRefundQueryResp == null){
            log.error("订单号 : [{}] ；退款单号 : [{}] ;退款失败 ：[{}]",aliRefundQueryPara.getOut_trade_no(),
                    aliRefundQueryPara.getOut_refund_no(),"服务降级");

            throw new BizException("申请退款失败,请重试");
        }

        if("FAIL".equalsIgnoreCase(aliRefundQueryResp.getReturn_code())){
            log.error("订单号 : [{}] ；退款单号 : [{}] ;return_msg ：[{}]",aliRefundQueryPara.getOut_trade_no(),
                    aliRefundQueryPara.getOut_refund_no(),aliRefundQueryResp.getReturn_msg());

            throw new BizException(aliRefundQueryResp.getReturn_msg());
        }

        if("FAIL".equalsIgnoreCase(aliRefundQueryResp.getResult_code())){
            // 判断err_code
            log.error("订单号 : [{}] ；退款单号 : [{}] ;err_code_desc ：[{}]",aliRefundQueryPara.getOut_trade_no(),
                    aliRefundQueryPara.getOut_refund_no(),aliRefundQueryResp.getErr_code_des());

            throw new BizException(aliRefundQueryResp.getErr_code_des());
        }

        return aliRefundQueryResp;
    }

    public AliCancelResp cancel(AliCancelPara aliCancelPara)  throws Exception{
        AliCancelResp aliCancelResp = this.aliMicropay.cancel(aliCancelPara);
        if(aliCancelResp == null){
            log.error("订单号 : [{}] ;撤销失败 ：[{}]",aliCancelPara.getOut_trade_no(),"服务降级");
            throw new BizException("申请撤销失败,请重试");
        }

        if("FAIL".equalsIgnoreCase(aliCancelResp.getReturn_code())){
            log.error("订单号 : [{}] ;return_msg ：[{}]",aliCancelPara.getOut_trade_no(),
                               aliCancelResp.getReturn_msg());
            throw new BizException(aliCancelResp.getReturn_msg());
        }

        if("FAIL".equalsIgnoreCase(aliCancelResp.getResult_code())){
            // 判断err_code
            log.error("订单号 : [{}] ；退款单号 : [{}] ;err_code_desc ：[{}]",aliCancelPara.getOut_trade_no(),
                   aliCancelResp.getErr_code_des());
            throw new BizException(aliCancelResp.getErr_code_des());
        }

        // 关于是否需要再次重调
        return aliCancelResp;
    }
}