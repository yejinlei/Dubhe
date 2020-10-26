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

package org.dubhe.k8s.api;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.resource.BizDistributeTrain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @description NodeApiTest测试类
 * @date 2020-04-22
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DistributeTrainApiTest {
    @Resource
    private DistributeTrainApi distributeTrainApi;

    @Test
    public void testCreate(){
        DistributeTrainBO bo = new DistributeTrainBO();
        bo.setName("yepeng11");
        bo.setNamespace("yep");
        bo.setSize(3);
        bo.setImage("harbor.dubhe.ai/oneflow/oneflow-cuda:py36-v3");
        bo.setMasterCmd("export NODE_IPS=`cat /home/hostfile.json |jq -r '.[]|.ip'|paste -d \",\" -s` && cd /workspace/Classification/cnns && rm -rf core.* && rm -rf ./output/snapshots/* && python3 of_cnn_train_val.py --train_data_dir=$DATA_ROOT/train --train_data_part_num=$TRAIN_DATA_PART_NUM --val_data_dir=$DATA_ROOT/validation --val_data_part_num=$VAL_DATA_PART_NUM --num_nodes=$NODE_NUM --node_ips=\"$NODE_IPS\" --gpu_num_per_node=$GPU_NUM_PER_NODE --model_update=\"momentum\" --learning_rate=0.256 --loss_print_every_n_iter=1 --batch_size_per_device=1 --val_batch_size_per_device=1 --num_epoch=1 --model=\"resnet50\" --model_save_dir=/model");
        bo.setMemNum(8192);
        bo.setCpuNum(4000);
        bo.setGpuNum(2);
        bo.setSlaveCmd("export NODE_IPS=`cat /home/hostfile.json |jq -r '.[]|.ip'|paste -d \",\" -s` && cd /workspace/Classification/cnns && rm -rf core.* && rm -rf ./output/snapshots/* && python3 of_cnn_train_val.py --train_data_dir=$DATA_ROOT/train --train_data_part_num=$TRAIN_DATA_PART_NUM --val_data_dir=$DATA_ROOT/validation --val_data_part_num=$VAL_DATA_PART_NUM --num_nodes=$NODE_NUM --node_ips=\"$NODE_IPS\" --gpu_num_per_node=$GPU_NUM_PER_NODE --model_update=\"momentum\" --learning_rate=0.256 --loss_print_every_n_iter=1 --batch_size_per_device=1 --val_batch_size_per_device=1 --num_epoch=1 --model=\"resnet50\" --model_save_dir=/model");
        bo.setDatasetStoragePath("/nfs/sunjd/dataset/of_dataset");
        bo.setWorkspaceStoragePath("/nfs/sunjd/workspace");
        bo.setModelStoragePath("/nfs/sunjd/model");
        bo.setBusinessLabel("train");
        bo.setDelayCreateTime(10);
        bo.setDelayDeleteTime(10);

        distributeTrainApi.create(bo);
    }
    @Test
    public void deleteByResourceName() {
        PtBaseResult result = distributeTrainApi.deleteByResourceName("tianlong", "tianlong-dt");
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void create() {
        String filePath = "D:\\Devial\\之江实验室\\分布式训练\\image and demo\\resnet50\\demo-env.yaml";
        String ymlStr = FileUtil.readString(filePath,"utf-8");
        BizDistributeTrain result = distributeTrainApi.create(ymlStr);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void delete() {
        String filePath = "D:\\Devial\\之江实验室\\分布式训练\\image and demo\\resnet50\\demo-env.yaml";
        String ymlStr = FileUtil.readString(filePath,"utf-8");
        Boolean result = distributeTrainApi.delete(ymlStr);
        System.out.println(JSON.toJSONString(result));
    }
}