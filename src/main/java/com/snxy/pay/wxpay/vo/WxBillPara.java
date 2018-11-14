package com.snxy.pay.wxpay.vo;

/**
 * Created by 24398 on 2018/11/12.
 */
public class WxBillPara {
    private String  appid;  // 不同商户的appid 和mch_id不同
    private String  mch_id;

    private String  bill_date; // 对账单日期  20140908
    private String bill_type;  // All 所有  SUCCESS 成功支付  REFUND 退款    REVOKED  已撤销

}
