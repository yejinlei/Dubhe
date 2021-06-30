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


import lombok.extern.slf4j.Slf4j;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.config.MinioConfig;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.*;

/**
 * @description 线程工具
 * @date 2020-10-19
 */
@Slf4j
public class ThreadUtils {

    private ThreadUtils() {
    }

    /**
     * 根据需要处理的数量创建线程数
     *
     * @param listSize 集合数量
     * @return int 数量
     */
    public static int createThread(int listSize) {
        return listSize / getNeedThreadNumber() == MagicNumConstant.ZERO ? MagicNumConstant.ONE : listSize / getNeedThreadNumber();
    }


    /**
     * 获取需要创建的线程数
     *
     * @return int 数量
     */
    public static int getNeedThreadNumber() {
        final int numOfCores = Runtime.getRuntime().availableProcessors();
        MinioConfig minioConfig = (MinioConfig) SpringContextHolder.getBean("minioConfig");
        final double blockingCoefficient = minioConfig.getBlockingCoefficient();
        return (int) (numOfCores / (MagicNumConstant.ONE - blockingCoefficient));
    }

    /**
     * 按要求分多线程执行
     *
     * @param partitions  分线程集合
     * @throws Exception 线程执行异常
     */
    public static void runMultiThread(List<Callable<Integer>> partitions) throws Exception {
        final ExecutorService executorService = Executors.newFixedThreadPool(ThreadUtils.getNeedThreadNumber());
        final List<Future<Integer>> valueOfStocks = executorService.invokeAll(partitions);
        Integer endCount = MagicNumConstant.ZERO;
        for (final Future<Integer> value : valueOfStocks) {
            endCount += value.get();
        }
        executorService.shutdown();
        Thread.sleep(1000);
    }

}
