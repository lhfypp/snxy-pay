package com.snxy.pay.wxpay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/15.
 */
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Summary {
    private String total_bill_no;
    private String total_fee;
    private String total_refund;
    private String total_coupon_refund_fee;  // 代金券或立减优惠退款金额
    private String total_service_fee; // 总手续费


}
