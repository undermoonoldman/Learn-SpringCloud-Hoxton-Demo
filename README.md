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

   ### Feign是一个声明式的Web服务客户端，让编写Web服务客户端变得非常容易，只需 创建一个接口并在接口上添加注解即可

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

      + #### com.undermoonoldman.springcloud.config 类中配置日志级别

      + #### yml 配置文件中设置 feign 日志以什么级别监控哪个接口

7. ## Hystrix熔断器

   ### Hystrix 能干吗，服务降级，服务熔断，实时监控

   1. ### 重要概念

      + #### 服务降级

        ##### 什么是服务降级，这里有两个场景。当下游的服务因为某种原因**响应过慢**，下游服务主动停掉一些不太重要的业务，释放出服务器资源；当下游的服务因为某种原因**不可用**，上游主动调用本地的一些降级逻辑，避免卡顿，迅速返回给用户增加响应速度，而不是直接报错，也避免客户端等待。异常，超时，熔断，线程池或者信号量都会触发服务降级。服务降级有很多种降级方式！如开关降级、限流降级、熔断降级，服务熔断属于降级方式的一种

      + #### 服务熔断

        ##### 什么是服务熔断，当下游的服务因为某种原因突然**变得不可用**或**响应过慢**，上游服务为了保证自己整体服务的可用性，不再继续调用目标服务，直接返回，快速释放资源。如果目标服务情况好转则恢复调用，需要说明的是熔断其实是一个框架级的处理，那么这套熔断机制的设计，基本上业内用的是`断路器模式`，也就是我们常见的 closed，opne，halfopen 三状态转换。最开始处于`closed`状态，一旦检测到错误到达一定阈值，便转为`open`状态，这时候会有个 reset timeout，到了这个时间了，会转移到`half open`状态，尝试放行一部分请求到后端，一旦检测成功便回归到`closed`状态，即恢复服务

      + #### 服务限流
      
        ##### 并发过高，达到提供服务能力的上限。这时可以按照一定策略对超出能力之外的请求进行拒绝服务，比如令牌策略，桶策略等等
      
   2. ### 项目基础构建

      + #### 创建cloud-provider-hystrix-payment8001 支付服务提供者

        ##### 提供两个接口供调用，一个模拟正常请求，即时返回；一个模拟耗时业务请求，延时三秒返回

      + #### 创建cloud-consumer-feign-hystrix-order80 支付服务调用者

        ##### 提供两个接口，分别调用支付服务提供者的两个接口

   3. ### 并发压测

      + #### 创建Jmeter线程组

        ##### 200个为一组，一共100组

      + #### 模拟并发访问接口

        ##### 模拟20000个请求调用支付服务的耗时接口，这时再尝试去请求即时接口，原本能立刻返回的接口也出现了转圈等待的现象

      + #### 产生的问题

        ##### 超时导致服务器变慢(转圈)；出错(宕机或程序运行出错)

      + #### 优化方向

        ##### 超时不再等待，出错要有兜底。对方服务(8001)超时了,调用者(80)不能一直卡死等待,必须有服务降级；对方服务(8001)down机了,调用者(80)不能一直卡死等待,必须有服务降级；对方服务(8001)ok,调用者(80)自己有故障或有自我要求(自己的等待时间小于服务提供者)

   4. ### 服务降级

      + #### 改造 8001 服务提供者

        ##### 主启动类添加@EnableCircuitBreaker注解，业务类接口编写兜底方法，一旦调用服务方法失败并抛出了错误信息后,会自动调用@HystrixCommand标注好的fallbckMethod调用类中的指定的兜底方法

      + #### 改造 80 服务调用者

        ##### 主启动类添加@EnableCircuitBreaker注解，其他模仿之前对服务提供者的改造。

      + #### 存在问题

        ##### 每一个业务方法都有对应的兜底方法，代码膨胀严重。现在需要有一套统一的兜底方法，如果不单独配置的话业务方法出错时就会执行默认的兜底方法。兜底方法与正常业务代码写在一起，耦合严重。

      + #### 优化改造

        ##### 80 服务调用者使用 Feign 调用 8001 服务提供者，我们只需要在Feign客户端定义的接口添加一个服务降级处理的实现类即可实现解耦。在 80 服务调用者的 controller 上添加注解 @DefaultProperties(defaultFallback = "payment_Global_FallbackMethod") 可以指定我们编写的全局默认兜底方法

   5. #### 服务熔断

      + #### 熔断类型

        ##### 全开：请求不再调用当前服务,内部设置一般为MTTR(平均故障处理时间),当打开长达导所设时钟则进入半熔断状态；关闭：熔断关闭后不会对服务进行熔断；半开：部分请求根据规则调用当前服务,如果请求成功且符合规则则认为当前服务恢复正常,关闭熔断

      + #### 断路器开启或者关闭的条件

        ##### 当满足一定的阈值的时候(默认10秒钟超过20个请求次数)，当失败率达到一定的时候(默认10秒内超过50%的请求次数)，到达以上阈值,断路器将会开启，当开启的时候,所有请求都不会进行转发，一段时间之后(默认5秒)，这个时候断路器是半开状态，会让其他一个请求进行转发.。如果成功,断路器会关闭，若失败，继续开启。重复4和5

      + #### 改造 8001 服务提供者

        ##### @HystrixProperty(name = "circuitBreaker.enabled", value = "true")，是否开启断路器；@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10")，请求次数；@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000")，时间窗口期；@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")，失败率达到多少后跳闸。合起来解读就是，在10秒窗口期中10次请求有6次是请求失败的,断路器将起作用

      + #### 效果

        ##### 接口对请求参数为负数会报错。我们人工短时间多次发起参数为负数的请求，制造人为错误从而引发熔断。这时我们会发现，接口切换到了兜底逻辑上，当我们尝试使用参数为正数的请求，获取依然是兜底的返回对象。当等待一会后，就又能正常的请求接口了

   6. ### 服务监控hystrixDashboard

      + #### 创建 cloud-consumer-hystrix-dashboard9001

        ##### 所有Provider微服务提供类(8001/8002/8003)都需要监控依赖部署，主启动类新注解@EnableHystrixDashboard，需要注意的是新版本Hystrix需要在主启动MainAppHystrix8001中指定监控路径
   
8. ## Gateway网关

   1. ### 核心概念

      + #### 路由

        ##### 路由是构建网关的基本模块,它由ID,目标URI,一系列的断言和过滤器组成,如断言为true则匹配该路由

      + #### 断言

        ##### 断言是对请求的判定规则，如果匹配成功则会进行路由

      + #### 过滤

        ##### 使用过滤器,可以在请求被路由前或者之后对请求进行修改

   2. ### 项目基础构建

      + #### 创建cloud-gateway-gateway9527 网关

        ##### 注意网关项目不必引入web和actuator的依赖坐标

      + #### 编写路由配置并与注册中心整合

        ##### 路由的id标识，uri以及断言

      + #### 同时需要启动还有注册中心集群与8001支付提供者

   3. ### 断言详解

      #### 可以使用时间，请求头信息，cookie信息，主机域名，请求方法，请求路径，请求参数，网段划分，权重的方式来作为断言匹配的条件

   4. ### 网关的配置还可以通过配置类的方式用代码来编写

      #### 返回的类型为RouteLocator，具体写法详见示例代码

   5. ### 开启动态路由功能

      #### spring.cloud.gateway.discovery.locator.enabled.true

   6. ### 负载均衡约束

      #### 在断言的url配置中，使用 "lb://服务名称"的配置方式，配置了lb约束之后，springcloud会采用负载均衡客户端来解析服务名称，我们可以开启8002支付提供者与8001组成集群来测试网关的负载功能是否正常

   7. ### 过滤器

      #### 有“pre”和“post”两种方式的filter。客户端的请求先经过“pre”类型的filter，然后将请求转发到具体的业务服务，收到业务服务的响应之后，再经过“post”类型的filter处理，最后返回响应到客户端。在Spring Cloud Gateway中，filter从作用范围可分为另外两种，一种是针对于单个路由的gateway filter，它在配置文件中的写法同predict类似；另外一种是针对于所有路由的global gateway filer。Spring Cloud Gateway包含许多内置的GatewayFilter工厂，详情参照官网示例代码

   8. ### 自定义全局过滤器

      #### 自定义过滤器需要实现GatewayFilter和Ordered2个接口。getOrder()方法是来给过滤器设定优先级别的，值越大则优先级越低。还有有一个filterI(exchange,chain)方法用来编写过滤器的逻辑

   9. ### 自定义过滤器工厂

     #### 如果能自定义过滤器工厂，就可以在配置文件中配置过滤器了。具体做法参考官方文档
   
9. ## config 分布式配置中心

   1. ### 创建本地配置文件中心 cloud-config-center-3344

      #### 随着微服务数量的增多，配置文件数量也随之上升，这样就带来了配置文件修改更新的复杂性。需要构建一个类似于git式的分布式配置文件管理，可以方便的在一处更新并同步到其他微服务。我们先建立远程Git仓库，并按照“文件名-dev/pro/test.扩展名”的方式来存放配置文件。在本地配置中心项目中配置远程仓库的链接地址，项目名称，连接加密方式(如果有的话)，拉取的分支。配置完成启动项目，可以通过本地3344端口与对应的配置文件名访问到远程Git仓库的配置文件

   2. ### 创建本地配置文件客户端 cloud-config-client-3355

      #### 3355 不直接从远程仓库拉取配置文件，而是从3344配置中心获取配置文件。启动项目可以通过3355端口获取本地配置中心从远程拉取的配置文件。但是有一个缺陷，就是更新远程仓库配置文件后。刷新3344可以获取更新内容，而3355无法获取更新内容

   3. ### 开启手动动态刷新

      #### 引入actuator监控依赖，并在需要使用远程配置信息的controller上添加@RefreshScope注解。这样当远程仓库配置文件有更新后，还需要对3355发一个POST请求后再次刷新就可以获取更新内容了，这里我们使用命令curl -X POST "http://config-3355.com:3355/actuator/refresh"

   4. ### 整合 BUS 消息总线

      #### 可以实现分布式自动刷新的功能，解决了上面每个微服务都需要单独手动刷新的问题，目前只支持RabbitMQ与Kafka作为消息代理

      + #### 全局广播

        ##### 3344，3355，3366添加RabbitMQ相关依赖，配置文件中编写RabbitMQ连接相关的属性。这样当修改了远程仓库的配置文件时，只需要执行一次命令刷新3344本地配置中心，3355，3366就也可以获取最新的更改了。命令curl -X POST "http://config-3344.com:3344/actuator/bus-refresh"

      + #### 定点更新

        ##### 远程仓库配置文件更新后，只想通知3355获取更新，而3366不更新。只需要更改刷新命令即可。curl -X POST "http://config-3344.com:3344/actuator/bus-refresh/config-client:3355"

10. ## Steam 消息驱动
  
   1. ### 简介
    
      #### SpringCloud Stream 是一个构件消息驱动微服务的框架，应用程序通过 inputs 或者 outputs 来与 binder 对象交互，通过我们配置来 binding，而 binder 对象负责与消息中间件交互。SpringCloud Stream 屏蔽了底层差异，提供了通用的交互方式，引入了发布-订阅，消费组，分区三个核心概念。目前支持的消息中间件仅有 RabbitMQ 与 Kafka
    
   2. ### 普通发布-订阅模式
    
      + #### 创建消息生产者 send-8801.com
      
        ##### 配置 binder相关信息，包括整合 RabbitMQ，配置连接参数；通道相关配置（注意消息生产者要配置为 output），在发送消息的类上开启注解@EnableBinding(Source.class)，使用 MessageChannel 发送消息。
      
      + #### 创建消息消费者 receive-8802.com 与 receive-8803.com
      
        ##### 相关配置与消息生产者类似，不同点在于通道处要配置为 input。接收消息的类上开启注解 @EnableBinding(Sink.class)，接收消息的方法上开启注解 @StreamListener(Sink.INPUT)，使用 Message 对象来接收消息。
      
      + #### 效果
      
        #### 8801 每生产一条消息，8802 于 8803 就会同时收到
    
   3. ### 重复消费与持久化
    
      + #### 避免重复消费
      
        ##### 如果是这样的业务场景，8801 负责异步产生订单，而 8802 与 8803 组成的集群负责销单。安装前面的逻辑，就会产生一个订单被重复处理的问题。原因是没有配置分组时会生成两个匿名组，不同组不存在消费竞争。现在为 8802 与 8803 配置相同的组，同组有消费竞争，即可避免前面发生的重复消费的问题。
      
      + #### 持久化
      
        ##### 如果消费者突然下线，那重新上线后没来得及消费的消息该如何处理。这里我们把 8803 的分组信息去掉，8802 则保留。把 8802 与 8803 进行重启，模拟消费者掉线又重新上线的过程。这时会发现没有来得及消费的消息都重新被 8802 消费了，而 8803 没有消费任何消息。只有配置了分组才能进行持久化
  
11. ## Sleuth 链路跟踪  
  
   1. ### zipkin 
    
      #### 下载 jar 包直接运行就可以了
    
   2. ### 改造 8001 服务提供者
    
      #### 添加链路监控包含 sleuth+zipkin 依赖，编写配置文件，controller 中提供一个用于测试链路监控的方法以供调用
    
   3. ### 改造 80 服务调用者
    
      ####添加链路监控包含 sleuth+zipkin 依赖，编写配置文件，controller 中提供一个方法调用 8001 的对应接口 
    
   4. ### 监控查看
    
      #### 使用 80 来调用 8001 提供的服务。然后进入 zipkin 的 web 管理界面，localhost:9411 就可以查看链路调用的详情了
  
     
