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

package org.dubhe.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.dubhe.constant.StringConstant;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.enums.SwitchEnum;
import org.dubhe.utils.DateUtil;
import org.dubhe.utils.JwtUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @description 处理新增和更新的基础数据填充，配合BaseEntity和MyBatisPlusConfig使用
 * @date 2020-06-10
 */
@Component
public class MetaHandlerConfig implements MetaObjectHandler {


    /**
     * 新增数据执行
     *
     * @param metaObject 基础数据
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.isNull(getFieldValByName(StringConstant.CREATE_TIME, metaObject))) {
            this.setFieldValByName(StringConstant.CREATE_TIME, DateUtil.getCurrentTimestamp(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(StringConstant.UPDATE_TIME, metaObject))) {
            this.setFieldValByName(StringConstant.UPDATE_TIME, DateUtil.getCurrentTimestamp(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(StringConstant.UPDATE_USER_ID, metaObject))) {
            this.setFieldValByName(StringConstant.UPDATE_USER_ID, getUserId(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(StringConstant.CREATE_USER_ID, metaObject))) {
            this.setFieldValByName(StringConstant.CREATE_USER_ID, getUserId(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(StringConstant.ORIGIN_USER_ID, metaObject))) {
            this.setFieldValByName(StringConstant.ORIGIN_USER_ID, getUserId(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(StringConstant.DELETED, metaObject))) {
            this.setFieldValByName(StringConstant.DELETED, SwitchEnum.getBooleanValue(SwitchEnum.OFF.getValue()), metaObject);
        }
    }

    /**
     * 更新数据执行
     *
     * @param metaObject 基础数据
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (Objects.isNull(getFieldValByName(StringConstant.UPDATE_TIME, metaObject))) {
            this.setFieldValByName(StringConstant.UPDATE_TIME, DateUtil.getCurrentTimestamp(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(StringConstant.UPDATE_USER_ID, metaObject))) {
            this.setFieldValByName(StringConstant.UPDATE_USER_ID, getUserId(), metaObject);
        }

    }


    /**
     * 获取用户ID
     *
     * @return
     */
    private Long getUserId() {
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        return Objects.isNull(userDTO) ? null : userDTO.getId();
    }
}
