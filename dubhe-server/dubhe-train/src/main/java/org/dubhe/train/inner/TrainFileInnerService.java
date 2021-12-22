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

package org.dubhe.train.inner;

import org.dubhe.train.domain.vo.PtImageAndAlgorithmVO;

/**
 * @description  训练内部文件服务类
 * @date 2021-09-22
 */
public interface TrainFileInnerService {

    /**
     * 构造训练文件路径
     * @param userId
     * @param jobName
     * @return
     */
    String buildTrainCommonPath(Long userId, String jobName);

    /**
     * 构造训练文件相对路径
     *
     * @param userId
     * @param jobName
     * @return
     */
    String buildTrainRelativePath(Long userId, String jobName);

    /**
     * 拷贝训练的算法代码
     *
     * @param trainCommonPath
     * @param algorithmCodeDir
     * @return
     */
    void copyTrainAlgorithmCode(String trainCommonPath, String algorithmCodeDir);

    /**
     * 获取workspace目录
     *
     * @param imageAndAlgorithmVO
     * @return
     */
    String getWorkSpaceDir(PtImageAndAlgorithmVO imageAndAlgorithmVO);
}
