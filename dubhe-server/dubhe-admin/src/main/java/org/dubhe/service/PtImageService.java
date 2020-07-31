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

package org.dubhe.service;

import org.dubhe.domain.dto.PtImageQueryDTO;
import org.dubhe.domain.dto.PtImageUploadDTO;
import org.dubhe.domain.entity.HarborProject;

import java.util.List;
import java.util.Map;

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
     * 定时到harbor同步imageName
     */
    void harborImageNameSync();


    /**
     * 通过imageName查询所含镜像版本信息
     *
     * @param imageName 镜像名
     * @return List<String>  镜像集合
     */
    List<String> searchImages(String imageName);


    /**
     * 查询harbor镜像列表
     *
     * @return List<HarborProject> harbor镜像集合
     **/
    List<HarborProject> getHarborProjectList();
}
