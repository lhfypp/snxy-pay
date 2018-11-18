package com.snxy.pay.service.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/12.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxCancelResp {

 /*   [{transaction_id=4200000225201811162204368124, charset=UTF-8, nonce_str=2e091bb717cd4dcf8d08c10bb5379249,
            refund_status=SUCCESS, out_refund_no=1542332483421, sign=CCCAEC5B461B958AD557DE1840F69491,
            mch_id=m20180529001249122, refund_id=50100408762018111607014814586, version=2.0.1,
            pass_trade_no=1000120181116293175841, coupon_refund_fee=0, out_trade_no=1542332483424, refund_fee=1,
            appid=a20180529001249122,
            result_code=SUCCESS, pass_refund_no=20181116293783104, sign_type=MD5, return_code=SUCCESS}]*/

    private String charset;
    private String version;
    private String sign_type;
    private String return_code;
    private String return_msg;
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String pass_trade_no;
    private String pass_refund_no;
    private String refund_fee;
    private String sign;
    private String result_code;
    private String refund_id;
    private String refund_status;
    private String transaction_id;
    private String coupon_refund_fee;
    private String out_refund_no;
    private String out_trade_no;
    private String recall; // 是否继续调用撤销
    private String err_code;
    private String err_code_des;



}
