# 之江天枢 - 数据集导入脚本

**之江天枢一站式人工智能开源平台**（简称：**之江天枢**）,为了实现其他平台已标注完成的数据集在「一站式开发平台」上进行开发，我们增加了数据集导入功能，用来导入本地已存在的数据集文件。

## 环境依赖

安装如下软件环境。
- OpenJDK：1.8+

## 下载脚本

- 数据集导入模板：http://tianshu.org.cn/static/upload/file/dubhe-dataset-template.zip 
- 数据集导入脚本：http://tianshu.org.cn/static/upload/file/upload_dataset.zip


## 创建数据集：

- 首先需要参考[部署文档](http://docs.dubhe.ai/docs/setup/deploy-guide)成功部署「一站式平台」
- 准备好本地待导入数据集文件，包括图片、标注和标签文件，文件格式参考 [目录说明](http://docs.dubhe.ai/docs/module/dataset/import-dataset#%E7%9B%AE%E5%BD%95%E8%AF%B4%E6%98%8E)
- 登录天枢深度学习平台，在「数据管理」模块下创建数据集，[使用文档](http://docs.dubhe.ai/docs/module/dataset/create-dataset)
   
## 运行脚本：

1.下载导入脚本压缩包（upload_dataset），解压之后， `application-{env}` 为脚本配置文件，默认 `env` 环境为 `dev`，需要自行配置数据源、MinIO 相关配置。

2.运行脚本，Windows 下执行 `run.bat`; macOS/Linux 系统运行 run.sh。

3. 根据不同环境需求，可自行配置 `application-{env}.yml`文件。`
run.bat {env}`即可执行对应的 `application-{env}.yml` 配置文件，注意在运行脚本前需要保证配置文件已存在。

3.根据提示输入数据集 ID。
 
4.根据提示输入待导入数据集绝对路径。

5. 导入成功。

   
## 目录结构：

[目录说明](http://docs.dubhe.ai/img/data/import-data9.png)

- 图片目录：origin (图片支持四种格式：.jpg,.png,.bmp,.jpeg)
- 标注目录：annotation (标注文件仅支持 .json 格式）
- 标签文件：文件格式为 `label_{name}.json`，其中 `name` 为「标签组」名称，且不能与已有标签组名称重复
   
## 文件格式

### 标签文件:

> 格式如下：
```
    name: 名称
    color: 颜色(16进制编码)
``` 

详细示例：   
```
[{
	"name": "行人",
	"color": "#ffbb96"
}, 
{
	"name": "自行车",
	"color": "#fcffe6"
}, 
{
	"name": "汽车",
	"color": "#f4ffb8"
}]
```

### 标注文件：

1. 图片分类

> 格式如下：
```
    name: 对应标签名称
    score：置信分数（0-1）
```
详细示例： 
```
[{"name":"wheaten_terrier","score":1}]
```

2. 目标检测

> 格式如下：
```
    name: 对应标签名称
    bbox: 标注位置
    score：置信分数（0-1）
```
详细示例： 
```
[{
    "name": "行人",
    "bbox": [321.6755762696266, 171.32076993584633, 185.67924201488495, 145.02639323472977],
    "score": 0.6922634840011597
}, 
{
    "name": "自行车",
    "bbox": [40.88740050792694, 22.707078605890274, 451.21362805366516, 326.0102793574333],
    "score": 0.6069411635398865
}]
```
  
## 了解更多 
  
http://docs.dubhe.ai/docs/module/dataset/import-dataset
   







