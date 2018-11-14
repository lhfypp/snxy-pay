package com.snxy.pay.wxpay.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Created by 24398 on 2018/11/11.
 */
@Data
@Builder
public class PayDTO {
    private String bank_type;  // 付款银行
    private Integer total_fee; // 单位分
    private String transaction_id; // 微信订单号
    private String out_trade_no; // 商户订单号
    private String time_end; // yyyyMMddhhmmss
}