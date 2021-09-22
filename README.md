## 功能
+ 错误日志上报: 目前支持 Logback Log4j, 自动上报 ERROR 及以上级别. 
+ 分析告警: 按异常stackTrace聚合,提供新异常和突增异常告警.每日 10:00 重置异常数.


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
| giru.event.error-log.enabled  | true  |  启用错误日志采集  |
| giru.event.report.enabled  | true  | 启用事件上报  |
| giru.event.error-log.stacktraceDepth | 50 | Exception栈深度 |

## 服务端
    
    //按需接入IM 邮件 短信 告警
    com.biasee.giru.event.core.service.AlertApi.alert

### 配置
    见 giru-web/src/main/resources/application.yml
