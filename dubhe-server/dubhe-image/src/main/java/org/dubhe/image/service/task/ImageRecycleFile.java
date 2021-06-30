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
package org.dubhe.image.service.task;

import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.constant.HarborProperties;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.harbor.api.HarborApi;
import org.dubhe.image.service.PtImageService;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.global.AbstractGlobalRecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @description 镜像资源回收/还原
 * @date 2021-03-23
 */
@RefreshScope
@Component(value = "imageRecycleFile")
public class ImageRecycleFile extends AbstractGlobalRecycle {

    @Autowired
    private HarborApi harborApi;

    @Autowired
    private HarborProperties harborProperties;

    @Autowired
    private PtImageService ptImageService;

    /**
     * 自定义回收镜像实现
     *
     * @param detail 数据清理详情参数
     * @param dto 资源回收创建对象
     * @return true 继续执行,false 中断任务详情回收(本次无法执行完毕，创建新任务到下次执行)
     */
    @Override
    protected boolean clearDetail(RecycleDetailCreateDTO detail, RecycleCreateDTO dto) throws Exception {
        LogUtil.info(LogEnum.IMAGE, "image custom recycle file,params:{}", detail);
        if (StrUtil.isNotBlank(detail.getRecycleCondition())) {
            String imageUrl = harborProperties.getAddress() + StrUtil.SLASH + detail.getRecycleCondition();
            LogUtil.info(LogEnum.IMAGE, "delete harbor image url:{}", imageUrl);
            //同步删除harbor镜像
            harborApi.deleteImageByTag(imageUrl);
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
        ptImageService.recycleRollback(dto);
    }
}
