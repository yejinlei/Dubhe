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
package org.dubhe.train.async;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.base.enums.ModelResourceEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.k8s.domain.resource.BizDistributeTrain;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.train.config.TrainJobConfig;
import org.dubhe.train.constant.TrainConstant;
import org.dubhe.train.dao.PtTrainJobMapper;
import org.dubhe.train.domain.dto.BaseTrainJobDTO;
import org.dubhe.train.domain.entity.PtTrainJob;
import org.dubhe.train.domain.vo.PtImageAndAlgorithmVO;
import org.dubhe.train.enums.ResourcesPoolTypeEnum;
import org.dubhe.train.enums.TrainJobStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

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
     * @param userId                用户id
     * @param ptImageAndAlgorithmVO 镜像和算法信息
     * @param ptTrainJob            训练任务实体信息
     */
    public void doDistributedJob(BaseTrainJobDTO baseTrainJobDTO, Long userId, PtImageAndAlgorithmVO ptImageAndAlgorithmVO, PtTrainJob ptTrainJob) {
        try {
            //判断是否存在相应的namespace,如果没有则创建
            String namespace = getNamespace(userId);
            // 构建DistributeTrainBO
            DistributeTrainBO bo = buildDistributeTrainBO(baseTrainJobDTO, userId, ptImageAndAlgorithmVO, ptTrainJob, namespace);
            if (null == bo) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "user {} failed to create train Job ,distributeTrainBO is empty, the namespace is {}", userId, namespace);
                ptTrainJob.putStatusDetail("Message", "内部错误");
                updateTrainStatus(userId, ptTrainJob, baseTrainJobDTO, "", false);
                return;
            }
            // 调度K8s
            BizDistributeTrain bizDistributeTrain = distributeTrainApi.create(bo);
            if (bizDistributeTrain.isSuccess()) {
                // 调度成功
                updateTrainStatus(userId, ptTrainJob, baseTrainJobDTO, bizDistributeTrain.getName(), true);
            } else {
                // 调度失败
                String message = null == bizDistributeTrain.getMessage() ? "未知的错误" : bizDistributeTrain.getMessage();
                LogUtil.error(LogEnum.BIZ_TRAIN, "userId {} create Distribute Train, K8s creation failed, the received parameters are {}, the wrong information is{}", userId, bo, message);
                ptTrainJob.putStatusDetail("Message", message);
                updateTrainStatus(userId, ptTrainJob, baseTrainJobDTO, bizDistributeTrain.getName(), false);
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "doDistributedJob ERROR！{} ", e);
            ptTrainJob.putStatusDetail("Message", "内部错误");
            updateTrainStatus(userId, ptTrainJob, baseTrainJobDTO, "", false);
        }
    }

    /**
     * 构造分布式训练DistributeTrainBO(炼知模型暂不支持)
     *
     * @param baseTrainJobDTO       训练任务信息
     * @param userId                用户id
     * @param ptImageAndAlgorithmVO 镜像和算法信息
     * @param ptTrainJob            训练任务实体信息
     * @param namespace             命名空间
     * @return DistributeTrainBO
     */
    private DistributeTrainBO buildDistributeTrainBO(BaseTrainJobDTO baseTrainJobDTO, Long userId, PtImageAndAlgorithmVO ptImageAndAlgorithmVO, PtTrainJob ptTrainJob, String namespace) {
        //绝对路径
        String basePath = fileStoreApi.getBucket() + trainJobConfig.getManage() + StrUtil.SLASH
                + userId + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        //相对路径
        String relativePath = StrUtil.SLASH + trainJobConfig.getManage() + StrUtil.SLASH
                + userId + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        String[] codeDirArray = ptImageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        String workspaceDir = codeDirArray[codeDirArray.length - 1];
        // 算法路径待拷贝的地址
        String sourcePath = fileStoreApi.getBucket() + ptImageAndAlgorithmVO.getCodeDir().substring(1);
        String trainDir = basePath.substring(1) + StrUtil.SLASH + workspaceDir;

        if (!fileStoreApi.copyPath(fileStoreApi.getRootDir() + sourcePath, fileStoreApi.getRootDir() + trainDir)) {
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
        if (ptImageAndAlgorithmVO.getIsTrainModelOut()) {
            // 拼接 out
            fileStoreApi.createDir(fileStoreApi.getRootDir() + basePath + StrUtil.SLASH + trainJobConfig.getModelPath());
            baseTrainJobDTO.setTrainModelPath(relativePath + StrUtil.SLASH + trainJobConfig.getModelPath());
            sb.append(paramPrefix).append(trainJobConfig.getDockerTrainModelPath());
        }
        if (ptImageAndAlgorithmVO.getIsTrainOut()) {
            // 拼接 输出日志
            fileStoreApi.createDir(fileStoreApi.getRootDir() + basePath + StrUtil.SLASH + trainJobConfig.getOutPath());
            baseTrainJobDTO.setTrainOutPath(relativePath + StrUtil.SLASH + trainJobConfig.getOutPath());
            sb.append(paramPrefix).append(trainJobConfig.getDockerTrainOutPath());
        }
        if (ptImageAndAlgorithmVO.getIsVisualizedLog()) {
            // 拼接 输出可视化日志
            fileStoreApi.createDir(fileStoreApi.getRootDir() + basePath + StrUtil.SLASH + trainJobConfig.getVisualizedLogPath());
            baseTrainJobDTO.setVisualizedLogPath(relativePath + StrUtil.SLASH + trainJobConfig.getVisualizedLogPath());
            sb.append(paramPrefix).append(trainJobConfig.getDockerVisualizedLogPath());
        }
        // 拼接python固定参数 数据集
        sb.append(paramPrefix).append(trainJobConfig.getDockerDataset());

        String valDataSourcePath = baseTrainJobDTO.getValDataSourcePath();
        if (StringUtils.isNotBlank(valDataSourcePath)) {
            sb.append(paramPrefix).append(trainJobConfig.getLoadValDatasetKey()).append(SymbolConstant.FLAG_EQUAL).append(trainJobConfig.getDockerValDatasetPath());
        }

        // 模型路径挂载及其参数拼接
        DistributeTrainBO distributeTrainBO = new DistributeTrainBO();
        buildBoAboutModel(baseTrainJobDTO, distributeTrainBO, sb);

        JSONObject runParams = baseTrainJobDTO.getRunParams();
        if (null != runParams && !runParams.isEmpty()) {
            // 拼接用户自定义参数
            runParams.entrySet().forEach(entry ->
                    sb.append(paramPrefix).append(entry.getKey()).append(SymbolConstant.FLAG_EQUAL).append(entry.getValue()).append(StrUtil.SPACE)
            );
        }
        // 在用户自定以参数拼接晚后拼接固定参数，防止被用户自定义参数覆盖
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getResourcesPoolType())) {
            // 需要GPU
            sb.append(paramPrefix).append(trainJobConfig.getGpuNumPerNode()).append(SymbolConstant.FLAG_EQUAL).append(baseTrainJobDTO.getGpuNum()).append(StrUtil.SPACE);
        }
        String mainCommand = sb.toString();
        // 拼接辅助日志打印
        String wholeCommand = " echo 'Distribute training mission begins...  "
                + mainCommand
                + " ' && "
                + mainCommand
                + " && echo 'Distribute training mission is over' ";
        distributeTrainBO
                .setNamespace(namespace)
                .setName(baseTrainJobDTO.getJobName())
                .setSize(ptTrainJob.getResourcesPoolNode())
                .setImage(ptImageAndAlgorithmVO.getImageUrl())
                .setMasterCmd(wholeCommand)
                .setMemNum(baseTrainJobDTO.getMemNum())
                .setCpuNum(baseTrainJobDTO.getCpuNum() * MagicNumConstant.ONE_THOUSAND)
                .putFsMounts(TrainConstant.WORKSPACE_VOLUME_MOUNTS, fileStoreApi.formatPath(fileStoreApi.getRootDir() + basePath))
                .putFsMounts(TrainConstant.MODEL_VOLUME_MOUNTS, k8sNameTool.getAbsolutePath(relativePath + StrUtil.SLASH + trainJobConfig.getOutPath()))
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.ALGORITHM))
                .setTaskIdentifyLabel(baseTrainJobDTO.getTaskIdentify());
        if (StringUtils.isNotBlank(baseTrainJobDTO.getDataSourcePath())) {
            distributeTrainBO.putFsMounts(TrainConstant.DATASET_VOLUME_MOUNTS, k8sNameTool.getAbsolutePath(baseTrainJobDTO.getDataSourcePath()));
        }
        if (StringUtils.isNotBlank(valDataSourcePath)) {
            distributeTrainBO.putFsMounts(trainJobConfig.getDockerValDatasetPath(), fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + valDataSourcePath));
        }
        //延时启动，单位为分钟
        if (baseTrainJobDTO.getDelayCreateTime() != null && baseTrainJobDTO.getDelayCreateTime() > 0) {
            distributeTrainBO.setDelayCreateTime(baseTrainJobDTO.getDelayCreateTime() * MagicNumConstant.SIXTY);
        }
        //定时停止，单位为分钟
        if (baseTrainJobDTO.getDelayDeleteTime() != null && baseTrainJobDTO.getDelayDeleteTime() > 0) {
            distributeTrainBO.setDelayDeleteTime(baseTrainJobDTO.getDelayDeleteTime() * MagicNumConstant.SIXTY);
        }
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getResourcesPoolType())) {
            // 需要GPU
            distributeTrainBO.setGpuNum(baseTrainJobDTO.getGpuNum());
        }
        // 主从一致
        distributeTrainBO.setSlaveCmd(distributeTrainBO.getMasterCmd());
        return distributeTrainBO;
    }


    /**
     * 提交job
     *
     * @param baseTrainJobDTO       训练任务信息
     * @param userId                用户id
     * @param ptImageAndAlgorithmVO 镜像和算法信息
     */
    public void doJob(BaseTrainJobDTO baseTrainJobDTO, Long userId, PtImageAndAlgorithmVO ptImageAndAlgorithmVO, PtTrainJob ptTrainJob) {
        PtJupyterJobBO jobBo = null;
        String k8sJobName = "";
        try {
            //判断是否存在相应的namespace,如果没有则创建
            String namespace = getNamespace(userId);

            //封装PtJupyterJobBO对象,调用创建训练任务接口
            jobBo = pkgPtJupyterJobBo(baseTrainJobDTO, userId, ptImageAndAlgorithmVO, namespace);
            if (null == jobBo) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "userId {} create TrainJob, ptJupyterJobBO is empty,the received parameters namespace：{}", userId, namespace);
                ptTrainJob.putStatusDetail("Message", "内部错误");
                updateTrainStatus(userId, ptTrainJob, baseTrainJobDTO, k8sJobName, false);
                return;
            }
            PtJupyterJobVO ptJupyterJobResult = trainJobApi.create(jobBo);
            if (!ptJupyterJobResult.isSuccess()) {
                String message = null == ptJupyterJobResult.getMessage() ? "未知的错误" : ptJupyterJobResult.getMessage();
                LogUtil.error(LogEnum.BIZ_TRAIN, "userId {} create TrainJob, k8s creation failed, the received parameters are {}, the wrong information is{}", userId, jobBo, message);
                ptTrainJob.putStatusDetail("Message", message);
                updateTrainStatus(userId, ptTrainJob, baseTrainJobDTO, k8sJobName, false);
            }
            k8sJobName = ptJupyterJobResult.getName();
            //更新训练任务状态
            updateTrainStatus(userId, ptTrainJob, baseTrainJobDTO, k8sJobName, true);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "userId {} create TrainJob, K8s creation failed, the received parameters are {}, the wrong information is{}", userId,
                    jobBo, e);
            ptTrainJob.putStatusDetail("Message", "内部错误");
            updateTrainStatus(userId, ptTrainJob, baseTrainJobDTO, k8sJobName, false);
        }
    }


    /**
     * 获取namespace
     *
     * @param userId 用户id
     * @return String     命名空间
     */
    private String getNamespace(Long userId) {
        String namespaceStr = k8sNameTool.generateNamespace(userId);
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
    private PtJupyterJobBO pkgPtJupyterJobBo(BaseTrainJobDTO baseTrainJobDTO, Long userId,
                                             PtImageAndAlgorithmVO ptImageAndAlgorithmVO, String namespace) {

        //绝对路径
        String commonPath = fileStoreApi.getBucket() + trainJobConfig.getManage() + StrUtil.SLASH
                + userId + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        //相对路径
        String relativeCommonPath = StrUtil.SLASH + trainJobConfig.getManage() + StrUtil.SLASH
                + userId + StrUtil.SLASH + baseTrainJobDTO.getJobName();
        String[] codeDirArray = ptImageAndAlgorithmVO.getCodeDir().split(StrUtil.SLASH);
        String workspaceDir = codeDirArray[codeDirArray.length - 1];
        // 算法路径待拷贝的地址
        String sourcePath = fileStoreApi.getBucket() + ptImageAndAlgorithmVO.getCodeDir().substring(1);
        String trainDir = commonPath.substring(1) + StrUtil.SLASH + workspaceDir;
        LogUtil.info(LogEnum.BIZ_TRAIN, "Algorithm path copy sourcePath:{},commonPath:{},trainDir:{}", sourcePath, commonPath, trainDir);
        boolean bool = fileStoreApi.copyPath(fileStoreApi.getRootDir() + sourcePath.substring(1), fileStoreApi.getRootDir() + trainDir);
        if (!bool) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "During the process of userId {} creating training Job , it failed to copy algorithm directory {} to the target directory {}", userId, sourcePath.substring(1),
                    trainDir);
            return null;
        }

        List<String> list = new ArrayList<>();
        PtJupyterJobBO jobBo = new PtJupyterJobBO();
        JSONObject runParams = baseTrainJobDTO.getRunParams();

        StringBuilder sb = new StringBuilder();
        sb.append(ptImageAndAlgorithmVO.getRunCommand());
        // 拼接out,log和dataset
        String pattern = trainJobConfig.getPythonFormat();
        if (ptImageAndAlgorithmVO.getIsTrainModelOut()) {
            fileStoreApi.createDir(fileStoreApi.getRootDir() + commonPath + StrUtil.SLASH + trainJobConfig.getModelPath());
            baseTrainJobDTO.setTrainModelPath(relativeCommonPath + StrUtil.SLASH + trainJobConfig.getModelPath());
            sb.append(pattern).append(trainJobConfig.getDockerTrainModelPath());
        }
        if (ptImageAndAlgorithmVO.getIsTrainOut()) {
            fileStoreApi.createDir(fileStoreApi.getRootDir() + commonPath + StrUtil.SLASH + trainJobConfig.getOutPath());
            baseTrainJobDTO.setTrainOutPath(relativeCommonPath + StrUtil.SLASH + trainJobConfig.getOutPath());
            sb.append(pattern).append(trainJobConfig.getDockerTrainOutPath());
        }
        if (ptImageAndAlgorithmVO.getIsVisualizedLog()) {
            fileStoreApi.createDir(fileStoreApi.getRootDir() + commonPath + StrUtil.SLASH + trainJobConfig.getVisualizedLogPath());
            baseTrainJobDTO.setVisualizedLogPath(relativeCommonPath + StrUtil.SLASH + trainJobConfig.getVisualizedLogPath());
            sb.append(pattern).append(trainJobConfig.getDockerVisualizedLogPath());
        }
        sb.append(pattern).append(trainJobConfig.getDockerDataset());

        String valDataSourcePath = baseTrainJobDTO.getValDataSourcePath();
        if (StringUtils.isNotBlank(valDataSourcePath)) {
            sb.append(pattern).append(trainJobConfig.getLoadValDatasetKey()).append(SymbolConstant.FLAG_EQUAL).append(trainJobConfig.getDockerValDatasetPath());
        }
        //模型路径挂载及其参数拼接
        buildBoAboutModel(baseTrainJobDTO, jobBo, sb);

        if (null != runParams && !runParams.isEmpty()) {
            runParams.forEach((k, v) ->
                    sb.append(pattern).append(k).append(SymbolConstant.FLAG_EQUAL).append(v).append(StrUtil.SPACE)
            );
        }
        // 在用户自定以参数拼接晚后拼接固定参数，防止被用户自定义参数覆盖
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getResourcesPoolType())) {
            // 需要GPU
            sb.append(pattern).append(trainJobConfig.getGpuNumPerNode()).append(SymbolConstant.FLAG_EQUAL).append(baseTrainJobDTO.getGpuNum()).append(StrUtil.SPACE);
        }
        String executeCmd = sb.toString();
        list.add("-c");

        String workPath = trainJobConfig.getDockerTrainPath() + StrUtil.SLASH + workspaceDir;
        String command;
        Integer modelResource = baseTrainJobDTO.getModelResource();
        if (null != modelResource && modelResource.intValue() == ModelResourceEnum.ATLAS.getType().intValue()) {
            command = "&& " + trainJobConfig.getAtlasAnaconda() +
                    " && cd " + workPath +
                    " && " + trainJobConfig.getAtlasPythonioencoding() + executeCmd;
        } else {
            command = " && cd " + workPath + " && " + executeCmd;
        }
        command = "echo 'training mission begins... " + executeCmd + "\r\n '" + command + " && echo 'the training mission is over' ";

        list.add(command);

        jobBo.setNamespace(namespace)
                .setName(baseTrainJobDTO.getJobName())
                .setImage(ptImageAndAlgorithmVO.getImageUrl())
                .setCmdLines(list)
                .putFsMounts(trainJobConfig.getDockerTrainPath(), fileStoreApi.getRootDir() + commonPath.substring(1))
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.ALGORITHM))
                .setTaskIdentifyLabel(baseTrainJobDTO.getTaskIdentify());
        if (StringUtils.isNotBlank(baseTrainJobDTO.getDataSourcePath())) {
            jobBo.putFsMounts(trainJobConfig.getDockerDatasetPath(), fileStoreApi.getRootDir() + fileStoreApi.getBucket().substring(1) + baseTrainJobDTO.getDataSourcePath());
        }
        if (StringUtils.isNotBlank(valDataSourcePath)) {
            jobBo.putFsMounts(trainJobConfig.getDockerValDatasetPath(), fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + valDataSourcePath));
        }
        //挂载pip路径
        if (StringUtils.isNotBlank(baseTrainJobDTO.getPipSitePackagePath())) {
            String formatPath = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + baseTrainJobDTO.getPipSitePackagePath());
            jobBo.putFsMounts(trainJobConfig.getDockerPipSitePackagePath(), formatPath);
            //检测pip包依赖路径
            int startIndex = -1;
            List<String> cmdLines = jobBo.getCmdLines();
            for (int i = 0; i < cmdLines.size(); i++) {
                //bash -c 这种情况
                if ("-c".equals(cmdLines.get(i))) {
                    startIndex = i;
                }
            }
            String cmdLine = cmdLines.get(startIndex + 1);
            String appendPythonPath = " export PYTHONPATH=" + trainJobConfig.getDockerPipSitePackagePath() + " && ";
            cmdLine = appendPythonPath + cmdLine;
            cmdLines.set(startIndex + 1, cmdLine);
        }
        //延时启动，单位为分钟
        if (baseTrainJobDTO.getDelayCreateTime() != null && baseTrainJobDTO.getDelayCreateTime() > 0) {
            jobBo.setDelayCreateTime(baseTrainJobDTO.getDelayCreateTime() * MagicNumConstant.SIXTY);
        }
        //自动停止，单位为分钟
        if (baseTrainJobDTO.getDelayDeleteTime() != null && baseTrainJobDTO.getDelayDeleteTime() > 0) {
            jobBo.setDelayDeleteTime(baseTrainJobDTO.getDelayDeleteTime() * MagicNumConstant.SIXTY);
        }
        jobBo.setCpuNum(baseTrainJobDTO.getCpuNum() * MagicNumConstant.ONE_THOUSAND).setMemNum(baseTrainJobDTO.getMemNum());
        if (ResourcesPoolTypeEnum.isGpuCode(baseTrainJobDTO.getResourcesPoolType())) {
            jobBo.setUseGpu(true).setGpuNum(baseTrainJobDTO.getGpuNum());
        } else {
            jobBo.setUseGpu(false);
        }
        return jobBo;
    }

    /**
     * 模型路径挂载及其参数拼接
     *
     * @param baseTrainJobDTO 训练任务基本信息
     * @param jobBo           训练任务实体
     * @param sb              训练命令参数
     */
    private void buildBoAboutModel(BaseTrainJobDTO baseTrainJobDTO, Object jobBo, StringBuilder sb) {
        if (null == baseTrainJobDTO.getModelResource()) {
            return;
        }
        String modelLoadPathDir = baseTrainJobDTO.getModelPath();
        //非炼知模型
        if (StringUtils.isNotBlank(modelLoadPathDir)) {
            //将模型路径model_load_dir路径
            sb.append(trainJobConfig.getPythonFormat()).append(trainJobConfig.getLoadKey()).append(SymbolConstant.FLAG_EQUAL).append(trainJobConfig.getDockerModelPath());
            if (jobBo instanceof PtJupyterJobBO) {
                PtJupyterJobBO ptJupyterJobBO = (PtJupyterJobBO) jobBo;
                ptJupyterJobBO.putFsMounts(trainJobConfig.getDockerModelPath(), fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelLoadPathDir));
            } else if (jobBo instanceof DistributeTrainBO) {
                DistributeTrainBO distributeTrainBO = (DistributeTrainBO) jobBo;
                distributeTrainBO.putFsMounts(trainJobConfig.getDockerModelPath(), fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelLoadPathDir));
            }
            return;
        }
        //炼知模型中的教师模型
        appendAtlasModelPath(baseTrainJobDTO.getTeacherModelPathList(), jobBo, sb, true);
        //炼知模型中的学生模型
        appendAtlasModelPath(baseTrainJobDTO.getStudentModelPathList(), jobBo, sb, false);
    }

    /**
     * 炼知模型路径挂载及其参数拼接
     *
     * @param modelPathList 模型路径集合
     * @param jobBo         训练任务实体
     * @param sb            训练命令参数
     * @param isTeacher     是否教师模型
     */
    private void appendAtlasModelPath(List<String> modelPathList, Object jobBo, StringBuilder sb, boolean isTeacher) {
        if (null == modelPathList || modelPathList.isEmpty()) {
            return;
        }
        StringBuilder appendModelPath = new StringBuilder();
        String preModelKey;
        String preModelPath;
        if (isTeacher) {
            preModelKey = trainJobConfig.getDockerTeacherModelKey();
            preModelPath = trainJobConfig.getDockerTeacherModelPath();
        } else {
            preModelKey = trainJobConfig.getDockerStudentModelKey();
            preModelPath = trainJobConfig.getDockerStudentModelPath();
        }
        modelPathList.stream()
                .forEach(modelPath -> {
                    String[] urlArray = modelPath.split(SymbolConstant.SLASH);
                    String dockerModelPath = urlArray[urlArray.length - MagicNumConstant.ONE];
                    String mountPath = preModelPath + SymbolConstant.SLASH + dockerModelPath;
                    appendModelPath.append(mountPath).append(SymbolConstant.COMMA);
                    if (jobBo instanceof PtJupyterJobBO) {
                        PtJupyterJobBO ptJupyterJobBO = (PtJupyterJobBO) jobBo;
                        ptJupyterJobBO.putFsMounts(mountPath, fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelPath));
                    } else if (jobBo instanceof DistributeTrainBO) {
                        DistributeTrainBO distributeTrainBO = (DistributeTrainBO) jobBo;
                        distributeTrainBO.putFsMounts(mountPath, fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + modelPath));
                    }
                });
        String resultPath = SymbolConstant.MARK +
                appendModelPath.toString().substring(MagicNumConstant.ZERO, appendModelPath.toString().length() - MagicNumConstant.ONE) +
                SymbolConstant.MARK;

        sb.append(trainJobConfig.getPythonFormat()).append(preModelKey).append(SymbolConstant.FLAG_EQUAL).append(resultPath);
    }

    /**
     * 训练任务异步处理更新训练状态
     *
     * @param userId             用户id
     * @param ptTrainJob         训练任务
     * @param baseTrainJobDTO    训练任务信息
     * @param k8sJobName         k8s创建的job名称，或者分布式训练名称
     * @param createTrainSuccess 创建训练任务是否异常(true：正常，false：失败)
     **/
    private void updateTrainStatus(Long userId, PtTrainJob ptTrainJob, BaseTrainJobDTO baseTrainJobDTO, String k8sJobName, boolean createTrainSuccess) {

        ptTrainJob.setK8sJobName(k8sJobName)
                .setModelPath(baseTrainJobDTO.getTrainModelPath())
                .setOutPath(baseTrainJobDTO.getTrainOutPath())
                .setVisualizedLogPath(baseTrainJobDTO.getVisualizedLogPath());
        LogUtil.info(LogEnum.BIZ_TRAIN, "user {} training tasks are processed asynchronously to update training status，receiving parameters:{}", userId, ptTrainJob);

        //训练任务创建失败
        if (!createTrainSuccess) {
            ptTrainJob.setTrainStatus(TrainJobStatusEnum.CREATE_FAILED.getStatus());
        }
        ptTrainJobMapper.updateById(ptTrainJob);
    }
}
