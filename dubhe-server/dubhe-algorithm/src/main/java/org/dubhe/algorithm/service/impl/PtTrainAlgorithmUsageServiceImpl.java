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

package org.dubhe.algorithm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.algorithm.dao.PtTrainAlgorithmUsageMapper;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageCreateDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageDeleteDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageQueryDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageUpdateDTO;
import org.dubhe.algorithm.domain.entity.PtTrainAlgorithmUsage;
import org.dubhe.algorithm.domain.vo.PtTrainAlgorithmUsageQueryVO;
import org.dubhe.algorithm.service.PtTrainAlgorithmUsageService;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.CommonPermissionDataDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.aspect.PermissionAspect;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description 用途管理 服务实现类
 * @date 2020-06-23
 */
@Service
public class PtTrainAlgorithmUsageServiceImpl implements PtTrainAlgorithmUsageService {

    @Autowired
    private PtTrainAlgorithmUsageMapper ptTrainAlgorithUsagemMapper;

    @Autowired
    private UserContextService userContextService;

    /**
     * 算法用途
     *
     * @param ptTrainAlgorithmUsageQueryDTO 查询算法用途参数
     * @return Map<String, Object>          返回查询算法用途分页
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> queryAll(PtTrainAlgorithmUsageQueryDTO ptTrainAlgorithmUsageQueryDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        QueryWrapper<PtTrainAlgorithmUsage> wrapper = WrapperHelp.getWrapper(ptTrainAlgorithmUsageQueryDTO);

        Page page = ptTrainAlgorithmUsageQueryDTO.toPage();
        IPage<PtTrainAlgorithmUsage> ptTrainAlgorithms = null;

        if (ptTrainAlgorithmUsageQueryDTO.getIsContainDefault()) {
            wrapper.in("origin_user_id", user.getId(), PermissionAspect.PUBLIC_DATA_USER_ID);
        } else {
            wrapper.eq("origin_user_id", user.getId());
        }

        wrapper.eq("type", ptTrainAlgorithmUsageQueryDTO.getType());

        DataContext.set(CommonPermissionDataDTO.builder().type(true).build());
        ptTrainAlgorithms = ptTrainAlgorithUsagemMapper.selectPage(page, wrapper);
        DataContext.remove();

        List<PtTrainAlgorithmUsageQueryVO> ptTrainAlgorithmUsageQueryResult = ptTrainAlgorithms.getRecords().stream()
                .map(x -> {
                    PtTrainAlgorithmUsageQueryVO ptTrainAlgorithmUsageQueryVO = new PtTrainAlgorithmUsageQueryVO();
                    BeanUtils.copyProperties(x, ptTrainAlgorithmUsageQueryVO);
                    ptTrainAlgorithmUsageQueryVO.setIsDefault(Objects.equals(x.getOriginUserId(), PermissionAspect.PUBLIC_DATA_USER_ID));
                    return ptTrainAlgorithmUsageQueryVO;
                }).collect(Collectors.toList());
        return PageUtil.toPage(page, ptTrainAlgorithmUsageQueryResult);
    }

    /**
     * 新增算法用途
     *
     * @param ptTrainAlgorithmUsageCreateDTO 新增算法用途参数
     * @return List<Long>                    返回新增算法用途
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> create(PtTrainAlgorithmUsageCreateDTO ptTrainAlgorithmUsageCreateDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        PtTrainAlgorithmUsage ptTrainAlgorithmUsage = new PtTrainAlgorithmUsage();
        ptTrainAlgorithmUsage.setAuxInfo(ptTrainAlgorithmUsageCreateDTO.getAuxInfo())
                .setType(ptTrainAlgorithmUsageCreateDTO.getType());

        int insertResult = ptTrainAlgorithUsagemMapper.insert(ptTrainAlgorithmUsage);

        if (insertResult < 1) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} secondary information was not saved successfully", user.getUsername());
            throw new BusinessException("用户辅助信息未保存成功");
        }
        return Collections.singletonList(ptTrainAlgorithmUsage.getId());
    }

    /**
     * 删除算法用途
     *
     * @param ptTrainAlgorithmUsageDeleteDTO 删除算法用途参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(PtTrainAlgorithmUsageDeleteDTO ptTrainAlgorithmUsageDeleteDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        Set<Long> idList = Stream.of(ptTrainAlgorithmUsageDeleteDTO.getIds()).collect(Collectors.toSet());
        QueryWrapper<PtTrainAlgorithmUsage> query = new QueryWrapper<>();
        query.in("id", idList);
        Integer queryCountResult = ptTrainAlgorithUsagemMapper.selectCount(query);

        if (queryCountResult < idList.size()) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} failed to delete user secondary. No permissions to delete corresponding data in user secondary table", user.getUsername());
            throw new BusinessException("您删除的ID不存在或已被删除");
        }
        int deleteCountResult = ptTrainAlgorithUsagemMapper.deleteBatchIds(idList);

        if (deleteCountResult < idList.size()) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} failed to delete user assistance information. User service deletion based on id array {} failed", user.getUsername(), ptTrainAlgorithmUsageDeleteDTO.getIds());
            throw new BusinessException("内部错误");
        }
    }

    /**
     *更新算法用途
     *
     * @param ptTrainAlgorithmUsageUpdateDTO 更新算法用途参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PtTrainAlgorithmUsageUpdateDTO ptTrainAlgorithmUsageUpdateDTO) {
        //获取用户信息
        UserContext user = userContextService.getCurUser();
        QueryWrapper<PtTrainAlgorithmUsage> query = new QueryWrapper<>();
        query.in("id", ptTrainAlgorithmUsageUpdateDTO.getId());
        Integer queryIntResult = ptTrainAlgorithUsagemMapper.selectCount(query);

        if (queryIntResult < MagicNumConstant.ONE) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} failed to modify user auxiliary information and has no permission to modify the corresponding data in the algorithm table", user.getUsername());
            throw new BusinessException("您修改的ID不存在或已被删除,请重新输入");
        }
        PtTrainAlgorithmUsage ptTrainAlgorithmUsage = new PtTrainAlgorithmUsage();
        ptTrainAlgorithmUsage.setId(ptTrainAlgorithmUsageUpdateDTO.getId());
        ptTrainAlgorithmUsage.setAuxInfo(ptTrainAlgorithmUsageUpdateDTO.getAuxInfo());

        int updateResult = ptTrainAlgorithUsagemMapper.updateById(ptTrainAlgorithmUsage);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_ALGORITHM, "User {} failed to modify user assistance information", user.getUsername());
            throw new BusinessException("内部错误");
        }

    }

}
