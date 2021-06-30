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
 * @description 医学数据集服务实现类
 * @date 2020-11-11
 */
@Service
public class DataMedicineServiceImpl extends ServiceImpl<DataMedicineMapper, DataMedicine> implements DataMedicineService {

    /**
     * 医学数据集文件服务
     */
    @Autowired
    private DataMedicineFileService dataMedicineFileService;

    @Autowired
    private DataMedicineMapper dataMedicineMapper;

    /**
     * 数据回收服务
     */
    @Autowired
    private RecycleService recycleService;

    @Autowired
    private MinioUtil minioUtil;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 路径名前缀
     */
    @Value("${storage.file-store-root-path:/nfs/}")
    private String prefixPath;

    /**
     * dcm服务器地址
     */
    @Value("${dcm.host}")
    private String dcmHost;

    /**
     * dcm服务端口
     */
    @Value("${dcm.port}")
    private String dcmPort;

    /**
     * nfs服务器地址
     */
    @Value("${storage.file-store}")
    private String nfsHost;

    /**
     * 文件服务器账号地址 需要免密
     */
    @Value("${data.server.userName}")
    private String dataServerUserName;

    /**
     * 获取DataMedicine中所有属性
     */
    private final Field[] fields = DataMedicine.class.getDeclaredFields();

    /**
     * 医学自动标注
     *
     * @param medicineId 医学数据集ID
     */

    @Override
    public DataMedicine getDataMedicineById(Long medicineId) {
        return getById(medicineId);
    }

    /**
     * 检测是否为公共数据集
     *
     * @param id 数据集id
     * @return Boolean 是否为公共数据集
     */
    @Override
    public Boolean checkPublic(Long id, OperationTypeEnum type) {
        DataMedicine dataMedicine = baseMapper.selectById(id);
        return checkPublic(dataMedicine, type);
    }

    /**
     * 检测是否为公共数据集
     *
     * @param dataMedicine 数据集
     */
    @Override
    public Boolean checkPublic(DataMedicine dataMedicine, OperationTypeEnum type) {
        if (Objects.isNull(dataMedicine)) {
            return false;
        }
        if (DatasetTypeEnum.PUBLIC.getValue().equals(dataMedicine.getType())) {
            //操作类型校验公共数据集
            if (OperationTypeEnum.UPDATE.equals(type)) {
                BaseService.checkAdminPermission();
                //操作类型校验公共数据集
            } else if (OperationTypeEnum.LIMIT.equals(type)) {
                throw new BusinessException(DATASET_PUBLIC_LIMIT_ERROR);
            } else {
                return true;
            }

        }
        return false;
    }


    /**
     * 数据还原
     *
     * @param dto 数据清理参数
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
                    //还原数据集状态
                    baseMapper.updateStatusById(datasetId, false);
                    //还原数据集文件状态
                    dataMedicineFileService.updateStatusById(datasetId, false);
                    return;
                }
            }

        }
    }


    /**
     * 根据医学数据集Id删除数据
     *
     * @param datasetId         医学数据集Id
     */
    @Override
    public void deleteByDatasetId(Long datasetId) {
        baseMapper.deleteByDatasetId(datasetId);
    }

    /**
     * 导入医学数据集
     *
     * @param dataMedicineImportDTO 导入医学数据集参数
     * @return boolean 导入是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importDataMedicine(DataMedicineImportDTO dataMedicineImportDTO) {
        DataMedicine dataMedicineUpdate = baseMapper.selectById(dataMedicineImportDTO.getId());
        baseMapper.updateById(dataMedicineUpdate);
        dataMedicineFileService.save(dataMedicineImportDTO.getDataMedicineFileCreateList(), dataMedicineUpdate);
        //上传dcm文件到dcm服务器
        String command = String.format(DcmConstant.DCM_UPLOAD, dataServerUserName, nfsHost, prefixPath, dcmHost, dcmPort,
                prefixPath + File.separator + bucketName + File.separator + "dataset" + File.separator + "dcm" + File.separator + dataMedicineImportDTO.getId() + File.separator + "origin");
        try {
            Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "org.dubhe.dcm file upload fail");
            throw new BusinessException("dcm文件上传失败");
        }
        return true;
    }

    /**
     * 创建医学数据集
     *
     * @param dataMedicineCreateDTO 创建医学数据集参数
     * @return Long 医学数据集ID
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
     * 更新医学数据集
     *
     * @param dataMedicine 医学数据集
     */
    @Override
    public void updateByMedicineId(DataMedicine dataMedicine) {
        baseMapper.updateById(dataMedicine);
    }

    /**
     * 删除数据集
     *
     * @param dataMedicineDeleteDTO 删除数据集参数
     * @return boolean 是否删除成功
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
     * 删除数据集
     *
     * @return boolean 是否删除成功
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
     * 添加医学数据集删除回收数据
     *
     * @param id 医学数据集ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRecycleDataByDeleteDataset(Long id) {
        //删除MinIO文件
        try {
            //落地回收详情数据文件回收信息
            List<RecycleDetailCreateDTO> detailList = new ArrayList<>();
            detailList.add(RecycleDetailCreateDTO.builder()
                    .recycleCondition(id.toString())
                    .recycleType(RecycleTypeEnum.TABLE_DATA.getCode())
                    .recycleNote(RecycleTool.generateRecycleNote("落地 数据集DB 数据文件回收", id))
                    .build());
            //落地回收详情minio 数据文件回收信息
            detailList.add(RecycleDetailCreateDTO.builder()
                    .recycleCondition(prefixPath + bucketName + DcmConstant.DCM_FILE_SEPARATOR + DcmConstant.DCM_ANNOTATION_PATH + id)
                    .recycleType(RecycleTypeEnum.FILE.getCode())
                    .recycleNote(RecycleTool.generateRecycleNote("落地 minio 数据文件回收", id))
                    .build());

            //落地回收信息
            RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                    .recycleModule(RecycleModuleEnum.BIZ_DATAMEDICINE.getValue())
                    .recycleCustom(RecycleResourceEnum.DATAMEDICINE_RECYCLE_FILE.getClassName())
                    .restoreCustom(RecycleResourceEnum.DATAMEDICINE_RECYCLE_FILE.getClassName())
                    .recycleDelayDate(NumberConstant.NUMBER_1)
                    .recycleNote(RecycleTool.generateRecycleNote("删除医学数据集相关信息", id))
                    .detailList(detailList)
                    .build();
            recycleService.createRecycleTask(recycleCreateDTO);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "DataMedicineServiceImpl addRecycleDataByDeleteDataset error {}", e);
        }

    }

    /**
     * 医学数据集查询
     *
     * @param dataMedicineQueryDTO 查询条件
     * @return MapMap<String, Object> 查询出对应的数据集
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
        //预置数据集类型校验
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
     * 医学数据集详情
     *
     * @param medicalId 医学数据集ID
     * @return DataMedicineVO 医学数据集VO
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
     * 根据医学数据集Id获取完成标注文件
     *
     * @param medicalId 医学数据集ID
     * @return JSONObject 完成标注文件
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
     * 根据医学数据集Id获取自动标注文件
     *
     * @param medicalId 医学数据集ID
     * @return DataMedicineCompleteAnnotationVO 标注文件
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
     * 根据医学数据集Id修改数据集
     *
     * @param dataMedcineUpdateDTO 医学数据集修改DTO
     * @param medicineId           医学数据集Id
     * @return boolean  修改是否成功
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
