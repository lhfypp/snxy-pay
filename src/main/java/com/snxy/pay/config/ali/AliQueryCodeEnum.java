package com.snxy.pay.config.ali;

public enum AliQueryCodeEnum {

    ACQ_SYSTEM_ERROR("ACQ_SYSTEM_ERROR","系统错误,重新发起请求"),
    ACQ_XML_ERROR("ACQ_XML_ERROR","XML 格式错误"),
    ACQ_INVALID_SIGN("ACQ_INVALID_SIGN","无效签名"),
    ACQ_INVALID_PARAMETER("ACQ_INVALID_PARAMETER","参数无效"),
    ACQ_TRADE_NOT_EXIST("ACQ_TRADE_NOT_EXIST","查询的交易不存在,检查传入的交易号是否正确"),
    IORU008("IORU008","发送外联平台服务器异常"),
    NOEXIST("NOEXIST","查询的交易不存在"),
    IORU009("IORU009","外联平台返回的报文为空");

    private String code;
    private String msg;
    private String returnMsg;

    AliQueryCodeEnum(String code, String msg, String returnMsg) {
        this.code = code;
        this.msg = msg;
        this.returnMsg = returnMsg;
    }

    AliQueryCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getReturnMsg() {
        if(this.returnMsg == null){
            return msg;
        }
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
}
