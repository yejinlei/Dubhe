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

package org.dubhe.train.domain.dto;

import lombok.Data;
import org.dubhe.biz.base.dto.PtDatasetSmallDTO;
import org.dubhe.biz.base.dto.PtStorageSmallDTO;
import org.dubhe.biz.base.dto.TeamSmallDTO;
import org.dubhe.biz.base.dto.UserSmallDTO;


import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 训练任务
 * @date 2020-03-17
 */
@Data
public class PtTrainJobDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String remark;
    private PtDatasetSmallDTO dataset;
    private PtImageSmallDTO image;
    private PtStorageSmallDTO storage;
    private TeamSmallDTO team;
    private String selectorLabel;
    private Integer podNum;
    private Integer gpuNum;
    private Integer memNum;
    private Integer cpuNum;
    private Integer level;
    private Timestamp startTime;
    private Timestamp closeTime;
    private String cmd;
    private String status;
    private UserSmallDTO confirmUser;
    private UserSmallDTO createUser;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Boolean deleted;
    private Long originUserId;
}
