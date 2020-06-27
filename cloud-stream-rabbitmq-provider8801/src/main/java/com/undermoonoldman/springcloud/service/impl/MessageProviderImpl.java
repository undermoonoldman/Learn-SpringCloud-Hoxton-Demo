package com.undermoonoldman.springcloud.service.impl;

import com.undermoonoldman.springcloud.service.IMassageProvider;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import javax.annotation.Resource;
import java.util.UUID;

//非传统的mvc service，只需要开启绑定器
@EnableBinding(Source.class) //定义消息的推送管道
public class MessageProviderImpl implements IMassageProvider {

    @Resource
    private MessageChannel output;//消息发送管道，output对应生产者

    @Override
    public String send() {
        String serial = UUID.randomUUID().toString();
        output.send(MessageBuilder.withPayload(serial).build());
        System.out.println("***********serial"+serial);
        return serial;
    }
}
