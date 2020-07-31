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

package org.dubhe.data.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @description 用于映射前端文件筛选与后端文件状态
 * @date 2020-05-06
 */
@Getter
public enum FileTypeEnum {
    /**
     * 全部
     */
    All(0, "全部"),
    /**
     * 未标注
     */
    UNFINISHED(1, "未标注"),
    /**
     * 自动标注完成
     */
    AUTO_FINISHED(2, "自动标注完成"),
    /**
     * 已标注完成
     */
    FINISHED(3, "手动标注完成"),
    /**
     * 自动目标跟踪完成
     */
    AUTO_TRACK_FINISHED(4, "自动目标跟踪完成");

    static Set<Integer> ALL_STATUS = new HashSet<Integer>() {{
        addAll(FileStatusEnum.getAllValue());
    }};

    static Set<Integer> UNFINISHED_STATUS = new HashSet<Integer>() {{
        add(FileStatusEnum.INIT.getValue());
        add(FileStatusEnum.ANNOTATING.getValue());
    }};

    static Set<Integer> AUTO_FINISHED_STATUS = new HashSet<Integer>() {{
        add(FileStatusEnum.AUTO_ANNOTATION.getValue());
    }};

    static Set<Integer> FINISHED_STATUS = new HashSet<Integer>() {{
        add(FileStatusEnum.FINISHED.getValue());
    }};

    static Set<Integer> AUTO_TRACK_FINISHED_STATUS = new HashSet<Integer>() {{
        add(FileStatusEnum.FINISH_AUTO_TRACK.getValue());
    }};

    private static final Map<Integer, Set<Integer>> TYPE_STATUS_MAP = new HashMap<Integer, Set<Integer>>() {{
        put(All.value, ALL_STATUS);
        put(UNFINISHED.value, UNFINISHED_STATUS);
        put(AUTO_FINISHED.value, AUTO_FINISHED_STATUS);
        put(FINISHED.value, FINISHED_STATUS);
        put(AUTO_TRACK_FINISHED.value, AUTO_TRACK_FINISHED_STATUS);
    }};

    FileTypeEnum(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private int value;
    private String msg;

    /**
     * 获取指定数据集状态下的文件状态列表
     *
     * @param type
     * @return
     */
    public static Set<Integer> getStatus(Integer type) {
        return TYPE_STATUS_MAP.get(type);
    }

}
