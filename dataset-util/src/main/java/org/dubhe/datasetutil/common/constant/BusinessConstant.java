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
package org.dubhe.datasetutil.common.constant;

import org.dubhe.datasetutil.common.base.MagicNumConstant;

/**
 * @description 常量
 * @date 2020-10-19
 */
public class BusinessConstant {

    private BusinessConstant(){}

    /**
     * 分表业务编码 - 文件表
     */
    public static final String DATA_FILE = "DATA_FILE";

    /**
     * 分表业务编码 - 文件版本关系表
     */
    public static final String DATA_VERSION_FILE = "DATA_VERSION_FILE";

    /**
     * 分表业务编码 - 文件版本标签关系表
     */
    public static final String DATA_FILE_ANNOTATION = "DATA_FILE_ANNOTATION";

    /**
     * 图片文件路径
     */
    public static final String IMAGE_ORIGIN = "origin";
    /**
     * 标注文件路径
     */
    public static final String ANNOTATION = "annotation";
    /**
     * 版本文件路径
     */
    public static final String VERSION_FILE = "versionFile";
    /**
     * 视频文件路径
     */
    public static final String VIDEO = "video";
    /**
     * 版本文件V0001路径
     */
    public static final String V0001 = "V0001";
    /**
     * 版本文件ofrecord路径
     */
    public static final String OFRECORD = "ofrecord";
    /**
     * 版本文件train路径
     */
    public static final String TRAIN = "train";
    /**
     * 标签文件路径
     */
    public static final String LABEL = "label";
    /**
     * 分隔符
     */
    public static final String FILE_SEPARATOR = "/";
    /**
     * 后缀.
     */
    public static final String SPOT = ".";
    /**
     * 逗号,
     */
    public static final String COMMA = ",";

    /**
     * JSON后缀名
     */
    public static final String SUFFIX_JSON = ".JSON";

    /**
     * SQL后缀名
     */
    public static final String SUFFIX_SQL = ".sql";
    /**
     * minio根目录
     */
    public static final String MINIO_ROOT_PATH = "dataset";
    /**
     * 下划线
     */
    public static final String UNDERLINE = "_";

    /**
     * 分段ID范围区间 50表示 50间隔ID存一张表
     */
    public static final long INTERVAL_NUMBER = MagicNumConstant.FIFTY_LONG;
    /**
     * 分批长度
     */
    public static final int SUB_LENGTH = MagicNumConstant.FIVE_THOUSAND;

    /**
     * 字母Y
     */
    public static final String Y = "Y";

    public static final String DEFAULT_VERSION = "V0001";

    /**
     * 版本文件表
     */
    public static final String DATA_DATASET_VERSION_FILE = "data_dataset_version_file";
    /**
     * 数据集文件表
     */
    public static final String DATASET_FILE = "data_file";

    /**
     * 文本摘要
     */
    public static final String ABSTRACT  = "abstract_";

    /**
     * 表后缀
     */
    public static final String TABLE_SUFFIX = "_1";

    /**
     * 删除服务器无效文件(大文件)
     * 示例：rsync --delete-before -d /空目录 /需要回收的源目录
     */
    public static final String DEL_COMMAND = "ssh %s@%s \"mkdir -p %s; rsync --delete-before -d %s %s; rmdir %s %s\"";

}
