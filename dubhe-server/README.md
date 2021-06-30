# Spring Cloud
## 微服务框架核心组件
Nacos + Fegin + Gateway + （Spring Security + JWT + OAuth2）

数据库连接池 Druid

需要额外部署 Mysql Nacos

## 初始化配置
### Mysql

初始化sql位置   /sql

**地址：** 127.0.0.1:3306
**用户名：** test **密码：** test

### Nacos

**如何部署：** https://nacos.io/zh-cn/docs/quick-start.html

配置中心配置参考  /yaml

**配置规则：** ${prefix}-${spring.profiles.active}.${file-extension}

**详见：** https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html

**地址：** http://127.0.0.1:8848/nacos/#/login

**用户名:** nacos **密码：** nacos

开发人员进行开发自测时，请使用以自己名字命名的namespace进行测试，不要使用dev或者test


### swagger
默认开启,若需要关闭，手动配置swagger.enabled: false

**各模块swagger访问地址：** http://{IP}:{port}/doc.html

**可通过swagger统一的网关访问地址：** http://{gateway IP}:{gateway port}/doc.html查看基于gateway路由配置的后台rest服务

### OAuth2
授权token获取样例：
*POST* http://localhost:8866/oauth/token?grant_type=password&username=admin&client_id=dubhe-client&client_secret=dubhe-secret&password=123456&scope=all

请求资源时在header添加：
Authorization: 'Bearer '+${access_token}

测试服务提供者配置中心动态获取配置样例：
header 添加-> Authorization：'Bearer '+${access_token}
*GET* http://localhost:8860/config/get

刷新token样例：
*POST* http://localhost:8866/oauth/token?grant_type=refresh_token&client_id=dubhe-client&client_secret=dubhe-secret&scope=all&refresh_token=${refresh_token}

### Admin 
登录接口
```$xslt
url:
*POST* http://localhost:8870/auth/login
param:
{"username":"admin","password":"RBb2Czac2HBI9XNj4ZLF1QcTytOe5pN1vHZHYuAVgSAPRcYbndn/4zGDxKdXS1j0sLsDsKZLUojEXFnYHpsKxA==","code":"jggg","uuid":"validate_codeea991a3cb8ea47cca05744a47ad17a37"}
```


