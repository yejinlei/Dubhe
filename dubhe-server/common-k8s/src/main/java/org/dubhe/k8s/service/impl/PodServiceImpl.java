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
package org.dubhe.k8s.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.dto.PodLogDownloadQueryDTO;
import org.dubhe.k8s.domain.dto.PodLogQueryDTO;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;
import org.dubhe.k8s.domain.vo.PodLogQueryVO;
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.k8s.service.PodService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.k8s.utils.PodUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description Pod业务接口
 * @date 2020-08-14
 */
@Service
public class PodServiceImpl implements PodService {

    @Autowired
    private ResourceCache resourceCache;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private LogMonitoringApi logMonitoringApi;

    @Autowired
    private UserContextService userContextService;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    /**
     * 查询Pod信息
     * @param podQueryDTO
     * @return List<PodVO> Pod信息
     */
    @Override
    public List<PodVO> getPods(PodQueryDTO podQueryDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        String namespace = podQueryDTO.getNamespace();
        if (!BaseService.isAdmin(user)) {
            checkNamespace(namespace, user.getId());
        }
        List<PodVO> podVOList = new ArrayList<>();
        Set<String> podSet = resourceCache.getPodNameByResourceName(namespace, podQueryDTO.getResourceName());
        if (CollectionUtils.isEmpty(podSet)) {
            return podVOList;
        }
        List<String> podList = new ArrayList<>(podSet);
        Collections.sort(podList);
        int masterIndex = 1;
        int slaveIndex = 1;
        int podIndex = 1;

        for (String pod : podList) {
            if (PodUtil.isMaster(pod)) {
                podVOList.add(new PodVO(pod, "Master" + (masterIndex++), namespace));
            } else if (PodUtil.isSlave(pod)) {
                podVOList.add(new PodVO(pod, "Slave" + (slaveIndex++), namespace));
            } else {
                podVOList.add(new PodVO(pod, "Pod" + (podIndex++), namespace));
            }
        }
        Collections.sort(podVOList, Comparator.comparing(PodVO::getDisplayName));
        return podVOList;
    }

    /**
     * 按Pod名称分页查询Pod日志
     * @param podLogQueryDTO pod日志查询条件实体类
     * @return PodLogQueryVO Pod日志
     */
    @Override
    public PodLogQueryVO getPodLog(PodLogQueryDTO podLogQueryDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        String namespace = podLogQueryDTO.getNamespace();
        if (!BaseService.isAdmin(user)) {
            checkNamespace(namespace, user.getId());
        }
        LogMonitoringVO result = getLogMonitoringVO(podLogQueryDTO);
        return new PodLogQueryVO(result.getLogs(),
                podLogQueryDTO.getQueryStart(),
                podLogQueryDTO.getQueryStart() + result.getTotalLogs().intValue() - 1,
                result.getTotalLogs().intValue());
    }

    /**
     * 获取单pod日志字符串
     * @param podLogQueryDTO pod日志查询条件实体类
     * @return String Pod日志
     */
    @Override
    public String getPodLogStr(PodLogQueryDTO podLogQueryDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        String namespace = podLogQueryDTO.getNamespace();
        if (!BaseService.isAdmin(user)) {
            checkNamespace(namespace, user.getId());
        }
        return getPodLogStr(namespace, podLogQueryDTO);
    }

    /**
     * 获取单pod日志字符串
     * @param namespace 命名空间
     * @param podLogQueryDTO pod日志查询实体类
     * @return String Pod日志
     */
    private String getPodLogStr(String namespace, PodLogQueryDTO podLogQueryDTO) {
        StringBuilder logBuilder = new StringBuilder();
        LogMonitoringVO result = getLogMonitoringVO(namespace, podLogQueryDTO);
        // 当前查询总条数
        int curQueryLogSize = 0;
        // 计划查询总条数
        Integer toQueryLogSize = podLogQueryDTO.getLines();
        while (CollectionUtil.isNotEmpty(result.getLogs())) {
            result.getLogs().forEach(log -> logBuilder.append(log).append(StrUtil.CRLF));
            curQueryLogSize += result.getLogs().size();
            // 设置下次查询起始坐标 = 上次查询坐标+上次查询跨度
            podLogQueryDTO.setStartLine(podLogQueryDTO.getQueryStart() + podLogQueryDTO.getQueryLines());
            if (toQueryLogSize != null) {
                // 当前有计划查询总条数
                if (curQueryLogSize >= toQueryLogSize) {
                    // 满足计划查询总条数，查询完毕
                    break;
                } else if (curQueryLogSize + podLogQueryDTO.getQueryLines() > toQueryLogSize) {
                    // 若本批次查询会超出计划查询总条数，则取 计划查询总条数 - 当前查询总条数 作为本批次查询条数
                    podLogQueryDTO.setLines(toQueryLogSize - curQueryLogSize);
                }
            }
            result = getLogMonitoringVO(namespace, podLogQueryDTO);
        }
        return logBuilder.toString();
    }

    /**
     * 查询Pod日志
     * @param podLogQueryDTO pod日志查询条件实体类
     * @return LogMonitoringVO Pod日志
     */
    private LogMonitoringVO getLogMonitoringVO(PodLogQueryDTO podLogQueryDTO) {
        return getLogMonitoringVO(podLogQueryDTO.getNamespace(), podLogQueryDTO);
    }

    /**
     * 查询Pod日志
     * @param namespace 命名空间
     * @param podLogQueryDTO pod日志查询实体类
     * @return LogMonitoringVO Pod日志
     */
    private LogMonitoringVO getLogMonitoringVO(String namespace, PodLogQueryDTO podLogQueryDTO) {
        LogMonitoringBO logMonitoringBo = new LogMonitoringBO(namespace, podLogQueryDTO);
        return logMonitoringApi.searchLogByPodName(
                podLogQueryDTO.getQueryStart(),
                podLogQueryDTO.getQueryLines(),
                logMonitoringBo);
    }

    /**
     * 按Pod名称下载日志
     * @param podLogDownloadQueryDTO pod日志下载条件实体类
     * @param response 响应
     */
    @Override
    public void downLoadPodLog(PodLogDownloadQueryDTO podLogDownloadQueryDTO, HttpServletResponse response) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        String namespace = podLogDownloadQueryDTO.getNamespace();
        if (!BaseService.isAdmin(user)) {
            checkNamespace(namespace, user.getId());
        }
        String random = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT) + RandomUtil.randomString(4);
        String baseTempPath = System.getProperty("java.io.tmpdir");
        // 临时目录
        String tempPath = baseTempPath + File.separator + random;
        fileStoreApi.createDir(tempPath);
        for (PodVO podVO : podLogDownloadQueryDTO.getPodVOList()) {
            // 按节点名称获取日志
            String podLogStr = getPodLogStr(namespace, new PodLogQueryDTO(podVO.getPodName()));
            // 生成Pod日志文件
            String podLogFilePath = tempPath + File.separator + podVO.getDisplayName() + ".log";
            fileStoreApi.createOrAppendFile(podLogFilePath, podLogStr, true);
        }
        // 压缩文件 (与临时目录同目录级)
        String zipFile = tempPath + ".zip";
        fileStoreApi.zipDirOrFile(tempPath, zipFile);
        // 下载文件
        fileStoreApi.download(zipFile, response);
        // 删除临时文件
        fileStoreApi.deleteDirOrFile(zipFile);
        fileStoreApi.deleteDirOrFile(tempPath);
    }

    /**
     * 统计Pod日志数量
     * @param podLogDownloadQueryDTO  pod日志下载条件实体类
     * @return Map<String, Long> String：Pod name，Long： Pod count
     */
    @Override
    public Map<String, Long> getLogCount(PodLogDownloadQueryDTO podLogDownloadQueryDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        String namespace = podLogDownloadQueryDTO.getNamespace();
        if (!BaseService.isAdmin(user)) {
            checkNamespace(namespace, user.getId());
        }
        Map<String, Long> logCountMap = new HashMap<>(podLogDownloadQueryDTO.getPodVOList().size() * 2);
        for (int i = 0; i < podLogDownloadQueryDTO.getPodVOList().size(); i++) {
            String podName = podLogDownloadQueryDTO.getPodVOList().get(i).getPodName();
            logCountMap.put(podName, logMonitoringApi.searchLogCountByPodName(new LogMonitoringBO(namespace, podName)));
        }
        return logCountMap;
    }

    /**
     * namespace 命名空间名称校验
     *
     *
     */
    private void checkNamespace(String namespace, Long curUserId) {
        if (curUserId == null) {
            throw new RuntimeException("Please Login!");
        }
        if (!namespace.equals(k8sNameTool.generateNamespace(curUserId))) {
            throw new RuntimeException("权限不足");
        }
    }

}
