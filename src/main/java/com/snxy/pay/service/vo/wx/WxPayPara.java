package com.snxy.pay.service.vo.wx;

import lombok.*;

/**
 * Created by lvhai on 2018/11/9.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WxPayPara {

    private String  appid;  // 不同商户的appid 和mch_id不同
    private String  mch_id;

    private String  body;// 商品描述
    private String  out_trade_no; // 商户订单号
    private Integer total_fee;  // 总金额，单位非分
    private String  spbill_create_ip; // 订单生成的及机器IP
    private String  auth_code; // 支付授权码
}
