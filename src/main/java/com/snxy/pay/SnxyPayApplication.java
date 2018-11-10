package com.snxy.pay;

import com.snxy.pay.config.ZhongxinServiceInfoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class SnxyPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnxyPayApplication.class, args);
	}
	@Bean
	@ConfigurationProperties(prefix = "zhongxin")
	public ZhongxinServiceInfoConfig getZhongxinServiceInfoConfig(){
		return  new ZhongxinServiceInfoConfig();
	}
}
