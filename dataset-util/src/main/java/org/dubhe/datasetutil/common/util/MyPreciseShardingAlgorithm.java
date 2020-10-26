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
package org.dubhe.datasetutil.common.util;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.service.DataSequenceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * @description 数据分片
 * @date 2020-09-21
 */
public class MyPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long>{

    @Autowired
    private DataSequenceService dataSequenceService;

    /**
     * 数据表分片
     *
     * @param collection           集合
     * @param preciseShardingValue 分片值
     * @return 字符串
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        long startIndex = MagicNumConstant.ONE;
        long endIndex = MagicNumConstant.FIFTY;
        dataSequenceService =  SpringContextHolder.getBean(DataSequenceService.class);
        String tableName = preciseShardingValue.getLogicTableName()+ BusinessConstant.UNDERLINE + preciseSharding(preciseShardingValue.getValue(),startIndex ,endIndex);
        if(!dataSequenceService.checkTableExist(tableName)){
            dataSequenceService.createTable(tableName);
        }
        return tableName;
    }

    /**
     * 分片实现
     *
     * @param indexId    起始位置
     * @param startIndex 起始值
     * @param endIndex   结束值
     * @return long 返回截止值
     */
    public long preciseSharding(long indexId,long startIndex , long endIndex){
        if(indexId > endIndex){
            startIndex = startIndex + BusinessConstant.INTERVAL_NUMBER;
            endIndex = endIndex + BusinessConstant.INTERVAL_NUMBER;
            return preciseSharding(indexId,startIndex,endIndex);
        }
        return endIndex / BusinessConstant.INTERVAL_NUMBER;
    }
}
