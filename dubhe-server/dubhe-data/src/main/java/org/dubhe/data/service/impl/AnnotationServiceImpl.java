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
import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.ResponseCode;
import org.dubhe.biz.base.enums.OperationTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.data.constant.*;
import org.dubhe.data.domain.bo.FileBO;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.*;
import org.dubhe.data.machine.constant.DataStateMachineConstant;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.constant.FileStateMachineConstant;
import org.dubhe.data.machine.enums.FileStateEnum;
import org.dubhe.data.machine.utils.StateMachineUtil;
import org.dubhe.data.service.*;
import org.dubhe.data.service.store.IStoreService;
import org.dubhe.data.service.store.MinioStoreServiceImpl;
import org.dubhe.data.util.GeneratorKeyUtil;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * @description ??????service
 * @date 2020-03-27
 */
@Service
public class AnnotationServiceImpl implements AnnotationService {

    /**
     * esSearch??????
     */
    @Value("${es.index}")
    private String esIndex;

    /**
     * ??????????????????
     */
    @Autowired
    private FileService fileService;

    /**
     * ???????????????
     */
    @Autowired
    private TaskService taskService;

    /**
     * ??????????????????
     */
    @Autowired
    private DatasetService datasetService;

    /**
     * ?????????????????????
     */
    @Resource(type = MinioStoreServiceImpl.class)
    private IStoreService storeService;

    /**
     * ?????????????????????
     */
    @Autowired
    private DatasetVersionFileService datasetVersionFileService;

    @Autowired
    private GeneratorKeyUtil generatorKeyUtil;

    /**
     * ??????????????????
     */
    static PriorityBlockingQueue<TaskSplitBO> queue;

    /**
     * ??????????????????
     */
    private ConcurrentHashMap<String, TaskSplitBO> autoAnnotating;

    /**
     * ???????????????
     */
    @Autowired
    private DatasetVersionService datasetVersionService;

    /**
     * ????????????????????????
     */
    @Autowired
    private DataFileAnnotationService dataFileAnnotationService;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * ??????????????????
     */
    private ConcurrentHashSet<Long> tracking;

    /**
     * ???????????????
     */
    @Autowired
    private org.dubhe.data.util.FileUtil fileUtil;

    /**
     * ????????????
     */
    public static final int QUEUE_SIZE = MagicNumConstant.FIFTY;

    /**
     * ????????????
     */
    public static final int TRACKING_SIZE = MagicNumConstant.FIVE;

    /**
     * ?????????
     */
    @PostConstruct
    public void init() {
        queue = new PriorityBlockingQueue<>(QUEUE_SIZE, Comparator.comparingInt(TaskSplitBO::getPriority).reversed());
        autoAnnotating = new ConcurrentHashMap<>(MagicNumConstant.SIXTEEN);
        tracking = new ConcurrentHashSet<>(MagicNumConstant.SIXTEEN);
    }

    /**
     * ????????????(????????????)
     *
     * @param batchAnnotationInfoCreateDTO ????????????
     * @return int ?????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataPermissionMethod
    public void save(Long datasetId, BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO) {
        for (AnnotationInfoCreateDTO annotationInfoCreateDTO : batchAnnotationInfoCreateDTO.getAnnotations()) {
            save(datasetId, annotationInfoCreateDTO);
        }
    }

    /**
     * ??????????????????
     *
     * @param annotationInfoCreateDTO ????????????
     * @return int ?????????????????????
     */
    @Override
    public void save(Long datasetId, AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        Dataset dataset = datasetService.getOneById(datasetId);
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        //?????????????????????????????????
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (datasetVersionService.getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        annotationInfoCreateDTO.setDatasetId(datasetId);
        annotationInfoCreateDTO.setCurrentVersionName(dataset.getCurrentVersionName());
        annotationInfoCreateDTO.setDataType(dataset.getDataType());
        doSave(annotationInfoCreateDTO);
        saveDatasetFileAnnotationsByImage(annotationInfoCreateDTO);
        //????????????????????????????????????
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{new DatasetVersionFile() {{
                setDatasetId(dataset.getId());
                setFileId(annotationInfoCreateDTO.getId());
                setVersionName(dataset.getCurrentVersionName());
            }}});
            setEventMethodName(FileStateMachineConstant.FILE_SAVE_COMPLETE_EVENT);
            setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
        }});
        //???????????????????????????????????????
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{dataset});
            setEventMethodName(DataStateMachineConstant.DATA_FINISH_MANUAL_EVENT);
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        }});
    }

    /**
     * ????????????(??????)
     *
     * @param annotationInfoCreateDTO ????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataPermissionMethod
    public void save(Long fileId, Long datasetId, AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        Dataset dataset = datasetService.getOneById(datasetId);
        if (dataset == null) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        //?????????????????????????????????
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (datasetVersionService.getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        annotationInfoCreateDTO.setId(fileId);
        annotationInfoCreateDTO.setDatasetId(datasetId);
        annotationInfoCreateDTO.setCurrentVersionName(dataset.getCurrentVersionName());
        annotationInfoCreateDTO.setDataType(dataset.getDataType());
        doSave(annotationInfoCreateDTO);
        saveDatasetFileAnnotationsByImage(annotationInfoCreateDTO);
        //????????????????????????????????????
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{dataset});
            setEventMethodName(DataStateMachineConstant.DATA_MANUAL_ANNOTATION_SAVE_EVENT);
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        }});
        //?????????????????????????????????
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{new DatasetVersionFile() {{
                setDatasetId(dataset.getId());
                setFileId(fileId);
                setVersionName(dataset.getCurrentVersionName());
            }}});
            setEventMethodName(FileStateMachineConstant.FILE_MANUAL_ANNOTATION_SAVE_EVENT);
            setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
        }});
    }

    /**
     * ??????????????????
     *
     * @param annotationInfoCreateDTO ????????????
     */
    private void doSave(AnnotationInfoCreateDTO annotationInfoCreateDTO) {

        if (annotationInfoCreateDTO == null || annotationInfoCreateDTO.getAnnotation() == null
                || annotationInfoCreateDTO.getId() == null) {
            LogUtil.warn(LogEnum.BIZ_DATASET, "annotation info invalid. annotation:{}", annotationInfoCreateDTO);
            return;
        }
        QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper
                .eq("id", annotationInfoCreateDTO.getId()).eq("dataset_id", annotationInfoCreateDTO.getDatasetId());
        File fileOne = fileService.selectOne(fileQueryWrapper);
        if (fileOne == null) {
            LogUtil.warn(LogEnum.BIZ_DATASET, ErrorEnum.FILE_ABSENT.getMsg() + "fileId is" + annotationInfoCreateDTO.getId());
            throw new BusinessException(ErrorEnum.FILE_ABSENT);
        }
        datasetService.autoAnnotatingCheck(fileOne);
        String filePath = fileUtil.getWriteAnnotationAbsPath(fileOne.getDatasetId(), fileOne.getName());
        String annotation = annotationInfoCreateDTO.getAnnotation();
        storeService.write(filePath, annotation);


    }

    /**
     * ?????????????????????????????????
     *
     * @param annotationInfoCreateDTO   ??????????????????
     */
    private void saveDatasetFileAnnotations(AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        List<AnnotationDTO> annotationDTOS = JSONObject.parseArray(annotationInfoCreateDTO.getAnnotation(), AnnotationDTO.class);
        Long datasetId = annotationInfoCreateDTO.getDatasetId();
        DatasetVersionFile datasetVersionFile = datasetVersionFileService.getDatasetVersionFile(
                datasetId, annotationInfoCreateDTO.getCurrentVersionName(), annotationInfoCreateDTO.getId());
        if (Objects.isNull(datasetVersionFile)) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_FILE_IS_ERROR);
        }
        if (!CollectionUtil.isEmpty(annotationDTOS)) {
            Long versionFileId = datasetVersionFile.getId();
            List<Long> fileLabelIds = annotationDTOS.stream().map(a -> a.getCategoryId()).collect(Collectors.toList());
            List<Long> dbLabelIds = dataFileAnnotationService.findInfoByVersionId(datasetId,versionFileId);
            if (!CollectionUtil.isEmpty(dbLabelIds)) {
                dataFileAnnotationService.deleteAnnotationFileByVersionIdAndLabelIds(datasetId,versionFileId, dbLabelIds);
            }
            dataFileAnnotationService.insertAnnotationFileByVersionIdAndLabelIds(datasetId, versionFileId, fileLabelIds, datasetVersionFile.getFileName());
            //????????????????????????????????????
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{new DatasetVersionFile() {{
                    setDatasetId(annotationInfoCreateDTO.getDatasetId());
                    setFileId(annotationInfoCreateDTO.getId());
                    setVersionName(annotationInfoCreateDTO.getCurrentVersionName());
                }}});
                setEventMethodName(FileStateMachineConstant.FILE_SAVE_COMPLETE_EVENT);
                setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
            }});
        } else {
            datasetVersionFileService.updateStatusById(
                    DatasetVersionFile.builder().id(datasetVersionFile.getId())
                            .datasetId(datasetVersionFile.getDatasetId())
                            .annotationStatus(FileStateEnum.NOT_ANNOTATION_FILE_STATE.getCode()).build());
            dataFileAnnotationService.deleteBatch(datasetId,Arrays.asList(datasetVersionFile.getId()));
        }
    }

    /**
     * ????????????
     *
     * @param annotationInfoCreateDTO ????????????
     * @param fileId                  ??????id
     * @return int ?????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataPermissionMethod
    public void finishManual(Long fileId, Long datasetId, AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        annotationInfoCreateDTO.setDatasetId(datasetId);
        Dataset dataset = datasetService.getOneById(datasetId);
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        //?????????????????????????????????
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (datasetVersionService.getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        annotationInfoCreateDTO.setId(fileId);
        annotationInfoCreateDTO.setDataType(dataset.getDataType());
        annotationInfoCreateDTO.setCurrentVersionName(dataset.getCurrentVersionName());
        doSave(annotationInfoCreateDTO);
        //??????????????????Json?????????????????????DB
        if (dataset.getAnnotateType().equals(AnnotateTypeEnum.TEXT_CLASSIFICATION.getValue())
                || DatatypeEnum.IMAGE.getValue().equals(annotationInfoCreateDTO.getDataType())
                || DatatypeEnum.VIDEO.getValue().equals(annotationInfoCreateDTO.getDataType())
                || dataset.getAnnotateType().equals(AnnotateTypeEnum.AUDIO_CLASSIFY.getValue())) {
            saveDatasetFileAnnotations(annotationInfoCreateDTO);
        }
        if (DatatypeEnum.IMAGE.getValue().equals(annotationInfoCreateDTO.getDataType())){
            //????????????????????????????????????
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{new DatasetVersionFile() {{
                    setDatasetId(dataset.getId());
                    setFileId(fileId);
                    setVersionName(dataset.getCurrentVersionName());
                }}});
                setEventMethodName(FileStateMachineConstant.FILE_SAVE_COMPLETE_EVENT);
                setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
            }});
        }
        if(dataset.getAnnotateType().equals(AnnotateTypeEnum.TEXT_SEGMENTATION.getValue())||
                dataset.getAnnotateType().equals(AnnotateTypeEnum.NAMED_ENTITY_RECOGNITION.getValue()) ||
                dataset.getAnnotateType().equals(AnnotateTypeEnum.SPEECH_RECOGNITION.getValue())){
            List<AnnotationDTO> annotationDTOS = JSONObject.parseArray(annotationInfoCreateDTO.getAnnotation(), AnnotationDTO.class);
            if(!CollectionUtil.isEmpty(annotationDTOS)){
                //????????????????????????????????????
                StateMachineUtil.stateChange(new StateChangeDTO() {{
                    setObjectParam(new Object[]{new DatasetVersionFile() {{
                        setDatasetId(annotationInfoCreateDTO.getDatasetId());
                        setFileId(annotationInfoCreateDTO.getId());
                        setVersionName(annotationInfoCreateDTO.getCurrentVersionName());
                    }}});
                    setEventMethodName(FileStateMachineConstant.FILE_SAVE_COMPLETE_EVENT);
                    setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
                }});
            } else {
                DatasetVersionFile datasetVersionFile = datasetVersionFileService.getDatasetVersionFile(
                        datasetId, annotationInfoCreateDTO.getCurrentVersionName(), annotationInfoCreateDTO.getId());
                datasetVersionFileService.updateStatusById(
                        DatasetVersionFile.builder().id(datasetVersionFile.getId())
                                .datasetId(datasetVersionFile.getDatasetId())
                                .annotationStatus(FileStateEnum.NOT_ANNOTATION_FILE_STATE.getCode()).build());
                dataFileAnnotationService.deleteBatch(datasetId,Arrays.asList(datasetVersionFile.getId()));
            }
        }
        //???????????????????????????????????????
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{dataset});
            setEventMethodName(DataStateMachineConstant.DATA_FINISH_MANUAL_EVENT);
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        }});
        fileService.recoverEsStatus(datasetId, fileId);
        if(dataset.getDataType().equals(MagicNumConstant.TWO)||dataset.getDataType().equals(MagicNumConstant.THREE) ||
        dataset.getAnnotateType().equals(AnnotateTypeEnum.TEXT_SEGMENTATION.getValue())||
        dataset.getAnnotateType().equals(AnnotateTypeEnum.NAMED_ENTITY_RECOGNITION.getValue())){
            UpdateRequest updateRequest = new UpdateRequest(esIndex,"_doc",fileId.toString());
            JSONObject esJsonObject = new JSONObject();
            if(annotationInfoCreateDTO.getAnnotation() == null){
                esJsonObject.put("labelId", null);
                esJsonObject.put("prediction", null);
                esJsonObject.put("annotation", null);
                esJsonObject.put("status", String.valueOf(FileTypeEnum.UNFINISHED.getValue()));
            } else {
                JSONObject jsonObject = JSONArray.parseArray(annotationInfoCreateDTO.getAnnotation()).getJSONObject(0);
                esJsonObject.put("labelId",jsonObject.getString("category_id"));
                esJsonObject.put("prediction",jsonObject.getString("score"));
                esJsonObject.put("status", String.valueOf(FileTypeEnum.FINISHED.getValue()));
                esJsonObject.put("annotation",annotationInfoCreateDTO.getAnnotation());
            }
            updateRequest.doc(esJsonObject,XContentType.JSON);
            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            try {
                restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "update es data error:{}", e);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param annotationDeleteDTO ??????????????????
     * @return boolean ????????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataPermissionMethod
    public void reAuto(AnnotationDeleteDTO annotationDeleteDTO) {
        Dataset dataset = datasetService.getOneById(annotationDeleteDTO.getDatasetId());
        if (!Objects.isNull(dataset)) {
            verificationAnnotationCondition(dataset.getAnnotateType());
        }
        //?????????????????????????????????
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (datasetVersionService.getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        //????????????????????????
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
            setEventMethodName(DataStateMachineConstant.DATA_DELETE_ANNOTATING_EVENT);
            setObjectParam(new Object[]{annotationDeleteDTO.getDatasetId().intValue()});
        }});

        //?????????????????????ID??????Changed???????????????
        datasetVersionFileService.updateChanged(annotationDeleteDTO.getDatasetId(), dataset.getCurrentVersionName());

        List<Long> taskIds = taskService.auto(new AutoAnnotationCreateDTO() {{
            setDatasetIds(new Long[]{annotationDeleteDTO.getDatasetId()});
            setType(DataTaskTypeEnum.AGAIN_ANNOTATION.getValue());
        }});
        //??????task?????????????????????????????????
        if (CollectionUtil.isNotEmpty(taskIds)) {
            taskIds.stream().forEach(aLong -> {
                Task task = taskService.detail(aLong);
                task.setType(DataTaskTypeEnum.AGAIN_ANNOTATION.getValue());
                taskService.updateByTaskId(task);
            });
        }
    }

    /**
     * ??????????????????
     *
     * @param files ??????set
     */
    public void delete(Set<File> files) {
        files.forEach(this::delete);
    }

    /**
     * ??????????????????
     *
     * @param file ??????
     */
    public void delete(File file) {
        if (file == null) {
            return;
        }
        String filePath = fileUtil.getWriteAnnotationAbsPath(file.getDatasetId(), file.getName());
        storeService.delete(filePath);
        LogUtil.info(LogEnum.BIZ_DATASET, "delete file. file:{}", filePath);
    }

    /**
     * ????????????map
     *
     * @return Map<String, TaskSplitBO> ??????????????????????????????(????????????????????????)
     */
    @Override
    public Map<String, TaskSplitBO> getTaskPool() {
        return autoAnnotating;
    }

    /**
     * ??????????????????
     *
     * @param taskId                       ?????????id
     * @param batchAnnotationInfoCreateDTO ????????????
     * @return boolean ??????????????????return true ??????????????????
     */
    @Override
    public boolean finishAuto(String taskId, BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO) {
        LogUtil.info(LogEnum.BIZ_DATASET, "finishAuto log is:" + taskId, batchAnnotationInfoCreateDTO);
        TaskSplitBO taskSplitBO = autoAnnotating.get(taskId);
        if (taskSplitBO == null) {
            throw new BusinessException(ErrorEnum.TASK_SPLIT_ABSENT);
        }
        doFinishAuto(taskSplitBO, batchAnnotationInfoCreateDTO.toMap());
        return true;
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @return
     */
    public void autoTrack() {
        if (tracking.size() >= TRACKING_SIZE) {
            return;
        }
        //???????????????????????????
        List<Dataset> datasets = queryDatasetsToBeTracked();
        if (CollectionUtil.isEmpty(datasets)) {
            LogUtil.info(LogEnum.BIZ_DATASET, "there is currently no data to track");
            return;
        }
        datasets.stream().forEach(dataset -> {
            if (CollectionUtil.isEmpty(taskService.getExecutingTask(dataset.getId(), DataTaskTypeEnum.TARGET_TRACK.getValue()))) {
                taskService.track(dataset);
            }
        });
    }

    /**
     * ????????????????????????????????????
     *
     * @return List<Dataset> ??????????????????????????????
     */
    public List<Dataset> queryDatasetsToBeTracked() {
        //??????????????????
        QueryWrapper<Dataset> datasetQueryWrapper = new QueryWrapper<>();
        datasetQueryWrapper.lambda()
                .eq(Dataset::getDataType, DatatypeEnum.VIDEO.getValue())
                .in(Dataset::getStatus, Constant.AUTO_TRACK_NEED_STATUS);
        return datasetService.queryList(datasetQueryWrapper);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param dataset ?????????
     * @return Map<Long, List < DatasetVersionFile>> ?????????????????????????????????????????????
     */
    @Override
    public Map<Long, List<DatasetVersionFile>> queryFileAccordingToCurrentVersionAndStatus(Dataset dataset) {
        Map<Long, List<DatasetVersionFile>> fileMap = new HashMap<>(MagicNumConstant.SIXTEEN);
        //????????????????????????????????????????????????
        List<DatasetVersionFile> fileList = filterFilesThatNeedToBeTracked(dataset.getId(), dataset.getCurrentVersionName());
        if (fileList != null) {
            fileMap.put(dataset.getId(), fileList);
        }
        return fileMap;
    }

    /**
     * ???????????????????????????
     *
     * @param datasetId   ?????????id
     * @param versionName ????????????
     * @return List<DatasetVersionFile> ??????????????????
     */
    public List<DatasetVersionFile> filterFilesThatNeedToBeTracked(Long datasetId, String versionName) {
        List<DatasetVersionFile> versionFiles = datasetVersionFileService.getFilesByDatasetIdAndVersionName(datasetId, versionName);
        long size = versionFiles.stream().filter(f ->
                !FileStateCodeConstant.AUTO_TAG_COMPLETE_FILE_STATE.equals(f.getStatus()) || FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE.equals(f.getStatus())).count();
        return size == versionFiles.size() ? versionFiles : null;
    }

    /**
     * ??????????????????
     *
     * @param taskSplit ????????????
     * @param resMap    ????????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void doFinishAuto(TaskSplitBO taskSplit, Map<Long, AnnotationInfoCreateDTO> resMap) {
        LogUtil.info(LogEnum.BIZ_DATASET, "finish auto. ts:{}, resMap:{}", taskSplit, resMap);
        //???????????????????????????????????????
        Dataset dataset = datasetService.getOneById(taskSplit.getDatasetId());
        //??????????????????
        if (AnnotateTypeEnum.TEXT_CLASSIFICATION.getValue().equals(taskSplit.getAnnotateType())) {
            taskSplit.getFiles().forEach(fileBO -> {
                AnnotationInfoCreateDTO annotationInfo = resMap.get(fileBO.getId());
                if (annotationInfo == null) {
                    return;
                }
                storeService.write(fileUtil.getAnnotationAbsPath(taskSplit.getDatasetId(), fileBO.getName()), annotationInfo.getAnnotation());
            });
        }
        taskSplit.setVersionName(dataset.getCurrentVersionName());
        List<DatasetVersionFile> versionFileIds = datasetVersionFileService.getVersionFileByDatasetAndFile(dataset.getId(), dataset.getCurrentVersionName(), resMap.keySet());
        //??????????????????
        if (!CollectionUtils.isEmpty(resMap)) {
            List<DataFileAnnotation> dataFileAnnotations = new ArrayList<>();
            versionFileIds.forEach(versionFile -> {
                List<Long> dbLabelIds = dataFileAnnotationService.findInfoByVersionId(dataset.getId(),versionFile.getId());
                if (!CollectionUtil.isEmpty(dbLabelIds)) {
                    dataFileAnnotationService.deleteAnnotationFileByVersionIdAndLabelIds(dataset.getId(),versionFile.getId(), dbLabelIds);
                }
                List<AnnotationDTO> annotationDTOS = JSONObject.parseArray(resMap.get(versionFile.getFileId()).getAnnotation(), AnnotationDTO.class);
                if(!CollectionUtils.isEmpty(annotationDTOS)){
                    if(AnnotateTypeEnum.CLASSIFICATION.getValue().equals(dataset.getAnnotateType()) || AnnotateTypeEnum.TEXT_CLASSIFICATION.getValue().equals(dataset.getAnnotateType())){
                        AnnotationDTO annotationDTO = annotationDTOS.stream().max(Comparator.comparingDouble(AnnotationDTO::getScore)).get();
                        dataFileAnnotations.add(new DataFileAnnotation(dataset.getId(), annotationDTO.getCategoryId(), versionFile.getId(), annotationDTOS.get(0).getScore(), versionFile.getFileName()));
                    }
                    if(AnnotateTypeEnum.OBJECT_DETECTION.getValue().equals(dataset.getAnnotateType()) || AnnotateTypeEnum.OBJECT_TRACK.getValue().equals(dataset.getAnnotateType())
                            || AnnotateTypeEnum.SEMANTIC_CUP.getValue().equals(dataset.getAnnotateType())){
                        annotationDTOS.forEach(annotationDTO -> {
                            dataFileAnnotations.add(new DataFileAnnotation(dataset.getId(), annotationDTO.getCategoryId(), versionFile.getId(), annotationDTO.getScore(), versionFile.getFileName()));
                        });
                    }
                }
            });
            if(!CollectionUtils.isEmpty(dataFileAnnotations)){
                Queue<Long> dataFileAnnotionIds = generatorKeyUtil.getSequenceByBusinessCode(Constant.DATA_FILE_ANNOTATION, dataFileAnnotations.size());
                for (DataFileAnnotation dataFileAnnotation : dataFileAnnotations) {
                    dataFileAnnotation.setId(dataFileAnnotionIds.poll());
                    dataFileAnnotation.setStatus(MagicNumConstant.ZERO);
                    dataFileAnnotation.setInvariable(MagicNumConstant.ZERO);
                }
                dataFileAnnotationService.insertDataFileBatch(dataFileAnnotations);
            }
        }

        HashSet<Long> annotationInfoIsNotEmpty = new HashSet<Long>() {{
            addAll(resMap.keySet().stream().filter(k -> !JSON.parseArray(resMap.get(k).getAnnotation()).isEmpty()).collect(Collectors.toSet()));
        }};
        //?????????????????????????????????????????????????????????????????????->??????????????????????????????
        if (!annotationInfoIsNotEmpty.isEmpty()) {
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{annotationInfoIsNotEmpty, taskSplit.getDatasetId(), taskSplit.getVersionName()});
                setEventMethodName(FileStateMachineConstant.FILE_DO_FINISH_AUTO_ANNOTATION_BATCH_EVENT);
                setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
            }});
        }
        HashSet<Long> annotationInfoIsEmpty = new HashSet<Long>() {{
            addAll(resMap.keySet().stream().filter(k -> JSON.parseArray(resMap.get(k).getAnnotation()).isEmpty()).collect(Collectors.toSet()));
        }};
        //?????????????????????????????????????????????????????????????????????->??????????????????????????????
        if (!annotationInfoIsEmpty.isEmpty()) {
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{annotationInfoIsEmpty, taskSplit.getDatasetId(), taskSplit.getVersionName()});
                setEventMethodName(FileStateMachineConstant.FILE_DO_FINISH_AUTO_ANNOTATION_INFO_IS_EMPTY_BATCH_EVENT);
                setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
            }});
        }
        if (taskSplit.getAnnotateType().equals(MagicNumConstant.SEVEN)) {
            List<FileBO> fileBOS = taskSplit.getFiles();
            fileBOS.forEach(fileBO -> fileService.recoverEsStatus(taskSplit.getDatasetId(),fileBO.getId()));
        }
        //?????????????????????
        taskService.finishFile(taskSplit.getTaskId(), taskSplit.getFiles().size(), dataset);
    }


    /**
     * ??????????????????
     *
     * @param datasetId          ?????????id
     * @param autoTrackCreateDTO ??????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void finishAutoTrack(Long datasetId, AutoTrackCreateDTO autoTrackCreateDTO) {
        if (!ResponseCode.SUCCESS.equals(autoTrackCreateDTO.getCode())) {
            LogUtil.info(LogEnum.BIZ_DATASET, "auto track is error" + autoTrackCreateDTO.getMsg());
            return;
        }
        LogUtil.info(LogEnum.BIZ_DATASET, "target tracking success modify status");
        Dataset dataset = datasetService.getOneById(datasetId);
        if (dataset == null) {
            LogUtil.error(LogEnum.BIZ_DATASET, "datasetId can't null");
        } else if (!DatatypeEnum.VIDEO.getValue().equals(dataset.getDataType())) {
            LogUtil.error(LogEnum.BIZ_DATASET, "wrong dataset type, not video. dataset:{}", datasetId);
        } else {
            //????????????????????????????????????>?????????????????????
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{dataset});
                setEventMethodName(DataStateMachineConstant.DATA_TARGET_COMPLETE_EVENT);
                setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
            }});
            //????????????????????????????????????->?????????????????????
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{dataset});
                setEventMethodName(FileStateMachineConstant.FILE_DO_FINISH_AUTO_TRACK_EVENT);
                setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
            }});
            tracking.remove(datasetId);
            LogUtil.info(LogEnum.BIZ_DATASET, "target tracking is complete dataset:{}", datasetId);
        }
        LogUtil.info(LogEnum.BIZ_DATASET, "exception update of target tracking algorithm callback. dataset:{}", datasetId);
    }

    /**
     * ??????????????????
     *
     * @param datasetId
     */
    @Override
    public void track(Long datasetId) {
        Dataset dataset = datasetService.getOneById(datasetId);
        if (dataset == null || !DatatypeEnum.VIDEO.getValue().equals(dataset.getDataType())) {
            throw new BusinessException(ErrorEnum.DATASET_TRACK_TYPE_ERROR);
        }
        //?????????????????????????????????
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (datasetVersionService.getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        taskService.track(dataset);
    }

    /**
     * ????????????????????????????????????
     *
     * @param datasetId  ?????????ID
     */
    @Override
    public void deleteAnnotating(Long datasetId) {
        datasetVersionFileService.deleteAnnotating(datasetId);
    }


    /**
     * ??????????????????????????????????????????
     *
     * @param annotationType ??????????????????
     */
    private void verificationAnnotationCondition(Integer annotationType) {
        if (AnnotateTypeEnum.SEMANTIC_CUP.getValue().compareTo(annotationType) == 0) {
            throw new BusinessException(AnnotateTypeEnum.SEMANTIC_CUP.getMsg() + ErrorEnum.DATASET_NOT_ANNOTATION);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param annotationInfoCreateDTO ??????????????????
     */
    private void saveDatasetFileAnnotationsByImage(AnnotationInfoCreateDTO annotationInfoCreateDTO) {

        List<AnnotationDTO> annotationDTOS = JSONObject.parseArray(annotationInfoCreateDTO.getAnnotation(), AnnotationDTO.class);
        if(CollectionUtil.isEmpty(annotationDTOS)){
            return;
        }
        Long datasetId = annotationInfoCreateDTO.getDatasetId();
        DatasetVersionFile datasetVersionFile = datasetVersionFileService.getDatasetVersionFile(
                datasetId, annotationInfoCreateDTO.getCurrentVersionName(), annotationInfoCreateDTO.getId());
        if (Objects.isNull(datasetVersionFile)) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_FILE_IS_ERROR);
        }
        Long versionFileId = datasetVersionFile.getId();
        List<Long> fileLabelIds = annotationDTOS.stream().map(a -> a.getCategoryId()).collect(Collectors.toList());
        List<Long> dbLabelIds = dataFileAnnotationService.findInfoByVersionId(datasetId,versionFileId);
        if (!CollectionUtil.isEmpty(dbLabelIds)) {
            dataFileAnnotationService.deleteAnnotationFileByVersionIdAndLabelIds(datasetId,versionFileId, dbLabelIds);
        }
        dataFileAnnotationService.insertAnnotationFileByVersionIdAndLabelIds(datasetId, versionFileId, fileLabelIds, datasetVersionFile.getFileName());
    }

}