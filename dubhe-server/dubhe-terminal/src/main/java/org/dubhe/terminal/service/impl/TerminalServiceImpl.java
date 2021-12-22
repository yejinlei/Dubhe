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

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.dockerjava.api.DockerClient;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.Pod;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.cloud.authconfig.service.AdminClient;
import org.dubhe.docker.config.DockerClientFactory;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.ServiceApi;
import org.dubhe.k8s.api.TerminalApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.dao.K8sTaskIdentifyMapper;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.bo.TerminalBO;
import org.dubhe.k8s.domain.entity.K8sTaskIdentify;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizService;
import org.dubhe.k8s.domain.resource.BizServicePort;
import org.dubhe.k8s.domain.vo.K8sEventVO;
import org.dubhe.k8s.domain.vo.TerminalResourceVO;
import org.dubhe.k8s.enums.BusinessLabelServiceNameEnum;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.service.K8sCallbackEventService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.terminal.async.PreserveTerminalAsync;
import org.dubhe.terminal.config.TerminalConfig;
import org.dubhe.terminal.constant.TerminalConstant;
import org.dubhe.terminal.dao.PtImageMapper;
import org.dubhe.terminal.dao.TerminalInfoMapper;
import org.dubhe.terminal.dao.TerminalMapper;
import org.dubhe.terminal.domain.dto.TerminalCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalDTO;
import org.dubhe.terminal.domain.dto.TerminalDetailDTO;
import org.dubhe.terminal.domain.dto.TerminalK8sPodCallbackCreateDTO;
import org.dubhe.terminal.domain.dto.TerminalPreserveDTO;
import org.dubhe.terminal.domain.entity.PtImage;
import org.dubhe.terminal.domain.entity.Terminal;
import org.dubhe.terminal.domain.entity.TerminalInfo;
import org.dubhe.terminal.domain.vo.TerminalVO;
import org.dubhe.terminal.enums.TerminalInfoStatusEnum;
import org.dubhe.terminal.enums.TerminalStatusEnum;
import org.dubhe.terminal.service.TerminalInfoService;
import org.dubhe.terminal.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description 专业版终端实现
 * @date 2021-07-12
 */
@Service
public class TerminalServiceImpl extends ServiceImpl<TerminalMapper,Terminal> implements TerminalService {
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
    private K8sTaskIdentifyMapper k8sTaskIdentifyMapper;

    @Autowired
    private AdminClient adminClient;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private PreserveTerminalAsync preserveTerminalAsync;

    @Autowired
    private ServiceApi serviceApi;

    @Autowired
    private TerminalInfoService terminalInfoService;

    @Resource
    private K8sCallbackEventService k8sCallbackEventService;

    @Value("Task:Terminal:" + "${spring.profiles.active}_image_remark_")
    private String imageRemarkPrefix;

    private static final String CONN = "Conn";

    /**
     * 创建
     *
     * @param terminalCreateDTO
     * @return TerminalVO
     */
    @Override
    public TerminalVO create(TerminalCreateDTO terminalCreateDTO) {
        try {
            return start(terminalCreateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "create error : {}", e.getMessage(), e);
            throw new BusinessException("内部错误:" + e.getMessage());
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
        try {
            if (terminalCreateDTO == null || terminalCreateDTO.getId() == null) {
                LogUtil.error(LogEnum.TERMINAL, "restart error : {}", terminalCreateDTO);
                throw new BusinessException("缺少id");
            }
            return start(terminalCreateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "restart error : {}", e.getMessage(), e);
            throw new BusinessException("内部错误:" + e.getMessage());
        }
    }

    @Override
    public boolean preserve(TerminalPreserveDTO terminalPreserveDTO) {
        LogUtil.info(LogEnum.TERMINAL, "preserve terminal terminalPreserveDTO: {}", terminalPreserveDTO);
        Terminal terminal = terminalMapper.selectById(terminalPreserveDTO.getId());
        if (terminal == null) {
            LogUtil.error(LogEnum.TERMINAL, "preserve terminal 数据不存在 terminalPreserveDTO: {}", terminalPreserveDTO);
            throw new BusinessException("数据不存在");
        }

        LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TerminalInfo::getTerminalId, terminalPreserveDTO.getId());
        List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(terminalInfoList)) {
            LogUtil.error(LogEnum.TERMINAL, "preserve terminalInfoList 数据不存在 terminalPreserveDTO: {}", terminalPreserveDTO);
            throw new BusinessException("数据不存在");
        }

        TerminalInfo masterTerminalInfo = null;
        for (TerminalInfo terminalInfo : terminalInfoList) {
            if (terminalInfo.isMasterFlag()) {
                masterTerminalInfo = terminalInfo;
            }
        }
        if (masterTerminalInfo == null) {
            LogUtil.error(LogEnum.TERMINAL, "master 节点不存在 terminalPreserveDTO:{}", terminalPreserveDTO);
            throw new BusinessException("master 节点不存在");
        }

        BizPod pod = podApi.getWithResourceName(k8sNameTool.getNamespace(terminal.getCreateUserId()), masterTerminalInfo.getK8sResourceName());
        if (pod == null) {
            LogUtil.error(LogEnum.TERMINAL, "master 容器不存在 terminalPreserveDTO:{}", terminalPreserveDTO);
            throw new BusinessException("master 容器不存在");
        }
        if (!PodPhaseEnum.RUNNING.getPhase().equals(pod.getPhase()) || pod.getPodIp() == null || StringUtils.isNotEmpty(pod.getContainerStateMessages())) {
            LogUtil.error(LogEnum.TERMINAL, "master 容器未运行 terminalPreserveDTO:{}", terminalPreserveDTO);
            throw new BusinessException("master 容器未运行");
        }
        String containerID = pod.getContainerId();
        if (StringUtils.isEmpty(containerID)) {
            LogUtil.error(LogEnum.TERMINAL, "master 容器未运行 terminalPreserveDTO:{}", terminalPreserveDTO);
            throw new BusinessException("master 容器未运行");
        }
        terminal.setStatus(TerminalStatusEnum.SAVING.getCode());
        terminal.putStatusDetail(TerminalStatusEnum.SAVING.getDescription(), "commit 镜像...");
        terminalMapper.updateById(terminal);
        DockerClient dockerClient = dockerClientFactory.getDockerClient(pod.getHostIP());
        String newImagePath = terminal.getImageProject() + SymbolConstant.SLASH + terminal.getCreateUserId() + SymbolConstant.SLASH + terminalPreserveDTO.getImageName();
        String newImageRepository = terminalConfig.getHarborAddress() + SymbolConstant.SLASH + newImagePath;

        preserveTerminalAsync.commitAndPush(dockerClient, containerID, newImageRepository, terminalPreserveDTO, terminal);

        terminal.setImageUrl(newImagePath + SymbolConstant.COLON + terminalPreserveDTO.getImageTag());
        terminal.setImageName(terminalPreserveDTO.getImageName());
        //避免镜像描述占用description字段
        redisUtils.set(imageRemarkPrefix + terminal.getId(), terminalPreserveDTO.getImageRemark());
        terminal.setImageTag(terminalPreserveDTO.getImageTag());
        terminal.setLastStopTime(new Date());
        terminal.setUpdateInfo(userContextService.getCurUserId());

        return terminalMapper.updateById(terminal) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(TerminalDTO terminalDTO) {
        try {
            Terminal terminal = terminalMapper.selectById(terminalDTO.getId());
            if (terminal == null) {
                LogUtil.error(LogEnum.TERMINAL, "delete 数据不存在 terminalDTO: {}", terminalDTO);
                throw new BusinessException("数据不存在");
            }
            terminal.setDeleted(true);
            terminal.setLastStopTime(new Date());
            terminal.setStatus(TerminalStatusEnum.DELETED.getCode());
            terminalMapper.deleteById(terminal);
            //删除任务缓存
            k8sTaskIdentifyMapper.deleteByTaskId(terminal.getId());

            String namespace = k8sNameTool.getNamespace(terminal.getCreateUserId());

            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getTerminalId, terminalDTO.getId());
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);

            if (!CollectionUtils.isEmpty(terminalInfoList)) {
                for (TerminalInfo terminalInfo : terminalInfoList) {
                    PtBaseResult deleteResult= terminalApi.delete(namespace,terminalInfo.getK8sResourceName());
                    if (!deleteResult.isSuccess()){
                        LogUtil.error(LogEnum.TERMINAL,"create error : {}",deleteResult.getMessage());
                        throw new BusinessException("内部错误:"+deleteResult.getMessage());
                    }
                    terminalInfo.setDeleted(true);
                    terminalInfoMapper.deleteById(terminalInfo);
                    //删除关联的事件信息
                    k8sCallbackEventService.delete(terminalInfo.getK8sResourceName(), BizEnum.TERMINAL.getBizCode());
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "detail error : {}", e.getMessage(), e);
            throw new BusinessException("内部错误:" + e.getMessage());
        }
    }

    /**
     * 查询详情
     *
     * @param terminalDTO
     * @return
     */
    @Override
    public TerminalVO detail(TerminalDTO terminalDTO,boolean enableUsername) {
        try {
            Terminal terminal = terminalMapper.selectById(terminalDTO.getId());
            if (terminal == null) {
                LogUtil.error(LogEnum.TERMINAL, "detail 数据不存在 terminalDTO: {}", terminalDTO);
                throw new BusinessException("数据不存在");
            }

            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getTerminalId, terminalDTO.getId());
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);

            //设置创建用户名
            TerminalVO terminalVO = new TerminalVO(terminal, terminalInfoList);
            if (enableUsername) {
                //设置创建用户名
                DataResponseBody<UserDTO> userDTODataResponseBody = adminClient.getUsers(terminal.getCreateUserId());
                if (userDTODataResponseBody != null && userDTODataResponseBody.getData() != null) {
                    terminalVO.setCreateUserName(userDTODataResponseBody.getData().getUsername());
                }
            }

            //设置需要透出的pod事件列表
            List<String> resourceNameList = terminalInfoList.stream().map(TerminalInfo::getK8sResourceName).distinct().collect(Collectors.toList());
            //通过resourceName列表查询对应的k8s事件
            List<K8sEventVO> eventVOList = k8sCallbackEventService.queryByResourceName(resourceNameList);
            terminalVO.setEventList(eventVOList);

            return terminalVO;
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "detail error : {}", e.getMessage(), e);
            throw new BusinessException("内部错误:" + e.getMessage());
        }
    }
    /**
     * 获取终端列表
     *
     * @param refreshStatus 是否从k8s刷新当前终端状态
     * @return
     */
    @Override
    public List<TerminalVO> listWithK8sStatus(boolean refreshStatus){

        UserContext user = userContextService.getCurUser();
        List<Terminal> terminals = list(user);

        if(!BaseService.isAdmin(user) && refreshStatus){
            refreshTerminalStatus(terminals.stream().map(obj->obj.getId()).collect(Collectors.toList()));
        }

        List<TerminalVO> treminals =listVO(terminals);
        return  treminals;
    }

    /**
     * 获取用户对应 终端列表
     *
     * @param user
     * @return
     */
    @Override
    public List<Terminal> list(UserContext user){
        try{
            List<Terminal> terminalInfoList = new ArrayList<>();
            if (!BaseService.isAdmin(user)){
                LambdaQueryWrapper<Terminal> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Terminal::getCreateUserId, userContextService.getCurUserId());
                terminalInfoList = terminalMapper.selectList(wrapper);
            } else {
                terminalInfoList = terminalMapper.selectList(null);
            }

            if (CollectionUtils.isEmpty(terminalInfoList)){
                return Collections.emptyList();
            }
            return terminalInfoList;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"list error : {}",e.getMessage(),e);
            throw new BusinessException("内部错误:"+e.getMessage());
        }
    }

    /**
     * terminal 列表获得vo
     *
     * @param terminalList terminal 列表
     * @return
     */
    @Override
    public List<TerminalVO> listVO(List<Terminal> terminalList) {
        try{
            List<TerminalVO> terminalVOList = new ArrayList<>();

            if (CollectionUtils.isEmpty(terminalList)){
                return terminalVOList;
            }

            for (Terminal terminal : terminalList) {
                terminalVOList.add(detail(new TerminalDTO(terminal.getId()),false));
            }

            //设置用户信息
            if (!CollectionUtils.isEmpty(terminalVOList)) {
                List<Long> createUserIds = terminalVOList.stream().map(TerminalVO::getCreateUserId).collect(Collectors.toList());
                DataResponseBody<List<UserDTO>> userDTODataResponseBody = adminClient.getUserList(createUserIds);
                if (userDTODataResponseBody != null && !CollectionUtils.isEmpty(userDTODataResponseBody.getData())) {
                    Map<Long, UserDTO> userMap = userDTODataResponseBody.getData().stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
                    for (TerminalVO terminalVO : terminalVOList) {
                        if (userMap.get(terminalVO.getCreateUserId()) != null) {
                            terminalVO.setCreateUserName(userMap.get(terminalVO.getCreateUserId()).getUsername());
                        }
                    }
                }
            }

            return terminalVOList;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"listVO error : {}",e.getMessage(),e);
            throw new BusinessException("内部错误:"+e.getMessage());
        }
    }

    /**
     * 更新终端描述
     *
     * @return
     */
    @Override
    public boolean update(TerminalDetailDTO terminalDetailDTO) {
        LogUtil.info(LogEnum.TERMINAL, "update terminal description terminalDetailDTO: {}", terminalDetailDTO);
        Terminal terminal = terminalMapper.selectById(terminalDetailDTO.getId());
        if (terminal == null) {
            LogUtil.error(LogEnum.TERMINAL, "update terminal description terminalPreserveDTO: {}", terminalDetailDTO);
            throw new BusinessException("数据不存在");
        }
        terminal.setDescription(terminalDetailDTO.getDescription());

        return terminalMapper.updateById(terminal) > 0;

    }

    /**
     * 刷新终端中节点 terminalInfo 状态与 k8s 集群同步
     *
     * @param terminalInfoIdList TerminalInfo id 列表
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TerminalInfo> refreshTerminalInfoStatus(List<Long> terminalInfoIdList) {
        try {
            if(CollectionUtils.isEmpty(terminalInfoIdList)){
                return Collections.emptyList();
            }
            List<TerminalInfo> terminalInfos = terminalInfoMapper.selectBatchIds(terminalInfoIdList);
            if (terminalInfos == null){
                LogUtil.error(LogEnum.TERMINAL,"refreshTerminalInfoStatus no terminalInfo found terminalInfoId:{}",StringUtils.join(terminalInfoIdList,SymbolConstant.COMMA));
                return Collections.emptyList();
            }

            List<Long> createUserIds = terminalInfos.stream().map(TerminalInfo::getCreateUserId).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());

            // 批量获取userId对应 namespace 的 k8s service 与 pod 对象
            List<BizPod> bizPods = new ArrayList<>();
            List<BizService>  bizServices = new ArrayList<>();
            for(Long createUserid:createUserIds){
                String namespace = k8sNameTool.getNamespace(createUserid);

                List<BizService>  bizServiceTmp= serviceApi.getWithNameSpace(namespace);
                bizServices.addAll(bizServiceTmp);

                List<BizPod> bizPodsTmp = podApi.getWithNamespace(namespace);
                bizPods.addAll(bizPodsTmp);
            }

            // 根据labels匹配对应pod 与 service ，更新 terminalInfo
            Optional<BizPod> bizPodResult;
            Optional<BizService> bizServiceResult;
            for (TerminalInfo terminalInfo:terminalInfos) {
                final Map<String,String> podlabels = podApi.getLabels(terminalInfo.getK8sResourceName());
                bizPodResult = bizPods.stream().filter(bizPod->bizPod.getLabels().values().containsAll(podlabels.values())).findFirst();
                bizPodResult.ifPresent(obj-> {
                    terminalInfo.setPodIp(obj.getPodIp());
                    terminalInfo.setStatus(TerminalInfoStatusEnum.getCode(obj.getRealPodPhase()));
                });

                final Map<String,String> servicelabels = serviceApi.getLabels(terminalInfo.getK8sResourceName());
                bizServiceResult = bizServices.stream().filter(bizService->bizService.getLabels().values().containsAll(servicelabels.values())).findFirst();
                bizServiceResult.ifPresent(obj->{
                    BizServicePort bizServicePort = obj.getServicePortByTargetPort(MagicNumConstant.TWENTY_TWO);
                    if (bizServicePort != null){
                        terminalInfo.setSshPort(bizServicePort.getNodePort());
                    }
                });

                if (terminalInfo.getSshPort() != null){
                    if (StringUtils.isNotEmpty(terminalInfo.getSshUser())){
                        terminalInfo.setSsh(StrUtil.format(TerminalConstant.SSH_USER_COMMAND,terminalInfo.getSshPort(),terminalInfo.getSshUser(),terminalConfig.getSshHost()));
                    }else {
                        terminalInfo.setSsh(StrUtil.format(TerminalConstant.SSH_COMMAND,terminalInfo.getSshPort(),terminalConfig.getSshHost()));
                    }
                }

                terminalInfo.setUpdateInfo(userContextService.getCurUserId());
            }

            terminalInfoService.updateBatchById(terminalInfos);
            return terminalInfos;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"refreshStatus error : {}",e.getMessage(),e);
            return Collections.emptyList();
        }
    }

    /**
     * 刷新终端 terminal 状态与k8s集群同步
     *
     * @param idList Terminal id列表
     * @return
     */
    @Override
    public List<Terminal> refreshTerminalStatus(List<Long> idList) {
        try {
            if(CollectionUtils.isEmpty(idList)){
                return Collections.emptyList();
            }
            List<Terminal> terminals = terminalMapper.selectBatchIds(idList);
            if (terminals == null){
                LogUtil.error(LogEnum.TERMINAL,"refreshTerminalStatus no terminal found ids:{}",StringUtils.join(idList,SymbolConstant.COMMA));
                return Collections.emptyList();
            }

            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TerminalInfo::getTerminalId, terminals.stream().map(Terminal::getId).collect(Collectors.toList()));
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);

            List<TerminalInfo> refreshTerminalInfoList = refreshTerminalInfoStatus(terminalInfoList.stream().map(TerminalInfo::getId).collect(Collectors.toList()));
            Long runningNode = 0l;
            Long curUserId = userContextService.getCurUserId();
            for(Terminal terminal:terminals){
                runningNode = refreshTerminalInfoList.stream().filter(obj->
                    obj.getTerminalId().equals(terminal.getId()) &&
                    obj. getStatus().equals(TerminalInfoStatusEnum.RUNNING.getCode())
                ).count();
                terminal.setRunningNode(runningNode.intValue());
                terminal.setUpdateInfo(curUserId);
            }

            updateBatchById(terminals);
            return terminals;
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL,"refreshStatus error : {}",e.getMessage(),e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean terminalPodCallback(int times, TerminalK8sPodCallbackCreateDTO req) {
        LogUtil.info(LogEnum.TERMINAL, "terminalPodCallback times:{} req:{}", times, req);
        try {
            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getK8sResourceName, req.getResourceName());
            TerminalInfo terminalInfo = terminalInfoMapper.selectOne(wrapper);
            if (terminalInfo == null) {
                LogUtil.error(LogEnum.TERMINAL, "terminalPodCallback no terminalInfo found k8sResourceName:{}", req.getResourceName());
                return false;
            }

            //修改状态
            if (StringUtils.isEmpty(req.getMessages())) {
                terminalInfo.removeStatusDetail(req.getResourceName());
            } else {
                terminalInfo.putStatusDetail(req.getResourceName(), req.getMessages());
            }
            terminalInfo.setStatus(TerminalInfoStatusEnum.getCode(req.getPhase()));
            terminalInfo.setUpdateInfo(userContextService.getCurUserId());
            terminalInfoMapper.updateById(terminalInfo);
            refreshTerminalInfoStatus(Arrays.asList(terminalInfo.getId()));

            Terminal terminal = terminalMapper.selectById(terminalInfo.getTerminalId());
            Integer oldRunningNode = terminal.getRunningNode();
            Integer runningNode = 0;
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectByTerminalId(terminal.getId());
            for (TerminalInfo info : terminalInfoList) {
                if (TerminalInfoStatusEnum.RUNNING.getCode().equals(info.getStatus())) {
                    runningNode++;
                }
            }
            terminal.setRunningNode(runningNode);
            terminal.setUpdateInfo(userContextService.getCurUserId());
            //只修改节点运行计数，不修改状态
            terminal.setStatus(null);
            terminalMapper.updateById(terminal);

            //所有终端连接就绪后进行ssh免密和NOD_IPS环境变量设置
            LogUtil.info(LogEnum.TERMINAL, "terminalPodCallback totalNode:{} oldRunningNode:{} runningNode:{}", terminal.getTotalNode(), oldRunningNode, terminal.getRunningNode());
            if (!terminal.getTotalNode().equals(oldRunningNode) && terminal.getTotalNode().equals(terminal.getRunningNode())) {
                terminalReadyTask(terminal.getId());
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "terminalPodCallback error : {}", e.getMessage(), e);
        }
        return true;
    }

    /**
     * 推送镜像完成
     *
     * @param terminalId
     */
    @Override
    public void pushImageComplete(Long terminalId, Long userId) {
        try {
            LogUtil.info(LogEnum.TERMINAL, "pushImageComplete id:{}", terminalId);
            if (terminalId == null) {
                return;
            }
            Terminal terminal = terminalMapper.selectById(terminalId);
            if (terminal == null) {
                LogUtil.error(LogEnum.TERMINAL, "pushImageComplete no terminal found id:{}", terminalId);
                return;
            }
            stop(userId, terminalId);
            if (TerminalStatusEnum.SAVING.getCode().equals(terminal.getStatus())) {
                terminal.setStatus(TerminalStatusEnum.DELETED.getCode());
                terminal.removeStatusDetail();
                terminalMapper.updateById(terminal);

                LambdaQueryWrapper<PtImage> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PtImage::getImageUrl, terminal.getImageUrl());
                PtImage ptImage = ptImageMapper.selectOne(wrapper);
                if (ptImage == null) {
                    ptImage = new PtImage();
                }

                ptImage.setImageName(terminal.getImageName());
                ptImage.setImageUrl(terminal.getImageUrl());
                ptImage.setImageTag(terminal.getImageTag());
                ptImage.setRemark(String.valueOf(redisUtils.get(imageRemarkPrefix + terminal.getId())));
                redisUtils.del(imageRemarkPrefix + terminal.getId());
                ptImage.setProjectName(terminal.getImageProject());
                ptImage.setImageResource(MagicNumConstant.ZERO);
                ptImage.setImageStatus(MagicNumConstant.ONE);
                ptImage.setDeleted(false);
                ptImage.setUpdateUserId(userId);
                ptImage.setUpdateTime(new Timestamp(new java.util.Date().getTime()));

                List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectByTerminalId(terminalId);
                if (!CollectionUtils.isEmpty(terminalInfoList)){
                    ptImage.setSshPwd(terminalInfoList.get(0).getSshPassword());
                    ptImage.setSshUser(terminalInfoList.get(0).getSshUser());
                }

                if (ptImage.getId() != null) {
                    ptImageMapper.updateById(ptImage);
                } else {
                    ptImage.setOriginUserId(userId);
                    ptImage.setCreateUserId(userId);
                    ptImage.setCreateTime(new Timestamp(new java.util.Date().getTime()));
                    ptImageMapper.insert(ptImage);
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "pushImageComplete error : {}", e.getMessage(), e);
        }
    }

    /**
     * 推送镜像失败
     *
     * @param terminalId
     * @param message 失败信息
     */
    @Override
    public void pushImageError(Long terminalId, String message, Long userId) {
        try {
            LogUtil.info(LogEnum.TERMINAL, "pushImageError id:{}", terminalId);
            if (terminalId == null) {
                return;
            }
            stop(userId, terminalId);

            Terminal terminal = terminalMapper.selectById(terminalId);
            if (terminal == null) {
                LogUtil.error(LogEnum.TERMINAL, "pushImageError no terminal found id:{}", terminalId);
                return;
            }
            terminal.setStatus(TerminalStatusEnum.FAILED.getCode());
            terminal.putStatusDetail(TerminalStatusEnum.SAVING.getDescription(), message);
            terminalMapper.updateById(terminal);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "pushImageError error : {}", e.getMessage(), e);
        }
    }

    /**
     * 一个终端内所有连接就绪后执行的任务
     *
     * @param terminalId
     */
    @Override
    public void terminalReadyTask(Long terminalId) {
        LogUtil.info(LogEnum.TERMINAL, "terminalReadyTask id:{}", terminalId);
        Terminal terminal = terminalMapper.selectById(terminalId);
        if (terminal == null) {
            LogUtil.error(LogEnum.TERMINAL, "terminalReadyTask error no terminal found id:{}", terminalId);
            return;
        }
        //终端连接只有一个或未全部就绪则不处理
        if (terminal.getTotalNode() == MagicNumConstant.ONE || terminal.getRunningNode() == null || !terminal.getRunningNode().equals(terminal.getTotalNode())) {
            LogUtil.info(LogEnum.TERMINAL, "terminal not ready or only one connection id:{} totalNode:{} runningNode:{}", terminalId, terminal.getTotalNode(), terminal.getRunningNode());
            return;
        }
        //取出master 连接，将其置于podList第一个，以保证其ip在 NODE_IPS 第一个
        List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectByTerminalId(terminal.getId());
        TerminalInfo masterTerminalInfo = null;
        for (TerminalInfo terminalInfo : terminalInfoList) {
            if (terminalInfo.isMasterFlag()) {
                masterTerminalInfo = terminalInfo;
            }
        }
        terminalInfoList.remove(masterTerminalInfo);
        if (masterTerminalInfo == null) {
            return;
        }
        List<Pod> podList = podApi.listByResourceName(masterTerminalInfo.getK8sResourceName());
        for (TerminalInfo terminalInfo : terminalInfoList) {
            podList.addAll(podApi.listByResourceName(terminalInfo.getK8sResourceName()));
        }
        boolean ready = true;
        //根据pod信息判断所有pod是否就绪
        for (Pod pod : podList) {
            if (!PodPhaseEnum.RUNNING.getPhase().equals(pod.getStatus().getPhase()) || StringUtils.isEmpty(pod.getStatus().getPodIP())) {
                ready = false;
                LogUtil.info(LogEnum.TERMINAL, "terminalReadyTask terminal not ready pod:{}", pod);
                break;
            }
        }
        if (ready) {
            podApi.sshAuthentication(podList);
            podApi.setNodeIpsEnv(podList);
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
    private TerminalBO buildTerminalBO(TerminalCreateDTO terminalCreateDTO, TerminalInfo terminalInfo, String namespace, String taskIdentifyLabel) {
        TerminalBO terminalBO = new TerminalBO();
        terminalBO.setNamespace(namespace);
        terminalBO.setResourceName(terminalInfo.getK8sResourceName());
        terminalBO.setReplicas(1);
        terminalBO.setGpuNum(terminalInfo.getGpuNum());
        terminalBO.setMemNum(terminalInfo.getMemNum());
        terminalBO.setCpuNum(terminalInfo.getCpuNum());
        terminalBO.setImage(terminalConfig.getHarborAddress() + SymbolConstant.SLASH + terminalCreateDTO.getImageUrl());
        terminalBO.setFsMounts(Maps.newHashMap());
        terminalBO.setBusinessLabel(BusinessLabelServiceNameEnum.TERMINAL.getBusinessLabel());
        terminalBO.setTaskIdentifyLabel(taskIdentifyLabel);
        terminalBO.addPort(MagicNumConstant.TWENTY_TWO);
        terminalBO.addPorts(terminalCreateDTO.getPorts());
        terminalBO.setCmdLines(terminalCreateDTO.getCmdLines());
        if (terminalInfo.getGpuNum() > MagicNumConstant.ZERO) {
            terminalBO.setGpuType(terminalInfo.getGpuType());
            terminalBO.setGpuModel(terminalInfo.getGpuModel());
            terminalBO.setK8sLabelKey(terminalInfo.getK8sLabelKey());
            terminalBO.setUseGpu(true);
        } else {
            terminalBO.setUseGpu(false);
        }
        if (terminalCreateDTO.getDataSourcePath() != null) {
            String dataSetDir = fileStoreApi.getRootDir() + fileStoreApi.getBucket().substring(1) + terminalCreateDTO.getDataSourcePath();
            terminalBO.putfsMounts(TerminalConstant.DATASET_VOLUME_MOUNTS, new PtMountDirBO(dataSetDir, true));
        }
        if (terminalInfo.getDiskMemNum() != null) {
            String workspaceDir = fileStoreApi.getRootDir() + fileStoreApi.getBucket().substring(1) + terminalConfig.getTerminalDir() + SymbolConstant.SLASH + userContextService.getCurUserId() + SymbolConstant.SLASH + terminalConfig.getWorkspaceDir();
            terminalBO.putfsMounts(TerminalConstant.WORKSPACE_VOLUME_MOUNTS, new PtMountDirBO(workspaceDir, terminalInfo.getDiskMemNum() + K8sParamConstants.MEM_UNIT, terminalInfo.getDiskMemNum() + K8sParamConstants.MEM_UNIT, false));
        }
        return terminalBO;
    }

    /**
     * 部署服务
     *
     * @param terminalCreateDTO
     * @return
     */
    private TerminalVO start(TerminalCreateDTO terminalCreateDTO) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "TerminalService create terminalCreateDTO:{}", terminalCreateDTO);
            String k8sResourceName = k8sNameTool.generateResourceName(BizEnum.TERMINAL, RandomUtil.randomString(MagicNumConstant.FIVE));
            String namespace = "";
            String sshUser = terminalCreateDTO.getSshUser();
            String sshPassword = terminalCreateDTO.getSshPwd();

            if (terminalCreateDTO.getId() == null) {
                namespace = k8sNameTool.getNamespace(userContextService.getCurUser());
            } else {
                Terminal oldTerminal = terminalMapper.selectById(terminalCreateDTO.getId());
                namespace = k8sNameTool.getNamespace(oldTerminal.getCreateUserId());
            }

            Terminal terminal = new Terminal();
            terminal.setId(terminalCreateDTO.getId());
            terminal.setName(StringUtils.isEmpty(terminalCreateDTO.getName()) ? CONN + SymbolConstant.HYPHEN + k8sResourceName : terminalCreateDTO.getName());
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
            if (terminal.getId() == null) {
                terminal.setOriginUserId(userContextService.getCurUserId());
                terminal.setCreateUserId(userContextService.getCurUserId());
                terminalMapper.insert(terminal);
            } else {
                terminal.setUpdateInfo(userContextService.getCurUserId());
                terminalMapper.updateById(terminal);
            }

            //复用ssh 用户名 密码
            if (terminal.getId() != null) {
                List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectByTerminalId(terminal.getId());
                if (!CollectionUtils.isEmpty(terminalInfoList)) {
                    sshUser = StringUtils.isEmpty(sshUser) ? terminalInfoList.get(0).getSshUser() : sshUser;
                    sshPassword = StringUtils.isEmpty(sshPassword) ? terminalInfoList.get(0).getSshPassword() : sshPassword;
                }
            }

            List<TerminalInfo> terminalInfoList = new ArrayList<>();

            if (CollectionUtils.isEmpty(terminalCreateDTO.getInfo())) {
                LogUtil.error(LogEnum.TERMINAL, "start 未填写节点规格 terminalCreateDTO:{}", terminalCreateDTO);
                throw new BusinessException(ResponseCode.ERROR, "未填写节点规格!");
            }

            if (terminalCreateDTO.isSameInfo()) {
                for (int i = 0; i < terminalCreateDTO.getTotalNode(); i++) {
                    //第一个默认为主节点
                    TerminalInfo terminalInfo = terminalCreateDTO.getInfo().get(0).toTerminalInfo(terminal.getId(), k8sResourceName + i, userContextService.getCurUserId(), sshUser, sshPassword);
                    if (i == 0) {
                        terminalInfo.setMasterFlag(true);
                    }
                    terminalInfoList.add(terminalInfo);
                }
            } else {
                for (int i = 0; i < terminalCreateDTO.getInfo().size(); i++) {
                    //第一个默认为主节点
                    TerminalInfo terminalInfo = terminalCreateDTO.getInfo().get(i).toTerminalInfo(terminal.getId(), k8sResourceName + i, userContextService.getCurUserId(), sshUser, sshPassword);
                    if (i == 0) {
                        terminalInfo.setMasterFlag(true);
                    }
                    terminalInfoList.add(terminalInfo);
                }
            }
            terminalInfoMapper.deleteByTerminalId(terminal.getId());
            //terminalInfo 数据入库
            for (TerminalInfo terminalInfo : terminalInfoList) {
                if (terminalInfo.getId() == null) {
                    terminalInfo.setCreateUserId(userContextService.getCurUserId());
                    terminalInfoMapper.insert(terminalInfo);
                } else {
                    terminalInfo.setUpdateInfo(userContextService.getCurUserId());
                    terminalInfoMapper.updateById(terminalInfo);
                }
            }

            //获取任务识别标识
            K8sTaskIdentify taskIdentify = new K8sTaskIdentify();
            taskIdentify.setTaskId(terminal.getId());
            taskIdentify.setTaskName(terminal.getName());
            k8sTaskIdentifyMapper.insert(taskIdentify);

            //启动k8s服务
            for (TerminalInfo terminalInfo : terminalInfoList) {
                TerminalBO terminalBO = buildTerminalBO(terminalCreateDTO, terminalInfo, namespace, String.valueOf(taskIdentify.getId()));
                TerminalResourceVO terminalResourceVO = terminalApi.create(terminalBO);
                if (!terminalResourceVO.isSuccess()) {
                    terminalInfo.setStatus(TerminalStatusEnum.FAILED.getCode());
                    terminalInfo.putStatusDetail(TerminalStatusEnum.FAILED.getDescription(), terminalResourceVO.getMessage());
                    terminalInfoMapper.updateById(terminalInfo);
                    LogUtil.error(LogEnum.TERMINAL, "create error : {}", terminalResourceVO.getMessage());
                    throw new BusinessException("内部错误:" + terminalResourceVO.getMessage());
                }
            }

            return detail(new TerminalDTO(terminal.getId()),true);
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "create error : {}", e.getMessage(), e);
            throw new BusinessException("内部错误:" + e.getMessage());
        }
    }

    /**
     * 停止服务
     *
     * @param userId 用户id
     * @param terminalId
     */
    private void stop(Long userId, Long terminalId) {
        try {
            LogUtil.info(LogEnum.TERMINAL, "TerminalService stop userId {} terminalId {}", userId, terminalId);
            if (terminalId == null) {
                return;
            }
            LambdaQueryWrapper<TerminalInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TerminalInfo::getTerminalId, terminalId);
            List<TerminalInfo> terminalInfoList = terminalInfoMapper.selectList(wrapper);

            String namespace = k8sNameTool.getNamespace(userId);

            if (!CollectionUtils.isEmpty(terminalInfoList)) {
                for (TerminalInfo terminalInfo : terminalInfoList) {
                    terminalApi.delete(namespace, terminalInfo.getK8sResourceName());
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.TERMINAL, "stop error : {}", e.getMessage(), e);
        }
    }
}
