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

package org.dubhe.terminal.service.impl;
import java.sql.Timestamp;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.dockerjava.api.DockerClient;
import com.google.common.collect.Maps;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.docker.api.DockerApi;
import org.dubhe.docker.config.DockerClientFactory;
import org.dubhe.docker.enums.DockerOperationEnum;
import org.dubhe.docker.utils.DockerCallbackTool;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.TerminalApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.bo.TerminalBO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizServicePort;
import org.dubhe.k8s.domain.vo.TerminalResourceVO;
import org.dubhe.k8s.enums.BusinessLabelServiceNameEnum;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.docker.callback.TerminalPushImageResultCallback;
import org.dubhe.terminal.config.TerminalConfig;
import org.dubhe.terminal.constant.TerminalConstant;
import org.dubhe.terminal.dao.PtImageMapper;
import org.dubhe.terminal.dao.TerminalInfoMapper;
import org.dubhe.terminal.dao.TerminalMapper;
import org.dubhe.terminal.domain.dto.TerminalCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalDTO;
import org.dubhe.terminal.domain.dto.TerminalK8sPodCallbackCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalPreserveDTO;
import org.dubhe.terminal.domain.entity.PtImage;
import org.dubhe.terminal.domain.entity.Terminal;
import org.dubhe.terminal.domain.entity.TerminalInfo;
import org.dubhe.terminal.domain.vo.TerminalVO;
import org.dubhe.terminal.enums.TerminalInfoStatusEnum;
import org.dubhe.terminal.enums.TerminalStatusEnum;
import org.dubhe.terminal.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description 专业版终端实现
 * @date 2021-07-12
 */
@Service
public class TerminalServiceImpl implements TerminalService {
    @Autowired
    @Qualifier("hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private TerminalConfig terminalConfig;

    @Autowired
    private TerminalApi terminalApi;

    @Autowired
    private PodApi podApi;

    @Autowired
    private DockerApi dockerApi;

    @Autowired
    private DockerClientFactory dockerClientFactory;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private TerminalMapper terminalMapper;

    @Autowired
    private PtImageMapper ptImageMapper;

    @Autowired
    private TerminalInfoMapper terminalInfoMapper;

    @Autowired
    private DockerCallbackTool dockerCallbackTool;

    @Autowired
    private ResourceCache resourceCache;

    @Autowired
    private RedisUtils redisUtils;

    @Value("Task:Terminal:"+"${spring.profiles.active}_terminal_id_")
    private String terminalIdPrefix;

    private static final String CONN = "Conn";

    /**
     * 创建
     *
     * @param terminalCreateDTO
     * @return TerminalVO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TerminalVO create(TerminalCreateDTO terminalCreateDTO) {
        try{
            return start(terminalCreateDTO);
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"create error : {}",e.getMessage(),e);
            throw new BusinessException("内部错误:"+e.getMessage());
        }
    }

    /**
     * 重启
     *
     * @param terminalCreateDTO
     * @return TerminalVO
     */
    @Override
    public TerminalVO restart(TerminalCreateDTO terminalCreateDTO) {
        try{
            if (terminalCreateDTO == null || terminalCreateDTO.getId() == null){
                LogUtil.error(LogEnum.TERMINAL,"restart error : {}",terminalCreateDTO);
                throw new BusinessException("缺少id");
            }
            return start(terminalCreateDTO);
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"restart error : {}",e.getMessage(),e);
            throw new BusinessException("内部错误:"+e.getMessage());
        }
    }

    @Override
    public boolean preserve(TerminalPreserveDTO terminalPreserveDTO) {
        Terminal terminal = terminalMapper.selectById(terminalPreserveDTO.getId());
        if (terminal == null){
            LogUtil.error(LogEnum.TERMINAL,"preserve terminal 数据不存在 terminalPreserveDTO: {}",terminalPreserveDTO);
            throw new BusinessException("数据不存在");
        }

        LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TerminalInfo::getTerminalId, terminalPreserveDTO.getId());
        List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(terminalInfoList)){
            LogUtil.error(LogEnum.TERMINAL,"preserve terminalInfoList 数据不存在 terminalPreserveDTO: {}",terminalPreserveDTO);
            throw new BusinessException("数据不存在");
        }

        TerminalInfo masterTerminalInfo = null;
        for (TerminalInfo terminalInfo : terminalInfoList) {
            if (terminalInfo.isMasterFlag()){
                masterTerminalInfo = terminalInfo;
            }
        }
        if (masterTerminalInfo == null){
            LogUtil.error(LogEnum.TERMINAL,"master 节点不存在 terminalPreserveDTO:{}",terminalPreserveDTO);
            throw new BusinessException("master 节点不存在");
        }

        BizPod pod = podApi.getWithResourceName(k8sNameTool.getNamespace(terminal.getCreateUserId()),masterTerminalInfo.getK8sResourceName());
        if (pod == null){
            LogUtil.error(LogEnum.TERMINAL,"master 容器不存在 terminalPreserveDTO:{}",terminalPreserveDTO);
            throw new BusinessException("master 容器不存在");
        }
        if (!PodPhaseEnum.RUNNING.getPhase().equals(pod.getPhase()) || pod.getPodIp() == null || StringUtils.isNotEmpty(pod.getContainerStateMessages())){
            LogUtil.error(LogEnum.TERMINAL,"master 容器未运行 terminalPreserveDTO:{}",terminalPreserveDTO);
            throw new BusinessException("master 容器未运行");
        }
        String containerID = pod.getContainerId();
        if (StringUtils.isEmpty(containerID)){
            LogUtil.error(LogEnum.TERMINAL,"master 容器未运行 terminalPreserveDTO:{}",terminalPreserveDTO);
            throw new BusinessException("master 容器未运行");
        }
        terminal.setStatus(TerminalStatusEnum.SAVING.getCode());
        terminal.putStatusDetail(TerminalStatusEnum.SAVING.getDescription(),"commit 镜像...");
        terminalMapper.updateById(terminal);
        DockerClient dockerClient = dockerClientFactory.getDockerClient(pod.getHostIP());
        String newImagePath = terminal.getImageProject()+SymbolConstant.SLASH+terminal.getCreateUserId()+SymbolConstant.SLASH+terminalPreserveDTO.getImageName();
        String newImageRepository = terminalConfig.getHarborAddress()+SymbolConstant.SLASH+newImagePath;
        try {
            dockerApi.commit(dockerClient,containerID,newImageRepository,terminalPreserveDTO.getImageTag());
            terminal.setStatus(null);
            terminal.putStatusDetail(TerminalStatusEnum.SAVING.getDescription(),"push 镜像...");
            terminalMapper.updateById(terminal);
            boolean pushResult = dockerApi.push(dockerClient,newImageRepository+SymbolConstant.COLON+terminalPreserveDTO.getImageTag(),new TerminalPushImageResultCallback(dockerCallbackTool.getCallbackUrl(SymbolConstant.LOCAL_HOST,terminalConfig.getServerPort(), DockerOperationEnum.PUSH.getType()),terminal.getId(),dockerClient,terminal.getCreateUserId()));
            if (!pushResult){
                LogUtil.error(LogEnum.TERMINAL,"master 推送镜像错误 terminalPreserveDTO:{}",terminalPreserveDTO);
                throw new BusinessException("推送镜像错误:");
            }
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"master 保存容器错误:{}",e.getMessage(),e);
            throw new BusinessException("保存容器错误:"+e.getMessage());
        }

        terminal.setImageUrl(newImagePath+SymbolConstant.COLON+terminalPreserveDTO.getImageTag());
        terminal.setImageName(terminalPreserveDTO.getImageName());
        terminal.setDescription(terminalPreserveDTO.getImageRemark());
        terminal.setImageTag(terminalPreserveDTO.getImageTag());
        terminal.setLastStopTime(new Date());
        terminal.setUpdateInfo(userContextService.getCurUserId());

        return terminalMapper.updateById(terminal) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(TerminalDTO terminalDTO) {
        try{
            Terminal terminal = terminalMapper.selectById(terminalDTO.getId());
            if (terminal == null){
                LogUtil.error(LogEnum.TERMINAL,"delete 数据不存在 terminalDTO: {}",terminalDTO);
                throw new BusinessException("数据不存在");
            }
            terminal.setDeleted(true);
            terminal.setLastStopTime(new Date());
            terminal.setStatus(TerminalStatusEnum.DELETED.getCode());
            terminalMapper.deleteById(terminal);
            // 删除任务缓存
            String taskIdentify = (String) redisUtils.get(terminalIdPrefix + String.valueOf(terminal.getId()));
            if (StringUtils.isNotEmpty(taskIdentify)){
                redisUtils.del(taskIdentify, terminalIdPrefix + String.valueOf(terminal.getId()));
            }
            String namespace = k8sNameTool.getNamespace(terminal.getCreateUserId());

            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getTerminalId, terminalDTO.getId());
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);

            if (!CollectionUtils.isEmpty(terminalInfoList)){
                for (TerminalInfo terminalInfo : terminalInfoList) {
                    terminalApi.delete(namespace,terminalInfo.getK8sResourceName());
                    terminalInfo.setDeleted(true);
                    terminalInfoMapper.deleteById(terminalInfo);
                }
            }
            return true;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"detail error : {}",e.getMessage(),e);
            throw new BusinessException("内部错误:"+e.getMessage());
        }
    }

    /**
     * 查询详情
     *
     * @param terminalDTO
     * @return
     */
    @Override
    public TerminalVO detail(TerminalDTO terminalDTO) {
        try{
            Terminal terminal = terminalMapper.selectById(terminalDTO.getId());
            if (terminal == null){
                LogUtil.error(LogEnum.TERMINAL,"detail 数据不存在 terminalDTO: {}",terminalDTO);
                throw new BusinessException("数据不存在");
            }

            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getTerminalId, terminalDTO.getId());
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);

            return new TerminalVO(terminal,terminalInfoList);
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"detail error : {}",e.getMessage(),e);
            throw new BusinessException("内部错误:"+e.getMessage());
        }
    }

    /**
     * 查询列表
     *
     * @return
     */
    @Override
    public List<TerminalVO> list() {
        try{
            List<TerminalVO> terminalVOList = new ArrayList<>();
            List<Terminal> terminalInfoList = new ArrayList<>();

            UserContext user = userContextService.getCurUser();
            if (!BaseService.isAdmin(user)){
                LambdaQueryWrapper<Terminal> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Terminal::getCreateUserId, userContextService.getCurUserId());
                terminalInfoList = terminalMapper.selectList(wrapper);
            }else {
                terminalInfoList = terminalMapper.selectList(null);
            }

            if (CollectionUtils.isEmpty(terminalInfoList)){
                return terminalVOList;
            }

            for (Terminal terminal : terminalInfoList) {
                terminalVOList.add(detail(new TerminalDTO(terminal.getId())));
            }
            return terminalVOList;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"list error : {}",e.getMessage(),e);
            throw new BusinessException("内部错误:"+e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TerminalInfo refreshTerminalInfoStatus(Long terminalInfoId) {
        try {
            if (terminalInfoId == null){
                return null;
            }
            TerminalInfo terminalInfo = terminalInfoMapper.selectById(terminalInfoId);
            if (terminalInfo == null){
                LogUtil.error(LogEnum.TERMINAL,"refreshTerminalInfoStatus no terminalInfo found terminalInfoId:{}",terminalInfoId);
                return null;
            }

            String namespace = k8sNameTool.getNamespace(terminalInfo.getCreateUserId());

            TerminalResourceVO terminalResourceVO = terminalApi.get(namespace,terminalInfo.getK8sResourceName());
            if (terminalResourceVO != null && terminalResourceVO.getBizService() != null){
                BizServicePort bizServicePort = terminalResourceVO.getBizService().getServicePortByTargetPort(MagicNumConstant.TWENTY_TWO);
                if (bizServicePort != null){
                    terminalInfo.setSshPort(bizServicePort.getNodePort());
                }
            }
            BizPod pod = podApi.getWithResourceName(namespace,terminalInfo.getK8sResourceName());
            if (pod != null){
                terminalInfo.setPodIp(pod.getPodIp());
            }
            if (terminalInfo.getSshPort() != null){
                if (StringUtils.isNotEmpty(terminalInfo.getSshUser())){
                    terminalInfo.setSsh(StrUtil.format(TerminalConstant.SSH_USER_COMMAND,terminalInfo.getSshPort(),terminalInfo.getSshUser(),terminalConfig.getSshHost()));
                }else {
                    terminalInfo.setSsh(StrUtil.format(TerminalConstant.SSH_COMMAND,terminalInfo.getSshPort(),terminalConfig.getSshHost()));
                }
            }
            terminalInfo.setStatus(TerminalInfoStatusEnum.getCode(pod.getRealPodPhase()));
            terminalInfo.setUpdateInfo(userContextService.getCurUserId());
            terminalInfoMapper.updateById(terminalInfo);
            return terminalInfo;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"refreshStatus error : {}",e.getMessage(),e);
            return null;
        }
    }

    @Override
    public Terminal refreshTerminalStatus(Long id) {
        try {
            if (id == null){
                return null;
            }
            Terminal terminal = terminalMapper.selectById(id);
            if (terminal == null){
                LogUtil.error(LogEnum.TERMINAL,"refreshTerminalStatus no terminal found id:{}",id);
                return null;
            }

            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getTerminalId, terminal.getId());
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);

            Integer runningNode = 0;
            for (TerminalInfo terminalInfo : terminalInfoList) {
                TerminalInfo refreshTerminalInfo = refreshTerminalInfoStatus(terminalInfo.getId());
                if (refreshTerminalInfo != null && TerminalInfoStatusEnum.RUNNING.getCode().equals(refreshTerminalInfo.getStatus())){
                    ++runningNode;
                }
            }
            terminal.setRunningNode(runningNode);
            terminal.setUpdateInfo(userContextService.getCurUserId());

            terminalMapper.updateById(terminal);
            return terminal;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"refreshStatus error : {}",e.getMessage(),e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean terminalPodCallback(int times, TerminalK8sPodCallbackCreateDTO req) {
        LogUtil.info(LogEnum.TERMINAL,"terminalPodCallback times:{} req:{}",times,req);
        try {
            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getK8sResourceName, req.getResourceName());
            TerminalInfo terminalInfo = terminalInfoMapper.selectOne(wrapper);
            if (terminalInfo == null){
                LogUtil.error(LogEnum.TERMINAL,"terminalPodCallback no terminalInfo found k8sResourceName:{}",req.getResourceName());
                return false;
            }

            //修改状态
            if (StringUtils.isEmpty(req.getMessages())){
                terminalInfo.removeStatusDetail(req.getResourceName());
            }else {
                terminalInfo.putStatusDetail(req.getResourceName(),req.getMessages());
            }
            terminalInfo.setStatus(TerminalInfoStatusEnum.getCode(req.getPhase()));
            terminalInfo.setUpdateInfo(userContextService.getCurUserId());
            terminalInfoMapper.updateById(terminalInfo);
            refreshTerminalInfoStatus(terminalInfo.getId());

            Terminal terminal = terminalMapper.selectById(terminalInfo.getTerminalId());
            if (TerminalInfoStatusEnum.RUNNING.getCode().equals(terminalInfo.getStatus()) && !TerminalInfoStatusEnum.RUNNING.getCode().equals(req.getPhase())){
                if (terminal.getRunningNode() > MagicNumConstant.ZERO){
                    terminal.setRunningNode(terminal.getRunningNode() - MagicNumConstant.ONE);
                }else {
                    terminal.setRunningNode(MagicNumConstant.ZERO);
                }
            }
            if (!TerminalInfoStatusEnum.RUNNING.getCode().equals(terminalInfo.getStatus()) && TerminalInfoStatusEnum.RUNNING.getCode().equals(req.getPhase())){
                terminal.setRunningNode(terminal.getRunningNode()+MagicNumConstant.ONE);
            }
            terminal.setUpdateInfo(userContextService.getCurUserId());
            //只修改节点运行计数，不修改状态
            terminal.setStatus(null);
            terminalMapper.updateById(terminal);
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"terminalPodCallback error : {}",e.getMessage(),e);
        }
        return true;
    }

    /**
     * 推送镜像完成
     *
     * @param terminalId
     */
    @Override
    public void pushImageComplete(Long terminalId,Long userId) {
        try{
            LogUtil.info(LogEnum.TERMINAL,"pushImageComplete id:{}",terminalId);
            if (terminalId == null){
                return;
            }
            Terminal terminal = terminalMapper.selectById(terminalId);
            if (terminal == null){
                LogUtil.error(LogEnum.TERMINAL,"pushImageComplete no terminal found id:{}",terminalId);
                return;
            }
            stop(userId,terminalId);
            if (TerminalStatusEnum.SAVING.getCode().equals(terminal.getStatus())){
                terminal.setStatus(TerminalStatusEnum.DELETED.getCode());
                terminal.removeStatusDetail(TerminalStatusEnum.SAVING.getDescription());
                terminalMapper.updateById(terminal);

                LambdaQueryWrapper<PtImage> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PtImage::getImageUrl, terminal.getImageUrl());
                PtImage ptImage = ptImageMapper.selectOne(wrapper);
                if (ptImage == null){
                    ptImage = new PtImage();
                }

                ptImage.setImageName(terminal.getImageName());
                ptImage.setImageUrl(terminal.getImageUrl());
                ptImage.setImageTag(terminal.getImageTag());
                ptImage.setRemark(terminal.getDescription());
                ptImage.setProjectName(terminal.getImageProject());
                ptImage.setImageResource(MagicNumConstant.ZERO);
                ptImage.setImageStatus(MagicNumConstant.ONE);
                ptImage.setDeleted(false);
                ptImage.setUpdateUserId(userId);
                ptImage.setUpdateTime(new Timestamp(new java.util.Date().getTime()));

                if (ptImage.getId() != null){
                    ptImageMapper.updateById(ptImage);
                }else {
                    ptImage.setOriginUserId(userId);
                    ptImage.setCreateUserId(userId);
                    ptImage.setCreateTime(new Timestamp(new java.util.Date().getTime()));
                    ptImageMapper.insert(ptImage);
                }
            }
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"pushImageComplete error : {}",e.getMessage(),e);
        }
    }

    /**
     * 推送镜像失败
     *
     * @param terminalId
     * @param message 失败信息
     */
    @Override
    public void pushImageError(Long terminalId, String message,Long userId) {
        try{
            LogUtil.info(LogEnum.TERMINAL,"pushImageError id:{}",terminalId);
            if (terminalId == null){
                return;
            }
            stop(userId,terminalId);

            Terminal terminal = terminalMapper.selectById(terminalId);
            if (terminal == null){
                LogUtil.error(LogEnum.TERMINAL,"pushImageError no terminal found id:{}",terminalId);
                return;
            }
            terminal.setStatus(TerminalStatusEnum.FAILED.getCode());
            terminal.putStatusDetail(TerminalStatusEnum.SAVING.getDescription(),message);
            terminalMapper.updateById(terminal);
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"pushImageError error : {}",e.getMessage(),e);
        }
    }

    /**
     * 构建TerminalBO
     *
     * @param terminalCreateDTO
     * @param terminalInfo
     * @param namespace 命名空间
     * @return TerminalBO
     */
    private TerminalBO buildTerminalBO(TerminalCreateDTO terminalCreateDTO,TerminalInfo terminalInfo,String namespace, String taskIdentifyLabel){
        TerminalBO terminalBO = new TerminalBO();
        terminalBO.setNamespace(namespace);
        terminalBO.setResourceName(terminalInfo.getK8sResourceName());
        terminalBO.setReplicas(1);
        terminalBO.setGpuNum(terminalInfo.getGpuNum());
        terminalBO.setMemNum(terminalInfo.getMemNum());
        terminalBO.setCpuNum(terminalInfo.getCpuNum());
        terminalBO.setImage(terminalConfig.getHarborAddress()+SymbolConstant.SLASH+terminalCreateDTO.getImageUrl());
        terminalBO.setFsMounts(Maps.newHashMap());
        terminalBO.setBusinessLabel(BusinessLabelServiceNameEnum.TERMINAL.getBusinessLabel());
        terminalBO.setTaskIdentifyLabel(taskIdentifyLabel);
        terminalBO.addPort(MagicNumConstant.TWENTY_TWO);
        terminalBO.addPorts(terminalCreateDTO.getPorts());
        terminalBO.setCmdLines(terminalCreateDTO.getCmdLines());
        if (terminalCreateDTO.getDataSourcePath() != null){
            String dataSetDir = fileStoreApi.getRootDir() + fileStoreApi.getBucket().substring(1)+terminalCreateDTO.getDataSourcePath();
            terminalBO.putfsMounts(TerminalConstant.DATASET_VOLUME_MOUNTS,new PtMountDirBO(dataSetDir,true));
        }
        if (terminalInfo.getDiskMemNum() != null){
            String workspaceDir = fileStoreApi.getRootDir() + fileStoreApi.getBucket().substring(1)+ terminalConfig.getTerminalDir()+SymbolConstant.SLASH+userContextService.getCurUserId()+SymbolConstant.SLASH+terminalConfig.getWorkspaceDir();
            terminalBO.putfsMounts(TerminalConstant.WORKSPACE_VOLUME_MOUNTS,new PtMountDirBO(workspaceDir,terminalInfo.getDiskMemNum()+ K8sParamConstants.MEM_UNIT,terminalInfo.getDiskMemNum()+ K8sParamConstants.MEM_UNIT,false));
        }
        return terminalBO;
    }

    /**
     * 部署服务
     *
     * @param terminalCreateDTO
     * @return
     */
    private TerminalVO start(TerminalCreateDTO terminalCreateDTO){
        try{
            LogUtil.info(LogEnum.BIZ_K8S, "TerminalService create terminalCreateDTO:{}", terminalCreateDTO);
            String k8sResourceName = k8sNameTool.generateResourceName(BizEnum.TERMINAL, RandomUtil.randomString(MagicNumConstant.FIVE));
            String namespace = "";
            String sshUser = terminalCreateDTO.getSshUser();
            String sshPassword = terminalCreateDTO.getSshPwd();

            if (terminalCreateDTO.getId() == null){
                namespace = k8sNameTool.getNamespace(userContextService.getCurUser());
            }else {
                Terminal oldTerminal = terminalMapper.selectById(terminalCreateDTO.getId());
                namespace = k8sNameTool.getNamespace(oldTerminal.getCreateUserId());
            }

            Terminal terminal = new Terminal();
            terminal.setId(terminalCreateDTO.getId());
            terminal.setName(StringUtils.isEmpty(terminalCreateDTO.getName())?CONN+ SymbolConstant.HYPHEN +k8sResourceName:terminalCreateDTO.getName());
            terminal.setImageName(terminalCreateDTO.getImageName());
            terminal.setImageTag(terminalCreateDTO.getImageTag());
            terminal.setImageUrl(terminalCreateDTO.getImageUrl());
            terminal.setDataSourceName(terminalCreateDTO.getDataSourceName());
            terminal.setDataSourcePath(terminalCreateDTO.getDataSourcePath());
            terminal.setTotalNode(terminalCreateDTO.getTotalNode());
            terminal.setDescription(terminalCreateDTO.getDescription());
            terminal.setSameInfo(terminalCreateDTO.isSameInfo());
            terminal.setLastStartTime(new Date());
            terminal.setStatus(TerminalStatusEnum.RUNNING.getCode());

            //terminal数据入库
            if (terminal.getId() == null){
                terminal.setOriginUserId(userContextService.getCurUserId());
                terminal.setCreateUserId(userContextService.getCurUserId());
                terminalMapper.insert(terminal);
            }else {
                terminal.setUpdateInfo(userContextService.getCurUserId());
                terminalMapper.updateById(terminal);
            }

            //复用ssh 用户名 密码
            if (terminal.getId() != null){
                List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectByTerminalId(terminal.getId());
                if (!CollectionUtils.isEmpty(terminalInfoList)){
                    sshUser = StringUtils.isEmpty(sshUser) ? terminalInfoList.get(0).getSshUser():sshUser;
                    sshPassword = StringUtils.isEmpty(sshPassword) ? terminalInfoList.get(0).getSshPassword():sshPassword;
                }
            }

            List<TerminalInfo> terminalInfoList = new ArrayList<>();

            if (CollectionUtils.isEmpty(terminalCreateDTO.getInfo())){
                LogUtil.error(LogEnum.TERMINAL,"start 未填写节点规格 terminalCreateDTO:{}",terminalCreateDTO);
                throw new BusinessException(ResponseCode.ERROR, "未填写节点规格!");
            }

            if (terminalCreateDTO.isSameInfo()){
                for (int i = 0;i < terminalCreateDTO.getTotalNode();i++){
                    //第一个默认为主节点
                    TerminalInfo terminalInfo = terminalCreateDTO.getInfo().get(0).toTerminalInfo(terminal.getId(),k8sResourceName+i,userContextService.getCurUserId(),sshUser,sshPassword);
                    if (i == 0){
                        terminalInfo.setMasterFlag(true);
                    }
                    terminalInfoList.add(terminalInfo);
                }
            }else {
                for (int i = 0;i < terminalCreateDTO.getInfo().size();i++){
                    //第一个默认为主节点
                    TerminalInfo terminalInfo = terminalCreateDTO.getInfo().get(i).toTerminalInfo(terminal.getId(),k8sResourceName+i,userContextService.getCurUserId(),sshUser,sshPassword);
                    if (i == 0){
                        terminalInfo.setMasterFlag(true);
                    }
                    terminalInfoList.add(terminalInfo);
                }
            }
            terminalInfoMapper.deleteByTerminalId(terminal.getId());
            //terminalInfo 数据入库
            for (TerminalInfo terminalInfo : terminalInfoList) {
                if (terminalInfo.getId() == null){
                    terminalInfo.setCreateUserId(userContextService.getCurUserId());
                    terminalInfoMapper.insert(terminalInfo);
                }else {
                    terminalInfo.setUpdateInfo(userContextService.getCurUserId());
                    terminalInfoMapper.updateById(terminalInfo);
                }
            }
            //获取任务识别标识
            String taskIdentify = resourceCache.getTaskIdentify(terminal.getId(),terminal.getName(),terminalIdPrefix);
            //启动k8s服务
            for (TerminalInfo terminalInfo : terminalInfoList) {
                TerminalBO terminalBO = buildTerminalBO(terminalCreateDTO,terminalInfo,namespace, taskIdentify);
                terminalApi.create(terminalBO);
            }

            return detail(new TerminalDTO(terminal.getId()));
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"create error : {}",e.getMessage(),e);
            throw new BusinessException("内部错误:"+e.getMessage());
        }
    }

    /**
     * 停止服务
     *
     * @param userId 用户id
     * @param terminalId
     */
    private void stop(Long userId,Long terminalId){
        try{
            LogUtil.info(LogEnum.TERMINAL, "TerminalService stop userId {} terminalId {}", userId,terminalId);
            if (terminalId == null){
                return;
            }
            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getTerminalId, terminalId);
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);

            String namespace = k8sNameTool.getNamespace(userId);

            if (!CollectionUtils.isEmpty(terminalInfoList)){
                for (TerminalInfo terminalInfo : terminalInfoList) {
                    terminalApi.delete(namespace,terminalInfo.getK8sResourceName());
                }
            }
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"stop error : {}",e.getMessage(),e);
        }
    }
}
