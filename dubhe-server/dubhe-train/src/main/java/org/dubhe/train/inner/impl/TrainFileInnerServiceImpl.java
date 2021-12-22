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

package org.dubhe.train.inner.impl;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.train.config.TrainJobConfig;
import org.dubhe.train.domain.vo.PtImageAndAlgorithmVO;
import org.dubhe.train.inner.TrainFileInnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.dubhe.train.constant.TrainConstant.*;

/**
 * @description  训练内部文件服务实现类
 * @date 2021-09-22
 */
@Component
public class TrainFileInnerServiceImpl implements TrainFileInnerService {
    @Resource
    private TrainJobConfig trainJobConfig;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;


    /**
     * @see TrainFileInnerService#buildTrainCommonPath(Long, String)
     */
    @Override
    public String buildTrainCommonPath(Long userId, String jobName) {
        return String.format(TRAIN_PATH_PATTERN, fileStoreApi.getBucket() + trainJobConfig.getManage(), userId, jobName);
    }

    /**
     * @see TrainFileInnerService#buildTrainRelativePath(Long, String)
     */
    @Override
    public String buildTrainRelativePath(Long userId, String jobName) {
        return String.format(TRAIN_RELATIVE_PATH_PATTERN, trainJobConfig.getManage(), userId, jobName);
    }


    /**
     * @see TrainFileInnerService#copyTrainAlgorithmCode(String, String)
     */
    public void copyTrainAlgorithmCode(String trainCommonPath, String algorithmCodeDir) {
        String[] algorithmCodeDirArray = algorithmCodeDir.split(StrUtil.SLASH);
        String workspaceDir = algorithmCodeDirArray[algorithmCodeDirArray.length - 1];
        // 算法路径待拷贝的地址
        String sourcePath = fileStoreApi.getBucket() + algorithmCodeDir.substring(1);
        String trainDir = trainCommonPath.substring(1) + StrUtil.SLASH + workspaceDir;
        LogUtil.info(LogEnum.BIZ_TRAIN, "Algorithm path copy sourcePath:{},commonPath:{},trainDir:{}", sourcePath, trainCommonPath, trainDir);
        boolean result = fileStoreApi.copyPath(fileStoreApi.getRootDir() + sourcePath.substring(1), fileStoreApi.getRootDir() + trainDir);
        if (!result) {
            LogUtil.error(LogEnum.BIZ_TRAIN, " it failed to copy algorithm directory {} to the target directory {}", sourcePath.substring(1),
                    trainDir);
            throw new BusinessException("训练算法文件拷贝失败");
        }
    }

    /**
     * @see TrainFileInnerService#getWorkSpaceDir(PtImageAndAlgorithmVO) 
     */
    @Override
    public String getWorkSpaceDir(PtImageAndAlgorithmVO imageAndAlgorithmVO) {
        String codeDir = imageAndAlgorithmVO.getCodeDir();
        if (StringUtils.isBlank(codeDir)) {
            throw new BusinessException("算法路径为空");
        }
        String[] codeDirArray = imageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        return codeDirArray[codeDirArray.length - 1];
    }
}
