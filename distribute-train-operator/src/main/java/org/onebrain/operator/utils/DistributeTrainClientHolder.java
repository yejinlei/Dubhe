/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

package org.onebrain.operator.utils;

import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.onebrain.operator.crd.DistributeTrain;
import org.onebrain.operator.crd.DistributeTrainList;
import org.onebrain.operator.crd.DoneableDistributeTrain;

/**
 * @description 分布式训练客户端持有器
 * @date 2020-09-23
 */
public class DistributeTrainClientHolder {

    private static MixedOperation<DistributeTrain, DistributeTrainList, DoneableDistributeTrain, Resource<DistributeTrain, DoneableDistributeTrain>> distributeTrainClient;

    public static MixedOperation<DistributeTrain, DistributeTrainList, DoneableDistributeTrain, Resource<DistributeTrain, DoneableDistributeTrain>> getClient(){
        return distributeTrainClient;
    }

    public static void setDistributeTrainClient(MixedOperation<DistributeTrain, DistributeTrainList, DoneableDistributeTrain, Resource<DistributeTrain, DoneableDistributeTrain>> client){
        distributeTrainClient = client;
    }
}
