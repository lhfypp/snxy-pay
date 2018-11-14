package com.snxy.pay.config;

public enum CancelCodeEnum {

    SYSTEMERROR("SYSTEMERROR","接口返回错误"),
    INVALID_TRANSACTIONID("INVALID_TRANSACTIONID","无效transaction_id"),
    PARAM_ERROR("PARAM_ERROR","参数错误"),
    REQUIRE_POST_METHOD("REQUIRE_POST_METHOD","请使用 post 方法"),
    SIGNERROR("SIGNERROR","签名错误");

    private String errCode ;
    private String msg;

    CancelCodeEnum(String errCode, String msg) {
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
        return "CancelCodeEnum{" +
                "errCode='" + errCode + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
