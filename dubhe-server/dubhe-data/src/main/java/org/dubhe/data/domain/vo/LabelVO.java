package org.dubhe.data.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 标签
 * @date 2020-10-14
 */
@Data
@Builder
public class LabelVO implements Serializable {

    private Long id;

    private String name;

    private String color;
}
