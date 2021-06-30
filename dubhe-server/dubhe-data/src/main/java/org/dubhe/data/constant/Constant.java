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

package org.dubhe.data.constant;


import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.data.machine.constant.DataStateCodeConstant;
import org.dubhe.data.machine.constant.FileStateCodeConstant;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @description 常量
 * @date 2020-04-10
 */
public class Constant {

    public static final String MODULE_URL_PREFIX = "/";
    /**
     * 如有变更，需同时修改MVCConfig.class中的ResourceLocations
     */
    public static final String DATASET_ORIGIN_NAME = "origin";
    public static final String DATASET_ORIGIN_PATH = DATASET_ORIGIN_NAME + File.separator;
    public static final String DATASET_ANNOTATION_NAME = "annotation";
    public static final String DATASET_ANNOTATION_PATH = DATASET_ANNOTATION_NAME + File.separator;

    /**
     * 标注任务默认优先级别
     */
    public static final int DEFAULT_PRIORITY = MagicNumConstant.ZERO;
    public static final int TASK_MAX_RETRY = MagicNumConstant.THREE;
    public static final int ANNOTATION_TASK_WORKER_NUM = MagicNumConstant.ONE;

    public static final long DEFAULT_USER = MagicNumConstant.ZERO_LONG;

    /**
     * 预留的用于系统可支持的自动标注的标签id, <=RESERVED_LABEL_ID的不可用于自定义
     */
    public static final long RESERVED_LABEL_ID = MagicNumConstant.TEN_THOUSAND_LONG;

    /**
     * 自动标注需要符合的状态
     */
    public static final Set<Integer> AUTO_ANNOTATION_NEED_STATUS = new HashSet<Integer>() {{
        add(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE);
    }};

    /**
     * 自动跟踪需要符合的状态
     */
    public static final Set<Integer> AUTO_TRACK_NEED_STATUS = new HashSet<Integer>() {{
        add(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE);
        add(DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE);
    }};

    /**
     * 数据集版本规则 - 正则表达式
     */
    public static final String DATASET_VERSION_NAME_REGEXP = "^V[0-9]{4}$";

    /**
     * 数据集版本格式说明
     */
    public static final String DATASET_VERSION_NAME_REGEXP_NOTE = "版本规则: 1.满足V0001结构(V0001-V9999) " +
            "2.只能是字母、数字、下划线或者中划线组成的合法字符串长度限制8个字符";

    /**
     * 默认版本号
     */
    public static final String DEFAULT_VERSION = "V0001";

    /**
     * 数据集版本号前缀
     */
    public static final String DATASET_VERSION_PREFIX = "V";

    public static final String DATA_TYPE_RULE = "数据类型参数不对,请使用: 0-图片 1-视频 2-文本 3-表格 4-语音 100-自定义导入";

    public static final String ANNOTATE_TYPE_RULE = "数据标注类型参数不对, 请使用: 1-目标检测 2-图像分类 5-目标跟踪 6-文本分类 7-语义分割 8-声音分类 9-语音识别 100-自定义导入";

    /**
     * 排序规则
     */
    public static final String SORT_ASC = "asc";

    public static final String SORT_DESC = "desc";

    /**
     * ZIP压缩文件后缀
     */
    public static final String COMPRESS_ZIP = ".zip";

    /**
     * 视频文件pid
     */
    public static final long PID_OF_VIDEO = MagicNumConstant.ZERO_LONG;
    /**
     * id
     **/
    public static final String ID = "id";

    /**
     * 版本状态：未删除
     */
    public static final int NOT_DELETED = MagicNumConstant.ZERO;

    /**
     * 数据转换url
     */
    public static final String OFRECORD = "ofrecord";

    /**
     * 训练所需路径文件夹名称
     */
    public static final String TRAIN = "train";

    /**
     * 分页内容
     */
    public static final String RESULT = "result";

    /**
     * 整数匹配
     */
    public static final Pattern PATTERN_NUM = Pattern.compile("^[-\\+]?[\\d]*$");

    /**
     * 数据集名称路径
     */
    public static final String DATASET_PATH_NAME = "dataset" + File.separator;

    /**
     * 版本文件路径
     */
    public static final String VERSION_PATH_NAME = File.separator + "versionFile" + File.separator;

    /**
     * 标注文件路径
     */
    public static final String ANNOTATION_PATH_NAME = File.separator + "annotation";

    /**
     * 当前目录下的全部
     */
    public static final String ALL_IN_THE_CURRENT_DIRECTORY = File.separator + "*";

    /**
     * 复制命令(带创建文件夹)
     */
    public static final String COMMAND = "ssh %s@%s \"mkdir -p %s;cp %s/* %s;mkdir -p %s;cp %s/* %s\"";

    /**
     * 复制命令
     */
    public static final String COPY_COMMAND = "ssh %s@%s \"cp -f %s %s\"";

    /**
     * 文件信息已改变
     */
    public static final int CHANGED = MagicNumConstant.ONE;

    /**
     * 文件信息未改变
     */
    public static final int UNCHANGED = MagicNumConstant.ZERO;

    /**
     * 转换成功
     */
    public static final String CONVERSION_SUCCESS = "ok";

    /**
     * 图片文件夹名称
     */
    public static final String ORIGIN_DIRECTORY = "origin";

    /**
     * 标注信息文件夹名称
     */
    public static final String ANNOTATION_DIRECTORY = "annotation";

    /**
     * 数据集文件夹名称
     */
    public static final String DATASET_DIRECTORY = "dataset";

    /**
     * 临时文件
     */
    public static final String UPLOAD_TEMP = File.separator + "upload-temp";


    /**
     * 分表业务编码 - 文件表
     */
    public static final String DATA_FILE = "DATA_FILE";

    /**
     * 分表业务编码 - 文件版本关系表
     */
    public static final String DATA_VERSION_FILE = "DATA_VERSION_FILE";

    /**
     * 分表业务编码 - 文件版本关系表
     */
    public static final String DATA_FILE_ANNOTATION = "DATA_FILE_ANNOTATION";


    /**
     *  数据集预置标签组默认ID COCO
     */
    public static final Long COCO_ID = 1L;

    /**
     *  数据集预置标签组默认ID ImageNet
     */
    public static final Long IMAGENET_ID = 2L;

    /**
     *  文本数据集预置标签组ID
     */
    public static final Long TXT_PREPARE_ID = 748L;


    /**
     *  大数据默认删除数量
     */
    public static final int LIMIT_NUMBER = 10000;



    /**
     * redis 预置标签key
     */
    public final static String DATASET_LABEL_PUB_KEY = "dateset:label:pub";



    /**
     *  数据集预置标签组类型
     */
    public static final Integer COMMON_LABEL_GROUP_TYPE = 1;



    /**
     *  抽象名称前缀
     */
    public static final String ABSTRACT_NAME_PREFIX = "abstract_";



}
