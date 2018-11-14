package com.snxy.pay.config;

/**
 * Created by 24398 on 2018/11/11.
 */
public enum  TradeStatusEnum {
    SUCCESS("SUCCESS","支付成功"),
    REFUND("REFUND","转入退款"),
    NOTPAY("NOTPAY","未支付"),
    CLOSED("CLOSED","已关闭"),
    REVOKED("REVOKED","已撤销"),
    USERPAYING("USERPAYING","用户支付中"),
    NOPAY("NOPAY","未支付"),
    PAYERROR("PAYERROR","支付失败");
    private String code ;
    private String msg ;
    TradeStatusEnum(String code ,String msg){
       this.code = code;
       this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "TradeStatusEnum{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
