# 之江天枢-服务端

**之江天枢一站式人工智能开源平台**（简称：**之江天枢**），包括海量数据处理、交互式模型构建（包含Notebook和模型可视化）、AI模型高效训练。多维度产品形态满足从开发者到大型企业的不同需求，将提升人工智能技术的研发效率、扩大算法模型的应用范围，进一步构建人工智能生态“朋友圈”。

## 源码部署

### 准备环境
安装如下软件环境。
- OpenJDK：1.8+
- Redis: 3.0+
- Maven: 3.0+
- MYSQL: 5.5.0+

### 下载源码
``` bash
git clone https://codeup.teambition.com/zhejianglab/dubhe-server.git
# 进入项目根目录
cd dubhe-server
```

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
dubhe-admin/src/main/resources/config/application-prod.yml
```

### 构建
``` bash
# 构建，生成的 jar 包位于 ./dubhe-admin/target/dubhe-admin-1.0.jar
mvn clean compile package
```

### 启动
``` bash
# 指定启动环境为 prod
java -jar ./dubhe-admin/target/dubhe-admin-1.0.jar --spring.profiles.active=prod
```

## 本地开发

### 必要条件：
    导入maven项目，下载所需的依赖包
    mysql下创建数据库dubhe，初始化数据脚本
    安装redis

### 启动：
    mvn spring-boot:run

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
