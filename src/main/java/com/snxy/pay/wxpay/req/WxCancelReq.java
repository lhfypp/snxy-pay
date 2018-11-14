package com.snxy.pay.wxpay.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/12.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WxCancelReq {

    private String version;
    private String method;
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;

    private String out_trade_no; // 商户订单号

}
