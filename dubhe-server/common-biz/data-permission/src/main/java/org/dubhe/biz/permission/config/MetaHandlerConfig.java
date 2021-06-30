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

package org.dubhe.biz.permission.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.db.constant.MetaHandlerConstant;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @description 处理新增和更新的基础数据填充，配合BaseEntity和MyBatisPlusConfig使用
 * @date 2020-11-26
 */
@Component
public class MetaHandlerConfig implements MetaObjectHandler {


    @Resource
    private UserContextService userContextService;
    
    /**
     * 新增数据执行
     *
     * @param metaObject 基础数据
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.isNull(getFieldValByName(MetaHandlerConstant.CREATE_TIME, metaObject))) {
            this.setFieldValByName(MetaHandlerConstant.CREATE_TIME, DateUtil.getCurrentTimestamp(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(MetaHandlerConstant.UPDATE_TIME, metaObject))) {
            this.setFieldValByName(MetaHandlerConstant.UPDATE_TIME, DateUtil.getCurrentTimestamp(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(MetaHandlerConstant.UPDATE_USER_ID, metaObject))) {
            this.setFieldValByName(MetaHandlerConstant.UPDATE_USER_ID, userContextService.getCurUserId(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(MetaHandlerConstant.CREATE_USER_ID, metaObject))) {
            this.setFieldValByName(MetaHandlerConstant.CREATE_USER_ID, userContextService.getCurUserId(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(MetaHandlerConstant.ORIGIN_USER_ID, metaObject))) {
            this.setFieldValByName(MetaHandlerConstant.ORIGIN_USER_ID, userContextService.getCurUserId(), metaObject);
        }
        if (Objects.isNull(getFieldValByName(MetaHandlerConstant.DELETED, metaObject))) {
            this.setFieldValByName(MetaHandlerConstant.DELETED, false, metaObject);
        }
    }

    /**
     * 更新数据执行
     *
     * @param metaObject 基础数据
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(MetaHandlerConstant.UPDATE_TIME, DateUtil.getCurrentTimestamp(), metaObject);
        this.setFieldValByName(MetaHandlerConstant.UPDATE_USER_ID, userContextService.getCurUserId(), metaObject);
    }


}
