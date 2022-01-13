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
package org.dubhe.tadl.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Objects;

/**
 * @description yaml解析工具
 * @date 2020-03-22
 */
@Component
public class YamlParseUtil {

    /**
     * 解析yaml
     *
     * @param yaml 输入
     * @return Yaml
     */
    public static HashMap<String, Object> YamlParse(String yaml) {
        if (ObjectUtils.isEmpty(yaml)) {
            return null;
        }
        return new Yaml().load(yaml);
    }


    /**
     * 将 yaml 转换为Java对象
     *
     * @param yaml       yaml 字符串
     * @param stageClazz 阶段类
     * @param <T>        阶段
     * @return 阶段对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getStageObj(String yaml, Class<T> stageClazz) {
        //yaml(str) -> map -> convert to camel style -> str(yaml) -> JSON ->Object
        JSONObject json = new JSONObject(KeyNameConvertUtil.convertToCamelStyle(
                Objects.requireNonNull(
                        YamlParseUtil.YamlParse(yaml)
                )
        ));
        return (T) JSON.toJavaObject(json, stageClazz);
    }

}
