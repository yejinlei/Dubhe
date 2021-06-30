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
package org.dubhe.algorithm.async;

import org.dubhe.algorithm.client.NoteBookClient;
import org.dubhe.algorithm.constant.AlgorithmConstant;
import org.dubhe.algorithm.dao.PtTrainAlgorithmMapper;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmCreateDTO;
import org.dubhe.algorithm.domain.entity.PtTrainAlgorithm;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.NoteBookAlgorithmUpdateDTO;
import org.dubhe.biz.base.enums.AlgorithmStatusEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.utils.K8sNameTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @description 异步上传算法
 * @date 2020-08-10
 */
@Component
public class TrainAlgorithmUploadAsync {

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private NoteBookClient noteBookClient;

    @Autowired
    private PtTrainAlgorithmMapper trainAlgorithmMapper;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    /**
     * 异步任务创建算法
     *
     * @param user 当前登录用户信息
     * @param ptTrainAlgorithm 算法信息
     * @param trainAlgorithmCreateDTO 创建算法条件
     */
    @Async(AlgorithmConstant.ALGORITHM_EXECUTOR)
    public void createTrainAlgorithm(UserContext user, PtTrainAlgorithm ptTrainAlgorithm, PtTrainAlgorithmCreateDTO trainAlgorithmCreateDTO) {
        String path = fileStoreApi.getBucket() + trainAlgorithmCreateDTO.getCodeDir();
        //校验创建算法来源(true:由fork创建算法，false：其它创建算法方式),若为true则拷贝预置算法文件至新路径
        if (trainAlgorithmCreateDTO.getFork()) {
            //生成算法相对路径
            String algorithmPath = k8sNameTool.getPath(BizPathEnum.ALGORITHM, user.getId());
            //拷贝预置算法文件夹
            boolean copyResult = fileStoreApi.copyPath(fileStoreApi.getRootDir() + path, fileStoreApi.getRootDir() + fileStoreApi.getBucket() + algorithmPath);
            if (!copyResult) {
                LogUtil.error(LogEnum.BIZ_ALGORITHM, "The user {} copied the preset algorithm path {} successfully", user.getUsername(), path);
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

        LogUtil.info(LogEnum.BIZ_ALGORITHM, "async update algorithmPath by algorithmId:{} and update noteBook by noteBookId:{}", ptTrainAlgorithm.getId(), trainAlgorithmCreateDTO.getNoteBookId());
        if (flag) {
            ptTrainAlgorithm.setAlgorithmStatus(AlgorithmStatusEnum.SUCCESS.getCode());
            //更新fork算法新路径
            trainAlgorithmMapper.updateById(ptTrainAlgorithm);
            //保存算法根据notbookId更新算法id
            if (trainAlgorithmCreateDTO.getNoteBookId() != null) {
                LogUtil.info(LogEnum.BIZ_ALGORITHM, "Save algorithm Update algorithm ID :{} according to notBookId:{}", trainAlgorithmCreateDTO.getNoteBookId(), ptTrainAlgorithm.getId());
                NoteBookAlgorithmUpdateDTO noteBookAlgorithmUpdateDTO = new NoteBookAlgorithmUpdateDTO();
                noteBookAlgorithmUpdateDTO.setAlgorithmId(ptTrainAlgorithm.getId());
                noteBookAlgorithmUpdateDTO.setNotebookIdList(Arrays.asList(trainAlgorithmCreateDTO.getNoteBookId()));
                noteBookClient.updateNoteBookAlgorithm(noteBookAlgorithmUpdateDTO);
            }
        } else {
            ptTrainAlgorithm.setAlgorithmStatus(AlgorithmStatusEnum.FAIL.getCode());
            trainAlgorithmMapper.updateById(ptTrainAlgorithm);
        }
    }
}
