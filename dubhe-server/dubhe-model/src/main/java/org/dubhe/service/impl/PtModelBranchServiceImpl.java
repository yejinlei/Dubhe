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
import org.dubhe.domain.vo.PtModelBranchCreateVO;
import org.dubhe.domain.vo.PtModelBranchDeleteVO;
import org.dubhe.domain.vo.PtModelBranchQueryVO;
import org.dubhe.domain.vo.PtModelBranchUpdateVO;
import org.dubhe.enums.*;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.PtModelBranchService;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.service.storage.AsyncStorage;
import org.dubhe.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 模型版本管理
 * @date 2020-03-24
 */
@Service
public class PtModelBranchServiceImpl implements PtModelBranchService {

    @Autowired
    private PtModelBranchMapper ptModelBranchMapper;

    @Autowired
    private PtModelInfoMapper ptModelInfoMapper;

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private LocalFileUtil localFileUtil;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private AsyncStorage asyncStorage;

    @Autowired
    private RecycleTaskService recycleTaskService;

    @Autowired
    private RecycleConfig recycleConfig;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtModelBranchQueryVO.class);
    }

    /**
     * 查询数据分页
     *
     * @param ptModelBranchQueryDTO 模型版本管理查询参数
     * @return Map<String, Object>  模型版本管理分页对象
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> queryAll(PtModelBranchQueryDTO ptModelBranchQueryDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        Page page = new Page(null == ptModelBranchQueryDTO.getCurrent() ? 1 : ptModelBranchQueryDTO.getCurrent()
                , null == ptModelBranchQueryDTO.getSize() ? 10 : ptModelBranchQueryDTO.getSize());
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}查询模型版本列表展示开始, 接收的参数为{}，Page{}", user.getUsername(), ptModelBranchQueryDTO, page);

        QueryWrapper wrapper = WrapperHelp.getWrapper(ptModelBranchQueryDTO);

        IPage<PtModelBranch> ptModelBranchs = null;
        try {
            String order = null == ptModelBranchQueryDTO.getOrder() ? PtModelUtil.SORT_DESC : ptModelBranchQueryDTO.getOrder();
            if (ptModelBranchQueryDTO.getSort() != null && FIELD_NAMES.contains(ptModelBranchQueryDTO.getSort())) {
                switch (order.toLowerCase()) {
                    case PtModelUtil.SORT_ASC:
                        wrapper.orderByAsc(StringUtils.humpToLine(ptModelBranchQueryDTO.getSort()));
                        break;
                    default:
                        wrapper.orderByDesc(StringUtils.humpToLine(ptModelBranchQueryDTO.getSort()));
                        break;
                }
            } else {
                wrapper.orderByDesc(PtModelUtil.ID);
            }
            ptModelBranchs = ptModelBranchMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "查询模型列表展示异常:{}, 请求信息:{}", e, ptModelBranchQueryDTO);
            throw new BusinessException("查询模型列表展示异常");
        }

        List<PtModelBranchQueryVO> ptModelBranchQueryVOs = ptModelBranchs.getRecords().stream().map(x -> {
            PtModelBranchQueryVO ptModelBranchQueryVO = new PtModelBranchQueryVO();
            BeanUtils.copyProperties(x, ptModelBranchQueryVO);
            return ptModelBranchQueryVO;
        }).collect(Collectors.toList());
        return PageUtil.toPage(page, ptModelBranchQueryVOs);
    }

    /**
     * 创建
     *
     * @param ptModelBranchCreateDTO 模型版本管理创建对象
     * @return PtModelBranchCreateVO 模型版本管理返回创建VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelBranchCreateVO create(PtModelBranchCreateDTO ptModelBranchCreateDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}创建模型版本开始, 接收的参数为{}", user.getUsername(), ptModelBranchCreateDTO);
        PtModelBranch ptModelBranch = new PtModelBranch();
        BeanUtils.copyProperties(ptModelBranchCreateDTO, ptModelBranch);
        QueryWrapper<PtModelInfo> ptModelInfoQueryWrapper = new QueryWrapper<PtModelInfo>();
        ptModelInfoQueryWrapper.eq("id", ptModelBranchCreateDTO.getParentId());
        PtModelInfo ptModelInfo = ptModelInfoMapper.selectOne(ptModelInfoQueryWrapper);
        if (ptModelInfo == null) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}更新模型列表未成功", user.getUsername());
            throw new BusinessException("模型版本创建失败");
        }
        ptModelBranch.setVersionNum(getVersion(ptModelInfo));
        ptModelBranch.setModelPath("");
        //源文件路径
        String sourcePath = nfsConfig.getBucket() + ptModelBranchCreateDTO.getModelAddress();
        if (nfsUtil.fileOrDirIsEmpty(sourcePath)) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "The user {} upload path or source path {} does not exist", user.getUsername(), sourcePath);
            throw new BusinessException("源文件或路径不存在");
        }
        if (ptModelBranchCreateDTO.getModelSource() == PtModelUtil.USER_UPLOAD) {
            //目标路径
            String targetPath = k8sNameTool.getNfsPath(BizNfsEnum.MODEL, user.getId());
            //校验path是否带有压缩文件，如有，则解压至当前文件夹并删除压缩文件
            if (sourcePath.endsWith(PtModelUtil.ZIP)) {
                boolean unzip = localFileUtil.unzipLocalPath(sourcePath, nfsConfig.getBucket() + targetPath);
                if (!unzip) {
                    LogUtil.error(LogEnum.BIZ_MODEL, "用户{}解压模型文件失败", user.getUsername());
                    throw new BusinessException("模型文件解压失败");
                }
            } else {
                boolean nfsCopy = nfsUtil.copyFile(sourcePath, nfsConfig.getBucket() + targetPath);
                if (!nfsCopy) {
                    LogUtil.info(LogEnum.BIZ_MODEL, "模型文件拷贝失败");
                    throw new BusinessException("模型文件拷贝失败");
                }
            }
            //修改存储路径
            ptModelBranch.setModelAddress(targetPath);
            if (ptModelBranchMapper.insert(ptModelBranch) < 1) {
                LogUtil.error(LogEnum.BIZ_MODEL, "用户{}创建新版本未成功", user.getUsername());
                throw new BusinessException("模型版本创建失败");
            }
        } else if (ptModelBranchCreateDTO.getModelSource() == PtModelUtil.TRAINING_IMPORT || ptModelBranchCreateDTO.getModelSource() == PtModelUtil.MODEL_OPTIMIZATION) {
            //目标路径
            String targetPath = k8sNameTool.getNfsPath(BizNfsEnum.MODEL, user.getId());
            ptModelBranch.setModelAddress(targetPath);
            ptModelBranch.setStatus(0);
            if (ptModelBranchMapper.insert(ptModelBranch) < 1) {
                LogUtil.error(LogEnum.BIZ_MODEL, "用户{}创建新版本未成功", user.getUsername());
                throw new BusinessException("模型版本创建失败");
            }
            asyncStorage.copyFileAsync(sourcePath, nfsConfig.getBucket() + targetPath, ptModelBranchMapper, ptModelBranch);
        }
        //模型信息更新
        ptModelInfo.setVersionNum(ptModelBranch.getVersionNum());
        ptModelInfo.setModelAddress(ptModelBranch.getModelAddress());
        ptModelInfo.setTotalNum(ptModelInfo.getTotalNum() + 1);
        if (ptModelInfoMapper.updateById(ptModelInfo) < 1) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}修改版本未成功,进行版本表修改操作失败", user.getUsername());
            throw new BusinessException("模型版本创建失败");
        }

        PtModelBranchCreateVO ptModelBranchCreateVO = new PtModelBranchCreateVO();
        ptModelBranchCreateVO.setId(ptModelBranch.getId());
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}保存新的版本结束, 返回新增版本id为{}", user.getUsername(), ptModelBranch.getId());
        return ptModelBranchCreateVO;
    }

    /**
     * 模型版本获取
     *
     * @param ptModelInfo      模型版本管理创建对象
     * @return String          模型版本
     */
    private String getVersion(PtModelInfo ptModelInfo) {
        String version = "V" + String.format("%04d", ptModelInfo.getTotalNum() + 1);
        return version;
    }

    /**
     * 编辑
     *
     * @param ptModelBranchUpdateDTO 模型版本管理修改对象
     * @return PtModelBranchUpdateVO 模型版本管理返回更新VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelBranchUpdateVO update(PtModelBranchUpdateDTO ptModelBranchUpdateDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}更新模型版本开始, 接收的参数为{}", user.getUsername(), ptModelBranchUpdateDTO);

        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("id", ptModelBranchUpdateDTO.getId());
        Integer i = ptModelBranchMapper.selectCount(wrapper);
        if (i < 1) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}修改模型版本未成功,没有权限在模型版本表中修改对应数据", user.getUsername());
            throw new BusinessException("您修改的ID不存在请重新输入");
        }

        PtModelBranch ptModelBranch = ptModelBranchMapper.selectById(ptModelBranchUpdateDTO.getId());
        BeanUtils.copyProperties(ptModelBranchUpdateDTO, ptModelBranch);

        if (ptModelBranchMapper.updateById(ptModelBranch) < 1) {
            //模型版本未修改成功，抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}修改模型版本未成功,进行模型版本表修改操作失败", user.getUsername());
            throw new BusinessException("模型版本更新失败");
        }

        //返回修改模型版本id
        PtModelBranchUpdateVO ptModelBranchUpdateVO = new PtModelBranchUpdateVO();
        ptModelBranchUpdateVO.setId(ptModelBranch.getId());
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}保存模型版本结束, 返回修改模型版本id={}", user.getUsername(), ptModelBranchUpdateVO.getId());
        return ptModelBranchUpdateVO;
    }

    /**
     * 多选删除
     *
     * @param ptModelBranchDeleteDTO 模型版本管理删除对象
     * @return PtModelBranchDeleteVO 模型版本管理返回删除VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtModelBranchDeleteVO deleteAll(PtModelBranchDeleteDTO ptModelBranchDeleteDTO) {
        //从会话中获取用户信息
        UserDTO user = JwtUtils.getCurrentUserDto();
        LogUtil.info(LogEnum.BIZ_MODEL, "用户{}删除模型版本, 接收的参数为{}", user.getUsername(), ptModelBranchDeleteDTO);

        //数组ids去重
        List<Long> ids = Arrays.stream(ptModelBranchDeleteDTO.getIds()).distinct().collect(Collectors.toList());

        //权限校验
        QueryWrapper query = new QueryWrapper<>();
        query.in("id", ids);
        if (ptModelBranchMapper.selectCount(query) < ids.size()) {
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}删除模型版本未成功,没有权限在模型版本表中删除对应数据", user.getUsername());
            throw new BusinessException("您没有此权限");
        }

        //获取parentID
        List<PtModelBranch> ptModelBranchs = ptModelBranchMapper.selectBatchIds(ids);
        List<Long> parentIdLists = ptModelBranchs.stream().map(x -> {
            return x.getParentId();
        }).distinct().collect(Collectors.toList());

        //删除任务参数
        if (ptModelBranchMapper.deleteBatchIds(ids) < ids.size()) {
            //模型版本未删除成功,抛出异常，并返回失败信息
            LogUtil.error(LogEnum.BIZ_MODEL, "用户{}删除模型版本未成功,根据id数组{}进行模型版本表删除操作失败", user.getUsername(), ids);
            throw new BusinessException("模型版本删除失败");
        }

        //更新parent的状态
        LogUtil.info(LogEnum.BIZ_MODEL, "更新算法的parentID[]", parentIdLists);
        for (int num = 0; num < parentIdLists.size(); num++) {
            QueryWrapper<PtModelBranch> queryWrapper = new QueryWrapper<PtModelBranch>();
            queryWrapper.eq("parent_id", parentIdLists.get(num));
            queryWrapper.orderByDesc("id");
            queryWrapper.last("limit 1");
            List<PtModelBranch> ptModelBranchList = ptModelBranchMapper.selectList(queryWrapper);
            PtModelInfo ptModelInfo = ptModelInfoMapper.selectById(parentIdLists.get(num));
            if (ptModelBranchList.size() > 0) {
                ptModelInfo.setVersionNum(ptModelBranchList.get(0).getVersionNum());
                ptModelInfo.setModelAddress(ptModelBranchList.get(0).getModelAddress());
                if (ptModelInfoMapper.updateById(ptModelInfo) < 1) {
                    LogUtil.error(LogEnum.BIZ_MODEL, "用户{}删除模型版本未成功,进行模型管理表修改操作失败", user.getUsername());
                    throw new BusinessException("模型版本删除失败");
                }
            } else {
                ptModelInfo.setVersionNum("");
                ptModelInfo.setModelAddress("");
                if (ptModelInfoMapper.updateById(ptModelInfo) < 1) {
                    LogUtil.error(LogEnum.BIZ_MODEL, "用户{}删除模型版本未成功,进行模型管理表修改操作失败", user.getUsername());
                    throw new BusinessException("模型版本删除失败");
                }
            }
        }
        //定时任务删除相应的模型文件
        RecycleTaskCreateDTO recycleTask = new RecycleTaskCreateDTO();
        for (PtModelBranch ptModelBranch : ptModelBranchs) {
            recycleTask.setRecycleModule(RecycleModuleEnum.BIZ_MODEL.getValue())
                    .setRecycleType(RecycleTypeEnum.FILE.getCode())
                    .setRecycleDelayDate(recycleConfig.getModelValid())
                    .setRecycleCondition(nfsUtil.formatPath(nfsConfig.getRootDir() + nfsConfig.getBucket() + ptModelBranch.getModelAddress()))
                    .setRecycleNote("删除模型文件");
            recycleTaskService.createRecycleTask(recycleTask);
        }
        PtModelBranchDeleteVO ptModelBranchDeleteVO = new PtModelBranchDeleteVO();
        ptModelBranchDeleteVO.setIds(ptModelBranchDeleteDTO.getIds());
        return ptModelBranchDeleteVO;
    }
}
