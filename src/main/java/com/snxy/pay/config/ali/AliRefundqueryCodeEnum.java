package com.snxy.pay.config.ali;

public enum AliRefundqueryCodeEnum {

    ACQ_SYSTEM_ERROR("ACQ_SYSTEM_ERROR","系统错误,重新发起请求"),
    ACQ_XML_ERROR("ACQ_XML_ERROR","XML 格式错误"),
    ACQ_INVALID_SIGN("ACQ_INVALID_SIGN","无效签名"),
    ACQ_INVALID_PARAMETER("ACQ_INVALID_PARAMETER","参数无效,检查请求参数"),
    ACQ_TRADE_NOT_EXIST("ACQ_TRADE_NOT_EXIST","查询退款的交易不存在,确认交易号是否为正确的支付宝交易号");

    private String code ;
    private String msg;
    private String returnMsg ;

    AliRefundqueryCodeEnum(String code, String msg, String returnMsg) {
        this.code = code;
        this.msg = msg;
        this.returnMsg = returnMsg;
    }

    AliRefundqueryCodeEnum(String code, String msg) {
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
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
}
