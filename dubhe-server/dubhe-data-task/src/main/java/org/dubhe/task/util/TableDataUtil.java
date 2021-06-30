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

package org.dubhe.task.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.csvreader.CsvReader;
import org.apache.logging.log4j.util.Strings;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.domain.entity.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @description csv、excel操作工具类
 * @date 2021-03-24
 */
@Component
public class TableDataUtil {

    @Value("${minio.bucketName}")
    private String bucketName;
    @Autowired
    private MinioUtil minioUtil;

    /**
     * excel文档数据读取
     *
     * @param filePath      源文件地址
     * @param fileName      源文件名称
     * @param minioFilePath minio上存储文件地址
     * @param mergeColumn   需要合并的行
     * @param excludeHeader 是否排除头 true or false
     * @return List<File> 读取到的所有行
     * @throws Exception
     */
    public List<File> excelRead(String filePath, String fileName, String minioFilePath, String mergeColumn, boolean excludeHeader) throws Exception {
        LogUtil.info(LogEnum.BIZ_DATASET, "excel read param filePath:{} fileName:{} minioFilePath:{} mergeColumn:{} excludeHeader:{}",
                filePath,fileName,minioFilePath,mergeColumn,excludeHeader);
        List<File> list = new ArrayList<>();
        Sheet sheet = new Sheet(1,excludeHeader?1:0);
        List<Object> readList = com.alibaba.excel.EasyExcelFactory.read(new FileInputStream(filePath), sheet);
        LogUtil.info(LogEnum.BIZ_DATASET, "excel read column size {}", readList.size());
        List<String> column = Arrays.asList(mergeColumn.split(","));
        for (int i = 0; i < readList.size(); i++) {
            List<String> result = new ArrayList<>();
            List<String> data = (ArrayList<String>)readList.get(i);
            for (String index : column) {
                if(data.size() > Integer.valueOf(index) && StringUtils.isNotEmpty(data.get(Integer.valueOf(index)))) {
                    result.add(data.get(Integer.valueOf(index)));
                }
            }
            if (result.size() > 0) {
                try{
                    String newFileName = fileName + "_" + i + ".txt";
                    String fullFilePath = minioFilePath + newFileName;
                    minioUtil.writeString(bucketName, fullFilePath, Strings.join(result, ','));
                    //保存到list用于数据库导入
                    list.add(File.builder().build().setName(newFileName)
                            .setUrl(bucketName + "/" + fullFilePath));
                } catch (Exception e) {
                    LogUtil.error(LogEnum.BIZ_DATASET, "csv file read error {}", e);
                }
            }
        }
        return list;
    }

    /**
     * csv文档数据读取
     *
     * @param filePath      源文件地址
     * @param fileName      源文件名称
     * @param minioFilePath minio上存储文件地址
     * @param mergeColumn   需要合并的行
     * @param excludeHeader 是否排除头 true or false
     * @return List<File> 读取到的所有行
     * @return
     */
    public List<File> csvRead(String filePath, String fileName, String minioFilePath, String mergeColumn, boolean excludeHeader) {
        List<File> list = new ArrayList<>();
        try {
            CsvReader csvReader = new CsvReader(new InputStreamReader(new FileInputStream(new java.io.File(filePath)),"UTF-8"));
            int currentNum = 0;
            if (excludeHeader) {
                csvReader.readHeaders();
                currentNum ++;
            }
            while (csvReader.readRecord()) {
                String line = csvReader.getRawRecord();
                if (StringUtils.isNotEmpty(line)) {
                    String[] lineArray = line.split(",");
                    List<String> content = new ArrayList<>();
                    for (String column : mergeColumn.split(",")) {
                        if (lineArray.length - 1 >= Integer.parseInt(column) && StringUtils.isNotEmpty(lineArray[Integer.parseInt(column)])) {
                            content.add(lineArray[Integer.parseInt(column)]);
                        }
                    }
                    if (content.size() > 0) {
                        String newFileName = fileName + "_" + currentNum + ".txt";
                        String fullFilePath = minioFilePath + newFileName;
                        minioUtil.writeString(bucketName, fullFilePath, Strings.join(content, ','));
                        //保存到list用于数据库导入
                        list.add(File.builder().build().setName(newFileName)
                                .setUrl(bucketName + "/" + fullFilePath));
                    }
                }
                currentNum ++;
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "read csv error {}", e);
        }
        return list;
    }

}
