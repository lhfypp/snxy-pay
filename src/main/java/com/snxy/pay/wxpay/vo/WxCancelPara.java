package com.snxy.pay.wxpay.vo;

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
public class WxCancelPara {
    // private String method;
    // private String version;
    private String  appid;  // 不同商户的appid 和mch_id不同
    private String  mch_id;
 //   private String  nonce_str;
    //  private String sign;


    private String  out_trade_no; // 商户订单号


}
