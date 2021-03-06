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

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.base.dto.CommonPermissionDataDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.OperationTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.data.constant.ErrorEnum;
import org.dubhe.data.machine.constant.DataStateCodeConstant;
import org.dubhe.dcm.constant.DcmConstant;
import org.dubhe.dcm.dao.DataMedicineMapper;
import org.dubhe.dcm.domain.dto.*;
import org.dubhe.dcm.domain.entity.DataMedicine;
import org.dubhe.dcm.domain.vo.DataMedicineCompleteAnnotationVO;
import org.dubhe.dcm.domain.vo.DataMedicineVO;
import org.dubhe.dcm.service.DataMedicineFileService;
import org.dubhe.dcm.service.DataMedicineService;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.dubhe.data.constant.ErrorEnum.DATASET_PUBLIC_LIMIT_ERROR;


/**
 * @description ??????????????????????????????
 * @date 2020-11-11
 */
@Service
public class DataMedicineServiceImpl extends ServiceImpl<DataMedicineMapper, DataMedicine> implements DataMedicineService {

    /**
     * ???????????????????????????
     */
    @Autowired
    private DataMedicineFileService dataMedicineFileService;

    @Autowired
    private DataMedicineMapper dataMedicineMapper;

    /**
     * ??????????????????
     */
    @Autowired
    private RecycleService recycleService;

    @Autowired
    private MinioUtil minioUtil;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * ???????????????
     */
    @Value("${storage.file-store-root-path:/nfs/}")
    private String prefixPath;

    /**
     * dcm???????????????
     */
    @Value("${dcm.host}")
    private String dcmHost;

    /**
     * dcm????????????
     */
    @Value("${dcm.port}")
    private String dcmPort;

    /**
     * nfs???????????????
     */
    @Value("${storage.file-store}")
    private String nfsHost;

    /**
     * ??????????????????????????? ????????????
     */
    @Value("${data.server.userName}")
    private String dataServerUserName;

    /**
     * ??????DataMedicine???????????????
     */
    private final Field[] fields = DataMedicine.class.getDeclaredFields();

    /**
     * ??????????????????
     *
     * @param medicineId ???????????????ID
     */

    @Override
    public DataMedicine getDataMedicineById(Long medicineId) {
        return getById(medicineId);
    }

    /**
     * ??????????????????????????????
     *
     * @param id ?????????id
     * @return Boolean ????????????????????????
     */
    @Override
    public Boolean checkPublic(Long id, OperationTypeEnum type) {
        DataMedicine dataMedicine = baseMapper.selectById(id);
        return checkPublic(dataMedicine, type);
    }

    /**
     * ??????????????????????????????
     *
     * @param dataMedicine ?????????
     */
    @Override
    public Boolean checkPublic(DataMedicine dataMedicine, OperationTypeEnum type) {
        if (Objects.isNull(dataMedicine)) {
            return false;
        }
        if (DatasetTypeEnum.PUBLIC.getValue().equals(dataMedicine.getType())) {
            //?????????????????????????????????
            if (OperationTypeEnum.UPDATE.equals(type)) {
                BaseService.checkAdminPermission();
                //?????????????????????????????????
            } else if (OperationTypeEnum.LIMIT.equals(type)) {
                throw new BusinessException(DATASET_PUBLIC_LIMIT_ERROR);
            } else {
                return true;
            }

        }
        return false;
    }


    /**
     * ????????????
     *
     * @param dto ??????????????????
     */
    @Override
    public void allRollback(RecycleCreateDTO dto) {
        List<RecycleDetailCreateDTO> detailList = dto.getDetailList();
        if (CollectionUtil.isNotEmpty(detailList)) {
            for (RecycleDetailCreateDTO recycleDetailCreateDTO : detailList) {
                if (!Objects.isNull(recycleDetailCreateDTO) &&
                        RecycleTypeEnum.TABLE_DATA.getCode().compareTo(recycleDetailCreateDTO.getRecycleType()) == 0) {
                    Long datasetId = Long.valueOf(recycleDetailCreateDTO.getRecycleCondition());
                    DataMedicine dataMedicine = baseMapper.findDataMedicineByIdAndDeleteIsFalse(datasetId);
                    DataMedicine dataMedicineBySeriesUid = baseMapper.findDataMedicineBySeriesUid(dataMedicine.getSeriesInstanceUid());
                    if(dataMedicineBySeriesUid != null){
                        throw new BusinessException(ErrorEnum.MEDICINE_MEDICAL_ALREADY_EXISTS_RESTORE);
                    }
                    //?????????????????????
                    baseMapper.updateStatusById(datasetId, false);
                    //???????????????????????????
                    dataMedicineFileService.updateStatusById(datasetId, false);
                    return;
                }
            }

        }
    }


    /**
     * ?????????????????????Id????????????
     *
     * @param datasetId         ???????????????Id
     */
    @Override
    public void deleteByDatasetId(Long datasetId) {
        baseMapper.deleteByDatasetId(datasetId);
    }

    /**
     * ?????????????????????
     *
     * @param dataMedicineImportDTO ???????????????????????????
     * @return boolean ??????????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importDataMedicine(DataMedicineImportDTO dataMedicineImportDTO) {
        DataMedicine dataMedicineUpdate = baseMapper.selectById(dataMedicineImportDTO.getId());
        baseMapper.updateById(dataMedicineUpdate);
        dataMedicineFileService.save(dataMedicineImportDTO.getDataMedicineFileCreateList(), dataMedicineUpdate);
        //??????dcm?????????dcm?????????
        String command = String.format(DcmConstant.DCM_UPLOAD, dataServerUserName, nfsHost, prefixPath, dcmHost, dcmPort,
                prefixPath + File.separator + bucketName + File.separator + "dataset" + File.separator + "dcm" + File.separator + dataMedicineImportDTO.getId() + File.separator + "origin");
        try {
            Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "org.dubhe.dcm file upload fail");
            throw new BusinessException("dcm??????????????????");
        }
        return true;
    }

    /**
     * ?????????????????????
     *
     * @param dataMedicineCreateDTO ???????????????????????????
     * @return Long ???????????????ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DataMedicineCreateDTO dataMedicineCreateDTO) {
        Long originUserId = JwtUtils.getCurUserId();
        DataMedicine dataMedicine = dataMedicineMapper.findBySeriesUidAndNotId(dataMedicineCreateDTO.getSeriesInstanceUID(), originUserId);
        if (dataMedicine != null) {
            throw new BusinessException(ErrorEnum.MEDICINE_MEDICAL_ALREADY_EXISTS);
        }
        QueryWrapper<DataMedicine> dataMedicineQueryWrapper = new QueryWrapper<>();
        dataMedicineQueryWrapper.eq("name", dataMedicineCreateDTO.getName());
        int count = baseMapper.selectCount(dataMedicineQueryWrapper);
        if (count > MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.MEDICINE_NAME_ERROR);
        }

        DataMedicine dataMedicineCreate = DataMedicineCreateDTO.from(dataMedicineCreateDTO, JwtUtils.getCurUserId());
        save(dataMedicineCreate);
        return dataMedicineCreate.getId();
    }

    /**
     * ?????????????????????
     *
     * @param dataMedicine ???????????????
     */
    @Override
    public void updateByMedicineId(DataMedicine dataMedicine) {
        baseMapper.updateById(dataMedicine);
    }

    /**
     * ???????????????
     *
     * @param dataMedicineDeleteDTO ?????????????????????
     * @return boolean ??????????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(DataMedicineDeleteDTO dataMedicineDeleteDTO) {
        for (Long id : dataMedicineDeleteDTO.getIds()) {
            deleteDataMedicine(id);
        }
        return true;
    }

    /**
     * ???????????????
     *
     * @return boolean ??????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public boolean deleteDataMedicine(Long id) {
        DataMedicine dataMedicine = baseMapper.selectById(id);
        if (dataMedicine == null) {
            throw new BusinessException(ErrorEnum.DATAMEDICINE_ABSENT);
        }
        checkPublic(dataMedicine, OperationTypeEnum.UPDATE);
        if (dataMedicine.getStatus().equals(DataStateCodeConstant.AUTOMATIC_LABELING_STATE)) {
            throw new BusinessException(ErrorEnum.DATAMEDICINE_AUTOMATIC);
        }
        baseMapper.updateStatusById(id, true);
        dataMedicineFileService.updateStatusById(id, true);
        addRecycleDataByDeleteDataset(id);
        return true;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param id ???????????????ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRecycleDataByDeleteDataset(Long id) {
        //??????MinIO??????
        try {
            //??????????????????????????????????????????
            List<RecycleDetailCreateDTO> detailList = new ArrayList<>();
            detailList.add(RecycleDetailCreateDTO.builder()
                    .recycleCondition(id.toString())
                    .recycleType(RecycleTypeEnum.TABLE_DATA.getCode())
                    .recycleNote(RecycleTool.generateRecycleNote("?????? ?????????DB ??????????????????", id))
                    .build());
            //??????????????????minio ????????????????????????
            detailList.add(RecycleDetailCreateDTO.builder()
                    .recycleCondition(prefixPath + bucketName + DcmConstant.DCM_FILE_SEPARATOR + DcmConstant.DCM_ANNOTATION_PATH + id)
                    .recycleType(RecycleTypeEnum.FILE.getCode())
                    .recycleNote(RecycleTool.generateRecycleNote("?????? minio ??????????????????", id))
                    .build());

            //??????????????????
            RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                    .recycleModule(RecycleModuleEnum.BIZ_DATAMEDICINE.getValue())
                    .recycleCustom(RecycleResourceEnum.DATAMEDICINE_RECYCLE_FILE.getClassName())
                    .restoreCustom(RecycleResourceEnum.DATAMEDICINE_RECYCLE_FILE.getClassName())
                    .recycleDelayDate(NumberConstant.NUMBER_1)
                    .recycleNote(RecycleTool.generateRecycleNote("?????????????????????????????????", id))
                    .detailList(detailList)
                    .build();
            recycleService.createRecycleTask(recycleCreateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "DataMedicineServiceImpl addRecycleDataByDeleteDataset error {}", e);
        }

    }

    /**
     * ?????????????????????
     *
     * @param dataMedicineQueryDTO ????????????
     * @return MapMap<String, Object> ???????????????????????????
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> listVO(DataMedicineQueryDTO dataMedicineQueryDTO) {
        if (dataMedicineQueryDTO.getCurrent() == null || dataMedicineQueryDTO.getSize() == null) {
            throw new BusinessException(ErrorEnum.PARAM_ERROR);
        }
        QueryWrapper<DataMedicine> wrapper = WrapperHelp.getWrapper(dataMedicineQueryDTO);
        if (dataMedicineQueryDTO.getAnnotateType() != null) {
            if (dataMedicineQueryDTO.getAnnotateType() % MagicNumConstant.ONE_THOUSAND == MagicNumConstant.ZERO) {
                wrapper.between("annotate_type", dataMedicineQueryDTO.getAnnotateType()
                        , dataMedicineQueryDTO.getAnnotateType() + MagicNumConstant.ONE_THOUSAND);
            } else {
                wrapper.eq("annotate_type", dataMedicineQueryDTO.getAnnotateType());
            }
        }
        wrapper.eq("deleted", MagicNumConstant.ZERO)
                .eq("type", dataMedicineQueryDTO.getType());
        //???????????????????????????
        if (!Objects.isNull(dataMedicineQueryDTO.getType()) && dataMedicineQueryDTO.getType().compareTo(DatasetTypeEnum.PUBLIC.getValue()) == 0) {
            DataContext.set(CommonPermissionDataDTO.builder().id(null).type(true).build());
        }
        if (StringUtils.isNotBlank(dataMedicineQueryDTO.getName())) {
            wrapper.lambda().and(w ->
                    w.eq(DataMedicine::getId, dataMedicineQueryDTO.getName())
                            .or()
                            .like(DataMedicine::getName, dataMedicineQueryDTO.getName())
            );
        }
        if (StringUtils.isNotBlank(dataMedicineQueryDTO.getSort())) {
            for (Field field : fields) {
                if (field.getName().equals(dataMedicineQueryDTO.getSort())) {
                    field.setAccessible(true);
                    TableField annotation = field.getAnnotation(TableField.class);
                    if (annotation == null) {
                        continue;
                    }
                    if ("desc".equals(dataMedicineQueryDTO.getOrder())) {
                        wrapper.orderByDesc(annotation.value());
                    } else if ("asc".equals(dataMedicineQueryDTO.getOrder())) {
                        wrapper.orderByAsc(annotation.value());
                    }
                }
            }
        } else {
            wrapper.lambda().orderByDesc(DataMedicine::getUpdateTime);
        }
        Page<DataMedicineVO> pages = new Page<DataMedicineVO>() {{
            setCurrent(dataMedicineQueryDTO.getCurrent());
            setSize(dataMedicineQueryDTO.getSize());
            setTotal(baseMapper.selectCount(wrapper));
            List<DataMedicineVO> collect = baseMapper.selectList(
                    wrapper
                            .last(" limit " + (dataMedicineQueryDTO.getCurrent() - NumberConstant.NUMBER_1) * dataMedicineQueryDTO.getSize() + ", " + dataMedicineQueryDTO.getSize())
            ).stream().map(DataMedicineVO::from).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                setRecords(collect);
            }
        }};
        return PageUtil.toPage(pages);
    }

    /**
     * ?????????????????????
     *
     * @param medicalId ???????????????ID
     * @return DataMedicineVO ???????????????VO
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public DataMedicineVO get(Long medicalId) {
        DataMedicine dataMedicine = baseMapper.selectById(medicalId);
        if (dataMedicine == null) {
            throw new BusinessException(ErrorEnum.DATAMEDICINE_ABSENT);
        } else if (dataMedicine.getStatus().equals(DataStateCodeConstant.AUTOMATIC_LABELING_STATE)) {
            throw new BusinessException(ErrorEnum.DATAMEDICINE_AUTOMATIC);
        }
        if (checkPublic(medicalId, OperationTypeEnum.SELECT)) {
            DataContext.set(CommonPermissionDataDTO.builder().id(medicalId).type(true).build());
        }
        DataMedicineVO dataMedicineVO = DataMedicineVO.from(dataMedicine);
        return dataMedicineVO;
    }

    /**
     * ?????????????????????Id????????????????????????
     *
     * @param medicalId ???????????????ID
     * @return JSONObject ??????????????????
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public JSONObject getFinished(Long medicalId) {
        DataMedicine dataMedicine = baseMapper.selectById(medicalId);
        if (dataMedicine == null) {
            throw new BusinessException(ErrorEnum.DATAMEDICINE_ABSENT);
        }
        try {
            String finishedFilePath = DcmConstant.DCM_ANNOTATION_PATH + medicalId + DcmConstant.DCM_ANNOTATION;
            String annotation = minioUtil.readString(bucketName, finishedFilePath);
            JSONObject jsonObject = JSONObject.parseObject(annotation);
            return jsonObject;
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "MinIO read the dataMedicine file error", e);
        }
        return null;
    }

    /**
     * ?????????????????????Id????????????????????????
     *
     * @param medicalId ???????????????ID
     * @return DataMedicineCompleteAnnotationVO ????????????
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public DataMedicineCompleteAnnotationVO getAuto(Long medicalId) {
        DataMedicine dataMedicine = baseMapper.selectById(medicalId);
        if (dataMedicine == null) {
            throw new BusinessException(ErrorEnum.DATAMEDICINE_ABSENT);
        }
        DataMedicineCompleteAnnotationVO dataMedicineCompleteAnnotationVO = new DataMedicineCompleteAnnotationVO();
        try {
            String autoFilePath = DcmConstant.DCM_ANNOTATION_PATH + medicalId + DcmConstant.DCM_MERGE_ANNOTATION;
            String annotation = minioUtil.readString(bucketName, autoFilePath);
            JSONObject jsonObject = JSONObject.parseObject(annotation);
            dataMedicineCompleteAnnotationVO.setSeriesInstanceUID(jsonObject.getString(DcmConstant.SERIES_INSTABCE_UID));
            dataMedicineCompleteAnnotationVO.setStudyInstanceUID(jsonObject.getString(DcmConstant.STUDY_INSTANCE_UID));
            dataMedicineCompleteAnnotationVO.setAnnotations(jsonObject.getString(DcmConstant.ANNOTATION));
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "MinIO read the dataMedicine file error {}", e);
        }
        return dataMedicineCompleteAnnotationVO;
    }

    /**
     * ?????????????????????Id???????????????
     *
     * @param dataMedcineUpdateDTO ?????????????????????DTO
     * @param medicineId           ???????????????Id
     * @return boolean  ??????????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public boolean update(DataMedcineUpdateDTO dataMedcineUpdateDTO, Long medicineId) {
        DataMedicine dataMedicine = baseMapper.selectById(medicineId);
        if (dataMedicine == null) {
            throw new BusinessException(ErrorEnum.DATAMEDICINE_ABSENT);
        }
        checkPublic(dataMedicine, OperationTypeEnum.UPDATE);
        dataMedicine.setName(dataMedcineUpdateDTO.getName());
        dataMedicine.setUpdateUserId(JwtUtils.getCurUserId());
        if (dataMedcineUpdateDTO.getRemark() != null) {
            dataMedicine.setRemark(dataMedcineUpdateDTO.getRemark());
        }

        int count;
        try {
            count = baseMapper.updateById(dataMedicine);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorEnum.DATASET_NAME_DUPLICATED_ERROR, null, e);
        }
        if (count == MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH);
        }
        return true;
    }
}
