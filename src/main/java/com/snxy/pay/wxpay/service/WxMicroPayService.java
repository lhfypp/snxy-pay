package com.snxy.pay.wxpay.service;

import com.netflix.loadbalancer.InterruptTask;
import com.snxy.common.exception.BizException;
import com.snxy.pay.config.*;
import com.snxy.pay.wxpay.dto.*;
import com.snxy.pay.wxpay.req.WxPayQueryReq;
import com.snxy.pay.wxpay.resp.*;
import com.snxy.pay.wxpay.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.*;
import java.util.concurrent.ExecutorService;

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

}
