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
package org.dubhe.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.config.NfsConfig;
import org.dubhe.config.RecycleConfig;
import org.dubhe.dao.PtMeasureMapper;
import org.dubhe.data.constant.Constant;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.entity.PtMeasure;
import org.dubhe.domain.vo.PtImageQueryVO;
import org.dubhe.domain.vo.PtMeasureQueryVO;
import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.RecycleModuleEnum;
import org.dubhe.enums.RecycleTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.PtMeasureService;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
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

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private RecycleTaskService recycleTaskService;

    @Autowired
    private RecycleConfig recycleConfig;

    public final static List<String> FIELD_NAMES;

    static {
        FIELD_NAMES = ReflectionUtils.getFieldNames(PtImageQueryVO.class);
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
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        Page page = ptMeasureQueryDTO.toPage();

        QueryWrapper<PtMeasure> query = new QueryWrapper<>();
        if (StrUtil.isNotEmpty(ptMeasureQueryDTO.getNameOrId())) {
            query.and(x -> x.eq("id", ptMeasureQueryDTO.getNameOrId()).or().like("name", ptMeasureQueryDTO.getNameOrId()));
        }

        //排序
        IPage<PtMeasure> ptMeasures;
        try {
            if (StrUtil.isNotEmpty(ptMeasureQueryDTO.getSort()) && FIELD_NAMES.contains(ptMeasureQueryDTO.getSort())) {
                if (Constant.SORT_ASC.equalsIgnoreCase(ptMeasureQueryDTO.getOrder())) {
                    query.orderByAsc(StringUtils.humpToLine(ptMeasureQueryDTO.getSort()));
                } else {
                    query.orderByDesc(StringUtils.humpToLine(ptMeasureQueryDTO.getSort()));
                }
            } else {
                query.orderByDesc(Constant.ID);
            }
            ptMeasures = ptMeasureMapper.selectPage(page, query);
        } catch (Exception e) {
            LogUtil.error(LogEnum.MEASURE, "User {} query measure list display exception :{}, request information :{}", currentUser.getId(), e, ptMeasureQueryDTO);
            throw new BusinessException("查询度量列表展示异常");
        }

        List<PtMeasureQueryVO> ptMeasureQueryResult = ptMeasures.getRecords().stream().map(x -> {
            PtMeasureQueryVO ptMeasureQueryVO = new PtMeasureQueryVO();
            BeanUtils.copyProperties(x, ptMeasureQueryVO);
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
    public void createMeasure(PtMeasureDTO ptMeasureCreateDTO) {
        //从会话中获取用户信息
        UserDTO currentUser = JwtUtils.getCurrentUserDto();

        PtMeasure ptMeasure = new PtMeasure();
        BeanUtils.copyProperties(ptMeasureCreateDTO, ptMeasure);
        ptMeasure.setCreateUserId(currentUser.getId());
        try {
            ptMeasureMapper.insert(ptMeasure);
        } catch (Exception e) {
            LogUtil.error(LogEnum.MEASURE, " pt_measure table insert operation failed,Cause of exception is {}", e);
            throw new BusinessException("内部错误");
        }
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
        UserDTO currentUser = JwtUtils.getCurrentUserDto();

        PtMeasure ptMeasure = new PtMeasure();
        BeanUtils.copyProperties(ptMeasureUpdateDTO, ptMeasure);
        ptMeasure.setUpdateUserId(currentUser.getId());

        try {
            PtMeasure measure = ptMeasureMapper.selectById(ptMeasureUpdateDTO.getId());
            if (measure == null) {
                LogUtil.error(LogEnum.MEASURE, "The user{} update measure failed,inquire condition id{} no result", currentUser.getId(), ptMeasureUpdateDTO.getId());
                throw new BusinessException("内部错误");
            }
            ptMeasureMapper.updateById(ptMeasure);
        } catch (BusinessException e) {
            LogUtil.error(LogEnum.MEASURE, " pt_measure table update operation failed,Cause of exception is {}", e);
            throw new BusinessException("内部错误");
        }
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
        UserDTO currentUser = JwtUtils.getCurrentUserDto();
        try {

            Set<Long> idList = ptMeasureDeleteDTO.getIds();
            List<PtMeasure> measureList = ptMeasureMapper.selectBatchIds(idList);
            if (CollUtil.isEmpty(idList)) {
                LogUtil.error(LogEnum.MEASURE, "The user:{} delete measure failed,inquire condition id:{} no result", currentUser.getId(), ptMeasureDeleteDTO.getIds());
                throw new BusinessException("您删除的ID不存在或已被删除");
            }

            int count = ptMeasureMapper.deleteBatchIds(idList);
            if (count < measureList.size()) {
                LogUtil.error(LogEnum.MEASURE, "The user:{} delete measure failed,inquire condition id:{} no result", currentUser.getId(), ptMeasureDeleteDTO.getIds());
                throw new BusinessException("您删除的ID不存在或已被删除");
            }
            measureList.forEach(x -> {
                delMeasureFile(x.getUrl());
            });

        } catch (BusinessException e) {
            LogUtil.error(LogEnum.MEASURE, " pt_measure table delete operation failed,Cause of exception is {}", e);
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
                String url = StrUtil.SLASH + nfsConfig.getBucket() + StrUtil.SLASH + ptMeasureList.get(0).getUrl();
                if (!nfsUtil.fileOrDirIsEmpty(url)) {
                    int bytesRead = 0;
                    //获取文件输入流
                    bufferedInput = nfsUtil.getInputStream(url);
                    while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                        //将读取的字节转为字符串对象
                        String tmpStr = new String(buffer, 0, bytesRead);
                        sb.append(tmpStr);
                    }
                }
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.MEASURE, " getMeasureByName method read jsonFile operation failed,Cause of exception is {}", e);
            throw new BusinessException("内部错误");
        } finally {
            IOUtil.close(bufferedInput);
        }
        return JSONUtil.toJsonStr(sb);
    }

    /**
     * 删除nfs服务器上的度量文件
     *
     * @param filePath 度量文件相对路径
     */
    private void delMeasureFile(String filePath) {
        String nfsBucket = nfsConfig.getRootDir() + nfsConfig.getBucket() + StrUtil.SLASH;
        //度量文件的nfs服务器相对路径
        filePath = filePath.substring(0, filePath.lastIndexOf(StrUtil.SLASH));
        //度量文件的nfs服务器绝对路径
        String recyclePath = nfsUtil.formatPath((nfsBucket + StrUtil.SLASH + filePath));

        //创建已删除度量的无效文件回收任务
        RecycleTaskCreateDTO recycleTask = new RecycleTaskCreateDTO();
        recycleTask.setRecycleModule(RecycleModuleEnum.BIZ_TRAIN.getValue())
                .setRecycleType(RecycleTypeEnum.FILE.getCode())
                .setRecycleDelayDate(recycleConfig.getTrainValid())
                .setRecycleCondition(recyclePath)
                .setRecycleNote("回收已删除度量文件");
        LogUtil.info(LogEnum.MEASURE, "delete measure add recycle task:{}", recycleTask);
        recycleTaskService.createRecycleTask(recycleTask);
    }
}
