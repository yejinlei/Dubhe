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
package org.dubhe.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.admin.dao.UserGroupMapper;
import org.dubhe.admin.dao.UserRoleMapper;
import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.Group;
import org.dubhe.admin.domain.entity.User;
import org.dubhe.admin.domain.entity.UserRole;
import org.dubhe.admin.domain.vo.UserGroupVO;
import org.dubhe.admin.service.UserGroupService;
import org.dubhe.admin.service.UserService;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description ????????????????????????
 * @date 2021-05-06
 */
@Service
public class UserGroupServiceImpl implements UserGroupService {

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleMapper userRoleMapper;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(UserGroupVO.class);
    }


    /**
     * ???????????????????????????
     *
     * @param queryDTO ????????????DTO
     * @return Map<String, Object>  ????????????????????????
     */
    @Override
    public Map<String, Object> queryAll(UserGroupQueryDTO queryDTO) {

        Page page = queryDTO.toPage();
        QueryWrapper<Group> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotEmpty(queryDTO.getKeyword())) {
            queryWrapper.and(x -> x.eq("id", queryDTO.getKeyword()).or().like("name", queryDTO.getKeyword()));
        }

        //??????
        IPage<Group> groupList;
        try {
            if (queryDTO.getSort() != null && FIELD_NAMES.contains(queryDTO.getSort())) {
                if (StringConstant.SORT_ASC.equalsIgnoreCase(queryDTO.getOrder())) {
                    queryWrapper.orderByAsc(StringUtils.humpToLine(queryDTO.getSort()));
                } else {
                    queryWrapper.orderByDesc(StringUtils.humpToLine(queryDTO.getSort()));
                }
            } else {
                queryWrapper.orderByDesc(StringConstant.ID);
            }
            groupList = userGroupMapper.selectPage(page, queryWrapper);
        } catch (Exception e) {
            LogUtil.error(LogEnum.IMAGE, "query image list display exception {}", e);
            throw new BusinessException("?????????????????????????????????");
        }
        List<UserGroupVO> userGroupResult = groupList.getRecords().stream().map(x -> {
            UserGroupVO userGroupVO = new UserGroupVO();
            BeanUtils.copyProperties(x, userGroupVO);
            return userGroupVO;
        }).collect(Collectors.toList());
        return PageUtil.toPage(page, userGroupResult);
    }

    /**
     * ???????????????
     *
     * @param groupCreateDTO ?????????????????????DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Group create(UserGroupDTO groupCreateDTO) {
        //??????????????????
        UserContext curUser = userContextService.getCurUser();
        Group userGroup = new Group();
        try {
            BeanUtils.copyProperties(groupCreateDTO, userGroup);
            userGroup.setCreateUserId(curUser.getId());
            userGroupMapper.insert(userGroup);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("???????????????????????????");
        }
        return userGroup;
    }

    /**
     * ?????????????????????
     *
     * @param groupUpdateDTO ?????????????????????DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserGroupDTO groupUpdateDTO) {
        //??????????????????
        UserContext curUser = userContextService.getCurUser();

        Group userGroup = new Group();
        BeanUtils.copyProperties(groupUpdateDTO, userGroup);
        userGroup.setUpdateUserId(curUser.getId());
        userGroupMapper.updateById(userGroup);

    }

    /**
     * ???????????????
     *
     * @param ids ?????????ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            userGroupMapper.delUserByGroupId(id);
            userGroupMapper.delUserGroupByGroupId(id);
        }
    }

    /**
     * ?????????????????????
     *
     * @param userGroupUpdDTO ???????????????DTO??????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updUserWithGroup(UserGroupUpdDTO userGroupUpdDTO) {
        if (userGroupUpdDTO.getGroupId() != null) {
            userGroupMapper.delUserByGroupId(userGroupUpdDTO.getGroupId());
            for (Long userId : userGroupUpdDTO.getUserIds()) {
                userGroupMapper.addUserWithGroup(userGroupUpdDTO.getGroupId(), userId);
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param userGroupDelDTO ?????????????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delUserWithGroup(UserGroupUpdDTO userGroupDelDTO) {
        for (Long userId : userGroupDelDTO.getUserIds()) {
            userGroupMapper.delUserWithGroup(userGroupDelDTO.getGroupId(), userId);
        }
    }


    /**
     * ??????????????????????????????
     *
     * @return List<User> ????????????????????????
     */
    @Override
    public List<User> findUserWithOutGroup() {
        return userGroupMapper.findUserWithOutGroup();
    }


    /**
     *  ???????????????????????????
     *
     * @param groupId ?????????id
     * @return List<User> ????????????
     */
    @Override
    public List<User> queryUserByGroupId(Long groupId) {
        return userGroupMapper.queryUserByGroupId(groupId);
    }

    /**
     * ????????????????????????????????????
     *
     * @param userStateUpdateDTO ??????DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserState(UserStateUpdateDTO userStateUpdateDTO) {
        //????????????????????????id
        List<User> userList = userGroupMapper.queryUserByGroupId(userStateUpdateDTO.getGroupId());
        Set<Long> ids = new HashSet<>();
        if (CollUtil.isNotEmpty(userList)) {
            for (User user : userList) {
                ids.add(user.getId());
            }
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(User::getId, ids);
        updateWrapper.set(User::getEnabled, userStateUpdateDTO.isEnabled());
        userService.update(updateWrapper);
    }

    /**
     * ???????????????????????????
     *
     * @param userGroupUpdDTO ???????????????????????????DTO
     */
    @Override
    public void delUser(UserGroupUpdDTO userGroupUpdDTO) {
        //????????????????????????id
        List<User> userList = userGroupMapper.queryUserByGroupId(userGroupUpdDTO.getGroupId());
        userGroupMapper.delUserByGroupId(userGroupUpdDTO.getGroupId());
        Set<Long> ids = new HashSet<>();
        if (CollUtil.isNotEmpty(userList)) {
            for (User user : userList) {
                ids.add(user.getId());
            }
        }
        userService.delete(ids);
    }

    /**
     * ????????????????????????????????????
     *
     * @param userRoleUpdateDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRole(UserRoleUpdateDTO userRoleUpdateDTO) {
        //????????????????????????id
        List<User> userList = userGroupMapper.queryUserByGroupId(userRoleUpdateDTO.getGroupId());
        List<UserRole> userRoleList = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        if (CollUtil.isNotEmpty(userList)) {
            for (User user : userList) {
                ids.add(user.getId());
                for (Long roleId : userRoleUpdateDTO.getRoleIds()) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(roleId);
                    userRoleList.add(userRole);
                }
            }
        }
        //??????????????????
        userRoleMapper.deleteByUserId(ids);
        //????????????????????????
        userRoleMapper.insertBatchs(userRoleList);
    }
}
