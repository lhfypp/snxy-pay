package com.snxy.pay.config.wx;

public enum WxQueryCodeEnum {

    ORDERNOTEXIST("ORDERNOTEXIST","此交易订单号不存在"),
    SYSTEMERROR("SYSTEMERROR","系统错误"),
    TIMEOUT("TIMEOUT","发送外联平台服务器异常"),
    NOEXIST("NOEXIST","订单不存在");


    private String errCode ;
    private String msg;

    WxQueryCodeEnum(String errCode, String msg) {
        this.errCode = errCode ;
        this.msg = msg;

    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
}
