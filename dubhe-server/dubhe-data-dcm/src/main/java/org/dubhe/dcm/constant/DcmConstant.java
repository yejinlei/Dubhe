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

package org.dubhe.dcm.constant;

/**
 * @description 常量
 * @date 2020-04-10
 */
public class DcmConstant {

    public static final String MODULE_URL_PREFIX = "/";

    /**
     *  medicineId
     */
    public static final String MEDICINE_ID = "medicineId";

    /**
     *  count
     */
    public static final String COUNT ="count";

    /**
     *  status
     */
    public static final String STATUS ="status";

    /**
     * dcm文件路径
     */
    public static final String DCM_ANNOTATION_PATH = "dataset/dcm/";

    /**
     * dcm自动标注文件名
     */
    public static final String DCM_ANNOTATION = "/annotation/finished_annotation.json";

    /**
     * dcm自动合并标注文件名
     */
    public static final String DCM_MERGE_ANNOTATION = "/annotation/merge_annotation.json";

    /**
     * 文件分隔符
     */
    public static final String DCM_FILE_SEPARATOR = "/";

    /**
     * seriesInstanceUID
     */
    public static final String SERIES_INSTABCE_UID = "seriesInstanceUID";

    /**
     * StudyInstanceUID
     */
    public static final String STUDY_INSTANCE_UID = "StudyInstanceUID";

    /**
     * annotation
     */
    public static final String ANNOTATION = "annotation";

    /**
     * dcm文件上传
     */
    public static final String DCM_UPLOAD = "ssh %s@%s \"docker run --rm -v %s:/nfs dcm4che/dcm4che-tools:5.10.5 storescu -c DCM4CHEE@%s:%s %s\"";

}
