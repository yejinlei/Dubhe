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

package org.dubhe.enums;


import org.dubhe.base.MagicNumConstant;
import org.dubhe.k8s.enums.PodPhaseEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @description notebook 状态枚举
 * @create 2020-4-28
 */
public enum NoteBookStatusEnum {
    /**
     * 运行
     */
    RUN(0, "运行中", 0),
    /**
     * 停止
     */
    STOP(1, "停止", 0),
    /**
     * 删除
     */
    DELETE(2, "删除", 0),
    /**
     * 启动中
     */
    STARTING(3, "启动中", 1),
    /**
     * 停止中
     */
    STOPPING(4, "停止中", 1),
    /**
     * 删除中
     */
    DELETING(5, "删除中", 1),
    /**
     * 运行异常
     */
    ERROR(6, "运行异常", 0);
    /**
     * 编码
     */
    private Integer code;
    /**
     * 描述
     */
    private String description;
    /**
     * 删除标识
     */
    private Integer deleteType;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDeleteType() {
        return deleteType;
    }

    public void setDeleteType(Integer deleteType) {
        this.deleteType = deleteType;
    }

    NoteBookStatusEnum(int code, String description, int deleteType) {
        this.code = code;
        this.description = description;
        this.deleteType = deleteType;
    }

    public static String getDescription(Integer code) {
        if (code != null) {
            for (NoteBookStatusEnum en : NoteBookStatusEnum.values()) {
                if (en.getCode().equals(code)) {
                    return en.getDescription();
                }
            }
        }
        return null;
    }

    /**
     * k8s状态转换成NoteBook状态
     * 当查询不到k8s状态时即为删除
     *
     * @param bizPodPhase
     * @return
     */
    public static NoteBookStatusEnum convert(String bizPodPhase) {
        if (bizPodPhase == null
                || PodPhaseEnum.DELETED.getPhase().equals(bizPodPhase)
                || PodPhaseEnum.FAILED.getPhase().equals(bizPodPhase)) {
            return STOP;
        } else if (PodPhaseEnum.PENDING.getPhase().equals(bizPodPhase)) {
            return STARTING;
        } else if (PodPhaseEnum.RUNNING.getPhase().equals(bizPodPhase)) {
            return RUN;
        } else if (PodPhaseEnum.SUCCEEDED.getPhase().equals(bizPodPhase)) {
            return STOPPING;
        } else {
            return ERROR;
        }
    }


    /**
     * 查找可以删除的状态集合
     *
     * @return List<Integer>
     */
    public static List<Integer> getCanDeleteStatus() {
        List<Integer> list = new ArrayList<>();
        for (NoteBookStatusEnum en : NoteBookStatusEnum.values()) {
            if (en.getDeleteType().equals(MagicNumConstant.ONE)) {
                list.add(en.getCode());
            }
        }
        return list;
    }

}
