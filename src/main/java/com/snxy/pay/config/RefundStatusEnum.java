package com.snxy.pay.config;

/**
 * Created by 24398 on 2018/11/12.
 */
public enum RefundStatusEnum {
    SUCCESS("SUCCESS","成功"),
    FAIL("FAIL","退款失败"),
    PROCESSING("PROCESSING","退款处理中"),
    NOTSURE("NOTSURE","未确定，需要商户原退款单号重新发起"),
    CHANGE("CHANGE","需要通过线下方式进行退款");

    private String status;
    private String msg;
    RefundStatusEnum(String status,String msg){
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

    @Override
    public String toString() {
        return "RefundStatusEnum{" +
                "status='" + status + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
