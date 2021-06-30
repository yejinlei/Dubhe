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

package org.dubhe.data.util;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.service.DataSequenceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @description 分标策略配置
 * @date 2020-09-21
 */
public class MyPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long>{

    @Autowired
    private DataSequenceService dataSequenceService;
    /**
     * 分段ID范围区间 50表示 50间隔ID存一张表
     */
    private long INTERVAL_NUMBER = 50;

    private static Set<String> tableNames = new HashSet<>();

    /**
     * 分表策略处理
     *
     * @param collection            分表集合
     * @param preciseShardingValue  分表健信息
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        long startIndex = 1;
        long endIndex = 50;
        String tableName = preciseShardingValue.getLogicTableName()+ "_" + preciseSharding(preciseShardingValue.getValue(),startIndex ,endIndex);
        if(!tableNames.contains(tableName)) {
            dataSequenceService =  SpringContextHolder.getBean(DataSequenceService.class);
            if(!dataSequenceService.checkTableExist(tableName)){
                try{
                    dataSequenceService.createTable(tableName);
                } catch (Exception e) {
                    LogUtil.info(LogEnum.DATA_SEQUENCE, "table name repeat {}", tableName);
                }
            }
            tableNames.add(tableName);
        }
        return tableName;
    }

    /**
     * 分表处理逻辑
     *
     * @param indexId     当前值
     * @param startIndex  开始值
     * @param endIndex    结束值
     * @return
     */
    public long preciseSharding(long indexId,long startIndex , long endIndex){
        if(indexId > endIndex){
            startIndex = startIndex + INTERVAL_NUMBER;
            endIndex = endIndex + INTERVAL_NUMBER;
            return preciseSharding(indexId,startIndex,endIndex);
        }
        return endIndex / INTERVAL_NUMBER;
    }
}
