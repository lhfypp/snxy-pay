package com.snxy.pay.service.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/17.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliCancelResp {
    private String version;
    private String charset;
    private String sign_type;
    private String return_code;
    private String return_msg;
    private String appid;
    private String mch_id;
    private String recall;
    private String result_code;
    private String err_code;
    private String err_code_des;
    private String nonce_str;
    private String sign;


}
