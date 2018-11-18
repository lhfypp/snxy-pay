package com.snxy.pay.config.wx;

public enum WxRefundQueryCodeEnum {

    SYSTEMERROR("SYSTEMERROR","接口返回错误"),
    INVALID_TRANSACTIONID("INVALID_TRANSACTIONID","无效 transaction_id"),
    PARAM_ERROR("PARAM_ERROR","参数错误"),
    APPID_NOT_EXIST("APPID_NOT_EXIST","APPID 不存在"),
    MCHID_NOT_EXIST("MCHID_NOT_EXIST","MCHID 不存在"),
    APPID_MCHID_NOT_MATCH("APPID_MCHID_NOT_MATCH","appid 和 mch_id 不匹配"),
    REQUIRE_POST_METHOD("REQUIRE_POST_METHOD","请使用 post 方法"),
    SIGNERROR("SIGNERROR","签名错误"),
    XML_FORMAT_ERROR("","XML 格式错误");

    private String errCode ;
    private String msg;

    WxRefundQueryCodeEnum(String errCode, String msg) {
        this.msg = msg ;
        this.errCode = errCode;
    }

    WxRefundQueryCodeEnum(String msg) {
        this.msg = msg ;
    }
    public void setMsg(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return msg;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
}
