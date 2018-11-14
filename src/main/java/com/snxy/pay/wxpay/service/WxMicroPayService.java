package com.snxy.pay.wxpay.service;

import com.netflix.loadbalancer.InterruptTask;
import com.snxy.common.exception.BizException;
import com.snxy.pay.config.*;
import com.snxy.pay.wxpay.dto.RefundDTO;
import com.snxy.pay.wxpay.dto.RefundQueryDTO;
import com.snxy.pay.wxpay.req.WxPayQueryReq;
import com.snxy.pay.wxpay.resp.*;
import com.snxy.pay.wxpay.vo.WxCancelPara;
import com.snxy.pay.wxpay.vo.WxPayPara;
import com.snxy.pay.wxpay.vo.WxRefundPara;
import com.snxy.pay.wxpay.vo.WxRefundQueryPara;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lvhai on 2018/11/9.
 */
@Component
@Slf4j
public class WxMicroPayService {
    @Resource
    private ZhongXinServiceInfoConfig zsc;
    @Resource
    private WxMicroPay wxMicroPay;
    private static final int firstWaitTime = 5 * 1000;
    private static final int interval = 10 * 1000;
    private static final int overtime = 30 * 1000;
    //创建一个可重用固定线程数的线程池
    @Resource
    ExecutorService threadPool ;


    public Map<String,Object> pay(WxPayPara wxPayPara) throws Exception {
        ///TODO 记录交易日志

        WxPayResp wxPayResp = wxMicroPay.pay(wxPayPara);
        //通信错误
        if ("FAIL".equals(wxPayResp.getReturn_code())) {
            // 支付失败
            log.error(wxPayPara.getOut_trade_no() +"支付失败，通信错误");
            throw new BizException("支付失败，通信错误");
        }
        //  通信成功   然后  根据result_code 值判断
        String resultCode = wxPayResp.getResult_code();
        log.debug("支付返回结果码值resultCode:[{}]", resultCode);
        Map<String,Object> retMap = new HashMap();
        if("SUCCESS".equals(resultCode)){
            // 成功
            retMap.put("code",0);
            retMap.put("payDTO",null);
            return retMap ;
        }
        String errCode = wxPayResp.getErr_code();

        if (MicroPayCodeEnum.SYSTEMERROR.getErrCode().equalsIgnoreCase(errCode)
                || MicroPayCodeEnum.BANKERROR.getErrCode().equalsIgnoreCase(errCode)) {
            //SYSTEMERROR 立即调用被扫订单结果查询 API
            // BANKERROR 请立即调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作。
            this.asyncQuery(wxPayPara, false);

            retMap.put("code",1001);
            retMap.put("msg","支付进行中");
            retMap.put("payDTO",null);
            return retMap;

        } else if (MicroPayCodeEnum.USERPAYING.getErrCode().equalsIgnoreCase(errCode)) {
           //USERPAYING 等待 5 秒，然后调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作
            this.asyncQuery(wxPayPara, true);
            retMap.put("code",1001);
            retMap.put("msg","支付进行中");
            retMap.put("payDTO",null);
            return retMap;
        } else {
            ///TODO  支付失败 更新业务订单状态
            refreshOrderState(null);
        }
        retMap.put("code",1);
        retMap.put("msg",wxPayResp.getErr_code_des());
        return retMap;
    }


    private void asyncQuery(WxPayPara wxPayPara, boolean isFirstWait) {

        ///TODO 开启一个新的线程,执行如下方法,可考虑线程池去做，Executor
        threadPool.execute(() -> {
            long start = System.currentTimeMillis();
            long deadline = overtime + start;

            InterruptTask task = new InterruptTask(deadline - System.currentTimeMillis());
            WxPayQueryResp wxPayQueryResp = null;
            Boolean paySuccess = false;
            if (isFirstWait) {
                try {
                    log.debug("停止一段时间开始执行:{}", firstWaitTime);
                    Thread.sleep(firstWaitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (!Thread.interrupted()) {
                ///TODO 由wxPayPara生成查询参数
                WxPayQueryReq wxPayQueryReq = WxPayQueryReq.builder()
                                       .appid(wxPayPara.getAppid())
                                       .mch_id(wxPayPara.getMch_id())
                                       .out_trade_no(wxPayPara.getOut_trade_no())
                                       .build();
                try {
                    wxPayQueryResp = this.wxMicroPay.query(wxPayQueryReq);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ///TODO 需要判断 return_code 和 result_code
                   // 注意服务降级的判断
                if(wxPayQueryResp != null){
                    String tradeState = wxPayQueryResp.getTrade_state();
                    log.debug("查询交易状态trade_state:[{}]", tradeState);
                    //USERPAYING--用户支付中
                    //PAYERROR--支付失败(其他原因，如银行返回失败),这个需要支持吗？不支持
                  /*  if (!("USERPAYING".equalsIgnoreCase(tradeState))) {
                        log.debug("其它交易状态跳出:[{}]", tradeState);
                        break;
                    }*/
                    String errCode = wxPayQueryResp.getErr_code();
                    if ((TradeStatusEnum.SUCCESS.getCode().equalsIgnoreCase(tradeState))) {
                        // 查询到支付成功跳出
                        log.debug("其它交易状态跳出:[{}]", tradeState);
                        paySuccess = true;
                        break;
                    }else if(QueryCodeEnum.NOEXIST.getErrCode().equals(errCode) ||
                            QueryCodeEnum.ORDERNOTEXIST.getErrCode().equals(errCode)){
                        // 订单不存在，直接跳出
                        log.error("此订单:[{}] 不存在",wxPayQueryReq.getOut_trade_no());
                        break;
                    }

                       // 其他状态继续进行循环查询直至超时
                }
                // 没有查询到支付成功状态，则轮询直至到超时时间结束
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
              //  判断是否查询成功
            if(paySuccess){
                // 查询到支付成功
            }else{
                // 支付失败
            }
            this.refreshOrderState(null);
        });

    }

    private void refreshOrderState(Map<String, String> map) {
        log.debug("更新订单状态");
    }


    public RefundDTO refund(WxRefundPara wxRefundPara) throws Exception{
          RefundDTO refundDTO = null;
        for(int i =0;i< 2;i++){
            // 最多重试3次
            WxRefundResp wxRefundResp = this.wxMicroPay.refund(wxRefundPara);
            if(wxRefundResp == null || wxRefundResp.getReturn_code().equals("FAIL")){
                // 服务降级 和通讯失败
                throw new BizException("退款单号 "+wxRefundPara.getOut_refund_no()+" 申请退款失败 "+"服务异常");
            } else if( wxRefundResp.getResult_code().equals("SUCCESS")){
                  // 提交退款申请成功
                 refundDTO = RefundDTO.builder()
                                               .out_refund_no(wxRefundResp.getOut_refund_no())
                                               .refund_channel(wxRefundResp.getRefund_channel())
                                               .refund_fee(wxRefundResp.getRefund_fee())
                                               .out_trade_no(wxRefundResp.getOut_trade_no())
                                               .build();

                 return  refundDTO;
            }else if(wxRefundResp.getResult_code().equals(RefundCodeEnum.SYSTEMERROR.getErrCode())){
                if(i == 1){
                    // 提交退款申请失败
                    throw new BizException("退款单号 "+wxRefundPara.getOut_refund_no()+" 申请退款失败 "+wxRefundResp.getErr_code_des());
                }else{
                    // 重试
                    try{
                      Thread.sleep(2000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    continue;
                }
            }else{
                throw new BizException("退款单号 "+wxRefundPara.getOut_refund_no()+" 申请退款失败 "+wxRefundResp.getErr_code_des());
            }


        }
        return refundDTO;
    }



    public RefundQueryDTO refundQuery(WxRefundQueryPara wxRefundQueryPara) throws Exception{
        RefundQueryDTO refundQueryDTO = null;
        for(int i =0;i < 2;i++){
            WxRefundQueryResp wxRefundQueryResp = this.wxMicroPay.refundQuery(wxRefundQueryPara);
            if(wxRefundQueryResp == null || wxRefundQueryResp.getReturn_code().equals("FAIL")){
                // 服务降级 和通讯失败
                throw new BizException("退款单号 "+wxRefundQueryPara.getOut_refund_no()+" 查询退款失败 "+"服务异常");
            }else if(wxRefundQueryResp.getResult_code().equals("SUCCESS")){
                 // return_code 和result_code 均为SUCCESS
                 refundQueryDTO = RefundQueryDTO.builder()
                                                .out_refund_no(wxRefundQueryResp.getOut_refund_no())
                                                .refund_channel(wxRefundQueryResp.getRefund_channel())
                                                .out_trade_no(wxRefundQueryResp.getOut_trade_no())
                                                .refund_status(wxRefundQueryResp.getRefund_status())
                                                .build();

                 return refundQueryDTO;
            }else if(wxRefundQueryResp.getErr_code().equals(RefundQueryCodeEnum.SYSTEMERROR.getErrCode())){
                  // 系统超时判断是否重新调用api
                if(i == 1){
                    // 认为本次查询退款操作失败
                    throw new BizException("退款单号 "+wxRefundQueryPara.getOut_refund_no()+"查询退款失败 "+wxRefundQueryResp.getErr_code_des());
                }else{
                    // 重新调用api
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }else{
                throw new BizException("退款单号 "+wxRefundQueryResp.getOut_refund_no()+"查询退款失败 "+wxRefundQueryResp.getErr_code_des());
            }

        }
        return null;
    }


    /***
     * 取消订单
     * @param wxCancelPara
     * @throws Exception
     */
    public void cancel(WxCancelPara wxCancelPara) throws Exception{
        WxCancelResp wxCancelResp = this.wxMicroPay.cancel(wxCancelPara);
        if(wxCancelResp == null || wxCancelResp.getReturn_code().equals("FAIL")){
            // 服务降级和通讯失败
            String tipMsg = null;
            if(wxCancelResp == null){
                tipMsg = "服务降级";
            }else{
                tipMsg = "通讯失败";
            }
            log.error("商户订单号 ： [{}] 取消订单号失败 : [{}]",wxCancelPara.getOut_trade_no(),tipMsg);
            throw new BizException("取消订单失败,请重试");
        }else if(!wxCancelResp.getResult_code().equals("SUCCESS")){
            log.error("商户订单号 ： [{}] 取消订单号失败 : [{}]",wxCancelPara.getOut_trade_no(),wxCancelResp.getErr_code_des());
            throw new BizException("取消订单失败 ："+wxCancelResp.getErr_code_des());
        }

    }






}
