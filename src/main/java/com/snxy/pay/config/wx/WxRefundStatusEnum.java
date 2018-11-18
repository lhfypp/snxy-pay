package com.snxy.pay.config.wx;

/**
 * Created by 24398 on 2018/11/12.
 */
public enum WxRefundStatusEnum {
    SUCCESS("SUCCESS","成功"),
    FAIL("FAIL","退款失败"),
    PROCESSING("PROCESSING","退款处理中"),
    NOTSURE("NOTSURE","未确定，需要商户原退款单号重新发起"),
    CHANGE("CHANGE","需要通过线下方式进行退款");

    private String status;
    private String msg;
    WxRefundStatusEnum(String status, String msg){
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
        return "WxRefundStatusEnum{" +
                "status='" + status + '\'' +
                ", msg='" + msg + '\'' +
                '}';
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
}
