package com.undermoonoldman.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class ApplicationContextConfig {
    @Bean
    @LoadBalanced //赋予RestTemplate负载均衡的能力，如果需要写入自定义负载均衡算法，需要将此注释掉
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
