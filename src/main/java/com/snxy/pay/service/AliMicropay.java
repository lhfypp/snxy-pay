package com.snxy.pay.service;

import com.snxy.common.util.UUIDUtil;
import com.snxy.pay.config.ZhongXinServiceInfoConfig;
import com.snxy.pay.config.ali.AliMethodNameConst;
import com.snxy.pay.fegin.ZxPayService;
import com.snxy.pay.service.resp.*;
import com.snxy.pay.service.vo.ali.*;
import com.snxy.pay.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvhai on 2018/11/9.
 * 阿里反扫支付
 */
@Component
@Slf4j
public class AliMicropay {
    @Resource
    private ZhongXinServiceInfoConfig zsc;
    @Resource
    private ZxPayService zxPayService;

    public AliPayResp pay(AliPayPara aliPayPara) throws Exception {
        // 设置参数
        Map<String,String> map =  BeanUtils.describe(aliPayPara);
            map.remove("class");
            map.put("scene","bar_code");
            map.put("method", AliMethodNameConst.Ali_MICROPAY);
            map.put("version",this.zsc.getVersion());
            map.put("nonce_str", UUIDUtil.getUUID());
        log.info("请求map ： [{}]",map);
        //获取请求的字节数组
        byte[] bytes = CommonUtils.getReqByte(map,this.zsc.getKey());
        String payQueryResult = this.zxPayService.bankService(bytes);
        AliPayResp aliPayResp = CommonUtils.parse2Bean(payQueryResult,AliPayResp.class);

        return aliPayResp;
    }


    public AliPayQueryResp payQuery(AliPayQueryPara aliPayQueryPara) throws Exception {
        // 设置参数
        Map<String,String> map =  BeanUtils.describe(aliPayQueryPara);
        map.remove("class");
        map.put("method", AliMethodNameConst.Ali_QUERY);
        map.put("version",this.zsc.getVersion());
        map.put("nonce_str", UUIDUtil.getUUID());
        //获取请求的字节数组
        byte[] bytes = CommonUtils.getReqByte(map,this.zsc.getKey());
        String payQueryResult = this.zxPayService.bankService(bytes);
        AliPayQueryResp aliPayQueryResp = CommonUtils.parse2Bean(payQueryResult,AliPayQueryResp.class);

        return aliPayQueryResp;
    }

    public AliRefundResp refund(AliRefundPara aliRefundPara) throws Exception {
        Map<String,String> map = BeanUtils.describe(aliRefundPara);
        map.remove("class");
        map.put("method",AliMethodNameConst.Ali_REFUND);
        map.put("version",this.zsc.getVersion());
        map.put("nonce_str",UUIDUtil.getUUID());
        byte[] bytes = CommonUtils.getReqByte(map,this.zsc.getKey());
        String refundResult = this.zxPayService.bankService(bytes);
        AliRefundResp aliRefundResp = CommonUtils.parse2Bean(refundResult,AliRefundResp.class);
        return aliRefundResp;
    }

    public AliRefundQueryResp refundQuery(AliRefundQueryPara aliRefundQueryPara) throws Exception {
        Map<String,String> map = BeanUtils.describe(aliRefundQueryPara);
           map.remove("class");
           map.put("method",AliMethodNameConst.Ali_REFUNDQUERY);
           map.put("version",this.zsc.getVersion());
           map.put("nonce_str",UUIDUtil.getUUID());

        byte []  bytes = CommonUtils.getReqByte(map,this.zsc.getKey());
        String refundQueryResult = this.zxPayService.bankService(bytes);
        AliRefundQueryResp aliRefundQueryResp = CommonUtils.parse2Bean(refundQueryResult,AliRefundQueryResp.class);

        return aliRefundQueryResp;
    }

    public AliCancelResp cancel(AliCancelPara aliCancelPara) throws Exception {
        Map<String,String> map = BeanUtils.describe(aliCancelPara);
           map.remove("class");
           map.put("method",AliMethodNameConst.Ali_REVERSE);
           map.put("version",this.zsc.getVersion());
           map.put("nonce_str",UUIDUtil.getUUID());

        byte[] bytes = CommonUtils.getReqByte(map,this.zsc.getKey());
        String cancelResult = this.zxPayService.bankService(bytes);
        AliCancelResp aliCancelResp = CommonUtils.parse2Bean(cancelResult,AliCancelResp.class);
        return aliCancelResp;
    }
}