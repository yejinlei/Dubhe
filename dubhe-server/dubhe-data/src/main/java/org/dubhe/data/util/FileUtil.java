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

package org.dubhe.data.util;


import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.ErrorEnum;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.service.DatasetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @description 文件工具
 * @date 2020-04-10
 */
@Component
public class FileUtil {

    @Value("${data.files.rootPath:dataset/}")
    private String datasetRootPath;
    @Resource
    @Lazy
    private DatasetService datasetService;

    @Value("${storage.file-store-root-path}")
    private String nfs;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 允许上传的文件格式
     */
    private static final String ALLOW_FILE_TYPE =".json";

    /**
     * 获取数据集根路径
     *
     * @param datasetId 数据集ID
     * @return String 数据集根路径
     */
    public String getDatasetAbsPath(Long datasetId) {
        return datasetRootPath + datasetId;
    }

    /**
     * 获取读标注文件路径 changed=1则到annotation读取否则去versionFile下读取
     *
     * @param datasetId   数据集ID
     * @param fileName    文件名称
     * @param versionName 版本名称
     * @param change      是否发生改变（true发生改变，false未改变）
     * @return 读取文件的路径
     */
    public String getReadAnnotationAbsPath(Long datasetId, String fileName, String versionName, boolean change) {
        return StringUtils.isBlank(versionName) ?
                getDatasetAbsPath(datasetId) + File.separator + Constant.DATASET_ANNOTATION_PATH + fileName :
                change ?
                        getDatasetAbsPath(datasetId) + Constant.VERSION_PATH_NAME + versionName + File.separator + Constant.DATASET_ANNOTATION_PATH + fileName :
                        getDatasetAbsPath(datasetId) + File.separator + Constant.DATASET_ANNOTATION_PATH + fileName;

    }

    /**
     * 获取写标注文件路径
     *
     * @param datasetId 数据集ID
     * @param fileName  文件名称
     * @return 写文件的路径
     */
    public String getWriteAnnotationAbsPath(Long datasetId, String fileName){
        return getDatasetAbsPath(datasetId)+File.separator + Constant.DATASET_ANNOTATION_PATH+fileName;
    }


    /**
     * 获取源文件绝对路径或源文件的文件夹绝对路径
     *
     * @param datasetId       数据集ID
     * @param fileName        文件名称
     * @param needFileName    是否需要文件名
     * @return 源文件绝对路径或源文件的文件夹绝对路径
     */
    public String getOriginFileAbsPath(Long datasetId, String fileName,boolean needFileName){
        return nfs+bucketName+File.separator + getDatasetAbsPath(datasetId)+File.separator + (needFileName?Constant.DATASET_ORIGIN_PATH+fileName:Constant.DATASET_ORIGIN_NAME);
    }

    /**
     * 获取源文件绝对路径或源文件的文件夹绝对路径
     *
     * @param url    相对路径
     * @return 源文件绝对路径或源文件绝对路径
     */
    public String getOriginFileAbsPath(String url){
        return nfs + url;
    }

    /**
     * 获取数据集指定文件标注地址(支持多版本)
     *
     * @param datasetId 数据集ID
     * @param fileName  文件名称
     * @return String 数据集指定文件标注地址(支持多版本)
     */
    public String getAnnotationAbsPath(Long datasetId, String fileName) {
        Dataset dataset = datasetService.getOneById(datasetId);
        return getAnnotationDirAbsPath(datasetId) +
                (org.springframework.util.StringUtils.isEmpty(dataset.getCurrentVersionName()) ? "" : dataset.getCurrentVersionName() + File.separator)
                + fileName;
    }

    /**
     * 获取数据集标注文件地址
     *
     * @param datasetId 数据集id
     * @return String   数据集标注文件地址
     */
    public String getAnnotationDirAbsPath(Long datasetId) {
        return getDatasetAbsPath(datasetId) + File.separator + Constant.DATASET_ANNOTATION_PATH;
    }


    /**
     * 获取标注文件绝对路径（带nfs）
     *
     * @param datasetId       数据集ID
     * @param fileName        文件名称
     * @return 源文件绝对路径
     */
    public String getNfsReadAnnotationAbsPath(Long datasetId, String fileName,String versionName,boolean change){
        return nfs+bucketName+File.separator +getReadAnnotationAbsPath(datasetId,fileName,versionName,change);
    }

    /**
     * 写标注文件的文件夹绝对路径
     *
     * @param datasetId       数据集ID
     * @return  当前数据集写标注文件的文件夹绝对路劲
     */
    public String getNfsWriteAnnotationAbsPath(Long datasetId){
        return nfs+bucketName+File.separator +getDatasetAbsPath(datasetId)+File.separator + Constant.DATASET_ANNOTATION_NAME;
    }


    /**
     * 通过本地文件访问json并读取
     *
     * @param file 读取文件
     * @return  读取的文件内容
     */
    public static String readFile(MultipartFile file){
        StringBuffer lastStr= new StringBuffer();
        BufferedReader reader=null;
        try{
            Reader br = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            reader = new BufferedReader( br);
            String tempString=null;
            while((tempString=reader.readLine())!=null){
                lastStr.append(tempString);
            }
        }catch(IOException e){
            LogUtil.error(LogEnum.BIZ_DATASET,"Class FileUtil method readFile error , error info is :{}",e);
            throw new BusinessException(ErrorEnum.NET_ERROR);
        }finally{
            if(reader!=null){
                try{
                    reader.close();
                }catch(IOException e){
                    LogUtil.error(LogEnum.BIZ_DATASET,"Class FileUtil method readFile error , error info is :{}",e);
                }
            }
        }
        return lastStr.toString();
    }

    /**
     * 文件格式/大小/属性校验
     *
     * @param file json文件
     */
    public static void checkoutFile(MultipartFile file) {
        if(Objects.isNull(file)){
            throw new BusinessException(ErrorEnum.FILE_ABSENT);
        }
        String fileName = file.getOriginalFilename();
        if(Objects.isNull(fileName)){
            throw new BusinessException(ErrorEnum.LABELGROUP_FILE_NAME_NOT_EXIST);
        }
        String lastFileName = fileName.substring(fileName.lastIndexOf("."));
        if(!ALLOW_FILE_TYPE.equals(lastFileName)){
            throw new BusinessException(ErrorEnum.LABELGROUP_JSON_FILE_ERROR);
        }else if(file.getSize() > NumberConstant.NUMBER_1024 * NumberConstant.NUMBER_1024 * NumberConstant.NUMBER_5){
            throw new BusinessException(ErrorEnum.LABELGROUP_JSON_FILE_SIZE_ERROR);
        }

    }

    /**
     * 拼接文件名称和数据集ID
     * @param datasetId 数据集ID
     * @param fileName  文件名称
     * @return  拼接后名称
     */
    public static String spliceFileNameAndDatasetId(Long datasetId, String fileName){
        return new StringBuffer(fileName).append(SymbolConstant.HYPHEN).append(datasetId.toString()).toString();
    }

    /**
     * 拼接文件名称和数据集版本
     * @param version 数据集版本号
     * @param fileName  文件名称
     * @return  拼接后名称
     */
    public static String spliceFileNameAndVersion(String version, String fileName){
        return new StringBuffer(fileName).append(SymbolConstant.HYPHEN).append(version).toString();
    }


    /**
     * 截取文件名称和数据集ID
     * @param datasetId 数据集ID
     * @param fileName  文件名称
     * @return  截取后名称
     */
    public static String interceptFileNameAndDatasetId(Long datasetId, String fileName){
        return StringUtils.substringBeforeLast(fileName, SymbolConstant.HYPHEN + datasetId);
    }

    /**
     * 截取文件名称和数据集版本号
     * @param version 数据集版本
     * @param fileName  文件名称
     * @return  截取后名称
     */
    public static String interceptFileNameAndVersion(String version, String fileName){
        return StringUtils.substringBeforeLast(fileName, SymbolConstant.HYPHEN + version);
    }



}