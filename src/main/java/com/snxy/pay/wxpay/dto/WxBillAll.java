package com.snxy.pay.wxpay.dto;

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
public class WxBillAll {

   /* 交易时间,应用ID,商户ID,设备号,微信订单号,商户订单号,用户标识, 交易类型,
    交易状态,付款银行,货币种类,总金额,代金券或立减券优惠金额,微信退款单号,
    商户退款单号,退款金额,代金券或立减券退款金额，退款类型，退款状态,商品名
    称,商户数据包,手续费,费率,门店 ID,门店名称,收银员 ID,扩展参数 1,扩展参数 2,
    扩展参数 3,扩展参数 4*/

   private String tradeTime;  // 交易时间         0
   private String  appid;  // 不同商户的appid 和mch_id不同   1
   private String  mch_id;       // 商户ID   2
   private String  device_info;  // 设备号   3
   private String  transaction_id; // 微信订单号  4
   private String  out_trade_no;  // 商户订单号   5
   private String  openid ;  // 用户标识   6
   private String  tradeType;  // 交易类型  7
   private String  tradeStatus ;// 交易状态 8
    private String  payBank; // 付款银行 9
    private String  fee_type ;// 货币种类  10
    private String  total_fee; // 总金额   11
    private String  coupon_fee; // 代金券或立减券优惠金额  12
    private String refund_id;  // 微信退款单号  13
    private String out_refund_no; // 商户退款单号  14
    private String refund_fee; // 退款金额  15
    private String coupon_refund_fee; // 代金券或立减券退款金额  16
    private String refund_type; // 退款类型   17
    private String refund_status;  // 退款状态  18
    private String goods_name;  // 商品名称   19
    private String attach;   // 商户数据包  20
    private String service_charge; // 手续费  21
    private String rate; // 费率             22
    private String store_appid; // 门店 ID  23
    private String store_name; // 门店名称  24
    private String cashier;  // 收银员 ID    25
    private String extend_para1;  // 扩展参数 1  26
    private String extend_para2;  // 扩展参数 2   27
    private String extend_para3;  //  扩展参数 3   28
    private String extend_para4;  // 扩展参数 4   29




}
