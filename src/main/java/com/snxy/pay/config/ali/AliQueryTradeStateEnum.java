package com.snxy.pay.config.ali;

/**
 * Created by 24398 on 2018/11/18.
 */
public enum  AliQueryTradeStateEnum {

    SUCCESS("SUCCESS","支付成功"),
    CLOSED("CLOSED","已关闭"),
    USERPAYING("USERPAYING","用户支付中");
    public String tradeState;
    private String desc;
    AliQueryTradeStateEnum(String tradeState,String desc){
        this.tradeState = tradeState;
        this.desc = desc;
    }

    public String getTradeState() {
        return tradeState;
    }

    public void setTradeState(String tradeState) {
        this.tradeState = tradeState;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }



}
