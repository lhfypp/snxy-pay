package com.snxy.pay.service.vo.ali;

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
public class AliPayQueryPara {
   private String appid;
   private String mch_id;
   private String out_trade_no;
}
