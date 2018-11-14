package com.snxy.pay.config;

public enum MicroPayCodeEnum {

    SYSTEMERROR("SYSTEMERROR","接口返回错误"),
    PARAM_ERROR("PARAM_ERROR","参数错误"),
    ORDERPAID("ORDERPAID","订单已支付"),
    NOAUTH("NOAUTH","商户无权限"),
    AUTHCODEEXPIRE("AUTHCODEEXPIRE","二维码已过期请用户在微信上刷新后再试"),
    NOTENOUGH("NOTENOUGH","余额不足"),
    NOTSUPORTCARD("NOTSUPORTCARD"," "),
    ORDERCLOSED("ORDERCLOSED","订单已关闭"),
    ORDERREVERSED("ORDERREVERSED","订单已撤销"),
    BANKERROR("BANKERROR","银行系统异常"),
    USERPAYING("USERPAYING","用户支付中，需要输入密码"),
    AUTH_CODE_ERROR("AUTH_CODE_ERROR","授权码参数错误"),
    AUTH_CODE_INVALID("AUTH_CODE_INVALID","授权码检验错误"),
    XML_FORMAT_ERROR("XML_FORMAT_ERROR","XML 格式错误"),
    REQUIRE_POST_METHOD("REQUIRE_POST_METHOD","请使用post方法"),
    SIGNERROR("SIGNERROR","签名错误"),
    LACK_PARAMS("LACK_PARAMS","缺少参数"),
    NOT_UTF8("NOT_UTF8","编码格式错误"),
    BUYER_MISMATCH("BUYER_MISMATCH","支付帐号错误"),
    APPID_NOT_EXIST("APPID_NOT_EXIST","APPID 不存在"),
    MCHID_NOT_EXIST("MCHID_NOT_EXIST","MCHID 不存在"),
    OUT_TRADE_NO_USED("OUT_TRADE_NO_USED","商户订单号重复"),
    APPID_MCHID_NOT_MATCH("APPID_MCHID_NOT_MATCH","appid 和 mch_id不匹配");

    private String errCode ;
    private String errMsg;

    MicroPayCodeEnum(String errCode, String errMsg) {
        this.errCode = errCode ;
        this.errMsg = errMsg;

    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String toString() {
        return "MicroPayCodeEnum{" +
                "errCode='" + errCode + '\'' +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }

}
