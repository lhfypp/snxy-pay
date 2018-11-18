package com.snxy.pay.service.vo.wx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/12.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxRefundPara {

  //  private String version;
  //  private String method;
    private String appid;
    private String mch_id;
 //   private String nonce_str;
  //  private String sign;

    private String out_trade_no; // 商户订单号
    private String out_refund_no; // 商户退款号
    private Integer total_fee;  // 总金额，单位非分
    private Integer refund_fee;  // 退款总金额

}
