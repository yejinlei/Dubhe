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
package org.dubhe.async;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.config.TrainJobConfig;
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
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.k8s.domain.resource.BizDistributeTrain;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;
import org.dubhe.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 提交训练任务
 * @date 2020-07-17
 */
@Component
public class TrainJobAsync {

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private NamespaceApi namespaceApi;

    @Autowired
    private TrainJobConfig trainJobConfig;

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private LocalFileUtil localFileUtil;

    @Autowired
    private PtTrainJobMapper ptTrainJobMapper;

    @Autowired
    private TrainJobApi trainJobApi;

    @Autowired
    private DistributeTrainApi distributeTrainApi;


    /**
     * 提交分布式训练
     *
     * @param baseTrainJobDTO       训练任务信息
     * @param currentUser           用户
     * @param ptImageAndAlgorithmVO 镜像和算法信息
     * @param ptTrainJob            训练任务实体信息
     */
    public void doDistributedJob(BaseTrainJobDTO baseTrainJobDTO, UserDTO currentUser, PtImageAndAlgorithmVO ptImageAndAlgorithmVO, PtTrainJob ptTrainJob) {
        try {
            //判断是否存在相应的namespace,如果没有则创建
            String namespace = getNamespace(currentUser);
            // 构建DistributeTrainBO
            DistributeTrainBO bo = buildDistributeTrainBO(baseTrainJobDTO, currentUser, ptImageAndAlgorithmVO, ptTrainJob, namespace);
            if (null == bo) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "user{}create TrainJob，Encapsulating ptjupyterjobbo object is empty，the received parameters namespace：{}", currentUser.getId(), namespace);
                updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, "", false);
                return;
            }
            // 调度K8s
            BizDistributeTrain bizDistributeTrain = distributeTrainApi.create(bo);
            if (bizDistributeTrain.isSuccess()) {
                // 调度成功
                updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, bizDistributeTrain.getName(), true);
            } else {
                // 调度失败
                LogUtil.error(LogEnum.BIZ_TRAIN, "distributeTrainApi.create FAILED! {}", bizDistributeTrain);
                updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, bizDistributeTrain.getName(), false);
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "doDistributedJob ERROR！{} ", e);
            updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, "", false);
        }
    }

    /**
     * 构造分布式训练DistributeTrainBO
     *
     * @param baseTrainJobDTO           训练任务信息
     * @param currentUser               用户
     * @param ptImageAndAlgorithmVO     镜像和算法信息
     * @param ptTrainJob                训练任务实体信息
     * @param namespace                 命名空间
     * @return  DistributeTrainBO
     */
    private DistributeTrainBO buildDistributeTrainBO(BaseTrainJobDTO baseTrainJobDTO, UserDTO currentUser, PtImageAndAlgorithmVO ptImageAndAlgorithmVO, PtTrainJob ptTrainJob, String namespace) {
        //绝对路径
        String basePath = nfsUtil.getNfsConfig().getBucket() + trainJobConfig.getManage() + StrUtil.SLASH
                + currentUser.getId() + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        //相对路径
        String relativePath = StrUtil.SLASH + trainJobConfig.getManage() + StrUtil.SLASH
                + currentUser.getId() + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        String[] codeDirArray = ptImageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        String workspaceDir = codeDirArray[codeDirArray.length - 1];
        // 算法路径待拷贝的地址
        String sourcePath = nfsUtil.getNfsConfig().getBucket() + ptImageAndAlgorithmVO.getCodeDir().substring(1);
        String trainDir = basePath.substring(1) + StrUtil.SLASH + workspaceDir;

        if (!localFileUtil.copyPath(sourcePath, trainDir)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "buildDistributeTrainBO copyPath failed ! sourcePath:{},basePath:{},trainDir:{}", sourcePath, basePath, trainDir);
            return null;
        }
        // 参数前缀
        String paramPrefix = trainJobConfig.getPythonFormat();
        // 初始化固定头命令，获取分布式节点IP
        StringBuilder sb = new StringBuilder("export NODE_IPS=`cat /home/hostfile.json |jq -r \".[]|.ip\"|paste -d \",\" -s` ");
        // 切换到算法路径下
        sb.append(" && cd ").append(trainJobConfig.getDockerTrainPath()).append(StrUtil.SLASH).append(workspaceDir).append(" && ");
        // 拼接用户自定义python启动命令
        sb.append(ptImageAndAlgorithmVO.getRunCommand());
        // 拼接python固定参数 节点IP
        sb.append(paramPrefix).append(trainJobConfig.getNodeIps()).append("=\"$NODE_IPS\" ");
        // 拼接python固定参数 节点数量
        sb.append(paramPrefix).append(trainJobConfig.getNodeNum()).append(SymbolConstant.FLAG_EQUAL).append(ptTrainJob.getResourcesPoolNode()).append(StrUtil.SPACE);
        if (ptImageAndAlgorithmVO.getIsTrainOut()) {
            // 拼接 out
            nfsUtil.createDir(basePath + StrUtil.SLASH + trainJobConfig.getOutPath());
            baseTrainJobDTO.setOutPath(relativePath + StrUtil.SLASH + trainJobConfig.getOutPath());
            sb.append(paramPrefix).append(trainJobConfig.getDockerOutPath());
        }
        if (ptImageAndAlgorithmVO.getIsTrainLog()) {
            // 拼接 输出日志
            nfsUtil.createDir(basePath + StrUtil.SLASH + trainJobConfig.getLogPath());
            baseTrainJobDTO.setLogPath(relativePath + StrUtil.SLASH + trainJobConfig.getLogPath());
            sb.append(paramPrefix).append(trainJobConfig.getDockerLogPath());
        }
        if (ptImageAndAlgorithmVO.getIsVisualizedLog()) {
            // 拼接 输出可视化日志
            nfsUtil.createDir(basePath + StrUtil.SLASH + trainJobConfig.getVisualizedLogPath());
            baseTrainJobDTO.setVisualizedLogPath(relativePath + StrUtil.SLASH + trainJobConfig.getVisualizedLogPath());
            sb.append(paramPrefix).append(trainJobConfig.getDockerVisualizedLogPath());
        }
        // 拼接python固定参数 数据集
        sb.append(paramPrefix).append(trainJobConfig.getDockerDataset());
        JSONObject runParams = baseTrainJobDTO.getRunParams();
        if (null != runParams && !runParams.isEmpty()) {
            // 拼接用户自定义参数
            runParams.entrySet().forEach(entry ->
                    sb.append(paramPrefix).append(entry.getKey()).append(SymbolConstant.FLAG_EQUAL).append(entry.getValue()).append(StrUtil.SPACE)
            );
        }
        // 在用户自定以参数拼接晚后拼接固定参数，防止被用户自定义参数覆盖
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getPtTrainJobSpecs().getResourcesPoolType())) {
            // 需要GPU
            sb.append(paramPrefix).append(trainJobConfig.getGpuNumPerNode()).append(SymbolConstant.FLAG_EQUAL).append(baseTrainJobDTO.getGpuNumPerNode()).append(StrUtil.SPACE);
        }
        String mainCommand = sb.toString();
        // 拼接辅助日志打印
        String wholeCommand = " echo 'Distribute training mission begins...  "
                + mainCommand
                + " ' && "
                + mainCommand
                + " && echo 'Distribute training mission is over' ";
        DistributeTrainBO distributeTrainBO = new DistributeTrainBO()
                .setNamespace(namespace)
                .setName(baseTrainJobDTO.getJobName())
                .setSize(ptTrainJob.getResourcesPoolNode())
                .setImage(ptImageAndAlgorithmVO.getImageName())
                .setMasterCmd(wholeCommand)
                .setMemNum(baseTrainJobDTO.getMenNum())
                .setCpuNum(baseTrainJobDTO.getCpuNum())
                .setDatasetStoragePath(k8sNameTool.getAbsoluteNfsPath(baseTrainJobDTO.getDataSourcePath()))
                .setWorkspaceStoragePath(localFileUtil.formatPath(nfsUtil.getNfsConfig().getRootDir() + basePath))
                .setModelStoragePath(k8sNameTool.getAbsoluteNfsPath(relativePath + StrUtil.SLASH + trainJobConfig.getOutPath()))
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.ALGORITHM));
        //延时启动，单位为分钟
        if (baseTrainJobDTO.getDelayCreateTime() != null && baseTrainJobDTO.getDelayCreateTime() > 0) {
            distributeTrainBO.setDelayCreateTime(baseTrainJobDTO.getDelayCreateTime() * MagicNumConstant.SIXTY);
        }
        //定时停止，单位为分钟
        if (baseTrainJobDTO.getDelayDeleteTime() != null && baseTrainJobDTO.getDelayDeleteTime() > 0) {
            distributeTrainBO.setDelayDeleteTime(baseTrainJobDTO.getDelayDeleteTime() * MagicNumConstant.SIXTY);
        }
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getPtTrainJobSpecs().getResourcesPoolType())) {
            // 需要GPU
            distributeTrainBO.setGpuNum(baseTrainJobDTO.getGpuNumPerNode());
        }
        // 主从一致
        distributeTrainBO.setSlaveCmd(distributeTrainBO.getMasterCmd());
        return distributeTrainBO;
    }


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
        try {
            //判断是否存在相应的namespace,如果没有则创建
            String namespace = getNamespace(currentUser);

            //封装PtJupyterJobBO对象,调用创建训练任务接口
            jobBo = pkgPtJupyterJobBo(baseTrainJobDTO, currentUser, ptImageAndAlgorithmVO, namespace);
            if (null == jobBo) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "user {} create TrainJob，Encapsulating ptjupyterjobbo object is empty，the received parameters namespace：{}", currentUser.getId(), namespace);
                updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, false);
            }
            PtJupyterJobVO ptJupyterJobResult = trainJobApi.create(jobBo);
            if (!ptJupyterJobResult.isSuccess()) {
                String message = null == ptJupyterJobResult.getMessage() ? "未知的错误" : ptJupyterJobResult.getMessage();
                LogUtil.error(LogEnum.BIZ_TRAIN, "user {} create TrainJob, K8s creation failed, the received parameters are {}, the wrong information is{}", currentUser.getUsername(), jobBo, message);
                ptTrainJob.setTrainMsg(message);
                updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, false);
            }
            k8sJobName = ptJupyterJobResult.getName();
            //更新训练任务状态
            updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, true);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "user {} create TrainJob, K8s creation failed, the received parameters are {}, the wrong information is{}", currentUser.getUsername(),
                    jobBo, e);
            ptTrainJob.setTrainMsg("内部错误");
            updateTrainStatus(currentUser, ptTrainJob, baseTrainJobDTO, k8sJobName, false);
        }
    }


    /**
     * 获取namespace
     *
     * @param currentUser 用户
     * @return String     命名空间
     */
    private String getNamespace(UserDTO currentUser) {
        String namespaceStr = k8sNameTool.generateNamespace(currentUser.getId());
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
        String commonPath = nfsUtil.getNfsConfig().getBucket() + trainJobConfig.getManage() + StrUtil.SLASH
                + currentUser.getId() + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        //相对路径
        String relativeCommonPath = StrUtil.SLASH + trainJobConfig.getManage() + StrUtil.SLASH
                + currentUser.getId() + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        String[] codeDirArray = ptImageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        String workspaceDir = codeDirArray[codeDirArray.length - 1];
        // 算法路径待拷贝的地址
        String sourcePath = nfsUtil.getNfsConfig().getBucket() + ptImageAndAlgorithmVO.getCodeDir().substring(1);
        String trainDir = commonPath.substring(1) + StrUtil.SLASH + workspaceDir;
        LogUtil.info(LogEnum.BIZ_TRAIN, "Algorithm path copy::sourcePath:{},commonPath:{},trainDir:{}", sourcePath, commonPath, trainDir);
        boolean bool = localFileUtil.copyPath(sourcePath.substring(1), trainDir);
        if (!bool) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "During the process of user {} creating training Job and encapsulating k8s creating job interface parameters, it failed to copy algorithm directory {} to the specified directory {}", currentUser.getUsername(), sourcePath.substring(1),
                    trainDir);
            return null;
        }

        List<String> list = new ArrayList<>();
        JSONObject runParams = baseTrainJobDTO.getRunParams();

        StringBuilder sb = new StringBuilder();
        sb.append(ptImageAndAlgorithmVO.getRunCommand());
        // 拼接out,log和dataset
        String pattern = trainJobConfig.getPythonFormat();
        if (ptImageAndAlgorithmVO.getIsTrainOut()) {
            nfsUtil.createDir(commonPath + StrUtil.SLASH + trainJobConfig.getOutPath());
            baseTrainJobDTO.setOutPath(relativeCommonPath + StrUtil.SLASH + trainJobConfig.getOutPath());
            sb.append(pattern).append(trainJobConfig.getDockerOutPath());
        }
        if (ptImageAndAlgorithmVO.getIsTrainLog()) {
            nfsUtil.createDir(commonPath + StrUtil.SLASH + trainJobConfig.getLogPath());
            baseTrainJobDTO.setLogPath(relativeCommonPath + StrUtil.SLASH + trainJobConfig.getLogPath());
            sb.append(pattern).append(trainJobConfig.getDockerLogPath());
        }
        if (ptImageAndAlgorithmVO.getIsVisualizedLog()) {
            nfsUtil.createDir(commonPath + StrUtil.SLASH + trainJobConfig.getVisualizedLogPath());
            baseTrainJobDTO.setVisualizedLogPath(relativeCommonPath + StrUtil.SLASH + trainJobConfig.getVisualizedLogPath());
            sb.append(pattern).append(trainJobConfig.getDockerVisualizedLogPath());
        }
        sb.append(pattern).append(trainJobConfig.getDockerDataset());

        String valDataSourcePath = baseTrainJobDTO.getValDataSourcePath();
        if (StringUtils.isNotBlank(valDataSourcePath)) {
            sb.append(pattern).append(trainJobConfig.getLoadValDatasetKey()).append(SymbolConstant.FLAG_EQUAL).append(trainJobConfig.getDockerValDatasetPath());
        }
        //将模型加载路径拼接到
        String modelLoadPathDir = baseTrainJobDTO.getModelLoadPathDir();
        if (StringUtils.isNotBlank(modelLoadPathDir)) {
            //将模型路径model_load_dir路径
            sb.append(pattern).append(trainJobConfig.getLoadKey()).append(SymbolConstant.FLAG_EQUAL).append(trainJobConfig.getDockerModelPath());
        }

        if (null != runParams && !runParams.isEmpty()) {
            runParams.forEach((k, v) ->
                    sb.append(pattern).append(k).append(SymbolConstant.FLAG_EQUAL).append(v).append(StrUtil.SPACE)
            );
        }
        // 在用户自定以参数拼接晚后拼接固定参数，防止被用户自定义参数覆盖
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getPtTrainJobSpecs().getResourcesPoolType())) {
            // 需要GPU
            sb.append(pattern).append(trainJobConfig.getGpuNumPerNode()).append(SymbolConstant.FLAG_EQUAL).append(baseTrainJobDTO.getGpuNumPerNode()).append(StrUtil.SPACE);
        }
        String executeCmd = sb.toString();
        list.add("-c");

        String workPath = trainJobConfig.getDockerTrainPath() + StrUtil.SLASH + workspaceDir;
        String command = "echo 'training mission begins... " + executeCmd + "\r\n '" +
                " && cd " + workPath +
                " && " + executeCmd +
                " && echo 'the training mission is over' ";
        list.add(command);

        PtJupyterJobBO jobBo = new PtJupyterJobBO();
        jobBo.setNamespace(namespace)
                .setName(baseTrainJobDTO.getJobName())
                .setImage(ptImageAndAlgorithmVO.getImageName())
                .putNfsMounts(trainJobConfig.getDockerDatasetPath(), nfsUtil.getNfsConfig().getRootDir() + nfsUtil.getNfsConfig().getBucket().substring(1) + baseTrainJobDTO.getDataSourcePath())
                .setCmdLines(list)
                .putNfsMounts(trainJobConfig.getDockerTrainPath(), nfsUtil.getNfsConfig().getRootDir() + commonPath.substring(1))
                .putNfsMounts(trainJobConfig.getDockerModelPath(), nfsUtil.formatPath(nfsUtil.getAbsolutePath(modelLoadPathDir)))
                .putNfsMounts(trainJobConfig.getDockerValDatasetPath(), nfsUtil.formatPath(nfsUtil.getAbsolutePath(valDataSourcePath)))
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.ALGORITHM));
        //延时启动，单位为分钟
        if (baseTrainJobDTO.getDelayCreateTime() != null && baseTrainJobDTO.getDelayCreateTime() > 0) {
            jobBo.setDelayCreateTime(baseTrainJobDTO.getDelayCreateTime() * MagicNumConstant.SIXTY);
        }
        //自动停止，单位为分钟
        if (baseTrainJobDTO.getDelayDeleteTime() != null && baseTrainJobDTO.getDelayDeleteTime() > 0) {
            jobBo.setDelayDeleteTime(baseTrainJobDTO.getDelayDeleteTime() * MagicNumConstant.SIXTY);
        }
        jobBo.setCpuNum(baseTrainJobDTO.getCpuNum()).setMemNum(baseTrainJobDTO.getMenNum());
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getPtTrainJobSpecs().getResourcesPoolType())) {
            jobBo.setUseGpu(true).setGpuNum(baseTrainJobDTO.getGpuNumPerNode());
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
     * @param k8sJobName      k8s创建的job名称，或者分布式训练名称
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
        }
    }
}
