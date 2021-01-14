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
package org.dubhe.harbor.api;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.harbor.domain.vo.ImageVO;
import java.util.List;
import java.util.Map;

/**
 * @description HarborApi接口
 * @date 2020-07-01
 */
public interface HarborApi {
    /**
     * 通过ProjectName名称查询镜像名称
     *
     * @param  projectNameList 项目名称集合
     * @return List<String> 镜像名称集合
     */
    List<String> searchImageNames(List<String> projectNameList);
    /**
     * 通过projectM名称查询镜像名称
     *
     * @param projectNameList 项目名称集合
     * @return List<Map> 镜像结果集
     */
    List<Map> searchImageByProjects(List<String> projectNameList);

    /**
     * 根据完整镜像路径查询判断是否具有镜像
     *
     * @param imageUrl 镜像路径
     * @return Boolean true 存在 false 不存在
     */
    Boolean isExistImage(String imageUrl);
    /**
     * 查询所有镜像名称不用根据ProjectName进行查询
     *
     * @return List<Map> 镜像结果集
     **/
    List<Map> findImageList();
    /**
     * 分页查询（镜像）
     *
     * @param  page 分页对象
     * @return ImageVO 镜像vo对象
     **/
    ImageVO findImagePage(Page page);
    /**
     * 删除镜像
     *
     * @param  imageUrl 镜像的完整路径
     */
    void deleteImageByTag(String imageUrl);

}
