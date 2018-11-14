package com.snxy.pay.wxpay.service;

import com.netflix.loadbalancer.InterruptTask;
import com.snxy.pay.wxpay.vo.WXPayPara;
import com.snxy.pay.wxpay.vo.WXQueryPara;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lvhai on 2018/11/9.
 */
@Component
@Slf4j
public class MicropayDecorator extends Micropay {

    private static final int firstWaitTime = 5 * 1000;
    private static final int interval = 10 * 1000;
    private static final int overtime = 30 * 1000;
    //创建一个可重用固定线程数的线程池
    @Autowired
    ExecutorService threadPool ;//= Executors.newCachedThreadPool();

    @Override
    public Map<String, String> pay(WXPayPara wxPayPara) {
        ///TODO 记录交易日志


        Map<String, String> resultMap = super.pay(wxPayPara);
        //通信错误
        if ("FAIL".equals(resultMap.get("return_code"))) {
            return resultMap;
        }
        String resultCode = resultMap.get("result_code");
        log.debug("支付返回结果码值resultCode:[{}]", resultCode);
        //SYSTEMERROR 立即调用被扫订单结果查询 API
        // BANKERROR 请立即调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作。
        //USERPAYING 等待 5 秒，然后调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作
        if ("SYSTEMERROR".equalsIgnoreCase(resultCode) || "BANKERROR".equalsIgnoreCase(resultCode)) {
            asyncQuery(wxPayPara, false);
        } else if ("USERPAYING".equalsIgnoreCase(resultCode)) {
            asyncQuery(wxPayPara, true);
        } else {
            ///TODO 更新业务订单状态
            refreshOrderState(resultMap);
        }
        return resultMap;
    }

    private void asyncQuery(WXPayPara wxPayPara, boolean isFirstWait) {

        ///TODO 开启一个新的线程,执行如下方法,可考虑线程池去做，Executor
        threadPool.execute(() -> {
            long start = System.currentTimeMillis();
            long deadline = overtime + start;

            InterruptTask task = new InterruptTask(deadline - System.currentTimeMillis());
            Map<String, String> queryMap = null;
            if (isFirstWait) {
                try {
                    log.debug("停止一段时间开始执行:{}", firstWaitTime);
                    Thread.sleep(firstWaitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (!Thread.interrupted()) {//(System.currentTimeMillis()< deadline) {

                ///TODO 由wxPayPara生成查询参数
                WXQueryPara wxQueryPara = WXQueryPara.builder().build();
                queryMap = this.query(wxQueryPara);

                ///TODO 需要判断 return_code 和 result_code

                String tradeState = queryMap.get("trade_state");
                log.debug("查询交易状态trade_state:[{}]", tradeState);
                //USERPAYING--用户支付中
                //PAYERROR--支付失败(其他原因，如银行返回失败),这个需要支持吗？不支持
                if (!("USERPAYING".equalsIgnoreCase(tradeState)) //"PAYERROR".equalsIgnoreCase(tradeState)
                    // || System.currentTimeMillis() > deadline
                        ) {
                    log.debug("其它交易状态跳出:[{}]", tradeState);

                    break;
                }
                try {
                    log.debug("准备休息时长:{}", interval);
                    Thread.sleep(interval); //休息
                } catch (InterruptedException e) {
                    log.debug("睡眠被打断，跳出循环");
////                    e.printStackTrace();
                    break;
                }
            }
            task.cancel();
            log.debug("执行循环查询耗时:{}", (System.currentTimeMillis() - start));
            ///TODO 更新业务订单状态, 非必要继续查询状态或者超时了
            refreshOrderState(queryMap);
        });

    }

    private void refreshOrderState(Map<String, String> map) {
        log.debug("更新订单状态");
    }

}
