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

package org.dubhe.task;

import org.dubhe.constant.NumberConstant;
import org.dubhe.constant.ServingConstant;
import org.dubhe.dao.BatchServingMapper;
import org.dubhe.dao.ServingInfoMapper;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.BatchServing;
import org.dubhe.domain.entity.ServingInfo;
import org.dubhe.domain.entity.ServingModelConfig;
import org.dubhe.enums.BizEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.ResourcesPoolTypeEnum;
import org.dubhe.enums.ServingErrorEnum;
import org.dubhe.enums.ServingFrameTypeEnum;
import org.dubhe.enums.ServingStatusEnum;
import org.dubhe.enums.ServingTypeEnum;
import org.dubhe.exception.BaseErrorCode;
import org.dubhe.exception.BusinessException;
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.ModelServingApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.bo.ModelServingBO;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.resource.BizDistributeTrain;
import org.dubhe.k8s.domain.resource.BizIngressRule;
import org.dubhe.k8s.domain.vo.ModelServingVO;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;
import org.dubhe.service.MailService;
import org.dubhe.service.ServingModelConfigService;
import org.dubhe.utils.GrpcClient;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.K8sUtil;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @description 部署服务异步处理
 * @date 2020-09-02
 */
@Component
public class DeployServingAsyncTask {

    @Resource
    private ServingInfoMapper servingInfoMapper;

    @Resource
    private BatchServingMapper batchServingMapper;

    @Resource
    private ServingModelConfigService servingModelConfigService;

    @Resource
    private ModelServingApi modelServingApi;

    @Resource
    private K8sUtil k8sUtil;

    @Resource
    private K8sNameTool k8sNameTool;

    @Resource
    private DistributeTrainApi distributeTrainApi;

    @Resource
    private TrainJobApi trainJobApi;

    @Resource
    private MailService mailService;

    /**
     * Service源码路径
     */
    @Value("${serving.sourcePath}")
    private String sourcePath;

    @Value("${serving.gpu-image}")
    private String gpuImage;

    @Value("${serving.cpu-image}")
    private String cpuImage;

    private final static String TRUE = "True";

    private final static String FALSE = "False";

    /**
     * 创建pod，部署模型
     *
     * @param userDTO         用户信息
     * @param servingInfo     在线服务信息
     * @param modelConfigList 在线服务模型部署信息集合
     */
    @Async("servingExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void deployServing(UserDTO userDTO, ServingInfo servingInfo, List<ServingModelConfig> modelConfigList) {
        boolean flag = false;
        for (ServingModelConfig servingModelConfig : modelConfigList) {
            try {
                ModelServingBO bo = buildModelServingBO(userDTO, servingInfo, servingModelConfig);
                //创建pod
                ModelServingVO modelServingVO = modelServingApi.create(bo);
                if (ServingConstant.SUCCESS_CODE.equals(modelServingVO.getCode())) {
                    // 获取pod对应的url，并修改模型部署状态
                    List<BizIngressRule> rules = modelServingVO.getBizIngress().getRules();
                    //取第一个url
                    String url = rules.get(NumberConstant.NUMBER_0).getHost();
                    if (StringUtils.isNotBlank(url)) {
                        servingModelConfig.setUrl(url);
                        if (servingModelConfigService.updateById(servingModelConfig)) {
                            flag = true;
                        } else {
                            LogUtil.error(LogEnum.SERVING, "User {} failed saving online service model config. Database update FAILED, service id={}", userDTO.getUsername(), servingModelConfig.getId());
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.SERVING, "User {} create serving failed.The name of serving is {}", userDTO.getUsername(), servingInfo.getName(), e);
            }
        }

        //修改服务状态
        if (!flag) {
            servingInfo.setStatus(ServingStatusEnum.EXCEPTION.getStatus());
        }
        //grpc协议，创建对应的通道
        if (ServingTypeEnum.GRPC.getType().equals(servingInfo.getType())) {
            String url = modelConfigList.get(0).getUrl();
            if (StringUtils.isNotBlank(url)) {
                GrpcClient.createChannel(servingInfo.getId(), url, userDTO);
            }
        }
        int result = servingInfoMapper.updateById(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} failed to update the servingInfo table.The name of serving is {}", userDTO.getUsername(), servingInfo.getName());
        }
    }

    /**
     * 构建模型部署参数
     *
     * @param userDTO            用户信息
     * @param servingInfo        在线服务信息
     * @param servingModelConfig 在线服务模型部署信息
     * @return ModelServingBO 返回构建后对象
     */
    private ModelServingBO buildModelServingBO(UserDTO userDTO, ServingInfo servingInfo, ServingModelConfig servingModelConfig) {
        ModelServingBO bo = new ModelServingBO();
        //容器端口
        if (ServingTypeEnum.GRPC.getType().equals(servingInfo.getType())) {
            bo.setGrpcPort(ServingConstant.POD_GRPC_PORT);
        } else {
            bo.setHttpPort(ServingConstant.POD_HTTP_PORT);
        }
        //推理脚本名称，不同协议采用不同的推理脚本
        String scriptName = ServingConstant.HTTP_SCRIPT;
        if (ServingTypeEnum.GRPC.getType().equals(servingInfo.getType())) {
            scriptName = ServingConstant.GRPC_SCRIPT;
        }

        // 构建参数
        String command = String.format(ServingConstant.SERVING_COMMAND, ServingConstant.TS_SERVING_PATH, scriptName, ServingFrameTypeEnum.getFrameName(servingModelConfig.getFrameType()),
                ServingConstant.MODEL_PATH, ResourcesPoolTypeEnum.isGpuCode(servingModelConfig.getResourcesPoolType()) ? TRUE : FALSE) + servingModelConfig.getDeployParam();
        String resourceInfo = StringUtils.getRandomString() + servingModelConfig.getId();
        servingModelConfig.setResourceInfo(resourceInfo);
        bo.setNamespace(k8sUtil.getNamespace(userDTO))
                .setResourceName(k8sUtil.getResourceName(resourceInfo))
                .setReplicas(servingModelConfig.getResourcesPoolNode())
                .setGpuNum(servingModelConfig.getGpuNum())
                .setCpuNum(servingModelConfig.getCpuNum())
                .setMemNum(servingModelConfig.getMemNum())
                .setImage(ResourcesPoolTypeEnum.isGpuCode(servingModelConfig.getResourcesPoolType()) ? gpuImage : cpuImage)
                .setCmdLines(Arrays.asList("-c", command));
        bo.setNfsMounts(new HashMap<String, PtMountDirBO>(NumberConstant.NUMBER_4) {{
            put(ServingConstant.MODEL_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(servingModelConfig.getModelAddress())));
            put(ServingConstant.TS_SERVING_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(sourcePath)));
        }});
        bo.setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.SERVING));
        return bo;
    }

    /**
     * 删除pod
     *
     * @param userDTO         用户信息
     * @param servingInfo     在线服务信息
     * @param modelConfigList 在线服务模型部署信息集合
     */
    @Async("servingExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void deleteServing(UserDTO userDTO, ServingInfo servingInfo, List<ServingModelConfig> modelConfigList) {
        boolean flag = true;
        for (ServingModelConfig servingModelConfig : modelConfigList) {
            String namespace = k8sUtil.getNamespace(userDTO);
            String resourceName = k8sUtil.getResourceName(servingModelConfig.getResourceInfo());
            PtBaseResult ptBaseResult = modelServingApi.delete(namespace, resourceName);
            if (!ServingConstant.SUCCESS_CODE.equals(ptBaseResult.getCode())) {
                flag = false;
            }
        }
        if (!flag) {
            servingInfo.setStatus(ServingStatusEnum.EXCEPTION.getStatus());
            LogUtil.error(LogEnum.SERVING, "An Exception occurred when user {} stopping the service, service name:{}", userDTO.getUsername(), servingInfo.getName());
        }
        LogUtil.info(LogEnum.SERVING, "User {} stopped the service with SUCCESS, service name:{}", userDTO.getUsername(), servingInfo.getName());
        //grpc协议关闭对应通道
        if (ServingTypeEnum.GRPC.getType().equals(servingInfo.getType())) {
            GrpcClient.shutdownChannel(servingInfo.getId(), userDTO);
        }
        int result = servingInfoMapper.updateById(servingInfo);
        if (result < NumberConstant.NUMBER_1) {
            LogUtil.error(LogEnum.SERVING, "User {} FAILED stopping the online service. Database update FAILED. Service id={}, service name:{}，service status:{}", userDTO.getUsername(), servingInfo.getId(), servingInfo.getName(), servingInfo.getStatus());
            throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 异步部署批量服务
     *
     * @param userDTO      用户信息
     * @param batchServing 批量服务信息
     */
    @Async("servingExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void deployBatchServing(UserDTO userDTO, BatchServing batchServing) {
        if (batchServing.getResourcesPoolNode() == NumberConstant.NUMBER_1) {
            //单节点
            PtJupyterJobBO ptJupyterJobBO = buildJobBo(userDTO, batchServing);
            if (ptJupyterJobBO != null) {
                PtJupyterJobVO vo = trainJobApi.create(ptJupyterJobBO);
                if (vo.isSuccess()) {
                    LogUtil.info(LogEnum.SERVING, "User {} deployed batching service with SUCCESS. Service name：{}", userDTO.getUsername(), batchServing.getName());
                    batchServingMapper.updateById(batchServing);
                    return;
                }
            }

        } else {
            //多节点分布式
            DistributeTrainBO distributeTrainBO = buildDistributeTrainBO(userDTO, batchServing);
            if (distributeTrainBO != null) {
                BizDistributeTrain distribute = distributeTrainApi.create(distributeTrainBO);
                if (distribute.isSuccess()) {
                    LogUtil.info(LogEnum.SERVING, "User {} deployed batching service with SUCCESS. Service name：{}", userDTO.getUsername(), batchServing.getName());
                    batchServingMapper.updateById(batchServing);
                    return;
                }
            }
        }
        batchServing.setStatus(ServingStatusEnum.EXCEPTION.getStatus());
        batchServingMapper.updateById(batchServing);
        LogUtil.error(LogEnum.SERVING, "User {} FAILED deployed batching service. Service name：{}", userDTO.getUsername(), batchServing.getName());
    }

    /**
     * 异步删除批量服务
     *
     * @param userDTO      用户信息
     * @param batchServing 批量服务信息
     */
    @Async("servingExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchServing(UserDTO userDTO, BatchServing batchServing) {
        String namespace = k8sUtil.getNamespace(userDTO);
        String resourceName = k8sUtil.getBatchResourceName(batchServing.getResourceInfo());
        if (batchServing.getResourcesPoolNode() == NumberConstant.NUMBER_1) {
            if (trainJobApi.delete(namespace, resourceName)) {
                return;
            }
        } else {
            PtBaseResult result = distributeTrainApi.deleteByResourceName(namespace, resourceName);
            if (ServingConstant.SUCCESS_CODE.equals(result.getCode())) {
                return;
            }
        }
        batchServing.setStatus(ServingStatusEnum.EXCEPTION.getStatus());
        batchServingMapper.updateById(batchServing);
        LogUtil.error(LogEnum.SERVING, "User {} FAILED deployed batching service. Service name：{}", userDTO.getUsername(), batchServing.getName());
    }

    /**
     * 构建单节点推理参数
     *
     * @param userDTO      用户信息
     * @param batchServing 批量服务信息
     * @return PtJupyterJobBO 返回构建后对象
     */
    public PtJupyterJobBO buildJobBo(UserDTO userDTO, BatchServing batchServing) {
        String command = String.format(ServingConstant.BATCH_COMMAND, ServingConstant.TS_SERVING_PATH, ServingFrameTypeEnum.getFrameName(batchServing.getFrameType()),
                ServingConstant.MODEL_PATH, ServingConstant.INPUT_PATH, ServingConstant.OUTPUT_PATH, FALSE, ResourcesPoolTypeEnum.isGpuCode(batchServing.getResourcesPoolType()) ? TRUE : FALSE) + batchServing.getDeployParam();
        String resourceInfo = StringUtils.getRandomString() + batchServing.getId();
        batchServing.setResourceInfo(resourceInfo);
        PtJupyterJobBO bo = new PtJupyterJobBO()
                .setNamespace(k8sUtil.getNamespace(userDTO))
                .setName(k8sUtil.getBatchResourceName(resourceInfo))
                .setCpuNum(batchServing.getCpuNum())
                .setGpuNum(batchServing.getGpuNum())
                .setUseGpu(ResourcesPoolTypeEnum.isGpuCode(batchServing.getResourcesPoolType()))
                .setMemNum(batchServing.getMemNum())
                .setCmdLines(Arrays.asList("-c", command))
                .setNfsMounts(new HashMap<String, PtMountDirBO>(NumberConstant.NUMBER_6) {{
                    put(ServingConstant.MODEL_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(batchServing.getModelAddress())));
                    put(ServingConstant.INPUT_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(batchServing.getInputPath())));
                    put(ServingConstant.OUTPUT_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(batchServing.getOutputPath())));
                    put(ServingConstant.TS_SERVING_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(sourcePath)));
                }})
                .setImage(ResourcesPoolTypeEnum.isGpuCode(batchServing.getResourcesPoolType()) ? gpuImage : cpuImage)
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.BATCH_SERVING));
        return bo;
    }

    /**
     * 构建分布式推理参数
     *
     * @param userDTO      用户信息
     * @param batchServing 批量服务信息
     * @return DistributeTrainBO 返回构建后对象
     */
    public DistributeTrainBO buildDistributeTrainBO(UserDTO userDTO, BatchServing batchServing) {
        String command = String.format(ServingConstant.BATCH_COMMAND, ServingConstant.TS_SERVING_PATH, ServingFrameTypeEnum.getFrameName(batchServing.getFrameType()),
                ServingConstant.MODEL_PATH, ServingConstant.INPUT_PATH, ServingConstant.OUTPUT_PATH, TRUE, ResourcesPoolTypeEnum.isGpuCode(batchServing.getResourcesPoolType()) ? TRUE : FALSE) + batchServing.getDeployParam();
        String resourceInfo = StringUtils.getRandomString() + batchServing.getId();
        batchServing.setResourceInfo(resourceInfo);
        DistributeTrainBO bo = new DistributeTrainBO()
                .setName(k8sUtil.getBatchResourceName(resourceInfo))
                .setNamespace(k8sUtil.getNamespace(userDTO))
                .setSize(batchServing.getResourcesPoolNode())
                .setImage(ResourcesPoolTypeEnum.isGpuCode(batchServing.getResourcesPoolType()) ? gpuImage : cpuImage)
                .setMemNum(batchServing.getMemNum())
                .setCpuNum(batchServing.getCpuNum())
                .setGpuNum(batchServing.getGpuNum())
                .setMasterCmd(command)
                .setSlaveCmd(command)
                .setBusinessLabel(k8sNameTool.getPodLabel(BizEnum.BATCH_SERVING));
        bo.setNfsMounts(new HashMap<String, PtMountDirBO>(NumberConstant.NUMBER_6) {{
            put(ServingConstant.MODEL_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(batchServing.getModelAddress())));
            put(ServingConstant.INPUT_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(batchServing.getInputPath())));
            put(ServingConstant.OUTPUT_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(batchServing.getOutputPath())));
            put(ServingConstant.TS_SERVING_PATH, new PtMountDirBO(k8sUtil.getAbsoluteNfsPath(sourcePath)));
        }});
        return bo;
    }

    /**
     * 批量服务推理成功异步发送邮件通知
     *
     * @param receiverMailAddress 邮件地址
     * @param id                  批量服务ID
     */
    @Async("servingExecutor")
    public void asyncSendServingMail(String receiverMailAddress, Long id) {
        try {
            final StringBuffer sb = new StringBuffer();
            sb.append("<h2>" + "亲爱的").append(receiverMailAddress).append("您好！</h2>");
            sb.append("<p style='text-align: center; font-size: 24px; font-weight: bold'>ID为：" + id + "的批量服务推理成功，请及时查看！</p>");
            mailService.sendHtmlMail(receiverMailAddress, "推理成功通知", sb.toString());
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING, "UserServiceImpl sendMail error , param:{} error:{}", receiverMailAddress, e);
            throw new BusinessException(BaseErrorCode.ERROR_SYSTEM.getCode(), BaseErrorCode.ERROR_SYSTEM.getMsg());
        }
    }
}
