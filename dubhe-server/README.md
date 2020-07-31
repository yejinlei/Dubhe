# 一站式开发平台-服务端

## 本地开发

### 准备环境
安装如下软件环境。
- OpenJDK：1.8+
- Redis: 3.0+
- Maven: 3.0+
- MYSQL: 5.5.0+

### 创建DB
在MySQL中依次执行如下sql文件
```
sql/v1/00-Dubhe-DB.sql
sql/v1/01-Dubhe-DDL.sql
sql/v1/02-Dubhe-DML.sql
```

### 配置
根据实际情况修改如下配置文件。
```
dubhe-admin/src/main/resources/config/application-dev.yml
```

### 启动：
```
mvn spring-boot:run
```

## 代码结构：
```
├── common   公共模块
├── dubhe-admin 开发与训练模块  
│   ├── src  
│   │   └── main    
│   │       ├── java    
│   │       │   └── org   
│   │       │       └── dubhe  
│   │       │           ├── AppRun.java  
│   │       │           ├── domain   实体对象  
│   │       │           ├── repository  数据库层  
│   │       │           ├── rest       控制层  
│   │       │           └── service   服务层  
│   │       │               ├── dto   数据传输对象     
│   │       │               ├── impl  服务实现  
│   │       │               └── mapper 对象转化  
│   │       └── resources   配置文件  
├── dubhe-data   数据处理模块  
├── dubhe-model  模型管理模块
├── dubhe-system  系统管理
``` 

## docker服务器
    上传镜像功能依赖docker服务，harbor与dokcer的信任配置如下：
### 1、对外开放端口
    vi /lib/systemd/system/docker.service
    ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock -H tcp://0.0.0.0:2375 -H unix://var/run/docker.sock
### 2、信任harbor地址
    vi /etc/docker/daemon.json
    {
      "exec-opts": ["native.cgroupdriver=systemd"],
      "log-driver": "json-file",
      "insecure-registries":[harbor地址],
      "log-opts": {
              "max-size": "100m"
       }
    }
### 3、重新启动
    systemctl daemon-reload
    service docker restart
    systemctl status docker
