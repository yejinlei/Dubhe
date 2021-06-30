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

package org.dubhe.k8s.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * @description yaml基本操作工具类
 * @date 2020-04-15
 */
public class YamlUtils {

    /**
     * 导出yaml时无论有没有值都忽略的属性
     **/
    private final static ImmutableList<String> IGNORE_PROPERTIES_WHEN_DUMP_YAML = ImmutableList.of("resourceVersion");

    private static Yaml yaml;

    static {
        initYaml();
    }

    /**
     * 初始化Yaml
     */
    private static void initYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        yaml = new Yaml(new Representer() {
            @Override
            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
                // 如果属性为null空数组，忽略它
                boolean beIgnored = (propertyValue instanceof Collection && ((Collection) propertyValue).size() == 0);
                if (propertyValue == null || beIgnored) {
                    return null;
                } else {
                    if (IGNORE_PROPERTIES_WHEN_DUMP_YAML.contains(property.getName())) {
                        return null;
                    }
                    return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                }
            }
        }, options);
    }

    /**
     * javabean转yaml字符串
     *
     * @param resource 对象
     * @return String
     */
    public static String convertToYaml(Object resource) {
        return yaml.dump(resource);
    }

    /**
     * javabean导出成yaml文件
     *
     * @param resource 资源名称
     * @param filePath 文件路径
     * @return void
     */
    public static void dumpToYaml(Object resource, String filePath) {
        try (Writer writer = new FileWriter(filePath)) {
            yaml.dump(resource, writer);
        } catch (IOException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "dumpToYaml error:{}", e);
        }
    }

    /**
     * 转换成Yml格式字符串
     *
     * @param obj KubernetesResource对象
     * @return yml格式字符串
     */
    public static String dumpAsYaml(HasMetadata obj) {
        try {
            return SerializationUtils.dumpAsYaml(obj);
        } catch (JsonProcessingException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "dumpAsYamle error:{}", e);
        }
        return SymbolConstant.BLANK;
    }
}
