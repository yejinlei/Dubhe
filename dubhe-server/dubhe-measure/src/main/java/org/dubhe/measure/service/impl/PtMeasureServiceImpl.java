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
package org.dubhe.measure.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Joiner;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.MeasureStateEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.ReflectionUtils;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.biz.file.utils.IOUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.measure.async.GenerateMeasureFileAsync;
import org.dubhe.measure.dao.PtMeasureMapper;
import org.dubhe.measure.domain.dto.PtMeasureCreateDTO;
import org.dubhe.measure.domain.dto.PtMeasureDeleteDTO;
import org.dubhe.measure.domain.dto.PtMeasureQueryDTO;
import org.dubhe.measure.domain.dto.PtMeasureUpdateDTO;
import org.dubhe.measure.domain.entity.PtMeasure;
import org.dubhe.measure.domain.vo.PtMeasureQueryVO;
import org.dubhe.measure.service.PtMeasureService;
import org.dubhe.recycle.config.RecycleConfig;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description 度量管理实现类
 * @date 2020-11-16
 */
@Service
public class PtMeasureServiceImpl implements PtMeasureService {


    @Autowired
    private PtMeasureMapper ptMeasureMapper;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    @Autowired
    private RecycleService recycleService;

    @Autowired
    private RecycleConfig recycleConfig;

    @Autowired
    private GenerateMeasureFileAsync measureFileAsync;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private UserContextService userContextService;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtMeasureQueryDTO.class);
    }


    /**
     * 查询度量信息
     *
     * @param ptMeasureQueryDTO 查询条件
     * @return Map<String, Object> 度量列表分页信息
     */
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    @Override
    public Map<String, Object> getMeasure(PtMeasureQueryDTO ptMeasureQueryDTO) {

        //从会话中获取用户信息
        UserContext currentUser = userContextService.getCurUser();
        Page page = ptMeasureQueryDTO.toPage();

        QueryWrapper<PtMeasure> query = new QueryWrapper<>();

        if (!BaseService.isAdmin(currentUser)) {
            query.eq("create_user_id", currentUser.getId());
        }

        if (ptMeasureQueryDTO.getMeasureStatus() != null) {
            query.eq("measure_status", ptMeasureQueryDTO.getMeasureStatus());
        }

        if (StrUtil.isNotEmpty(ptMeasureQueryDTO.getNameOrId())) {
            query.and(x -> x.eq("id", ptMeasureQueryDTO.getNameOrId()).or().like("name", ptMeasureQueryDTO.getNameOrId()));
        }

        //排序
        IPage<PtMeasure> ptMeasures;
        try {
            if (StrUtil.isNotEmpty(ptMeasureQueryDTO.getSort()) && FIELD_NAMES.contains(ptMeasureQueryDTO.getSort())) {
                if (StringConstant.SORT_ASC.equalsIgnoreCase(ptMeasureQueryDTO.getOrder())) {
                    query.orderByAsc(StringUtils.humpToLine(ptMeasureQueryDTO.getSort()));
                } else {
                    query.orderByDesc(StringUtils.humpToLine(ptMeasureQueryDTO.getSort()));
                }
            } else {
                query.orderByDesc(StringConstant.ID);
            }
            ptMeasures = ptMeasureMapper.selectPage(page, query);
        } catch (Exception e) {
            LogUtil.error(LogEnum.MEASURE, "User {} query measure list failed exception {}", currentUser.getId(), e);
            throw new BusinessException("查询度量列表展示异常");
        }

        List<PtMeasureQueryVO> ptMeasureQueryResult = ptMeasures.getRecords().stream().map(x -> {
            PtMeasureQueryVO ptMeasureQueryVO = new PtMeasureQueryVO();
            BeanUtils.copyProperties(x, ptMeasureQueryVO);
            if (StrUtil.isNotEmpty(x.getModelUrls())) {
                ptMeasureQueryVO.setModelUrls(StrUtil.split(x.getModelUrls(), ','));
            }
            return ptMeasureQueryVO;
        }).collect(Collectors.toList());
        return PageUtil.toPage(page, ptMeasureQueryResult);
    }

    /**
     * 新建度量
     *
     * @param ptMeasureCreateDTO 新建度量入参DTO
     */
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createMeasure(PtMeasureCreateDTO ptMeasureCreateDTO) {
        //从会话中获取用户信息
        UserContext currentUser = userContextService.getCurUser();

        //同一个用户度量名称不能重复
        List<PtMeasure> ptMeasures = ptMeasureMapper.selectList(new LambdaQueryWrapper<PtMeasure>()
                .eq(PtMeasure::getName, ptMeasureCreateDTO.getName())
                .eq(PtMeasure::getCreateUserId, currentUser.getId())
        );
        if (CollUtil.isNotEmpty(ptMeasures)) {
            throw new BusinessException("度量名称已存在!");
        }

        PtMeasure ptMeasure = new PtMeasure();
        BeanUtils.copyProperties(ptMeasureCreateDTO, ptMeasure);
        //自定义度量文件存放路径
        String measurePath = k8sNameTool.getPath(BizPathEnum.MEASURE, currentUser.getId());

        ptMeasure
                .setUrl(measurePath)
                .setDatasetId(ptMeasureCreateDTO.getDatasetId())
                .setDatasetUrl(ptMeasureCreateDTO.getDatasetUrl())
                .setModelUrls(Joiner.on(",").join(ptMeasureCreateDTO.getModelUrls()))
                .setCreateUserId(currentUser.getId());
        try {
            ptMeasureMapper.insert(ptMeasure);
        } catch (Exception e) {
            LogUtil.error(LogEnum.MEASURE, "pt_measure table insert operation failed,exception {}", e);
            throw new BusinessException("内部错误");
        }
        //异步生成度量文件
        measureFileAsync.generateMeasureFile(ptMeasure, measurePath);
    }

    /**
     * 修改度量
     *
     * @param ptMeasureUpdateDTO 修改度量入参DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMeasure(PtMeasureUpdateDTO ptMeasureUpdateDTO) {
        //从会话中获取用户信息
        UserContext currentUser = userContextService.getCurUser();
        PtMeasure measure = ptMeasureMapper.selectById(ptMeasureUpdateDTO.getId());
        if (measure == null) {
            throw new BusinessException("度量不存在!");
        }
        if (MeasureStateEnum.MAKING.equals(measure.getMeasureStatus())) {
            throw new BusinessException("度量文件生成中不支持编辑!");
        }

        //同一个用户度量名称不能重复
        List<PtMeasure> ptMeasures = ptMeasureMapper.selectList(new LambdaQueryWrapper<PtMeasure>()
                .eq(PtMeasure::getName, ptMeasureUpdateDTO.getName())
                .eq(PtMeasure::getCreateUserId, currentUser.getId())
        );
        if (CollUtil.isNotEmpty(ptMeasures)) {
            throw new BusinessException("度量名称已存在!");
        }

        BeanUtils.copyProperties(ptMeasureUpdateDTO, measure);
        measure.setMeasureStatus(MeasureStateEnum.MAKING.getCode())
                .setUpdateUserId(currentUser.getId());
        ptMeasureMapper.updateById(measure);

        //自定义度量文件存放路径
        String measurePath = k8sNameTool.getPath(BizPathEnum.MEASURE, currentUser.getId());
        measureFileAsync.generateMeasureFile(measure, measurePath);

    }

    /**
     * 根据id删除度量
     *
     * @param ptMeasureDeleteDTO 删除度量的条件DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMeasure(PtMeasureDeleteDTO ptMeasureDeleteDTO) {
        //从会话中获取用户信息
        UserContext currentUser = userContextService.getCurUser();
        try {

            Set<Long> idList = ptMeasureDeleteDTO.getIds();
            List<PtMeasure> measureList = ptMeasureMapper.selectBatchIds(idList);
            if (CollUtil.isEmpty(idList)) {
                throw new BusinessException("您删除的ID不存在或已被删除");
            }

            int count = ptMeasureMapper.deleteBatchIds(idList);
            if (count < measureList.size()) {
                throw new BusinessException("您删除的ID不存在或已被删除");
            }
            measureList.forEach(ptMeasure -> {
                delMeasureFile(ptMeasure);
            });

        } catch (BusinessException e) {
            LogUtil.error(LogEnum.MEASURE, "delete the measure failed,exception {}", e);
            throw new BusinessException("内部错误");
        }
    }

    /**
     * 根据度量名称返回度量文件信息
     *
     * @param name 度量名称
     * @return String 度量文件json字符串
     */
    @Override
    public String getMeasureByName(String name) {

        List<PtMeasure> ptMeasureList = ptMeasureMapper.selectList(new LambdaQueryWrapper<PtMeasure>()
                .eq(PtMeasure::getName, name));
        BufferedInputStream bufferedInput = null;
        byte[] buffer = new byte[1024];
        StringBuilder sb = new StringBuilder();
        try {
            if (CollUtil.isNotEmpty(ptMeasureList)) {
                String url = fileStoreApi.formatPath(fileStoreApi.getRootDir() + fileStoreApi.getBucket() + StrUtil.SLASH + ptMeasureList.get(0).getUrl());

                if (fileStoreApi.fileOrDirIsExist(url)) {
                    int bytesRead = 0;
                    //获取文件输入流
                    bufferedInput = new BufferedInputStream(new FileInputStream(url));
                    while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                        //将读取的字节转为字符串对象
                        String tmpStr = new String(buffer, 0, bytesRead);
                        sb.append(tmpStr);
                    }
                }
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.MEASURE, "getMeasureByName method read jsonFile operation failed,exception{}", e);
            throw new BusinessException("内部错误");
        } finally {
            IOUtil.close(bufferedInput);
        }
        return JSONUtil.toJsonStr(sb);
    }


    /**
     * 删除nfs服务器上的度量文件
     *
     * @param ptMeasure 度量实体类对象
     */
    private void delMeasureFile(PtMeasure ptMeasure) {
        String recyclePath = "";
        String filePath = ptMeasure.getUrl();
        if (StrUtil.isNotBlank(filePath)) {
            String nfsBucket = fileStoreApi.getRootDir() + fileStoreApi.getBucket() + StrUtil.SLASH;
            //度量文件的nfs服务器相对路径
            filePath = filePath.substring(0, filePath.lastIndexOf(StrUtil.SLASH));
            //度量文件的nfs服务器绝对路径
            recyclePath = fileStoreApi.formatPath((nfsBucket + StrUtil.SLASH + filePath));
        }

        // 创建已删除度量的无效文件回收任务
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_MEASURE.getValue())
                .recycleDelayDate(recycleConfig.getMeasureValid())
                .recycleNote(RecycleTool.generateRecycleNote("删除度量文件", ptMeasure.getName(), ptMeasure.getId()))
                .recycleCustom(RecycleResourceEnum.MEASURE_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.MEASURE_RECYCLE_FILE.getClassName())
                .remark(String.valueOf(ptMeasure.getId()))
                .build();

        recycleCreateDTO.addRecycleDetailCreateDTO(RecycleDetailCreateDTO.builder()
                .recycleType(RecycleTypeEnum.FILE.getCode())
                .recycleCondition(recyclePath)
                .recycleNote(RecycleTool.generateRecycleNote("删除度量文件", ptMeasure.getName(), ptMeasure.getId()))
                .remark(String.valueOf(ptMeasure.getId()))
                .build()
        );
        recycleService.createRecycleTask(recycleCreateDTO);
    }

    /**
     * 度量文件回收还原
     *
     * @param dto 还原DTO对象
     */
    @Override
    public void recycleRollback(RecycleCreateDTO dto) {
        String measureId = dto.getRemark();
        ptMeasureMapper.updateDeletedById(Long.valueOf(measureId), false);
    }
}
