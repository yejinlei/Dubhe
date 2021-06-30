/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed un   der the Apache License, Version 2.0 (the "License");
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

package org.dubhe.data.domain.bo;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.util.FileUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description 增强任务拆分BO
 * @date 2020-06-28
 */
@Data
public class EnhanceTaskSplitBO implements Serializable {

    /**
     * 增强后图片存放位置
     */
    private String enhanceFilePath;

    /**
     * 增强后图片标注文件存放位置
     */
    private String enhanceAnnotationPath;

    /**
     * 任务优先级
     */
    private Integer priority;

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务类型
     */
    private Integer type;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 数据集版本
     */
    private String versionName;

    /**
     * 待增强图片信息
     */
    private List<DatasetFileBO> fileDtos;

    /**
     * 任务执行人
     */
    private Long userId;

    /**
     * 发送时间
     */
    private Long sendTime;

    /**
     * 任务ID
     */
    private String reTaskId;

    public EnhanceTaskSplitBO() {
    }

    public EnhanceTaskSplitBO(Long taskId, List<File> files, Long datasetId, String versionName, Map<Long, DatasetVersionFile> datasetVersionFilesMap, Integer enhanceType,FileUtil fileUtil) {

        this.enhanceFilePath = fileUtil.getOriginFileAbsPath(datasetId,null,false);
        this.enhanceAnnotationPath = fileUtil.getNfsWriteAnnotationAbsPath(datasetId);
        this.priority = Constant.DEFAULT_PRIORITY;
        this.id = taskId;
        this.datasetId = datasetId;
        this.versionName = versionName;
        this.type = enhanceType;
        List<DatasetFileBO> fileDtos = new ArrayList<>();
        files.stream().forEach(file -> {
            fileDtos.add(new DatasetFileBO(
                    fileUtil.getOriginFileAbsPath(file.getUrl()),
                    fileUtil.getNfsReadAnnotationAbsPath(datasetId,file.getName(),versionName,datasetVersionFilesMap.get(file.getId()).getChanged()== NumberConstant.NUMBER_0),
                    file.getId(),
                    datasetVersionFilesMap.get(file.getId()).getAnnotationStatus(),
                    file.getWidth(),
                    file.getHeight()));
        });
        this.fileDtos = fileDtos;
        if (ObjectUtil.isNotNull(JwtUtils.getCurUserId())) {
            this.setUserId(JwtUtils.getCurUserId());
        }
    }

    /**
     * 获取增强后文件路径
     *
     * @param suffix        文件后缀
     * @param datasetFileBO 数据集文件
     * @return String       增强后文件路径
     */
    public String createEnhanceFilePath(String suffix, DatasetFileBO datasetFileBO) {
        String filePath = datasetFileBO.getFilePath();
        String fileFullName = filePath.substring(filePath.lastIndexOf(java.io.File.separator) + MagicNumConstant.ONE, filePath.length());
        String fileName = fileFullName.substring(0, fileFullName.lastIndexOf("."));
        String fileSuffix = fileFullName.substring(fileFullName.lastIndexOf("."), fileFullName.length());
        return enhanceFilePath + java.io.File.separator + fileName + suffix + fileSuffix;
    }

}
