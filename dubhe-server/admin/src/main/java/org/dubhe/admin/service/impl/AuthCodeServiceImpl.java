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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.admin.dao.AuthCodeMapper;
import org.dubhe.admin.domain.dto.AuthCodeCreateDTO;
import org.dubhe.admin.domain.dto.AuthCodeQueryDTO;
import org.dubhe.admin.domain.dto.AuthCodeUpdateDTO;
import org.dubhe.admin.domain.dto.RoleAuthUpdateDTO;
import org.dubhe.admin.domain.entity.Auth;
import org.dubhe.admin.domain.entity.AuthPermission;
import org.dubhe.admin.domain.entity.Permission;
import org.dubhe.admin.domain.entity.RoleAuth;
import org.dubhe.admin.domain.vo.AuthVO;
import org.dubhe.admin.service.AuthCodeService;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.utils.PageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description ????????????????????????
 * @date 2021-05-14
 */
@Service
public class AuthCodeServiceImpl extends ServiceImpl<AuthCodeMapper, Auth> implements AuthCodeService {

    @Autowired
    private AuthCodeMapper authCodeMapper;

    @Autowired
    private UserContextService userContextService;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(AuthVO.class);
    }

    /**
     * ???????????????????????????
     *
     * @param authCodeQueryDTO ??????????????????
     * @return Map<String, Object> ?????????????????????
     */
    @Override
    public Map<String, Object> queryAll(AuthCodeQueryDTO authCodeQueryDTO) {

        Page page = authCodeQueryDTO.toPage();
        QueryWrapper<Auth> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotEmpty(authCodeQueryDTO.getAuthCode())) {
            queryWrapper.and(x -> x.eq("id", authCodeQueryDTO.getAuthCode()).or().like("authCOde", authCodeQueryDTO.getAuthCode()));
        }

        //??????
        IPage<Auth> groupList;
        try {
            if (authCodeQueryDTO.getSort() != null && FIELD_NAMES.contains(authCodeQueryDTO.getSort())) {
                if (StringConstant.SORT_ASC.equalsIgnoreCase(authCodeQueryDTO.getOrder())) {
                    queryWrapper.orderByAsc(StringUtils.humpToLine(authCodeQueryDTO.getSort()));
                } else {
                    queryWrapper.orderByDesc(StringUtils.humpToLine(authCodeQueryDTO.getSort()));
                }
            } else {
                queryWrapper.orderByDesc(StringConstant.ID);
            }
            groupList = authCodeMapper.selectPage(page, queryWrapper);
        } catch (Exception e) {
            throw new BusinessException("?????????????????????????????????");
        }
        List<AuthVO> authResult = groupList.getRecords().stream().map(x -> {
            AuthVO authVO = new AuthVO();
            BeanUtils.copyProperties(x, authVO);
            List<Permission> permissions = authCodeMapper.getPermissionByAuthId(x.getId());
            authVO.setPermissions(permissions);
            return authVO;
        }).collect(Collectors.toList());
        return PageUtil.toPage(page, authResult);
    }

    /**
     * ???????????????
     *
     * @param authCodeCreateDTO ???????????????DTO??????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(AuthCodeCreateDTO authCodeCreateDTO) {
        //??????????????????id
        Long curUserId = userContextService.getCurUserId();
        Auth auth = new Auth();
        BeanUtil.copyProperties(authCodeCreateDTO, auth);
        checkAuthCodeIsExist(auth);
        auth.setCreateUserId(curUserId);
        authCodeMapper.insert(auth);
        tiedWithPermission(auth.getId(), authCodeCreateDTO.getPermissions());
    }

    /**
     * ?????????????????????
     *
     * @param authCodeUpdateDTO ?????????????????????DTO??????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(AuthCodeUpdateDTO authCodeUpdateDTO) {
        //??????????????????id
        Long curUserId = userContextService.getCurUserId();
        Auth auth = new Auth();
        BeanUtil.copyProperties(authCodeUpdateDTO, auth);
        checkAuthCodeIsExist(auth);
        auth.setUpdateUserId(curUserId);
        authCodeMapper.updateById(auth);
        if (CollUtil.isNotEmpty(authCodeUpdateDTO.getPermissions())) {
            authCodeMapper.untiedWithPermission(authCodeUpdateDTO.getId());
            tiedWithPermission(auth.getId(), authCodeUpdateDTO.getPermissions());
        }
    }

    /**
     * ?????????????????????
     *
     * @param ids ?????????id??????
     */
    @Override
    public void delete(Set<Long> ids) {
        //?????????????????????
        removeByIds(ids);
        //????????????????????????????????????
        for (Long id : ids) {
            authCodeMapper.untiedWithPermission(id);
        }
    }


    private void tiedWithPermission(Long authId, Set<Long> permissionIds) {
        List<AuthPermission> list = new ArrayList<>();
        for (Long id : permissionIds) {
            AuthPermission authPermission = new AuthPermission();
            authPermission.setAuthId(authId);
            authPermission.setPermissionId(id);
            list.add(authPermission);
        }
        authCodeMapper.tiedWithPermission(list);
    }

    /**
     * ??????????????????
     *
     * @param roleAuthUpdateDTO
     * @return
     */
    @Override
    public void updateRoleAuth(RoleAuthUpdateDTO roleAuthUpdateDTO) {
        authCodeMapper.untiedRoleAuthByRoleId(roleAuthUpdateDTO.getRoleId());
        List<RoleAuth> roleAuths = new ArrayList<>();
        if(CollUtil.isNotEmpty(roleAuthUpdateDTO.getAuthIds())) {
            for (Long authId : roleAuthUpdateDTO.getAuthIds()) {
                RoleAuth roleAuth = new RoleAuth();
                roleAuth.setRoleId(roleAuthUpdateDTO.getRoleId());
                roleAuth.setAuthId(authId);
                roleAuths.add(roleAuth);
            }
            authCodeMapper.tiedRoleAuth(roleAuths);
        }
    }

    /**
     * ???????????????tree
     *
     * @return List<Auth> ?????????tree
     */
    @Override
    public List<AuthVO> getAuthCodeList() {
        List<Auth> authList = authCodeMapper.selectList(new LambdaQueryWrapper<>());
        List<AuthVO> resultList = new ArrayList<>();
        if (CollUtil.isNotEmpty(authList)) {
            for (Auth auth : authList) {
                AuthVO authVO = new AuthVO();
                BeanUtils.copyProperties(auth, authVO);
                List<Permission> permissions = authCodeMapper.getPermissionByAuthId(authVO.getId());
                authVO.setPermissions(permissions);
                resultList.add(authVO);
            }
        }
        return resultList;
    }

    /**
     * ??????authCode???????????????
     *
     * @param auth ???????????????
     * @return List<Auth> ???????????????
     */
    private void checkAuthCodeIsExist(Auth auth) {
        List<Auth> authList = authCodeMapper.selectList(new LambdaQueryWrapper<Auth>()
                .eq(Auth::getAuthCode, auth.getAuthCode()));
        for (Auth authObj : authList) {
            if (Objects.equals(auth.getAuthCode(), authObj.getAuthCode()) &&
                    !Objects.equals(auth.getId(), authObj.getId())) {
                throw new BusinessException("???????????????????????????");
            }
        }
    }
}
