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

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.constant.TrainJobConstant;
import org.dubhe.dao.PtTrainJobMapper;
import org.dubhe.domain.dto.BaseTrainJobDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.PtTrainJob;
import org.dubhe.domain.vo.PtImageAndAlgorithmVO;
import org.dubhe.enums.BizEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.ResourcesPoolTypeEnum;
import org.dubhe.enums.TrainJobStatusEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.NfsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @description 提交训练任务
 * @date 2020-07-17
 */

@Component
public class TrainJobAsyncTask {

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private NamespaceApi namespaceApi;

    @Autowired
    private TrainJobConstant trainJobConstant;

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private PtTrainJobMapper ptTrainJobMapper;

    @Autowired
    private TrainJobApi trainJobApi;

    /**
     * 提交job
     *
     * @param baseTrainJobDTO       训练任务信息
     * @param currentUser           用户
     * @param ptImageAndAlgorithmVO 镜像和算法信息
     */
    public void doJob(BaseTrainJobDTO baseTrainJobDTO, UserDTO currentUser, PtImageAndAlgorithmVO ptImageAndAlgorithmVO, PtTrainJob ptTrainJob) {
        PtJupyterJobBO jobBo = null;
        String k8sJobName = "";
        boolean flag = false;
        try {
            //判断是否存在相应的namespace,如果没有则创建
            String namespace = getNamespace(currentUser);

            //封装PtJupyterJobBO对象,调用创建训练任务接口
            jobBo = pkgPtJupyterJobBo(baseTrainJobDTO, currentUser, ptImageAndAlgorithmVO, namespace);
            if (null == jobBo) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "user{}create TrainJob，Encapsulating ptjupyterjobbo object is empty，the received parameters namespace：{}", currentUser.getId(), namespace);
                updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, flag);
            }
            PtJupyterJobVO ptJupyterJobResult = trainJobApi.create(jobBo);
            k8sJobName = ptJupyterJobResult.getName();
            if (null == ptJupyterJobResult || !ptJupyterJobResult.isSuccess()) {
                if (null != ptJupyterJobResult && ("" + K8sResponseEnum.LACK_OF_RESOURCES).equals(ptJupyterJobResult.getCode())) {
                    updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, flag);
                    LogUtil.error(LogEnum.BIZ_TRAIN, "user{}create TrainJob, K8s creation failed, the received parameters are{}, the wrong information is{}", currentUser.getUsername(),
                            jobBo, ptJupyterJobResult.getMessage());
                }
                String message = null == ptJupyterJobResult ? "未知的错误" : ptJupyterJobResult.getMessage();
                LogUtil.error(LogEnum.BIZ_TRAIN, "user{}create TrainJob, K8s creation failed, the received parameters are {}, the wrong information is{}", currentUser.getUsername(), jobBo, message);
                updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, flag);
            }
            flag = true;
            //更新训练任务状态
            updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, flag);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "user{}create TrainJob, K8s creation failed, the received parameters are {}, the wrong information is{}", currentUser.getUsername(),
                    jobBo, e);
            updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, flag);
        }
    }


    /**
     * 获取namespace
     *
     * @param currentUser 用户
     * @return String     命名空间
     */
    private String getNamespace(UserDTO currentUser) {
        String namespaceStr = k8sNameTool.generateNameSpace(currentUser.getId());
        BizNamespace bizNamespace = namespaceApi.get(namespaceStr);
        if (null == bizNamespace) {
            BizNamespace namespace = namespaceApi.create(namespaceStr, null);
            if (null == namespace || !namespace.isSuccess()) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} failed to create namespace during training job...");
                throw new BusinessException("内部错误");
            }
        }
        return namespaceStr;
    }


    /**
     * 封装出创建job所需的BO
     *
     * @param baseTrainJobDTO       训练任务信息
     * @param ptImageAndAlgorithmVO 镜像和算法信息
     * @param namespace             命名空间
     * @return PtJupyterJobBO       jupyter任务BO
     */
    private PtJupyterJobBO pkgPtJupyterJobBo(BaseTrainJobDTO baseTrainJobDTO, UserDTO currentUser,
                                             PtImageAndAlgorithmVO ptImageAndAlgorithmVO, String namespace) {
        //绝对路径
        String commonPath = nfsUtil.getNfsConfig().getBucket() + trainJobConstant.getManage() + StrUtil.SLASH
                + currentUser.getId() + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        //相对路径
        String relativeCommonPath = StrUtil.SLASH + trainJobConstant.getManage() + StrUtil.SLASH
                + currentUser.getId() + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        String[] codeDirArray = ptImageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        String workspaceDir = codeDirArray[codeDirArray.length - 1];
        // 算法路径待拷贝的地址
        String sourcePath = nfsUtil.getNfsConfig().getBucket() + ptImageAndAlgorithmVO.getCodeDir().substring(1);
        String trainDir = commonPath.substring(1) + StrUtil.SLASH + workspaceDir;
        LogUtil.info(LogEnum.BIZ_TRAIN, "Algorithm path copy::sourcePath:{},commonPath:{},trainDir:{}", sourcePath, commonPath, trainDir);
        boolean bool = nfsUtil.copyPath(sourcePath.substring(1), trainDir);
        if (!bool) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "During the process of user {} creating training Job and encapsulating k8s creating job interface parameters, it failed to copy algorithm directory {} to the specified directory {}", currentUser.getUsername(), sourcePath,
                    commonPath);
            return null;
        }

        List<String> list = new ArrayList<>();
        JSONObject runParams = baseTrainJobDTO.getRunParams();

        StringBuilder sb = new StringBuilder();
        sb.append(ptImageAndAlgorithmVO.getRunCommand());
        // 拼接out,log和dataset
        String pattern = trainJobConstant.getPythonFormat();
        if (ptImageAndAlgorithmVO.getIsTrainOut()) {
            nfsUtil.createDir(commonPath + StrUtil.SLASH + trainJobConstant.getOutPath());
            baseTrainJobDTO.setOutPath(relativeCommonPath + StrUtil.SLASH + trainJobConstant.getOutPath());
            sb.append(pattern).append(trainJobConstant.getDockerOutPath());
        }
        if (ptImageAndAlgorithmVO.getIsTrainLog()) {
            nfsUtil.createDir(commonPath + StrUtil.SLASH + trainJobConstant.getLogPath());
            baseTrainJobDTO.setLogPath(relativeCommonPath + StrUtil.SLASH + trainJobConstant.getLogPath());
            sb.append(pattern).append(trainJobConstant.getDockerLogPath());
        }
        if (ptImageAndAlgorithmVO.getIsVisualizedLog()) {
            nfsUtil.createDir(commonPath + StrUtil.SLASH + trainJobConstant.getVisualizedLogPath());
            baseTrainJobDTO.setVisualizedLogPath(relativeCommonPath + StrUtil.SLASH + trainJobConstant.getVisualizedLogPath());
            sb.append(pattern).append(trainJobConstant.getDockerVisualizedLogPath());
        }

        sb.append(pattern).append(trainJobConstant.getDockerDataset());

        String command = sb.toString();
        if (null != runParams && !runParams.isEmpty()) {
            sb.append(pattern);
            runParams.entrySet()
                    .forEach(entry -> sb.append(entry.getKey()).append("=").append(entry.getValue()).append(pattern));
            command = sb.toString().substring(0, sb.toString().length() - pattern.length());
        }
        list.add("-c");
        command = "echo 'training mission begins... " + command + "\r\n" + "'&& cd " + trainJobConstant.getDockerTrainPath() + StrUtil.SLASH
                + workspaceDir + " && " + command + " && echo 'the training mission is over' ";
        list.add(command);
        PtJupyterJobBO jobBo = new PtJupyterJobBO();
        jobBo.setNamespace(namespace)
                .setName(baseTrainJobDTO.getJobName())
                .setImage(ptImageAndAlgorithmVO.getImageName())
                .putNfsMounts(trainJobConstant.getDockerDatasetPath(), nfsUtil.getNfsConfig().getRootDir() + nfsUtil.getNfsConfig().getBucket().substring(1) + baseTrainJobDTO.getDataSourcePath())
                .setCmdLines(list)
                .putNfsMounts(trainJobConstant.getDockerTrainPath(), nfsUtil.getNfsConfig().getRootDir() + commonPath.substring(1))
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.ALGORITHM));

        jobBo.setCpuNum(baseTrainJobDTO.getPtTrainJobSpecs().getSpecsInfo().getInteger("cpuNum")).setMemNum(baseTrainJobDTO.getPtTrainJobSpecs().getSpecsInfo().getInteger("memNum"));
        if (ResourcesPoolTypeEnum.GPU.getCode().equals(baseTrainJobDTO.getPtTrainJobSpecs().getResourcesPoolType())) {
            jobBo.setUseGpu(true).setGpuNum(baseTrainJobDTO.getPtTrainJobSpecs().getSpecsInfo().getInteger("gpuNum"));
        } else {
            jobBo.setUseGpu(false);
        }
        return jobBo;
    }

    /**
     * 训练任务异步处理更新训练状态
     *
     * @param user            用户
     * @param ptTrainJob      训练任务
     * @param baseTrainJobDTO 训练任务信息
     * @param k8sJobName      k8s创建的job名称
     * @param flag            创建训练任务是否异常(true：正常，false：失败)
     **/
    private void updateTrainStatus(UserDTO user, PtTrainJob ptTrainJob, BaseTrainJobDTO baseTrainJobDTO, String k8sJobName, boolean flag) {

        ptTrainJob.setK8sJobName(k8sJobName)
                .setOutPath(baseTrainJobDTO.getOutPath())
                .setLogPath(baseTrainJobDTO.getLogPath())
                .setVisualizedLogPath(baseTrainJobDTO.getVisualizedLogPath());
        LogUtil.info(LogEnum.BIZ_TRAIN, "user {} training tasks are processed asynchronously to update training status，receiving parameters:{}", user.getId(), ptTrainJob);
        if (flag) {
            ptTrainJobMapper.updateById(ptTrainJob);
        } else {
            ptTrainJob.setTrainStatus(TrainJobStatusEnum.CREATE_FAILED.getStatus());
            //训练任务创建失败
            ptTrainJobMapper.updateById(ptTrainJob);
            throw new BusinessException("内部错误");
        }
    }
}
