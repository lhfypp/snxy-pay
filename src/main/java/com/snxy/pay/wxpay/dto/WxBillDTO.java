package com.snxy.pay.wxpay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by 24398 on 2018/11/15.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxBillDTO<T> {

    List<T> wxBills;
    Summary summary;
}
