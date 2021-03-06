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
package org.dubhe.datasetutil.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.config.MinioConfig;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.common.constant.FileStateCodeConstant;
import org.dubhe.datasetutil.common.enums.DatatypeEnum;
import org.dubhe.datasetutil.common.enums.LogEnum;
import org.dubhe.datasetutil.common.enums.PresetDatasetEnum;
import org.dubhe.datasetutil.common.exception.ImportDatasetException;
import org.dubhe.datasetutil.common.util.*;
import org.dubhe.datasetutil.domain.dto.FileAnnotationDTO;
import org.dubhe.datasetutil.domain.entity.*;
import org.dubhe.datasetutil.service.*;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.dubhe.datasetutil.common.constant.BusinessConstant.FILE_SEPARATOR;

/**
 * @description ??????????????????????????????
 * @date 2020-10-12
 */
@Slf4j
@Component
public class PresetDatasetImportHandle {

    /**
     * esSearch??????
     */
    @Value("${es.index}")
    private String esIndex;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DataFileService dataFileService;

    @Autowired
    private DataLabelService dataLabelService;

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

    @Resource
    private BulkProcessor bulkProcessor;

    private final AtomicInteger fileCount = new AtomicInteger();


    private final List<File> annotationFiles = new LinkedList<>();

    private final List<File> originFiles = new LinkedList<>();

    private final Map<String, FileAnnotationDTO> fileAnnotationMap = new ConcurrentHashMap<>();


    @Value("${minio.dosAddress}")
    private String dosAddress;

    private final static Set<String> datasetIds = new HashSet<>();


    private volatile List<DataLabel> labels = new ArrayList<>();

    static {
        PresetDatasetEnum[] values = PresetDatasetEnum.values();
        for (PresetDatasetEnum datasetEnum : values) {
            datasetIds.add(datasetEnum.getType());
        }
    }

    /**
     * ?????????????????????
     *
     * @param scanner ?????????????????????
     */
    public synchronized void importPresetDataset(Scanner scanner) {
        //?????????????????????
        long datasetId = verificationDatasetId(scanner);
        try {
            LocalDateTime startTime = LocalDateTime.now();
            //???????????????????????????sql????????????
            String rootPath = verificationFilePathAndSaveSqlData(scanner, datasetId);
            //??????????????????????????????
            Dataset dataset = findDataset(datasetId);
            if (Objects.isNull(dataset)) {
                throw new ImportDatasetException("?????????ID: " + datasetId + "?????????!");
            }
            //??????????????? minio
            executeUploadToMinio(dataset, rootPath);
            executeUploadToDB(dataset);
            LocalDateTime endTime = LocalDateTime.now();
            Duration between = Duration.between(startTime, endTime);
            log.warn("");
            PrintUtils.printLine("  Success: ????????????  ", PrintUtils.GREEN);
            PrintUtils.printLine(" ??????????????????:{" + startTime + "}   ??????????????????:{" + endTime + "}   ???????????????(??????){" + between.toMinutes() + "}", PrintUtils.YELLOW);
            log.warn("");
            System.out.println("#   ????????????? Y / N  #");
            Scanner scannerExit = new Scanner(System.in);
            if (BusinessConstant.Y.toLowerCase().equals(scannerExit.nextLine().toLowerCase())) {
                System.exit(MagicNumConstant.ZERO);
            }
        } catch (Exception e) {
            log.error("");
            PrintUtils.printLine("  Error???" + e.getMessage(), PrintUtils.RED);
            log.error("");
            Dataset dataset = findDataset(datasetId);
            if (!Objects.isNull(dataset)) {
                PrintUtils.printLine("  ????????????,????????????????????????,??????????????????  ", PrintUtils.RED);
                //??????minio??????
                delDatasetMinioInfo(dataset.getUri());
                //?????????????????????
                delDatasetInfoById(datasetId, dataset.getDataType());
            }
        } finally {
            originFiles.clear();
            annotationFiles.clear();
            labels.clear();
        }

    }


    /**
     * ?????????????????????Minio
     *
     * @param dataset  ???????????????
     * @param rootPath ???????????????
     * @throws Exception ????????????
     */
    private void executeUploadToMinio(Dataset dataset, String rootPath) throws Exception {
        List<File> allFileList = new LinkedList<>(annotationFiles);
        allFileList.addAll(originFiles);
        log.warn("........????????????????????????" + allFileList.size() + "????????????,??????????????????.........");
        int batchNumber = MagicNumConstant.ZERO;
        int oneSize = ThreadUtils.createThread(allFileList.size());
        ProcessBarUtil.initProcess("?????????????????????", (long) allFileList.size());
        if (allFileList.size() > MagicNumConstant.TEN_THOUSAND) {
            log.warn("........???????????????.........");
            List<List<File>> partitionList = Lists.partition(allFileList, MagicNumConstant.FIVE_THOUSAND);
            for (List<File> imageFileNameList1 : partitionList) {
                batchNumber++;
                dealFileListToMinio(imageFileNameList1, oneSize, dataset, batchNumber, rootPath);
            }
        } else {
            log.warn("........???????????????.........");
            batchNumber++;
            dealFileListToMinio(allFileList, oneSize, dataset, batchNumber, rootPath);
        }

    }


    /**
     * ??????????????????????????????
     *
     * @param dataset ???????????????
     * @throws Exception ????????????
     */
    private void executeUploadToDB(Dataset dataset) throws Exception {
        log.warn("........????????????????????????" + originFiles.size() + "????????????????????????,??????????????????.........");
        int batchNumber = MagicNumConstant.ZERO;
        int oneSize = ThreadUtils.createThread(originFiles.size());
        //???????????????????????????????????????
        if (DatatypeEnum.VIDEO.getValue().compareTo(dataset.getDataType()) == 0) {
            sortByName(originFiles);
            runTaskSql(originFiles, dataset);
            log.warn("#-------------????????????????????????????????? ???" + oneSize + "??????-------------#");
            return;
        }
        if (originFiles.size() > MagicNumConstant.TEN_THOUSAND) {
            List<List<File>> partitionList = Lists.partition(originFiles, MagicNumConstant.FIVE_THOUSAND);
            for (List<File> imageFileNameList1 : partitionList) {
                batchNumber++;
                LogUtil.info(LogEnum.BIZ_DATASET, "?????? ???" + batchNumber + "?????????,??????????????????" + imageFileNameList1.size() + "??? ??????: ");
                dealFileListToSql(imageFileNameList1, oneSize, dataset, batchNumber);
            }
        } else {
            batchNumber++;
            dealFileListToSql(originFiles, oneSize, dataset, batchNumber);
        }

    }


    /**
     * ????????????????????????minio
     *
     * @param allFileList ????????????
     * @param oneSize     ??????????????????
     * @param dataset     ???????????????
     * @param batchNumber ????????????
     * @param rootPath    ?????????
     * @throws Exception ????????????
     */
    public void dealFileListToMinio(List<File> allFileList, int oneSize, Dataset dataset, int batchNumber, String rootPath) throws Exception {
        List<Callable<Integer>> partitions = new LinkedList<>();
        List<File> need = new LinkedList<>();
        for (int i = 0; i < allFileList.size(); i++) {
            need.add(allFileList.get(i));
            if (need.size() == oneSize || i == allFileList.size() - MagicNumConstant.ONE) {
                List<File> fileNameList = new LinkedList<>(need);

                need.clear();
                partitions.add(() -> runTask(fileNameList, dataset));
            }
        }
        ThreadUtils.runMultiThread(partitions);
    }


    /**
     * ????????????????????????sql
     *
     * @param allFileList ????????????
     * @param oneSize     ??????????????????
     * @param dataset     ???????????????
     * @param batchNumber ????????????
     * @throws Exception ????????????
     */
    public void dealFileListToSql(List<File> allFileList, int oneSize, Dataset dataset, int batchNumber) throws Exception {
        int dealSize = MagicNumConstant.ZERO;
        List<Callable<Integer>> partitions = new LinkedList<>();
        List<File> need = new LinkedList<>();
        for (int i = 0; i < allFileList.size(); i++) {
            need.add(allFileList.get(i));
            if (need.size() == oneSize || i == allFileList.size() - MagicNumConstant.ONE) {
                List<File> fileNameList = new LinkedList<>(need);
                dealSize += fileNameList.size();
                LogUtil.info(LogEnum.BIZ_DATASET, "????????????????????? ???" + batchNumber + "?????????,??????????????????" + dealSize + "????????????????????????");
                need.clear();
                partitions.add(() -> runTaskSql(fileNameList, dataset));
            }
        }
        ThreadUtils.runMultiThread(partitions);
    }


    /**
     * ??????????????????????????????
     *
     * @param files   ????????????
     * @param dataset ???????????????
     * @return ????????????
     */
    private Integer runTaskSql(List<File> files, Dataset dataset) {
        Integer success = MagicNumConstant.ZERO;
        List<DataFile> dataFilesList = new LinkedList<>();
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            //????????????
            String absolutePath = file.getAbsolutePath();
            //????????? /${datasetID}/
            String rootName = BusinessConstant.FILE_SEPARATOR + dataset.getId() + BusinessConstant.FILE_SEPARATOR;
            // dubhe-dev/dataset/${datasetID}/origin/${a.jpg}
            String fileName = minioConfig.getBucketName() + File.separator + BusinessConstant.MINIO_ROOT_PATH + rootName +
                    StringUtils.substringAfter(absolutePath, File.separator + dataset.getId() + File.separator);
            //?????? Linux ??????
            String targetFilePath = StringUtils.replaceChars(fileName, "\\", "/");
            //?????? dataset??????
            DataFile dataFile = new DataFile();
            dataFile.setName(HandleFileUtil.readFileName(file.getName()));
            dataFile.setUrl(targetFilePath);
            dataFile.setStatus(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE);
            dataFile.setDatasetId(dataset.getId());
            dataFile.setFileType(MagicNumConstant.ZERO);
            dataFile.setPid(MagicNumConstant.ZERO_LONG);
            dataFile.setCreateUserId(dataset.getCreateUserId());
            dataFile.setOriginUserId(MagicNumConstant.ZERO_LONG);
            if (dataset.getDataType().compareTo(DatatypeEnum.IMAGE.getValue()) == 0) {
                try {
                    BufferedImage image = ImageIO.read(file);
                    dataFile.setWidth(image.getWidth());
                    dataFile.setHeight(image.getHeight());
                } catch (IOException e) {
                    throw new ImportDatasetException(" ??????????????????????????? ");
                }
            }
            dataFile.setOriginUserId(MagicNumConstant.ZERO_LONG);
            dataFilesList.add(dataFile);
            // 500 ???????????? ????????????????????????
            if (dataFilesList.size() % MagicNumConstant.FIVE_HUNDRED == MagicNumConstant.ZERO || i == files.size() - MagicNumConstant.ONE) {
                Queue<Long> dataFileIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_FILE, dataFilesList.size());
                for (DataFile dataFileEntity : dataFilesList) {
                    dataFileEntity.setId(dataFileIds.poll());
                }
                //??? dataset_file ???
                dataFileService.saveBatchDataFile(dataFilesList);
                //?????? DatasetVersionFile??????
                List<DataVersionFile> dataVersionFileList = new ArrayList<>();
                for (DataFile datasetFile : dataFilesList) {
                    DataVersionFile dataVersionFile = new DataVersionFile();
                    dataVersionFile.setDatasetId(dataset.getId());
                    dataVersionFile.setFileId(datasetFile.getId());
                    dataVersionFile.setStatus(MagicNumConstant.TWO);
                    dataVersionFile.setVersionName(dataset.getDataType().compareTo(DatatypeEnum.TXT.getValue()) == 0 ? null : BusinessConstant.V0001);
                    dataVersionFile.setAnnotationStatus(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE);
                    dataVersionFile.setFileName(datasetFile.getName());
                    dataVersionFileList.add(dataVersionFile);
                }
                Queue<Long> dataFileVersionIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_VERSION_FILE, dataVersionFileList.size());
                for (DataVersionFile dataVersionFile : dataVersionFileList) {
                    dataVersionFile.setId(dataFileVersionIds.poll());
                }
                //??? dataset_version_file ???
                dataVersionFileService.saveBatchDataFileVersion(dataVersionFileList);

                List<DataFileAnnotation> dataFileAnnotations = dataVersionFileList.stream().map(dataVersionFile -> {

                            FileAnnotationDTO fileAnnotationDTO = null;
                            try {
                                fileAnnotationDTO = fileAnnotationMap.get(dataVersionFile.getFileName());
                                //?????? datasetFileAnnotation ??????
                                DataFileAnnotation dataFileAnnotation = DataFileAnnotation.builder()
                                        .datasetId(dataset.getId())
                                        .LabelId(ObjectUtils.isEmpty(fileAnnotationDTO) ? null : fileAnnotationDTO.getCategoryId())
                                        .prediction(1D)
                                        .versionFileId(dataVersionFile.getId())
                                        .build();
                                if (DatatypeEnum.TXT.getValue().equals(dataset.getDataType())) {
                                    try {
                                        String bucketName = StringUtils.substringBefore(dataFile.getUrl(), "/");
                                        String fullFilePath = StringUtils.substringAfter(dataFile.getUrl(), "/");
                                        String content = minioUtil.readString(bucketName, fullFilePath);
                                        Map<String, String> jsonMap = new HashMap<>();
                                        jsonMap.put("content", content);
                                        jsonMap.put("name", dataFile.getName());
                                        jsonMap.put("status", FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE.toString());
                                        jsonMap.put("datasetId", dataset.getId().toString());
                                        jsonMap.put("createUserId", dataFile.getCreateUserId() == null ? null : dataFile.getCreateUserId().toString());
                                        jsonMap.put("createTime", dataFile.getCreateTime() == null ? null : dataFile.getCreateTime().toString());
                                        jsonMap.put("updateUserId", dataFile.getUpdateUserId() == null ? null : dataFile.getUpdateUserId().toString());
                                        jsonMap.put("updateTime", dataFile.getUpdateTime() == null ? null : dataFile.getUpdateTime().toString());
                                        jsonMap.put("fileType", dataFile.getFileType() == null ? null : dataFile.getFileType().toString());
                                        jsonMap.put("enhanceType", dataFile.getEnhanceType() == null ? null : dataFile.getEnhanceType().toString());
                                        jsonMap.put("originUserId", dataFile.getOriginUserId().toString());
                                        jsonMap.put("prediction", "1");
                                        jsonMap.put("labelId", dataFileAnnotation.getLabelId().toString());
                                        jsonMap.put("versionName", StringUtils.isEmpty(dataset.getCurrentVersionName())?"V0000" : dataset.getCurrentVersionName());
                                        IndexRequest request = new IndexRequest(esIndex);
                                        request.source(jsonMap);
                                        request.id(dataVersionFile.getFileId().toString());
                                        bulkProcessor.add(request);
                                    } catch (Exception e) {
                                        LogUtil.error(LogEnum.BIZ_DATASET, "??????es??????: {} ", e);
                                    }
                                }
                                return ObjectUtils.isEmpty(dataFileAnnotation.getLabelId()) ? null : dataFileAnnotation;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                ).filter(dataVersionFile -> !ObjectUtils.isEmpty(dataVersionFile)).collect(Collectors.toList());
                Queue<Long> dataFileAnnotationIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_FILE_ANNOTATION, dataFileAnnotations.size());
                for (DataFileAnnotation dataFileAnnotation : dataFileAnnotations) {
                    dataFileAnnotation.setId(dataFileAnnotationIds.poll());
                }
                //??? dataset_file_annotation ???
                dataFileAnnotationService.saveDataFileAnnotation(dataFileAnnotations);

                dataFileAnnotations.clear();

                dataVersionFileList.clear();
                dataFilesList.clear();
            }
            success++;
        }
        bulkProcessor.flush();
        return success;

    }

    /**
     * ??????????????????
     *
     * @param files   ????????????
     * @param dataset ?????????
     * @return Integer ????????????
     */
    private Integer runTask(List<File> files, Dataset dataset) throws Exception {
        Integer success = MagicNumConstant.ZERO;

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            File parentFile = file.getParentFile();
            String absolutePath = file.getAbsolutePath();
            String rootName = BusinessConstant.FILE_SEPARATOR + dataset.getId() + BusinessConstant.FILE_SEPARATOR;
            String fileName = StringUtils.substringAfter(absolutePath, File.separator + dataset.getId() + File.separator);
            String targetFilePath = StringUtils.replaceChars(BusinessConstant.MINIO_ROOT_PATH + rootName + fileName, "\\", "/");

            if (BusinessConstant.ANNOTATION.equals(parentFile.getName()) || (
                    BusinessConstant.ANNOTATION.equals(parentFile.getParentFile().getName()) &&
                            BusinessConstant.V0001.equals(parentFile.getName())
            )) {
                targetFilePath = buildFileName(targetFilePath);
                JSONArray jsonArray = replaceJsonNode(file, labels, dataset);
                String tempFilePath = absolutePath + "_temp.json";
                FileUtil.appendString(jsonArray.toJSONString(), tempFilePath, "UTF-8");
                minioUtil.upLoadFileByInputStream(targetFilePath, tempFilePath);
                FileUtil.del(tempFilePath);
            } else {
                minioUtil.upLoadFile(absolutePath, targetFilePath);
            }
            ProcessBarUtil.processBar01(1L);
            success++;
        }

        return success;

    }


    /**
     * ??????????????????
     *
     * @param fileName ????????????
     * @return ?????????????????????
     */
    public String buildFileName(String fileName) {
        if (fileName.toLowerCase().endsWith(BusinessConstant.SUFFIX_JSON.toLowerCase())) {
            fileName = StringUtils.substringBefore(fileName, BusinessConstant.SUFFIX_JSON.toLowerCase());
        }
        return fileName;
    }

    /**
     * ???????????????ID
     *
     * @param scanner ?????????????????????
     */
    public long verificationDatasetId(Scanner scanner) {
        boolean flag = false;
        long datasetId = 0;
        while (!flag) {
            System.out.println("");
            System.out.println("#  ???????????????????????? (????????????: " + dosAddress + ") #");
            System.out.println("");
            for (PresetDatasetEnum presetDatasetEnum : PresetDatasetEnum.values()) {
                StringBuffer sb = new StringBuffer();
                sb.append("# ").append(presetDatasetEnum.getType()).append("???").append(presetDatasetEnum.getDesc()).append("  ");
                System.out.println(sb.toString());
            }
            String datasetIdStr = scanner.nextLine();

            try {
                datasetId = Long.parseLong(datasetIdStr.trim());
            } catch (Exception e) {
                log.error("");
                PrintUtils.printLine("  Error: ?????????ID????????????????????????", PrintUtils.RED);
                log.error("");
                continue;
            }

            long finalDatasetId = datasetId;
            Optional<PresetDatasetEnum> datasetEnum = Arrays.stream(PresetDatasetEnum.values()).filter(a -> a.getType().equals(String.valueOf(finalDatasetId))).findAny();
            if (!datasetEnum.isPresent()) {
                log.error("");
                PrintUtils.printLine("  Error: ?????????ID????????????????????????ID", PrintUtils.RED);
                log.error("");
                continue;
            }


            Dataset dataset = findDataset(datasetId);
            if (!Objects.isNull(dataset)) {
                log.error("");
                PrintUtils.printLine("  Error: ????????????????????????????????????", PrintUtils.RED);
                log.error("");
                continue;
            }


            flag = true;
        }

        return datasetId;
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
     * ???????????????
     *
     * @param datasetId ?????????Id
     * @return Dataset  ???????????????ID????????????????????????
     */
    private Dataset findDataset(Long datasetId) {
        return datasetService.findDatasetByIdNormal(datasetId);
    }


    /**
     * ??????????????????
     *
     * @param scanner   ???????????????
     * @param datasetId ?????????ID
     * @return String ?????????
     */
    public String verificationFilePathAndSaveSqlData(Scanner scanner, Long datasetId) throws Exception {
        boolean flag = false;
        String filePath = "";
        while (!flag) {
            System.out.println(" ");
            System.out.println("# ?????????????????????????????????????????????????????? #");
            filePath = scanner.nextLine();
            File file = new File(filePath.trim());

            if (!file.exists()) {
                log.error("");
                PrintUtils.printLine("  ???" + filePath + "??? ?????????????????????,???????????????", PrintUtils.RED);
                log.error("");
                continue;
            } else {
                //???????????????????????????????????????sql????????????
                log.info("........??????????????????,??????????????????.................");
                checkFileDirectoryAndSaveSqlData(filePath, datasetId);
                log.info("........??????????????????,???????????????????????????,??????????????????.................");
                flag = true;
            }
        }
        return filePath;
    }


    /**
     * ???????????????sql???????????????
     *
     * @param file sql??????
     */
    @Transactional(rollbackFor = Exception.class)
    public void readAndSaveSqlData(File file) throws Exception {
        List<String> list = HandleFileUtil.readFileInfo(file);
        if (!CollectionUtils.isEmpty(list)) {
            datasetService.saveBatch(list);
        }
    }


    /**
     * ??????????????????JSON????????????
     *
     * @param annotationFile ????????????
     * @param dataLabelList  ???????????????
     * @param dataset        ???????????????
     * @return ??????json??????
     * @throws IOException
     */
    public JSONArray replaceJsonNode(File annotationFile, List<DataLabel> dataLabelList, Dataset dataset) throws IOException {
        JSONArray jsonArray = new JSONArray();
        if (annotationFile.exists()) {
            String annotationFileContext = HandleFileUtil.readFile(annotationFile);
            jsonArray = JSONArray.parseArray(annotationFileContext);
            if (!jsonArray.isEmpty()) {
                replaceAllNode(jsonArray, dataLabelList, dataset, annotationFile.getName());
            }
        }
        return jsonArray;
    }

    /**
     * ???????????????
     *
     * @param jsonArray     ??????????????????
     * @param dataLabelList ????????????
     * @param dataset       ???????????????
     * @param fileName      ????????????
     */
    public void replaceAllNode(JSONArray jsonArray, List<DataLabel> dataLabelList, Dataset dataset, String fileName) {
        for (int i = MagicNumConstant.ZERO; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            jsonObject.put("category_id", findDataLabelId(dataLabelList, jsonObject.get("name").toString()));
            FileAnnotationDTO annotationDTO = jsonObject.toJavaObject(FileAnnotationDTO.class);
            fileAnnotationMap.put(buildFileName(fileName), annotationDTO);
            jsonObject.put("category_id",jsonObject.get("name"));
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
     * ??????????????????
     *
     * @param strPath   ????????????
     * @param datasetId ?????????ID
     */
    public void checkFileDirectoryAndSaveSqlData(String strPath, Long datasetId) throws Exception {
        File f = new File(strPath);
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files == null || Objects.requireNonNull(files).length == 0) {
                throw new ImportDatasetException(" ????????????  ???" + strPath + "????????????????????? ");
            }
            for (File file : files) {
                //??????????????????????????????????????????
                if (file.isDirectory()) {
                    //??????????????????
                    checkoutDirectoryName(file);
                    checkFileDirectoryAndSaveSqlData(file.getPath(), datasetId);
                    // /Downloads/COCO2017-val/1/   ????????????????????????
                    // annotation		dataset.sql		label_COCO2017-val.json	     origin		versionFile
                } else if (datasetIds.contains(file.getParentFile().getName())) {
                    //??????????????? sql??????
                    if (file.getName().toLowerCase().endsWith(BusinessConstant.SUFFIX_SQL.toLowerCase())) {
                        readAndSaveSqlData(file);
                    }
                    // ??????????????? .json ?????????????????????
                    if (file.getName().toLowerCase().endsWith(BusinessConstant.SUFFIX_JSON.toLowerCase())) {
                        labels = readLabelContext(file);
                        if (!CollectionUtils.isEmpty(labels)) {
                            dataLabelService.saveBatchDataLabel(labels);
                            List<DatasetDataLabel> dataLabels = labels.stream().map(a ->
                                    DatasetDataLabel.builder().datasetId(datasetId).labelId(a.getId()).build()).collect(Collectors.toList());
                            datasetDataLabelService.saveBatchDatasetDataLabel(dataLabels);
                        }
                    }
                    // /Downloads/COCO2017-val/1/   ?????????????????????????????????/1/??????????????????????????????
                } else if (!datasetIds.contains(file.getParentFile().getName())) {
                    ///Downloads/COCO2017-val/1/origin/
                    File parentFile = file.getParentFile();
                    // ??? origin ?????????
                    if (
                            BusinessConstant.IMAGE_ORIGIN.equals(parentFile.getName()) &&
                                    String.valueOf(datasetId).equals(parentFile.getParentFile().getName())
                    ) {
                        originFiles.add(file);
                    } else {
                        annotationFiles.add(file);
                    }
                    //????????????
                    fileCount.getAndIncrement();
                }


            }
        }
    }


    /**
     * ????????????????????????
     *
     * @param file ??????
     */
    public void checkoutDirectoryName(File file) {
        //???????????????
        String fileName = file.getName();
        //??????????????????
        String path = file.getPath();
        //??????????????????????????????????????????
        String parentFileName = file.getParentFile().getName();
        //???????????????????????????????????????????????????????????????
        Optional<PresetDatasetEnum> optional = Arrays.stream(PresetDatasetEnum.values()).filter(a -> a.getType().equals(parentFileName)).findAny();
        //???????????????????????? /Downloads/COCO2017-val/1/xxx/xxx    ?????????
        //?????????????????????????????????
        if (optional.isPresent() &&
                !(BusinessConstant.IMAGE_ORIGIN.equals(fileName) || BusinessConstant.VERSION_FILE.equals(fileName)
                        || BusinessConstant.ANNOTATION.equals(fileName) || BusinessConstant.VIDEO.equals(fileName))
        ) {
            log.error("");
            PrintUtils.printLine("  ???" + path + "??? ?????????????????????,???????????????", PrintUtils.RED);
            log.error("");
        } else if (BusinessConstant.ANNOTATION.equals(parentFileName) && !(BusinessConstant.V0001.equals(fileName))) {
            log.error("");
            PrintUtils.printLine("  ???" + path + "??? ?????????????????????,???????????????", PrintUtils.RED);
            log.error("");

        } else if (BusinessConstant.VERSION_FILE.equals(parentFileName) && !(BusinessConstant.V0001.equals(fileName))) {
            log.error("");
            PrintUtils.printLine("  ???" + path + "??? ?????????????????????,???????????????", PrintUtils.RED);
            log.error("");

        } else if (BusinessConstant.OFRECORD.equals(parentFileName) && !(BusinessConstant.TRAIN.equals(fileName))) {
            log.error("");
            PrintUtils.printLine("  ???" + path + "??? ?????????????????????,???????????????", PrintUtils.RED);
            log.error("");

        } else if (BusinessConstant.V0001.equals(parentFileName) &&
                !(BusinessConstant.IMAGE_ORIGIN.equals(fileName) || BusinessConstant.ANNOTATION.equals(fileName) || BusinessConstant.OFRECORD.equals(fileName))
        ) {
            log.error("");
            PrintUtils.printLine("  ???" + path + "??? ?????????????????????,???????????????", PrintUtils.RED);
            log.error("");

        }
    }


    /**
     * ????????????????????????minio????????????
     *
     * @param uri ????????????
     */
    private void delDatasetMinioInfo(String uri) {
        if (!Objects.isNull(uri)) {
            String path = minioConfig.getNfsRootPath() + minioConfig.getBucketName() + StrUtil.SLASH + uri;
            deleteFileByCMD(path);
        }

    }

    /**
     * ?????????????????????
     *
     * @param datasetId ?????????ID
     * @param dataType  ????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void delDatasetInfoById(long datasetId, Integer dataType) {
        datasetService.deleteDatasetById(datasetId);
        dataFileService.deleteFileByDatasetId(datasetId);
        dataVersionFileService.deleteVersionByDatasetId(datasetId);
        dataLabelService.deleteLabelByDatasetId(datasetId);
        datasetDataLabelService.deleteDatasetLabelByDatasetId(datasetId);
        if (DatatypeEnum.TXT.getValue().compareTo(dataType) == 0) {
            dataFileAnnotationService.delDataFileAnnotationById(datasetId);
        }
    }

    /**
     * ???????????????
     *
     * @param list ????????????
     */
    private void sortByName(List<File> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 1; j < list.size() - i; j++) {
                File a;
                if (compareByName(list.get(j - 1), list.get(j)) > 0) {
                    a = list.get(j - 1);
                    list.set((j - 1), list.get(j));
                    list.set(j, a);
                }
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param fileOne ????????????
     * @param fileTwo ????????????
     * @return ????????????
     */
    private int compareByName(File fileOne, File fileTwo) {
        return buildImgName(fileOne).compareTo(buildImgName(fileTwo));
    }


    /**
     * ??????????????????
     *
     * @param file ??????
     * @return ????????????
     */
    private Integer buildImgName(File file) {
        int value = MagicNumConstant.ZERO;
        try {
            value = Integer.parseInt(StringUtils.substringBefore(StringUtils.substringAfterLast(file.getName(), "_"), "."));
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "????????? ???" + file.getName() + "?????????????????????");
        }
        return value;
    }

    /**
     * ????????????
     *
     * @param path ????????????
     */
    public void deleteFileByCMD(String path) {
        String sourcePath = formatPath(path);
        //?????????????????????????????????????????????
        String emptyDir = "";
        String nfsBucket = minioConfig.getNfsRootPath() + minioConfig.getBucketName() + StrUtil.SLASH;
        sourcePath = sourcePath.endsWith(StrUtil.SLASH) ? sourcePath : sourcePath + StrUtil.SLASH;
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????/nfs/dubhe-test/xxxx/
        try {
            if (sourcePath.startsWith((nfsBucket))
                    && sourcePath.length() > nfsBucket.length()) {
                emptyDir = "/tmp/empty_" + RandomUtil.randomNumbers(10) + StrUtil.SLASH;
                LogUtil.info(LogEnum.BIZ_DATASET, "recycle task sourcePath:{},emptyDir:{}", sourcePath, emptyDir);
                String exec = "/bin/sh";
                String c = "-c";
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    exec = "cmd.exe";
                    c = "/C";
                }
                Process process = Runtime.getRuntime().exec(new String[]{exec, c,
                        String.format(BusinessConstant.DEL_COMMAND, minioConfig.getServerUserName(), minioConfig.getEndpoint(), emptyDir, emptyDir, sourcePath, emptyDir, sourcePath)});
                recycleSourceIsOk(process);
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "minio ???????????????????????????: {} ", e);
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param process Process??????
     * @return boolean linux????????????????????????????????????
     */
    public boolean recycleSourceIsOk(Process process) {
        InputStreamReader stream = new InputStreamReader(process.getErrorStream());
        BufferedReader reader = new BufferedReader(stream);
        StringBuffer errMessage = new StringBuffer();
        boolean recycleIsOk = true;
        try {
            while (reader.read() != MagicNumConstant.NEGATIVE_ONE) {
                errMessage.append(reader.readLine());
            }
            int status = process.waitFor();
            if (status != 0) {
                LogUtil.error(LogEnum.BIZ_DATASET, "???????????????????????????: {} ", errMessage.toString());
                recycleIsOk = false;
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "???????????????????????????: {} ", e);
            recycleIsOk = false;
        } finally {
            IOUtil.close(reader, stream);
        }
        return recycleIsOk;
    }


    /**
     * ???????????????????????? "/"
     *
     * @param path ??????
     * @return String
     */
    public String formatPath(String path) {
        if (!StringUtils.isEmpty(path)) {
            return path.replaceAll("///*", FILE_SEPARATOR);
        }
        return path;
    }


}
