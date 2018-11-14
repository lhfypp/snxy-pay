package com.snxy.pay.config;


import lombok.Data;

/**
 * Created by lvhai on 2018/11/9.
 */
@Data
public class ZhongXinServiceInfoConfig {
    private String localIp;
    private String serviceUrl;
    private String notifyUrl;
    private String  version;
    private String key;
}
