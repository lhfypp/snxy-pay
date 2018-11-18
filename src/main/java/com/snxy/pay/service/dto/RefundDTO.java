package com.snxy.pay.service.dto;

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
public class RefundDTO {
    private String refund_channel;//  ORIGINAL—原路退款 ; BALANCE—退回到余额
    private String refund_fee; // 退款金额
    private String out_refund_no; // 退单号
    private String out_trade_no; // 订单号
}
