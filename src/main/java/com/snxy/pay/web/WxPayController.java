package com.snxy.pay.web;

import com.snxy.common.response.ResultData;
import com.snxy.pay.service.dto.RefundDTO;
import com.snxy.pay.service.dto.WxBillDTO;
import com.snxy.pay.service.resp.WxCancelResp;
import com.snxy.pay.service.resp.WxPayQueryResp;
import com.snxy.pay.service.resp.WxRefundQueryResp;
import com.snxy.pay.service.WxMicroPayDecorator;
import com.snxy.pay.service.resp.WxRefundResp;
import com.snxy.pay.service.vo.wx.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by 24398 on 2018/11/11.
 */

@RestController
@Slf4j
@RequestMapping("/wx")
public class WxPayController {

    @Resource
    private WxMicroPayDecorator wxMicroPayDecorator;

    /***
     * 微信支付
     * @param wxPayPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/pay")
    public ResultData wxPay(WxPayPara wxPayPara,Integer businessType,String operatorId) throws Exception{
        // 参数校验
        Map<String, Object> map = this.wxMicroPayDecorator.pay(wxPayPara,businessType,operatorId);
        if((Integer)map.get("code") == 1){
            return ResultData.fail((String) map.get("msg"));
        }
        return ResultData.success((Integer) map.get("code"),(String) map.get("msg"), map.get("obj"));
    }

    /***
     * 查询订单支付情况
     * @param wxPayQueryPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/query")
    public ResultData wxPayQuery(WxPayQueryPara wxPayQueryPara) throws Exception{
        Map<String,Object> map = this.wxMicroPayDecorator.payQuery(wxPayQueryPara);
        Integer code = (Integer) map.get("code");
        String successMsg = (String) map.get("msg");
        WxPayQueryResp wxPayQueryResp = (WxPayQueryResp) map.get("obj");
        return ResultData.success(code,successMsg,wxPayQueryResp);
    }

    /***
     * 申请退款
     * @param wxRefundPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/refund")
    public ResultData wxRefund(WxRefundPara wxRefundPara,Integer businessType,String operatorId) throws Exception{
        WxRefundResp wxRefundResp = this.wxMicroPayDecorator.refund(wxRefundPara, businessType,operatorId);
        return ResultData.success(wxRefundResp);
    }

    /***
     * 退款查询
     * @param wxRefundQueryPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/refund/query")
    public ResultData wxRefundQuery(WxRefundQueryPara wxRefundQueryPara) throws Exception{

        Map<String,Object> map = this.wxMicroPayDecorator.refundQuery(wxRefundQueryPara);
          Integer code = (Integer) map.get("code");
          String successMsg = (String)map.get("successMsg");
          WxRefundResp wxRefundResp = (WxRefundResp) map.get("obj");
        return ResultData.success(code,successMsg,wxRefundResp);
    }

    /**
     * 取消订单
     * @param wxCancelPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/cancel")
    public ResultData wxCancel(WxCancelPara wxCancelPara,String operatorId) throws Exception{
        log.info("wxCancelPara : [{}]",wxCancelPara);
        WxCancelResp wxCancelResp = this.wxMicroPayDecorator.cancel(wxCancelPara,operatorId);
        return ResultData.success(wxCancelResp);
    }

    /***
     * 查询账单
     * @param wxBillPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/bill")
    public ResultData  wxBill(WxBillPara wxBillPara) throws Exception{
        WxBillDTO wxBillDTO = this.wxMicroPayDecorator.bill(wxBillPara);
        return ResultData.success(wxBillDTO);
    }

}
