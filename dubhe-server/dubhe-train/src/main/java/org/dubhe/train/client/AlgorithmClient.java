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
package org.dubhe.train.client;

import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectAllBatchIdDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectAllByIdDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectByIdDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.TrainAlgorithmQureyVO;
import org.dubhe.train.client.fallback.AlgorithmClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @description 算法管理远程服务调用接口
 * @date 2020-12-21
 */
@FeignClient(value = ApplicationNameConst.SERVER_ALGORITHM, contextId = "algorithmClient", fallback = AlgorithmClientFallback.class)
public interface AlgorithmClient {

    /**
     *  根据Id查询所有数据
     * @param trainAlgorithmSelectAllByIdDTO 算法id
     * @return PtTrainAlgorithm 算法详情（包含软删除的数据）
     */
    @GetMapping("/algorithms/selectAllById")
    DataResponseBody<TrainAlgorithmQureyVO> selectAllById(@SpringQueryMap TrainAlgorithmSelectAllByIdDTO trainAlgorithmSelectAllByIdDTO);

    /**
     *  根据Id查询
     * @param trainAlgorithmSelectByIdDTO 算法id
     * @return PtTrainAlgorithm 算法详情
     */
    @GetMapping("/algorithms/selectById")
    DataResponseBody<TrainAlgorithmQureyVO> selectById(@SpringQueryMap TrainAlgorithmSelectByIdDTO trainAlgorithmSelectByIdDTO);

    /**
     *  根据算法id集合批量查询
     * @param trainAlgorithmSelectAllBatchIdDTO 算法id集合
     * @return List<PtTrainAlgorithm>   算法详情列表
     */
    @GetMapping("/algorithms/selectAllBatchIds")
    DataResponseBody<List<TrainAlgorithmQureyVO>> selectAllBatchIds(@SpringQueryMap TrainAlgorithmSelectAllBatchIdDTO trainAlgorithmSelectAllBatchIdDTO);

}


