package com.snxy.pay.service;

import com.netflix.loadbalancer.InterruptTask;
import com.snxy.common.exception.BizException;
import com.snxy.pay.config.*;
import com.snxy.pay.config.wx.WxMicroPayCodeEnum;
import com.snxy.pay.config.wx.WxQueryCodeEnum;
import com.snxy.pay.config.wx.WxRefundStatusEnum;
import com.snxy.pay.config.wx.WxTradeStatusEnum;
import com.snxy.pay.service.dto.*;
import com.snxy.pay.service.req.WxPayQueryReq;
import com.snxy.pay.service.resp.*;
import com.snxy.pay.service.vo.wx.*;
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


    public Map<String,Object> pay(WxPayPara wxPayPara) throws Exception {
        ///TODO 记录交易日志
        WxPayResp wxPayResp = this.wxMicroPay.pay(wxPayPara);
        //通信错误
        if ("FAIL".equals(wxPayResp.getReturn_code())) {
            // 支付失败
            log.error("商户单号 : [{}] ,支付失败 , returnCode :[{}] ,returnMsg :[{}]",wxPayPara.getOut_trade_no(),wxPayResp.getReturn_code(),wxPayResp.getReturn_msg() );
            throw new BizException("支付失败，"+wxPayResp.getReturn_msg());
        }
        //  通信成功   然后  根据result_code 值判断
        String resultCode = wxPayResp.getResult_code();
        log.debug("支付返回结果码值 resultCode : [{}]", resultCode);
        Map<String,Object> retMap = new HashMap();
        if("SUCCESS".equals(resultCode)){
            // 成功
            retMap.put("code",0);
            retMap.put("payDTO",null);
            return retMap ;
        }
        // 判断 err_code 这时候result_code
        String errCode = wxPayResp.getErr_code();
          log.info("errCode ; [{}]",errCode);
        if (WxMicroPayCodeEnum.SYSTEMERROR.getErrCode().equalsIgnoreCase(errCode)
                || WxMicroPayCodeEnum.BANKERROR.getErrCode().equalsIgnoreCase(errCode)) {
            //SYSTEMERROR 立即调用被扫订单结果查询 API
            // BANKERROR 请立即调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作。
            this.asyncQuery(wxPayPara, false);
            retMap.put("code",1001);
            retMap.put("msg","支付进行中");
            retMap.put("payDTO",null);
            return retMap;

        }

        if (WxMicroPayCodeEnum.USERPAYING.getErrCode().equalsIgnoreCase(errCode)) {
           //USERPAYING 等待 5 秒，然后调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作
            this.asyncQuery(wxPayPara, true);
            retMap.put("code",1001);
            retMap.put("msg","支付进行中");
            retMap.put("payDTO",null);
            return retMap;
        }
            ///TODO  支付失败 更新业务订单状态
        this.refreshOrderState(null);

        log.error("商户单号 ： [{}] ，支付失败 ：err_code_des : [{}]",wxPayPara.getOut_trade_no(),wxPayResp.getErr_code_des());
        retMap.put("code",1);
        retMap.put("msg",wxPayResp.getErr_code_des());
        return retMap;
    }


    private void asyncQuery(WxPayPara wxPayPara, boolean isFirstWait) {
        ///TODO 开启一个新的线程,执行如下方法,可考虑线程池去做，Executor
        threadPool.execute(() -> {
            long start = System.currentTimeMillis();
         //   long deadline = overtime + start;
            InterruptTask task = new InterruptTask(overtime);
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
                    log.debug("查询交易状态 trade_state:[{}]", tradeState);
                    String errCode = wxPayQueryResp.getErr_code();
                    log.debug("查询交易 errCode : [{}]",errCode);
                    if ((WxTradeStatusEnum.SUCCESS.getCode().equalsIgnoreCase(tradeState))) {
                        // 查询到支付成功跳出
                        log.debug("其它交易状态跳出:[{}]", tradeState);
                        paySuccess = true;
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
            WxRefundResp wxRefundResp = this.wxMicroPay.refund(wxRefundPara);
            if(wxRefundResp == null ){
                // 服务降级
                log.error("退款单号 : [{}] ;申请退款失败 服务降级 ; [{}]",wxRefundPara.getOut_refund_no(),wxRefundResp);
                throw new BizException( "申请退款失败,请重试");
            }
            if( "FAIL".equals(wxRefundResp.getReturn_code())){
                log.error("退款单号 : [{}] ;returnMsg ; [{}]",wxRefundPara.getOut_refund_no(),wxRefundResp.getReturn_msg());
                throw new BizException(wxRefundResp.getReturn_msg());
            } else if( !"SUCCESS".equals(wxRefundResp.getResult_code())){
                // 提交退款申请失败
                log.error("退款单号 : [{}] 申请退款失败; 错误码 ：[{}]",wxRefundPara.getOut_refund_no(),wxRefundResp.getErr_code_des());
                throw new BizException("申请退款失败 ,"+wxRefundResp.getErr_code_des());
            }
          // 提交退款申请成功
         refundDTO = RefundDTO.builder()
                           .out_refund_no(wxRefundResp.getOut_refund_no())
                           .refund_channel(wxRefundResp.getRefund_channel())
                           .refund_fee(wxRefundResp.getRefund_fee())
                           .out_trade_no(wxRefundResp.getOut_trade_no())
                           .build();

         return  refundDTO;
    }



    public WxRefundQueryResp refundQuery(WxRefundQueryPara wxRefundQueryPara) throws Exception{
      //  RefundQueryDTO refundQueryDTO = null;

            WxRefundQueryResp wxRefundQueryResp = this.wxMicroPay.refundQuery(wxRefundQueryPara);
            if(wxRefundQueryResp == null){
                // 服务降级
                log.error("退款单号 ：[{}] ,服务降级",wxRefundQueryPara.getOut_refund_no());
                throw new BizException("退款单号 "+wxRefundQueryPara.getOut_refund_no()+" 查询退款失败,请重试");
            }
            if("FAIL".equals(wxRefundQueryResp.getReturn_code())){
                // return_code 为fail
                log.error("退款单号 ：[{}] ,return_msg : [{}]",wxRefundQueryPara.getOut_refund_no(),wxRefundQueryResp.getReturn_msg());
                throw new BizException(wxRefundQueryResp.getReturn_msg());
            }else if(!"SUCCESS".equals(wxRefundQueryResp.getResult_code())){
                log.error("退款单号 :[{}] ,查询退款失败 : [{}]",wxRefundQueryPara.getOut_refund_no(),wxRefundQueryResp.getErr_code_des());
                throw new BizException(wxRefundQueryResp.getErr_code_des());
            }

             // return_code 和result_code 均为SUCCESS

              String refundStatus =  wxRefundQueryResp.getRefund_status();
              if(refundStatus != null){
                  String refundStatusMsg = WxRefundStatusEnum.getStatusMsg(refundStatus);
                  wxRefundQueryResp.setRefund_status_msg(refundStatusMsg);
              }

             return wxRefundQueryResp;

    }


    /***
     * 取消订单
     * @param wxCancelPara
     * @throws Exception
     */
    public WxCancelResp cancel(WxCancelPara wxCancelPara) throws Exception{
        WxCancelResp wxCancelResp = this.wxMicroPay.cancel(wxCancelPara);
        if(wxCancelResp == null ){
            // 服务降级和通讯失败
            log.error("退款单号 ： [{}] 撤销订单号失败 : [{}]",wxCancelPara.getOut_refund_no(),"服务降级");
            throw new BizException("取消订单失败,请重试");
        }
        if("FAIL".equals( wxCancelResp.getReturn_code())){
            log.error("退款单号 ：[{}] 撤销订单失败 ： [{}]",wxCancelPara.getOut_refund_no(),wxCancelResp.getReturn_msg());
            throw new BizException(wxCancelResp.getReturn_msg());
        }
        if(!"SUCCESS".equals(wxCancelResp.getResult_code())){
            log.error("退款单号 ： [{}] 取消订单号失败 : [{}]",wxCancelPara.getOut_refund_no(),wxCancelResp.getErr_code_des());
            throw new BizException(wxCancelResp.getErr_code_des());
        }

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

}
