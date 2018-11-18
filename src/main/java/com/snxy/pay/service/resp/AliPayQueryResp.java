package com.snxy.pay.service.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/16.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliPayQueryResp {
    private String version;
    private String charset;
    private String sign_type;
    private String return_code;
    private String return_msg;
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String result_code;
    private String err_code;
    private String err_code_des;

    private  String trade_state;
    private String openid;
    private String trade_type;
    private String fee_type;
    private String total_fee;
    private String coupon_fee;
    private String transaction_id;
    private String pass_trade_no;
    private String out_trade_no;
    private String buyer_logon_id;
    private String fund_bill_list;


}
