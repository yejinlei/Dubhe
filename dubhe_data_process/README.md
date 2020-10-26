# 之江天枢-算法端

**之江天枢一站式人工智能开源平台**（简称：**之江天枢**），包括海量数据处理、交互式模型构建（包含Notebook和模型可视化）、AI模型高效训练。多维度产品形态满足从开发者到大型企业的不同需求，将提升人工智能技术的研发效率、扩大算法模型的应用范围，进一步构建人工智能生态“朋友圈”。

## 算法部署

部署请参考 http://tianshu.org.cn/?/course 中文档**部署数据处理算法**

## 代码结构：

```
├── LICENSE
├── README.md
├── algorithm-annotation.py  #目标检测和图像分类算法
├── algorithm-imagenet.py    #图像分类中imagenet标签处理算法
├── algorithm-imgprocess.py  #数据增强算法
├── algorithm-ofrecord.py    #ofrecord数据转换算法
├── algorithm-track.py       #跟踪算法
├── algorithm-videosample.py #视频采样算法
├── annotation.py
├── common                   #基础工具
├── data
├── imagenet.py
├── imgprocess.py
├── luascript
├── of_model                 #oneflow模型文件
├── ofrecord.py
├── predict_with_print_box.py
├── taskexecutor.py
├── track.py
├── track_only
└── videosample.py
```