package com.snxy.pay.config;

/**
 * Created by lvhai on 2018/11/9.
 * 方法常量定义
 */
public class ALIMethodNameConst {

    /**
     * 微信反扫（刷卡）支付方法名
     */
    public static final String WX_MICROPAY = "mbupay.wxpay.micropay";
    /**
     * 微信查询订单方法
     */
    public static final String WX_QUERY = "mbupay.wxpay.query";

    /**
     * 微信申请退款接口
     */
    public static final String WX_REFUND = "mbupay.wxpay.refund";
    /**
     * 微信查询退款接口
     */
    public static final String WX_REFUNDQUERY = "mbupay.wxpay.refundquery";
    /**
     * 微信撤销订单接口
     */
    public static final String WX_REVERSE = "mbupay.wxpay.reverse";

    /**
     * 微信对账单接口
     */
    public static final String WX_BILL = "mbupay.wxpay.bill";
}
