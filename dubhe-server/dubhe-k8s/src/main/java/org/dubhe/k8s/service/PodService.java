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
package org.dubhe.k8s.service;

import org.dubhe.k8s.domain.dto.PodLogDownloadQueryDTO;
import org.dubhe.k8s.domain.dto.PodLogQueryDTO;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.vo.PodLogQueryVO;
import org.dubhe.k8s.domain.vo.PodVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @description Pod业务接口
 * @date 2020-08-14
 */
public interface PodService {

    /**
     * 查询Pod信息
     * @param podQueryDTO pod查询条件对象
     * @return List<PodVO> pod对象集合
     */
    List<PodVO> getPods(PodQueryDTO podQueryDTO);

    /**
     * 按Pod名称分页查询Pod日志
     * @param podLogQueryDTO pod日志查询条件对象
     * @return PodLogQueryVO Pod日志
     */
    PodLogQueryVO getPodLog(PodLogQueryDTO podLogQueryDTO);

    /**
     * 获取单pod日志字符串
     * @param podLogQueryDTO pod日志查询条件对象
     * @return String Pod日志
     */
    String getPodLogStr(PodLogQueryDTO podLogQueryDTO);

    /**
     * 按Pod名称下载日志
     * @param podLogDownloadQueryDTO pod日志下载查询对象
     * @param response 响应
     */
    void downLoadPodLog(PodLogDownloadQueryDTO podLogDownloadQueryDTO, HttpServletResponse response);

    /**
     * 统计Pod日志数量
     * @param podLogDownloadQueryDTO 日志下载查询对象
     * @return Map<String,Long> String：Pod name，Long： Pod count
     */
    Map<String,Long> getLogCount(PodLogDownloadQueryDTO podLogDownloadQueryDTO);
}
