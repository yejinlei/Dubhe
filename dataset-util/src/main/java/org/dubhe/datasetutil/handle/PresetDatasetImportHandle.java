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
 * @description 导入预置数据集工具类
 * @date 2020-10-12
 */
@Slf4j
@Component
public class PresetDatasetImportHandle {

    /**
     * esSearch索引
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
     * 导入预置数据集
     *
     * @param scanner 控制台输入数据
     */
    public synchronized void importPresetDataset(Scanner scanner) {
        //校验数据集信息
        long datasetId = verificationDatasetId(scanner);
        try {
            LocalDateTime startTime = LocalDateTime.now();
            //校验文件目录并保存sql文件信息
            String rootPath = verificationFilePathAndSaveSqlData(scanner, datasetId);
            //构建上传文件路径数据
            Dataset dataset = findDataset(datasetId);
            if (Objects.isNull(dataset)) {
                throw new ImportDatasetException("数据集ID: " + datasetId + "不存在!");
            }
            //上传文件到 minio
            executeUploadToMinio(dataset, rootPath);
            executeUploadToDB(dataset);
            LocalDateTime endTime = LocalDateTime.now();
            Duration between = Duration.between(startTime, endTime);
            log.warn("");
            PrintUtils.printLine("  Success: 执行成功  ", PrintUtils.GREEN);
            PrintUtils.printLine(" 执行开始时间:{" + startTime + "}   执行结束时间:{" + endTime + "}   执行总时长(分钟){" + between.toMinutes() + "}", PrintUtils.YELLOW);
            log.warn("");
            System.out.println("#   是否结束? Y / N  #");
            Scanner scannerExit = new Scanner(System.in);
            if (BusinessConstant.Y.toLowerCase().equals(scannerExit.nextLine().toLowerCase())) {
                System.exit(MagicNumConstant.ZERO);
            }
        } catch (Exception e) {
            log.error("");
            PrintUtils.printLine("  Error：" + e.getMessage(), PrintUtils.RED);
            log.error("");
            Dataset dataset = findDataset(datasetId);
            if (!Objects.isNull(dataset)) {
                PrintUtils.printLine("  执行异常,正在清理异常数据,请勿关闭窗口  ", PrintUtils.RED);
                //删除minio数据
                delDatasetMinioInfo(dataset.getUri());
                //删除数据集信息
                delDatasetInfoById(datasetId, dataset.getDataType());
            }
        } finally {
            originFiles.clear();
            annotationFiles.clear();
            labels.clear();
        }

    }


    /**
     * 实际上传文件到Minio
     *
     * @param dataset  数据集实体
     * @param rootPath 文件根路径
     * @throws Exception 上传异常
     */
    private void executeUploadToMinio(Dataset dataset, String rootPath) throws Exception {
        List<File> allFileList = new LinkedList<>(annotationFiles);
        allFileList.addAll(originFiles);
        log.warn("........系统需要处理：【" + allFileList.size() + "】份文件,请勿关闭窗口.........");
        int batchNumber = MagicNumConstant.ZERO;
        int oneSize = ThreadUtils.createThread(allFileList.size());
        ProcessBarUtil.initProcess("预置数据集导入", (long) allFileList.size());
        if (allFileList.size() > MagicNumConstant.TEN_THOUSAND) {
            log.warn("........系统处理中.........");
            List<List<File>> partitionList = Lists.partition(allFileList, MagicNumConstant.FIVE_THOUSAND);
            for (List<File> imageFileNameList1 : partitionList) {
                batchNumber++;
                dealFileListToMinio(imageFileNameList1, oneSize, dataset, batchNumber, rootPath);
            }
        } else {
            log.warn("........系统处理中.........");
            batchNumber++;
            dealFileListToMinio(allFileList, oneSize, dataset, batchNumber, rootPath);
        }

    }


    /**
     * 实际上传文件到数据库
     *
     * @param dataset 数据集实体
     * @throws Exception 上传异常
     */
    private void executeUploadToDB(Dataset dataset) throws Exception {
        log.warn("........系统需要处理：【" + originFiles.size() + "】份文件到数据库,请勿关闭窗口.........");
        int batchNumber = MagicNumConstant.ZERO;
        int oneSize = ThreadUtils.createThread(originFiles.size());
        //视频数据导入单线程顺序处理
        if (DatatypeEnum.VIDEO.getValue().compareTo(dataset.getDataType()) == 0) {
            sortByName(originFiles);
            runTaskSql(originFiles, dataset);
            log.warn("#-------------系统已总共成功处理文件 【" + oneSize + "】个-------------#");
            return;
        }
        if (originFiles.size() > MagicNumConstant.TEN_THOUSAND) {
            List<List<File>> partitionList = Lists.partition(originFiles, MagicNumConstant.FIVE_THOUSAND);
            for (List<File> imageFileNameList1 : partitionList) {
                batchNumber++;
                LogUtil.info(LogEnum.BIZ_DATASET, "第： 【" + batchNumber + "】批次,需要处理：【" + imageFileNameList1.size() + "】 文件: ");
                dealFileListToSql(imageFileNameList1, oneSize, dataset, batchNumber);
            }
        } else {
            batchNumber++;
            dealFileListToSql(originFiles, oneSize, dataset, batchNumber);
        }

    }


    /**
     * 多线程上传数据到minio
     *
     * @param allFileList 文件数据
     * @param oneSize     每次处理次数
     * @param dataset     数据集实体
     * @param batchNumber 上传批次
     * @param rootPath    根路径
     * @throws Exception 上传异常
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
     * 多线程上传数据到sql
     *
     * @param allFileList 文件数据
     * @param oneSize     每次处理次数
     * @param dataset     数据集实体
     * @param batchNumber 上传批次
     * @throws Exception 上传异常
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
                LogUtil.info(LogEnum.BIZ_DATASET, "系统将处理第： 【" + batchNumber + "】批次,需要处理：【" + dealSize + "】个文件至数据库");
                need.clear();
                partitions.add(() -> runTaskSql(fileNameList, dataset));
            }
        }
        ThreadUtils.runMultiThread(partitions);
    }


    /**
     * 实际实际上传执行方法
     *
     * @param files   上传文件
     * @param dataset 数据集实体
     * @return 执行次数
     */
    private Integer runTaskSql(List<File> files, Dataset dataset) {
        Integer success = MagicNumConstant.ZERO;
        List<DataFile> dataFilesList = new LinkedList<>();
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            //绝对路径
            String absolutePath = file.getAbsolutePath();
            //根目录 /${datasetID}/
            String rootName = BusinessConstant.FILE_SEPARATOR + dataset.getId() + BusinessConstant.FILE_SEPARATOR;
            // dubhe-dev/dataset/${datasetID}/origin/${a.jpg}
            String fileName = minioConfig.getBucketName() + File.separator + BusinessConstant.MINIO_ROOT_PATH + rootName +
                    StringUtils.substringAfter(absolutePath, File.separator + dataset.getId() + File.separator);
            //转换 Linux 斜杠
            String targetFilePath = StringUtils.replaceChars(fileName, "\\", "/");
            //构建 dataset对象
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
                    throw new ImportDatasetException(" 读取图片高和宽失败 ");
                }
            }
            dataFile.setOriginUserId(MagicNumConstant.ZERO_LONG);
            dataFilesList.add(dataFile);
            // 500 写一次库 或者最后写一次库
            if (dataFilesList.size() % MagicNumConstant.FIVE_HUNDRED == MagicNumConstant.ZERO || i == files.size() - MagicNumConstant.ONE) {
                Queue<Long> dataFileIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_FILE, dataFilesList.size());
                for (DataFile dataFileEntity : dataFilesList) {
                    dataFileEntity.setId(dataFileIds.poll());
                }
                //写 dataset_file 表
                dataFileService.saveBatchDataFile(dataFilesList);
                //构建 DatasetVersionFile对象
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
                //写 dataset_version_file 表
                dataVersionFileService.saveBatchDataFileVersion(dataVersionFileList);

                List<DataFileAnnotation> dataFileAnnotations = dataVersionFileList.stream().map(dataVersionFile -> {

                            FileAnnotationDTO fileAnnotationDTO = null;
                            try {
                                fileAnnotationDTO = fileAnnotationMap.get(dataVersionFile.getFileName());
                                //构建 datasetFileAnnotation 对象
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
                                        LogUtil.error(LogEnum.BIZ_DATASET, "上传es失败: {} ", e);
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
                //写 dataset_file_annotation 表
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
     * 实际执行任务
     *
     * @param files   上传文件
     * @param dataset 数据集
     * @return Integer 执行次数
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
     * 构建文件名称
     *
     * @param fileName 文件名称
     * @return 构建后文件名称
     */
    public String buildFileName(String fileName) {
        if (fileName.toLowerCase().endsWith(BusinessConstant.SUFFIX_JSON.toLowerCase())) {
            fileName = StringUtils.substringBefore(fileName, BusinessConstant.SUFFIX_JSON.toLowerCase());
        }
        return fileName;
    }

    /**
     * 校验数据集ID
     *
     * @param scanner 控制台输入参数
     */
    public long verificationDatasetId(Scanner scanner) {
        boolean flag = false;
        long datasetId = 0;
        while (!flag) {
            System.out.println("");
            System.out.println("#  请选择预置数据集 (参考文档: " + dosAddress + ") #");
            System.out.println("");
            for (PresetDatasetEnum presetDatasetEnum : PresetDatasetEnum.values()) {
                StringBuffer sb = new StringBuffer();
                sb.append("# ").append(presetDatasetEnum.getType()).append("：").append(presetDatasetEnum.getDesc()).append("  ");
                System.out.println(sb.toString());
            }
            String datasetIdStr = scanner.nextLine();

            try {
                datasetId = Long.parseLong(datasetIdStr.trim());
            } catch (Exception e) {
                log.error("");
                PrintUtils.printLine("  Error: 数据集ID非法，请重新输入", PrintUtils.RED);
                log.error("");
                continue;
            }

            long finalDatasetId = datasetId;
            Optional<PresetDatasetEnum> datasetEnum = Arrays.stream(PresetDatasetEnum.values()).filter(a -> a.getType().equals(String.valueOf(finalDatasetId))).findAny();
            if (!datasetEnum.isPresent()) {
                log.error("");
                PrintUtils.printLine("  Error: 数据集ID不属于预置数据集ID", PrintUtils.RED);
                log.error("");
                continue;
            }


            Dataset dataset = findDataset(datasetId);
            if (!Objects.isNull(dataset)) {
                log.error("");
                PrintUtils.printLine("  Error: 数据集已存在，请重新选择", PrintUtils.RED);
                log.error("");
                continue;
            }


            flag = true;
        }

        return datasetId;
    }

    /**
     * 读取标签文件中标签数据
     *
     * @param file 标签文件
     * @return List<DataLabel> 标签数据集合
     */
    public List<DataLabel> readLabelContext(File file) throws IOException {
        String fileContext = HandleFileUtil.readFile(file);
        List<DataLabel> dataLabelList = JSONArray.parseArray(fileContext, DataLabel.class);
        for (DataLabel dataLabel : dataLabelList) {
            if (StringUtils.isEmpty(dataLabel.getName()) || StringUtils.isEmpty(dataLabel.getColor())) {
                throw new ImportDatasetException(" 标签文件不规范,未能读到 'name' 或者 'color' ");
            }
        }
        return dataLabelList;
    }


    /**
     * 查询数据集
     *
     * @param datasetId 数据集Id
     * @return Dataset  根据数据集ID查询返回的数据集
     */
    private Dataset findDataset(Long datasetId) {
        return datasetService.findDatasetByIdNormal(datasetId);
    }


    /**
     * 校验文件路径
     *
     * @param scanner   输入控制台
     * @param datasetId 数据集ID
     * @return String 字符串
     */
    public String verificationFilePathAndSaveSqlData(Scanner scanner, Long datasetId) throws Exception {
        boolean flag = false;
        String filePath = "";
        while (!flag) {
            System.out.println(" ");
            System.out.println("# 请输入待上传本地预置数据集的完整路径 #");
            filePath = scanner.nextLine();
            File file = new File(filePath.trim());

            if (!file.exists()) {
                log.error("");
                PrintUtils.printLine("  【" + filePath + "】 文件路径不存在,请重新输入", PrintUtils.RED);
                log.error("");
                continue;
            } else {
                //校验文件目录是否合法并保存sql文件数据
                log.info("........数据校验开始,请勿关闭窗口.................");
                checkFileDirectoryAndSaveSqlData(filePath, datasetId);
                log.info("........数据校验完成,即将执行下一步操作,请勿关闭窗口.................");
                flag = true;
            }
        }
        return filePath;
    }


    /**
     * 读取并保存sql文件中数据
     *
     * @param file sql文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void readAndSaveSqlData(File file) throws Exception {
        List<String> list = HandleFileUtil.readFileInfo(file);
        if (!CollectionUtils.isEmpty(list)) {
            datasetService.saveBatch(list);
        }
    }


    /**
     * 检查并且替换JSON中的节点
     *
     * @param annotationFile 标注文件
     * @param dataLabelList  数据集集合
     * @param dataset        数据集实体
     * @return 标签json数据
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
     * 替换节点值
     *
     * @param jsonArray     标注文件集合
     * @param dataLabelList 标签集合
     * @param dataset       数据集实体
     * @param fileName      文件名称
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
     * 查询需要替换的节点
     *
     * @param dataLabelList 标签集合
     * @param objectValue   替换的节点值
     * @return long 替换标签的Id
     */
    public long findDataLabelId(List<DataLabel> dataLabelList, String objectValue) {
        Optional<DataLabel> matchedDataLabel = dataLabelList.stream().filter(dataLabel -> objectValue.equals(dataLabel.getName())).findAny();
        if (!matchedDataLabel.isPresent()) {
            throw new ImportDatasetException(" 标注文件中name的值不存在于标签中!");
        }
        return matchedDataLabel.get().getId();
    }


    /**
     * 校验文件目录
     *
     * @param strPath   文件地址
     * @param datasetId 数据集ID
     */
    public void checkFileDirectoryAndSaveSqlData(String strPath, Long datasetId) throws Exception {
        File f = new File(strPath);
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files == null || Objects.requireNonNull(files).length == 0) {
                throw new ImportDatasetException(" 文件目录  【" + strPath + "】下不存在文件 ");
            }
            for (File file : files) {
                //是文件夹则一层剥一层的去校验
                if (file.isDirectory()) {
                    //校验文件目录
                    checkoutDirectoryName(file);
                    checkFileDirectoryAndSaveSqlData(file.getPath(), datasetId);
                    // /Downloads/COCO2017-val/1/   在此目录文件夹下
                    // annotation		dataset.sql		label_COCO2017-val.json	     origin		versionFile
                } else if (datasetIds.contains(file.getParentFile().getName())) {
                    //读取并保存 sql文件
                    if (file.getName().toLowerCase().endsWith(BusinessConstant.SUFFIX_SQL.toLowerCase())) {
                        readAndSaveSqlData(file);
                    }
                    // 判断是否为 .json 结尾的标签文件
                    if (file.getName().toLowerCase().endsWith(BusinessConstant.SUFFIX_JSON.toLowerCase())) {
                        labels = readLabelContext(file);
                        if (!CollectionUtils.isEmpty(labels)) {
                            dataLabelService.saveBatchDataLabel(labels);
                            List<DatasetDataLabel> dataLabels = labels.stream().map(a ->
                                    DatasetDataLabel.builder().datasetId(datasetId).labelId(a.getId()).build()).collect(Collectors.toList());
                            datasetDataLabelService.saveBatchDatasetDataLabel(dataLabels);
                        }
                    }
                    // /Downloads/COCO2017-val/1/   不在此目录文件夹下（在/1/目录下的子文件夹中）
                } else if (!datasetIds.contains(file.getParentFile().getName())) {
                    ///Downloads/COCO2017-val/1/origin/
                    File parentFile = file.getParentFile();
                    // 在 origin 目录中
                    if (
                            BusinessConstant.IMAGE_ORIGIN.equals(parentFile.getName()) &&
                                    String.valueOf(datasetId).equals(parentFile.getParentFile().getName())
                    ) {
                        originFiles.add(file);
                    } else {
                        annotationFiles.add(file);
                    }
                    //文件计数
                    fileCount.getAndIncrement();
                }


            }
        }
    }


    /**
     * 校验文件目录名称
     *
     * @param file 文件
     */
    public void checkoutDirectoryName(File file) {
        //获取文件名
        String fileName = file.getName();
        //获取文件路径
        String path = file.getPath();
        //获取当前文件所在文件夹的名称
        String parentFileName = file.getParentFile().getName();
        //筛选出当前文件夹中符合预置数据集名称的文件
        Optional<PresetDatasetEnum> optional = Arrays.stream(PresetDatasetEnum.values()).filter(a -> a.getType().equals(parentFileName)).findAny();
        //文件路径如果输入 /Downloads/COCO2017-val/1/xxx/xxx    则错误
        //以下均为文件路径的校验
        if (optional.isPresent() &&
                !(BusinessConstant.IMAGE_ORIGIN.equals(fileName) || BusinessConstant.VERSION_FILE.equals(fileName)
                        || BusinessConstant.ANNOTATION.equals(fileName) || BusinessConstant.VIDEO.equals(fileName))
        ) {
            log.error("");
            PrintUtils.printLine("  【" + path + "】 文件路径不合法,请重新输入", PrintUtils.RED);
            log.error("");
        } else if (BusinessConstant.ANNOTATION.equals(parentFileName) && !(BusinessConstant.V0001.equals(fileName))) {
            log.error("");
            PrintUtils.printLine("  【" + path + "】 文件路径不合法,请重新输入", PrintUtils.RED);
            log.error("");

        } else if (BusinessConstant.VERSION_FILE.equals(parentFileName) && !(BusinessConstant.V0001.equals(fileName))) {
            log.error("");
            PrintUtils.printLine("  【" + path + "】 文件路径不合法,请重新输入", PrintUtils.RED);
            log.error("");

        } else if (BusinessConstant.OFRECORD.equals(parentFileName) && !(BusinessConstant.TRAIN.equals(fileName))) {
            log.error("");
            PrintUtils.printLine("  【" + path + "】 文件路径不合法,请重新输入", PrintUtils.RED);
            log.error("");

        } else if (BusinessConstant.V0001.equals(parentFileName) &&
                !(BusinessConstant.IMAGE_ORIGIN.equals(fileName) || BusinessConstant.ANNOTATION.equals(fileName) || BusinessConstant.OFRECORD.equals(fileName))
        ) {
            log.error("");
            PrintUtils.printLine("  【" + path + "】 文件路径不合法,请重新输入", PrintUtils.RED);
            log.error("");

        }
    }


    /**
     * 根据文件路径删除minio文件数据
     *
     * @param uri 文件路径
     */
    private void delDatasetMinioInfo(String uri) {
        if (!Objects.isNull(uri)) {
            String path = minioConfig.getNfsRootPath() + minioConfig.getBucketName() + StrUtil.SLASH + uri;
            deleteFileByCMD(path);
        }

    }

    /**
     * 删除数据集信息
     *
     * @param datasetId 数据集ID
     * @param dataType  数据类型
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
     * 按名称排序
     *
     * @param list 文件集合
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
     * 文件名称排序
     *
     * @param fileOne 文件名称
     * @param fileTwo 文件名称
     * @return 排序大小
     */
    private int compareByName(File fileOne, File fileTwo) {
        return buildImgName(fileOne).compareTo(buildImgName(fileTwo));
    }


    /**
     * 构建图片名称
     *
     * @param file 文件
     * @return 图片名称
     */
    private Integer buildImgName(File file) {
        int value = MagicNumConstant.ZERO;
        try {
            value = Integer.parseInt(StringUtils.substringBefore(StringUtils.substringAfterLast(file.getName(), "_"), "."));
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "文件： 【" + file.getName() + "】名称格式错误");
        }
        return value;
    }

    /**
     * 文件删除
     *
     * @param path 删除路径
     */
    public void deleteFileByCMD(String path) {
        String sourcePath = formatPath(path);
        //判断该路径是否存在文件或文件夹
        String emptyDir = "";
        String nfsBucket = minioConfig.getNfsRootPath() + minioConfig.getBucketName() + StrUtil.SLASH;
        sourcePath = sourcePath.endsWith(StrUtil.SLASH) ? sourcePath : sourcePath + StrUtil.SLASH;
        //校验回收文件是否存在以及回收文件必须至少在当前环境目录下还有一层目录，如：/nfs/dubhe-test/xxxx/
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
            LogUtil.error(LogEnum.BIZ_DATASET, "minio 文件流删除文件失败: {} ", e);
        }
    }

    /**
     * 判断执行服务器命名是否成功退出
     *
     * @param process Process对象
     * @return boolean linux命令是否执行成功正常退出
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
                LogUtil.error(LogEnum.BIZ_DATASET, "文件流删除文件失败: {} ", errMessage.toString());
                recycleIsOk = false;
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "文件流删除文件失败: {} ", e);
            recycleIsOk = false;
        } finally {
            IOUtil.close(reader, stream);
        }
        return recycleIsOk;
    }


    /**
     * 替换路劲中多余的 "/"
     *
     * @param path 路径
     * @return String
     */
    public String formatPath(String path) {
        if (!StringUtils.isEmpty(path)) {
            return path.replaceAll("///*", FILE_SEPARATOR);
        }
        return path;
    }


}
