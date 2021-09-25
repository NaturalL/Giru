## Demo
http://104.128.93.128:8092

## 功能
+ 错误日志上报: 目前支持 Logback, Log4j 1.x, 自动上报 ERROR 及以上级别. 
+ 分析及告警: 按异常 stackTrace 特征聚合,提供新异常和突增异常告警. N天内未出现过为新异常，异常数量超过基线1倍、10倍...为突增异常
+ OutOfMemoryError 等预定义的严重异常，也支持手动标记严重\默认\琐碎异常,琐碎异常减少告警

### 特点
+ 客户端接入简单；服务端无中间件等依赖
+ 常见的按关键字告警无法区分不同位置抛出的名称相同的异常，Giru 提取调用栈特征，不放过任何新异常
+ 人工配置频率阈值，过于繁琐，Giru 以过去 N 天异常数作为基线，无需人工配置
+ 告警过多掩盖重要信息，太少没有效果，Giru 提供最重要的新异常和突增异常告警，忽略日常琐碎异常

## 客户端

### 添加依赖

    <dependency>
        <groupId>com.biasee.giru</groupId>
        <artifactId>giru-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>

### 配置

|  参数 |  默认 |  备注 |
| ------------ | ------------ | ------------ |
| giru.event.url | 无 | 服务端地址，格式如：http://localhost:8092 |
| spring.application.name | 无 | 应用名 |
| giru.event.error-log.enabled  | true  |  启用错误日志采集  |
| giru.event.report.enabled  | true  | 启用事件上报  |
| giru.event.error-log.stacktraceDepth | 50 | Exception栈深度 |

## 服务端
    
    //需要编写代码，接入IM 邮件 短信，及时接收异常信息并处理
    com.biasee.giru.event.core.service.AlertApi.alert

### 配置
    见 giru-web/src/main/resources/application.yml
    
    
### 其他监控
https://rollbar.com/

https://www.weibo.com/1728555142/J4hmX2mZh?type=comment#_rnd1632367893509

https://raygun.com/

https://newrelic.com/

https://sentry.io/welcome/
