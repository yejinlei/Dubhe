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

package org.dubhe.data.constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 数据集标签类型
 * @date 2020-07-01
 */
public enum DatasetLabelEnum {

    /**
     * 自定义标签
     */
    CUSTOM(0, "自定义标签"),
    /**
     * 自动标注标签
     */
    AUTO(1, "自动标注标签"),
    /**
     * imageNet
     */
    IMAGE_NET(2, "ImageNet"),
    /**
     * MS COCO
     */
    MS_COCO(3, "MS COCO"),
    /**
     * 文本
     */
    TXT(4, "文本");

    DatasetLabelEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    private Integer type;
    private String name;

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    /**
     * 预置标签类型
     */
    public static final List<DatasetLabelEnum> PRESET_LABELS = new ArrayList<DatasetLabelEnum>() {{
        add(DatasetLabelEnum.IMAGE_NET);
        add(DatasetLabelEnum.MS_COCO);
    }};

    /**
     * 获取所有预置标签 web端展示用
     *
     * @return
     */
    public static Map<Integer, String> getPresetLabels() {
        return PRESET_LABELS.stream().collect(Collectors.toMap(datasetLabelEnum -> datasetLabelEnum.getType(), datasetLabelEnum -> datasetLabelEnum.getName()));
    }

}
