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
package org.dubhe.recycle.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.config.NfsConfig;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.dao.RecycleDetailMapper;
import org.dubhe.recycle.dao.RecycleMapper;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.domain.entity.Recycle;
import org.dubhe.recycle.domain.entity.RecycleDetail;
import org.dubhe.recycle.enums.RecycleStatusEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @description 通用垃圾回收 实现类
 * @date 2021-02-03
 */
@Service
public class RecycleServiceImpl implements RecycleService {

    @Autowired
    private RecycleMapper recycleMapper;

    @Autowired
    private RecycleDetailMapper recycleDetailMapper;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private UserContextService userContextService;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    /**
     * 创建垃圾回收任务
     *
     * @param recycleCreateDTO 垃圾回收任务信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRecycleTask(RecycleCreateDTO recycleCreateDTO) {
        if (CollectionUtil.isEmpty(recycleCreateDTO.getDetailList())) {
            throw new BusinessException("请添加回收详情！");
        }
        //获取当前用户信息
        UserContext curUser = userContextService.getCurUser();
        //设置默认回收延迟时间
        if (recycleCreateDTO.getRecycleDelayDate() <= 0) {
            recycleCreateDTO.setRecycleDelayDate(recycleConfig.getDate());
        }
        for (RecycleDetailCreateDTO detail : recycleCreateDTO.getDetailList()) {
            //如果是删除文件任务，校验根目录及系统环境
            if (Objects.equals(detail.getRecycleType(), RecycleTypeEnum.FILE.getCode()) &&
                    fileStoreApi.formatPath(detail.getRecycleCondition()).startsWith(nfsConfig.getRootDir() + nfsConfig.getBucket())) {
                LogUtil.error(LogEnum.GARBAGE_RECYCLE, "User {} created recycle task failed,file sourcePath :{} invalid", curUser.getUsername(), detail.getRecycleCondition());
                throw new BusinessException("创建回收文件任务失败");
            }
        }
        long createUserId = Objects.isNull(recycleCreateDTO.getCreateUserId()) ? curUser.getId() : recycleCreateDTO.getCreateUserId();
        long updateUserId = Objects.isNull(recycleCreateDTO.getUpdateUserId()) ? curUser.getId() : recycleCreateDTO.getUpdateUserId();
        // 组装回收任务
        Recycle recycle = new Recycle();
        BeanUtils.copyProperties(recycleCreateDTO, recycle);
        recycle.setRecycleStatus(RecycleStatusEnum.PENDING.getCode());
        recycle.setCreateUserId(createUserId);
        recycle.setUpdateUserId(updateUserId);
        recycle.setRecycleDelayDate(DateUtil.getRecycleTime(recycleCreateDTO.getRecycleDelayDate()));
        recycleMapper.insert(recycle);
        // 组装任务详情
        for (RecycleDetailCreateDTO detail : recycleCreateDTO.getDetailList()) {
            RecycleDetail recycleDetail = new RecycleDetail();
            BeanUtils.copyProperties(detail, recycleDetail);
            recycleDetail.setRecycleId(recycle.getId());
            if (recycleDetail.getRecycleStatus() == null) {
                recycleDetail.setRecycleStatus(RecycleStatusEnum.PENDING.getCode());
            }
            recycleDetail.setCreateUserId(createUserId);
            recycleDetail.setUpdateUserId(updateUserId);
            recycleDetailMapper.insert(recycleDetail);
        }
    }

    /**
     * 修改回收任务状态
     *
     * @param recycle 回收任务
     * @param statusEnum 回收状态
     * @param recycleResponse 回收响应
     * @param userId 操作用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecycle(Recycle recycle, RecycleStatusEnum statusEnum, String recycleResponse, long userId) {
        recycle.setRecycleStatus(statusEnum.getCode());
        // 确保响应信息会刷新
        if (recycleResponse == null) {
            recycleResponse = statusEnum.getDescription();
        }
        recycle.setRecycleResponse(StringUtils.truncationString(recycleResponse, MagicNumConstant.FIVE_HUNDRED));
        recycle.setUpdateUserId(userId);
        recycle.setRecycleDelayDate(new Timestamp(System.currentTimeMillis()));
        recycleMapper.updateById(recycle);
    }

    /**
     * 修改回收任务详情状态
     *
     * @param recycleDetail 回收任务详情
     * @param statusEnum 回收状态
     * @param recycleResponse 回收响应
     * @param userId 操作用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecycleDetail(RecycleDetail recycleDetail, RecycleStatusEnum statusEnum, String recycleResponse, long userId) {
        recycleDetail.setRecycleStatus(statusEnum.getCode());
        // 确保响应信息会刷新
        if (recycleResponse == null) {
            recycleResponse = statusEnum.getDescription();
        }
        recycleDetail.setRecycleResponse(StringUtils.truncationString(recycleResponse, MagicNumConstant.FIVE_HUNDRED));
        recycleDetail.setUpdateUserId(userId);
        recycleDetailMapper.updateById(recycleDetail);
    }
}
