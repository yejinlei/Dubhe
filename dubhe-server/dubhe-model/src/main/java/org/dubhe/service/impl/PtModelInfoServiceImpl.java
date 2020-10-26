/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.config.NfsConfig;
import org.dubhe.config.RecycleConfig;
import org.dubhe.dao.PtModelBranchMapper;
import org.dubhe.dao.PtModelInfoMapper;
import org.dubhe.domain.PtModelBranch;
import org.dubhe.domain.PtModelInfo;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.vo.PtModelInfoCreateVO;
import org.dubhe.domain.vo.PtModelInfoDeleteVO;
import org.dubhe.domain.vo.PtModelInfoQueryVO;
import org.dubhe.domain.vo.PtModelInfoUpdateVO;
import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.RecycleModuleEnum;
import org.dubhe.enums.RecycleTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.PtModelBranchService;
import org.dubhe.service.PtModelInfoService;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 模型管理
 * @date 2020-03-24
 */
@Service
public class PtModelInfoServiceImpl implements PtModelInfoService {

    @Autowired
    private PtModelBranchMapper ptModelBranchMapper;

    @Autowired
    private PtModelInfoMapper ptModelInfoMapper;

    @Autowired
    private PtModelBranchService ptModelBranchService;

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private RecycleTaskService recycleTaskService;

    @Autowired
    private RecycleConfig recycleConfig;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtModelInfoQueryVO.class);
    }

    /**
     * 查询数据分页
     *
     * @param ptModelInfoQueryDTO 模型管理查询参数
     * @return Map<String, Object> 模型管理分页对象
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> queryAll(PtModelInfoQueryDTO ptModelInfoQueryDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        Page page = new Page(null == ptModelInfoQueryDTO.getCurrent() ? 1 : ptModelInfoQueryDTO.getCurrent()
                , null == ptModelInfoQueryDTO.getSize() ? 10 : ptModelInfoQueryDTO.getSize());
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}查询模型管理列表展示开始, 接收的参数为{}，Page{}", user.getUsername(), ptModelInfoQueryDTO, page);
        QueryWrapper<PtModelInfo> wrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(ptModelInfoQueryDTO.getName())) {
            wrapper.and(qw -> qw.eq("id", ptModelInfoQueryDTO.getName()).or().like("name",
                    ptModelInfoQueryDTO.getName()));
        }
        if (ptModelInfoQueryDTO.getModelResource() == null || ptModelInfoQueryDTO.getModelResource() == PtModelUtil.NUMBER_ZERO) {
            wrapper.eq("model_resource", PtModelUtil.NUMBER_ZERO);
        } else {
            wrapper.eq("model_resource", ptModelInfoQueryDTO.getModelResource());
        }
        if (!StringUtils.isEmpty(ptModelInfoQueryDTO.getModelClassName())) {
            wrapper.and(qw -> qw.like("model_type", ptModelInfoQueryDTO.getModelClassName()));
        }
        IPage<PtModelInfo> ptModelInfos = null;
        try {
            String order = null == ptModelInfoQueryDTO.getOrder() ? PtModelUtil.SORT_DESC : ptModelInfoQueryDTO.getOrder();
            if (ptModelInfoQueryDTO.getSort() != null && FIELD_NAMES.contains(ptModelInfoQueryDTO.getSort())) {
                switch (order.toLowerCase()) {
                    case PtModelUtil.SORT_ASC:
                        wrapper.orderByAsc(StringUtils.humpToLine(ptModelInfoQueryDTO.getSort()));
                        break;
                    default:
                        wrapper.orderByDesc(StringUtils.humpToLine(ptModelInfoQueryDTO.getSort()));
                        break;
                }
            } else {
                wrapper.orderByDesc(PtModelUtil.ID);
            }
            ptModelInfos = ptModelInfoMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "查询模型列表展示异常:{}, 请求信息:{}", e, ptModelInfoQueryDTO);
            throw new BusinessException("查询模型列表展示异常");
        }

        List<PtModelInfoQueryVO> ptModelInfoQueryVOs = ptModelInfos.getRecords().stream().map(x -> {
            PtModelInfoQueryVO ptModelInfoQueryVO = new PtModelInfoQueryVO();
            BeanUtils.copyProperties(x, ptModelInfoQueryVO);
            return ptModelInfoQueryVO;
        }).collect(Collectors.toList());

        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}查询模型管理列表展示结束, 结果为{}", user.getUsername(), ptModelInfoQueryVOs);
        return PageUtil.toPage(page, ptModelInfoQueryVOs);
    }

    /**
     * 创建
     *
     * @param ptModelInfoCreateDTO 模型管理创建对象
     * @return PtModelInfoCreateVO 模型管理返回创建VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelInfoCreateVO create(PtModelInfoCreateDTO ptModelInfoCreateDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}保存模型开始, 接收的参数为{}", user.getUsername(), ptModelInfoCreateDTO);

        //保存任务参数
        PtModelInfo ptModelInfo = new PtModelInfo();
        BeanUtils.copyProperties(ptModelInfoCreateDTO, ptModelInfo);

        if (ptModelInfoMapper.insert(ptModelInfo) < 1) {
            //模型管理未保存成功，抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}保存模型未成功，进行模型管理表插入操作失败", user.getUsername());
            throw new BusinessException("模型创建失败");
        }

        //如果上传的模型存在，则创建一个版本
        if (ptModelInfoCreateDTO.getModelAddress() != null) {
            PtModelBranchCreateDTO ptModelBranchCreateDTO = new PtModelBranchCreateDTO();
            BeanUtils.copyProperties(ptModelInfoCreateDTO, ptModelBranchCreateDTO);
            ptModelBranchCreateDTO.setParentId(ptModelInfo.getId());
            ptModelBranchService.create(ptModelBranchCreateDTO);
        }
        PtModelInfoCreateVO ptModelInfoCreateVO = new PtModelInfoCreateVO();
        ptModelInfoCreateVO.setId(ptModelInfo.getId());
        return ptModelInfoCreateVO;
    }

    /**
     * 编辑
     *
     * @param ptModelInfoUpdateDTO 模型管理修改对象
     * @return PtModelInfoUpdateVO 模型管理返回更新VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelInfoUpdateVO update(PtModelInfoUpdateDTO ptModelInfoUpdateDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}更新模型开始, 接收的参数为{}", user.getUsername(), ptModelInfoUpdateDTO);

        //权限校验
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("id", ptModelInfoUpdateDTO.getId());
        if (ptModelInfoMapper.selectCount(wrapper) < 1) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}修改模型未成功,没有权限在模型表中修改对应数据", user.getUsername());
            throw new BusinessException("您修改的ID不存在请重新输入");
        }

        //修改任务参数
        PtModelInfo ptModelInfo = new PtModelInfo();
        BeanUtils.copyProperties(ptModelInfoUpdateDTO, ptModelInfo);
        if (ptModelInfoMapper.updateById(ptModelInfo) < 1) {
            //任务参数未修改成功，抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}修改模型未成功,进行模型表修改操作失败", user.getUsername());
            throw new BusinessException("模型更新失败");
        }
        PtModelInfoUpdateVO ptModelInfoUpdateVO = new PtModelInfoUpdateVO();
        ptModelInfoUpdateVO.setId(ptModelInfo.getId());
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}保存模型结束, 返回修改任务参数id={}", user.getUsername(), ptModelInfo.getId());
        return ptModelInfoUpdateVO;
    }

    /**
     * 多选删除
     *
     * @param ptModelInfoDeleteDTO 模型管理删除对象
     * @return PtModelInfoDeleteVO 模型管理返回删除VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelInfoDeleteVO deleteAll(PtModelInfoDeleteDTO ptModelInfoDeleteDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}删除模型列表, 接收的参数为{}", user.getUsername(), ptModelInfoDeleteDTO);

        //数组ids去重
        List<Long> ids = Arrays.stream(ptModelInfoDeleteDTO.getIds()).distinct().collect(Collectors.toList());

        //权限校验
        QueryWrapper query = new QueryWrapper<>();
        query.in("id", ids);
        if (ptModelInfoMapper.selectCount(query) < ids.size()) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}删除模型列表未成功,没有权限在模型管理表中删除对应数据", user.getUsername());
            throw new BusinessException("您没有此权限");
        }

        //删除任务参数
        if (ptModelInfoMapper.deleteBatchIds(ids) < ids.size()) {
            //模型列表未删除成功,抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}删除模型列表未成功,根据id数组{}进行模型管理表删除操作失败", user.getUsername(), ids);
            throw new BusinessException("模型删除失败");
        }

        QueryWrapper queryBranch = new QueryWrapper<>();
        queryBranch.in("parent_id", ids);

        List<PtModelBranch> ptModelBranches = ptModelBranchMapper.selectList(queryBranch);
        List<Long> branchlists = ptModelBranches.stream().map(x -> {
            return x.getId();
        }).collect(Collectors.toList());
        if (branchlists.size() > 0) {
            if (ptModelBranchMapper.deleteBatchIds(branchlists) < branchlists.size()) {
                LogUtil.error(LogEnum.BIZ_MODEL, "用户{}删除模型版本未成功,根据id数组{}进行模型版本表删除操作失败", user.getUsername(), ids);
                throw new BusinessException("模型删除失败");
            }
            //定时任务删除相应的模型文件
            RecycleTaskCreateDTO recycleTask = new RecycleTaskCreateDTO();
            for (PtModelBranch ptModelBranch : ptModelBranches) {
                recycleTask.setRecycleModule(RecycleModuleEnum.BIZ_MODEL.getValue())
                        .setRecycleType(RecycleTypeEnum.FILE.getCode())
                        .setRecycleDelayDate(recycleConfig.getModelValid())
                        .setRecycleCondition(nfsUtil.formatPath(nfsConfig.getRootDir() + nfsConfig.getBucket() + ptModelBranch.getModelAddress()))
                        .setRecycleNote("删除模型文件");
                recycleTaskService.createRecycleTask(recycleTask);
            }
        }

        //返回删除的模型管理参数id数组
        PtModelInfoDeleteVO ptModelInfoDeleteVO = new PtModelInfoDeleteVO();
        ptModelInfoDeleteVO.setIds(ptModelInfoDeleteDTO.getIds());
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}删除模型列表结束, 返回删除模型列表数组ids={}", user.getUsername(), ids);
        return ptModelInfoDeleteVO;
    }

    /**
     * 根据模型来源查询模型信息
     *
     * @param ptModelInfoQueryDTO 模型查询对象
     * @return  PtModelInfoQueryVO 模型管理返回查询VO
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<PtModelInfoQueryVO> findModelByResource(PtModelInfoQueryDTO ptModelInfoQueryDTO) {
        UserDTO userDto = JwtUtils.getCurrentUserDto();
        List<PtModelInfo> modelInfos = ptModelInfoMapper.findModelByResource(ptModelInfoQueryDTO.getModelResource(),userDto.getId());
        ArrayList<PtModelInfoQueryVO> ptModelInfoQueryVOS=new ArrayList<>();
        if(modelInfos!=null && modelInfos.size()!=0){
            modelInfos.stream().forEach(ptModelInfo -> {
                PtModelInfoQueryVO ptModelInfoQueryVO=new PtModelInfoQueryVO();
                ptModelInfoQueryVO.setName(ptModelInfo.getName());
                ptModelInfoQueryVO.setId(ptModelInfo.getId());
                ptModelInfoQueryVOS.add(ptModelInfoQueryVO);
            });
        }
        return ptModelInfoQueryVOS;
    }

}
