package com.snxy.pay.config.wx;

public enum WxBillCodeEnum {

    SYSTEMERROR("SYSTEMERROR","接口返回错误"),
    INVALID_TRANSACTIONID("INVALID_TRANSACTIONID","无效 transaction_id"),
    PARAM_ERROR("PARAM_ERROR","参数错误");

    private String errCode ;
    private String msg;

    WxBillCodeEnum(String errCode, String msg) {
        this.errCode = errCode ;
        this.msg = msg;

    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    @Override
    public String toString() {
        return "WxBillCodeEnum{" +
                "errCode='" + errCode + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
