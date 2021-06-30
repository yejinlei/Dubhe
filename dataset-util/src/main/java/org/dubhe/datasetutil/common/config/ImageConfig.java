/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.datasetutil.common.config;

import lombok.Data;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @description 图片格式配置文件
 * @date 2020-10-29
 */
@Data
@Component
@ConfigurationProperties(prefix = "suffix")
public class ImageConfig {
    /**
     * 图片格式字符串
     */
    private String imageFormat;

    /**
     * 文本格式字符串
     */
    private String txtFormat;

    /**
     * 构建图片格式集合
     *
     * @return List<String>
     */
    public List<String> buildImageFormat() {
        return Arrays.asList(imageFormat.split(BusinessConstant.COMMA));
    }

    /**
     * 构建文本格式集合
     *
     * @return List<String>
     */
    public List<String> buildTxtFormat() {
        return Arrays.asList(txtFormat.split(BusinessConstant.COMMA));
    }

}
