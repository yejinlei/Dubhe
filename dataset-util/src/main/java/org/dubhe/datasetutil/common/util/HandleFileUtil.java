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
package org.dubhe.datasetutil.common.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 文件工具类
 * @date 2020-10-15
 */
public class HandleFileUtil {

    /**
     * 读取文件内容
     *
     * @param file 文件对象
     * @return String 文件内容
     */
    public static String readFile(File file) throws IOException{
        StringBuilder stringBuffer = new StringBuilder();
        LineIterator fileContext =  FileUtils.lineIterator(file,"UTF-8");
        while (fileContext.hasNext()) {
            stringBuffer.append(fileContext.nextLine());
        }
        return stringBuffer.toString();
    }


    /**
     * 读取文件内容
     *
     * @param file 文件对象
     * @return Map<String,List<String>> 文件内容
     */
    public static List<String>  readFileInfo(File file) throws IOException{

       List<String> datasetList = new ArrayList<>();
        LineIterator fileContext =  FileUtils.lineIterator(file,"UTF-8");
        while (fileContext.hasNext()) {
            String line = fileContext.nextLine();
            if(!ObjectUtils.isEmpty(line)){
                datasetList.add(line);
            }

        }
        return datasetList;
    }


    /**
     * 获取文件名后缀名
     *
     * @param fileName 文件名
     * @return String 文件后缀名
     */
    public static String readFileSuffixName(String fileName){
        return fileName.substring(fileName.lastIndexOf("."));
    }


    /**
     * 获取文件名(踢除后缀名)
     *
     * @param fileName 文件名
     * @return String 文件名(踢除后缀名)
     */
    public static String readFileName(String fileName){
        return fileName.substring(0,fileName.lastIndexOf("."));
    }


    /**
     * 生成文件路径
     *
     * @param businessCode 业务类型
     * @return String 文件路径
     */
    public static String generateFilePath(String businessCode){
        return BusinessConstant.FILE_SEPARATOR + businessCode;
    }


    /**
     * 获取标签组名称
     *
     * @param fileName 文件名称
     * @return String 标签组名
     */
    public static String getLabelGroupName(String fileName){
        return fileName.substring(fileName.indexOf(BusinessConstant.UNDERLINE) + 1,fileName.lastIndexOf(BusinessConstant.SPOT));
    }

}
