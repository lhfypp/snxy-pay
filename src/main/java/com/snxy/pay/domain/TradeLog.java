package com.snxy.pay.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeLog {
    private Long id;

    private Integer businessTypeId;

    private String businessTypeDesc;

    private Integer channel;

    private String wxAppid;

    private String returnCode;

    private String returnMsg;

    private String resultCode;

    private String errCode;

    private String errCodeDes;

    private String tradeTime;

    private String appid;

    private String mchId;

    private String deviceInfo;

    private String transactionId;

    private String outTradeNo;

    private String openid;

    private String tradeMethod;

    private String tradeType;

    private String bankType;

    private String feeType;

    private String totalFee;

    private String couponFee;

    private String refundId;

    private String passRefundNo;

    private String outRefundNo;

    private String recall;

    private String refundFee;

    private String couponRefundFee;

    private String body;

    private String detail;

    private String serviceCharge;

    private String rate;

    private String storeId;

    private String storeName;

    private String operator;

    private String extend1;

    private String extend2;

    private String extend3;

    private String extend4;

    private Date gmtCreate;

    private Date gmtModified;

    private Byte isDelete;

}