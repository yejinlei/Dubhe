/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */
package org.dubhe.datasetutil.handle;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.config.ImageConfig;
import org.dubhe.datasetutil.common.config.MinioConfig;
import org.dubhe.datasetutil.common.constant.AnnotateTypeEnum;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.common.constant.FileStateCodeConstant;
import org.dubhe.datasetutil.common.enums.DatatypeEnum;
import org.dubhe.datasetutil.common.enums.LogEnum;
import org.dubhe.datasetutil.common.exception.ImportDatasetException;
import org.dubhe.datasetutil.common.util.*;
import org.dubhe.datasetutil.domain.dto.AnnotationDTO;
import org.dubhe.datasetutil.domain.entity.*;
import org.dubhe.datasetutil.domain.entity.DataVersionFile;
import org.dubhe.datasetutil.service.*;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @description ????????????????????????
 * @date 2020-10-12
 */
@Slf4j
@Component
public class DatasetImportHandle {

    /**
     * esSearch??????
     */
    @Value("${es.index}")
    private String esIndex;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DataLabelGroupService dataLabelGroupService;

    @Autowired
    private DataGroupLabelService dataGroupLabelService;

    @Autowired
    private DataLabelService dataLabelService;

    @Autowired
    private DataFileService dataFileService;

    @Autowired
    private DatasetDataLabelService datasetDataLabelService;

    @Autowired
    private DataVersionFileService dataVersionFileService;

    @Autowired
    private DataFileAnnotationService dataFileAnnotationService;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private GeneratorKeyUtil generatorKeyUtil;

    @Autowired
    private ImageConfig imageConfig;

    @Resource
    private BulkProcessor bulkProcessor;

    /**
     * ???????????????JSON???key
     */
    static List<String> annotationFileContextKey = new ArrayList<>();

    /**
     * ????????????????????????
     */
    static {
        annotationFileContextKey.add("score");
        annotationFileContextKey.add("area");
        annotationFileContextKey.add("name");
        annotationFileContextKey.add("bbox");
        annotationFileContextKey.add("segmentation");
        annotationFileContextKey.add("iscrowd");
    }

    /**
     * ???????????????
     *
     * @param scanner ??????
     */
    public void importDataset(Scanner scanner) throws Exception {
        Dataset dataset = verificationDatasetId(scanner);
        String filePath = verificationFilePath(scanner);
        File labelJsonFile = verificationFile(filePath, dataset);
        DataLabelGroup dataLabelGroup = saveDataLabelGroup(HandleFileUtil.getLabelGroupName(labelJsonFile.getName()), dataset);
        List<DataLabel> dataLabelList = readLabelContext(labelJsonFile);
        saveDataLabel(dataset, dataLabelList, dataLabelGroup.getId());
        log.info("........??????????????????,???????????????????????????,??????????????????.................");
        executeUploadAndSave(dataLabelList, filePath, dataset);
        dataset.setLabelGroupId(dataLabelGroup.getId());
        datasetService.updateDatasetStatus(dataset);
        log.warn("");
        PrintUtils.printLine("  Success: ????????????  ", PrintUtils.GREEN);
        log.warn("");
        System.out.println("#   ????????????? Y / N  #");
        Scanner scannerExit = new Scanner(System.in);
        if (BusinessConstant.Y.toLowerCase().equals(scannerExit.nextLine().toLowerCase())) {
            System.exit(MagicNumConstant.ZERO);
        }
    }

    /**
     * ?????????????????? ?????????
     *
     * @param globalFilePath ????????????
     * @param dataset        ?????????
     * @return file ????????????
     */
    public File verificationFile(String globalFilePath, Dataset dataset) throws IOException {
        File labelRootFiles = new File(globalFilePath);
        File imageRootFiles = new File(globalFilePath + HandleFileUtil.generateFilePath(BusinessConstant.IMAGE_ORIGIN));
        File annotationRootFiles = new File(globalFilePath + HandleFileUtil.generateFilePath(BusinessConstant.ANNOTATION));
        if (imageRootFiles.list() == null || annotationRootFiles.listFiles() == null) {
            throw new ImportDatasetException("???" + globalFilePath + "???????????????????????????(origin)????????????????????????(annotation)?????????????????? ");
        }
        File labelJsonFile = null;
        for (File file : Objects.requireNonNull(labelRootFiles.listFiles())) {
            if (file.isFile() && file.getName().toLowerCase().startsWith(BusinessConstant.LABEL.toLowerCase())
                    && file.getName().contains(BusinessConstant.UNDERLINE)
                    && file.getName().toLowerCase().endsWith(BusinessConstant.SUFFIX_JSON.toLowerCase())) {
                labelJsonFile = file;
            }
        }
        if (labelJsonFile == null) {
            throw new ImportDatasetException("???" + globalFilePath + "????????????????????????????????????");
        }
        dealLabelGroup(labelJsonFile.getName());
        List<DataLabel> dataLabelList = readLabelContext(labelJsonFile);
        Map<String, List<DataLabel>> dataLabelMap = dataLabelList.stream().collect(Collectors.groupingBy(DataLabel::getName));
        for (Map.Entry<String, List<DataLabel>> entry : dataLabelMap.entrySet()) {
            if (entry.getValue().size() > MagicNumConstant.ONE) {
                throw new ImportDatasetException(" ??????????????????????????????????????????" + entry.getKey() + "???");
            }
        }
        File[] imageFiles = imageRootFiles.listFiles();
        if (imageFiles == null || imageFiles.length == MagicNumConstant.ZERO) {
            throw new ImportDatasetException(" ??????????????????????????? ");
        }
        log.info("........??????????????????,??????????????????..............");
        for (File imageFile : imageFiles) {
            String suffixFileName = imageFile.getName().substring(imageFile.getName().lastIndexOf(BusinessConstant.SPOT));
            if (dataset.getDataType().compareTo(DatatypeEnum.IMAGE.getValue()) == 0) {
                if (!imageConfig.getImageFormat().contains(suffixFileName.toLowerCase())) {
                    throw new ImportDatasetException(" ?????????????????????????????????????????? ");
                }
            } else {
                if (!imageConfig.getTxtFormat().contains(suffixFileName.toLowerCase())) {
                    throw new ImportDatasetException(" ?????????????????????????????????????????? ");
                }
            }

        }
        File[] annotationFiles = annotationRootFiles.listFiles();
        if (annotationFiles == null || annotationFiles.length == MagicNumConstant.ZERO) {
            throw new ImportDatasetException(" ?????????????????????????????? ");
        }
        log.info("........????????????????????????,???????????????????????????,??????????????????.........");
        log.info("........????????????????????????,??????????????????..............");
        for (File annotationFile : annotationFiles) {
            if (!annotationFile.getName().toLowerCase().endsWith(BusinessConstant.SUFFIX_JSON.toLowerCase())) {
                throw new ImportDatasetException(" ?????????????????????????????????????????? ");
            }
            if (!containsJsonKey(annotationFile)) {
                throw new ImportDatasetException(" ???????????????" + annotationFile.getName() + "??? ?????????'name'?????? ");
            }
        }
        log.info("........??????????????????????????????,???????????????????????????,??????????????????..............");
        return labelJsonFile;
    }

    /**
     * ????????????
     *
     * @param filePath ???????????????
     * @param dataset  ?????????
     */
    public void executeUploadAndSave(List<DataLabel> dataLabelList, String filePath, Dataset dataset) throws Exception {
        String localImageFilePath = filePath + HandleFileUtil.generateFilePath(BusinessConstant.IMAGE_ORIGIN);
        List<String> imageFileNameList = FileUtil.listFileNames(localImageFilePath);
        log.warn("........????????????????????????" + imageFileNameList.size() + "????????????,??????????????????.........");
        int batchNumber = MagicNumConstant.ZERO;
        int oneSize = ThreadUtils.createThread(imageFileNameList.size());
        ProcessBarUtil.initProcess("???????????????", (long) imageFileNameList.size());
        if (imageFileNameList.size() > MagicNumConstant.TEN_THOUSAND) {
            log.warn("........???????????????.........");
            List<List<String>> partitionList = Lists.partition(imageFileNameList, MagicNumConstant.FIVE_THOUSAND);
            for (List<String> imageFileNameList1 : partitionList) {
                batchNumber++;
                dealFileList(imageFileNameList1, oneSize, dataLabelList, filePath, dataset, batchNumber);
            }
        } else {
            log.warn("........???????????????.........");
            batchNumber++;
            dealFileList(imageFileNameList, oneSize, dataLabelList, filePath, dataset, batchNumber);
        }
    }

    /**
     * @param imageFileNameList ????????????
     * @param oneSize           ??????????????????
     * @param dataLabelList     ?????????????????????
     * @param filePath          ????????????
     * @param dataset           ?????????
     * @throws Exception
     */
    public void dealFileList(List<String> imageFileNameList, int oneSize, List<DataLabel> dataLabelList, String filePath, Dataset dataset, int batchNumber) throws Exception {
        int dealSize = MagicNumConstant.ZERO;
        List<Callable<Integer>> partitions = new ArrayList<>();
        List<String> need = new ArrayList<>();
        for (int i = 0; i < imageFileNameList.size(); i++) {
            need.add(imageFileNameList.get(i));
            if (need.size() == oneSize || i == imageFileNameList.size() - MagicNumConstant.ONE) {
                List<String> fileNameList = new ArrayList<>(need);
                dealSize += fileNameList.size();
                need.clear();
                partitions.add(() -> runTask(dataLabelList, dataset, fileNameList, filePath));
            }
        }
        ThreadUtils.runMultiThread(partitions);
    }

    /**
     * ??????????????????
     *
     * @param dataset             ?????????
     * @param fileNameList        ??????????????????
     * @param dataSetRootFilePath ????????????
     * @return Integer ????????????
     */
    private Integer runTask(List<DataLabel> dataLabelList, Dataset dataset, List<String> fileNameList, String dataSetRootFilePath) throws Exception {
        Integer success = MagicNumConstant.ZERO;
        List<DataFile> dataFilesList = new ArrayList<>();
        String imageFileBaseDir = BusinessConstant.MINIO_ROOT_PATH + BusinessConstant.FILE_SEPARATOR + dataset.getId()
                + BusinessConstant.FILE_SEPARATOR + BusinessConstant.IMAGE_ORIGIN + BusinessConstant.FILE_SEPARATOR;
        String annotationFileBaseDir = BusinessConstant.MINIO_ROOT_PATH + BusinessConstant.FILE_SEPARATOR + dataset.getId()
                + BusinessConstant.FILE_SEPARATOR + BusinessConstant.ANNOTATION + BusinessConstant.FILE_SEPARATOR;
        for (int i = 0; i < fileNameList.size(); i++) {
            String imageUploadFile = imageFileBaseDir + fileNameList.get(i);
            String annotationFileName = HandleFileUtil.readFileName(fileNameList.get(i));
            File  annotationFile = new File(dataSetRootFilePath + HandleFileUtil.generateFilePath(BusinessConstant.ANNOTATION) + BusinessConstant.FILE_SEPARATOR + annotationFileName + BusinessConstant.SUFFIX_JSON.toLowerCase());
            JSONArray jsonArray = replaceJsonNode(annotationFile, dataLabelList);
            minioUtil.upLoadFile(dataSetRootFilePath + HandleFileUtil.generateFilePath(BusinessConstant.IMAGE_ORIGIN) + BusinessConstant.FILE_SEPARATOR + fileNameList.get(i), imageUploadFile);
            String tempFilePath = annotationFile.getAbsolutePath() + "_temp.json";
            FileUtil.appendString(jsonArray.toJSONString(), tempFilePath, "UTF-8");
            minioUtil.upLoadFileByInputStream(annotationFileBaseDir + annotationFileName, tempFilePath);
            FileUtil.del(tempFilePath);
            datasetService.updateDatasetStatusIsImport(dataset);
            DataFile dataFile = new DataFile(annotationFileName, dataset.getId(), minioConfig.getBucketName() + BusinessConstant.FILE_SEPARATOR + imageUploadFile, dataset.getCreateUserId(),
                    FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE, MagicNumConstant.ZERO, MagicNumConstant.ZERO_LONG, dataset.getCreateUserId());
            if (dataset.getDataType().compareTo(DatatypeEnum.IMAGE.getValue()) == 0) {
                BufferedImage image;
                try {
                    image = ImageIO.read(new File(dataSetRootFilePath + HandleFileUtil.generateFilePath(BusinessConstant.IMAGE_ORIGIN) + BusinessConstant.FILE_SEPARATOR + fileNameList.get(i)));
                } catch (IOException e) {
                    throw new ImportDatasetException(" ??????????????????????????? ");
                }
                dataFile.setWidth(image.getWidth());
                dataFile.setHeight(image.getHeight());
            }
            dataFilesList.add(dataFile);
            if (dataFilesList.size() % MagicNumConstant.FIVE_HUNDRED == MagicNumConstant.ZERO || i == fileNameList.size() - MagicNumConstant.ONE) {
                if(!CollectionUtils.isEmpty(dataFilesList)){
                    Queue<Long> dataFileIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_FILE, dataFilesList.size());
                    for (DataFile dataFileEntity : dataFilesList) {
                        dataFileEntity.setId(dataFileIds.poll());
                    }
                    saveDataFile(dataFilesList);
                }
                List<DataVersionFile> dataVersionFileList = new ArrayList<>();
                for (DataFile file : dataFilesList) {
                    File annotationFileTxt = new File(dataSetRootFilePath + HandleFileUtil.generateFilePath(BusinessConstant.ANNOTATION) + BusinessConstant.FILE_SEPARATOR + file.getName() + BusinessConstant.SUFFIX_JSON.toLowerCase());
                    JSONArray jsonArrayTxt = replaceJsonNode(annotationFileTxt, dataLabelList);
                    DataVersionFile dataVersionFile = new DataVersionFile(dataset.getId(), file.getId(), FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE, MagicNumConstant.ZERO, file.getName());
                    dataVersionFileList.add(dataVersionFile);
                    if (DatatypeEnum.TXT.getValue().equals(dataset.getDataType())) {
                        try {
                            String bucketName = StringUtils.substringBefore(file.getUrl(), "/");
                            String fullFilePath = StringUtils.substringAfter(file.getUrl(), "/");
                            String content = minioUtil.readString(bucketName, fullFilePath);
                            Map<String, String> jsonMap = new HashMap<>();
                            jsonMap.put("content", content);
                            jsonMap.put("name", file.getName());
                            jsonMap.put("status", FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE.toString());
                            jsonMap.put("datasetId", dataset.getId().toString());
                            jsonMap.put("createUserId", file.getCreateUserId() == null ? null : file.getCreateUserId().toString());
                            jsonMap.put("createTime", file.getCreateTime() == null ? null : file.getCreateTime().toString());
                            jsonMap.put("updateUserId", file.getUpdateUserId() == null ? null : file.getUpdateUserId().toString());
                            jsonMap.put("updateTime", file.getUpdateTime() == null ? null : file.getUpdateTime().toString());
                            jsonMap.put("fileType", file.getFileType() == null ? null : file.getFileType().toString());
                            jsonMap.put("enhanceType", file.getEnhanceType() == null ? null : file.getEnhanceType().toString());
                            jsonMap.put("originUserId", file.getOriginUserId().toString());
                            jsonMap.put("prediction", jsonArrayTxt.getJSONObject(0).get("score").toString());
                            jsonMap.put("labelId", jsonArrayTxt.getJSONObject(0).get("category_id").toString());
                            jsonMap.put("versionName", StringUtils.isEmpty(dataset.getCurrentVersionName())?"V0000" : dataset.getCurrentVersionName());
                            bulkProcessor.add(new IndexRequest(esIndex, "_doc", file.getId().toString()).source(jsonMap));
                        } catch (Exception e) {
                            LogUtil.error(LogEnum.BIZ_DATASET, "??????es??????: {} ", e);
                        }
                    }
                }
                if(!CollectionUtils.isEmpty(dataVersionFileList)){
                    Queue<Long> dataFileVersionIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_VERSION_FILE, dataVersionFileList.size());
                    for (DataVersionFile dataVersionFile : dataVersionFileList) {
                        dataVersionFile.setId(dataFileVersionIds.poll());
                    }
                    saveDataVersionFile(dataVersionFileList);
                }
                List<DataFileAnnotation> dataFileAnnotations = new ArrayList<>();
                for (DataVersionFile dataVersionFile : dataVersionFileList) {
                    File  annotationFileDb = new File(dataSetRootFilePath + HandleFileUtil.generateFilePath(BusinessConstant.ANNOTATION) + BusinessConstant.FILE_SEPARATOR + dataVersionFile.getFileName() + BusinessConstant.SUFFIX_JSON.toLowerCase());
                    JSONArray jsonArrayDb = replaceJsonNode(annotationFileDb, dataLabelList);
                    List<AnnotationDTO> annotationDTOSDb = JSONObject.parseArray(jsonArrayDb.toJSONString(), AnnotationDTO.class);
                    if(!CollectionUtils.isEmpty(jsonArrayDb)){
                        if (AnnotateTypeEnum.CLASSIFICATION.getValue().equals(dataset.getAnnotateType()) || AnnotateTypeEnum.TEXT_CLASSIFICATION.getValue().equals(dataset.getAnnotateType())) {
                            AnnotationDTO annotationDTO = annotationDTOSDb.stream().max(Comparator.comparingDouble(AnnotationDTO::getScore)).get();
                            Long labelId1 = annotationDTO.getCategoryId();
                            Double perdiction = annotationDTO.getScore();
                            dataFileAnnotations.add(new DataFileAnnotation(dataset.getId(), labelId1, dataVersionFile.getId(), perdiction, dataset.getCreateUserId(), dataVersionFile.getFileName()));
                        }
                        if (AnnotateTypeEnum.OBJECT_DETECTION.getValue().equals(dataset.getAnnotateType()) || AnnotateTypeEnum.OBJECT_TRACK.getValue().equals(dataset.getAnnotateType())
                                || AnnotateTypeEnum.SEMANTIC_CUP.getValue().equals(dataset.getAnnotateType())) {
                            for (int j = 0; j < jsonArrayDb.size(); j++) {
                                Object perdictionObject = jsonArrayDb.getJSONObject(j).get("score");
                                Double perdiction = null;
                                if (!Objects.isNull(perdictionObject)) {
                                    perdiction = Double.parseDouble(String.valueOf(perdictionObject));
                                }
                                Long labelId = (Long) jsonArrayDb.getJSONObject(j).get("category_id");
                                DataFileAnnotation dataFileAnnotation = new DataFileAnnotation(dataset.getId(), labelId, dataVersionFile.getId(), perdiction, dataset.getCreateUserId(), dataVersionFile.getFileName());
                                dataFileAnnotations.add(dataFileAnnotation);
                            }
                        }

                    }
                    if(!CollectionUtils.isEmpty(dataFileAnnotations)){
                        Queue<Long> dataFileAnnotationIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_FILE_ANNOTATION, dataFileAnnotations.size());
                        for (DataFileAnnotation dataFileAnnotation : dataFileAnnotations) {
                            dataFileAnnotation.setId(dataFileAnnotationIds.poll());
                        }
                        saveDataFileAnnotation(dataFileAnnotations);
                    }
                    dataFileAnnotations.clear();
                    }
                ProcessBarUtil.processBar01((long) dataVersionFileList.size());
                dataVersionFileList.clear();
                dataFilesList.clear();
            }
            success++;
        }
        return success;

    }

    /**
     * ????????????????????????
     *
     * @param file ??????file
     * @return String ??????????????????
     */
    public String InterceptingText(File file) {
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while ((s = br.readLine()) != null) {
                result = result + s;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String abstractTxt = StringUtils.substring(result, MagicNumConstant.ZERO, MagicNumConstant.FOUR_HUNDRED);
        return abstractTxt;
    }

    /**
     * ??????????????????JSON????????????
     *
     * @param annotationFile ????????????
     * @param dataLabelList  ???????????????
     */
    public JSONArray replaceJsonNode(File annotationFile, List<DataLabel> dataLabelList) throws IOException {
        JSONArray jsonArray = new JSONArray();
        if (annotationFile.exists()) {
            String annotationFileContext = HandleFileUtil.readFile(annotationFile);
            jsonArray = JSONArray.parseArray(annotationFileContext);
            if (!jsonArray.isEmpty()) {
                replaceAllNode(jsonArray, dataLabelList);
            }
        }
        return jsonArray;
    }

    /**
     * ???????????????
     *
     * @param jsonArray     ??????????????????
     * @param dataLabelList ????????????
     */
    public void replaceAllNode(JSONArray jsonArray, List<DataLabel> dataLabelList) {
        for (int i = MagicNumConstant.ZERO; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            jsonObject.put("category_id", findDataLabelId(dataLabelList, jsonObject.get("name").toString()));
            jsonObject.remove("name");
        }
    }

    /**
     * ???????????????????????????
     *
     * @param dataLabelList ????????????
     * @param objectValue   ??????????????????
     * @return long ???????????????Id
     */
    public long findDataLabelId(List<DataLabel> dataLabelList, String objectValue) {
        Optional<DataLabel> matchedDataLabel = dataLabelList.stream().filter(dataLabel -> objectValue.equals(dataLabel.getName())).findAny();
        if (!matchedDataLabel.isPresent()) {
            throw new ImportDatasetException(" ???????????????name???????????????????????????!");
        }
        return matchedDataLabel.get().getId();
    }

    /**
     * ??????JSON????????????key
     *
     * @param jsonArray json??????
     * @return JSON????????????key
     */
    public List<String> getJsonKeyList(JSONArray jsonArray) {
        List<String> listKey = new ArrayList<>();
        for (Object object : jsonArray) {
            LinkedHashMap<String, String> jsonMap = JSON.parseObject(object.toString(), new TypeReference<LinkedHashMap<String, String>>() {
            });
            for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
                listKey.add(entry.getKey());
            }
        }
        return listKey.stream().distinct().collect(Collectors.toList());
    }

    /**
     * ??????json?????????????????????name
     *
     * @param file ????????????
     * @return true/false true ??????  false ?????????
     */
    public boolean containsJsonKey(File file) {
        boolean flag = true;
        String annotationFileContext;
        try {
            annotationFileContext = HandleFileUtil.readFile(file);
        } catch (IOException e) {
            throw new ImportDatasetException(" ?????????" + file.getName() + "?????????????????????????????????????????????");
        }
        if (!StringUtils.isEmpty(annotationFileContext)) {
            JSONArray jsonArray = JSONArray.parseArray(annotationFileContext);
            for (Object object : jsonArray) {
                LinkedHashMap<String, String> jsonMap = JSON.parseObject(object.toString(), new TypeReference<LinkedHashMap<String, String>>() {
                });
                if (!jsonMap.containsKey("name")) {
                    flag = false;
                }
            }
        }
        return flag;
    }


    /**
     * ?????????????????????????????????
     *
     * @param file ????????????
     * @return List<DataLabel> ??????????????????
     */
    public List<DataLabel> readLabelContext(File file) throws IOException {
        String fileContext = HandleFileUtil.readFile(file);
        List<DataLabel> dataLabelList = JSONArray.parseArray(fileContext, DataLabel.class);
        for (DataLabel dataLabel : dataLabelList) {
            if (StringUtils.isEmpty(dataLabel.getName()) || StringUtils.isEmpty(dataLabel.getColor())) {
                throw new ImportDatasetException(" ?????????????????????,???????????? 'name' ?????? 'color' ");
            }
        }
        return dataLabelList;
    }

    /**
     * ??????????????????????????????
     *
     * @param labelGroupName ???????????????
     */
    public void dealLabelGroup(String labelGroupName) {
        String groupName = HandleFileUtil.getLabelGroupName(labelGroupName);
        int count = dataLabelGroupService.selectByLabelGroupName(groupName);
        if (count > MagicNumConstant.ZERO) {
            throw new ImportDatasetException(" ??????????????????" + groupName + "????????????????????????label_{name}.json????????? ");
        }
    }

    /**
     * ?????????????????????
     *
     * @param labelGroupName ???????????????
     * @param dataset        ?????????
     * @return DataLabelGroup ??????????????????
     */
    public DataLabelGroup saveDataLabelGroup(String labelGroupName, Dataset dataset) {
        long timeStamp = System.currentTimeMillis();
        DataLabelGroup dataLabelGroup = new DataLabelGroup();
        if (dataset.getDataType().equals(DatatypeEnum.TXT.getValue())) {
            dataLabelGroup.setLabelGroupType(MagicNumConstant.ONE);
        } else {
            dataLabelGroup.setLabelGroupType(MagicNumConstant.ZERO);
        }
        dataLabelGroup.setName(labelGroupName);
        dataLabelGroup.setOriginUserId(dataset.getCreateUserId());
        dataLabelGroup.setType(MagicNumConstant.ZERO_LONG);
        dataLabelGroup.setCreateUserId(dataset.getCreateUserId());
        dataLabelGroup.setCreateTime(new Timestamp(timeStamp));
        dataLabelGroup.setUpdateTime(new Timestamp(timeStamp));
        dataLabelGroupService.saveDataGroupLabel(dataLabelGroup);
        return dataLabelGroup;
    }

    /**
     * ??????????????????
     *
     * @param dataset          ?????????
     * @param listDataLabel    ????????????
     * @param dataLabelGroupId ?????????Id
     */
    public void saveDataLabel(Dataset dataset, List<DataLabel> listDataLabel, Long dataLabelGroupId) {
        listDataLabel.forEach(dataLabel -> dataLabel.setCreateUserId(dataset.getCreateUserId()));
        dataLabelService.saveBatchDataLabel(listDataLabel);
        List<DatasetDataLabel> listDatasetDataLabel = new ArrayList<>();
        for (DataLabel dataLabel : listDataLabel) {
            DatasetDataLabel datasetDataLabel = new DatasetDataLabel();
            datasetDataLabel.setLabelId(dataLabel.getId());
            datasetDataLabel.setDatasetId(dataset.getId());
            listDatasetDataLabel.add(datasetDataLabel);
        }
        saveDatasetDataLabel(listDatasetDataLabel);

        List<DataGroupLabel> listDataGroupLabel = new ArrayList<>();
        for (DatasetDataLabel datasetDataLabel : listDatasetDataLabel) {
            DataGroupLabel dataGroupLabel = new DataGroupLabel();
            dataGroupLabel.setLabelId(datasetDataLabel.getLabelId());
            dataGroupLabel.setLabelGroupId(dataLabelGroupId);
            listDataGroupLabel.add(dataGroupLabel);
        }
        saveDatasetDataGroupLabel(listDataGroupLabel);

    }

    /**
     * ??????????????????
     *
     * @param listDataFile file??????
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDataFile(List<DataFile> listDataFile) {
        dataFileService.saveBatchDataFile(listDataFile);
    }

    /**
     * ??????????????????????????????
     *
     * @param listDataVersionFile ??????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDataVersionFile(List<DataVersionFile> listDataVersionFile) {
        dataVersionFileService.saveBatchDataFileVersion(listDataVersionFile);
    }


    /**
     * ???????????????????????????????????????
     *
     * @param listDatasetDataLabel ???????????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDatasetDataLabel(List<DatasetDataLabel> listDatasetDataLabel) {
        datasetDataLabelService.saveBatchDatasetDataLabel(listDatasetDataLabel);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param listDataGroupLabel ????????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDatasetDataGroupLabel(List<DataGroupLabel> listDataGroupLabel) {
        dataGroupLabelService.saveDataGroupLabel(listDataGroupLabel);
    }

    /**
     * ????????????nlp?????????
     *
     * @param dataFileAnnotations nlp??????
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDataFileAnnotation(List<DataFileAnnotation> dataFileAnnotations) {
        dataFileAnnotationService.saveDataFileAnnotation(dataFileAnnotations);
    }

    /**
     * ???????????????
     *
     * @param datasetId ?????????Id
     * @return Dataset  ???????????????ID????????????????????????
     */
    private Dataset findDataset(Long datasetId) {
        return datasetService.findDatasetById(datasetId);
    }

    /**
     * ???????????????ID
     *
     * @param scanner ?????????????????????
     * @return Dataset ?????????
     */
    public Dataset verificationDatasetId(Scanner scanner) {
        boolean flag = false;
        Dataset dataset = new Dataset();
        while (!flag) {
            System.out.println(" ");
            System.out.println("# ??????????????????ID #");
            String datasetIdStr = scanner.nextLine();
            long datasetId = 0;
            try {
                datasetId = Long.parseLong(datasetIdStr.trim());
            } catch (Exception e) {
                log.error("");
                PrintUtils.printLine("  Error: ?????????ID??????,???????????????", PrintUtils.RED);
                log.error("");
                continue;
            }
            dataset = findDataset(datasetId);
            if (dataset == null) {
                log.error("");
                PrintUtils.printLine("  Error: ?????????ID???????????????????????????", PrintUtils.RED);
                log.error("");
                continue;
            }
            int countDataLabel = datasetService.findDataLabelById(dataset.getId());
            int countDataFile = datasetService.findDataFileById(dataset.getId());
            if (countDataLabel > MagicNumConstant.ZERO || countDataFile > MagicNumConstant.ZERO) {
                log.error("");
                PrintUtils.printLine("  Error: ??????????????????????????????????????????????????? ", PrintUtils.RED);
                log.error("");
                continue;
            } else {
                flag = true;
            }
        }
        return dataset;
    }

    /**
     * ??????????????????
     *
     * @param scanner ???????????????
     * @return String ?????????
     */
    public String verificationFilePath(Scanner scanner) {
        boolean flag = false;
        String filePath = "";
        while (!flag) {
            System.out.println(" ");
            System.out.println("# ????????????????????????????????????????????? #");
            filePath = scanner.nextLine();
            File file = new File(filePath.trim());
            if (!file.exists()) {
                log.error("");
                PrintUtils.printLine("  ???" + filePath + "????????????????????????,???????????????", PrintUtils.RED);
                log.error("");
                continue;
            } else {
                flag = true;
            }
        }
        return filePath;
    }
}
