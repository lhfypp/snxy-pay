package com.snxy.pay.fegin;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by 24398 on 2018/11/10.
 */

@FeignClient(name = "zx-wx-decorator",url = "${zhongxin.serviceUrl}",fallback = ZxPayServiceFallBack.class)
public interface ZxPayService {
    @Headers({"Content-Type: text/xml"})
    @RequestMapping(method = RequestMethod.POST)
    String bankService(@RequestBody byte[] data);


}
