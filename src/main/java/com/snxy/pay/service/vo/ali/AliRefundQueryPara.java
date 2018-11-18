package com.snxy.pay.service.vo.ali;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/16.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliRefundQueryPara {
    private String appid;
    private String mch_id;
    private String out_trade_no;
    private String out_refund_no;
}
