package com.snxy.pay.web;

import com.snxy.common.response.ResultData;
import com.snxy.pay.wxpay.dto.RefundDTO;
import com.snxy.pay.wxpay.dto.RefundQueryDTO;
import com.snxy.pay.wxpay.service.WxMicroPayService;
import com.snxy.pay.wxpay.vo.WxCancelPara;
import com.snxy.pay.wxpay.vo.WxPayPara;
import com.snxy.pay.wxpay.vo.WxRefundPara;
import com.snxy.pay.wxpay.vo.WxRefundQueryPara;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 24398 on 2018/11/11.
 */

@RestController
@Slf4j
@RequestMapping("/pay/wx")
public class WxPayController {

    @Resource
    private WxMicroPayService wxMicroPayService;

    /***
     * 微信支付
     * @param wxPayPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/pay")
    public ResultData wxPay(WxPayPara wxPayPara) throws Exception{
        // 参数校验
        Map<String, Object> map = wxMicroPayService.pay(wxPayPara);
        if((Integer)map.get("code") == 1){
            return ResultData.fail((String) map.get("msg"));
        }
        return ResultData.success((Integer) map.get("code"),(String) map.get("msg"),null);
    }

    /***
     * 申请退款
     * @param wxRefundPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/refund")
    public ResultData wxRefund(WxRefundPara wxRefundPara) throws Exception{
        RefundDTO refundDTO = this.wxMicroPayService.refund(wxRefundPara);
        return ResultData.success(refundDTO);
    }

    /***
     * 退款查询
     * @param wxRefundQueryPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/refund/query")
    public ResultData wxRefundQuery(WxRefundQueryPara wxRefundQueryPara) throws Exception{

        RefundQueryDTO refundQueryDTO = this.wxMicroPayService.refundQuery(wxRefundQueryPara);

        return ResultData.success(refundQueryDTO);
    }

    /**
     * 取消订单
     * @param wxCancelPara
     * @return
     * @throws Exception
     */
    @RequestMapping("/cancel")
    public ResultData wxCancel(WxCancelPara wxCancelPara) throws Exception{
        this.wxMicroPayService.cancel(wxCancelPara);
        return ResultData.success(null);
    }


}
