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

package org.dubhe.data.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.logging.log4j.util.Strings;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.dto.PtTrainDataSourceStatusQueryDTO;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.dto.UserSmallDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.OperationTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.service.AdminClient;
import org.dubhe.data.client.TrainServerClient;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.DatasetVersionMapper;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.*;
import org.dubhe.data.domain.vo.DatasetVersionCriteriaVO;
import org.dubhe.data.domain.vo.DatasetVersionVO;
import org.dubhe.data.machine.constant.DataStateCodeConstant;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.utils.StateIdentifyUtil;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.*;
import org.dubhe.data.util.ConversionUtil;
import org.dubhe.data.util.GeneratorKeyUtil;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.dubhe.data.constant.Constant.*;

/**
 * @description ????????????????????? ???????????????
 * @date 2020-05-14
 */
@Service
public class DatasetVersionServiceImpl extends ServiceImpl<DatasetVersionMapper, DatasetVersion>
        implements DatasetVersionService {


    /**
     * ???????????????
     */
    @Resource
    private DatasetServiceImpl datasetService;

    /**
     * ???????????????mapper
     */
    @Resource
    private DatasetVersionMapper datasetVersionMapper;

    /**
     * ???????????????????????????
     */
    @Resource
    private DatasetVersionFileService datasetVersionFileService;

    /**
     * ????????????????????????????????????
     */
    @Resource
    private DatasetVersionFileServiceImpl datasetVersionFileServiceImpl;

    /**
     * minIo???????????????
     */
    @Resource
    private MinioUtil minioUtil;

    /**
     * ?????????
     */
    @Autowired
    private BasePool pool;

    /**
     * bucketName
     */
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * ????????????????????????????????????
     */
    @Resource
    private StateIdentifyUtil stateIdentify;

    /**
     * ????????????
     */
    @Resource
    @Lazy
    public FileService fileService;

    /**
     * ????????????
     */
    @Autowired
    private AdminClient adminClient;

    /**
     * feign??????????????????
     */
    @Resource
    private TrainServerClient trainServiceClient;


    /**
     * ??????????????????(json to txt)
     */
    @Autowired
    private ConversionUtil conversionUtil;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private DataFileAnnotationServiceImpl dataFileAnnotationServiceImpl;

    @Autowired
    private DataFileAnnotationService dataFileAnnotationService;

    @Autowired
    private GeneratorKeyUtil generatorKeyUtil;

    @Autowired
    private DatasetLabelService datasetLabelService;

    /**
     * esSearch??????
     */
    @Value("${es.index}")
    private String esIndex;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private BulkProcessor bulkProcessor;

    private final ConcurrentHashMap<Long, Boolean> copyFlag = new ConcurrentHashMap<>();


    private static final String ANNOTATION = "annotation";

    private static final String VERSION_FILE = "versionFile";

    /**
     * ?????????????????????
     *
     * @param datasetVersionCreateDTO ?????????????????????
     * @return String ?????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String publish(DatasetVersionCreateDTO datasetVersionCreateDTO) {
        datasetVersionCreateDTO.setVersionName(getNextVersionName(datasetVersionCreateDTO.getDatasetId()));
        Dataset dataset = datasetService.getById(datasetVersionCreateDTO.getDatasetId());
        // 1.???????????????????????????
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetVersionCreateDTO.getDatasetId(), null);
        }
        //?????????????????????????????????
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        if("V0001".equals(dataset.getCurrentVersionName())&&dataset.getDataType().equals(MagicNumConstant.TWO)){
            throw new BusinessException(ErrorEnum.DATASET_PUBLISH_REJECT);
        }
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        // ?????????????????????????????????
        DataStateEnum currentDatasetStatus = stateIdentify.getStatus(dataset.getId(), dataset.getCurrentVersionName(), false);
        dataset.setStatus(currentDatasetStatus.getCode());
        if (!dataset.getStatus().equals(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE) && !dataset.getStatus().equals(DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE)
                && !dataset.getStatus().equals(DataStateCodeConstant.TARGET_COMPLETE_STATE)) {
            throw new BusinessException(ErrorEnum.DATASET_ANNOTATION_NOT_FINISH, "id:" + datasetVersionCreateDTO.getDatasetId(), null);
        }
        // 2.?????????????????????????????????????????????
        List<DatasetVersion> datasetVersionList = datasetVersionMapper.
                findDatasetVersion(datasetVersionCreateDTO.getDatasetId(), datasetVersionCreateDTO.getVersionName());
        if (CollectionUtil.isNotEmpty(datasetVersionList)) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_EXIST, null, null);
        }
        publishDo(dataset, datasetVersionCreateDTO);
        return datasetVersionCreateDTO.getVersionName();
    }

    /**
     * ???????????????????????????
     *
     * @param dataset                 ?????????
     * @param datasetVersionCreateDTO ?????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishDo(Dataset dataset, DatasetVersionCreateDTO datasetVersionCreateDTO) {
        String versionUrl = dataset.getUri() + File.separator
                + "versionFile" + File.separator + datasetVersionCreateDTO.getVersionName();
        DatasetVersion datasetVersion = new DatasetVersion(dataset.getCurrentVersionName(), versionUrl, datasetVersionCreateDTO);
        datasetVersion.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        datasetVersion.setOriginUserId(dataset.getCreateUserId());
        datasetVersion.setDataConversion(ConversionStatusEnum.PUBLISHING.getValue());
        datasetVersion.setOfRecord(datasetVersionCreateDTO.getOfRecord());
        //???????????????????????????
        datasetVersionMapper.insert(datasetVersion);
        // ???????????????????????????
        datasetService.updateVersionName(dataset.getId(), datasetVersionCreateDTO.getVersionName());
    }

    /**
     * ?????????????????????
     *
     * @param dataset        ?????????
     * @param datasetVersion ???????????????
     */
    public void publishCopyFile(Dataset dataset, DatasetVersion datasetVersion) {
        //????????????????????????
        copyFlag.put(datasetVersion.getId(), false);
        // targetDir = dataset/25/versionFile/V0001?????????????????????????????????????????????/origin
        String targetDir = dataset.getUri() + File.separator + VERSION_FILE + File.separator
                + datasetVersion.getVersionName() + File.separator + "origin";
        //?????????????????????????????????????????????????????????
        List<DatasetVersionFile> datasetVersionFiles =
                datasetVersionFileService.findByDatasetIdAndVersionName(dataset.getId(), datasetVersion.getVersionName());
        //??????????????????????????????????????????
        List<String> picNames = new ArrayList<>();
        //??????????????????????????????????????????URL
        List<String> picUrls = fileService.selectUrls(dataset.getId(), datasetVersion.getVersionName());
        picUrls.forEach(picUrl -> picNames.add(StringUtils.substringAfter(picUrl, "/")));
        try {
            minioUtil.copyDir(bucketName, picNames, targetDir);
            datasetVersion.setDataConversion(ConversionStatusEnum.NOT_CONVERSION.getValue());
            getBaseMapper().updateById(datasetVersion);
            if (AnnotateTypeEnum.OBJECT_DETECTION.getValue().equals(dataset.getAnnotateType())) {
                LogUtil.info(LogEnum.BIZ_DATASET, "yolo conversion start");
                conversionUtil.txtConversion(targetDir, dataset.getId());
                LogUtil.info(LogEnum.BIZ_DATASET, "yolo conversion end");
            }
            copyFlag.remove(datasetVersion.getId());
            if (dataset.getAnnotateType().equals(MagicNumConstant.TWO) && datasetVersion.getOfRecord().equals(MagicNumConstant.ONE)) {
                Task task = Task.builder().total(datasetVersionFiles.size())
                        .datasetId(dataset.getId())
                        .type(DataTaskTypeEnum.OFRECORD.getValue())
                        .labels("")
                        .datasetVersionId(datasetVersion.getId()).build();
                taskService.createTask(task);
            }
        } catch (Exception e) {
            copyFlag.put(datasetVersion.getId(), true);
            LogUtil.error(LogEnum.BIZ_DATASET, "fail to copy or conversion:{}", e);
            throw new BusinessException(ErrorEnum.DATASET_VERSION_ANNOTATION_COPY_EXCEPTION);
        }
    }

    /**
     * ????????????????????????
     *
     * @param id ?????????id
     * @return List<DatasetVersionVO> ????????????
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public List<DatasetVersionVO> versionList(Long id) {
        List<DatasetVersionVO> list = new ArrayList<>();
        DatasetVersionQueryCriteriaDTO datasetVersionQueryCriteria = new DatasetVersionQueryCriteriaDTO();
        datasetVersionQueryCriteria.setDatasetId(id);
        datasetVersionQueryCriteria.setDeleted(NOT_DELETED);
        List<DatasetVersion> datasetVersions = datasetVersionMapper.selectList(WrapperHelp.getWrapper(datasetVersionQueryCriteria));
        datasetVersions.forEach(datasetVersion -> {
            Integer imageCounts = datasetVersionFileService.getImageCountsByDatasetIdAndVersionName(
                    datasetVersion.getDatasetId(),
                    datasetVersion.getVersionName()
            );
            DatasetVersionVO datasetVersionVO = new DatasetVersionVO();
            datasetVersionVO.setVersionName(datasetVersion.getVersionName());
            datasetVersionVO.setVersionNote(datasetVersion.getVersionNote());
            datasetVersionVO.setImageCounts(imageCounts);
            if (!ConversionStatusEnum.NOT_COPY.getValue().equals(datasetVersion.getDataConversion())) {
                datasetVersionVO.setVersionUrl(datasetVersion.getVersionUrl());
            }
            if (ConversionStatusEnum.IS_CONVERSION.getValue().equals(datasetVersion.getDataConversion())) {
                String binaryUrl = datasetVersion.getVersionUrl() + File.separator + OFRECORD + File.separator + TRAIN;
                datasetVersionVO.setVersionOfRecordUrl(binaryUrl);
            }
            list.add(datasetVersionVO);
        });
        return list;
    }

    /**
     * ??????????????????
     *
     * @param version     ???????????????
     */
    public void saveDatasetVersionFiles(DatasetVersion version) {
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService.
                findByDatasetIdAndVersionName(version.getDatasetId(), version.getVersionSource());
        if (datasetVersionFiles != null && datasetVersionFiles.size() > MagicNumConstant.ZERO) {
            datasetVersionFiles.stream().forEach(datasetVersionFile -> {
                datasetVersionFile.setVersionName(version.getVersionName());
                datasetVersionFile.setBackupStatus(datasetVersionFile.getAnnotationStatus());
            });
            saveDatasetFileAnnotation(datasetVersionFiles, version.getVersionSource());
        }
    }

    /**
     * ????????????????????????
     *
     * @param datasetVersionFiles   ??????????????????
     * @param versionSource         ????????????
     */
    public void saveDatasetFileAnnotation(List<DatasetVersionFile> datasetVersionFiles,String versionSource){
        //versionSource???????????????????????????????????????versionFile??????
        datasetVersionFiles.stream().forEach(datasetVersionFile -> {
            if (null == datasetVersionFile.getAnnotationStatus()) {
                datasetVersionFile.setAnnotationStatus(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE);
            }
        });
        Queue<Long> dataFileIds = generatorKeyUtil.getSequenceByBusinessCode(Constant.DATA_VERSION_FILE, datasetVersionFiles.size());
        Queue<Long> versionFileIds = new LinkedList<>();
        for (DatasetVersionFile datasetVersionFile : datasetVersionFiles) {
            Long dataFileId = dataFileIds.poll();
            datasetVersionFile.setId(dataFileId);
            versionFileIds.add(dataFileId);
        }
        List<List<DatasetVersionFile>> splitVersionFiles = CollectionUtil.split(datasetVersionFiles, MagicNumConstant.FOUR_THOUSAND);
        splitVersionFiles.forEach(splitVersionFile->datasetVersionFileServiceImpl.getBaseMapper().saveList(splitVersionFile));
        //???????????????dataFileAnnotation??????
        List<DataFileAnnotation> dataFileAnnotations = dataFileAnnotationService.getAnnotationByVersion(
                datasetVersionFiles.get(0).getDatasetId(),versionSource, MagicNumConstant.TWO);
        Map<Long,List<DataFileAnnotation>> versionFileAnnotations = dataFileAnnotations.stream()
                .collect(Collectors.toMap(DataFileAnnotation::getVersionFileId, dataFileAnnotation -> {
            List<DataFileAnnotation> dataFileAnnotationList = new ArrayList<>();
            dataFileAnnotationList.add(dataFileAnnotation);
            return dataFileAnnotationList;
        },( oldVal, newVal) -> {
            oldVal.addAll(newVal);
            return oldVal;
        }));
        List<DataFileAnnotation> dataFileAnnotationList = new ArrayList<>();
        List<DataFileAnnotation> updateAnnotations = new ArrayList<>();
        LinkedHashMap<Long, List<DataFileAnnotation>> sortAnnotations = versionFileAnnotations.entrySet().stream().
                sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        sortAnnotations.entrySet().stream().map(Map.Entry::getValue).forEach(versionFileAnnotation->{
            Long versionFileId = versionFileIds.poll();
            versionFileAnnotation.forEach(annotation->{
                if(annotation.getStatus().equals(MagicNumConstant.TWO) && annotation.getInvariable().equals(MagicNumConstant.ONE)){
                    annotation.setStatus(MagicNumConstant.TWO);
                    annotation.setInvariable(MagicNumConstant.ONE);
                    annotation.setVersionFileId(versionFileId);
                    dataFileAnnotationList.add(annotation);
                }
                if(annotation.getStatus().equals(MagicNumConstant.ONE) && annotation.getInvariable().equals(MagicNumConstant.ONE)){
                    annotation.setStatus(MagicNumConstant.TWO);
                    updateAnnotations.add(annotation);
                }
                if(annotation.getStatus().equals(MagicNumConstant.ZERO) && annotation.getInvariable().equals(MagicNumConstant.ZERO)){
                    annotation.setStatus(MagicNumConstant.TWO);
                    annotation.setInvariable(MagicNumConstant.ONE);
                    annotation.setVersionFileId(versionFileId);
                    updateAnnotations.add(annotation);
                }
            });
        });
        if(!CollectionUtils.isEmpty(dataFileAnnotationList)){
            Queue<Long> dataFileAnnotionIds = generatorKeyUtil.getSequenceByBusinessCode(Constant.DATA_FILE_ANNOTATION, dataFileAnnotationList.size());
            for (DataFileAnnotation dataFileAnnotation : dataFileAnnotationList) {
                dataFileAnnotation.setId(dataFileAnnotionIds.poll());
            }
            List<List<DataFileAnnotation>> splitAnnotations = CollectionUtil.split(dataFileAnnotationList, MagicNumConstant.FOUR_THOUSAND);
            splitAnnotations.forEach(splitAnnotation->dataFileAnnotationServiceImpl.getBaseMapper().insertBatch(splitAnnotation));
        }
        dataFileAnnotationService.updateDataFileAnnotations(updateAnnotations);
    }

    /**
     * ??????????????????
     *
     * @param userDtoMap     ????????????
     * @param datasetVersion ???????????????
     * @return UserSmallDTO  ????????????
     */
    public UserSmallDTO getUserSmallDTO(Map<Long, UserSmallDTO> userDtoMap, DatasetVersion datasetVersion) {
        UserSmallDTO userSmallDTO = null;
        if (!userDtoMap.containsKey(datasetVersion.getCreateUserId())) {
            UserDTO userDTO = adminClient.getUsers(datasetVersion.getCreateUserId()).getData();
            if (ObjectUtil.isNotNull(userDTO)) {
                userSmallDTO = new UserSmallDTO(userDTO);
                userDtoMap.put(datasetVersion.getCreateUserId(), userSmallDTO);
            }
        } else {
            userSmallDTO = userDtoMap.get(datasetVersion.getCreateUserId());
        }
        return userSmallDTO;
    }

    /**
     * ?????????????????????
     *
     * @param datasetVersionQueryCriteria ????????????
     * @return Map<String, Object>        ????????????
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getList(DatasetVersionQueryCriteriaDTO datasetVersionQueryCriteria) {
        //????????????
        if (datasetVersionQueryCriteria.getCurrent() == null || datasetVersionQueryCriteria.getSize() == null) {
            throw new BusinessException(ErrorEnum.PARAM_ERROR);
        }
        //???????????????????????????
        Dataset dataset = datasetService.getById(datasetVersionQueryCriteria.getDatasetId());
        if (dataset == null) {
            throw new BusinessException(ErrorEnum.PARAM_ERROR);
        }

        //?????????????????????????????????
        QueryWrapper<DatasetVersion> wrapper = WrapperHelp.getWrapper(datasetVersionQueryCriteria);
        Page<DatasetVersionVO> pages = new Page<DatasetVersionVO>() {{
            setCurrent(datasetVersionQueryCriteria.getCurrent());
            setSize(datasetVersionQueryCriteria.getSize());
            setTotal(datasetVersionMapper.selectCount(wrapper));
            List<DatasetVersionVO> collect = datasetVersionMapper.selectList(
                    wrapper.last(" limit " + (datasetVersionQueryCriteria.getCurrent() - NumberConstant.NUMBER_1) * datasetVersionQueryCriteria.getSize() + ", " + datasetVersionQueryCriteria.getSize())
                    .ne("deleted", MagicNumConstant.ONE)
            ).stream().map(val -> {
                        return DatasetVersionVO.from(val,
                                dataset,
                                datasetService.progress(new ArrayList<Long>() {{
                                    add(dataset.getId());
                                }}).get(dataset.getId()),
                                datasetVersionFileService.selectDatasetVersionFileCount(val),
                                getUserSmallDTO(new HashMap<>(MagicNumConstant.SIXTEEN), val),
                                getUserSmallDTO(new HashMap<>(MagicNumConstant.SIXTEEN), val),
                                DatasetTypeEnum.PUBLIC.getValue().compareTo(dataset.getType()) != 0
                        );
                    }
            ).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                setRecords(collect);
            }
        }};
        return PageUtil.toPage(pages);
    }



    /**
     * ????????????
     *
     * @param datasetId          ?????????id
     * @param versionName        ?????????
     * @param datasetVersionUrls ???????????????url
     */
    public void delVersion(Long datasetId, String versionName, List<String> datasetVersionUrls) {
        Dataset dataset = datasetService.getById(datasetId);
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH, "id:" + datasetId, null);
        }
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        if (versionName.equals(dataset.getCurrentVersionName())) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_DELETE_CURRENT_ERROR);
        }
        UpdateWrapper<DatasetVersion> datasetVersionUpdateWrapper = new UpdateWrapper<>();
        datasetVersionUpdateWrapper.eq("dataset_id", datasetId)
                .eq("version_name", versionName);
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setDeleted(true);
        baseMapper.update(datasetVersion, datasetVersionUpdateWrapper);

        UpdateWrapper<DatasetVersionFile> datasetVersionFileUpdateWrapper = new UpdateWrapper<>();
        datasetVersionFileUpdateWrapper.eq("dataset_id", datasetId)
                .eq("version_name", versionName);
        DatasetVersionFile datasetVersionFile = new DatasetVersionFile();
        datasetVersionFile.setStatus(MagicNumConstant.ONE);
        datasetVersionFileServiceImpl.getBaseMapper().update(datasetVersionFile, datasetVersionFileUpdateWrapper);
        //?????????????????????minio??????
        datasetVersionUrls.forEach(dataseturl -> {
            try {
                minioUtil.del(bucketName, dataseturl);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "MinIO delete the dataset version file error", e);
            }
        });
    }

    /**
     * ?????????????????????
     *
     * @param datasetVersionDeleteDTO ???????????????????????????
     */
    @Override
    @DataPermissionMethod
    public void versionDelete(DatasetVersionDeleteDTO datasetVersionDeleteDTO) {
        datasetService.checkPublic(datasetVersionDeleteDTO.getDatasetId(), OperationTypeEnum.UPDATE);
        //??????????????????????????????url
        List<String> thisUrls = datasetVersionMapper.selectVersionUrl(datasetVersionDeleteDTO.getDatasetId(), datasetVersionDeleteDTO.getVersionName());
        List<String> datasetVersionUrls = new ArrayList<>();
        thisUrls.forEach(url -> {
            datasetVersionUrls.add(url);
            datasetVersionUrls.add(url + StrUtil.SLASH + "ofrecord" + StrUtil.SLASH + "train");
        });
        if (!CollectionUtils.isEmpty(datasetVersionUrls)) {
            //????????????url????????????
            PtTrainDataSourceStatusQueryDTO dto = new PtTrainDataSourceStatusQueryDTO();
            DataResponseBody<Map<String, Boolean>> trainDataSourceStatusData = trainServiceClient.getTrainDataSourceStatus(dto.setDataSourcePath(datasetVersionUrls));
            if (!trainDataSourceStatusData.succeed() || Objects.isNull(trainDataSourceStatusData.getData())) {
                throw new BusinessException(ErrorEnum.DATASET_VERSION_PTJOB_STATUS);
            }
            if (!trainDataSourceStatusData.getData().values().contains(false)) {
                this.delVersion(datasetVersionDeleteDTO.getDatasetId(), datasetVersionDeleteDTO.getVersionName(), datasetVersionUrls);
            }
        } else {
            this.delVersion(datasetVersionDeleteDTO.getDatasetId(), datasetVersionDeleteDTO.getVersionName(), datasetVersionUrls);
        }
    }

    /**
     * ?????????????????????
     *
     * @param datasetId   ?????????id
     * @param versionName ????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermissionMethod
    public void versionSwitch(Long datasetId, String versionName) {
        datasetService.checkPublic(datasetId, OperationTypeEnum.UPDATE);
        Dataset dataset = datasetService.getById(datasetId);
        // ????????????
        // 1.???????????????????????????
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetId, null);
        }
        //??????????????????????????????
        QueryWrapper<DatasetVersion> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DatasetVersion::getDatasetId, datasetId).eq(DatasetVersion::getVersionName, versionName);
        DatasetVersion datasetVersion = baseMapper.selectOne(queryWrapper);
        if(datasetVersion == null) {
            throw new BusinessException(ErrorEnum.DATASET_CHECK_VERSION_ERROR);
        }
        //?????????????????????????????????
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        // ????????????????????????????????????
        if (dataset.getStatus().equals(DataStateCodeConstant.AUTOMATIC_LABELING_STATE)
                || dataset.getStatus().equals(DataStateCodeConstant.TARGET_FOLLOW_STATE)
                || dataset.getStatus().equals(DataStateCodeConstant.TARGET_FAILURE_STATE)
        ) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_STATUS_NO_SWITCH, "id:" + datasetId, null);
        }
        dataFileAnnotationService.rollbackAnnotation(dataset.getId(), dataset.getCurrentVersionName()
                , MagicNumConstant.ONE, MagicNumConstant.ZERO);
        dataFileAnnotationService.rollbackAnnotation(dataset.getId(), dataset.getCurrentVersionName()
                , MagicNumConstant.TWO, MagicNumConstant.ONE);
        //????????????
        datasetVersionFileService.rollbackDataset(dataset);
        // 2.????????????
        datasetService.updateVersionName(datasetId, versionName);
        //????????????????????????????????????
        DataStateEnum status = stateIdentify.getStatus(dataset.getId(), versionName, true);
        datasetService.updateStatus(dataset.getId(), status);
    }

    /**
     * ??????????????????????????????
     *
     * @param datasetId ?????????id
     * @return String ???????????????????????????
     */
    @Override
    @DataPermissionMethod
    public String getNextVersionName(Long datasetId) {
        Dataset dataset = datasetService.getById(datasetId);
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetId, null);
        }
        String maxVersionName = datasetVersionMapper.getMaxVersionName(datasetId);
        if (StringUtils.isEmpty(maxVersionName)) {
            return Constant.DEFAULT_VERSION;
        } else {
            Integer versionName = Integer.parseInt(maxVersionName.substring(1)) + MagicNumConstant.ONE;
            return Constant.DATASET_VERSION_PREFIX + StringUtils.stringFillIn(versionName.toString(), MagicNumConstant.FOUR, MagicNumConstant.ZERO);
        }
    }

    /**
     * ???????????????????????????
     *
     * @param datasetId ?????????id
     */
    @Override
    public void updateStatusByDatasetId(Long datasetId, Boolean deleteFlag) {
        baseMapper.updateById(DatasetVersion.builder().datasetId(datasetId).deleted(deleteFlag).build());
    }


    /**
     * ????????????????????????
     *
     * @param datasetVersionId ??????id
     * @return int ??????????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishConvert(Long datasetVersionId, ConversionCreateDTO conversionCreateDTO) {
        LogUtil.info(LogEnum.BIZ_DATASET, "conversion call-back id:{},msg:{}", datasetVersionId, conversionCreateDTO.getMsg());
        DatasetVersion datasetVersion = getBaseMapper().selectById(datasetVersionId);
        if (CONVERSION_SUCCESS.equals(conversionCreateDTO.getMsg())) {
            datasetVersion.setDataConversion(ConversionStatusEnum.IS_CONVERSION.getValue());
        } else {
            datasetVersion.setDataConversion(ConversionStatusEnum.UNABLE_CONVERSION.getValue());
        }
        datasetVersion.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return getBaseMapper().updateById(datasetVersion);
    }

    /**
     * ????????????
     */
    @Override
    public void fileCopy() {
        DatasetVersionCriteriaVO needFileCopy = DatasetVersionCriteriaVO.builder()
                .deleted(NOT_DELETED).dataConversion(ConversionStatusEnum.NOT_COPY.getValue()).build();
        //?????????????????????????????????????????????
        List<DatasetVersion> versions = list(WrapperHelp.getWrapper(needFileCopy));
        if (CollectionUtil.isEmpty(versions)) {
            LogUtil.info(LogEnum.BIZ_DATASET, "No version data to copy");
            return;
        }
        versions.forEach(version -> {
            copyFlag.putIfAbsent(version.getId(), true);
            //????????????????????????????????????
            if (copyFlag.get(version.getId())) {
                try {
                    Dataset dataset = datasetService.getBaseMapper().selectById(version.getDatasetId());
                    //????????????????????????
                    pool.getExecutor().submit(() -> publishCopyFile(dataset, version));
                } catch (Exception e) {
                    LogUtil.error(LogEnum.BIZ_DATASET, "copy task is refused", e);
                }
            }
        });
    }

    /**
     * ??????????????????
     */
    @Override
    public void annotationFileCopy() {
        DatasetVersionCriteriaVO needFileCopy = DatasetVersionCriteriaVO.builder()
                .deleted(NOT_DELETED).dataConversion(ConversionStatusEnum.PUBLISHING.getValue()).build();
        List<DatasetVersion> versions = list(WrapperHelp.getWrapper(needFileCopy));
        versions.forEach(version -> {
            Dataset dataset = datasetService.getBaseMapper().selectById(version.getDatasetId());
            if(dataset.getAnnotateType().equals(AnnotateTypeEnum.TEXT_CLASSIFICATION.getValue())
                    ||dataset.getAnnotateType().equals(AnnotateTypeEnum.TEXT_SEGMENTATION.getValue())
                    ||dataset.getAnnotateType().equals(AnnotateTypeEnum.NAMED_ENTITY_RECOGNITION.getValue())){
                insertEsData("V0000", version.getVersionName(), dataset.getId(),dataset.getId(), null);
            }
            //???????????????????????????
            List<Label> datasetLabels = datasetLabelService.listLabelByDatasetId(dataset.getId());
            Map<Long, String> labelMaps = datasetLabels.stream().collect(Collectors.toMap(Label::getId, Label::getName));
            // ??????????????????????????????(?????????) - ????????????
            saveDatasetVersionFiles(version);
            // ??????????????????????????????
            datasetVersionFileService.newShipVersionNameChange(dataset.getId(),
                    version.getVersionSource(), version.getVersionName());
            String prefixPath = dataset.getUri() + "/";
            String annVersionTargetDir = prefixPath + VERSION_FILE + "/"
                    + version.getVersionName() + "/" + ANNOTATION;
            if (version.getVersionSource() == null) {
                String annotationSourceDir = prefixPath + ANNOTATION;
                List<String> annotationNames = new ArrayList<>();
                List<String> picUrls = fileService.selectUrls(dataset.getId(), version.getVersionName());
                picUrls.forEach(picUrl -> {
                    String fileName = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(picUrl, "/"), ".");
                    String annotationUrl = annotationSourceDir + "/" + fileName;
                    annotationNames.add(annotationUrl);
                });
                minioFileReplaceAndCopy(labelMaps, annotationNames, annVersionTargetDir, datasetLabels);
            } else {
                List<String> unChangedNames = fileService.selectNames(dataset.getId(), MagicNumConstant.ZERO, version.getVersionName());
                String unChangedAnnotationSourceDir = prefixPath + VERSION_FILE;
                List<String> unChangedAnnotationUrls = new ArrayList<>();
                unChangedNames.forEach(unChangedName -> {
                    String annotationUrl = unChangedAnnotationSourceDir + "/" + version.getVersionSource()
                            + "/" + ANNOTATION + "/" + unChangedName;
                    unChangedAnnotationUrls.add(annotationUrl);
                });
                minioFileReplaceAndCopy(labelMaps, unChangedAnnotationUrls, annVersionTargetDir, datasetLabels);
                List<String> changedNames = fileService.selectNames(dataset.getId(), MagicNumConstant.ONE, version.getVersionName());
                String changedAnnotationSourceDir = prefixPath + ANNOTATION;
                List<String> changedAnnotationUrls = new ArrayList<>();
                changedNames.forEach(changedName -> {
                    String annotationUrl = changedAnnotationSourceDir + "/" + changedName;
                    changedAnnotationUrls.add(annotationUrl);
                });
                minioFileReplaceAndCopy(labelMaps, changedAnnotationUrls, annVersionTargetDir, datasetLabels);
            }
            version.setDataConversion(ConversionStatusEnum.NOT_COPY.getValue());
            getBaseMapper().updateById(version);
            //????????????
            dataset.setCurrentVersionName(version.getVersionSource());
            dataFileAnnotationService.rollbackAnnotation(dataset.getId(), version.getVersionSource()
                    , MagicNumConstant.TWO, MagicNumConstant.ONE);
            dataFileAnnotationService.rollbackAnnotation(dataset.getId(), version.getVersionSource()
                    , MagicNumConstant.ONE, MagicNumConstant.ZERO);
            datasetVersionFileService.rollbackDataset(dataset);
        });
    }

    /**
     * ??????es??????
     *
     * @param versionSource         ?????????
     * @param versionTarget         ????????????
     * @param datasetId             ?????????id
     * @param fileNameMap           ????????????
     */
    @Override
    public void insertEsData(String versionSource, String versionTarget, Long datasetId, Long datasetIdTarget, Map<String, Long> fileNameMap){
        SearchRequest searchRequest = new SearchRequest(esIndex);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("datasetId",datasetId.toString()))
                .must(QueryBuilders.matchPhraseQuery("versionName", versionSource));
        QueryBuilder queryBuilder = boolQueryBuilder;
        sourceBuilder.query(queryBuilder).size(MagicNumConstant.MILLION * MagicNumConstant.TEN);
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (int i = 0; i < hits.length; i++) {
                EsDataFileDTO esDataFileDTO = JSON.parseObject(hits[i].getSourceAsString(), EsDataFileDTO.class);
                esDataFileDTO.setVersionName(versionTarget);
                Map<String, String> jsonMap = new HashMap<>();
                jsonMap.put("content", esDataFileDTO.getContent());
                jsonMap.put("name", esDataFileDTO.getName());
                jsonMap.put("status",esDataFileDTO.getStatus().toString());
                jsonMap.put("datasetId",datasetIdTarget.toString());
                jsonMap.put("createUserId",esDataFileDTO.getCreateUserId()==null?null:esDataFileDTO.getCreateUserId().toString());
                jsonMap.put("createTime",esDataFileDTO.getCreateTime()==null?null:esDataFileDTO.getCreateTime().toString());
                jsonMap.put("updateUserId",esDataFileDTO.getUpdateUserId()==null?null:esDataFileDTO.getUpdateUserId().toString());
                jsonMap.put("updateTime",esDataFileDTO.getUpdateTime()==null?null:esDataFileDTO.getUpdateTime().toString());
                jsonMap.put("fileType",esDataFileDTO.getFileType()==null?null:esDataFileDTO.getFileType().toString());
                jsonMap.put("enhanceType",esDataFileDTO.getEnhanceType()==null?null:esDataFileDTO.getEnhanceType().toString());
                jsonMap.put("originUserId",esDataFileDTO.getOriginUserId().toString());
                jsonMap.put("prediction",esDataFileDTO.getPrediction()==null?null:esDataFileDTO.getPrediction().toString());
                jsonMap.put("labelId",esDataFileDTO.getLabelId()==null?null:esDataFileDTO.getLabelId().toString());
                jsonMap.put("annotation", esDataFileDTO.getAnnotation()==null?null:esDataFileDTO.getAnnotation());
                jsonMap.put("versionName", versionTarget);
                IndexRequest request = new IndexRequest(esIndex);
                request.source(jsonMap);
                if(fileNameMap!=null){
                    request.id(fileNameMap.get(esDataFileDTO.getName()).toString());
                } else {
                    request.id(hits[i].getId());
                }
                bulkProcessor.add(request);
            }
            bulkProcessor.flush();
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "publish text to es error:{}", e);
        }
    }

    /**
     * ????????????????????????????????????(???????????????id?????????name)
     *
     * @param labelMaps   ????????????
     * @param sourceFiles ????????????????????????
     * @param targetDir   ???????????????????????????
     */
    public void minioFileReplaceAndCopy(Map<Long, String> labelMaps, List<String> sourceFiles, String targetDir, List<Label> datasetLabels) {
        /**
         * 1.???????????????
         * 2.????????????category_id???????????????
         * 3.???????????????????????????????????????
         */
        sourceFiles.stream().forEach(str->{
            try {
                String jsonStr = minioUtil.readString(bucketName, str);
                JSONArray jsonArray = JSON.parseArray(jsonStr);
                for(int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String categoryIdStr = jsonObject.getString("category_id");
                    if (NumberUtil.isNumber(categoryIdStr)) {
                        if (labelMaps.containsKey(Long.parseLong(categoryIdStr))) {
                            jsonObject.put("category_id", labelMaps.get(Long.parseLong(categoryIdStr)));
                        }
                    }
                }
                minioUtil.writeString(bucketName, targetDir + "/" + str.substring(str.lastIndexOf("/")+1, str.length()), JSON.toJSONString(jsonArray));
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "MinIO file write exception, {}", e);
            }
        });
        //???????????????????????????????????????
        List<String> labelStr = new ArrayList<>();
        for (Label label : datasetLabels){
            labelStr.add(label.getName());
        }
        try {
            minioUtil.writeString(bucketName, targetDir + "/labels.text", Strings.join(labelStr, ','));
            minioUtil.writeString(bucketName, targetDir + "/labelsIds.text", JSONObject.toJSONString(labelMaps));
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "MinIO file write exception, {}", e);
        }
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param datasetId ?????????id
     * @return Integer  ??????????????????
     */
    @Override
    @DataPermissionMethod
    public Integer getSourceFileCount(Long datasetId) {
        Dataset dataset = datasetService.getBaseMapper().selectById(datasetId);
        return datasetVersionFileService.getSourceFileCount(dataset);
    }

    /**
     * ???????????????????????????
     *
     * @param datasetVersionId ???????????????ID
     * @return ?????????????????????
     */
    @Override
    public DatasetVersion detail(Long datasetVersionId) {
        return baseMapper.selectById(datasetVersionId);
    }

    /**
     * ???????????????????????????
     *
     * @param id            ???????????????ID
     * @param sourceStatus  ?????????
     * @param targetStatus  ????????????
     */
    @Override
    public void update(Long id, Integer sourceStatus, Integer targetStatus) {
        UpdateWrapper<DatasetVersion> datasetVersionUpdateWrapper = new UpdateWrapper<>();
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setDataConversion(targetStatus);
        datasetVersionUpdateWrapper.eq("id", id);
        datasetVersionUpdateWrapper.eq("data_conversion", sourceStatus);
        baseMapper.update(datasetVersion, datasetVersionUpdateWrapper);
    }

    @Override
    public DatasetVersion getDatasetVersionSourceVersion(Dataset dataset) {
        return baseMapper.selectOne(new LambdaQueryWrapper<DatasetVersion>() {{
            eq(DatasetVersion::getDatasetId, dataset.getId());
            eq(DatasetVersion::getVersionName, dataset.getCurrentVersionName());
        }});
    }

    /**
     * ?????????????????????
     *
     * @param datasetId ?????????ID
     * @param versionName  ?????????
     * @return DatasetVersion ???????????????
     */
    @Override
    public DatasetVersion getVersionByDatasetIdAndVersionName(Long datasetId, String versionName) {
        QueryWrapper<DatasetVersion> datasetVersionQueryWrapper = new QueryWrapper<>();
        datasetVersionQueryWrapper.lambda().eq(DatasetVersion::getDatasetId, datasetId)
                .eq(DatasetVersion::getVersionName, versionName);
        return getBaseMapper().selectOne(datasetVersionQueryWrapper);
    }


    /**
     * ???????????????ID??????????????????
     *
     * @param datasetId ?????????ID
     */
    @Override
    public void deleteByDatasetId(Long datasetId) {
        baseMapper.deleteByDatasetId(datasetId);
    }


    /**
     * ???????????????????????????
     * @param originDataset      ??????????????????
     * @param targetDateset      ?????????????????????
     * @param currentVersionName ????????????
     */
    @Override
    public void backupDatasetVersionDataByDatasetId(Dataset originDataset, Dataset targetDateset, String currentVersionName) {
        DatasetVersion datasetVersion = getVersionByDatasetIdAndVersionName(originDataset.getId(), currentVersionName);
        if (!Objects.isNull(datasetVersion)) {
            if (!Objects.isNull(datasetVersion.getVersionUrl())) {
                String url = new StringBuffer(StringUtils.substringBeforeLast(datasetVersion.getVersionUrl(), SymbolConstant.SLASH)
                        .replace(originDataset.getId().toString(), targetDateset.getId().toString())).append(SymbolConstant.SLASH).append(DEFAULT_VERSION).toString();
                datasetVersion.setVersionUrl(url);
            }
            DatasetVersion version = DatasetVersion.builder()
                    .datasetId(targetDateset.getId())
                    .dataConversion(datasetVersion.getDataConversion())
                    .versionName(DEFAULT_VERSION)
                    .versionUrl(datasetVersion.getVersionUrl())
                    .teamId(datasetVersion.getTeamId())
                    .originUserId(MagicNumConstant.ZERO_LONG)
                    .versionNote(datasetVersion.getVersionNote())
                    .build();
            version.setCreateUserId(targetDateset.getCreateUserId());
            version.setUpdateUserId(version.getCreateUserId());
            baseMapper.insert(version);
        }
    }


    /**
     * ???????????????ID????????????????????????
     * @param datasetId ?????????ID
     * @return ??????????????????
     */
    @Override
    public List<String> getDatasetVersionNameListByDatasetId(Long datasetId) {
        return baseMapper.getDatasetVersionNameListByDatasetId(datasetId);
    }

    /**
     * ??????ofRecord??????
     *
     * @param datasetId          ?????????ID
     * @param versionName        ????????????
     */
    @Override
    public void createOfRecord(Long datasetId, String versionName){
        List<DatasetVersion> datasetVersionList = baseMapper.findDatasetVersion(datasetId, versionName);
        DatasetVersion datasetVersion = datasetVersionList.get(0);
        if(datasetVersion !=null){
            Task task = Task.builder().total(getBaseMapper().getCountByDatasetVersionId(datasetVersion.getDatasetId()
                    ,datasetVersion.getVersionName()))
                    .datasetId(datasetVersion.getDatasetId())
                    .type(DataTaskTypeEnum.OFRECORD.getValue())
                    .labels("")
                    .datasetVersionId(datasetVersion.getId()).build();
            taskService.createTask(task);
            datasetVersion.setOfRecord(MagicNumConstant.ONE);
            baseMapper.updateById(datasetVersion);
        }
    }

    /**
     * ??????????????????
     *
     * @param datasetVersion  ????????????
     */
    @Override
    public void insertOne(DatasetVersion datasetVersion) {
        baseMapper.insert(datasetVersion);
    }

}
