# 数据处理模块
该模块提供平台数据处理中自动标注、多目标跟踪、数据增强以及OFRecord转换服务

## 依赖

- opencv-python 
- numpy
- web.py
- oneflow
- Pillow
- scipy

## 服务启动
- 自动标注
```bash
python oneflow/run_label_server.py -p xxxx -m xxx
python oneflow/imagenet_server.py -p xxxx -m xxx
```
- 数据增强
```bash
python oneflow/img_process_server.py -p xxxx -m xxx
```
- 目标跟踪
```bash
python oneflow/track_srver.py -p xxxx -m xxx
```
- OFRecord转换
```bash
python oneflow/ofrecord_server.py -p xxxx -m xxx
```