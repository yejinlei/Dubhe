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
package org.dubhe.dcm.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.dcm.dao.DataLesionSliceMapper;
import org.dubhe.dcm.domain.dto.DataLesionSliceCreateDTO;
import org.dubhe.dcm.domain.dto.DataLesionSliceDeleteDTO;
import org.dubhe.dcm.domain.dto.DataLesionSliceUpdateDTO;
import org.dubhe.dcm.domain.entity.DataLesionSlice;
import org.dubhe.dcm.domain.vo.DataLesionSliceVO;
import org.dubhe.dcm.service.DataLesionSliceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;



/**
 * @description 病灶信息服务实现类
 * @date 2020-12-22
 */
@Service
public class DataLesionSliceServiceImpl extends ServiceImpl<DataLesionSliceMapper, DataLesionSlice> implements DataLesionSliceService {

    @Autowired
    private DataLesionSliceMapper dataLesionSliceMapper;

    @Override
    public boolean save(List<DataLesionSliceCreateDTO> dataLesionSliceCreateDTOS, Long medicineId) {
        if (!CollectionUtils.isEmpty(dataLesionSliceCreateDTOS)) {
            deleteByMedicineId(medicineId);
            List<DataLesionSlice> dataLesionSliceList = new ArrayList<>();
            dataLesionSliceCreateDTOS.forEach(dataLesionSliceCreateDTO -> {
                JSONArray jsonArray = new JSONArray();
                dataLesionSliceCreateDTO.getList().forEach(dataLesionDrawInfoDTO -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("drawId", dataLesionDrawInfoDTO.getDrawId());
                    jsonObject.put("sliceNumber", dataLesionDrawInfoDTO.getSliceNumber());
                    jsonArray.add(jsonObject);
                });
                DataLesionSlice dataLesionSlice = new DataLesionSlice(dataLesionSliceCreateDTO.getLesionOrder()
                        , dataLesionSliceCreateDTO.getSliceDesc(), medicineId, jsonArray.toJSONString(), JwtUtils.getCurUserId());
                dataLesionSliceList.add(dataLesionSlice);
            });
            insetDataLesionSliceBatch(dataLesionSliceList);
        } else {
            deleteByMedicineId(medicineId);
        }
        return true;
    }

    /**
     * 批量插入病灶信息
     *
     * @param dataLesionSliceList 病灶信息list
     * @return boolean 数据是否插入成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insetDataLesionSliceBatch(List<DataLesionSlice> dataLesionSliceList) {
        return saveBatch(dataLesionSliceList, dataLesionSliceList.size());
    }


    /**
     * 获取病灶信息
     *
     * @param medicineId 数据集ID
     * @return List<DataLesionSliceVO> 病灶信息list
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<DataLesionSliceVO> get(Long medicineId) {
        QueryWrapper<DataLesionSlice> dataLesionSliceQueryWrapper = new QueryWrapper<>();
        dataLesionSliceQueryWrapper.lambda().eq(DataLesionSlice::getMedicineId, medicineId);
        List<DataLesionSlice> dataLesionSlices = baseMapper.selectList(dataLesionSliceQueryWrapper);
        List<DataLesionSliceVO> dataLesionSliceVOS = new ArrayList<>();
        dataLesionSlices.forEach(dataLesionSlice -> {
            DataLesionSliceVO dataLesionSliceVO = new DataLesionSliceVO();
            dataLesionSliceVO.setId(dataLesionSlice.getId());
            dataLesionSliceVO.setLesionOrder(dataLesionSlice.getLesionOrder());
            dataLesionSliceVO.setSliceDesc(dataLesionSlice.getSliceDesc());
            dataLesionSliceVO.setList(dataLesionSlice.getDrawInfo());
            dataLesionSliceVOS.add(dataLesionSliceVO);
        });
        return dataLesionSliceVOS;
    }

    /**
     * 删除病灶信息
     *
     * @param dataLesionSliceDeleteDTO 病灶信息删除DTO
     * @return boolean 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(DataLesionSliceDeleteDTO dataLesionSliceDeleteDTO) {
        dataLesionSliceMapper.deleteByMedicineIdAndOrder(dataLesionSliceDeleteDTO.getId());
        return true;
    }

    /**
     * 修改病灶信息
     *
     * @param dataLesionSliceUpdateDTO 病灶信息更新DTO
     * @return boolean 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(DataLesionSliceUpdateDTO dataLesionSliceUpdateDTO) {
        DataLesionSlice dataLesionSlice = baseMapper.selectById(dataLesionSliceUpdateDTO.getId());
        dataLesionSlice.setLesionOrder(dataLesionSliceUpdateDTO.getLesionOrder());
        dataLesionSlice.setSliceDesc(dataLesionSliceUpdateDTO.getSliceDesc());
        JSONArray jsonArray = new JSONArray();
        dataLesionSliceUpdateDTO.getList().forEach(dataLesionDrawInfoDTO -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("drawId", dataLesionDrawInfoDTO.getDrawId());
            jsonObject.put("sliceNumber", dataLesionDrawInfoDTO.getSliceNumber());
            jsonArray.add(jsonObject);
        });
        dataLesionSlice.setDrawInfo(jsonArray.toJSONString());
        baseMapper.updateById(dataLesionSlice);
        return true;
    }

    /**
     * 保存时根据数据集Id清空病灶信息
     *
     * @param medicineId 数据集ID
     * @return boolean      是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByMedicineId(Long medicineId) {
        dataLesionSliceMapper.deleteByMedicineId(medicineId);
        return true;
    }
}
