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
package org.dubhe.async;

import org.dubhe.config.NfsConfig;
import org.dubhe.dao.PtTrainAlgorithmMapper;
import org.dubhe.domain.dto.PtTrainAlgorithmCreateDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.PtTrainAlgorithm;
import org.dubhe.enums.AlgorithmStatusEnum;
import org.dubhe.enums.BizNfsEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.NoteBookService;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.LocalFileUtil;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @description 异步上传算法
 * @date 2020-08-10
 */
@Component
public class TrainAlgorithmUploadAsync {

    @Autowired
    private LocalFileUtil localFileUtil;

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private NoteBookService noteBookService;

    @Autowired
    private PtTrainAlgorithmMapper trainAlgorithmMapper;

    /**
     * 异步任务创建算法
     *
     * @param user 当前登录用户信息
     * @param ptTrainAlgorithm 算法信息
     * @param trainAlgorithmCreateDTO 创建算法条件
     */
    @Async("trainExecutor")
    public void createTrainAlgorithm(UserDTO user, PtTrainAlgorithm ptTrainAlgorithm, PtTrainAlgorithmCreateDTO trainAlgorithmCreateDTO) {
        String path = nfsConfig.getBucket() + trainAlgorithmCreateDTO.getCodeDir();
        //校验创建算法来源(true:由fork创建算法，false：其它创建算法方式),若为true则拷贝预置算法文件至新路径
        if (trainAlgorithmCreateDTO.getFork()) {
            //生成算法相对路径
            String algorithmPath = k8sNameTool.getNfsPath(BizNfsEnum.ALGORITHM, user.getId());
            //拷贝预置算法文件夹
            boolean copyResult = localFileUtil.copyPath(path, nfsConfig.getBucket() + algorithmPath);
            if (!copyResult) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} copied the preset algorithm path {} successfully", user.getUsername(), path);
                updateTrainAlgorithm(ptTrainAlgorithm, trainAlgorithmCreateDTO, false);
                throw new BusinessException("内部错误");
            }

            ptTrainAlgorithm.setCodeDir(algorithmPath);

            //修改算法上传状态
            updateTrainAlgorithm(ptTrainAlgorithm, trainAlgorithmCreateDTO, true);

        } else {
            updateTrainAlgorithm(ptTrainAlgorithm, trainAlgorithmCreateDTO, true);
        }
    }


    /**
     * 更新上传算法状态
     *
     * @param ptTrainAlgorithm 算法信息
     * @param trainAlgorithmCreateDTO 创建算法的条件
     * @param flag 创建算法是否成功(true:成功，false:失败)
     */
    public void updateTrainAlgorithm(PtTrainAlgorithm ptTrainAlgorithm, PtTrainAlgorithmCreateDTO trainAlgorithmCreateDTO, boolean flag) {

        LogUtil.info(LogEnum.BIZ_TRAIN, "async update algorithmPath by algorithmId:{} and update noteBook by noteBookId:{}", ptTrainAlgorithm.getId(), trainAlgorithmCreateDTO.getNoteBookId());
        if (flag) {
            ptTrainAlgorithm.setAlgorithmStatus(AlgorithmStatusEnum.SUCCESS.getCode());
            //更新fork算法新路径
            trainAlgorithmMapper.updateById(ptTrainAlgorithm);
            //保存算法根据notbookId更新算法id
            if (trainAlgorithmCreateDTO.getNoteBookId() != null) {
                LogUtil.info(LogEnum.BIZ_TRAIN, "Save algorithm Update algorithm ID :{} according to notBookId:{}", trainAlgorithmCreateDTO.getNoteBookId(), ptTrainAlgorithm.getId());
                noteBookService.updateTrainIdByNoteBookId(trainAlgorithmCreateDTO.getNoteBookId(), ptTrainAlgorithm.getId());
            }
        } else {
            ptTrainAlgorithm.setAlgorithmStatus(AlgorithmStatusEnum.FAIL.getCode());
            trainAlgorithmMapper.updateById(ptTrainAlgorithm);
        }
    }
}
