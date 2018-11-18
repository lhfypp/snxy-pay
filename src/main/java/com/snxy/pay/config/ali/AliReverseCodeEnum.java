package com.snxy.pay.config.ali;

public enum AliReverseCodeEnum {

    AQC_SYSTEM_ERROR("AQC_SYSTEM_ERROR","系统错误,请使用相同的参数再次调用"),
    ACQ_XML_ERROR("ACQ_XML_ERROR","XML 格式错误"),
    ACQ_INVALID_SIGN("ACQ_INVALID_SIGN","无效签名"),
    ACQ_TRADE_SUCCESS_NOT_CANCEL("ACQ_TRADE_SUCCESS_NOT_CANCEL","交易成功，请使用退款接口撤销订单"),
    ACQ_TRADE_CANCEL_REPEAT("ACQ_TRADE_CANCEL_REPEAT","订单撤销重复提交，请调用订单查询接口查询订单状态"),
    ACQ_INVALID_PARAMETER("ACQ_INVALID_PARAMETER","参数无效 "),
    ACQ_TRADE_NOT_EXIST("ACQ_TRADE_NOT_EXIST","交易不存在,检查请求中的交易号和商户订单号是否正确"),
    ACQ_SELLER_BALANCE_NOT_ENOUGH("ACQ_SELLER_BALANCE_NOT_ENOUGH","商户的支付宝账户中无足够的资金进行撤销"),
    ACQ_REASON_TRADE_BEEN_FREEZEN("ACQ_REASON_TRADE_BEEN_FREEZEN","当前交易被冻结，不允许进行撤销"),;

    private String code;
    private String msg;
    private String returnMsg;

    AliReverseCodeEnum(String code, String msg, String returnMsg) {
        this.code = code;
        this.msg = msg;
        this.returnMsg = returnMsg;
    }

    AliReverseCodeEnum(String code, String msg) {
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
