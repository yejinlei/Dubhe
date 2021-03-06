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
 * @description Pod????????????
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
     * ??????Pod??????
     * @param podQueryDTO
     * @return List<PodVO> Pod??????
     */
    @Override
    public List<PodVO> getPods(PodQueryDTO podQueryDTO) {
        //??????????????????
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
     * ???Pod??????????????????Pod??????
     * @param podLogQueryDTO pod???????????????????????????
     * @return PodLogQueryVO Pod??????
     */
    @Override
    public PodLogQueryVO getPodLog(PodLogQueryDTO podLogQueryDTO) {
        //??????????????????
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
     * ?????????pod???????????????
     * @param podLogQueryDTO pod???????????????????????????
     * @return String Pod??????
     */
    @Override
    public String getPodLogStr(PodLogQueryDTO podLogQueryDTO) {
        //??????????????????
        UserContext user = userContextService.getCurUser();
        String namespace = podLogQueryDTO.getNamespace();
        if (!BaseService.isAdmin(user)) {
            checkNamespace(namespace, user.getId());
        }
        return getPodLogStr(namespace, podLogQueryDTO);
    }

    /**
     * ?????????pod???????????????
     * @param namespace ????????????
     * @param podLogQueryDTO pod?????????????????????
     * @return String Pod??????
     */
    private String getPodLogStr(String namespace, PodLogQueryDTO podLogQueryDTO) {
        StringBuilder logBuilder = new StringBuilder();
        LogMonitoringVO result = getLogMonitoringVO(namespace, podLogQueryDTO);
        // ?????????????????????
        int curQueryLogSize = 0;
        // ?????????????????????
        Integer toQueryLogSize = podLogQueryDTO.getLines();
        while (CollectionUtil.isNotEmpty(result.getLogs())) {
            result.getLogs().forEach(log -> logBuilder.append(log).append(StrUtil.CRLF));
            curQueryLogSize += result.getLogs().size();
            // ?????????????????????????????? = ??????????????????+??????????????????
            podLogQueryDTO.setStartLine(podLogQueryDTO.getQueryStart() + podLogQueryDTO.getQueryLines());
            if (toQueryLogSize != null) {
                // ??????????????????????????????
                if (curQueryLogSize >= toQueryLogSize) {
                    // ??????????????????????????????????????????
                    break;
                } else if (curQueryLogSize + podLogQueryDTO.getQueryLines() > toQueryLogSize) {
                    // ????????????????????????????????????????????????????????? ????????????????????? - ????????????????????? ???????????????????????????
                    podLogQueryDTO.setLines(toQueryLogSize - curQueryLogSize);
                }
            }
            result = getLogMonitoringVO(namespace, podLogQueryDTO);
        }
        return logBuilder.toString();
    }

    /**
     * ??????Pod??????
     * @param podLogQueryDTO pod???????????????????????????
     * @return LogMonitoringVO Pod??????
     */
    private LogMonitoringVO getLogMonitoringVO(PodLogQueryDTO podLogQueryDTO) {
        return getLogMonitoringVO(podLogQueryDTO.getNamespace(), podLogQueryDTO);
    }

    /**
     * ??????Pod??????
     * @param namespace ????????????
     * @param podLogQueryDTO pod?????????????????????
     * @return LogMonitoringVO Pod??????
     */
    private LogMonitoringVO getLogMonitoringVO(String namespace, PodLogQueryDTO podLogQueryDTO) {
        LogMonitoringBO logMonitoringBo = new LogMonitoringBO(namespace, podLogQueryDTO);
        return logMonitoringApi.searchLogByPodName(
                podLogQueryDTO.getQueryStart(),
                podLogQueryDTO.getQueryLines(),
                logMonitoringBo);
    }

    /**
     * ???Pod??????????????????
     * @param podLogDownloadQueryDTO pod???????????????????????????
     * @param response ??????
     */
    @Override
    public void downLoadPodLog(PodLogDownloadQueryDTO podLogDownloadQueryDTO, HttpServletResponse response) {
        //??????????????????
        UserContext user = userContextService.getCurUser();
        String namespace = podLogDownloadQueryDTO.getNamespace();
        if (!BaseService.isAdmin(user)) {
            checkNamespace(namespace, user.getId());
        }
        String random = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT) + RandomUtil.randomString(4);
        String baseTempPath = System.getProperty("java.io.tmpdir");
        // ????????????
        String tempPath = baseTempPath + File.separator + random;
        fileStoreApi.createDir(tempPath);
        for (PodVO podVO : podLogDownloadQueryDTO.getPodVOList()) {
            // ???????????????????????????
            String podLogStr = getPodLogStr(namespace, new PodLogQueryDTO(podVO.getPodName()));
            // ??????Pod????????????
            String podLogFilePath = tempPath + File.separator + podVO.getDisplayName() + ".log";
            fileStoreApi.createOrAppendFile(podLogFilePath, podLogStr, true);
        }
        // ???????????? (???????????????????????????)
        String zipFile = tempPath + ".zip";
        fileStoreApi.zipDirOrFile(tempPath, zipFile);
        // ????????????
        fileStoreApi.download(zipFile, response);
        // ??????????????????
        fileStoreApi.deleteDirOrFile(zipFile);
        fileStoreApi.deleteDirOrFile(tempPath);
    }

    /**
     * ??????Pod????????????
     * @param podLogDownloadQueryDTO  pod???????????????????????????
     * @return Map<String, Long> String???Pod name???Long??? Pod count
     */
    @Override
    public Map<String, Long> getLogCount(PodLogDownloadQueryDTO podLogDownloadQueryDTO) {
        //??????????????????
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
     * namespace ????????????????????????
     *
     *
     */
    private void checkNamespace(String namespace, Long curUserId) {
        if (curUserId == null) {
            throw new RuntimeException("Please Login!");
        }
        if (!namespace.equals(k8sNameTool.generateNamespace(curUserId))) {
            throw new RuntimeException("????????????");
        }
    }

}
