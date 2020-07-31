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

package org.dubhe.service.storage;

import org.dubhe.dao.PtModelBranchMapper;
import org.dubhe.domain.PtModelBranch;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.NfsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @description 异步文件拷贝
 * @date 2020-06-23
 */
@Component
public class AsyncStorage {

    @Autowired
    private NfsUtil nfsUtil;

    /**
     * 文件拷贝
     *
     * @param sourcePath            来源
     * @param destPath              目的
     * @param ptModelBranchMapper   数据库
     * @param ptModelBranch         模型版本信息
     */
    @Async(value="taskRunner")
    public void copyFileAsync(String sourcePath, String destPath, PtModelBranchMapper ptModelBranchMapper, PtModelBranch ptModelBranch) {
        LogUtil.info(LogEnum.BIZ_MODEL, "开始拷贝文件从{}到{}", sourcePath, destPath);
        Boolean nfsCopy = nfsUtil.copyPath(sourcePath, destPath);

        if (!nfsCopy) {
            LogUtil.info(LogEnum.BIZ_MODEL, "文件拷贝失败");
            ptModelBranch.setStatus(2);
        }
        else {
            LogUtil.info(LogEnum.BIZ_MODEL, "文件拷贝成功");
            ptModelBranch.setStatus(1);
        }

        if (ptModelBranchMapper.updateById(ptModelBranch) < 1) {
            throw new BusinessException("版本未保存成功");
        }
    }
}