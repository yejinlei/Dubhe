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

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.service.DatasetGroupLabelService;
import org.dubhe.data.service.LabelGroupService;
import org.dubhe.data.service.LabelService;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.global.AbstractGlobalRecycle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @description 数据集文件删除类
 * @date 2021-03-10
 */
@RefreshScope
@Component(value = "labelGroupRecycleFile")
public class LabelGroupRecycleFile extends AbstractGlobalRecycle {

    @Value("${recycle.over-second.file}")
    private long overSecond;


    /**
     * 标签组标签  Service
     */
    @Resource
    private DatasetGroupLabelService datasetGroupLabelService;

    /**
     * 标签 service
     */
    @Resource
    private LabelService labelService;


    /**
     * 标签组 service
     */
    @Resource
    private LabelGroupService labelGroupService;

    /**
     * 根据数据集Id删除数据文件
     *
     * @param detail 数据清理详情参数
     * @param dto 资源回收创建对象
     * @return true 继续执行,false 中断任务详情回收(本次无法执行完毕，创建新任务到下次执行)
     */
    @Override
    protected boolean clearDetail(RecycleDetailCreateDTO detail, RecycleCreateDTO dto) {
        LogUtil.info(LogEnum.BIZ_DATASET, "LabelGroupRecycleFile.clear() , param:{}", JSONObject.toJSONString(detail));
        if (!Objects.isNull(detail.getRecycleCondition()) && RecycleTypeEnum.TABLE_DATA.getCode().compareTo(detail.getRecycleType()) == 0) {
            //清理DB数据
            Long groupId = Long.valueOf(detail.getRecycleCondition());
            labelService.deleteByGroupId(groupId);
            datasetGroupLabelService.deleteByGroupId(groupId);
            labelGroupService.deleteByGroupId(groupId);
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
        List<RecycleDetailCreateDTO> detailList = dto.getDetailList();
        if (CollectionUtil.isNotEmpty(detailList)) {
            for (RecycleDetailCreateDTO recycleDetailCreateDTO : detailList) {
                if (!Objects.isNull(recycleDetailCreateDTO) &&
                        RecycleTypeEnum.TABLE_DATA.getCode().compareTo(recycleDetailCreateDTO.getRecycleType()) == 0) {
                    Long groupId = Long.valueOf(recycleDetailCreateDTO.getRecycleCondition());
                    labelService.updateStatusByGroupId(groupId, false);
                    datasetGroupLabelService.updateStatusByGroupId(groupId, false);
                    labelGroupService.updateStatusByGroupId(groupId, false);
                    return;
                }
            }
        }
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
