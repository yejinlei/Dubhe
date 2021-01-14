/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */
package org.dubhe.constant;

import lombok.Data;

/**
 * @description 云端serving常量
 * @date 2020-09-14
 */
@Data
public class ServingConstant {
    /**
     * 推理脚本挂载路径
     */
    public static final String REASONING_SCRIPT_PATH = "/usr/local/TS_Serving/reasoningScript/";
    /**
     * 输入数据挂载路径
     */
    public static final String INPUT_PATH = "/usr/local/TS_Serving/input/";
    /**
     * 输出数据挂载路径
     */
    public static final String OUTPUT_PATH = "/usr/local/TS_Serving/output/";
    /**
     * 模型接口配置挂载路径
     */
    public static final String MODEL_CONFIG_PATH = "/usr/local/TS_Serving/modelConfig/";
    /**
     * 模型挂载路径
     */
    public static final String MODEL_PATH = "/usr/local/TS_Serving/models/";
    /**
     * 日志挂载路径
     */
    public static final String LOG_PATH = "/usr/local/TS_Serving/logs/";
    /**
     * Serving源码挂载路径
     */
    public static final String TS_SERVING_PATH = "/usr/local/TS_Serving/serving";
    /**
     * POD HTTP端口
     */
    public static final Integer POD_HTTP_PORT = 5000;
    /**
     * HTTP 接口端口
     */
    public static final String HTTP_PORT = "32493";
    /**
     * POD GRPC端口
     */
    public static final Integer POD_GRPC_PORT = 5000;
    /**
     * 在线服务
     */
    public static final String SERVING = "serving-";
    /**
     * 批量服务
     */
    public static final String BATCH_SERVING = "batch-serving-";
    /**
     * 成功响应code
     */
    public static final String SUCCESS_CODE = "200";
    /**
     * 成功
     */
    public static final Integer SUCCESS = 1;
    /**
     * 失败
     */
    public static final Integer FAILED = 0;
    /**
     * cpu规格
     */
    public static final String CPU_SPECS = "cpu_specs";
    /**
     * gpu规格
     */
    public static final String GPU_SPECS = "gpu_specs";
    /**
     * pytorch模型格式后缀
     */
    public static final String PYTORCH_MODEL_SUFFIX = ".pth";
    /**
     * 路由消息Stream
     */
    public static final String SERVING_STREAM = "serving_stream";
    /**
     * redis中调用次数统计key值
     */
    public static final String INFERENCE_METRICS = "serving:inference:metrics:";
    /**
     * 拷贝文件命令
     */
    public static final String COPY_COMMAND = "ssh %s@%s \"rm -rf %s && mkdir -p %s && cp -r %s/* %s && echo success\"";
    /**
     * 推理支持图片类型
     */
    public static final String IMAGE_FORMAT = ".jpg;.png;.bmp;.jpeg";
    /**
     * http推理脚本
     */
    public static final String HTTP_SCRIPT = "http_server.py";
    /**
     * grpc推理脚本
     */
    public static final String GRPC_SCRIPT = "grpc_server.py";
    /**
     * 在线服务启动命令
     */
    public static final String SERVING_COMMAND = "python %s/%s --platform='%s' --model_path='%s' --use_gpu=%s";
    /**
     * 批量服务启动命令
     */
    public static final String BATCH_COMMAND = "export NODE_IPS=`cat /home/hostfile.json |jq -r '.[]|.ip'|paste -d \",\" -s` && python %s/batch_server.py --platform='%s' --model_path='%s' --input_path='%s' --output_path='%s' --enable_distributed=%s --use_gpu=%s";

    /**
     * 部署模型的推理接口名称
     */
    public static final String INFERENCE_INTERFACE_NAME = "/inference";
    /**
     * 批量服务nfs输出路径
     */
    public static final String OUTPUT_NFS_PATH = "/serving/output/";
    /**
     * 批量服务slave节点
     */
    public static final String SLAVE_POD = "-slave-";

}
