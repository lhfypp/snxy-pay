package com.snxy.pay.service;

import com.netflix.loadbalancer.InterruptTask;
import com.snxy.common.exception.BizException;
import com.snxy.pay.PayChannelEnum;
import com.snxy.pay.config.*;
import com.snxy.pay.config.wx.*;
import com.snxy.pay.domain.TradeLog;
import com.snxy.pay.domain.TradeResult;
import com.snxy.pay.service.dto.*;
import com.snxy.pay.service.req.WxPayQueryReq;
import com.snxy.pay.service.resp.*;
import com.snxy.pay.service.vo.wx.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by lvhai on 2018/11/9.
 */
@Component
@Slf4j
public class WxMicroPayDecorator {
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
    @Resource
    private TradeResultService tradeResultService;
    @Resource
    private TradeLogService tradeLogService;


    public Map<String,Object> pay(WxPayPara wxPayPara,Integer businessType,String operatorId) throws Exception {
        ///TODO 记录交易日志
        WxPayResp wxPayResp = this.wxMicroPay.pay(wxPayPara);
        if(wxPayResp == null){
            throw new BizException("系统异常，请重试");
        }

        // 先对return_code 进行判断
        TradeLog tradeLog = new TradeLog();
            tradeLog.setAppid(wxPayPara.getAppid());
            tradeLog.setMchId(wxPayPara.getMch_id());
            tradeLog.setBusinessTypeId(businessType);
            tradeLog.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessType));
            tradeLog.setReturnCode(wxPayResp.getReturn_code());
            tradeLog.setReturnMsg(wxPayResp.getReturn_msg());
            tradeLog.setBody(wxPayPara.getBody());
            tradeLog.setChannel(PayChannelEnum.ZXWX.getChannelId());
            tradeLog.setOutTradeNo(wxPayPara.getOut_trade_no());
            tradeLog.setTradeMethod(WXMethodNameConst.WX_MICROPAY);
            tradeLog.setTradeType(TradeTypeEnum.PAY.getDesc());
            tradeLog.setTotalFee(wxPayPara.getTotal_fee().toString());
            tradeLog.setErrCode(wxPayResp.getErr_code());
            tradeLog.setErrCodeDes(wxPayResp.getErr_code_des());
            tradeLog.setResultCode(wxPayResp.getResult_code());
            tradeLog.setOperator(operatorId);
            tradeLog.setGmtCreate(new Date());


        TradeResult tradeResult = new TradeResult();
            tradeResult.setAppid(wxPayPara.getAppid());
            tradeResult.setMchId(wxPayPara.getMch_id());
            tradeResult.setBusinessTypeId(businessType.longValue());
            tradeResult.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessType));
            tradeResult.setReturnCode(wxPayResp.getReturn_code());
            tradeResult.setReturnMsg(wxPayResp.getReturn_msg());
            tradeResult.setBody(wxPayPara.getBody());
            tradeResult.setChannel(PayChannelEnum.ZXWX.getChannelId());
            tradeResult.setOutTradeNo(wxPayPara.getOut_trade_no());
            tradeResult.setTradeMethod(WXMethodNameConst.WX_MICROPAY);
            tradeResult.setTradeType(TradeTypeEnum.PAY.getDesc());
            tradeResult.setTotalFee(wxPayPara.getTotal_fee().toString());
            tradeResult.setErrCode(wxPayResp.getErr_code());
            tradeResult.setErrCodeDes(wxPayResp.getErr_code_des());
            tradeResult.setResultCode(wxPayResp.getResult_code());
            tradeResult.setOperator(operatorId);
            tradeResult.setGmtCreate(new Date());

        //通信错误
        if ("FAIL".equals(wxPayResp.getReturn_code())) {
            // 支付失败
            log.error("商户单号 : [{}] ,支付失败 , returnCode :[{}] ,returnMsg :[{}]",wxPayPara.getOut_trade_no(),wxPayResp.getReturn_code(),wxPayResp.getReturn_msg() );
             // 记录
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            throw new BizException(wxPayResp.getReturn_msg());
        }

        //  通信成功   然后  根据result_code 值判断
        String resultCode = wxPayResp.getResult_code();
        log.debug("支付返回结果码值 resultCode : [{}]", resultCode);
        Map<String,Object> retMap = new HashMap();
        if("SUCCESS".equals(resultCode)){
            // 成功
            retMap.put("code",0);
            retMap.put("payDTO",wxPayResp);
            // 记录
            tradeLog.setOpenid(wxPayResp.getOpenid());
            tradeLog.setTransactionId(wxPayResp.getTransaction_id());
            tradeLog.setBankType(wxPayResp.getBank_type());
            tradeLog.setTradeTime(wxPayResp.getTime_end());
            tradeResult.setOpenid(wxPayResp.getOpenid());
            tradeResult.setBankType(wxPayResp.getBank_type());
            tradeResult.setTransactionId(wxPayResp.getTransaction_id());
            tradeResult.setTradeTime(wxPayResp.getTime_end());
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            return retMap ;
        }
        // 判断 err_code 这时候result_code
        String errCode = wxPayResp.getErr_code();
          log.info("errCode ; [{}]",errCode);
        if (WxMicroPayCodeEnum.SYSTEMERROR.getErrCode().equalsIgnoreCase(errCode)
                || WxMicroPayCodeEnum.BANKERROR.getErrCode().equalsIgnoreCase(errCode)) {
            //SYSTEMERROR 立即调用被扫订单结果查询 API
            // BANKERROR 请立即调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作。
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            this.asyncQuery(wxPayPara, tradeResult,false);
            retMap.put("code",1001);
            retMap.put("msg","支付进行中");
            retMap.put("obj",wxPayResp);
            // 记录
            return retMap;
        }

        if (WxMicroPayCodeEnum.USERPAYING.getErrCode().equalsIgnoreCase(errCode)) {
           //USERPAYING 等待 5 秒，然后调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作
            retMap.put("code",1001);
            retMap.put("msg","支付进行中");
            retMap.put("obj",wxPayResp);
            // 记录
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            log.info("tradeResult -----> : [{}]",tradeResult);
            this.asyncQuery(wxPayPara, tradeResult,true);
            return retMap;
        }

        log.error("商户单号 ： [{}] ，支付失败 ：err_code_des : [{}]",wxPayPara.getOut_trade_no(),wxPayResp.getErr_code_des());
        retMap.put("code",1);
        retMap.put("msg",wxPayResp.getErr_code_des());
        // 记录
        this.tradeLogService.logTrade(tradeLog);
        this.tradeResultService.logResult(tradeResult);
        return retMap;
    }


    private void asyncQuery(WxPayPara wxPayPara,TradeResult tradeResult, boolean isFirstWait) {
        ///TODO 开启一个新的线程,执行如下方法,可考虑线程池去做，Executor
        threadPool.execute(() -> {
            long start = System.currentTimeMillis();
            InterruptTask task = new InterruptTask(overtime);
            WxPayQueryResp wxPayQueryResp = null;
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
                    log.debug("查询交易状态 trade_state:[{}]", tradeState);
                    String errCode = wxPayQueryResp.getErr_code();
                    log.debug("查询交易 errCode : [{}]",errCode);
                    if ((WxTradeStatusEnum.SUCCESS.getStatus().equalsIgnoreCase(tradeState))) {
                        // 查询到支付成功跳出
                        log.debug("其它交易状态跳出:[{}]", tradeState);
                        break;
                    }else if(WxQueryCodeEnum.NOEXIST.getErrCode().equals(errCode) ||
                            WxQueryCodeEnum.ORDERNOTEXIST.getErrCode().equals(errCode)){
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
            if(wxPayQueryResp != null){
                String tradeState = wxPayQueryResp.getTrade_state();
                if(WxTradeStatusEnum.SUCCESS.getStatus().equalsIgnoreCase(tradeState)){
                    // 支付成功
                    tradeResult.setResultCode("SUCCESS");
                    tradeResult.setOpenid(wxPayQueryResp.getOpenid());
                    tradeResult.setTransactionId(wxPayQueryResp.getTransaction_id());
                    tradeResult.setGmtModified(new Date());
                    tradeResult.setTradeTime(wxPayQueryResp.getTime_end());
                    tradeResult.setBankType(wxPayQueryResp.getBank_type());

                    this.tradeResultService.updateSelective(tradeResult);
                }
            }

            // 修改记录结果
        });

    }


    public WxRefundResp refund(WxRefundPara wxRefundPara,Integer businessType,String operatorId) throws Exception{
        RefundDTO refundDTO = null;
        WxRefundResp wxRefundResp = this.wxMicroPay.refund(wxRefundPara);
        if(wxRefundResp == null ){
            // 服务降级
            log.error("退款单号 : [{}] ;申请退款失败 服务降级 ; [{}]",wxRefundPara.getOut_refund_no(),wxRefundResp);
            throw new BizException( "申请退款失败,请重试");
        }
        TradeLog tradeLog = new TradeLog();
            tradeLog.setAppid(wxRefundPara.getAppid());
            tradeLog.setMchId(wxRefundPara.getMch_id());
            tradeLog.setBusinessTypeId(businessType);
            tradeLog.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessType));
            tradeLog.setReturnCode(wxRefundResp.getReturn_code());
            tradeLog.setReturnMsg(wxRefundResp.getReturn_msg());
            tradeLog.setChannel(PayChannelEnum.ZXWX.getChannelId());
            tradeLog.setOutTradeNo(wxRefundPara.getOut_trade_no());
            tradeLog.setOutRefundNo(wxRefundPara.getOut_refund_no());
            tradeLog.setTradeMethod(WXMethodNameConst.WX_REFUND);
            tradeLog.setTotalFee(wxRefundPara.getTotal_fee().toString());
            tradeLog.setTradeType(TradeTypeEnum.REFUND.getDesc());
            tradeLog.setRefundFee(wxRefundPara.getRefund_fee().toString());
            tradeLog.setResultCode(wxRefundResp.getResult_code());
            tradeLog.setErrCode(wxRefundResp.getErr_code());
            tradeLog.setErrCodeDes(wxRefundResp.getErr_code_des());
        //    tradeLog.setOperator(wxRefundPara.getOp_user_id());
            tradeLog.setGmtCreate(new Date());
            tradeLog.setOperator(operatorId);

        TradeResult tradeResult = new TradeResult();
            tradeResult.setAppid(wxRefundPara.getAppid());
            tradeResult.setMchId(wxRefundPara.getMch_id());
            tradeResult.setBusinessTypeId(businessType.longValue());
            tradeResult.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessType));
            tradeResult.setReturnCode(wxRefundResp.getReturn_code());
            tradeResult.setReturnMsg(wxRefundResp.getReturn_msg());
            tradeResult.setChannel(PayChannelEnum.ZXWX.getChannelId());
            tradeResult.setOutTradeNo(wxRefundPara.getOut_trade_no());
            tradeResult.setOutRefundNo(wxRefundPara.getOut_refund_no());
            tradeResult.setTradeMethod(WXMethodNameConst.WX_REFUND);
            tradeResult.setTradeType(TradeTypeEnum.REFUND.getDesc());
            tradeResult.setTotalFee(wxRefundPara.getTotal_fee().toString());
            tradeResult.setRefundFee(wxRefundPara.getRefund_fee().toString());
            tradeResult.setResultCode(wxRefundResp.getResult_code());
            tradeResult.setErrCode(wxRefundResp.getErr_code());
            tradeResult.setErrCodeDes(wxRefundResp.getErr_code_des());
            //    tradeLog.setOperator(wxRefundPara.getOp_user_id());
            tradeResult.setOperator(operatorId);
            tradeResult.setGmtCreate(new Date());

        log.info("tradeResult : [{}]",tradeResult);
        log.info("tradeLog : [{}]",tradeLog);

        if( "FAIL".equals(wxRefundResp.getReturn_code())){
            log.error("退款单号 : [{}] ;returnMsg ; [{}]",wxRefundPara.getOut_refund_no(),wxRefundResp.getReturn_msg());
            // 记录日志与结果
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            throw new BizException(wxRefundResp.getReturn_msg());
        } else if( !"SUCCESS".equals(wxRefundResp.getResult_code())){
            // 提交退款申请失败
            log.error("退款单号 : [{}] 申请退款失败; 错误码 ：[{}]",wxRefundPara.getOut_refund_no(),wxRefundResp.getErr_code_des());
            // 记录日志与结果
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            throw new BizException("申请退款失败 ,"+ wxRefundResp.getErr_code_des());
        }
      // 提交退款申请成功
        tradeLog.setTransactionId(wxRefundResp.getTransaction_id());
        tradeLog.setRefundId(wxRefundResp.getRefund_id());
        tradeLog.setRefundFee(wxRefundResp.getRefund_fee());

        tradeResult.setTransactionId(wxRefundResp.getTransaction_id());
        tradeResult.setRefundId(wxRefundResp.getRefund_id());
        tradeResult.setRefundFee(wxRefundResp.getRefund_fee());
        // 记录日志与结果
        this.tradeLogService.logTrade(tradeLog);
        this.tradeResultService.logResult(tradeResult);

         return  wxRefundResp;
    }



    public Map<String,Object> refundQuery(WxRefundQueryPara wxRefundQueryPara) throws Exception{
      //  RefundQueryDTO refundQueryDTO = null;
            WxRefundQueryResp wxRefundQueryResp = this.wxMicroPay.refundQuery(wxRefundQueryPara);
            if(wxRefundQueryResp == null){
                // 服务降级
                log.error(" 商户单号 ：[{}] ; 退款单号 ：[{}] ,服务降级",wxRefundQueryPara.getOut_trade_no(),
                        wxRefundQueryPara.getOut_refund_no());
                throw new BizException(" 查询退款失败,请重试");
            }
            if("FAIL".equals(wxRefundQueryResp.getReturn_code())){
                // return_code 为fail
                log.error("商户单号 ：[{}] ;退款单号 ：[{}] ,return_msg : [{}]",wxRefundQueryPara.getOut_trade_no(),
                        wxRefundQueryPara.getOut_refund_no(),wxRefundQueryResp.getReturn_msg());
                throw new BizException(wxRefundQueryResp.getReturn_msg());
            }else if(!"SUCCESS".equals(wxRefundQueryResp.getResult_code())){
                log.error("商户单号 ：[{}] ;退款单号 :[{}] ,查询退款失败 : [{}]",wxRefundQueryPara.getOut_trade_no(),
                        wxRefundQueryPara.getOut_refund_no(),wxRefundQueryResp.getErr_code_des());
                throw new BizException(wxRefundQueryResp.getErr_code_des());
            }
           Map<String,Object> map = new HashMap<>();
             // return_code 和result_code 均为SUCCESS
            String refundStatus =  wxRefundQueryResp.getRefund_status();
            map.put("code",WxRefundStatusEnum.getCode(refundStatus));
            map.put("successMsg",WxRefundStatusEnum.getStatusMsg(refundStatus));
              if(refundStatus != null){
                  String refundStatusMsg = WxRefundStatusEnum.getStatusMsg(refundStatus);
                  wxRefundQueryResp.setRefund_status_msg(refundStatusMsg);
              }
            map.put("obj",wxRefundQueryResp);

             return map;

    }


    /***
     * 取消订单
     * @param wxCancelPara
     * @throws Exception
     */
    public WxCancelResp cancel(WxCancelPara wxCancelPara,String operatorId) throws Exception{
        WxCancelResp wxCancelResp = this.wxMicroPay.cancel(wxCancelPara);
        if(wxCancelResp == null ){
            // 服务降级和通讯失败
            log.error("商户单号 ： [{}] 撤销订单号失败 : [{}]",wxCancelPara.getOut_trade_no(),"服务降级");
            throw new BizException("取消订单失败,请重试");
        }
        Integer businessTypeId = BusinessTypeEnum.CANCEL_PAY.getBusinessTypeId();
        TradeLog tradeLog = new TradeLog();
            tradeLog.setAppid(wxCancelPara.getAppid());
            tradeLog.setMchId(wxCancelPara.getMch_id());
            tradeLog.setBusinessTypeId(businessTypeId);
            tradeLog.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessTypeId));
            tradeLog.setReturnCode(wxCancelResp.getReturn_code());
            tradeLog.setReturnMsg(wxCancelResp.getReturn_msg());
            tradeLog.setChannel(PayChannelEnum.ZXWX.getChannelId());
            tradeLog.setOutTradeNo(wxCancelPara.getOut_trade_no());
            tradeLog.setTradeMethod(WXMethodNameConst.WX_REVERSE);
            tradeLog.setTradeType(TradeTypeEnum.REVERSE.getDesc());
            tradeLog.setResultCode(wxCancelResp.getResult_code());
            tradeLog.setErrCode(wxCancelResp.getErr_code());
            tradeLog.setErrCodeDes(wxCancelResp.getErr_code_des());
            tradeLog.setOperator(operatorId);
            tradeLog.setGmtCreate(new Date());

        TradeResult tradeResult = new TradeResult();
            tradeResult.setAppid(wxCancelPara.getAppid());
            tradeResult.setMchId(wxCancelPara.getMch_id());
            tradeResult.setBusinessTypeId(businessTypeId.longValue());
            tradeResult.setBusinessTypeDesc(BusinessTypeEnum.getDesc(businessTypeId));
            tradeResult.setReturnCode(wxCancelResp.getReturn_code());
            tradeResult.setReturnMsg(wxCancelResp.getReturn_msg());
            tradeResult.setChannel(PayChannelEnum.ZXWX.getChannelId());
            tradeResult.setOutTradeNo(wxCancelPara.getOut_trade_no());
            tradeResult.setTradeMethod(WXMethodNameConst.WX_REVERSE);
            tradeResult.setTradeType(TradeTypeEnum.REVERSE.getDesc());
            tradeResult.setResultCode(wxCancelResp.getResult_code());
            tradeResult.setErrCode(wxCancelResp.getErr_code());
            tradeResult.setErrCodeDes(wxCancelResp.getErr_code_des());
            tradeResult.setOperator(operatorId);
            tradeResult.setGmtCreate(new Date());

            String recall = wxCancelResp.getRecall();
            String errCode = "1";
            if("N".equalsIgnoreCase(recall)){
               errCode = "1";
            }else{
                errCode = "4001" ;// 需要要继续调用撤销
            }
        if("FAIL".equals( wxCancelResp.getReturn_code())){
            log.error("商户单号 ：[{}] 撤销订单失败 ： [{}]",wxCancelPara.getOut_trade_no(),wxCancelResp.getReturn_msg());
            // 记录日志与结果
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            throw new BizException(errCode,wxCancelResp.getReturn_msg());
        }
        if(!"SUCCESS".equals(wxCancelResp.getResult_code())){
            log.error("商户单号 ： [{}] 取消订单号失败 : [{}]",wxCancelPara.getOut_trade_no(),wxCancelResp.getErr_code_des());
            // 记录日志与结果
            tradeLog.setRecall(recall);
            tradeResult.setRecall(recall);
            this.tradeLogService.logTrade(tradeLog);
            this.tradeResultService.logResult(tradeResult);
            throw new BizException(errCode,wxCancelResp.getErr_code_des());
        }
        // 记录日志与结果  撤销成功

        this.tradeLogService.logTrade(tradeLog);
        this.tradeResultService.logResult(tradeResult);

        return wxCancelResp;
    }


    public WxBillDTO bill(WxBillPara wxBillPara) throws Exception {
        WxBillResp wxBillResp = this.wxMicroPay.bill(wxBillPara);
        WxBillDTO wxBillDTO = null;
        if(wxBillResp.getMsg() != null){
            // 成功
            String msg = wxBillResp.getMsg();
            Integer sumStart = msg.indexOf("手续费总金额");
            String summaryStr = msg.substring(sumStart+6,msg.length());
            summaryStr.replaceAll("`","");
            String [] summerArr = summaryStr.split(",");
            Summary summary = new Summary();
            for(int i=0;i< summerArr.length;i++){
                // 总结
                String content = summerArr[i].trim();
                summary.setTotal_bill_no(content);
                summary.setTotal_fee(content);
                summary.setTotal_refund(content);
                summary.setTotal_coupon_refund_fee(content);
                summary.setTotal_service_fee(content);
            }
            // 针对不同的bill_type 获取不同的账单
            // All 所有  SUCCESS 成功支付  REFUND 退款    REVOKED  已撤销 文档没有已撤销
            String bill_type = wxBillPara.getBill_type();
            switch (bill_type){
                case "ALL" :   wxBillDTO = this.getWxBillAll(summary,msg); break;
                case "SUCCESS": wxBillDTO = this.getWxBillSuccessPay(summary,msg); break;
                case "REFUND" :  wxBillDTO = this.getWxBillRefund(summary,msg); break;
                case "REVOKED" : wxBillDTO =  null; break;
                default:  wxBillDTO = null ; break;
            }
        }else{
           // 查询失败
            log.error("查询账单失败 ：[{}]",wxBillResp.getReturn_msg());
            throw new BizException(wxBillResp.getReturn_msg());
        }


        return wxBillDTO;
    }


    public WxBillDTO<WxBillAll> getWxBillAll(Summary summary,String msg){
        Integer start = msg.indexOf("4");
        Integer end = msg.indexOf("`总交易单数");
        String billStr = msg.substring(start+1,end);
        List<WxBillAll> list = new ArrayList<>();
        if(billStr.length() > 0) {
            //有账单内容 去除 ` ``
            billStr.replaceAll("`|``","");
            String [] billStrArr = billStr.split(",");
            for(int i =0;i < billStr.length()/30;i++){
                WxBillAll wxBillAll = new WxBillAll();
                for(int j =0;j <30;j++){
                    Integer index = j + i*30;
                    String content = billStrArr[index];
                    wxBillAll.setTradeTime(content);
                    wxBillAll.setAppid(content);
                    wxBillAll.setMch_id(content);
                    wxBillAll.setDevice_info(content);
                    wxBillAll.setTransaction_id(content);
                    wxBillAll.setOut_trade_no(content);
                    wxBillAll.setOpenid(content);
                    wxBillAll.setTradeType(content);
                    wxBillAll.setPayBank(content);
                    wxBillAll.setFee_type(content);
                    wxBillAll.setTotal_fee(content);
                    wxBillAll.setCoupon_fee(content);
                    wxBillAll.setRefund_id(content);
                    wxBillAll.setOut_refund_no(content);
                    wxBillAll.setRefund_fee(content);
                    wxBillAll.setCoupon_refund_fee(content);
                    wxBillAll.setRefund_type(content);
                    wxBillAll.setRefund_status(content);
                    wxBillAll.setGoods_name(content);
                    wxBillAll.setAttach(content);
                    wxBillAll.setService_charge(content);
                    wxBillAll.setRate(content);
                    wxBillAll.setStore_appid(content);
                    wxBillAll.setStore_name(content);
                    wxBillAll.setCashier(content);
                    wxBillAll.setExtend_para1(content);
                    wxBillAll.setExtend_para2(content);
                    wxBillAll.setExtend_para3(content);
                    wxBillAll.setExtend_para4(content);
                }

                list.add(wxBillAll);
            }
        }

        WxBillDTO<WxBillAll> wxBillDTO = new WxBillDTO<>();
          wxBillDTO.setWxBills(list);
          wxBillDTO.setSummary(summary);

        return wxBillDTO;
    }

    public WxBillDTO<WxBillRefund> getWxBillRefund(Summary summary,String msg) {
        Integer start = msg.indexOf("4");
        Integer end = msg.indexOf("`总交易单数");
        String billStr = msg.substring(start + 1, end);
        WxBillDTO<WxBillRefund> wxBillDTO = new WxBillDTO<>();
        List<WxBillRefund> list = new ArrayList<>();
        if (billStr.length() > 0) {
            //有账单内容 去除 ` ``
            billStr.replaceAll("`|``", "");
            String[] billStrArr = billStr.split(",");
            for (int i = 0; i < billStr.length() / 31; i++) {
                WxBillRefund wxBillRefund = new WxBillRefund();
                for (int j = 0; j < 31; j++) {
                    Integer index = j + i * 31;
                    String content = billStrArr[index];
                    wxBillRefund.setTradeTime(content);
                    wxBillRefund.setAppid(content);
                    wxBillRefund.setMch_id(content);
                    wxBillRefund.setDevice_info(content);
                    wxBillRefund.setTransaction_id(content);
                    wxBillRefund.setOut_trade_no(content);
                    wxBillRefund.setOpenid(content);
                    wxBillRefund.setTradeType(content);
                    wxBillRefund.setTradeStatus(content);
                    wxBillRefund.setPayBank(content);
                    wxBillRefund.setFee_type(content);
                    wxBillRefund.setTotal_fee(content);
                    wxBillRefund.setCoupon_fee(content);
                    wxBillRefund.setApply_refund_time(content);
                    wxBillRefund.setRefund_time(content);
                    wxBillRefund.setRefund_id(content);
                    wxBillRefund.setOut_refund_no(content);
                    wxBillRefund.setRefund_fee(content);
                    wxBillRefund.setCoupon_refund_fee(content);
                    wxBillRefund.setRefund_type(content);
                    wxBillRefund.setRefund_status(content);
                    wxBillRefund.setGoods_name(content);
                    wxBillRefund.setAttach(content);
                    wxBillRefund.setService_charge(content);
                    wxBillRefund.setRate(content);
                    wxBillRefund.setStore_appid(content);
                    wxBillRefund.setStore_name(content);
                    wxBillRefund.setCashier(content);
                    wxBillRefund.setExtend_para1(content);
                    wxBillRefund.setExtend_para2(content);
                    wxBillRefund.setExtend_para3(content);
                    wxBillRefund.setExtend_para4(content);
                }

                list.add(wxBillRefund);
            }

            wxBillDTO.setSummary(summary);
            wxBillDTO.setWxBills(list);

        }

        return wxBillDTO;
    }

    public WxBillDTO<WxBillSuccessPay> getWxBillSuccessPay(Summary summary,String msg){
        Integer start = msg.indexOf("4");
        Integer end = msg.indexOf("`总交易单数");
        String billStr = msg.substring(start + 1, end);
        List<WxBillSuccessPay> list = new ArrayList<>();
        WxBillDTO<WxBillSuccessPay> wxBillDTO = new WxBillDTO<>();
        if (billStr.length() > 0) {
            //有账单内容 去除 ` ``
            billStr.replaceAll("`|``", "");
            String[] billStrArr = billStr.split(",");
            for (int i = 0; i < billStr.length() / 24; i++) {
                WxBillSuccessPay wxBillSuccessPay = new WxBillSuccessPay();
                for (int j = 0; j < 24; j++) {
                    Integer index = j + i * 24;
                    String content = billStrArr[index];
                    wxBillSuccessPay.setTradeTime(content);
                    wxBillSuccessPay.setAppid(content);
                    wxBillSuccessPay.setMch_id(content);
                    wxBillSuccessPay.setDevice_info(content);
                    wxBillSuccessPay.setTransaction_id(content);
                    wxBillSuccessPay.setOut_trade_no(content);
                    wxBillSuccessPay.setOpenid(content);
                    wxBillSuccessPay.setTradeType(content);
                    wxBillSuccessPay.setTradeStatus(content);
                    wxBillSuccessPay.setPayBank(content);
                    wxBillSuccessPay.setFee_type(content);
                    wxBillSuccessPay.setTotal_fee(content);
                    wxBillSuccessPay.setCoupon_fee(content);
               /*     wxBillRefund.setApply_refund_time(content);
                    wxBillRefund.setRefund_time(content);
                    wxBillRefund.setRefund_id(content);
                    wxBillRefund.setOut_refund_no(content);
                    wxBillRefund.setRefund_fee(content);
                    wxBillRefund.setCoupon_refund_fee(content);
                    wxBillRefund.setRefund_type(content);
                    wxBillRefund.setRefund_status(content);*/
                    wxBillSuccessPay.setGoods_name(content);
                    wxBillSuccessPay.setAttach(content);
                    wxBillSuccessPay.setService_charge(content);
                    wxBillSuccessPay.setRate(content);
                    wxBillSuccessPay.setStore_appid(content);
                    wxBillSuccessPay.setStore_name(content);
                    wxBillSuccessPay.setCashier(content);
                    wxBillSuccessPay.setExtend_para1(content);
                    wxBillSuccessPay.setExtend_para2(content);
                    wxBillSuccessPay.setExtend_para3(content);
                    wxBillSuccessPay.setExtend_para4(content);
                }

                list.add(wxBillSuccessPay);
            }

            wxBillDTO.setSummary(summary);
            wxBillDTO.setWxBills(list);

        }

        return wxBillDTO;

    }


    public static void main(String[] args) {
        String str = "2";
        String result = null;
        switch (str){
            case "1" : result = str ;break;
            case "2" : result = str + "000";break;
            case "3" : result = str +"1111";break;
            default:   result = "000";break;
        }

        log.info("result : [{}]",result);
    }

    public Map<String,Object> payQuery(WxPayQueryPara wxPayQueryPara) throws Exception {
        WxPayQueryResp wxPayQueryResp = this.wxMicroPay.payQuery(wxPayQueryPara);
        if(wxPayQueryResp == null ){
            // 服务降级和通讯失败
            log.error("商户单号 ： [{}] 查询订单支付情况失败 : [{}]",wxPayQueryPara.getOut_trade_no(),"服务降级");
            throw new BizException("取消订单失败,请重试");
        }
        if("FAIL".equals( wxPayQueryResp.getReturn_code())){
            log.error("商户单号 ：[{}] 查询订单支付情况失败 ： [{}]",wxPayQueryPara.getOut_trade_no(),wxPayQueryResp.getReturn_msg());
            throw new BizException(wxPayQueryResp.getReturn_msg());
        }

        if(!"SUCCESS".equals(wxPayQueryResp.getResult_code())){
            log.error("商户单号 ： [{}] 查询订单支付情况失败 : [{}]",wxPayQueryPara.getOut_trade_no(),wxPayQueryResp.getErr_code_des());
            throw new BizException(wxPayQueryResp.getErr_code_des());
        }

        // result_code     return_code 均为SUCCESS
      /*  SUCCESS--支付成功   0
         REFUND--转入退款   1001
         NOTPAY--未支付     1002
         CLOSED--已关闭     1003
         REVOKED--已撤销（刷卡支付）1004
         USERPAYING--用户支付中   1005
         NOPAY--未支付(确认支付超时)  1006
         PAYERROR--支付失败(其他原因，如银 行返回失败) 1007   */

        String trade_state = wxPayQueryResp.getTrade_state();
        WxTradeStatusEnum wxTradeStatusEnum = WxTradeStatusEnum.getByStatus(trade_state);
        Map<String,Object> map = new HashMap<>();
          map.put("code",wxTradeStatusEnum.getCode());
          map.put("msg",wxTradeStatusEnum.getMsg());
          map.put("obj",wxPayQueryResp);

        return map;
    }
}
