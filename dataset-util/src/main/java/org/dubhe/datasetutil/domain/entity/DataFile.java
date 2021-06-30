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
package org.dubhe.datasetutil.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.datasetutil.common.base.BaseEntity;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.common.constant.FileStateCodeConstant;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * @description 文件类
 * @date 2020-09-17
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@TableName("data_file")
@Data
public class DataFile extends BaseEntity implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 数据集id
     */
    private Long datasetId;

    /**
     * url
     */
    private String url;

    /**
     * 文件类型
     */
    private Integer fileType;

    /**
     * 父id
     */
    private Long pid;

    /**
     * 帧间隔
     */
    private Integer frameInterval;

    /**
     * 增强类型
     */
    private Integer enhanceType;

    /**
     * 宽
     */
    private Integer width;

    /**
     * 高
     */
    private Integer height;

    /**
     * 拥有人id
     */
    private Long originUserId;

    public DataFile() {
    }

    /**
     * 插入文件表
     *
     * @param name         文件名字
     * @param datasetId    数据集id
     * @param url          文件路径
     * @param createUserId 创建人id
     * @return DataFile   file对象
     */
    public DataFile(String name, Long datasetId, String url, Long createUserId, int status) {
        this.name = name;
        this.datasetId = datasetId;
        this.url = url;
        this.status = status;
        this.setDeleted(false);
        this.originUserId = createUserId;
    }


    /**
     * 插入文件表
     *
     * @param name         文件名字
     * @param datasetId    数据集id
     * @param url          文件路径
     * @param createUserId 创建人id
     * @param status       状态
     * @param fileType     文件类型
     * @param pid          父文件ID
     * @param originUserId 资源拥有者ID
     * @return DataFile   file对象
     */
    public DataFile(String name, Long datasetId, String url, Long createUserId,  int status, int fileType, long pid, long originUserId) {
        this.name = name;
        this.datasetId = datasetId;
        this.url = url;
        this.status = status;
        this.setDeleted(false);
        this.setCreateUserId(createUserId);
        this.fileType = fileType;
        this.pid = pid;
        this.originUserId = originUserId;
    }

    /**
     * 插入文件表
     *
     * @param name         文件名字
     * @param datasetId    数据集id
     * @param url          文件路径
     * @param createUserId 创建人id
     * @param read         文件宽高
     * @param status       状态
     * @param fileType     文件类型
     * @param pid          父文件ID
     * @param originUserId 资源拥有者ID
     * @return DataFile   file对象
     */
    public DataFile(String name, Long datasetId, String url, Long createUserId, BufferedImage read, int status, int fileType, long pid, long originUserId) {
        this.name = name;
        this.datasetId = datasetId;
        this.url = url;
        this.status = status;
        this.setDeleted(false);
        this.setCreateUserId(createUserId);
        this.width = read.getWidth();
        this.height = read.getHeight();
        this.fileType = fileType;
        this.pid = pid;
        this.originUserId = originUserId;
    }

}
