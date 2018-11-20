package com.snxy.pay.web;

import com.snxy.common.response.ResultData;
import com.snxy.pay.service.AliMicropayDecorator;
import com.snxy.pay.service.resp.*;
import com.snxy.pay.service.vo.ali.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by 24398 on 2018/11/17.
 */
@RestController
@Slf4j
@RequestMapping("/ali")
public class AliPayController {

    @Resource
    private AliMicropayDecorator aliMicropayDecorator;

    /***
     * 阿里反扫支付
     * @param aliPayPara
     * @return
     */
    @RequestMapping("/pay")
    public ResultData pay(AliPayPara aliPayPara,Integer businessType ) throws Exception{
        Map<String, Object> map = this.aliMicropayDecorator.pay(aliPayPara,businessType);
        Integer code = (Integer) map.get("code");
        String successMsg = (String) map.get("successMsg");
        AliPayResp aliPayResp = (AliPayResp) map.get("aliPayResp");
        return ResultData.success(code,successMsg,aliPayResp);
    }

    /***
     * 查询订单接口
     * @param aliPayQueryPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/pay/query")
    public ResultData payQuery(AliPayQueryPara aliPayQueryPara) throws Exception{
        Map<String,Object> map = this.aliMicropayDecorator.payQuery(aliPayQueryPara);
        Integer code = (Integer) map.get("code");
        String successMsg = (String) map.get("successMsg");
        AliPayQueryResp aliPayQueryResp = (AliPayQueryResp) map.get("obj");
        return ResultData.success(code,successMsg,aliPayQueryResp);
    }

    /***
     * 申请退款
     * @param aliRefundPara
     * @return
     */
    @RequestMapping("/refund")
    public ResultData refund(AliRefundPara aliRefundPara,Integer businessType ) throws Exception{
        AliRefundResp aliRefundResp = this.aliMicropayDecorator.refund(aliRefundPara,businessType);
        return ResultData.success(aliRefundResp);
    }

    /***
     * 退款查询
     * @param aliRefundQueryPara
     * @return
     */
    @RequestMapping("/refund/query")
    public ResultData refundQuery(AliRefundQueryPara aliRefundQueryPara) throws Exception{
        Map<String,Object> map = this.aliMicropayDecorator.refundQuery(aliRefundQueryPara);
        Integer code = (Integer) map.get("code");
        String successMsg = (String) map.get("successMsg");
        AliRefundQueryResp aliRefundQueryResp = (AliRefundQueryResp) map.get("obj");
        return ResultData.success(code,successMsg,aliRefundQueryResp);
    }

    /***
     * 取消订单
     * @param aliCancelPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/cancel")
    public ResultData cancel(AliCancelPara aliCancelPara) throws Exception{
        AliCancelResp aliCancelResp = this.aliMicropayDecorator.cancel(aliCancelPara);
        return ResultData.success(aliCancelResp);
    }


}
