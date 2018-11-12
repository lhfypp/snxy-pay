package com.snxy.pay;

import com.snxy.pay.alipay.service.ALIMicropayDecorator;
import com.snxy.pay.alipay.vo.ALIPayPara;
import com.snxy.pay.wxpay.service.MicropayDecorator;
import com.snxy.pay.wxpay.vo.WXPayPara;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SnxyPayApplicationTests {

	@Autowired
	MicropayDecorator micropay;
	@Autowired
	ALIMicropayDecorator aliMicropayDecorator;
	@Test
	public void contextLoads() throws InterruptedException {
		WXPayPara wxPayPara=WXPayPara.builder().build();
		micropay.pay(wxPayPara);
		Thread.sleep(60*1000);
	}
	@Test
	public void ALIPayTest() throws InterruptedException {
		ALIPayPara wxPayPara=ALIPayPara.builder().build();
		aliMicropayDecorator.pay(wxPayPara);
		Thread.sleep(60*1000);
	}
}
