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
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.config.ImageConfig;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.common.enums.LogEnum;
import org.dubhe.datasetutil.common.util.*;
import org.dubhe.datasetutil.common.constant.DataStateCodeConstant;
import org.dubhe.datasetutil.common.constant.FileStateCodeConstant;
import org.dubhe.datasetutil.common.enums.DatatypeEnum;
import org.dubhe.datasetutil.common.exception.ImportDatasetException;
import org.dubhe.datasetutil.common.util.*;
import org.dubhe.datasetutil.domain.entity.DataFile;
import org.dubhe.datasetutil.domain.entity.DataVersionFile;
import org.dubhe.datasetutil.domain.entity.Dataset;
import org.dubhe.datasetutil.service.DataFileService;
import org.dubhe.datasetutil.service.DataVersionFileService;
import org.dubhe.datasetutil.service.DatasetService;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @description 上传图片工具类
 * @date 2020-09-17
 */
@Slf4j
@Component
public class DatasetImageUploadHandle {

    /**
     * esSearch索引
     */
    @Value("${es.index}")
    private String esIndex;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private DataFileService dataFileService;

    @Autowired
    private DataVersionFileService dataVersionFileService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private GeneratorKeyUtil generatorKeyUtil;

    @Autowired
    private ImageConfig imageConfig;

    @Autowired
    private BulkProcessor bulkProcessor;

    /**
     * 缺陷图片集合
     */
    public final List<String> defectsFile = new ArrayList<>();

    /**
     * 上传图片
     *
     * @param scanner 输入
     */
    public void importPicture(Scanner scanner) throws Exception {
        Dataset dataset = verificationDatasetId(scanner);
        String imagePath = verificationFilePath(scanner,dataset);
        try{
            execute(imagePath, dataset.getId());
        } catch (Exception e) {
            log.error("");
            PrintUtils.printLine("  Error：" + e.getMessage(), PrintUtils.RED);
            log.error("");
        }
    }

    /**
     * 启动线程
     *
     * @param imagePath 本地文件地址
     * @param datasetId 数据集Id
     */
    public void execute(String imagePath, Long datasetId) throws Exception {
        List<String> fileNames = FileUtil.listFileNames(imagePath);
        log.warn("需要处理文件:【" + fileNames.size() + "】个文件");
        String fileBaseDir = BusinessConstant.MINIO_ROOT_PATH + BusinessConstant.FILE_SEPARATOR + datasetId
                + BusinessConstant.FILE_SEPARATOR + BusinessConstant.IMAGE_ORIGIN + BusinessConstant.FILE_SEPARATOR;
        int oneSize = ThreadUtils.createThread(fileNames.size());
        int batchNumber = MagicNumConstant.ZERO;
        //初始化进度条
        ProcessBarUtil.initProcess("图片导入", (long) fileNames.size());
        if (fileNames.size() > MagicNumConstant.TEN_THOUSAND) {
            log.warn("........系统处理中.........");
            List<List<String>> partitionList = Lists.partition(fileNames, MagicNumConstant.FIVE_THOUSAND);
            for (List<String> imageFileNameList1 : partitionList) {
                batchNumber++;
                dealFileList(imageFileNameList1, oneSize, fileBaseDir, imagePath, datasetId, batchNumber);
            }
        } else {
            log.warn("........系统处理中.........");
            batchNumber++;
            dealFileList(fileNames, oneSize, fileBaseDir, imagePath, datasetId, batchNumber);
        }
        log.warn("");
        PrintUtils.printLine("  Success: 执行成功  ", PrintUtils.GREEN);
        log.warn("");
        System.out.println("#   是否结束? Y / N  #");
        Scanner scannerExit = new Scanner(System.in);
        if (BusinessConstant.Y.toLowerCase().equals(scannerExit.nextLine().toLowerCase())) {
            System.exit(MagicNumConstant.ZERO);
        }
    }


    /**
     * @param fileNames   图片集合
     * @param oneSize     每次处理次数
     * @param fileBaseDir 文件根目录
     * @param imagePath   图片文件路径
     * @param datasetId   数据集ID
     * @throws Exception
     */
    public void dealFileList(List<String> fileNames, int oneSize, String fileBaseDir, String imagePath, Long datasetId, int batchNumber) throws Exception {
        int dealSize = MagicNumConstant.ZERO;
        Dataset dataset = datasetService.queryDatasetById(datasetId);
        List<String> need = new ArrayList<>();
        List<Callable<Integer>> partitions = new ArrayList<>();
        //初始化进度条
        for (int i = 0; i < fileNames.size(); i++) {
            String suffixFileName = fileNames.get(i).substring(fileNames.get(i).lastIndexOf(BusinessConstant.SPOT));
            if(dataset.getDataType().equals(DatatypeEnum.TXT.getValue())){
                if (imageConfig.getTxtFormat().contains(suffixFileName.toLowerCase())) {
                    need.add(fileNames.get(i));
                    if (need.size() == oneSize || i == fileNames.size() - MagicNumConstant.ONE) {
                        List<String> now = new ArrayList<>(need);
                        dealSize += now.size();
                        need.clear();
                        partitions.add(() -> run(datasetId, now, fileBaseDir, imagePath));
                    }
                }
            } else {
                if (imageConfig.getImageFormat().contains(suffixFileName.toLowerCase())) {
                    need.add(fileNames.get(i));
                    if (need.size() == oneSize || i == fileNames.size() - MagicNumConstant.ONE) {
                        List<String> now = new ArrayList<>(need);
                        dealSize += now.size();
                        need.clear();
                        partitions.add(() -> run(datasetId, now, fileBaseDir, imagePath));
                    }
                }
            }
        }
        ThreadUtils.runMultiThread(partitions);
        if (!CollectionUtils.isEmpty(defectsFile)) {
            log.error("");
            log.warn("#-------------系统共排查出缺陷文件【" + defectsFile.size() + "】个-------------#");
            log.error("");
            log.warn("缺陷文件列表 " + defectsFile.toString() + "");
            log.error("");
            defectsFile.clear();
        }


    }


    /**
     * 插入数据库数据
     *
     * @param datasetId   数据集Id
     * @param fileNames   文件Name
     * @param fileBaseDir 文件路径
     * @param imagePath   文件地址
     * @return Integer    成功数量
     */
    public Integer run(Long datasetId, List<String> fileNames, String fileBaseDir, String imagePath) {
        Integer success = MagicNumConstant.ZERO;
        Dataset dataset = datasetService.findCreateUserIdById(datasetId);
        List<DataFile> dataFiles = new ArrayList<>();
        List<DataVersionFile> dataVersionFiles = new ArrayList<>();
        for (int i = 0; i < fileNames.size(); i++) {
            try {
                String fileName = StringUtils.substring(fileNames.get(i), MagicNumConstant.ZERO, fileNames.get(i).lastIndexOf(BusinessConstant.SPOT)) + System.nanoTime();
                String suffixFileName = fileNames.get(i).substring(fileNames.get(i).lastIndexOf(BusinessConstant.SPOT));
                minioUtil.upLoadFile(imagePath + BusinessConstant.FILE_SEPARATOR + fileNames.get(i), fileBaseDir + fileName + suffixFileName);
                DataFile dataFile = new DataFile(fileName, datasetId, minioUtil.getUrl(fileBaseDir + fileName + suffixFileName),
                        dataset.getCreateUserId(), FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE);

                if (dataset.getDataType().compareTo(DatatypeEnum.IMAGE.getValue()) == 0) {
                    BufferedImage read;
                    try {
                        read = ImageIO.read(new File(imagePath + BusinessConstant.FILE_SEPARATOR + fileNames.get(i)));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        defectsFile.add(fileNames.get(i));
                        throw new ImportDatasetException("该图片文件内部错误 " + fileNames.get(i) + ",请重新审核后再去上传此图片,当前已经跳过此图片");
                    }
                    dataFile.setWidth(read.getWidth());
                    dataFile.setHeight(read.getHeight());
                }
                success++;
                dataFiles.add(dataFile);
                if (dataFiles.size() % MagicNumConstant.FIVE_HUNDRED == MagicNumConstant.ZERO || i == fileNames.size() - MagicNumConstant.ONE) {
                    Queue<Long> dataFileIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_FILE, dataFiles.size());
                    for (DataFile dataFileEntity : dataFiles) {
                        dataFileEntity.setId(dataFileIds.poll());
                    }
                    dataFileService.saveBatchDataFile(dataFiles);
                    for (DataFile file : dataFiles) {
                        dataVersionFiles.add(new DataVersionFile(datasetId, file.getId(), DataStateCodeConstant.NOT_ANNOTATION_STATE, MagicNumConstant.ZERO, file.getName()));
                        if(dataset.getDataType().equals(DatatypeEnum.TXT.getValue())){
                            try{
                                String bucketName = StringUtils.substringBefore(file.getUrl(),"/");
                                String fullFilePath = StringUtils.substringAfter(file.getUrl(), "/");
                                String content = minioUtil.readString(bucketName, fullFilePath);
                                Map<String, String> jsonMap = new HashMap<>();
                                jsonMap.put("content",content);
                                jsonMap.put("name", file.getName());
                                jsonMap.put("status",FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE.toString());
                                jsonMap.put("datasetId",dataset.getId().toString());
                                jsonMap.put("createUserId",file.getCreateUserId()==null?null:file.getCreateUserId().toString());
                                jsonMap.put("createTime",file.getCreateTime()==null?null:file.getCreateTime().toString());
                                jsonMap.put("updateUserId",file.getUpdateUserId()==null?null:file.getUpdateUserId().toString());
                                jsonMap.put("updateTime",file.getUpdateTime()==null?null:file.getUpdateTime().toString());
                                jsonMap.put("fileType",file.getFileType()==null?null:file.getFileType().toString());
                                jsonMap.put("enhanceType",file.getEnhanceType()==null?null:file.getEnhanceType().toString());
                                jsonMap.put("originUserId",file.getOriginUserId().toString());
                                jsonMap.put("versionName", StringUtils.isEmpty(dataset.getCurrentVersionName())?"V0000" : dataset.getCurrentVersionName());
                                bulkProcessor.add(new IndexRequest(esIndex, "_doc", file.getId().toString()).source(jsonMap));
                            } catch (Exception e){
                                LogUtil.error(LogEnum.BIZ_DATASET, "上传es失败: {} ", e);
                            }
                        }
                    }
                    if(dataset.getDataType().equals(DatatypeEnum.TXT.getValue())){
                        bulkProcessor.flush();
                    }
                    Queue<Long> dataFileVersionIds = generatorKeyUtil.getSequenceByBusinessCode(BusinessConstant.DATA_VERSION_FILE, dataVersionFiles.size());
                    for (DataVersionFile dataVersionFile : dataVersionFiles) {
                        dataVersionFile.setId(dataFileVersionIds.poll());
                    }
                    dataVersionFileService.saveBatchDataFileVersion(dataVersionFiles);
                    ProcessBarUtil.processBar01((long) dataVersionFiles.size());
                    dataVersionFiles.clear();
                    dataFiles.clear();
                }
            } catch (Exception e) {
                log.error(fileNames.get(i) + "{}", e);
                log.error("运行异常: {}", e.getMessage());
            }
        }
        return success;
    }


    /**
     * 校验数据集ID
     *
     * @param scanner 控制台输入参数
     * @return Dataset 数据集
     */
    public Dataset verificationDatasetId(Scanner scanner) {
        boolean flag = false;
        Dataset dataset = new Dataset();
        while (!flag) {
            System.out.println(" ");
            System.out.println("# 请输入数据集ID #");
            String datasetIdStr = scanner.nextLine();
            long datasetId;
            try {
                datasetId = Long.parseLong(datasetIdStr.trim());
            } catch (Exception e) {
                log.error("");
                PrintUtils.printLine("  Error: 数据集ID非法,请重新输入", PrintUtils.RED);
                log.error("");
                continue;
            }
            dataset = datasetService.findDatasetByIdNormal(datasetId);
            if (dataset == null) {
                log.error("");
                PrintUtils.printLine("  Error: 数据集ID不存在，请重新输入", PrintUtils.RED);
                log.error("");
                continue;
            } else {
                flag = true;
            }
        }
        return dataset;
    }

    /**
     * 校验文件路径及格式
     *
     * @param scanner 输入控制台
     * @param dataset 数据集
     * @return String 字符串
     */
    public String verificationFilePath(Scanner scanner,Dataset dataset) {
        boolean flag = false;
        String filePath = "";
        while (!flag) {
            System.out.println(" ");
            System.out.println("# 请输入待上传本地文件的绝对路径 #");
            filePath = scanner.nextLine();
            File file = new File(filePath.trim());
            if (!file.exists()) {
                log.error("");
                PrintUtils.printLine("  【" + filePath + "】文件路径不存在,请重新输入", PrintUtils.RED);
                log.error("");
                continue;
            }
            File fileNames = new File(filePath);
            File[] imageFiles = fileNames.listFiles();
            if (imageFiles == null || imageFiles.length == MagicNumConstant.ZERO) {
                log.error("");
                PrintUtils.printLine("  【" + filePath + "】目录下不存在文件 ", PrintUtils.RED);
                log.error("");
                continue;
            } else {
                flag = true;
            }
        }

        return filePath;
    }

}
