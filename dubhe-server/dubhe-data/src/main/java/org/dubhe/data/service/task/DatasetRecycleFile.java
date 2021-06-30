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
package org.dubhe.data.service.task;

import com.alibaba.fastjson.JSONObject;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.dao.DatasetLabelMapper;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.dao.FileMapper;
import org.dubhe.data.service.DatasetService;
import org.dubhe.data.service.DatasetVersionService;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.global.AbstractGlobalRecycle;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.dubhe.data.constant.Constant.LIMIT_NUMBER;

/**
 * @description 数据集文件删除类
 * @date 2020-10-09
 */
@RefreshScope
@Component(value = "datasetRecycleFile")
public class DatasetRecycleFile extends AbstractGlobalRecycle {

    @Value("${recycle.over-second.file}")
    private long overSecond;

    /**
     * 数据集文件服务 mapper
     */
    @Resource
    private FileMapper fileMapper;

    /**
     * 数据集版本文件 mapper
     */
    @Resource
    private DatasetVersionFileMapper datasetVersionFileMapper;

    /**
     * 数据集版本文件 mapper
     */
    @Resource
    private DatasetLabelMapper datasetLabelMapper;

    /**
     * 数据集 service
     */
    @Resource
    private DatasetService datasetService;

    /**
     * 数据集版本 service
     */
    @Resource
    private DatasetVersionService datasetVersionService;


    @Autowired
    private RecycleTool recycleTool;

    /**
     * 根据数据集Id删除数据文件
     *
     * @param detail 数据清理详情参数
     * @param dto    资源回收创建对象
     * @return true 继续执行,false 中断任务详情回收(本次无法执行完毕，创建新任务到下次执行)
     */
    @Override
    protected boolean clearDetail(RecycleDetailCreateDTO detail, RecycleCreateDTO dto) throws Exception {
        LogUtil.info(LogEnum.BIZ_DATASET, "DatasetRecycleFile.clear() , param:{}", JSONObject.toJSONString(detail));
        if (!Objects.isNull(detail.getRecycleCondition())) {
            //清理DB数据
            if (RecycleTypeEnum.TABLE_DATA.getCode().compareTo(detail.getRecycleType()) == 0) {
                Long datasetId = Long.valueOf(detail.getRecycleCondition());
                clearDataByDatasetId(datasetId, dto);
                //清理mino数据
            } else {
                String recycleCondition = detail.getRecycleCondition();
                recycleTool.delTempInvalidResources(recycleCondition);
            }


        }
        return true;
    }


    /**
     * 通过数据集ID删除数据集相关的DB数据
     *
     * @param datasetId 数据集ID
     * @param dto       资源回收创建对象
     */
    public void clearDataByDatasetId(Long datasetId, RecycleCreateDTO dto) throws Exception {
        initOverTime();
        //删除文件数据
        datasetService.deleteInfoById(datasetId);
        //删除版本数据
        datasetVersionService.deleteByDatasetId(datasetId);
        // 循环分批删除版本文件
        while (fileMapper.deleteByDatasetId(datasetId, LIMIT_NUMBER) > 0) {
            if (validateOverTime()) {
                //  延迟一秒
                TimeUnit.SECONDS.sleep(1);
            } else {
                // 超时添加新任务并中止任务
                LogUtil.warn(LogEnum.BIZ_DATASET, "DatasetRecycleFile.clear() 超时添加新任务并停止, param:{}", JSONObject.toJSONString(dto));
                if (!Objects.isNull(dto)) {
                    addNewRecycleTask(dto);
                }
                return;
            }
        }

        // 循环分批删除文件
        while (datasetVersionFileMapper.deleteBydatasetId(datasetId, LIMIT_NUMBER) > 0) {
            if (validateOverTime()) {
                //  延迟一秒
                TimeUnit.SECONDS.sleep(1);
            } else {
                // 超时添加新任务并中止任务
                LogUtil.warn(LogEnum.BIZ_DATASET, "DatasetRecycleFile.clear() 超时添加新任务并停止, param:{}", JSONObject.toJSONString(dto));
                if (!Objects.isNull(dto)) {
                    addNewRecycleTask(dto);
                }
                return;
            }
        }

        // 循环分批删除数据集标签
        while (datasetLabelMapper.deleteByDatasetId(datasetId) > 0) {
            if (validateOverTime()) {
                //  延迟一秒
                TimeUnit.SECONDS.sleep(1);
            } else {
                // 超时添加新任务并中止任务
                LogUtil.warn(LogEnum.BIZ_DATASET, "DatasetRecycleFile.clear() 超时添加新任务并停止, param:{}", JSONObject.toJSONString(dto));
                if (!Objects.isNull(dto)) {
                    addNewRecycleTask(dto);
                }
                return;
            }
        }
    }


    /**
     * 数据还原
     *
     * @param dto 数据清理参数
     */
    @Override
    protected void rollback(RecycleCreateDTO dto) {
        datasetService.allRollback(dto);
    }

    /**
     * 覆盖数据集文件删除超时时间
     * @return 自定义超时秒
     */
    @Override
    public long getRecycleOverSecond() {
        return overSecond;
    }
}
