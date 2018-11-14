package com.snxy.pay;

import com.snxy.pay.config.ZhongXinServiceInfoConfig;
import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
//@EnableDiscoveryClient
public class SnxyPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnxyPayApplication.class, args);
	}

	//	开启feign日志
	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}
	@Bean
	@ConfigurationProperties(prefix = "zhongxin")
	public ZhongXinServiceInfoConfig getZhongxinServiceInfoConfig(){
		return  new ZhongXinServiceInfoConfig();
	}
	@Bean
	public ExecutorService getThreadPool(){
		return Executors.newCachedThreadPool();
	}
}
