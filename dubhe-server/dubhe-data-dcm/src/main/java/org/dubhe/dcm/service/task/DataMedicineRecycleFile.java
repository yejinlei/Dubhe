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
package org.dubhe.dcm.service.task;

import com.alibaba.fastjson.JSONObject;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.dao.DatasetLabelMapper;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.dao.FileMapper;
import org.dubhe.data.service.DatasetService;
import org.dubhe.dcm.service.DataMedicineFileService;
import org.dubhe.dcm.service.DataMedicineService;
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
 * @date 2021-03-03
 */
@RefreshScope
@Component(value = "dataMedicineRecycleFile")
public class DataMedicineRecycleFile extends AbstractGlobalRecycle {

    @Value("${recycle.over-second.file}")
    private long overSecond;


    /**
     * 数据集 service
     */
    @Resource
    private DataMedicineService dataMedicineService;

    /**
     * 数据集 service
     */
    @Resource
    private DataMedicineFileService dataMedicineFileService;


    @Autowired
    private RecycleTool recycleTool;

    /**
     * 根据数据集Id删除数据文件
     *
     * @param detail 数据清理详情参数
     * @param dto 资源回收创建对象
     * @return true 继续执行,false 中断任务详情回收(本次无法执行完毕，创建新任务到下次执行)
     */
    @Override
    protected boolean clearDetail(RecycleDetailCreateDTO detail, RecycleCreateDTO dto) {
        LogUtil.info(LogEnum.BIZ_DATASET, "DataMedicineRecycleFile.clear() , param:{}", JSONObject.toJSONString(detail));
        if (!Objects.isNull(detail.getRecycleCondition()) && RecycleTypeEnum.TABLE_DATA.getCode().compareTo(detail.getRecycleType()) == 0) {
            //清理DB数据
            Long datasetId = Long.valueOf(detail.getRecycleCondition());
            dataMedicineService.deleteByDatasetId(datasetId);
            dataMedicineFileService.deleteByDatasetId(datasetId);
        }else {
            //清理mino数据
            String recycleCondition = detail.getRecycleCondition();
            recycleTool.delTempInvalidResources(recycleCondition);
        }
        return true;
    }


    /**
     * 数据还原
     *
     * @param dto 数据清理参数
     */
    @Override
    protected void rollback(RecycleCreateDTO dto) {
        dataMedicineService.allRollback(dto);
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
