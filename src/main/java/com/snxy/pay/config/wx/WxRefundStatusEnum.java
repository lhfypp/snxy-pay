package com.snxy.pay.config.wx;

/**
 * Created by 24398 on 2018/11/12.
 */
public enum WxRefundStatusEnum {
    SUCCESS("SUCCESS",0,"退款成功"),
    FAIL("FAIL",1001,"退款失败"),
    PROCESSING("PROCESSING",1002,"退款处理中"),
    NOTSURE("NOTSURE",1003,"未确定，需要商户原退款单号重新发起"),
    CHANGE("CHANGE",1004,"用户银行卡作废或者被冻结，需要通过线下方式进行退款");

    private String status;
    private Integer code ;
    private String msg;
    WxRefundStatusEnum(String status, Integer code ,String msg){
        this.code = code;
        this.status = status;
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }



    public  static String getStatusMsg(String refundStatus){
        String msg = null;
        WxRefundStatusEnum[] enums = WxRefundStatusEnum.values();
        for(int i =0;i< enums.length;i++){
            if(enums[i].getStatus().equals(refundStatus)){
                msg = enums[i].getMsg();
            }
        }
        return msg;
    }

    public  static Integer getCode(String refundStatus){
        Integer code = null;
        WxRefundStatusEnum[] enums = WxRefundStatusEnum.values();
        for(int i =0;i< enums.length;i++){
            if(enums[i].getStatus().equals(refundStatus)){
                code = enums[i].getCode();
            }
        }
        return code;
    }
}
