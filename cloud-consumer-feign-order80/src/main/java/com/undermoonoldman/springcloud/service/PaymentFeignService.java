package com.undermoonoldman.springcloud.service;

import com.undermoonoldman.springcloud.entity.CommonResult;
import com.undermoonoldman.springcloud.entity.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * 把对其他微服务的调用封装成service方法
 */
@Component
@FeignClient(value = "cloud-payment-service")//需要调用的服务的名称
public interface PaymentFeignService {

    @GetMapping(value = "/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id);

    @GetMapping("/payment/feign/timeout")
    public String paymentFeignTimeout();
}
