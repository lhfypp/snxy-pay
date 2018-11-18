package com.snxy.pay.config.ali;

public enum AliPublicCodeEnum {

    ISP_UNKNOW_ERROR("ISP_UNKNOW_ERROR", "服务暂不可用（业务系统不可用）"),
    AOP_UNKNOW_ERROR("AOP_UNKNOW_ERROR", "服务暂不可用（网关自身的未知错误)"),
    AOP_INVALID_AUTH_TOKEN("AOP_INVALID_AUTH_TOKEN", "无效的访问令牌"),
    AOP_AUTH_TOKEN_TIME_OUT("AOP_AUTH_TOKEN_TIME_OUT", "访问令牌已过期"),
    AOP_INVALIA_APP_AUTH_TOKEN("AOP_INVALIA_APP_AUTH_TOKEN", "无效的应用授权令牌"),
    AOP_INVALID_APP_AUTH_TOKEN_NO_API("AOP_INVALID_APP_AUTH_TOKEN_NO_API", "商户未授权当前接口"),
    AOP_APP_AUTH_TOKEN_TIME_OUT("AOP_APP_AUTH_TOKEN_TIME_OUT", "应用授权令牌已过期"),
    AOP_NO_PRODUCT_REG_BY_PARTNER("AOP_NO_PRODUCT_REG_BY_PARTNER", "商户未签约任何产品"),
    ISV_MISSING_METHOD("ISV_MISSING_METHOD", "缺少方法名参数"),
    ISV_MISSING_SIGNATURE("ISV_MISSING_SIGNATURE", "缺少签名参数"),
    ISV_MISSING_SIGNATURE_TYPE("ISV_MISSING_SIGNATURE_TYPE", "缺少签名类型参数"),
    ISV_MISSING_SIGNATURE_KEY("ISV_MISSING_SIGNATURE_KEY", "缺少签名配置"),
    ISV_MISSING_APP_ID("ISV_MISSING_APP_ID","缺少 appId 参数"),
    ISV_MISSING_TIMESTAMP("ISV_MISSING_TIMESTAMP","缺少时间戳参数"),
    ISV_MISSING_VERSION("ISV_MISSING_VERSION","缺少版本参数"),
    ISV_DECRYPTION_ERROR_MISSING_ENCRYPT_TYPE("ISV_DECRYPTION_ERROR_MISSING_ENCRYPT_TYPE","解密出错, 未指定加密算法"),
    ISV_INVALID_PARAMETER("ISV_INVALID_PARAMETER","参数无效"),
    ISV_UPLOAD_FAIL("ISV_UPLOAD_FAIL","文件上传失败"),
    ISV_INVALID_FILE_EXTENSION("ISV_INVALID_FILE_EXTENSION","文件扩展名无效"),
    ISV_INVALID_FILE_SIZE("ISV_INVALID_FILE_SIZE","文件大小无效"),
    ISV_INVALIID_METHOD("ISV_INVALIID_METHOD","不存在的方法名"),
    ISV_INVALID_FORMAT("ISV_INVALID_FORMAT","无效的数据格式"),
    ISV_INVALID_SIGNATURE_TYPE("ISV_INVALID_SIGNATURE_TYPE","无效的签名类型"),
    ISV_INVALID_SIGNATURE("ISV_INVALID_SIGNATURE","无效签名"),
    ISV_INVALID_ENCRYPT_TYPE("ISV_INVALID_ENCRYPT_TYPE","无效的加密类型"),
    ISV_INVALID_ENCRYPT("ISV_INVALID_ENCRYPT","解密异常"),
    ISV_INVALID_APP_ID("ISV_INVALID_APP_ID","无效的 appId 参数"),
    ISV_INVALID_TIMESTAMP("ISV_INVALID_TIMESTAMP","非法的时间戳参数"),
    ISV_INVALID_CHARSET("ISV_INVALID_CHARSET","字符集错误"),
    ISV_INVALID_DIGEST("ISV_INVALID_DIGEST","摘要错误"),
    ISV_DECRYPTION_ERROR_NOT_VALID_ENCRYPT_TYPE("ISV_DECRYPTION_ERROR_NOT_VALID_ENCRYPT_TYPE","解密出错，不支持的加密算法"),
    ISV_DECRYPTION_ERROR_NOT_VALID_ENCRYPT_KEY("ISV_DECRYPTION_ERROR_NOT_VALID_ENCRYPT_KEY","解密出错, 未配置加密密钥或加密密钥格式错误"),
    ISV_DECRYPTION_ERROR_UNKNOWN("ISV_DECRYPTION_ERROR_UNKNOWN","解密出错，未知异常"),
    ISV_MISSING_SIGNATURE_CONFIG("ISV_MISSING_SIGNATURE_CONFIG","验签出错, 未配置对应签名算法的公钥或者证书"),
    ISV_NOT_SUPPORT_APP_AUTH("ISV_NOT_SUPPORT_APP_AUTH","本接口不支持第三方代理调用"),
    ISV_INSUFFICIENT_ISV_PERMISSIONS("ISV_INSUFFICIENT_ISV_PERMISSIONS","ISV 权限不足"),
    ISV_INSUFFICIENT_USER_PERMISSIONS("ISV_INSUFFICIENT_USER_PERMISSIONS","用户权限不足");

    private String code ;
    private String msg ;
    private String returnMsg ;

    AliPublicCodeEnum(String code, String msg) {
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
