package com.snxy.pay.wxpay.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/14.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxBillReq {
    private String method;
    private String version;
    private String  appid;  // 不同商户的appid 和mch_id不同
    private String  mch_id;
    private String  nonce_str;
    private String sign;


    private String  bill_date; // 对账单日期  20140908
    private String bill_type;  // All 所有  SUCCESS 成功支付  REFUND 退款    REVOKED  已撤销


}
