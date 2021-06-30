# 之江天枢-分布式训练 operator
该模块是分布式训练CRD的控制器，管理分布式训练容器生命周期，为分布式训练容器注入其他容器ip。

## 源码部署

### 准备环境
安装如下软件环境。
- OpenJDK：1.8+
- Redis: 3.0+
- Maven: 3.0+

### 下载源码
``` bash
git clone https://codeup.teambition.com/zhejianglab/distribute-train-operator.git
# 进入项目根目录
cd distribute-train-operator
```

### 构建
``` bash
# 构建，生成的 jar 包位于 ./target/distribute-train-operator-1.0.jar
mvn clean compile package
```

### 部署
部署过程参看文档：[部署 分布式训练operator](http://docs.dubhe.ai/docs/setup/deploy-distribute-train-operator)
