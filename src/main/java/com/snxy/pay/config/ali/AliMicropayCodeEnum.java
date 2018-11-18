package com.snxy.pay.config.ali;

public  enum AliMicropayCodeEnum {

    ACQ_SYSTEM_ERROR("ACQ_SYSTEM_ERROR","接口返回错误,请立即调用查询订单 API，查询当前订单的状态 "),
    ACQ_XML_ERROR("ACQ_XML_ERROR","XML 格式错误"),
    ACQ_INVALID_SIGN("ACQ_INVALID_SIGN","无效签名"),
    ACQ_INVALID_APPID("ACQ_INVALID_APPID","商户 appid 和 mch_id 不存在或已停用或不匹配"),
    ACQ_ORDER_REPEAT("ACQ_ORDER_REPEAT","订单重复"),
    ACQ_INVALID_PARAMETER("ACQ_INVALID_PARAMETER","参数无效,检查请求参数"),
    ACQ_ACCESS_FORBIDDEN("ACQ_ACCESS_FORBIDDEN","无权限使用接口,未签约条码支付或者合同已到期"),
    ACQ_EXIST_FORBIDDEN_WORD("ACQ_EXIST_FORBIDDEN_WOR","订单信息中包含违禁词"),
    ACQ_PARTNER_ERROR("ACQ_PARTNER_ERROR","应用 APP_ID 填写错误"),
    ACQ_TOTAL_FEE_EXCEED("ACQ_TOTAL_FEE_EXCEED","订单总金额超过限额"),
    ACQ_PAYMENT_AUTH_CODE_INVALID("ACQ_PAYMENT_AUTH_CODE_INVALID","支付授权码无效"),
    ACQ_CONTEXT_INCONSISTENT("ACQ_CONTEXT_INCONSISTENT","交易信息被篡改"),
    ACQ_TRADE_HAS_SUCCESS("ACQ_TRADE_HAS_SUCCESS","交易已被支付"),
    ACQ_TRADE_HAS_CLOSE("ACQ_TRADE_HAS_CLOSE","交易已经关闭"),
    ACQ_BUYER_BALANCE_NOT_ENOUGH("ACQ_BUYER_BALANCE_NOT_ENOUGH","买家余额不足"),
    ACQ_BUYER_BANKCARD_BALANCE_NOT_ENOUGH("ACQ_BUYER_BANKCARD_BALANCE_NOT_ENOUGH","用户银行卡余额不足"),
    ACQ_ERROR_BALANCE_PAYMENT_DISABLE("ACQ_ERROR_BALANCE_PAYMENT_DISABLE","余额支付功能关闭"),
    ACQ_BUYER_SELLER_EQUAL("ACQ_BUYER_SELLER_EQUAL","买卖家不能相同,更换买家重新付款"),
    ACQ_TRADE_BUYER_NOT_MATCH("ACQ_TRADE_BUYER_NOT_MATCH","交易买家不匹配"),
    ACQ_BUYER_ENABLE_STATUS_FORBID("ACQ_BUYER_ENABLE_STATUS_FORBID","买家状态非法"),
    ACQ_PULL_MOBILE_CASHIER_FAIL("ACQ_PULL_MOBILE_CASHIER_FAIL","唤起移动收银台失败,用户刷新条码后，重新扫码发起请求"),
    ACQ_MOBILE_PAYMENT_SWITCH_OFF("ACQ_MOBILE_PAYMENT_SWITCH_OFF","用户的无线支付开关关闭"),
    ACQ_PAYMENT_FAIL("ACQ_PAYMENT_FAIL","支付失败"),
    ACQ_BUYER_PAYMENT_AMOUNT_DAY_LIMIT_ERROR("ACQ_BUYER_PAYMENT_AMOUNT_DAY_LIMIT_ERROR","买家付款日限额超限"),
    ACQ_BEYOND_PAY_RESTRICTION("ACQ_BEYOND_PAY_RESTRICTION","商户收款额度超限"),
    ACQ_BEYOND_PER_RECEIPT_RESTRICTION("ACQ_BEYOND_PER_RECEIPT_RESTRICTION","商户收款金额超过月限额"),
    ACQ_BUYER_PAYMENT_AMOUNT_MONTH_LIMIT_ERROR("ACQ_BUYER_PAYMENT_AMOUNT_MONTH_LIMIT_ERROR","买家付款月额度超限"),
    ACQ_SELLER_BEEN_BLOCKED("ACQ_SELLER_BEEN_BLOCKED","商家账号被冻结"),
    ACQ_ERROR_BUYER_CERTIFY_LEVEL_LIMIT("ACQ_ERROR_BUYER_CERTIFY_LEVEL_LIMIT","买家未通过人行认证"),
    ACQ_PAYMENT_REQUEST_HAS_RISK("ACQ_PAYMENT_REQUEST_HAS_RISK","支付有风险"),
    ACQ_NO_PAYMENT_INSTRUMENTS_AVAILABLE("ACQ_NO_PAYMENT_INSTRUMENTS_AVAILABLE","没用可用的支付工具"),
    ACQ_USER_FACE_PAYMENT_SWITCH_OFF("ACQ_USER_FACE_PAYMENT_SWITCH_OFF","用户当面付付款开关关闭");


    private String code;
    private String msg;
    private String returnMsg;

    AliMicropayCodeEnum(String code, String msg, String returnMsg){
        this.code = code;
        this.msg = msg;
        this.returnMsg = returnMsg;
    }

    AliMicropayCodeEnum(String code, String msg){
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
