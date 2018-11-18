package com.snxy.pay.service.dto;

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
public class WxBillSuccessPay {

     /* 交易时间,应用ID,商户ID,设备号,微信订单号,商户订单号,用户标识, 交易类
型,交易状态,付款银行,货币种类,总金额, 代金券或立减券优惠金额,商品名称,商
户数据包,手续费,费率,门店ID,门店名称,收银员ID,扩展参数1,扩展参数2,扩展参
数 3,扩展参数 4*/

    private String tradeTime;  // 交易时间                     0
    private String  appid;  // 不同商户的appid 和mch_id不同      1
    private String  mch_id;       // 商户ID                   2
    private String  device_info;  // 设备号                  3
    private String  transaction_id; // 微信订单号            4
    private String  out_trade_no;  // 商户订单号            5
    private String  openid ;  // 用户标识                    6
    private String  tradeType;  // 交易类型                  7
    private String  tradeStatus ;// 交易状态                  8
    private String  payBank; // 付款银行                      9
    private String  fee_type ;// 货币种类                      10
    private String  total_fee; // 总金额                      11
    private String  coupon_fee; // 代金券或立减券优惠金额       12
              /*  private String refund_id;  // 微信退款单号
                private String out_refund_no; // 商户退款单号
                private String refund_fee; // 退款金额
                private String coupon_refund_fee; // 代金券或立减券退款金额
                private String refund_type; // 退款类型
                private String refund_status;  // 退款状态*/
    private String goods_name;  // 商品名称                      13
    private String attach;   // 商户数据包                       14
    private String service_charge; // 手续费                      15
    private String rate; // 费率                                16
    private String store_appid; // 门店 ID                      17
    private String store_name; // 门店名称                     18
    private String cashier;  // 收银员 ID                        19
    private String extend_para1;  // 扩展参数 1                20
    private String extend_para2;  // 扩展参数 2               21
    private String extend_para3;  //  扩展参数 3                22
    private String extend_para4;  // 扩展参数 4                23


}
