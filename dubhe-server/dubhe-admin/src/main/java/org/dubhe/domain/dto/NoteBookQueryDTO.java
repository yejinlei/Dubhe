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

package org.dubhe.domain.dto;

import lombok.Data;
import org.dubhe.annotation.Query;
import org.dubhe.enums.NoteBookStatusEnum;
import org.dubhe.utils.K8sCallBackTool;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @description 查询notebook请求体
 * @date 2020-04-28
 */
@Data
public class NoteBookQueryDTO implements Serializable {

    @Query(propName = "status", type = Query.Type.NE)
    private Integer neStatus;

    @Query(propName = "status", type = Query.Type.IN)
    private List<Integer> statusList;

    @Query(propName = "k8s_pvc_path", type = Query.Type.EQ)
    private String k8sPvcPath;

    @Query(propName = "user_id", type = Query.Type.EQ)
    private Long userId;

    @Query(propName = "last_operation_timeout", type = Query.Type.LT)
    private Long lastOperationTimeout;


    private static final NoteBookQueryDTO QUERY_CRITERIA;

    public NoteBookQueryDTO() {
    }

    public NoteBookQueryDTO(Integer neStatus, String k8sPvcPath, Long userId) {
        this.neStatus = neStatus;
        this.k8sPvcPath = k8sPvcPath;
        this.userId = userId;
    }

    static {
        QUERY_CRITERIA = new NoteBookQueryDTO();
        QUERY_CRITERIA.setNeStatus(NoteBookStatusEnum.DELETE.getCode());
    }

    /**
     * 获取需要刷新状态的查询条件
     *
     * @return
     */
    public static NoteBookQueryDTO getToRefreshCriteria() {
        NoteBookQueryDTO refreshCriteria = new NoteBookQueryDTO();
        refreshCriteria.setStatusList(Arrays.asList(
                NoteBookStatusEnum.STARTING.getCode()
                , NoteBookStatusEnum.STOPPING.getCode()
                , NoteBookStatusEnum.DELETING.getCode()));
        refreshCriteria.setLastOperationTimeout(K8sCallBackTool.getCurrentSecondLong());
        return refreshCriteria;
    }

    /**
     * 获取查询列表需要的查询条件
     *
     * @return
     */
    public static NoteBookQueryDTO getListCriteria() {
        return QUERY_CRITERIA;
    }


}
