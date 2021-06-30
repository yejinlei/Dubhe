package org.dubhe.datasetutil.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.dubhe.datasetutil.common.base.BaseEntity;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @description TODO
 * @date 2021-03-23
 */
@Data
@TableName("data_dataset_version")
public class DatasetVersion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long datasetId;

    private Long teamId;

    private String versionName;

    private String versionNote;

    private String versionSource;

    private String versionUrl;

    private Integer dataConversion;

    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Boolean deleted = false;

    private Long originUserId;

    public DatasetVersion() {}

    public DatasetVersion(Long datasetId, String versionName, String versionNote) {
        this.datasetId = datasetId;
        this.versionName = versionName;
        this.setCreateUserId(0L);
        this.setCreateTime(new Timestamp(System.currentTimeMillis()));
        this.versionUrl = "dataset/"+datasetId +"/versionFile/"+versionName;
        this.dataConversion = 2;
        this.originUserId = 0L;
        this.versionNote = versionNote;
    }

}
