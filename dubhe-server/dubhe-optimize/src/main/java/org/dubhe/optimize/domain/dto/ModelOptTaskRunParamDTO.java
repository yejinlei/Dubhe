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

package org.dubhe.optimize.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description 模型优化创建资源运行的参数类
 * @date 2020-05-25
 */
@Data
@Accessors(chain = true)
public class ModelOptTaskRunParamDTO {

    /**
     * 实例id
     **/
    private Long instId;
    /**
     * 使用的镜像
     **/
    private String imageUrl;
    /**
     * 输入目录
     **/
    private String inputDir;
    /**
     * 输出目录
     **/
    private String outputDir;
    /**
     * 算法目录
     **/
    private String algorithmDir;
    /**
     * 日志目录
     **/
    private String logDir;
    /**
     * 数据集路径
     **/
    private String datasetDir;
    /**
     * 压缩前结果json目录
     **/
    private String resultJsonBeforeDir;
    /**
     * 压缩后结果json目录
     **/
    private String resultJsonAfterDir;
}
