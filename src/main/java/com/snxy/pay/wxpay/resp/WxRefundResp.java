package com.snxy.pay.wxpay.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/12.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WxRefundResp {
    private String charset;
    private String version;
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

    private String refund_channel;//  ORIGINAL—原路退款 ; BALANCE—退回到余额
    private String refund_fee; // 退款金额
    private String out_trade_no;
    private String out_refund_no;
}
