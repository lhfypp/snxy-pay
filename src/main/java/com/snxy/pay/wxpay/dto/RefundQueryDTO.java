package com.snxy.pay.wxpay.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/12.
 */
@NoArgsConstructor
@Data
@Builder
public class RefundQueryDTO {
    private String out_trade_no;
    private String out_refund_no;
    private String refund_channel;  // ORIGINAL-原路退款   BALANCE-退回余额
    private String refund_status ; // SUCCESS 退款成功  FAIL--退款失败
    // PROCESSING--退款处理中  NOTSURE--未确定，需要商户原退款单号重新发起
    // CHANGE--转入代发，退款到
    // 银行发现用户的卡作废或者
    // 冻结了，导致原路退款银行
    // 卡失败，资金回流到商户的
    // 现金帐号，需要商户人工干
    // 预，通过线下或者财付通转
    //  账的方式进行退款。

}
