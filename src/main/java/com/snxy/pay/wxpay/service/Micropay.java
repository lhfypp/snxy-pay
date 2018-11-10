package com.snxy.pay.wxpay.service;

import com.snxy.pay.wxpay.vo.WXPayPara;
import com.snxy.pay.wxpay.vo.WXQueryPara;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvhai on 2018/11/9.
 * 微信反扫支付
 */
@Component
public class Micropay {
    public Map<String,String> pay(WXPayPara wxPayPara){
        Map<String,String> resultMap=new HashMap<>();
        resultMap.put("return_code","SUCCESS");
        resultMap.put("result_code","USERPAYING");//"SYSTEMERROR");
        return resultMap;
    }
    public Map<String,String> query(WXQueryPara wxQueryPara){
        Map<String,String> resultMap=new HashMap<>();
        resultMap.put("trade_state","USERPAYING");
        return resultMap;
    }
}
