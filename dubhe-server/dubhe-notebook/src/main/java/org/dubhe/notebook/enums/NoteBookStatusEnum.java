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

package org.dubhe.notebook.enums;


import org.dubhe.k8s.enums.PodPhaseEnum;


/**
 * @description notebook 状态枚举
 * @date 2020-4-28
 */
public enum NoteBookStatusEnum {
    /**
     * 运行
     */
    RUN(0, "运行中"),
    /**
     * 停止
     */
    STOP(1, "停止"),
    /**
     * 删除
     */
    DELETED(2, "删除"),
    /**
     * 启动中
     */
    STARTING(3, "启动中"),
    /**
     * 停止中
     */
    STOPPING(4, "停止中"),
    /**
     * 删除中
     */
    DELETING(5, "删除中"),
    /**
     * 运行异常
     */
    ERROR(6, "运行异常");
    /**
     * 编码
     */
    private Integer code;
    /**
     * 描述
     */
    private String description;

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    NoteBookStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
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

    public static boolean deletable(Integer code) {
        return STOP.getCode().equals(code) || ERROR.getCode().equals(code);
    }

}
