package com.snxy.pay.fegin;

import org.springframework.stereotype.Component;

/**
 * Created by 24398 on 2018/11/10.
 */
@Component
public class ZxWxPayServiceFallBack implements ZxWxPayService{


    @Override
    public String bankService(byte[] data) {
        return null;
    }
}
