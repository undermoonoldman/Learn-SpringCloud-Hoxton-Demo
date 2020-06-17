package com.undermoonoldman.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class MyLB implements LoadBalancer {

    //并发访问计数器，每个取得访问权限的使用者都对计数器执行加操作
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    //模拟一次并发访问的过程，获取计数器的当前值，自增步长为一，执行一次自增后返回计数器的值
    public final int getAndIncrement(){
        int current;
        int next;
        do{
            current = this.atomicInteger.get();
            /*2147483647:整型最大值*/
            //计数器溢出后重置为零
            next = current >=2147483647 ? 0 : current + 1;
        }while (!this.atomicInteger.compareAndSet(current, next));
        System.out.println("******第几次访问next" + next);
        return next;
    }

    //负载均衡算法：第几次请求%服务器总数量=实际访问。服务每次启动从1开始
    //入参传入服务实例列表，按照自定义的负载均衡算法选取一个服务实例并返回出去
    @Override
    public ServiceInstance instances(List<ServiceInstance> serviceInstances) {
        int index= getAndIncrement() % serviceInstances.size();

        return serviceInstances.get(index);
    }
}
