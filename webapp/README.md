# 一站式开发平台-前端

**天枢人工智能开源开放平台**（简称：**天枢平台**）是天枢平台由之江实验室牵头，联合北京一流科技、中国信通院和浙江大学共同自研的人工智能开源平台。整个平台由一站式AI模型开发平台、高性能深度学习框架和模型炼知框架三大子系统组成。

其中， **一站式AI模型开发平台面**（简称：**一站式开发平台**）面向AI模型生产的生命周期，提供了包括数据处理、模型开发、模型训练和模型管理等功能，方便用户一站式构建AI算法。

## 特性
* 一站式开发
* 集成先进算法
* 灵活易用
* 性能优越

## 预览
![概览](/public/dubhe_dashboard.png "概览")

## 源码部署

### 1. 下载源码

``` bash
git clone https://codeup.teambition.com/zhejianglab/dubhe-web.git

# 进入根目录
cd dubhe-web

```
### 2. 配置

根据需要修改如下配置文件
```
config/index.js
settings.js
.env.production
```

### 3. 构建

``` bash
# 安装项目依赖
npm install

# 构建生产环境
npm run build:prod
```

### 4. 部署

- 构建完成后会在根目录生成 dist 文件夹，并将该文件夹上传至服务器；
- 在服务器 nginx.conf 文件中添加如下配置；

``` nginx
server {
    listen       80;        # 端口
    server_name  localhost; # 域名/外网IP

    location / {
        root   /home/wwwroot/dubhe-web/dist; # dist 文件夹根目录
        index  index.html;
        try_files $uri $uri/ /index.html;
    }
}

```

- 保存 `nginx.conf` 并重启 Nginx 使之生效。


## 本地开发

``` bash
# 下载源码
git clone https://codeup.teambition.com/zhejianglab/dubhe-web.git

# 进入项目根目录
cd dubhe-web

# 安装依赖
npm install

# 启动服务 localhost:8013
npm run dev
```

## 项目结构

```
├── public          公共静态文件 
├── src             源码目录 
│   ├── api         接口 
│   ├── assets      静态资源 
│   ├── assets      静态资源 
│   ├── boot        全局加载 
│   ├── components  公共组件 
│   ├── config      全局配置 
│   ├── directives  全局指令 
│   ├── hooks       全局Hook 
│   ├── layout      页面布局 
│   ├── mixins      混入 
│   ├── router      路由 
│   ├── store       存储 
│   ├── utils       工具函数 
│   ├── views       页面 
│   ├── App.vue     根组件 
│   ├── main.js     项目入口 
│   └── settings.js 项目设置 
```
