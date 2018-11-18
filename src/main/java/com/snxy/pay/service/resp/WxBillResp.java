package com.snxy.pay.service.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 24398 on 2018/11/14.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WxBillResp {
    private String version;
    private String return_code;
    private String return_msg;
    private String msg;
}
