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
package org.dubhe.tadl.utils;

import lombok.Data;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.tadl.constant.TadlConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class PathUtil {

    /**
     * tadl 实验下算法路径 "%s/TADL/experiment/%s/algorithm/TADL"
     * bucketName experimentId
     */
    @Value("${tadl.path.experiment.algorithm.algorithm}")
    private String experimentAlgorithmPath;

    /**
     * tadl 实验下算法yaml路径 "%s/TADL/experiment/%s/algorithm/yaml/"
     * bucketName experimentId
     */
    @Value("${tadl.path.experiment.algorithm.yaml}")
    private String experimentYamlPath;

    /**
     * tadl 实验下系统日志路径 "%s/TADL/experiment/%s/systemLogs/"
     * bucketName experimentId
     */
    @Value("${tadl.path.experiment.systemLogs}")
    private String experimentSystemLogsPath;

    /**
     * trial selectSpace 路径  "%s/TADL/experiment/%s/%s/%s/"
     * bucketName experimentId stageName trialId
     */
    @Value("${tadl.path.experiment.stage.trial.selectSpace}")
    private String trialSelectSpacePath;

    /**
     * trial result 路径 "%s/TADL/experiment/%s/%s/%s/result/"
     * bucketName experimentId stageName trialId
     */
    @Value("${tadl.path.experiment.stage.trial.result}")
    private String trialResultPath;

    /**
     * trial log 路径 "%s/TADL/experiment/%s/%s/%s/log/"
     * bucketName experimentId stageName trialId
     */
    @Value("${tadl.path.experiment.stage.trial.log}")
    private String trialLogPath;

    /**
     * stage searchSpace 路径 "%s/TADL/experiment/%s/"
     * bucketName experimentId
     */
    @Value("${tadl.path.experiment.stage.searchSpace}")
    private String stageSearchSpacePath;

    /**
     * stage bestSelectedSpace 路径 "%s/TADL/experiment/%s/"
     * bucketName experimentId
     */
    @Value("${tadl.path.experiment.stage.bestSelectedSpace}")
    private String stageBestSelectedSpacePath;

    /**
     * stage experimentConfig 路径 "%s/TADL/experiment/%s/"
     * bucketName experimentId
     */
    @Value("${tadl.path.experiment.stage.experimentConfig}")
    private String stageExperimentConfigPath;

    /**
     * stage bestSelectedSpace 路径 "%s/TADL/experiment/%s/"
     * bucketName experimentId
     */
    @Value("${tadl.path.experiment.stage.bestSelectedSpace}")
    private String bestSelectedSpacePath;

    /**
     * stage bestSelectedSpace 路径 "%s/TADL/experiment/%s/"
     * bucketName experimentId
     */
    @Value("${tadl.path.experiment.stage.bestCheckpoint}")
    private String bestCheckpointPath;

    /**
     * tadl 算法路径 "%s/TADL/algorithm/%s"
     * bucketName
     */
    @Value("${tadl.path.algorithm.algorithmPath}")
    private String algorithmPath;

    /**
     * tadl 实验路径 "%s/TADL/experiment/%s"
     * bucketName
     */
    @Value("${tadl.path.experiment.experimentPath}")
    private String experimentPath;

    /**
     * tadl 算法原版 yaml "%s/TADL/algorithm/%s/TADL/pytorch/%s/yaml/"
     * bucketName algorithmName algorithmName
     */
    @Value("${tadl.path.algorithm.parentYaml}")
    private String parentYaml;

    /**
     * tadl 算法 yaml "%s/TADL/algorithm/%s/yaml/%s"
     * bucketName algorithmName versionName
     */
    @Value("${tadl.path.algorithm.yaml}")
    private String yaml;
    /**
     * tadl 算法 TADL/pytorch/%s/yaml/
     * bucketName algorithmName versionName
     */
    @Value("${tadl.path.pytorch.algorithm.yaml}")
    private String pytorchAlgorithmYaml;
    /**
     * 获取  experimentAlgorithmPath
     *
     * @param bucketName   桶名称
     * @param experimentId 实验id
     * @return experimentAlgorithmPath
     */
    public String getExperimentAlgorithmPath(String bucketName, Long experimentId) {
        return String.format(experimentAlgorithmPath, bucketName, experimentId);
    }

    /**
     * 获取  experimentYamlPath
     *
     * @param bucketName   桶名称
     * @param experimentId 实验id
     * @return experimentYamlPath
     */
    public String getExperimentYamlPath(String bucketName, Long experimentId) {
        return String.format(experimentYamlPath, bucketName, experimentId);
    }

    /**
     * 获取 TrialResultPath
     *
     * @param bucketName   桶名称
     * @param experimentId 实验id
     * @param stageName    阶段id
     * @param trialId      trialId
     * @return trialResultPath
     */
    public String getTrialResultPath(String bucketName, Long experimentId, String stageName, Long trialId) {
        return String.format(trialResultPath, bucketName, experimentId, stageName, trialId);
    }

    /**
     * 获取 TrialLogPath
     *
     * @param bucketName   桶名称
     * @param experimentId 实验id
     * @param stageName    阶段id
     * @param trialId      trialId
     * @return trialLogPath
     */
    public String getTrialLogPath(String bucketName, Long experimentId, String stageName, Long trialId) {
        return String.format(trialLogPath, bucketName, experimentId, stageName, trialId);
    }

    /**
     * 获取 StageSearchSpacePath
     *
     * @param bucketName   桶名称
     * @param experimentId 实验id
     * @return stageSearchSpacePath
     */
    public String getStageSearchSpacePath(String bucketName, Long experimentId) {
        return String.format(stageSearchSpacePath, bucketName, experimentId);
    }

    /**
     * 获取 StageBestSelectedSpace
     *
     * @param bucketName   桶名称
     * @param experimentId 实验id
     * @return stageBestSelectedSpace
     */
    public String getBestSelectedSpacePath(String bucketName, Long experimentId) {
        return String.format(stageBestSelectedSpacePath, bucketName, experimentId);
    }

    /**
     * 获取 bestCheckpointPath
     *
     * @param bucketName   桶名称
     * @param experimentId 实验id
     * @return bestCheckpointPath
     */
    public String getBestCheckpointPath(String bucketName, Long experimentId) {
        return String.format(bestCheckpointPath, bucketName, experimentId);
    }

    /**
     * 获取 StageExperimentConfigPath
     *
     * @param bucketName   桶名称
     * @param experimentId 实验id
     * @return stageExperimentConfigPath
     */
    public String getStageExperimentConfigPath(String bucketName, Long experimentId) {
        return String.format(stageExperimentConfigPath, bucketName, experimentId);
    }

    /**
     * 获取  AlgorithmPath
     *
     * @param bucketName    桶名称
     * @param algorithmName 算法名称
     * @return algorithmPath
     */
    public String getAlgorithmPath(String bucketName, String algorithmName) {
        return String.format(algorithmPath, bucketName, algorithmName);
    }

    /**
     * 获取  experimentPath
     *
     * @param bucketName    桶名称
     * @param experimentId  实验ID
     * @return experimentPath
     */
    public String getExperimentPath(String bucketName, Long experimentId) {
        return String.format(experimentPath, bucketName, experimentId);
    }

    /**
     * 获取  AlgorithmYamlPath
     *
     * @param bucketName    桶名称
     * @param algorithmName 算法名称
     * @param versionName   版本名称
     * @return yaml
     */
    public String getYamlPath(String bucketName, String algorithmName, String versionName) {
        return String.format(yaml, bucketName, algorithmName, StringUtils.isBlank(versionName) ? StringUtils.EMPTY : versionName + TadlConstant.MODULE_URL_PREFIX);
    }

    /**
     * 获取pytorchAlgorithmYamlPath
     * @param algorithmName 算法名称
     * @return yamlPath
     */
    public String getPytorchAlgorithmYamlPath(String algorithmName){
        return String.format(pytorchAlgorithmYaml, algorithmName);
    }

}
