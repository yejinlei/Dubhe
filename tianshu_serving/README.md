## 云端Serving代码
---
**介绍**
----
+ **支持oneflow、tensorflow、pytorch三种框架模型部署** </br>

    1、通过如下命令启动http在线推理服务
    
        ```
        python http_server.py  --platform='框架名称' --model_path='模型地址'
        
        ```   
        
     通过访问localhost:5000/docs进入swagger页面，调用localhost:5000/inference进行图片上传得道推理结果，结果如下所示：
       
       ```
        {
            "image_name": "哈士奇.jpg",
            "predictions": [
                {
                    "label": "Eskimo dog, husky",
                    "probability": "0.679"
                },
                {
                    "label": "Siberian husky",
                    "probability": "0.213"
                },
                {
                    "label": "dogsled, dog sled, dog sleigh",
                    "probability": "0.021"
                },
                {
                    "label": "malamute, malemute, Alaskan malamute",
                    "probability": "0.006"
                },
                {
                    "label": "white wolf, Arctic wolf, Canis lupus tundrarum",
                    "probability": "0.001"
                }
            ]
        }
        
       ```
    2、同理通过如下命令启动grpc在线推理服务
    
        ```
        python grpc_server.py  --platform='框架名称' --model_path='模型地址'
        
        ``` 
        
     再启动grpc_client.py进行上传图片推理得道结果，或者根据ip端口自行编写grpc客户端
       
    3、支持多模型部署，可以自行配置config文件夹下的model_config_file.json进行多模型配置，启动http或grpc时输入不同的模型名称即可，或者自行修改inference接口入参来达到启动单一服务多模型推理的功能
    
+ **支持分布式模型部署推理** </br>

    需要推理大量图片时需要分布式推理功能，执行如下命令：
    
        ```
        python batch_server.py  --platform='框架名称' --model_path='模型地址' --input_path='批量图片地址' --output_path='输出JSON文件地址'
        
        ``` 
    输入的所有图片保存在input文件夹下，输入json文件保存在output_path文件夹，json名称与图片名称对应
    
+ **代码还包含了各种参数配置，日志文件输出、是否启用TLS等** </br>
        