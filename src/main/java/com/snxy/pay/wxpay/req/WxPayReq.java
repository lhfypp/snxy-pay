package com.snxy.pay.wxpay.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/10.
 */

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WxPayReq {
    private String version;
    private String method;
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;

    private String body;// 商品描述
    private String out_trade_no; // 商户订单号
    private Integer total_fee;  // 总金额，单位非分
    private String  spbill_create_ip; // 订单生成的及机器IP
    private String auth_code; // 支付授权码

}
