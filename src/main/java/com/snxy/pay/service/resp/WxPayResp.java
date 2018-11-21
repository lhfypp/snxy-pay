package com.snxy.pay.service.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/10.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WxPayResp{

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

   private String openid;
   private String is_subscribe;
   private String bank_type;  // 付款银行
   private Integer total_fee; // 单位分
   private String transaction_id; // 微信订单号
   private String out_trade_no; // 商户订单号
   private String time_end; // yyyyMMddhhmmss
}
