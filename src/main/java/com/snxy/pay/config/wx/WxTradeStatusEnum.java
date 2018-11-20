package com.snxy.pay.config.wx;

/**
 * Created by 24398 on 2018/11/11.
 */
public enum WxTradeStatusEnum {

     /*  SUCCESS--支付成功   0
         REFUND--转入退款   1001
         NOTPAY--未支付     1002
         CLOSED--已关闭     1003
         REVOKED--已撤销（刷卡支付）1004
         USERPAYING--用户支付中   1005
         NOPAY--未支付(确认支付超时)  1006
         PAYERROR--支付失败(其他原因，如银 行返回失败) 1007   */


    SUCCESS("SUCCESS",0,"支付成功"),
    REFUND("REFUND",1001,"转入退款"),
    NOTPAY("NOTPAY",1002,"未支付"),
    CLOSED("CLOSED",1003,"已关闭"),
    REVOKED("REVOKED",1004,"已撤销"),
    USERPAYING("USERPAYING",1005,"用户支付中"),
    NOPAY("NOPAY",1006,"未支付"),
    PAYERROR("PAYERROR",1007,"支付失败");
    private String status ;
    private Integer code;
    private String msg ;
    WxTradeStatusEnum(String status ,Integer code, String msg){
       this.status = status;
       this.code = code;
       this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static WxTradeStatusEnum getByStatus(String status){
        WxTradeStatusEnum wxTradeStatusEnum = null;
        WxTradeStatusEnum[] enums = WxTradeStatusEnum.values();
        for(int i =0;i< enums.length;i++){
            if(enums[i].getStatus().equalsIgnoreCase(status)){
                wxTradeStatusEnum = enums[i];
            }
        }
        return wxTradeStatusEnum;
    }



}
