package org.dubhe.tadl; /**
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

import org.dubhe.tadl.domain.dto.TrialRunParamDTO;
import org.dubhe.tadl.domain.dto.TrialStopParamDTO;
import org.dubhe.tadl.task.TrialJobAsyncTask;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @description
 * @date 2021-04-01
 */
@SpringBootTest
public class TrialJobAsyncTaskTest {

    @Resource
    private TrialJobAsyncTask trialJobAsyncTask;

    @Test
    public void runTrialTest(){
        TrialRunParamDTO trialRunParam = new TrialRunParamDTO();
        trialRunParam.setNamespace("namespace-1");
        trialRunParam.setCpuNum(2048);
        trialRunParam.setGpuNum(1);
        trialRunParam.setMemNum(8000);
        trialRunParam.setResourcesPoolType(1);
        trialRunParam.setExperimentId(1L);
        trialRunParam.setTrialId(1L);
        trialRunParam.setStageId(1L);
        trialRunParam.setAlgorithmPath("/org/dubhe/tadl/experiment/1/darts_tadl");
        trialRunParam.setDatasetPath("/org/dubhe/tadl/dataset/");
        trialRunParam.setTrialPath("/org/dubhe/tadl/experiment/1/");
        trialRunParam.setLogPath("/org/dubhe/tadl/experiment/1/log");
        trialRunParam.setCommand("cd ./algorithm/pytorch/darts && python darts_train.py --data_dir='/dataset/' --result_path='/trial/train/1/result/model_result.json' --search_space_path='/trial/train/1/result/search_space.json' --best_selected_space_path='/trial/train/1/result/best_selected_space.json' --log_path='/trial/train/1/log'");
//        trialRunParam.setCommand("cd ./algorithm/pytorch/darts && python darts_select.py --best_selected_space_path='/trial/train/1/result/best_selected_space.json'");
//        trialRunParam.setCommand("cd ./algorithm/pytorch/darts && python darts_retrain.py --data_dir='/dataset/' --result_path='/trial/retrain/1/result/model_result.json' --best_selected_space_path='/trial/train/1/result/best_selected_space.json' --best_checkpoint_dir='/trial/retrain/1/result/' --log_path='/trial/retrain/1/log'");
      //  trialJobAsyncTask.runTrial(trialRunParam);
    }

//    @Test
//    public void stopTrialTest(){
//        TrialStopParamDTO trialStopParamDTO = new TrialStopParamDTO();
//        trialStopParamDTO.setExperimentId(1L);
//        trialStopParamDTO.setTrialId(1L);
//        trialStopParamDTO.setNamespace("namespace-1");
//        trialJobAsyncTask.stopTrail(trialStopParamDTO);
//    }

}
