package com.snxy.pay.config.ali;

/**
 * Created by 24398 on 2018/11/19.
 */
public enum  AliRefundStatusEnum {
    SUCCESS("SUCCESS","退款成功"),
    FAIL("FAIL","退款失败"),
    PROCESSING("PROCESSING","退款处理中");

    private String status;
    private String desc;
    AliRefundStatusEnum(String status,String desc){
          this.status = status;
          this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "AliRefundStatusEnum{" +
                "status='" + status + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
