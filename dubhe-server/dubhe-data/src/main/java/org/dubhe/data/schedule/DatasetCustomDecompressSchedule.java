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

package org.dubhe.data.schedule;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.emc.ecs.nfsclient.nfs.io.Nfs3File;
import org.dubhe.data.constant.DatasetDecompressStateEnum;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.vo.DatasetQueryDTO;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.DatasetService;
import org.dubhe.utils.NfsUtil;
import org.dubhe.utils.WrapperHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import static org.dubhe.data.constant.Constant.*;

/**
 * @description
 * @date 2020-07-28
 */
@Component
public class DatasetCustomDecompressSchedule {

    @Autowired
    private DatasetService datasetService;
    @Autowired
    private DatasetMapper datasetMapper;

    /**
     * 线程池
     */
    @Autowired
    private BasePool pool;
    /**
     * nfs操作工具类
     */
    @Autowired
    private NfsUtil nfsUtil;

    @Value("${k8s.nfs-root-path}")
    private String nfsRootPath;

    @Value("${minio.bucketName}")
    private String bucket;

    /**
     * nfs服务暴露的IP地址
     */
    @Value("${k8s.nfs}")
    private String nfsIp;

    /**
     * 文件存储服务器用户名
     */
    @Value("${data.server.userName}")
    private String userName;

    /**
     * 需要解压的状态
     */
    private static final DatasetQueryDTO NEED_DECOMPRESS_QUERY = DatasetQueryDTO.builder()
            .decompressState(DatasetDecompressStateEnum.NOT_DECOMPRESSED.getValue()).build();

    @Scheduled(cron = "*/20 * * * * ?")
    public void datasetCustomDecompress() {
        QueryWrapper<Dataset> datasetQueryWrapper = WrapperHelp.getWrapper(NEED_DECOMPRESS_QUERY);
        datasetQueryWrapper.eq("is_import", 1);
        List<Dataset> datasets = datasetService.queryList(datasetQueryWrapper);
        if (CollectionUtil.isNotEmpty(datasets)) {
            datasets.stream().forEach(dataset -> {
                if (datasetMapper.updateDecompressState(dataset.getId(), DatasetDecompressStateEnum.NOT_DECOMPRESSED.getValue()
                        , DatasetDecompressStateEnum.DECOMPRESSING.getValue()) > 0) {
                    pool.getExecutor().submit(() -> datasetCustomDecompress(dataset));
                }
            });
        }
    }

    /**
     * 数据集压缩包解压
     *
     * @param dataset 数据集
     */
    void datasetCustomDecompress(Dataset dataset) {
        String targetPath = bucket + DATASET_PATH_NAME + dataset.getId() + VERSION_PATH_NAME + DEFAULT_VERSION;
        Boolean flag = nfsUtil.createDirs(false, File.separator + targetPath);
        if (flag) {
            String[] cmd = {"/bin/bash", "-c", String.format("ssh %s@%s \"unzip %s -d %s \"", userName, nfsIp, nfsRootPath + dataset.getArchiveUrl(), nfsRootPath + targetPath)};
            try {
                Runtime.getRuntime().exec(cmd);
                flag = true;
            } catch (Exception e) {
                flag = false;
                dataset.setDecompressFailReason("解压失败!");
            }
        }
        if (flag) {
            try {
                datasetService.initVersion(dataset);
                return;
            } catch (Exception e) {
                dataset.setDecompressFailReason("数据集版本初始化失败!");
            }
        }
        dataset.setDecompressState(DatasetDecompressStateEnum.DECOMPRESS_FAIL.getValue());
        datasetMapper.updateById(dataset);
    }

}
