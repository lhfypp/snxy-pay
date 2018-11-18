package com.snxy.pay.service.vo.ali;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/16.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AliRefundPara {

    private String appid;
    private String mch_id;
    private String out_trade_no;
    private String refund_fee;
    private String out_refund_no;
    private String op_user_id;

}
