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
package org.dubhe.datasetutil.handle;

import com.xiaoleilu.hutool.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.common.util.DateUtil;
import org.dubhe.datasetutil.common.util.GeneratorKeyUtil;
import org.dubhe.datasetutil.common.util.MinioUtil;
import org.dubhe.datasetutil.common.util.ThreadUtils;
import org.dubhe.datasetutil.domain.entity.DataFile;
import org.dubhe.datasetutil.domain.dto.DataVersionFile;
import org.dubhe.datasetutil.domain.entity.Dataset;
import org.dubhe.datasetutil.service.DataFileService;
import org.dubhe.datasetutil.service.DataVersionFileService;
import org.dubhe.datasetutil.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description 上传图片工具类
 * @date 2020-09-17
 */
@Slf4j
@Component
public class DatasetImageUploadHandle {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private DataFileService dataFileService;

    @Autowired
    private DataVersionFileService dataVersionFileService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private GeneratorKeyUtil generatorKeyUtil;

    /**
     * 启动线程
     *
     * @param imagePath 本地文件地址
     * @param datasetId 数据集Id
     */
    public void execute(String imagePath, Long datasetId) throws Exception {
        log.info("#-------------开始处理,时间[" + DateUtil.getNowStr() + "]-------------#");
        List<String> fileNames = FileUtil.listFileNames(imagePath);
        log.info("#-------------文件数量[" + fileNames.size() + "]------------------------");
        String fileBaseDir = BusinessConstant.MINIO_ROOT_PATH + BusinessConstant.FILE_SEPARATOR + datasetId
                + BusinessConstant.FILE_SEPARATOR + BusinessConstant.IMAGE_ORIGIN + BusinessConstant.FILE_SEPARATOR;
        List<Callable<Integer>> partitions = new ArrayList<>();
        int oneSize = ThreadUtils.createThread(fileNames.size());
        List<String> need = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (String fileName : fileNames) {
            need.add(fileName);
            if (need.size() == oneSize || atomicInteger.intValue() == fileNames.size() - 1) {
                List<String> now = new ArrayList<>(need);
                need.clear();
                partitions.add(() -> run(datasetId, now, fileBaseDir, imagePath));
            }
            atomicInteger.getAndIncrement();
        }
        ThreadUtils.runMultiThread(partitions);
    }

    /**
     * 插入数据库数据
     *
     * @param datasetId   数据集Id
     * @param fileNames   文件Name
     * @param fileBaseDir 文件路径
     * @param imagePath   文件地址
     * @return java.lang.Integer 成功数量
     */
    public Integer run(Long datasetId, List<String> fileNames, String fileBaseDir, String imagePath) {
        Integer success = 0;
        Dataset dataset = datasetService.findCreateUserIdById(datasetId);
        List<DataFile> dataFiles = new ArrayList<>();
        List<DataVersionFile> dataVersionFiles = new ArrayList<>();
        for (int i = 0; i < fileNames.size(); i++) {
            try {
                minioUtil.upLoadFile(fileBaseDir + fileNames.get(i), FileUtil.getInputStream(imagePath + BusinessConstant.FILE_SEPARATOR  + fileNames.get(i)));
                BufferedImage read = ImageIO.read(new File(imagePath + BusinessConstant.FILE_SEPARATOR  + fileNames.get(i)));
                success++;
                dataFiles.add(new DataFile(fileNames.get(i), datasetId, minioUtil.getUrl(fileBaseDir + fileNames.get(i)), dataset.getCreateUserId(), read));

                if (dataFiles.size() % 500 == 0 || i == fileNames.size() - 1) {
                    long startDataFileIndex = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_FILE, dataFiles.size());
                    for (DataFile dataFileEntity : dataFiles) {
                        dataFileEntity.setId(startDataFileIndex++);
                    }

                    dataFileService.saveBatchDataFile(dataFiles);
                    for (DataFile file : dataFiles) {
                        dataVersionFiles.add(new DataVersionFile(datasetId, file.getId(), 101, 0));
                    }
                    long startDataFileVersionIndex = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_VERSION_FILE, dataVersionFiles.size());
                    for (DataVersionFile dataVersionFile : dataVersionFiles) {
                        dataVersionFile.setId(startDataFileVersionIndex++);
                    }
                    dataVersionFileService.saveBatchDataFileVersion(dataVersionFiles);
                    dataVersionFiles.clear();
                    dataFiles.clear();
                }
            } catch (Exception e) {
                log.error("{}", e);
            }
        }
        return success;
    }

}
