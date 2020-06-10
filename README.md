# Learn-SpringCloud-Hoxton-Demo

### 学习 SpringCloud-Hoxton 与SpringCloud-Alibaba中的常用相关组件

1. ## 版本选择

   | 软件                |     版本      |
   | ------------------- | :-----------: |
   | SpringCloud-Hoxton  |  Hoxton.SR1   |
   | SpringCloud-Alibaba | 2.1.0.RELEASE |
   | JDK                 |     1.8+      |
   | Maven               |     3.5+      |
   | MySql               |     5.7+      |
   
2. ## 项目搭建

   ### 创建maven聚合工程，引入相关依赖并使用DependencyManagement(父工程中定义版本并不会直接引入，子模块中使用Dependency引入相关包是才会真正导入)控制版本，跳过maven单元测试

3. ## 模块创建初始化

   1. ### cloud-provider-payment8001 支付服务提供者

      #### 主要提供下单与查单两个api接口

   2. ### cloud-consumer-order80 支付服务消费者

      #### 用于调用订单服务提供者的两个接口

   3. ### cloud-api-commons 通用模块

      #### 实体类对象的存储位置，提供通用的工具类，供其他模块调用

4. ## Eureka服务注册与发现

   ### Eureka分为服务端与客户端，服务端类似于物业，客户端类似于业主，客户端在服务端注册之后可以方便的通过服务名查找到已注册相关服务的详细信息

   1. ### 单机节点

      + #### 创建cloud-eureka-server7001注册中心

        ##### 服务端需要在主启动类上添加eureka服务端相关的注解，在配置文件中编写服务名称，禁止向注册中心自我注册，禁止检索服务(只维护服务)，并填写注册中心的交互地址(服务注册与发现都要用到)

      + #### 支付服务提供者注册成为客户端

        ##### 客户端需要在主启动类上添加服务发现相关的注解，在配置文件中编写服务名称，向注册中心注册，启用检索服务，并填写注册中心的地址

      + #### 支付服务消费者注册成为客户端

        ##### 客户端需要在主启动类上添加服务发现相关的注解，在配置文件中编写服务名称，向注册中心注册，启用检索服务，并填写注册中心的地址

      + #### 支付消费者调用支付提供者

        ##### 消费者通过resttemplate调用提供者的相关接口，resttemplate需要知道提供者相关接口的访问url这样做耦合过高

   2. ### 集群构建

      + #### 创建cloud-eureka-server7002注册中心

        ##### 基本结构与7001相同，只是提供服务的端口不同

      + #### 创建cloud-provider-payment8002支付服务提供者

        ##### 基本结构与8001相同，只是提供服务的端口不同

      + ##### 两个注册中心服务器集群化配置

        ##### 两台注册节点相互注册，7001向7002注册，7002向7001注册

      + #### 注册中心客户端配置修改

        ##### 注册中心地址填写所有集群节点，同时开启向注册中心抓取已注册服务的信息(集群模式需要采集服务信息做负载均衡)，支付消费者使用resttemplate调用服务时，要使用@loadbalance注解生成一个具有负载均衡功能的resttemplate

   3. ### actuator微服务信息完善

      + #### maven引入相关的坐标

      + #### eureka.instance.instance-id 填写服务名称可以在控制面板查看

      + #### eureka.instance.prefer-ip-address: true 可以在控制面板现在服务的真实ip地址

   4. ### 服务发现Discovery

      + #### 主启动类开启 @EnableDiscoveryClient 注解

      + #### 在 controller 中注入 DiscoveryClient 就可以通过该对象获取已经注册的服务的实例了

   5. ### eureka的自我保护

      #### 在eureka控制面板经常会看到一下警告信息，一句话总结原因就是某时刻 一个微服务不可用了,Eureka不会立刻清理,依旧会对该服务的信息进行保存。由于CAP理论的存在，在高可用性，强一致性，分区容错性最多只能保证两项，现在的应用大多选择了分别式架构，所以高可用性与强一致性只能二选一，而 eureka 选择了高可用性，也就是 AP。当服务暂时不可用时，eureka会选择警告而不是立即把服务从注册列表中移除出去，这就是 eureka 的自我保护

      + #### 服务端禁止自我保护

        ##### 修改注册中心的配置文件 eureka.server.enable-self-preservation=false 关闭自我保护

      + #### 客户端延迟自我保护的触发时间

        ##### eureka.instance.lease-renewal-interval-in-seconds=30 设置客户端向注册中心发生心跳的时间间隔,单位为秒,默认值为30，eureka.instance.lease-expiration-duration-in-seconds=90 设置服务注册端在最后一次收到心跳后多长时间没有再收到心跳会把服务移除,单位为秒,默认值为90

   6. ### eureka 的备选方案

      #### eureka 目前处于停止更新的状态，备选方案有 Zookeeper 还有 Consul(Go语言开发)，与eureka不一样，两者都需要独立安装。CAP理论关注粒度是数据,而不是整体系统设计的策略，AP(eureka)，CP(Zookeeper/Consul)

5. ## Ribbon 客户端负载均衡

   ### 目前业界主流的负载均衡方案可分成两类，第一类是集中式负载均衡，在服务提供者与服务消费者之间独立架设负载均衡设备，比如NGINX；第二类是进程内负载均衡，将负载均衡逻辑集成到服务消费者，消费者从服务注册中心获知有哪些地址可用，然后自己再从这些地址中选择出一个合适的服务提供者，Ribbon 就属于这一类的

   1. ### 初体验

      #### eureka 中已经引入了 Ribbon 依赖，这时使用服务消费者来访问服务提供者集群时，就会发现负载均衡已经默认开启了，算法是轮询，效果是依次访问 8001与 8002
      
   2. ### 核心组件IRule与替换默认负载均衡算法
   
      #### IRule是一个接口，根据特定算法从服务列表中选取一个要访问的服务，常见的实现类有如下
   
      | 实现类                                  |                           对应算法                           |
      | --------------------------------------- | :----------------------------------------------------------: |
      | com.netflix.loadbalancer.RoundRobinRule |                             轮询                             |
      | com.netflix.loadbalancer.RandomRule     |                             随机                             |
      | com.netflix.loadbalancer.RetryRule      | 先按照RoundRobinRule的策略获取服务,如果获取服务失败则在指定时间内进行重试,获取可用的服务 |
      | WeightedResponseTimeRule                | 对RoundRobinRule的扩展,响应速度越快的实例选择权重越多大,越容易被选择 |
      | BestAvailableRule                       | 会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务,然后选择一个并发量最小的服务 |
      | AvailabilityFilteringRule               |            先过滤掉故障实例,再选择并发较小的实例             |
      | ZoneAvoidanceRule                       | 默认规则,复合判断server所在区域的性能和server的可用性选择服务器 |
      
      + #### 如何替换默认的负载均衡算法
      
        ##### 注意对负载均衡算法的配置类不能放在当前项目的扫描路径下(主启动类所在的包及其子包)
      
        1. ##### 在扫描包之外新建一个工厂bean返回上表任意一个实现类
      
        2. ##### 在主启动类上添加注解@RibbonClient(name="CLOUD-PAYMENT-SERVICE",configuration = MySelfRule.class)，把刚才自定义的算法类填进去
      
   3. ### 手写负载均衡算法
   
      + #### ApplicationContextBean去掉注解@LoadBalanced
   
        ##### 使restTemplate失去负载均衡的能力，负载均衡由我们自己编写的实现类来提供
   
      + #### 编写loadBalance接口
   
        ##### 该接口只有一个方法，输入一个服务列表，输出一个选定的服务
   
      + #### 编写loadBalance接口的实现类并使用Spring容器管理
   
        #### 主要逻辑就是完成服务的选取
   
      + #### 更改业务逻辑
   
        ##### 现在在使用restTemplate发送请求之前，先使用我们之前编写的loadBalance实现类获取一个服务实例，再从该实例中拿到访问地址等详情信息，最后使用restTemplate发送
   
6. ## OpenFeign 服务接口调用

   ###Feign是一个声明式的Web服务客户端，让编写Web服务客户端变得非常容易，只需 创建一个接口并在接口上添加注解即可

   1. ### 使用步骤

      + #### 修改主启动类

        ##### 主启动类添加@EnableFeignClients注解

      + #### 远程调用接口类编写并封装成service

        ##### 接口类添加注解 @FeignClient(value = "cloud-payment-service")填入远程调用的服务，接口方法添加注解填入调用地址

      + #### Controller 修改

        ##### 现在controller就可以调用刚才封装的service而完成一次远程调用，就像调用本地serivice一样，不用再使用restTemplate构建发送请求了

   2. ### 超时控制

      #### OpenFeign 自带 Ribbon 依赖，具有负载均衡功能，超时控制也由 Ribbon 提供，OpenFeign默认等待1秒钟,超过后报错，要调整超时时间需要修改配置文件。ribbon.ReadTimeout 指的是建立连接所用的时间,适用于网络状态正常的情况下,两端连接所用的时间，ribbon.ConnectTimeout 指的是建立连接后从服务器读取到可用资源所用的时间

   3. ### 日志打印

      + #### config 类中配置日志级别

      + #### yml 配置文件中设置 feign 日志以什么级别监控哪个接口

        

