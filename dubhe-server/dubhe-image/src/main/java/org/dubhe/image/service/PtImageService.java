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

package org.dubhe.image.service;

import org.dubhe.image.domain.dto.*;
import org.dubhe.image.domain.entity.PtImage;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 镜像服务service
 * @date 2020-06-22
 */
public interface PtImageService {

    /**
     * 查询镜像
     *
     * @param ptImageQueryDTO 查询条件
     * @return Map<String, Object> 镜像列表分页信息
     **/
    Map<String, Object> getImage(PtImageQueryDTO ptImageQueryDTO);


    /**
     * 上传镜像到harbor
     *
     * @param ptImageUploadDTO 上传条件
     */
    void uploadImage(PtImageUploadDTO ptImageUploadDTO);


    /**
     * 获取镜像信息
     *
     * @param ptImageQueryImageDTO 查询条件
     * @return List<String>  镜像集合
     */
    List<PtImage> searchImages(PtImageQueryImageDTO ptImageQueryImageDTO);

    /**
     * 删除镜像
     *
     * @param imageDeleteDTO 删除镜像条件参数
     */
    void deleteTrainImage(PtImageDeleteDTO imageDeleteDTO);

    /**
     * 修改镜像信息
     *
     * @param imageUpdateDTO 修改的镜像信息
     */
    void updateTrainImage(PtImageUpdateDTO imageUpdateDTO);


    /**
     * 获取镜像名称列表
     * @param ptImageQueryNameDTO 获取镜像名称列表查询条件
     * @return Set<String> 镜像列表
     */
    Set<String> getImageNameList(PtImageQueryNameDTO ptImageQueryNameDTO);

    /**
     * 修改镜像来源(notebook定制)
     *
     * @param id 镜像id
     */
    void updImageResource(Long id);

    /**
     * 获取镜像URL
     *
     * @param imageQueryUrlDTO 查询镜像路径DTO
     * @return String 镜像url
     */
    String getImageUrl(PtImageQueryUrlDTO imageQueryUrlDTO);

    /**
     * 镜像回收还原
     *
     * @param dto 还原DTO对象
     */
    void recycleRollback(RecycleCreateDTO dto);

    /**
     * 获取终端镜像列表
     *
     * @return List<PtImage>
     */
    List<PtImage> getTerminalImageList();
}
