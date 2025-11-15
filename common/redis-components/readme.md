## 使用 redis-component 这个starter 对应的服务配置文件针对不同的redis模式进行配置

### application.yml 配置示例（三种模式）

### 1. 单机模式（默认）

yaml

```
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password: yourpassword
      timeout: 5000ms
    fastjson:
      enable: true
```

------

### 2. 哨兵模式（Sentinel）

yaml

```
spring:
  data:
    redis:
      sentinel:
        master: mymaster           # 主节点名称
        nodes: 192.168.1.10:26379,192.168.1.11:26379,192.168.1.12:26379
      password: yourpassword
      database: 0
    fastjson:
      enable: true
```

------

### 3. 集群模式（Cluster）

yaml

```
spring:
  data:
    redis:
      cluster:
        nodes:
          - 192.168.1.10:7000
          - 192.168.1.10:7001
          - 192.168.1.11:7000
          - 192.168.1.11:7001
          - 192.168.1.12:7000
          - 192.168.1.12:7001
        max-redirects: 3
      password: yourpassword
    fastjson:
      enable: true
```

------

## 