package com.snxy.pay.wxpay.req;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/11.
 */

@Data
@Builder
@NoArgsConstructor

public class WxPayQueryReq {
    private String version;
    private String method;
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;

    private String out_trade_no; // 商户订单号
}
