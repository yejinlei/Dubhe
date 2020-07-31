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
package org.dubhe.task;

import org.dubhe.aspect.LogAspect;
import org.dubhe.domain.dto.BaseTrainJobDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.PtTrainJob;
import org.dubhe.domain.vo.PtImageAndAlgorithmVO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * @description 事务提交后触发异步操作
 * @date 2020-07-17
 */
@Component
public class TransactionAsyncManager {

    @Autowired
    private TrainJobAsyncTask trainJobAsyncTask;

    @Resource(name = "trainJobAsyncExecutor")
    private Executor trainJobAsyncExecutor;


    public void execute(BaseTrainJobDTO baseTrainJobDTO, UserDTO currentUser, PtImageAndAlgorithmVO ptImageAndAlgorithmVO, PtTrainJob ptTrainJob) {

        String traceId = MDC.get(LogAspect.TRACE_ID);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

            @Override
            public void afterCommit() {
                trainJobAsyncExecutor.execute(
                        () -> {
                            MDC.put(LogAspect.TRACE_ID, traceId);
                            trainJobAsyncTask.doJob(baseTrainJobDTO, currentUser, ptImageAndAlgorithmVO, ptTrainJob);
                            MDC.remove(LogAspect.TRACE_ID);
                        }
                );
            }
        });
    }
}
