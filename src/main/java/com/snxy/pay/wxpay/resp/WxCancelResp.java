package com.snxy.pay.wxpay.resp;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/12.
 */
@Data
@Builder
@NoArgsConstructor
public class WxCancelResp {
    private String charset;
    private String version;
    private String sign_type;
    private String return_code;
    private String return_msg;
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String result_code;
    private String recall; // 是否继续调用撤销
    private String err_code;
    private String err_code_des;



}