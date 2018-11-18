package com.snxy.pay.config.wx;

/**
 * Created by lvhai on 2018/11/9.
 * 方法常量定义
 */
public class WXMethodNameConst {

    /**
     * 微信反扫（刷卡）支付方法名
     */
    public static final String WX_MICROPAY = "mbupay.decorator.micropay";
    /**
     * 微信查询订单方法
     */
    public static final String WX_QUERY = "mbupay.decorator.query";

    /**
     * 微信申请退款接口
     */
    public static final String WX_REFUND = "mbupay.decorator.refund";
    /**
     * 微信查询退款接口
     */
    public static final String WX_REFUNDQUERY = "mbupay.decorator.refundquery";
    /**
     * 微信撤销订单接口
     */
    public static final String WX_REVERSE = "mbupay.decorator.reverse";

    /**
     * 微信对账单接口
     */
    public static final String WX_BILL = "mbupay.decorator.bill";
}
