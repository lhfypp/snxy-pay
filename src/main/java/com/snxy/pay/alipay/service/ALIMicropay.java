package com.snxy.pay.alipay.service;

import com.snxy.pay.alipay.vo.ALIPayPara;
import com.snxy.pay.alipay.vo.ALIQueryPara;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvhai on 2018/11/9.
 * 阿里反扫支付
 */
@Component
public class ALIMicropay {
    public Map<String, String> pay(ALIPayPara aliPayPara) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("return_code", "SUCCESS");
       resultMap.put("result_code","PAYING");
        //        resultMap.put("result_code", "FAIL");
        resultMap.put("err_code", "ACQ.SYSTEM_ERROR");
        return resultMap;
    }

    public Map<String, String> query(ALIQueryPara aliQueryPara) {
        Map<String, String> resultMap = new HashMap<>();
//        resultMap.put("trade_state", "USERPAYING");
        resultMap.put("trade_state","CLOSED");
        return resultMap;
    }
}