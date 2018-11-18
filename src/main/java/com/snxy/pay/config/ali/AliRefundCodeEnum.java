package com.snxy.pay.config.ali;

public enum AliRefundCodeEnum {

    ACQ_SYSTEM_ERROR("ACQ_SYSTEM_ERROR","系统错误,请使用相同的参数再次调用"),
    ACQ_XML_ERROR("ACQ_XML_ERROR","XML 格式错误"),
    ACQ_INVALID_SIGN("ACQ_INVALID_SIGN","无效签名"),
    ACQ_REFUND_ACCEPT("ACQ_REFUND_ACCEPT","退款请求已处理"),
    ACQ_INVALID_PARAMETER("ACQ_INVALID_PARAMETER","参数无效,请求参数有错，重新检查请求后，再调用退款"),
    ACQ_SELLER_BALANCE_NOT_ENOUGH("ACQ_SELLER_BALANCE_NOT_ENOUGH","卖家余额不足"),
    ACQ_REFUND_AMT_NOT_EQUAL_TOTAL("ACQ_REFUND_AMT_NOT_EQUAL_TOTAL","退款金额超限"),
    ACQ_REASON_TRADE_BEEN_FREEZEN("ACQ_REASON_TRADE_BEEN_FREEZEN","请求退款的交易被冻结"),
    ACQ_TRADE_NOT_EXIST("ACQ_TRADE_NOT_EXIST","交易不存在,检查请求中的交易号和商户订单号是否正确，"),
    ACQ_TRADE_HAS_FINISHED("ACQ_TRADE_HAS_FINISHED","交易已完结,该交易已完结，不允许进行退款"),
    ACQ_TRADE_STATUS_ERROR("ACQ_TRADE_STATUS_ERROR","交易状态非法,查询交易，确认交易是否已经付款"),
    ACQ_DISCORDANT_REPEAT_REQUEST("ACQ_DISCORDANT_REPEAT_REQUEST","不一致的请求,检查该退款号是否已退过款或更换退款号重新发起请求"),
    ACQ_REASON_TRADE_REFUND_FEE_ERR("ACQ_REASON_TRADE_REFUND_FEE_ERR","退款金额无效"),
    ACQ_TRADE_NOT_ALLOW_REFUND("ACQ_TRADE_NOT_ALLOW_RE","当前交易不允许退款");


    private String code ;
    private String msg ;
    private String returnMsg;

    AliRefundCodeEnum(String code, String msg, String returnMsg) {
        this.code = code;
        this.msg = msg;
        this.returnMsg = returnMsg;
    }

    AliRefundCodeEnum(String code, String msg) {
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
