package com.snxy.pay.config;

/**
 * Created by 24398 on 2018/11/19.
 */
public enum TradeTypeEnum {

    PAY(1,"支付"),
    REFUND(2,"退款"),
    REVERSE(3,"撤销订单");

    private Integer tradeType;
    private String  desc;

    TradeTypeEnum(Integer tradeType,String desc){
        this.tradeType = tradeType;
        this.desc = desc;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "TradeTypeEnum{" +
                "tradeType=" + tradeType +
                ", desc='" + desc + '\'' +
                '}';
    }
}
