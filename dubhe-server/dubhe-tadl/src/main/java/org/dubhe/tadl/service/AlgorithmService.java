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
package org.dubhe.tadl.service;

import org.dubhe.tadl.domain.dto.*;
import org.dubhe.tadl.domain.entity.Algorithm;
import org.dubhe.tadl.domain.vo.AlgorithmNextVersionVO;
import org.dubhe.tadl.domain.vo.AlgorithmVO;

import java.util.List;

/**
 * @description 算法管理服务类
 * @date 2020-03-22
 */
public interface AlgorithmService {

    /**
     * 解压算法包
     *
     * @param zipPath     压缩包路径
     * @return    解压是否成功
     */
    void unzip(String zipPath);

    /**
     * 获取yaml
     *
     * @param algorithm     算法名称
     * @param stageOrder    算法阶段
     * @param versionName   算法版本名称
     * @return yaml字符串
     */
    String getYaml(String algorithm, Integer stageOrder, String versionName);

    /**
     * 创建算法
     *
     * @param algorithmCreateDTO 创建算法DTO
     */
    void create(AlgorithmCreateDTO algorithmCreateDTO);

    /**
     * 算法列表查询
     *
     * @param content 搜索内容
     * @return 算法列表
     */
    List<AlgorithmVO> query(String content);

    /**
     * 更新算法
     *
     * @param algorithmUpdateDTO 更新算法DTO
     */
    void update(AlgorithmUpdateDTO algorithmUpdateDTO);

    /**
     * 根据版本删除算法
     *
     * @param algorithmVersionDeleteDTO 删除算法DTO
     */
    void delete(AlgorithmVersionDeleteDTO algorithmVersionDeleteDTO);

    /**
     * 查询算法 三个/一个 阶段数据
     *
     * @param algorithmId        算法id
     * @param stageOrder         算法阶段
     * @param algorithmVersionId 算法版本
     * @return 算法三个阶段数据
     */
    AlgorithmVO query(Integer algorithmId, Integer stageOrder, Long algorithmVersionId);

    /**
     * 根据算法 id 获取未删除的算法对象
     * @param algorithmId 算法id
     * @return 算法对象
     */
    Algorithm selectOneById(Long algorithmId);

    /**
     * 获取算法将要发布的版本号
     *
     * @param algorithmId 算法id
     * @return 下一个版本号名称
     */
    AlgorithmNextVersionVO getNextVersionName(Long algorithmId);

    /**
     * 版本发布
     *
     * @param algorithmVersionCreateDTO 算法版本创建DTO
     */
    void publish(AlgorithmVersionCreateDTO algorithmVersionCreateDTO);

    /**
     * 算法版本切换
     *
     * @param algorithmId         算法ID
     * @param algorithmVersionId  版本ID
     */
    void versionSwitch(Long algorithmId, Long algorithmVersionId);
    /**
     * 算法复制
     * @param zipPath
     * @return
     */
    void copyAlgorithm(String zipPath);

    /**
     * 读取yaml文件
     * @param algorithmYamlQueryDTO yaml查询条件
     * @return yaml参数
     */
    String readYaml(AlgorithmYamlQueryDTO algorithmYamlQueryDTO);

    /**
     * 通过id获取已删除或者未删除算法对象
     * @param algorithmId 算法id
     * @return 算法
     */
    Algorithm getOneById(Long algorithmId);
}
