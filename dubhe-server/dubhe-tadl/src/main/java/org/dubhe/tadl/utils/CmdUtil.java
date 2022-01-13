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

import cn.hutool.core.util.ObjectUtil;
import com.google.common.base.CaseFormat;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.tadl.config.TadlJobConfig;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.domain.entity.Algorithm;
import org.dubhe.tadl.domain.entity.AlgorithmStage;
import org.dubhe.tadl.enums.StageEnum;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description cmd 生成工具
 * @date 2020-03-22
 */
@Component
@Slf4j
public class CmdUtil {

    @Autowired
    private TadlJobConfig tadlJobConfig;

    /**
     * minIO桶名
     */
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 路径工具类
     */
    @Autowired
    private PathUtil pathUtil;

    /**
     * MinIO工具类
     */
    @Autowired
    private MinioUtil minioUtil;

    /**
     * 获取个阶段的算法启动命令
     *
     * @param stageEnum 算法阶段枚举
     * @param yaml      yaml
     * @param trialId   trial id
     * @return 命令
     */
    @SuppressWarnings("unchecked")
    public String getStartAlgCmd(StageEnum stageEnum,
                                 String yaml,
                                 AlgorithmStage algorithmStage,
                                 Integer trialId,
                                 Algorithm algorithm,
                                 Long experimentId){
        String executeScriptPath = TadlConstant.EXECUTE_SCRIPT_PATH + File.separator + algorithm.getName();
        //cd
        StringBuilder cmd = new StringBuilder("cd" + StringUtils.SPACE + tadlJobConfig.getDockerExperimentPath() + executeScriptPath + StringUtils.SPACE);
        //&&
        cmd.append(StringUtils.SPACE).append(TadlConstant.AND).append(StringUtils.SPACE);
        //python start script
        cmd.append(StringUtils.SPACE).append(algorithmStage.getPythonVersion()).append(StringUtils.SPACE).append(algorithmStage.getExecuteScript()).append(StringUtils.SPACE);

        //优先获取python脚本文件，若存在则按照脚本解析，否则按照默认方法解析
        try {
            //读取文件服务器中的python脚本内容
            String fileStr = getExperimentPythonFromMinIO(experimentId);
            if (!StringUtils.isEmpty(fileStr)){
                //本地创建文件
                File file=new File(TadlConstant.ALGORITHM_TRANSFORM_FILE_NAME);
                //若已存在文件，则删除原文件，再创建新的文件
                if (file.exists()){
                    file.delete();
                }
                file.createNewFile();
                //将文件服务器上的内容写入文件中
                Writer w=new FileWriter(file);
                w.write(fileStr);
                w.close();
                return this.getCmdFromPython(stageEnum,yaml,trialId,cmd.toString());
            }else {
                return this.getCmdFromDefault(stageEnum,yaml,trialId,cmd);
            }
        }catch (Exception e){
            LogUtil.error(LogEnum.TADL, "读取文件服务器中的python脚本内容失败", e);
            return this.getCmdFromDefault(stageEnum,yaml,trialId,cmd);
        }
    }

    /**
     * 获取minio 中实验的 python脚本
     * @param experimentId
     * @return python脚本内容
     */
    private String getExperimentPythonFromMinIO(Long experimentId){
        try {
            return minioUtil.readString(
                    bucketName,
                    pathUtil.getExperimentYamlPath(
                            StringUtils.EMPTY,
                            experimentId
                    ) + TadlConstant.ALGORITHM_TRANSFORM_FILE_NAME
            );
        } catch (Exception e) {
            LogUtil.error(LogEnum.TADL, "获取minio 中实验的 python脚本失败", e);
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }

    /**
     * 使用python脚本获取命令行
     * @param stageEnum
     * @param yaml
     * @param trialId
     * @param cmd
     * @return 命令行
     */
    private String getCmdFromPython(StageEnum stageEnum,
                                    String yaml,
                                    Integer trialId,
                                    String cmd){
        String pythonCmd = null;
        //存放调用参数，参数顺序为：
        //yaml文件字符串  命令行字符串  docker_experiment_path   docker_dataset_path  stage_name  trial_id
        List<String> paramList = new ArrayList<String>();
        paramList.add(TadlConstant.ALGORITHM_TRANSFORM_FILE_NAME);
        paramList.add(yaml);
        paramList.add(cmd);
        paramList.add(tadlJobConfig.getDockerExperimentPath());
        paramList.add(tadlJobConfig.getDockerDatasetPath());
        paramList.add(stageEnum.getName());
        paramList.add(trialId.toString());

        try{
            String[] array = paramList.toArray(new String[0]);
            // 执行py脚本并传参
            Process proc = Runtime.getRuntime().exec(array);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            //从python脚本的输入输出流来获取本来该打印在控制台的结果作为命令行
            while ((line = in.readLine()) != null) {
                LogUtil.info(LogEnum.TADL,"python print : " + line);
                pythonCmd = line;
            }
            in.close();
            proc.waitFor();
        }catch (Exception e){
            LogUtil.error(LogEnum.TADL, "获取minio 中实验的 python脚本失败", e);
            throw new BusinessException(TadlErrorEnum.CMD_FORM_ERROR);
        }

        if (StringUtil.isNullOrEmpty(pythonCmd)){
            throw new BusinessException(TadlErrorEnum.CMD_FORM_ERROR);
        }
        return pythonCmd;
    }

    /**
     * 使用默认规则获取命令行
     * @param stageEnum
     * @param yaml
     * @param trialId
     * @param cmd
     * @return 命令行
     */
    @SuppressWarnings("unchecked")
    private String getCmdFromDefault(StageEnum stageEnum,
                                     String yaml,
                                     Integer trialId,
                                     StringBuilder cmd){
        Map<String, Object> runParameterMap;
        Map<String, Object> fullYaml = YamlParseUtil.YamlParse(yaml);

        if (ObjectUtil.isNull(fullYaml)){
            throw new BusinessException(TadlErrorEnum.CMD_FORM_ERROR);
        }

        //获取到yaml中run_parameter部分参数
        Object runParameterObject = fullYaml.get(TadlConstant.RUN_PARAMETER);
        if (ObjectUtil.isNull(runParameterObject)){
            return cmd.toString();
        }

        //转化为Map<String, Object>的数据形式
        if (runParameterObject instanceof Map){
            runParameterMap = (Map<String, Object>)runParameterObject;
        } else {
            throw new BusinessException(TadlErrorEnum.CMD_FORM_ERROR);
        }


        //--param 根据约定进行路径与参数替换
        for (String key : runParameterMap.keySet()) {
            switch (key) {
                case TadlConstant.MODEL_SELECTED_SPACE_PATH_STRING:
                    runParameterMap.put(key, tadlJobConfig.getDockerExperimentPath() + File.separator + stageEnum.getName() + File.separator + trialId + TadlConstant.MODEL_SELECTED_SPACE_PATH);
                    break;
                case TadlConstant.RESULT_PATH_STRING:
                    runParameterMap.put(key, tadlJobConfig.getDockerExperimentPath() + File.separator + stageEnum.getName() + File.separator + trialId + TadlConstant.RESULT_PATH);
                    break;
                case TadlConstant.LOG_PATH_STRING:
                    runParameterMap.put(key, tadlJobConfig.getDockerExperimentPath() + File.separator + stageEnum.getName() + File.separator + trialId + TadlConstant.LOG_PATH);
                    break;
                case TadlConstant.BEST_CHECKPOINT_DIR_STRING:
                    runParameterMap.put(key, tadlJobConfig.getDockerExperimentPath() + TadlConstant.BEST_CHECKPOINT_DIR);
                    break;
                case TadlConstant.EXPERIMENT_DIR_STRING:
                    runParameterMap.put(key, tadlJobConfig.getDockerExperimentPath());
                    break;
                case TadlConstant.SEARCH_SPACE_PATH_STRING:
                    runParameterMap.put(key, tadlJobConfig.getDockerExperimentPath() + TadlConstant.SEARCH_SPACE_PATH);
                    break;
                case TadlConstant.BEST_SELECTED_SPACE_PATH_STRING:
                    runParameterMap.put(key, tadlJobConfig.getDockerExperimentPath() + TadlConstant.BEST_SELECTED_SPACE_PATH);
                    break;
                case TadlConstant.DATA_DIR_STRING:
                    runParameterMap.put(key, tadlJobConfig.getDockerDatasetPath());
                    break;
                case TadlConstant.TRIAL_ID_STRING:
                    runParameterMap.put(key, trialId);
                    break;
                default:
                    break;
            }
            cmd.append(TadlConstant.PARAM_SYMBOL).append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key)).append(SymbolConstant.SPACE).append(runParameterMap.get(key)).append(StringUtils.SPACE);
        }

        return cmd.toString();
    }
}
