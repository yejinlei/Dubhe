# 之江天枢-算法端

**之江天枢一站式人工智能开源平台**（简称：**之江天枢**），包括海量数据处理、交互式模型构建（包含Notebook和模型可视化）、AI模型高效训练。多维度产品形态满足从开发者到大型企业的不同需求，将提升人工智能技术的研发效率、扩大算法模型的应用范围，进一步构建人工智能生态“朋友圈”。

## 算法部署

源码部署
准备环境

ubuntu系统 版本18.04及以上
python 3.7+
redis  5.0+
oneflow 框架

## 下载源码
http://repo.codelab.org.cn/codeup/codelab/Dubhe.git

## 进入项目根目录
cd dubhe_data_process

## 启动算法 (参数指定需要启动的算法)
python main.py track 

具体部署流程请参考 http://tianshu.org.cn/?/course 中文档**部署数据处理算法**

## 快速上手：

### 代码结构：

```
├── 
├── common                              基础工具
|   ├── config
|   ├── constant
|   ├── util
├── log
├── of_model                            oneflow模型文件
├── program
|   ├── abstract
|       ├── actuator.py                 执行器抽象类
|       ├── algorithm.py                算法抽象类 
|       ├── storage.py                  存储抽象类
|   ├── exec
|       ├── annotation                  目标检测
|       ├── imagenet                    图像分类
|       ├── imgprocess                  数据增强
|       ├── lung_segmentation           肺部分割
|       ├── ofrecord                    ofrecord转换
|       ├── text_classification         文本分类
|       ├── track                       目标跟踪
|       ├── videosample                 视频采样
|   ├── impl
|       ├── config_actuator.py          执行器配置实现
|       ├── redis_storage.py            redis存储
|   ├── thread
├── script                              脚本
├── LICENSE
├── main.py
└── README.md
```

### 算法接入：

#### 算法文件
[algorithm.py](./program/abstract/algorithm.py) 需要实现此算法抽象类

算法文件目录放在 program/exec 下，实现 program/abstract 目录下的 algoriyhm.py 文件中的 Algorithm 类，
其中 __init__ 方法和 execut 方法需要实现，__init__ 方法为算法的初始化操作，execute 为算法执行入口，入参
为 jsonObject，返回值为 finish_data（算法执行完成放入 redis 中的信息）以及布尔类型（算法执行成功或者失败）

#### config.json文件
在 program/exec 的每个算法目录下，需要有 config.json 文件，用户启动 main.py 时通过参数来指定需要执行的算
法(参数与算法目录名称相同)

### config.json模板

#### 算法不需要使用GPU时的config.json
[config.json](./common/template/config.json)

用户需要提供的参数:
- step1："paramLocal"算法处理中队列名称
- step2:"module","class"替换为需要接入的算法
- step4:"paramLocal" 中"algorithm_task_queue","algorithm_processing_queue"替换为需要接入算法的待处理任务队列和处理中任务队列
- step:5:"module","class"替换为需要接入的算法
- step6:"paramLocal" 中"algorithm_task_queue","algorithm_processing_queue"替换为需要接入算法的处理成功和处理失败队列

#### 算法需要使用GPU时的config.json
[config_GPU.json](./common/template/config_GPU.json)

用户需要提供的参数:
- step1："paramLocal"算法处理中队列名称
- step3:"module","class"替换为需要接入的算法
- step5:"paramLocal" 中"algorithm_task_queue","algorithm_processing_queue"替换为需要接入算法的待处理任务队列和处理中任务队列
- step:6:"module","class"替换为需要接入的算法
- step7:"paramLocal" 中"algorithm_task_queue","algorithm_processing_queue"替换为需要接入算法的处理成功和处理失败队列

## 开发者指南
若用户需了解算法接入实现细节，请参考官方文档：开发人员自定义算法接入规范

