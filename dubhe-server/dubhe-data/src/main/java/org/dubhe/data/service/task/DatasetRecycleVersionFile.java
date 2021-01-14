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
import org.dubhe.data.constant.ErrorEnum;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.domain.entity.RecycleTask;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.AbstractGlobalRecycle;
import org.dubhe.utils.LogUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.dubhe.data.constant.Constant.LIMIT_NUMBER;

/**
 * @description 数据集版本文件删除类
 * @date 2020-10-09
 */
@Component(value = "datasetRecycleVersionFile")
public class DatasetRecycleVersionFile extends AbstractGlobalRecycle {

    /**
     * 数据集版本文件 mapper
     */
    @Resource
    private DatasetVersionFileMapper datasetVersionFileMapper;

    /**
     * 根据数据集Id删除数据文件
     *
     * @param object 数据回收实体
     */
    @Override
    public void clear(Object object) {
        LogUtil.info(LogEnum.BIZ_DATASET,"Class DatasetRecycleVersionFile execute clear method , param:{}", JSONObject.toJSONString(object));
        if(Objects.isNull(object)){
            return;
        }
        RecycleTask recycleTask = (RecycleTask) object;
        if(!Objects.isNull(recycleTask.getRecycleCondition())){
            Long datasetId = Long.valueOf(recycleTask.getRecycleCondition());
            //校验超时时间  && 延迟一秒 循环批量删除
            try {
                while (checkoutOverTime(recycleTask) && datasetVersionFileMapper.deleteBydatasetId(datasetId,LIMIT_NUMBER ) > 0) {
                    TimeUnit.SECONDS.sleep(1);
                }
            }catch (Exception e){
                LogUtil.info(LogEnum.BIZ_DATASET,"Class DatasetRecycleVersionFile execute clear method error , param:{} error:{}",
                        JSONObject.toJSONString(object), JSONObject.toJSONString(e));
                throw new BusinessException(ErrorEnum.DATASET_DELETE_ERROR);
            }



        }

    }
}
