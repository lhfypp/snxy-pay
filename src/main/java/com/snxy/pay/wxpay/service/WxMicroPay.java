package com.snxy.pay.wxpay.service;

import com.snxy.common.exception.BizException;
import com.snxy.common.util.UUIDUtil;
import com.snxy.pay.config.WXMethodNameConst;
import com.snxy.pay.config.ZhongXinServiceInfoConfig;
import com.snxy.pay.fegin.ZxWxPayService;
import com.snxy.pay.util.MD5;
import com.snxy.pay.util.SignUtils;
import com.snxy.pay.util.XmlUtils;
import com.snxy.pay.wxpay.req.*;
import com.snxy.pay.wxpay.resp.*;
import com.snxy.pay.wxpay.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lvhai on 2018/11/9.
 * 微信反扫支付
 */
@Component
@Slf4j
public class WxMicroPay {

    @Resource
    private ZhongXinServiceInfoConfig zxc;
    @Resource
    private ZxWxPayService zxWxPayService;

    /***
     * 支付
     * @param wxPayPara
     * @return
     */
    public WxPayResp pay(WxPayPara wxPayPara) throws Exception {

        // 将对象属性封装在HashMap中
        // apach BeanUtils 把对象封装成Map时会对一个key为class，value值为对象类型的键值对，注意去掉
        Map<String,String> map =  BeanUtils.describe(wxPayPara);
        map.remove("class");
        map.put("method",WXMethodNameConst.WX_MICROPAY);
        map.put("version",zxc.getVersion());
        map.put("nonce_str",UUIDUtil.getUUID());
        // 请求bytes
        byte[] reqByte = this.getReqByte(map);
        String payResult = zxWxPayService.bankService(reqByte);
        // 将返回的字符串解析成对象
        WxPayResp wxPayResp = this.parse2Bean(payResult,WxPayResp.class);
        log.info("payResult ： [{}]",payResult);
;
        return wxPayResp;
    }


    public   byte[] getReqByte(Map<String,String> map) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
     /*   Map<String,String> map = BeanUtils.describe(r);
        map.remove("class");
        log.info("map : [{}]",map);*/
        // 去掉Map中参数，然后进行sign
        Map<String, String> filterParaMap = SignUtils.paraFilter(map);
        StringBuilder sb = new StringBuilder();
        SignUtils.buildPayParams(sb,filterParaMap,false);
        //  String preStr = sb.toString();
        String sign = MD5.sign(sb.toString(),"&key=" + zxc.getKey(),"utf-8").toUpperCase();
        map.put("sign",sign);
        log.info("afterSignMap  : [{}]",map);
        // 请求字符串
        String reqStr = XmlUtils.parseXML(map);
        return reqStr.getBytes();
    }

    public <T> T  parse2Bean(String resultStr,Class<T> targetObj) throws Exception{
        T bean = null;
        if(resultStr != null && resultStr.startsWith("<xml")){
            Map<String,String> resultMap = XmlUtils.xml2map(resultStr, "xml");
            log.info("resultMap : [{}]",resultMap);
             bean = targetObj.newInstance();
            bean = this.map2Bean(resultMap,targetObj);
            if(bean != null){
                log.info("wxPayResp  :  [{}]",bean);
            }else{
                throw new BizException("map 转bean 出错");
            }
        }
        return bean;
    }


    public  <T> T map2Bean(Map<String, String> map, Class<T> targetObj)throws Exception {
        T bean =  targetObj.newInstance();
        BeanUtils.populate(bean, map);
        return bean;
    }


    /**
     * 查询支付结果
     * @param wxPayQueryReq
     * @return
     * @throws Exception
     */
    public WxPayQueryResp query(WxPayQueryReq wxPayQueryReq) throws Exception{
        // 设置参数
        Map<String,String> map =  BeanUtils.describe(wxPayQueryReq);
            map.remove("class");
            map.put("method",WXMethodNameConst.WX_QUERY);
            map.put("version",zxc.getVersion());
            map.put("nonce_str",UUIDUtil.getUUID());
        //获取请求的字节数组
        byte[] bytes = this.getReqByte(map);
        String payQueryResult = this.zxWxPayService.bankService(bytes);
        WxPayQueryResp wxPayQueryResp = this.parse2Bean(payQueryResult,WxPayQueryResp.class);
        return wxPayQueryResp;
    }

    /***
     * 退款接口
     * @param wxRefundPara
     * @return
     * @throws Exception
     */
    public WxRefundResp refund(WxRefundPara wxRefundPara) throws Exception{
        Map<String,String> map =  BeanUtils.describe(wxRefundPara);
            map.remove("class");
            map.put("method",WXMethodNameConst.WX_REFUND);
            map.put("version",zxc.getVersion());
            map.put("nonce_str",UUIDUtil.getUUID());
         // 获取请求字节数组
        byte[] bytes = this.getReqByte(map);
        String refundResult = this.zxWxPayService.bankService(bytes);
        WxRefundResp wxRefundResp = this.parse2Bean(refundResult,WxRefundResp.class);
        return wxRefundResp;
    };


    /***
     * 查询退款
     * @return
     */
    public WxRefundQueryResp refundQuery(WxRefundQueryPara wxRefundQueryPara) throws Exception{
        // 设置参数
        Map<String,String> map =  BeanUtils.describe(wxRefundQueryPara);
            map.remove("class");
            map.put("method",WXMethodNameConst.WX_REFUNDQUERY);
            map.put("version",zxc.getVersion());
            map.put("nonce_str",UUIDUtil.getUUID());
        // 获取请求字节数组
        byte[] bytes = this.getReqByte(map);
        String refundQueryResult = this.zxWxPayService.bankService(bytes);
        WxRefundQueryResp wxRefundQueryResp = this.parse2Bean(refundQueryResult,WxRefundQueryResp.class);
        return wxRefundQueryResp;
    }


    public WxCancelResp cancel(WxCancelPara wxCancelPara) throws Exception{
        // 设置参数
        Map<String,String> map = BeanUtils.describe(wxCancelPara);
        map.remove("class");
        map.put("method",WXMethodNameConst.WX_REFUNDQUERY);
        map.put("version",zxc.getVersion());
        map.put("nonce_str",UUIDUtil.getUUID());
        log.info("map : [{}]",map);

         // 获取请求字节数组
        byte[] bytes = this.getReqByte(map);
        String cancelResult = this.zxWxPayService.bankService(bytes);
        WxCancelResp wxCancelResp = this.parse2Bean(cancelResult,WxCancelResp.class);
        return wxCancelResp;
    }

    public WxBillResp bill(WxBillPara wxBillPara) throws Exception {
        Map<String,String> map =  BeanUtils.describe(wxBillPara);
           map.remove("class");
           map.put("method",WXMethodNameConst.WX_BILL);
           map.put("version",zxc.getVersion());
           map.put("nonce_str",UUIDUtil.getUUID());

        // 获取请求字节数组
        byte[] bytes = this.getReqByte(map);
        String billResult = this.zxWxPayService.bankService(bytes);
        WxBillResp wxBillResp = this.parse2Bean(billResult,WxBillResp.class);
        if(wxBillResp == null){
            wxBillResp =  new WxBillResp();
        }
            wxBillResp.setMsg(billResult);
        return wxBillResp;
    }
}
