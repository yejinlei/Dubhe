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
package org.dubhe.dubhek8s.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

/**
 * @description 任务 资源占用信息展示 VO
 * @date 2021-7-29
 */
@Data
@Accessors(chain = true)
public class TaskResVO {
    /**
     * 任务 ID
     */
    private Long taskId;
    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 业务标签
     */
    private String businessLabel;

    /**
     * 该任务所有Pod资源占用信息
     */
    List<PodResVO> podResVOS;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskResVO taskResVO = (TaskResVO) o;
        return Objects.equals(taskId, taskResVO.taskId) &&
                Objects.equals(taskName, taskResVO.taskName) &&
                Objects.equals(businessLabel, taskResVO.businessLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, taskName, businessLabel);
    }
}
